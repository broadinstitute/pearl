import { FormContent } from '@juniper/ui-core'
import React from 'react'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { CalculatedValue } from 'survey-core'
import { SplitCalculatedValueEditor } from 'forms/designer/SplitCalculatedValueEditor'

/**
 * A split-view designer for calculated values. On the left side, you can edit the calculated value expression,
 * and on the right side you can see the questions that are used in the expression. If you fill out the questions,
 * you can get a preview of how the value will be calculated.
 */
export const SplitCalculatedValueDesigner = ({ content, onChange }: {
  content: FormContent, onChange: (newContent: FormContent) => void
}) => {
  return <div className="mt-3 container w-100">
    <div className="d-flex">
      {renderNewElementButton(content, onChange, -1)}
    </div>
    {content && content.calculatedValues && content.calculatedValues.map((_, elementIndex) => (
      <div key={elementIndex} className="container">
        <SplitCalculatedValueEditor
          calculatedValueIndex={elementIndex}
          editedContent={content}
          onChange={onChange}/>
        <div className="d-flex">
          {renderNewElementButton(content, onChange, elementIndex)}
        </div>
      </div>
    ))}
  </div>
}

const renderNewElementButton = (
  formContent: FormContent,
  onChange: (newContent: FormContent) => void,
  calculatedValueIdx: number) => {
  return <div className="my-2">
    <Button variant="secondary"
      aria-label={`Insert a new calculated value`}
      tooltip={`Insert a new calculated value`}
      disabled={false}
      onClick={() => {
        const newContent = { ...formContent }

        if (!newContent.calculatedValues) {
          newContent.calculatedValues = []
        }

        newContent.calculatedValues.splice(calculatedValueIdx + 1, 0, new CalculatedValue())
        onChange(newContent)
      }}>
      <FontAwesomeIcon icon={faPlus}/> Insert calculated value
    </Button>
  </div>
}
