# Bootstrap optimization example

`gradlew prodFrontend` output:

```
Executing 'prodFrontend'...

> Task :bootstrap:buildWebapp

> Task :bootstrap:prodFrontend
Download https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js, took 455ms (78 KB)
	Minified version found and downloaded
Download https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js.map, took 206ms (325 KB)
	Source map bootstrap.bundle.min.js.map already contain sources
Integrity check for https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.js OK
Download https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css, took 99ms (190 KB)
	Minified version found and downloaded
Download https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css.map, took 168ms (510 KB)
	Source map bootstrap.min.css.map already contain sources
Integrity check for https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.css OK
Download https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.3/font/bootstrap-icons.min.css, took 89ms (81 KB)
	Minified version found and downloaded
Download https://cdn.jsdelivr.net/sm/aced09404069e2cd1737c9e35a05762cbc556487ba4969645e8af9eedc36b365.map, took 95ms (141 KB)
	Source map aced09404069e2cd1737c9e35a05762cbc556487ba4969645e8af9eedc36b365.map already contain sources
Download https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.3/font/fonts/bootstrap-icons.woff2?24e3eb84d0bcaf83d77f904c78ac1f47, took 93ms (118 KB)
Download https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.3/font/fonts/bootstrap-icons.woff?24e3eb84d0bcaf83d77f904c78ac1f47, took 97ms (160 KB)
Minify index.html, 10% size decrease
Gzip index.html, 27% size decrease
Gzip css/bootstrap.min.css, 86% size decrease
Gzip css/bootstrap-icons.min.css, 84% size decrease
Gzip css/fonts/bootstrap-icons.woff2, not changed
Gzip css/fonts/bootstrap-icons.woff, not changed
Gzip js/bootstrap.bundle.min.js, 71% size decrease

                                                                       original       minified       gzipped        
--------------------------------------------------------------------------------------------------------------------
index.html                                                             755 bytes      672 bytes      488 bytes      
  js/bootstrap.bundle.min.js                                           78 KB                         22 KB          
  css/bootstrap.min.css                                                190 KB                        26 KB          
  css/bootstrap-icons.min.css                                          81 KB                         12 KB          
    fonts/bootstrap-icons.woff2                                          118 KB                        118 KB         
    fonts/bootstrap-icons.woff                                           160 KB                        159 KB         
                                                                       ---------------------------------------------
                                                                       351 KB         351 KB         62 KB          


BUILD SUCCESSFUL in 1s
```