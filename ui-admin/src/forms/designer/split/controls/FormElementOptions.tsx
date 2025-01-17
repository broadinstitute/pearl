import React from 'react'
import { IconButton } from 'components/forms/Button'
import { faClone, faCode, faKeyboard, faWandMagicSparkles } from '@fortawesome/free-solid-svg-icons'
import { ListElementController } from 'portal/siteContent/designer/components/ListElementController'
import { FormContent, FormElement } from '@juniper/ui-core'

type FormElementOptionsProps = {
    showJsonEditor: boolean,
    setShowJsonEditor: (show: boolean) => void,
    showFreetextMode: boolean,
    setShowFreetextMode: (show: boolean) => void,
    elementIndex: number,
    element: FormElement,
    currentPageNo: number,
    editedContent: FormContent,
    onChange: (newContent: FormContent) => void
}

export const FormElementOptions = ({
  showJsonEditor,
  setShowJsonEditor,
  showFreetextMode,
  setShowFreetextMode,
  elementIndex,
  element,
  currentPageNo,
  editedContent,
  onChange
}: FormElementOptionsProps) => {
  return (
    <div className="d-flex justify-content-end">
      <div className="d-flex border rounded-3 rounded-top-0 border-top-0 bg-light">
        <IconButton icon={faCode}
          disabled={showFreetextMode}
          aria-label={
            showJsonEditor ?
              'Switch to designer' :
              showFreetextMode ? 'You must exit the freetext editor to switch to JSON editor' :
                'Switch to JSON editor'
          }
          onClick={() => setShowJsonEditor(!showJsonEditor)}
        />
        <IconButton icon={showFreetextMode ? faKeyboard : faWandMagicSparkles}
          disabled={showJsonEditor}
          aria-label={showFreetextMode ? 'Switch to designer' : 'Switch to freetext editor'}
          onClick={() => setShowFreetextMode(!showFreetextMode)}
        />
        <IconButton icon={faClone}
          aria-label={'Clone'}
          onClick={() => {
            const newContent = { ...editedContent }
            const newQuestion = { ...element, name: '' }
            newContent.pages[currentPageNo].elements.splice(elementIndex + 1, 0, newQuestion)
            onChange(newContent)
            //scroll to the new element
            const newElement = document.getElementById(`element[${elementIndex + 1}]`)
            if (newElement) {
              //wait for the cloned element to be rendered so we can scroll to it
              setTimeout(() => newElement.scrollIntoView({ behavior: 'auto' }), 100)
            }
          }}
        />
        <ListElementController
          index={elementIndex}
          items={editedContent.pages[currentPageNo].elements}
          updateItems={newItems => {
            const newContent = { ...editedContent }
            newContent.pages[currentPageNo].elements = newItems
            onChange(newContent)
          }}
        />
      </div>
    </div>
  )
}
