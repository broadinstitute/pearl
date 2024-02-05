import { ComponentCollection } from 'survey-core'

import medications from './medications.json'

ComponentCollection.Instance.add({
  name: 'medications',
  title: 'Medications',
  questionJSON: {
    type: 'multiplecombobox',
    placeholder: 'Select medications',
    options: medications.map(medication => ({ value: medication, text: medication }))
  }
})
