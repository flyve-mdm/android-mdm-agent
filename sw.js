---
---

self.addEventListener('install', (e) => {
  var CACHE_NAME = '{{ site.github.build_revision }}'

  caches.keys().then((cacheNames) => {
    return Promise.all(
      cacheNames.map((cacheName) => {
        if(cacheName != CACHE_NAME) {
          return caches.delete(cacheName)
        }
      })
    )
  })

  e.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll([
        '{{ site.baseurl }}/',
        '{{ site.baseurl }}/?homescreen=1',
        '{{ "/index.html" | absolute_url }}',
        '{{ "/index.html" | absolute_url }}?homescreen=1',
        '{{ "/contact.html" | absolute_url }}',
        '{{ "/howtos/index.html" | absolute_url }}',
        '{{ "/news/index.html" | absolute_url }}',
        '{{ "/projects.html" | absolute_url }}',
        '{{ "/css/main.css" | absolute_url }}',
        '{{ "/css/syntax.css" | absolute_url }}',
        '{{ "/images/typo.png" | absolute_url }}',
        '{{ "/images/logo-flyve.png" | absolute_url }}',
        '{{ "/images/logo-teclib.png" | absolute_url }}',
        '{{ "/images/logo.png" | absolute_url }}',
        '{{ "/images/logo2.png" | absolute_url }}',
        '{{ "/js/app.js" | absolute_url }}',
        '{{ "/js/jquery.min.js" | absolute_url }}',
        '{{ "/js/bootstrap.min.js" | absolute_url }}',
        '{{ "/manifest.json" | absolute_url }}',
        '{{ "/fonts/glyphs/winjs-symbols.ttf" | absolute_url }}',
        '{{ "/fonts/glyphs/winjs-symbols.eot" | absolute_url }}',
        '{{ "/fonts/glyphs/winjs-symbols.woff" | absolute_url }}',
        '{{ "/fonts/selawk.ttf" | absolute_url }}',
        '{{ "/fonts/selawk.eot" | absolute_url }}',
        '{{ "/fonts/selawkl.ttf" | absolute_url }}',
        '{{ "/fonts/selawkl.eot" | absolute_url }}',
        '{{ "/fonts/selawkb.ttf" | absolute_url }}',
        '{{ "/fonts/selawkb.eot" | absolute_url }}',
        '{{ "/fonts/selawksl.ttf" | absolute_url }}',
        '{{ "/fonts/selawksl.eot" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-brands-400.eot" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-brands-400.svg" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-brands-400.ttf" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-brands-400.woff" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-brands-400.woff2" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-regular-400.eot" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-regular-400.svg" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-regular-400.ttf" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-regular-400.woff" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-regular-400.woff2" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-solid-900.eot" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-solid-900.svg" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-solid-900.ttf" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-solid-900.woff" | absolute_url }}',
        '{{ "/fonts/fontawesome/fa-solid-900.woff2" | absolute_url }}',
        '{{ "/js/share-bar.js" | absolute_url }}',
        '{{ "/js/send-contact-email.js" | absolute_url }}',
      ])
    })
  )
})

self.addEventListener('fetch', (event) => {
  console.log(event.request.url)
  event.respondWith(
    caches.match(event.request).then((response) => {
      return response || fetch(event.request)
    })
  )
})
