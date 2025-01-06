/**
 * A SurveyJS question that renders a multiple selection combobox.
 * This provides similar functionality as the "tagbox" in https://github.com/surveyjs/custom-widgets.
 * However, this virtualizes the options list to support mahy more
 * options while remaining performant.
 */
import {
  ReactQuestionFactory,
  SurveyQuestionCheckbox
} from 'survey-react-ui'
import React from 'react'
import {
  ItemValue,
  LocalizableString,
  RendererFactory,
  SurveyModel
} from 'survey-core'
import { isString } from 'lodash'

// see https://surveyjs.io/form-library/examples/create-custom-question-renderer/reactjs#content-code
// for details on how to create a custom renderer

type Choice = ItemValue & {
  jsonObj: {
    otherStableId: string
    otherText: LocalizableString
  }
}


const OtherItem = ({ otherStableId, otherText, value, onChange } : {
  otherStableId: string
  otherText: string
  value: string
  onChange: (value: string) => void
}) => {
  const [otherValue, setOtherValue] = React.useState(value)

  return <>
    <span className='sv-string-viewer sv-string-viewer--multiline' >
      {otherText}
    </span>
    <input
      id={otherStableId}
      type='text'
      className='sd-input sd-text'
      value={otherValue}
      onChange={e => {
        onChange(e.target.value)
        setOtherValue(e.target.value)
      }}
    />
  </>
}

export class SurveyQuestionCheckboxMultipleOther extends SurveyQuestionCheckbox {
  protected renderItem(item: Choice, isFirst: boolean, cssClasses: string, index?: string): JSX.Element {
    const otherStableId = item!.jsonObj!.otherStableId
    const otherText = item.jsonObj.otherText
    return <>
      {super.renderItem(item, isFirst, cssClasses, index)}
      {this.question.isItemSelected(item)
        && otherStableId && this.renderOtherItem(otherStableId, otherText || otherStableId)}
    </>
  }

  protected renderOtherItem(otherStableId: string, otherText: string | { [index: string]: string }): JSX.Element {
    const survey: SurveyModel = this.question.survey as SurveyModel

    const text = isString(otherText) ? otherText : otherText[survey.locale || 'en']
    return <>
      <OtherItem
        otherStableId={otherStableId}
        otherText={text}
        value={survey.getValue(otherStableId)}
        onChange={(val: string) => survey.setValue(otherStableId, val)}
      />
    </>
  }
}


ReactQuestionFactory.Instance.registerQuestion('renderer-checkbox-multiple-other', props => {
  return React.createElement(SurveyQuestionCheckboxMultipleOther, props)
})

// Register the custom renderer with the RendererFactory. After this, you can use
// `renderAs: 'radiogroup-multiple-other'` in your JSON and if you provide
// otherStableId and otherText in your choice there will be an input field
// that shows up underneath it when selected.
RendererFactory.Instance.registerRenderer(
  'checkbox',
  'checkbox-multiple-other',
  'renderer-checkbox-multiple-other'
)
