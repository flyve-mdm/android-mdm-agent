/* eslint-disable no-unused-vars */
function sendContactEmail() {
  const contactData = {}

  $('form').serializeArray().forEach((item) => {
    if (contactData[item.name]) {
      if (typeof (contactData[item.name]) === 'string') {
        contactData[item.name] = [contactData[item.name]]
      }

      contactData[item.name].push(item.value)
    } else {
      contactData[item.name] = item.value
    }
  })

  let isValid = true

  const requiredElements = [
    'contactName',
    'contactEmail',
    [
      'contactMotivePricing',
      'contactMotivePartnership',
      'contactMotiveSupport',
      'contactMotiveOther',
    ],
    'contactSubject',
    'contactMessage',
  ]

  requiredElements.forEach((element) => {
    if (Array.isArray(element)) {
      let nestedElementIsValid = false

      element.forEach((nestedElement) => {
        if (contactData[nestedElement] && nestedElement[nestedElement] !== '') {
          nestedElementIsValid = true
        }
      })

      if (!nestedElementIsValid) {
        $('#contactMotive').css('color', 'red')
        isValid = false
      }
    } else if (!contactData[element] || contactData[element] === '') {
      $(`#${element}`).css('color', 'red')
      isValid = false
    }

    if (element === 'contactEmail') {
      if (!validateEmail(contactData[element])) {
        $('#contactEmail').css('color', 'red')
        isValid = false
      }
    }
  })

  if (isValid) {
    const link = `mailto:sales@teclib.com?subject=${escape(contactData.contactSubject)}`
    const body = escape(`
    URL: ${window.location.href}

    Name: ${contactData.contactName}

    Email: ${contactData.contactEmail}

    Company: ${contactData.contactCompany}

    Job position: ${contactData.contactJobPosition}

    Motive:
      - Pricing: ${contactData.contactMotivePricing ? 'Yes' : 'No'}
      - Partnership: ${contactData.contactMotivePartnership ? 'Yes' : 'No'}
      - Support: ${contactData.contactMotiveSupport ? 'Yes' : 'No'}
      - Other: ${contactData.contactMotiveOther}

    Subject: ${contactData.contactSubject}

    Message:
      ${contactData.contactMessage}
    `)

    window.location.href = `${link}&body=${body}`
  }
}
