import classNames from 'classnames'
import React, { useCallback, useState } from 'react'

import { FormContent } from '@juniper/ui-core'

import { validateFormContent } from './formContentValidation'
import { OnChangeFormContent } from './formEditorTypes'

type FormContentJsonEditorProps = {
  initialValue: FormContent
  readOnly?: boolean
  onChange: OnChangeFormContent
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const FormContentJsonEditor = (props: FormContentJsonEditorProps) => {
  const { initialValue, readOnly = false, onChange } = props
  const [editorValue, _setEditorValue] = useState(() => JSON.stringify(initialValue, null, 2))
  const [isValid, setIsValid] = useState(true)
  const setEditorValue = useCallback((newEditorValue: string) => {
    _setEditorValue(newEditorValue)
    try {
      const formContent = JSON.parse(newEditorValue)
      validateFormContent(formContent)
      setIsValid(true)
      onChange(true, formContent)
    } catch (e) {
      setIsValid(false)
      onChange(false, undefined)
    }
  }, [])

  return (
    <div className="d-flex flex-column flex-grow-1">
      <textarea
        className={classNames('w-100 flex-grow-1 form-control font-monospace', { 'is-invalid': !isValid })}
        readOnly={readOnly}
        style={{
          overflowX: 'auto',
          resize: 'none',
          // @ts-ignore TS thinks this isn't a valid style property
          textWrap: 'nowrap'
        }}
        value={editorValue}
        onChange={e => { setEditorValue(e.target.value) }}
      />
    </div>
  )
}
