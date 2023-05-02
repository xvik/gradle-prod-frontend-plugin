package ru.vyarus.gradle.frontend.core.model.root;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.core.model.HtmlPage;
import ru.vyarus.gradle.frontend.core.model.OptimizedResource;
import ru.vyarus.gradle.frontend.core.stat.Stat;
import ru.vyarus.gradle.frontend.core.OptimizationFlow;
import ru.vyarus.gradle.frontend.core.info.root.RootResourceInfo;
import ru.vyarus.gradle.frontend.core.util.DigestUtils;
import ru.vyarus.gradle.frontend.core.util.FileUtils;
import ru.vyarus.gradle.frontend.core.util.ResourceLoader;
import ru.vyarus.gradle.frontend.core.util.SizeFormatter;
import ru.vyarus.gradle.frontend.core.util.SourceMapUtils;
import ru.vyarus.gradle.frontend.core.util.minify.MinifyResult;
import ru.vyarus.gradle.frontend.core.util.minify.ResourceMinifier;

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
        final String integrity = element.attr("integrity");
        return integrity.isEmpty() ? null : integrity;
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
                            final String validSri = DigestUtils.buildSri(file, alg);
                            System.out.println("Loaded file deleted because of integrity tag validation fail: "
                                    + file.getAbsolutePath());
                            // delete invalid file
                            file.delete();
                            throw new IllegalStateException("Integrity check failed for downloaded file " + target
                                    + ":\n\tdeclared: " + getIntegrity() + "\n\tactual: " + validSri);
                        }
                        System.out.println("Integrity check for " + target + " OK");
                    }
                    // update target
                    changeTarget(FileUtils.relative(html.getFile(), file));

                    // removing crossorigin and integrity attributes (e.g. bootstrap example suggest using them)
                    if (element.hasAttr("crossorigin")) {
                        element.removeAttr("crossorigin");
                        recordChange("crossorigin removed");
                    }
                    if (element.hasAttr(INTEGRITY_ATTR)) {
                        element.removeAttr(INTEGRITY_ATTR);
                        recordChange("integrity removed");
                    }
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
                        + ") not found: optimizations would not be applied");

                ignore("not found");
            } else if (getIntegrity() != null) {
                // validate local integrity
                if (!DigestUtils.validateSriToken(file, getIntegrity())) {
                    final String alg = DigestUtils.parseSri(getIntegrity()).getAlg();
                    throw new IllegalStateException("Integrity check failed for file " + target
                            + ":\n\tdeclared: " + getIntegrity() + "\n\tactual: " + DigestUtils.buildSri(file, alg));
                }
                System.out.println("Integrity check for " + target + " OK");
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
    }

    public void minify() {
        if (isIgnored() || file.getName().toLowerCase().contains(".min.")) {
            // already minified
            return;
        }
        long size = file.length();
        System.out.print("Minify " + FileUtils.relative(html.getBaseDir(), file));
        try {
            final MinifyResult min = getMinifier().minify(file, getSettings().isSourceMaps());
            System.out.println(", " + SizeFormatter.formatChangePercent(size, min.getMinified().length()));
            if (min.getExtraLog() != null) {
                System.out.println(min.getExtraLog());
            }
            if (min.getSourceMap() != null) {
                System.out.println("\tSource map generated: "
                        + FileUtils.relative(getHtml().getFile(), min.getSourceMap()));
            }
            SourceMapUtils.includeSources(sourceMap);
            // remove original file
            System.out.println("\tMinified file source removed: " + file.getName());
            file.delete();

            changeFile(min.getMinified());
            sourceMap(min.getSourceMap());

            recordStat(Stat.MODIFIED, min.getMinified().length());
            recordChange("minified");
        } catch (RuntimeException ex) {
            System.out.println(" FAILED");
            throw ex;
        }
    }

    public void applyIntegrity() {
        // if integrity tag exists then it is assumed to be already validated (during resolve)
        if (!isIgnored() && getIntegrity() == null) {
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
        // gzip source map file (remote source maps would contain all sources)
        if (sourceMap != null && sourceMap.exists()) {
            FileUtils.gzip(sourceMap, html.getBaseDir());
        }
    }

    protected abstract ResourceMinifier getMinifier();

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
