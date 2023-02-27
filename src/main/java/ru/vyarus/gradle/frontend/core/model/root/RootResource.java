package ru.vyarus.gradle.frontend.core.model.root;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.core.model.HtmlPage;
import ru.vyarus.gradle.frontend.core.model.OptimizedResource;
import ru.vyarus.gradle.frontend.core.stat.Stat;
import ru.vyarus.gradle.frontend.core.OptimizationFlow;
import ru.vyarus.gradle.frontend.core.info.root.RootResourceInfo;
import ru.vyarus.gradle.frontend.util.DigestUtils;
import ru.vyarus.gradle.frontend.util.FileUtils;
import ru.vyarus.gradle.frontend.util.ResourceLoader;
import ru.vyarus.gradle.frontend.util.minify.MinifyResult;

import java.io.File;

/**
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public abstract class RootResource extends OptimizedResource implements RootResourceInfo {

    public static final String INTEGRITY_ATTR = "integrity";
    protected final HtmlPage html;
    protected final Element element;
    protected File file;
    protected File sourceMap;
    protected File gzip;
    protected final String attr;
    protected final File dir;

    protected boolean remote;

    public RootResource(final HtmlPage html, final Element element, final String attr, final File dir) {
        this.html = html;
        this.element = element;
        this.attr = attr;
        this.dir = dir;
    }

    public HtmlPage getHtml() {
        return html;
    }

    public OptimizationFlow.Settings getSettings() {
        return getHtml().getSettings();
    }

    @Override
    public boolean isRemote() {
        return remote;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public String getTarget() {
        return element.attr(attr);
    }

    @Override
    public String getIntegrity() {
        return element.attr("integrity");
    }

    @Override
    public File getSourceMap() {
        return sourceMap;
    }
    @Override
    public File getGzip() {
        return gzip;
    }

    public void resolve() {
        final String target = getTarget();

        if (target.toLowerCase().startsWith("http")) {
            remote = true;
            if (getSettings().isDownloadResources()) {
                // url - just downloading it to local directory here (as-is)
                file = ResourceLoader.download(target, getSettings().isPreferMinDownload(),
                        getSettings().isDownloadSourceMaps(), dir);
                if (file == null) {
                    // leave link as is - no optimizations
                    System.out.println("WARNING: failed to download resource " + target);
                    ignore("download fail");
                } else {
                    // if integrity tag specified - validate loaded file
                    if (getIntegrity() != null) {
                        if (!DigestUtils.validateSriToken(file, getIntegrity())) {
                            final String alg = DigestUtils.parseSri(getIntegrity()).getAlg();
                            // delete invalid file
                            file.delete();
                            throw new IllegalStateException("Integrity check failed for downloaded file "+target
                                    +":\n\tdeclared: "+getIntegrity()+"\n\tactual: "+DigestUtils.buildSri(file, alg));
                        }
                        System.out.println("Integrity check for " + target + " OK");
                    }
                    // update target
                    changeTarget(FileUtils.relative(html.getFile(), file));
                }
            } else {
                ignore("remote resource");
            }
        } else {
            // local file
            file = new File(html.getHtmlDir(), FileUtils.unhash(target));
            if (!file.exists()) {
                System.out.println("WARNING: " + FileUtils.relative(html.getFile(), file) + " (referenced from "
                        + FileUtils.relative(html.getBaseDir(), html.getFile())
                        + ") not found: no optimizations would be applied");

                ignore("not found");
            }
        }

        if (file != null && file.exists()) {
            recordStat(Stat.ORIGINAL, file.length());
        }
    }

    public void changeTarget(String url) {
        final String old = element.attr(attr);
        element.attr(attr, url);
        recordChange(old + " -> " + url);

        // target change ALWAYS into local so removing crossorigin and integrity attributes
        // e.g. bootstrap example suggest using them
        if (element.hasAttr("crossorigin")) {
            element.removeAttr("crossorigin");
            recordChange("crossorigin removed");
        }
        if (element.hasAttr(INTEGRITY_ATTR)) {
            element.removeAttr(INTEGRITY_ATTR);
            recordChange("integrity removed");
        }
    }

    public void minified(final MinifyResult result) {
        if (result.isChanged(file)) {
            // might be the same file if source maps disabled
            changeFile(result.getMinified());
            sourceMap(result.getSourceMap());

            recordStat(Stat.MODIFIED, result.getMinified().length());
            recordChange("minified");
        }
    }

    public void applyIntegrity() {
        if (!isIgnored()) {
            final String token = DigestUtils.buildSri(file, "SHA-384");
            element.attr(INTEGRITY_ATTR, token);
            recordChange("integrity token applied");
        }
    }

    // IMPORTANT must be applied after possible minification (last steps!)
    public void applyMd5() {
        if (file != null && file.exists()) {
            String md5 = FileUtils.computeMd5(file);
            // md5 might be already applied
            if (!getTarget().endsWith(md5)) {
                changeTarget(FileUtils.unhash(getTarget()) + "?" + md5);
            }
        }
    }

    // IMPORTANT must be applied after possible minification (last steps!)
    public void gzip() {
        if (file != null && file.exists()) {
            gzip = FileUtils.gzip(file, html.getBaseDir());
            recordStat(Stat.GZIP, gzip.length());
        }
    }

    public abstract void minify();

    private void changeFile(final File file) {
        if (!this.file.equals(file)) {
            // assuming files are in the same directory
            String url = getTarget().replace(this.file.getName(), file.getName());
            this.file = file;
            changeTarget(url);
        }
    }

    private void sourceMap(final File file) {
        if (file != null) {
            this.sourceMap = file;
            recordChange("source map generated: " + file.getName());
        }
    }
}
