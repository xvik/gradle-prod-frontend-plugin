package ru.vyarus.gradle.frontend.util.load;

import com.helger.css.ECSSVersion;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.decl.visit.AbstractModifyingCSSUrlVisitor;
import com.helger.css.decl.visit.CSSVisitor;
import com.helger.css.reader.CSSReader;
import com.helger.css.utils.CSSDataURLHelper;
import ru.vyarus.gradle.frontend.util.FileUtils;
import ru.vyarus.gradle.frontend.util.UrlUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vyacheslav Rusakov
 * @since 04.02.2023
 */
public class CssResourcesLoader {

    public static Map<String, File> processLinks(final File file, final String baseUrl) {
        final CascadingStyleSheet css = CSSReader.readFromFile(file, StandardCharsets.UTF_8, ECSSVersion.CSS30);
        if (css == null) {
            throw new IllegalStateException("Failed to read css file: " + file.getAbsolutePath());
        }
        final Map<String, File> res = new HashMap<>();
        CSSVisitor.visitCSSUrl(css, new AbstractModifyingCSSUrlVisitor() {
            @Nonnull
            @Override
            protected String getModifiedURI(@Nonnull String url) {
                // ignore data urls and svg references
                if (CSSDataURLHelper.isDataURL(url) || url.startsWith("#")) {
                    return url; 
                }
                if (!url.startsWith("http")) {
                    // relative resources (often started with ../) - not loading them would lead to error

                    // trying to preserve folder structure, but without going upper css location
                    // name extraction required to get rid of anti-cache part (?v=132)
                    String name = UrlUtils.getFileName(url);
                    String folder = null;
                    if (!url.startsWith("http")) {
                        // extracting folder from relative path
                        int idx = UrlUtils.getNameSeparatorPos(url);
                        if (idx > 0) {
                            folder = url.substring(0, idx).replace("../", "");
                            if (folder.startsWith("/")) {
                                folder = folder.substring(1);
                            }
                        }
                    }

                    if (folder == null || folder.trim().isEmpty()) {
                        folder = "resources";
                    }

                    File target = new File(file.getParentFile().getAbsolutePath() + "/" + folder + "/" + name);
                    try {
                        final String targetUrl = baseUrl + url;
                        System.out.println("Loading: " + targetUrl);
                        UrlUtils.download(targetUrl, target);
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to load relative css resource: " + url, e);
                    }
                    res.put(url, target);
                    return url;
                }
                return url;
            }
        });

        if (!res.isEmpty()) {
            // override css
            try {
                // replacing like this to not harm minification
                String content = Files.readString(file.toPath());
                for (Map.Entry<String, File> entry : res.entrySet()) {
                    File resource = entry.getValue();
                    String md5 = FileUtils.computeMd5(resource);
                    content = content.replace(entry.getKey(), FileUtils.relative(file, resource) + "?" + md5);
                }
                FileUtils.writeFile(file, content);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to fix css file links", e);
            }
        }
        return res.isEmpty() ? null : res;
    }

}
