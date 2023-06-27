# Vue optimization example

`gradlew prodFrontend` output:

```
Executing 'prodFrontend'...

> Task :vue:buildWebapp

> Task :vue:prodFrontend
Download https://unpkg.com/vue@2.7.14/dist/vue.min.js, took 413ms (104 KB)
	Minified version found and downloaded
	No source map file reference found
WARNING: js/unknown.js (referenced from index.html) not found: optimizations would not be applied
         (/home/xvik/projects/gradle-prod-frontend-plugin/examples/vue/build/webapp/js/unknown.js)

Download https://cdn.materialdesignicons.com/2.5.94/css/materialdesignicons.min.css, took 1.08s (108 KB)
Download https://cdn.materialdesignicons.com/2.5.94/css/materialdesignicons.min.css.map, took 714ms (87 KB)
	Download https://cdn.materialdesignicons.com/2.5.94/scss/_path.scss, took 582ms (713 bytes)
	../scss/_path.scss (713 bytes) embedded into materialdesignicons.min.css.map
	Download https://cdn.materialdesignicons.com/2.5.94/scss/_core.scss, took 584ms (455 bytes)
	../scss/_core.scss (455 bytes) embedded into materialdesignicons.min.css.map
	Download https://cdn.materialdesignicons.com/2.5.94/scss/_icons.scss, took 574ms (207 bytes)
	../scss/_icons.scss (207 bytes) embedded into materialdesignicons.min.css.map
	Download https://cdn.materialdesignicons.com/2.5.94/scss/_extras.scss, took 559ms (1 KB)
	../scss/_extras.scss (1 KB) embedded into materialdesignicons.min.css.map
	Download https://cdn.materialdesignicons.com/2.5.94/scss/_animated.scss, took 561ms (688 bytes)
	../scss/_animated.scss (688 bytes) embedded into materialdesignicons.min.css.map
	Source map updated: materialdesignicons.min.css.map (91 KB)
Download https://cdn.materialdesignicons.com/2.5.94/fonts/materialdesignicons-webfont.eot?v=2.5.94, took 844ms (365 KB)
Download https://cdn.materialdesignicons.com/2.5.94/fonts/materialdesignicons-webfont.eot?#iefix&v=2.5.94, took 828ms (365 KB)
	Duplicate file 'materialdesignicons-webfont.1.eot' removed in favour of existing 'materialdesignicons-webfont.eot'
Download https://cdn.materialdesignicons.com/2.5.94/fonts/materialdesignicons-webfont.woff2?v=2.5.94, took 742ms (134 KB)
Download https://cdn.materialdesignicons.com/2.5.94/fonts/materialdesignicons-webfont.woff?v=2.5.94, took 759ms (179 KB)
Download https://cdn.materialdesignicons.com/2.5.94/fonts/materialdesignicons-webfont.ttf?v=2.5.94, took 856ms (365 KB)
Download https://cdn.materialdesignicons.com/2.5.94/fonts/materialdesignicons-webfont.svg?v=2.5.94#materialdesigniconsregular, took 1.18s (2 MB)
WARNING: css/unknown.css  (referenced from index.html) not found: optimizations would not be applied
         (/home/xvik/projects/gradle-prod-frontend-plugin/examples/vue/build/webapp/css/unknown.css )

Minify index.html, 30% size increase(!)
Gzip index.html, 24% size decrease
Gzip css/materialdesignicons.min.css, 81% size decrease
Gzip css/fonts/materialdesignicons-webfont.eot, 50% size decrease
Gzip css/fonts/materialdesignicons-webfont.woff2, not changed
Gzip css/fonts/materialdesignicons-webfont.woff, not changed
Gzip css/fonts/materialdesignicons-webfont.ttf, 50% size decrease
Gzip css/fonts/materialdesignicons-webfont.svg, 84% size decrease
Gzip js/vue.min.js, 64% size decrease

                                                                       original       minified       gzipped        
--------------------------------------------------------------------------------------------------------------------
index.html                                                             333 bytes      436 bytes      330 bytes      
  js/vue.min.js                                                        104 KB                        37 KB          
  js/unknown.js                                                        not found
  css/materialdesignicons.min.css                                      108 KB                        19 KB          
    fonts/materialdesignicons-webfont.eot                                365 KB                        179 KB         
    fonts/materialdesignicons-webfont.eot                                365 KB                        179 KB         
    fonts/materialdesignicons-webfont.woff2                              134 KB                        133 KB         
    fonts/materialdesignicons-webfont.woff                               179 KB                        179 KB         
    fonts/materialdesignicons-webfont.ttf                                365 KB                        179 KB         
    fonts/materialdesignicons-webfont.svg                                2 MB                          419 KB         
  css/unknown.css                                                      not found
                                                                       ---------------------------------------------
                                                                       213 KB         213 KB         57 KB          


BUILD SUCCESSFUL in 10s
```