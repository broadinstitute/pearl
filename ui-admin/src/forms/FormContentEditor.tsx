import React, { useState } from 'react'
import { Tab, Tabs } from 'react-bootstrap'

import { AnswerMapping, FormContent, Portal, PortalEnvironmentLanguage, VersionedForm } from '@juniper/ui-core'

import { FormDesigner } from './FormDesigner'
import { OnChangeAnswerMappings, OnChangeFormContent } from './formEditorTypes'
import { FormContentJsonEditor } from './FormContentJsonEditor'
import { FormPreview } from './FormPreview'
import { validateFormContent } from './formContentValidation'
import ErrorBoundary from 'util/ErrorBoundary'
import { isEmpty } from 'lodash'
import useStateCallback from '../util/useStateCallback'
import AnswerMappingEditor from '../study/surveys/AnswerMappingEditor'
import { SideBySideFormDesigner } from './designer/SideBySideFormDesigner'
import { userHasPermission, useUser } from '../user/UserProvider'

type FormContentEditorProps = {
  portal: Portal
  initialContent: string
  initialAnswerMappings: AnswerMapping[]
  visibleVersionPreviews: VersionedForm[]
  supportedLanguages: PortalEnvironmentLanguage[]
  currentLanguage: PortalEnvironmentLanguage
  readOnly: boolean
  onFormContentChange: OnChangeFormContent
  onAnswerMappingChange: OnChangeAnswerMappings
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const FormContentEditor = (props: FormContentEditorProps) => {
  const {
    portal,
    initialContent,
    initialAnswerMappings,
    visibleVersionPreviews,
    supportedLanguages,
    currentLanguage,
    readOnly,
    onFormContentChange,
    onAnswerMappingChange
  } = props

  const [activeTab, setActiveTab] = useState<string | null>('designer-v2')
  const [tabsEnabled, setTabsEnabled] = useState(true)
  const { user } = useUser()

  const [editedContent, setEditedContent] = useStateCallback(() => JSON.parse(initialContent) as FormContent)

  console.log(editedContent)

  return (
    <div className="FormContentEditor d-flex flex-column flex-grow-1">
      <Tabs
        activeKey={activeTab ?? undefined}
        className="mb-1"
        mountOnEnter
        unmountOnExit
        onSelect={setActiveTab}
      >
        <Tab
          disabled={activeTab !== 'designer' && !tabsEnabled}
          eventKey="designer"
          title="Designer"
        >
          <ErrorBoundary>
            <FormDesigner
              readOnly={readOnly}
              content={editedContent}
              currentLanguage={currentLanguage}
              supportedLanguages={supportedLanguages}
              onChange={(newContent, callback?: () => void) => {
                setEditedContent(newContent, callback)
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
        {userHasPermission(user, portal.id, 'prototype_tester') && <Tab
          disabled={activeTab !== 'designer-v2' && !tabsEnabled}
          eventKey="designer-v2"
          title={<>Designer<span className='badge bg-primary fw-light ms-2'>BETA</span></>}
        >
          <ErrorBoundary>
            <SideBySideFormDesigner
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
        </Tab> }
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
          disabled={activeTab !== 'preview' && !tabsEnabled}
          eventKey="preview"
          title="Preview"
        >
          <ErrorBoundary>
            <FormPreview formContent={editedContent} currentLanguage={currentLanguage} />
          </ErrorBoundary>
        </Tab>
        { visibleVersionPreviews.map(form =>
          <Tab
            key={`preview${form.version}`}
            eventKey={`preview${form.version}`}
            title={`Version ${form.version}`}
          >
            <FormPreview
              formContent={JSON.parse(form.content) as FormContent}
              currentLanguage={currentLanguage}
            />
          </Tab>
        )}
      </Tabs>
    </div>
  )
}
