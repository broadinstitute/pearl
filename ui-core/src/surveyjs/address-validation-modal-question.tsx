/**
 * A SurveyJS question that renders a multiple selection combobox.
 * This provides similar functionality as the "tagbox" in https://github.com/surveyjs/custom-widgets.
 * However, this virtualizes the options list to support mahy more
 * options while remaining performant.
 */
import React from 'react'
import { ElementFactory, Question, Serializer } from 'survey-core'
import { ReactQuestionFactory, SurveyQuestionElementBase } from 'survey-react-ui'
import { AddressValidationResult, MailingAddress } from '../types/address'
import SuggestBetterAddressModal from '../components/SuggestBetterAddressModal'

export type AddressValidationQuestionValue = {
  inputAddress: MailingAddress,
  canceledSuggestedAddress: boolean,
  acceptedSuggestedAddress: boolean,
  modalDismissed: boolean,
  addressValidationResult: AddressValidationResult
}

const AddressValidationType = 'addressvalidation'
export class QuestionAddressValidationModel extends Question {
  getType() {
    return AddressValidationType
  }

  get street1() {
    return this.getPropertyValue('street1')
  }

  set street1(val: string) {
    this.setPropertyValue('street1', val)
  }

  get street2() {
    return this.getPropertyValue('street2')
  }

  set street2(val: string) {
    this.setPropertyValue('street2', val)
  }

  get city() {
    return this.getPropertyValue('city')
  }

  set city(val: string) {
    this.setPropertyValue('city', val)
  }

  // 'state' is already used on questions :(
  get stateProvince() {
    return this.getPropertyValue('state')
  }

  set stateProvince(val: string) {
    this.setPropertyValue('state', val)
  }

  get country() {
    return this.getPropertyValue('country')
  }

  set country(val: string) {
    this.setPropertyValue('country', val)
  }

  get postalCode() {
    return this.getPropertyValue('postalCode')
  }

  set postalCode(val: string) {
    this.setPropertyValue('postalCode', val)
  }
}

ElementFactory.Instance.registerElement(AddressValidationType, name => {
  return new QuestionAddressValidationModel(name)
})

Serializer.addClass(
  AddressValidationType,
  [{
    name: 'street1',
    category: 'general'
  }, {
    name: 'street2',
    category: 'general'
  }, {
    name: 'city',
    category: 'general'
  }, {
    name: 'stateProvince',
    category: 'general'
  }, {
    name: 'postalCode',
    category: 'general'
  }, {
    name: 'country',
    category: 'general'
  }],
  () => new QuestionAddressValidationModel(''),
  'question'
)


export class SurveyQuestionAddressValidation extends SurveyQuestionElementBase {
  get question() {
    return this.questionBase
  }

  get value(): AddressValidationQuestionValue {
    return this.question.value
  }

  accept(addr: MailingAddress) {
    const survey = this.questionBase.survey

    survey.getQuestionByName(this.question.street1)?.updateValueFromSurvey(addr.street1)
    survey.getQuestionByName(this.question.street2)?.updateValueFromSurvey(addr.street2)
    survey.getQuestionByName(this.question.city)?.updateValueFromSurvey(addr.city)
    survey.getQuestionByName(this.question.stateProvince)?.updateValueFromSurvey(addr.state)
    survey.getQuestionByName(this.question.postalCode)?.updateValueFromSurvey(addr.postalCode)
    survey.getQuestionByName(this.question.country)?.updateValueFromSurvey(addr.country)
  }

  renderElement() {
    return (
      <>
        {(this.value.addressValidationResult?.suggestedAddress
            && !this.value.canceledSuggestedAddress
            && !this.value.acceptedSuggestedAddress) &&
            <SuggestBetterAddressModal
              inputtedAddress={this.value.inputAddress}
              improvedAddress={this.value.addressValidationResult.suggestedAddress}
              hasInferredComponents={this.value.addressValidationResult.hasInferredComponents || false}
              accept={() => {
                this.question.value = {
                  ...this.value,
                  modalDismissed: true,
                  acceptedSuggestedAddress: true
                }
                if (this.value.addressValidationResult.suggestedAddress) {
                  this.accept(this.value.addressValidationResult.suggestedAddress)
                }
              }}
              deny={() => {
                this.question.value = {
                  ...this.value,
                  modalDismissed: true,
                  canceledSuggestedAddress: true
                }
              }}
              onDismiss={() => {
                this.question.value = {
                  ...this.value,
                  modalDismissed: true,
                  canceledSuggestedAddress: true
                }
              }}
            />}
      </>
    )
  }
}

ReactQuestionFactory.Instance.registerQuestion(AddressValidationType, props => {
  return React.createElement(SurveyQuestionAddressValidation, props)
})
