import React, { useState } from 'react'
import {
  Tab,
  Tabs
} from 'react-bootstrap'

import {
  AnswerMapping,
  FormContent,
  PortalEnvironmentLanguage,
  VersionedForm
} from '@juniper/ui-core'

import {
  OnChangeAnswerMappings,
  OnChangeFormContent
} from './formEditorTypes'
import { FormContentJsonEditor } from './FormContentJsonEditor'
import { FormPreview } from './FormPreview'
import { validateFormContent } from './formContentValidation'
import ErrorBoundary from 'util/ErrorBoundary'
import { isEmpty } from 'lodash'
import useStateCallback from 'util/useStateCallback'
import AnswerMappingEditor from 'study/surveys/AnswerMappingEditor'
import { SplitFormDesigner } from './designer/split/SplitFormDesigner'
import { SplitCalculatedValueDesigner } from 'forms/designer/SplitCalculatedValueDesigner'

type FormContentEditorProps = {
  initialContent: string
  initialAnswerMappings: AnswerMapping[]
  visibleVersionPreviews: VersionedForm[]
  supportedLanguages: PortalEnvironmentLanguage[]
  currentLanguage: PortalEnvironmentLanguage
  readOnly: boolean
  onFormContentChange: OnChangeFormContent
  onAnswerMappingChange: OnChangeAnswerMappings
}

export const FormContentEditor = (props: FormContentEditorProps) => {
  const {
    initialContent,
    initialAnswerMappings,
    supportedLanguages,
    currentLanguage,
    readOnly,
    onFormContentChange,
    onAnswerMappingChange
  } = props

  const [activeTab, setActiveTab] = useState<string | null>('split')
  const [tabsEnabled, setTabsEnabled] = useState(true)

  const [editedContent, setEditedContent] = useStateCallback(() => JSON.parse(initialContent) as FormContent)

  return (
    <div className="FormContentEditor d-flex flex-column flex-grow-1">
      <Tabs
        activeKey={activeTab ?? undefined}
        mountOnEnter
        unmountOnExit
        onSelect={setActiveTab}
        className="px-3"
      >
        <Tab
          disabled={activeTab !== 'split' && !tabsEnabled}
          eventKey="split"
          title="Designer"
        >
          <ErrorBoundary>
            <SplitFormDesigner
              content={editedContent}
              currentLanguage={currentLanguage}
              supportedLanguages={supportedLanguages}
              onChange={(newContent: FormContent) => {
                setEditedContent(newContent)
                try {
                  const errors = validateFormContent(newContent)
                  onFormContentChange(errors, newContent)
                } catch (err) {
                  //@ts-ignore
                  onFormContentChange([err.message], undefined)
                }
              }}
            />
          </ErrorBoundary>
        </Tab>
        <Tab
          disabled={activeTab !== 'json' && !tabsEnabled}
          eventKey="json"
          title="JSON Editor"
        >
          <ErrorBoundary>
            <FormContentJsonEditor
              initialValue={editedContent}
              readOnly={readOnly}
              onChange={(validationErrors, newContent) => {
                if (isEmpty(validationErrors) && newContent) {
                  setEditedContent(newContent)
                  onFormContentChange(validationErrors, newContent)
                } else {
                  onFormContentChange(validationErrors, undefined)
                }
                setTabsEnabled(isEmpty(validationErrors))
              }}
            />
          </ErrorBoundary>
        </Tab>
        <Tab
          disabled={activeTab !== 'answermappings' && !tabsEnabled}
          eventKey="answermappings"
          title="Answer Mappings"
        >
          <AnswerMappingEditor
            formContent={editedContent}
            initialAnswerMappings={initialAnswerMappings}
            onChange={onAnswerMappingChange}
          />
        </Tab>
        <Tab
          title={'Derived Values'}
          eventKey={'derivedvalues'}
          disabled={activeTab !== 'derivedvalues' && !tabsEnabled}
        >
          <SplitCalculatedValueDesigner content={editedContent} onChange={newForm => {
            setEditedContent(newForm)
            onFormContentChange([], newForm)
          }}/>
        </Tab>
        <Tab
          disabled={activeTab !== 'preview' && !tabsEnabled}
          eventKey="preview"
          title="Preview"
        >
          <ErrorBoundary>
            <FormPreview formContent={editedContent} currentLanguage={currentLanguage} />
          </ErrorBoundary>
        </Tab>
      </Tabs>
    </div>
  )
}
