package ru.vyarus.gradle.frontend.core.model.root;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.core.OptimizationFlow;
import ru.vyarus.gradle.frontend.core.info.SizeType;
import ru.vyarus.gradle.frontend.core.info.resources.root.ResourceInfo;
import ru.vyarus.gradle.frontend.core.model.HtmlPage;
import ru.vyarus.gradle.frontend.core.model.OptimizedEntity;
import ru.vyarus.gradle.frontend.core.util.DigestUtils;
import ru.vyarus.gradle.frontend.core.util.FileUtils;
import ru.vyarus.gradle.frontend.core.util.ResourceLoader;
import ru.vyarus.gradle.frontend.core.util.SizeFormatter;
import ru.vyarus.gradle.frontend.core.util.SourceMapUtils;
import ru.vyarus.gradle.frontend.core.util.UrlUtils;
import ru.vyarus.gradle.frontend.core.util.minify.MinifyResult;
import ru.vyarus.gradle.frontend.core.util.minify.ResourceMinifier;

import java.io.File;

/**
 * Root css or js resource (declared in html page).
 *
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "PMD.SystemPrintln"})
public abstract class RootResource extends OptimizedEntity implements ResourceInfo {

    /**
     * "integrity" attribute.
     */
    public static final String INTEGRITY_ATTR = "integrity";
    /**
     * "crossorigin" attribute.
     */
    public static final String CROSSORIGIN_ATTR = "crossorigin";
    /**
     * Html pare, referenced this resource.
     */
    protected final HtmlPage html;
    /**
     * Resource tag element (from jsoup tree).
     */
    protected final Element element;
    /**
     * Local resource file (may be null for not yet downloaded or absent resource).
     */
    protected File file;
    /**
     * Source map file (may be null for not yet downloaded or if source map disabled).
     */
    protected File sourceMap;
    /**
     * Gzip file (may be null if not yet generated).
     */
    protected File gzip;
    /**
     * Url attribute name (to support both js and css tags).
     */
    protected final String attr;
    /**
     * Directory to store downloaded resource.
     */
    protected final File dir;
    /**
     * True for remote resource (download required).
     */
    protected boolean remote;
    /**
     * Resource tag declaration in the original html file (for further replacement).
     * Note that script tag may miss closing tag (due to jsoup specifics).
     */
    protected final String sourceDeclaration;

    public RootResource(final HtmlPage html,
                        final Element element,
                        final String sourceDeclaration,
                        final String attr,
                        final File dir) {
        this.html = html;
        this.element = element;
        this.attr = attr;
        this.dir = dir;
        this.sourceDeclaration = sourceDeclaration;
    }

    /**
     * @return html page declared link to this resource
     */
    public HtmlPage getHtml() {
        return html;
    }

    /**
     * @return optimization settings (immutable)
     */
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
    public String getSourceDeclaration() {
        return sourceDeclaration;
    }

    @Override
    public String getTarget() {
        return element.attr(attr);
    }

    @Override
    public String getIntegrity() {
        final String integrity = element.attr(INTEGRITY_ATTR);
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

    // ACTIONS ---------------------------------------------------------------------

    /**
     * Download remote resource or check local file for existence.
     */
    public void resolve() {
        final String target = getTarget();

        if (target.toLowerCase().startsWith("http")) {
            download(target);
        } else {
            // local file
            file = new File(html.getHtmlDir(), UrlUtils.clearParams(target));
            if (!file.exists()) {
                System.out.println("WARNING: " + FileUtils.relative(html.getFile(), file) + " (referenced from "
                        + FileUtils.relative(html.getBaseDir(), html.getFile())
                        + ") not found: optimizations would not be applied"
                        + "\n         (" + file.getAbsolutePath() + ")\n");

                ignore("not found");
            }
        }

        if (file != null && file.exists()) {
            recordSize(SizeType.ORIGINAL, file.length());
        }
    }

    /**
     * Replace file url (usually, after download to local file path).
     *
     * @param url new url
     */
    public void changeTarget(final String url) {
        final String old = element.attr(attr);
        element.attr(attr, url);
        recordChange(old + " -> " + url);
    }

    /**
     * Minify file, if it's not already minified (has no ".min" in its name).
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void minify() {
        if (isIgnored() || file.getName().toLowerCase().contains(".min.")) {
            // already minified
            return;
        }
        final long size = file.length();
        System.out.print("Minify " + FileUtils.relative(html.getBaseDir(), file));
        try {
            final MinifyResult min = getMinifier().minify(file, getSettings().isGenerateSourceMaps());
            System.out.println(", " + SizeFormatter.formatChangePercent(size, min.getMinified().length()));
            if (min.getExtraLog() != null) {
                System.out.println(min.getExtraLog());
            }
            if (min.getSourceMap() != null) {
                System.out.println("\tSource map generated: "
                        + FileUtils.relative(getHtml().getFile(), min.getSourceMap()));
            }
            SourceMapUtils.includeSources(min.getSourceMap());
            // remove original file
            System.out.println("\tMinified file source removed: " + file.getName());
            file.delete();

            changeFile(min.getMinified());
            sourceMap(min.getSourceMap());

            recordSize(SizeType.MODIFIED, min.getMinified().length());
            recordChange("minified");
        } catch (RuntimeException ex) {
            System.out.println(" FAILED");
            throw ex;
        }
    }

    /**
     * Compute SRI token and add integrity attribute.
     */
    public void applyIntegrity() {
        // if integrity tag exists then it is assumed to be already validated (during resolve)
        if (!isIgnored() && getIntegrity() == null) {
            final String token = DigestUtils.buildSri(file, "SHA-384");
            element.attr(INTEGRITY_ATTR, token);
            recordChange("integrity token applied");
        }
    }

    /**
     * Compute MD5 for js and css files and apply it into urls.
     * NOTE: MD5 applied to urls in css sub resources under {@link #resolve()} (because without it impossible
     * to properly apply integrity and md5 for root css).
     * <p>
     * IMPORTANT must be applied after possible minification (last steps!) because, obviously, any further
     * modifications would make generated md5 invalid.
     */
    public void applyMd5() {
        if (file != null && file.exists()) {
            final String md5 = FileUtils.computeMd5(file);
            // md5 might be already applied
            if (!getTarget().endsWith(md5)) {
                changeTarget(UrlUtils.clearParams(getTarget()) + "?" + md5);
            }
        }
    }

    /**
     * Generate gzip files for html and all related resources.
     * IMPORTANT must be applied after possible minification (last steps!) because, obviously, any further
     * modifications would make generated gzip invalid.
     */
    public void gzip() {
        if (file != null && file.exists()) {
            gzip = FileUtils.gzip(file, html.getBaseDir());
            recordSize(SizeType.GZIPPED, gzip.length());
        }
        // gzip source map file (remote source maps would contain all sources)
        if (sourceMap != null && sourceMap.exists()) {
            FileUtils.gzip(sourceMap, html.getBaseDir());
        }
    }

    /**
     * @return resource minifier implementation
     */
    protected abstract ResourceMinifier getMinifier();

    /**
     * Change local file.
     *
     * @param file new file
     */
    protected void changeFile(final File file) {
        if (!this.file.equals(file)) {
            // assuming files are in the same directory
            final String url = getTarget().replace(this.file.getName(), file.getName());
            this.file = file;
            changeTarget(url);
        }
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    private void download(final String target) {
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
                if (element.hasAttr(CROSSORIGIN_ATTR)) {
                    element.removeAttr(CROSSORIGIN_ATTR);
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
    }

    private void sourceMap(final File file) {
        if (file != null) {
            this.sourceMap = file;
            recordChange("source map generated: " + file.getName());
        }
    }
}
