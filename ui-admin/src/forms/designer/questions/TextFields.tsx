import { set, unset } from 'lodash/fp'
import React from 'react'

import { TextQuestion } from '@juniper/ui-core'

import { NumberInput } from 'components/forms/NumberInput'

type TextFieldsProps = {
  disabled: boolean
  question: TextQuestion
  onChange: (newValue: TextQuestion) => void
}

/** Controls for editing fields specific to text questions. */
export const TextFields = (props: TextFieldsProps) => {
  const { disabled, question, onChange } = props

  return (
    <div className="bg-white rounded-bottom-3 p-2 mb-2 border border-top-0">
      <>
        <label className="form-label fw-semibold" htmlFor="text-question-input-type">Input type</label>
        <select
          className="form-select"
          id="text-question-input-type"
          disabled={disabled}
          value={question.inputType || 'text'}
          onChange={e => {
            const newType = e.target.value
            const update = newType === 'text' ? unset('inputType') : set('inputType', newType)
            onChange(update(question))
          }}
        >
          <option value="text">Text</option>
          <option value="number">Number</option>
        </select>
      </>

      {question.inputType === 'number' && (
        <fieldset>
          <div className="my-3">
            <NumberInput
              description="Minimum value accepted for this question."
              disabled={disabled}
              label="Minimum"
              placeholder="Undefined"
              value={question.min}
              onChange={value => {
                const update = value === undefined ? unset('min') : set('min', value)
                onChange(update(question))
              }}
            />
          </div>

          <div>
            <NumberInput
              description="Maximum value accepted for this question."
              disabled={disabled}
              label="Maximum"
              placeholder="Undefined"
              value={question.max}
              onChange={value => {
                const update = value === undefined ? unset('max') : set('max', value)
                onChange(update(question))
              }}
            />
          </div>
        </fieldset>
      )}
    </div>
  )
}
