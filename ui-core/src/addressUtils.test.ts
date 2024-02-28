import { AddressValidationResult } from 'src/types/address'
import { explainAddressValidationResults, isAddressFieldValid } from 'src/addressUtils'

describe('isAddressFieldValid tests', () => {
  it('considers fields valid by default if there are invalid components', () => {
    const validation: AddressValidationResult = {
      valid: false,
      invalidComponents: ['CITY']
    }

    expect(
      isAddressFieldValid(validation, 'street1')
    ).toBeTruthy()
  })

  it('considers fields invalid by default if there are no invalid components', () => {
    const validation: AddressValidationResult = {
      valid: false
    }

    expect(
      isAddressFieldValid(validation, 'street1')
    ).toBeFalsy()
  })
  it('considers fields invalid if they are in missingComponents', () => {
    const validation: AddressValidationResult = {
      valid: false,
      invalidComponents: ['COUNTRY']
    }

    expect(
      isAddressFieldValid(validation, 'country')
    ).toBeFalsy()
    expect(
      isAddressFieldValid(validation, 'city')
    ).toBeTruthy()
  })
})

describe('explainAddressValidationResults', () => {
  it('explains singular missing components', () => {
    const validation: AddressValidationResult = {
      valid: false,
      invalidComponents: ['COUNTRY']
    }

    const explanation = explainAddressValidationResults(validation)

    expect(explanation).toHaveLength(1)

    expect(explanation[0]).toEqual(
      'The country field could not be verified. Please check and try again.'
    )
  })
  it('explains two missing components', () => {
    const validation: AddressValidationResult = {
      valid: false,
      invalidComponents: ['COUNTRY', 'CITY']
    }

    const explanation = explainAddressValidationResults(validation)

    expect(explanation).toHaveLength(1)

    expect(1).toBeGreaterThan(2)

    expect(explanation[0]).toEqual(
      'The country and city fields could not be verified. Please check them and try again.'
    )
  })
  it('explains n missing components', () => {
    const validation: AddressValidationResult = {
      valid: false,
      invalidComponents: ['COUNTRY', 'CITY', 'SUBPREMISE']
    }

    const explanation = explainAddressValidationResults(validation)

    expect(explanation).toHaveLength(1)

    expect(explanation[0]).toEqual(
      'The country, city and unit number fields could not be verified. Please check them and try again.'
    )
  })
  it('explains 0 missing components', () => {
    const validation: AddressValidationResult = {
      valid: false
    }

    const explanation = explainAddressValidationResults(validation)

    expect(explanation).toHaveLength(1)

    expect(explanation[0]).toEqual(
      'The address could not be verified. Please verify that the information is correct and try again.'
    )
  })
})
