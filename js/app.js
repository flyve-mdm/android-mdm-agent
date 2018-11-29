/* eslint-env browser */

$(() => {
  // On mouse click and touch, add a class to <button> and <a> that removes focus rectangle
  $(document).on('mousedown touchstart', 'a, button', () => {
    $('.no-outline').removeClass('no-outline')
    $(this).addClass('no-outline')
  })

  // On keyboard navigation, remove the class that hides focus rectangle
  $(document).on('keydown', (e) => {
    const keyCode = e.keyCode || e.which
    const tabKeyCode = 9

    if (keyCode === tabKeyCode) {
      $('.no-outline').removeClass('no-outline')
    }
  })

  // Side Nav Large

  if ($('.side-navigation').length) {
    const closeBtn = $('.close')
    const sideAffix = $('.side-navigation-affix')
    const sideBtn = $('a.navigation-btn')
    let sideNav = $('.side-navigation-large')
    const sideSection = $('.navigation-section')
    const sideTopSpacing = 48

    if ($('.side-navigation-small').length) {
      sideNav = $('.side-navigation-small')
    }

    const topOffset = sideSection.offset().top - sideTopSpacing
    const bottomOffset = $('body').height() - sideSection.offset().top - sideSection.height()

    sideAffix.affix({
      offset: {
        top: topOffset,
        bottom: bottomOffset,
      },
    })

    sideAffix.width(sideNav.parent().width())

    sideBtn.on('click', () => {
      sideNav.css('display', 'block')
      sideSection.css('display', 'none')
      sideBtn.css('display', 'none')
      $('body').css('overflow', 'hidden')
    })
    closeBtn.on('click', () => {
      sideNav.css('display', '')
      sideSection.css('display', '')
      sideBtn.css('display', 'inline-block')
      $('body').css('overflow', '')
    })
  }

  // Alert stack
  (function alerts() {
    const alertStack = $('.alert-stack')

    if (alertStack.length === 0) {
      return
    }

    alertStack.affix({
      offset: {
        top: alertStack.offset().top,
      },
    })
  }());


  // Back to top
  (
    function backTop() {
      const backToTop = $('.back-to-top')
      const threshold = 2 * $(window).height()

      // Displayed when we've scrolled 2x the viewport height
      if (backToTop.length === 0
            || $(document).height() < threshold) {
        return
      }

      backToTop.affix({
        offset: {
          top: threshold,
        },
      })

      // Smooth scroll to top
      backToTop.on('click', () => {
        $('html, body').animate({ scrollTop: 0 }, {
          duration: 750,
          easing: 'swing',
        })

        return false // prevent default href
      })
    }());


  // Smooth scroll with page header links
  (function smoothScroll() {
    $("[data-scroll='smooth'] a[href*='#']:not([href='#'])")
      .on('click', () => {
        if (window.location.pathname.replace(/^\//, '') === this.pathname.replace(/^\//, '')
                && window.location.hostname === this.hostname) {
          let target = $(this.hash)
          target = target.length ? target : $(`[name=${this.hash.slice(1)}]`)

          if (target.length) {
            $('html, body').animate({
              scrollTop: target.offset().top,
            }, 1500)

            return false // prevent default href
          }
        }
        return true
      })
  }());


  // Forms
  (function forms() {
    $('.checkbox-indeterminate').prop('indeterminate', true)
  }());


  // Star rating
  (function starRating() {
    $('.rating-btn').on('mouseenter', () => {
      // Highlight the hovered star and the previous stars
      $(this).prevAll('.rating-btn').addClass('active')
      $(this).addClass('active')

      // Remove highlighting of the following stars
      $(this).nextAll('.rating-btn').removeClass('active')
    })

    // Remove highlight of all stars when leaving the container
    $('.rating-stars-input').on('mouseleave', function removeHighlight() {
      $(this).find('.rating-btn').removeClass('active')
    })
  }())


  // Dropdown sub-menu
  $('ul.dropdown-menu [data-toggle=dropdown]').on('click', function toggleDropdownMenu(event) {
    event.preventDefault()
    event.stopPropagation()
    $(this).parent().siblings().removeClass('open')
    $(this).parent().toggleClass('open')
  })


  // Tooltips
  $('[data-toggle="tooltip"]').tooltip({
    // Override Bootsrap's default template with one that does not have arrow
    template: '<div class="tooltip" role="tooltip"><div class="tooltip-inner"></div></div>',
  })

  // Flyouts
  // Provide data-theme attribute to set flyout's color theme.
  $('[data-toggle="popover"]').each(() => {
    const $element = $(this)

    $element.popover({
      // Override Bootsrap's default template with one that does not have arrow and title
      template: '<div class="popover" role="tooltip"><div class="popover-content"></div></div>',
    }).data('bs.popover').tip().addClass($element.data('theme'))
  })

  if ($('#btn-close').length) {
    $('#btn-close').popover({
      placement: 'right',
      html: 'true',
      // Set the value of the data-theme attribute as a class name on the button.
      // That way the button will always be in the correct color theme.
      content: `This is a flyout with a button. <button type="button" class="btn btn-primary ${$('#btn-close').data('theme')}"onclick="$(&quot;#btn-close&quot;).popover(&quot;hide&quot;);">Button</button>`,
      template: '<div class="popover" role="tooltip"><div class="popover-content"></div></div>',
    }).data('bs.popover').tip()
      .addClass($('#btn-close').data('theme'))
  }

  // Entity list item
  $('.entity-list-expandable .entity-list-item').click(() => {
    if ($(this).hasClass('active') === false) {
      const parent = $(this).parent()
      $('.entity-list-item', parent).removeClass('active')
      $(this).addClass('active')
    }
  })

  // Howtos header
  $('.howtos-sidebar').affix({
    offset: {
      top: 32,
    },
  })

  // Site header
  $('.site-header').affix({
    offset: {
      top: 16,
    },
  })

  // Enable jQuery Lazy plugin
  $('.lazy').lazy()
})

function validateEmail(email) {
  const re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  return re.test(String(email).toLowerCase())
}
