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
  RendererFactory,
  SurveyModel
} from 'survey-core'
import {
  isEmpty,
  isString
} from 'lodash'

// see https://surveyjs.io/form-library/examples/create-custom-question-renderer/reactjs#content-code
// for details on how to create a custom renderer

type LocalizedString = string | { [index: string]: string }

// I don't love that we have to do this manually, but
// using the "real" localization stuff via surveyjs
// makes this... a lot more complicated
const renderLocString = (locStr: LocalizedString, locale: string) => {
  if (isString(locStr)) {
    return locStr
  }

  if (isEmpty(locStr) || !locStr[locale]) {
    return locStr['default'] || locStr['en'] || ''
  }

  return locStr[locale]
}

type Choice = ItemValue & {
  jsonObj: {
    otherStableId: string
    otherText: LocalizedString
    otherPlaceholder: LocalizedString
  }
}


const OtherTextbox = ({ stableId, title, value, onChange }: {
  stableId: string
  title?: string
  placeholder?: string
  value: string
  onChange: (value: string) => void
}) => {
  const [otherValue, setOtherValue] = React.useState(value)

  return <div className="my-1">
    {!isEmpty(title) && <label htmlFor={stableId} className="h6 fw-semibold d-block">
      {title}
    </label>}
    <input
      id={stableId}
      type='text'
      className='sd-input sd-text'
      value={otherValue}
      aria-label={title}
      onChange={e => {
        onChange(e.target.value)
        setOtherValue(e.target.value)
      }}
    />
  </div>
}

export class SurveyQuestionCheckboxMultipleOther extends SurveyQuestionCheckbox {
  protected renderItem(item: Choice, isFirst: boolean, cssClasses: string, index?: string): JSX.Element {
    const otherStableId = item!.jsonObj!.otherStableId
    const otherText = item.jsonObj.otherText

    return <>
      {super.renderItem(item, isFirst, cssClasses, index)}
      {this.question.isItemSelected(item)
        && otherStableId && this.renderOtherItem(otherStableId, otherText)}
    </>
  }

  protected renderOtherItem(
    otherStableId: string,
    otherText?: string | { [index: string]: string },
    otherPlaceholder?: string | { [index: string]: string }): JSX.Element {
    const survey: SurveyModel = this.question.survey as SurveyModel

    const text = otherText && renderLocString(otherText, survey.getLocale())
    const placeholder = otherPlaceholder && renderLocString(otherPlaceholder, survey.getLocale())

    return <>
      <OtherTextbox
        stableId={otherStableId}
        title={text}
        placeholder={placeholder}
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
