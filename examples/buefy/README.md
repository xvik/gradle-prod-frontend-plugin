# Buefy optimization example

`gradlew prodFrontend` output:

```
Executing 'prodFrontend'...

> Task :buefy:buildWebapp

> Task :buefy:prodFrontend
Redirect resolved: https://unpkg.com/vue@2 --> https://unpkg.com/vue@2.7.14
Redirect resolved: https://unpkg.com/vue@2.7.14 --> https://unpkg.com/vue@2.7.14/dist/vue.js
Download https://unpkg.com/vue@2.7.14/dist/vue.min.js, took 410ms (104 KB)
	Minified version found and downloaded
	No source map file reference found
Download https://unpkg.com/buefy/dist/buefy.min.js, took 639ms (311 KB)
	No source map file reference found
Download https://unpkg.com/buefy/dist/buefy.min.css, took 920ms (358 KB)
	No source map file reference found
Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/css/materialdesignicons.min.css, took 569ms (263 KB)
Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/css/materialdesignicons.css.map, took 589ms (349 KB)
	Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/scss/materialdesignicons.scss, took 81ms (165 bytes)
	../scss/materialdesignicons.scss (165 bytes) embedded into materialdesignicons.css.map
	Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/scss/_variables.scss, took 425ms (157 KB)
	../scss/_variables.scss (157 KB) embedded into materialdesignicons.css.map
	Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/scss/_functions.scss, took 84ms (515 bytes)
	../scss/_functions.scss (515 bytes) embedded into materialdesignicons.css.map
	Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/scss/_path.scss, took 267ms (585 bytes)
	../scss/_path.scss (585 bytes) embedded into materialdesignicons.css.map
	Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/scss/_core.scss, took 81ms (455 bytes)
	../scss/_core.scss (455 bytes) embedded into materialdesignicons.css.map
	Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/scss/_icons.scss, took 81ms (209 bytes)
	../scss/_icons.scss (209 bytes) embedded into materialdesignicons.css.map
	Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/scss/_extras.scss, took 81ms (1 KB)
	../scss/_extras.scss (1 KB) embedded into materialdesignicons.css.map
	Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/scss/_animated.scss, took 82ms (688 bytes)
	../scss/_animated.scss (688 bytes) embedded into materialdesignicons.css.map
	Source map updated: materialdesignicons.css.map (529 KB)
Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/fonts/materialdesignicons-webfont.eot?v=5.8.55, took 190ms (981 KB)
Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/fonts/materialdesignicons-webfont.eot?#iefix&v=5.8.55, took 359ms (981 KB)
	Duplicate file 'materialdesignicons-webfont.1.eot' removed in favour of existing 'materialdesignicons-webfont.eot'
Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/fonts/materialdesignicons-webfont.woff2?v=5.8.55, took 108ms (312 KB)
Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/fonts/materialdesignicons-webfont.woff?v=5.8.55, took 120ms (445 KB)
Download https://cdn.jsdelivr.net/npm/@mdi/font@5.8.55/fonts/materialdesignicons-webfont.ttf?v=5.8.55, took 713ms (981 KB)
Minify index.html, 36% size increase(!)
Gzip index.html, 29% size decrease
Gzip css/buefy.min.css, 87% size decrease
Gzip css/materialdesignicons.min.css, 84% size decrease
Gzip css/fonts/materialdesignicons-webfont.eot, 54% size decrease
Gzip css/fonts/materialdesignicons-webfont.woff2, 2% size decrease
Gzip css/fonts/materialdesignicons-webfont.woff, not changed
Gzip css/fonts/materialdesignicons-webfont.ttf, 54% size decrease
Gzip js/vue.min.js, 64% size decrease
Gzip js/buefy.min.js, 77% size decrease

                                                                       original       minified       gzipped        
--------------------------------------------------------------------------------------------------------------------
index.html                                                             590 bytes      807 bytes      570 bytes      
  js/vue.min.js                                                        104 KB                        37 KB          
  js/buefy.min.js                                                      311 KB                        69 KB          
  css/buefy.min.css                                                    358 KB                        45 KB          
  css/materialdesignicons.min.css                                      263 KB                        41 KB          
    fonts/materialdesignicons-webfont.eot                                981 KB                        447 KB         
    fonts/materialdesignicons-webfont.eot                                981 KB                        447 KB         
    fonts/materialdesignicons-webfont.woff2                              312 KB                        304 KB         
    fonts/materialdesignicons-webfont.woff                               445 KB                        443 KB         
    fonts/materialdesignicons-webfont.ttf                                981 KB                        447 KB         
                                                                       ---------------------------------------------
                                                                       1 MB           1 MB           194 KB         


BUILD SUCCESSFUL in 7s
```