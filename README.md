# gradle-prod-frontend-plugin
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](http://www.opensource.org/licenses/MIT)
[![CI](https://github.com/xvik/gradle-prod-frontend-plugin/actions/workflows/CI.yml/badge.svg)](https://github.com/xvik/gradle-prod-frontend-plugin/actions/workflows/CI.yml)
[![Appveyor build status](https://ci.appveyor.com/api/projects/status/github/xvik/gradle-prod-frontend-plugin?svg=true)](https://ci.appveyor.com/project/xvik/gradle-prod-frontend-plugin)
[![codecov](https://codecov.io/gh/xvik/gradle-prod-frontend-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/xvik/gradle-prod-frontend-plugin)

### About

Optimize static resources in html file for production.

IMPORTANT: Plugin assumed to be used **in time of delivery creation** and not during development.
Plugin **does not merge** multiple css or js files!

Features:
* Download remote resources (from CDN). Automatic min version download
* Css related resources downoad support (web fonts, images, etc.)
* Js and css minification (if not minified already) without nodejs
* Source maps download or generation (embed sources inside source map)
* Integrity attributes support: check SRI after download and generate SRI tags for 
processed resources (resources modification prevention)
* Add MD5 hashes as anti-cahce to resource links (and to all urls inside css)
* Html minification (and inner js and css)
* Gzipped resource version generation
* Could be used on templates (jsp, freemarker etc.)

##### Summary

* Configuration: `prodFrontend`
* Tasks:
    - `prodFrontend` - frontend optimization      

### Setup

[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus/gradle-prod-frontend-plugin.svg)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/gradle-prod-frontend-plugin)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/ru.vyarus/prod-frontend/ru.vyarus.prod-frontend.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=plugins%20portal)](https://plugins.gradle.org/plugin/ru.vyarus.prod-frontend)

```groovy
plugins {
    id 'ru.vyarus.prod-frontend' version '1.0.2'
}
```

OR

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath 'ru.vyarus:gradle-prod-frontend-plugin:1.0.2'
    }
}
apply plugin: 'ru.vyarus.prod-frontend'
``` 

### Compatibility

NOTE: Java 11 or above is required.

Gradle | Version
--------|-------
6.2-8   | 1.0.1


### Usage

Plugin suppose to be used for situations when *nodejs is an overkill*. If you already
use nodejs, then this plugin is useless for you.

For example, small SPA application (e.g. vue.js based) or used bootstrap. 
In such cases, links to CDN used instead of local resources (integrity attributes are not required, 
but [proposes](https://getbootstrap.com/docs/5.3/getting-started/introduction/) by bootstrap "getting started" doc):

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Bootstrap demo</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.css" rel="stylesheet" integrity="sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65" crossorigin="anonymous">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.3/font/bootstrap-icons.css">
</head>
<body>
<h1>Hello, world!</h1>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.js" integrity="sha384-kenU1KFdBIe4zVF0s0G1M5b4hcpxyD9F7jL+jjXkk+Q2h455rYXK/7HAuoJl+0I4" crossorigin="anonymous"></script>
</body>
</html>
```

Setup like this is ideal for development, but for production it's better to switch to local files.
Delivery bundling must depend on plugin's `prodFrontend` task in order to load and minify resources, 
so html above become:

```html
<!doctype html><html lang=en><head><meta charset=utf-8><meta content="width=device-width,initial-scale=1" name=viewport><title>Bootstrap demo</title><link href=css/bootstrap.min.css?3f30c2c47d7d23c7a994db0c862d45a5 integrity=sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65 rel=stylesheet><link href=css/bootstrap-icons.min.css?592064a699096620d0525ffdcf31ae28 integrity=sha384-azAjN9PnX4ypgvvOU6KdrdSJ6KkBeiMMdG20p+P/+ujTot2gRwF6uHIw94nRlpWp rel=stylesheet><body><h1>Hello, world!</h1><script integrity=sha384-kenU1KFdBIe4zVF0s0G1M5b4hcpxyD9F7jL+jjXkk+Q2h455rYXK/7HAuoJl+0I4 src=js/bootstrap.bundle.min.js?b75ae000439862b6a97d2129c85680e8></script>
```

In console, you can see statistic for all optimized files:

```
                                                                       original       minified       gzipped        
--------------------------------------------------------------------------------------------------------------------
index.html                                                             767 bytes      672 bytes      488 bytes      
  js/bootstrap.bundle.min.js                                           78 KB                         22 KB          
  css/bootstrap.min.css                                                190 KB                        26 KB          
  css/bootstrap-icons.min.css                                          81 KB                         12 KB          
    fonts/bootstrap-icons.woff2                                          118 KB                        118 KB         
    fonts/bootstrap-icons.woff                                           160 KB                        159 KB         
                                                                       ---------------------------------------------
                                                                       351 KB         351 KB         62 KB          
```

Note that minified versions were *downloaded* and no manual js or css minification were performed.

You **can try** this and other examples in sample projects:

* [bootstrap](examples/bootstrap) - bootstrap example (from above)
* [buefyjs](examples/buefy) - vuejs application with buefy components (bulma based)
* [vuejs](examples/vue) - pure vuejs app (with unknown css to show how plugin handles it)
* [Static site optimization example](https://github.com/xvik/vyarus.ru/blob/master/build.gradle) - completely static site being optimized before netlify publication 

#### Required setup

Plugin searches for html files in target directory and optimize files **in this directory**.

Normally, you should have some delivery task which collects all html-related resources in one
build directory. You just need to run production optimization on this directory, before zipping it
(or any other delivery processing).

Example:

```groovy
tasks.register('buildWebapp', Copy) {
    from('src/main/webapp')
    into 'build/webapp'
}

tasks.named('prodFrontend').configure {dependsOn('buildWebapp') }
```

If there is some zipping task, it should depend on `prodFrontend`, which depends
on delivery preparation task (`buildWebapp`).

By default, plugin is configured to optimize `build/webapp` directory. To change directory:

```groovy
prodFrontend {
  sourceDir = 'build/otherDir'
}
```

NOTE: it would be **a bad idea** to run plugin on folder inside sources simply because 
plugin *modifies files*

#### CSS imports

WARNING: Be careful with css imports (`@import url('http://somewhere.com/style.css')`) - in current implementation
such relative css files would be downloaded, but *would not be checked for links inside it* (not processed as root css files). 
So, if such imported file would contain relative links, they will not work properly (no problems with absolute links).

There are two easy workarounds: 

1. Move such css import inside html file (declare it as root resource) 
2. Add url to download exclusions (to preserve resource loading from remote url) 

Of course, it is possible to implement complete css processing, but I don't see much need in it now. If you require
it, please create a new issue with situation description.


#### Templates

By default, plugin searches for files with `.html` or `.htm` extension. In order to optimize
templates, configure required template files extensions:

```groovy
prodFrontend {
  htmlExtensions = ['jsp']
}
```

NOTE: you **may** need to disable html minification for templates with `minify.html=false` 
(or using exclusion: `minify.ignore = [**.jsp]`) because minification **could** damage template specific constructs 

#### War plugin

There is no way to directly integrate plugin into war plugin flow. Instead, you'll
have to split war "source" generation somewhere in build dir, run optimization on it
and only after that run war building on optimized folder.

#### Exclusions

It might not be desirable to fully process all resources. In such cases exclusions could be specified.
There are 3 levels of exclusion:

* Global exclusions (`ignore`, GLOB) - affects html files search and local resource files processing
* Download exclusions (`download.ignore`, REGEX) - prevents remote resources download
* Minification exclusions (`minify.ignore`, GLOB) - avoid minification for some files

All ignored files would be mentioned in console log.

##### Global exclusions

* When some html files must be excluded from processing
* When some local (or downloaded) resources must not be processed

Example declaration:

```groovy
prodFrontend.ignore = ['**.htm', 'inner/*.js']
```

or as method

```groovy
prodFrontend.ignore '**.htm', 'inner/*.js'
```

Here all "htm" files and all js files inside 'inner' directory would be ignored.

* Glob syntax used ([native java globs](https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-))
* Path root is in configured source dir (where plugin search for html files)
* Setting affects also just downloaded resources (in this case all further operations on file skipped)

WARNING: be careful with css files, because if defined pattern would match just downloaded
css file, then all css sub links would not be downloaded (and these could be relative links, useless for local file).

Glob examples:

* `*.html` - all html files in root dir
* `*.{html,htm,js}` - all html, htm and js files in root dir
* `**.html` - all html files
* `*/*.html` - all html files in first level directories
* `**/*.html` - all html files in any sub directory (any level)
* `index.???` - all files in root directory with name index and 3-chars extension

Special characters:

* `*` It matches zero , one or more than one characters. While matching, it will not cross directories
boundaries.
* `**` It does the same as * but it crosses the directory boundaries.
* `?` It matches only one character for the given name.
* `\` It helps to avoid characters to be interpreted as special characters.
* `[]` In a set of characters, only single character is matched. If (-) hyphen is used then, it matches a
range of characters. Example: `[efg]` matches "e","f" or "g" . `[a-d]` matches a range from a to d.
* `{}` It helps to match the group of sub patterns.

[More syntax examples](https://mincong.io/2019/04/16/glob-expression-understanding/)

##### Download exclusions

* When some remote links should remain (including css sub resources)

Example declaration:

```groovy
prodFrontend.download.ignore = ['cdn.jsdelivr.net', '.*/bootstrap.*']
```

or as method

```groovy
prodFrontend.ignore 'cdn.jsdelivr.net', '.*/bootstrap.*'
```

Here all links from jsdelivr would be ignored and boostrap links:

`<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css"`
`<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"`

* Java regexps used
* Match is partial (`Matcher.find()`), so no need to cover entire url with pattern
* Match is case-insensitive (flag set automatically)
* Also affects css sub resource links (but not relative links!) 

For example, if css file declares web-fonts with absolute links:

```css
@font-face {
    font-family: "Material Design Icons";
    src: url("https://cdn.materialdesignicons.com/2.5.94/fonts/materialdesignicons-webfont.eot?v=2.5.94");
}
```

Then ignore like this would prevent download (even if root css was downloaded): 
`prodFrontend.download.ignore '.*/fonts/.*'`

##### Minification exclusions

* When some resources should not be minified 

Example declaration:

```groovy
prodFrontend.minify.ignore = ['index.html', 'inner/*.js']
```

or as method

```groovy
prodFrontend.ignore 'index.html', 'inner/*.js'
```

Here `index.html` (located in root dir) would not be minified and all js files in "inner" directory

* Use glob syntax (same as global `prodFrontend.ignore`)
* Affects html files and their resources (js, css)

NOTE: manual minification would not be performed for files with ".min." in its name
(e.g. downloaded min version from cdn).


### Performed optimizations

* Load remote resources (if cdn links used). Tries to load minified version (with source maps)
* For css resource, loads inner urls (fonts, images, etc.)
* If integrity attribute present on resource tag - validates resource after loading (security check)
* Minify html, js and css resources (not minified already).
* Html minification, including inner js and css minification
* Applies ani-cache: MD5 hash applied to all links to local files (in html and for css links)
* Applies integrity attributes to prevent malicious resources modification
* Embed original sources for loaded or generated source maps 
* Generate gzip versions for all resources (.gz files) to see gzip size and avoid runtime gzip generation
  (http server must be configured to serve prepared gz files).

Plugin *DOES NOT* bundle multiple js or css files together because it makes no sense now:
* [article 1](https://webspeedtools.com/should-i-combine-css-js/)
* [article 2](https://wpjohnny.com/why-you-shouldnt-combine-css-js-performance-reasons/)

Also, no sass or less support because plugin intended to be used for *delivery phase* only.

### Used tools

* Html parser: [jsoup (java)](https://jsoup.org)
* Html minifier: [minify-html (native)](https://github.com/wilsonzlin/minify-html)
* Js minifier: [closure compiler (java)](https://github.com/google/closure-compiler)
* Css minifier: [csso (js)](https://github.com/css/csso) (run with [graalvm](https://www.graalvm.org/), not nodejs!)
* Source maps manipulation: [jackson (java)](https://github.com/FasterXML/jackson)

### Configuration

By default, all optimization options are enabled.

Options with default values:

```groovy
prodFrontend {
  /**
   * Print plugin debug information (extended logs).
   */
  debug  = false
  /**
   * Directory where html files must be found and processed.
   */
  sourceDir = 'build/webapp'
  /**
   * Directory name for loaded js files (inside source dir).
   */
  jsDir = 'js'
  /**
   * Directory name for loaded css files (inside source dir).
   */
  cssDir = 'css'
  /**
   * File extensions to recognize as html files.
   */
  htmlExtensions = ['html', 'htm']
  /**
   * Glob patterns (relative to base dir) to ignore files processing 
   * (applies to html and resource files). 
   */
  ignore = []

  download {
    /**
     * Download js and css (e.g. declared as cdn links).
     */
    enabled = true
    /**
     * Try to download ".min" resource version first (applicable for cdn links).
     */
    preferMin = true
    /**
     * Load source maps (and source content) for minified versions.
     */
    sourceMaps = true
    /**
     * Regex patterns to not download remote resources (or css sub resources with absolute urls). 
     */
    ignore = []
  }

  minify {
    /**
     * Minify html files.
     */
    html = true
    /**
     * Minify raw js inside html (only if html minification active).
     */
    htmlJs = true
    /**
     * Minify raw css inside html (only if html minification active).
     */
    htmlCss = true
    /**
     * Minify js (not marked as ".min").
     */
    js = true
    /**
     * Minify css (not marked as ".min").
     */
    css = true
    /**
     * Generate source maps for minified resources.
     */
    generateSourceMaps = true
    /**
     * Glob patterns (relative to base dir) to ignore files minification 
     * (applies to html and resource files). 
     */
    ignore = []
  }

  /**
   * Apply MD5 hash into js and css urls (including inner css urls).
   */
  applyAntiCache = true
  /**
   * Add integrity attributes to resource tags so browser could verify loaded resource for unwanted changes.
   */
  applyIntegrity = true
  /**
   * Create ".gz" versions for all resources.
   */
  gzip = true
}
```

#### Anti-cache

Anti cache appends MD5 checksum to all file links. For example:
`some.resource.js` becomes `some.resource.js?24234354353454`.

With this you can configure *forever* cache for such resources on http server.

IMPORTANT: root html file cache should be reduced, or avoid cache at all (otherwise, 
obviously, hashes would not work properly).

Read more about cache in [this article](https://imagekit.io/blog/ultimate-guide-to-http-caching-for-static-assets/)

#### Integrity

Integrity attribute declares SRI token for downloaded resource. Browser could use
this token to compare just loaded resource with declared token. This way you can shield
from "in-the-middle" resources modification attack.

Read more: [subresource integrity](https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity)

By default, plugin generates such attribute for processed resources.

But, you can also specify this attribute for remote links so plugin could validate
downloaded resource. This is not much useful (to check on compile tome), but 
if target resource could be compromised it is a good option.

#### Gzip

Note that gzip option just generates gzipped file versions for all resources. For example,
`some.resource.js.gz` for  `some.resource.js` or  `index.html.gz` for `index.html`.

Gzipped files size is used in stats, so you can estimate the gain of gzipping.

Also, you can configure your http server to serve pre-generated gzip files instead of
hot gzipping. 

* [Example Apache config](https://www.christianroessler.net/tech/2015/apache-and-mod-deflate-serve-pre-compressed-content-instead-of-deflate-on-every-request.html)
* [Apache config example with explanations](https://damien.pobel.fr/post/precompress-brotli-gzip-static-site/)

Gzip executed with maximum compression level.

---
[![gradle plugin generator](http://img.shields.io/badge/Powered%20by-%20Gradle%20plugin%20generator-green.svg?style=flat-square)](https://github.com/xvik/generator-gradle-plugin)
