import React, { useEffect, useRef, useState } from 'react'

import { VersionedForm } from 'api/api'

import { Button } from 'components/forms/Button'
import { FormContentEditor } from 'forms/FormContentEditor'
import LoadedLocalDraftModal from 'forms/designer/modals/LoadedLocalDraftModal'
import DiscardLocalDraftModal from 'forms/designer/modals/DiscardLocalDraftModal'
import { deleteDraft, FormDraft, getDraft, saveDraft } from 'forms/designer/utils/formDraftUtils'

type SurveyEditorViewProps = {
  currentForm: VersionedForm
  readOnly?: boolean
  onCancel: () => void
  onSave: (update: { content: string }) => Promise<void>
}

/** renders a survey for editing/viewing */
const SurveyEditorView = (props: SurveyEditorViewProps) => {
  const {
    currentForm,
    readOnly = false,
    onCancel,
    onSave
  } = props

  const FORM_DRAFT_KEY = `surveyDraft_${currentForm.id}_${currentForm.version}`

  const [isEditorValid, setIsEditorValid] = useState(true)
  const [saving, setSaving] = useState(false)
  const [savingDraft, setSavingDraft] = useState(false)

  //Let the user know if we loaded a draft from local storage when the component first renders. It's important
  //for them to know if the version of the survey they're seeing are from a draft or are actually published.
  const [showLoadedDraftModal, setShowLoadedDraftModal] = useState(!!getDraft({ formDraftKey: FORM_DRAFT_KEY }))
  const [showDiscardDraftModal, setShowDiscardDraftModal] = useState(false)

  const [draft, setDraft] = useState<FormDraft | undefined>(
    !readOnly ? getDraft({ formDraftKey: FORM_DRAFT_KEY }) : undefined)

  const isSaveEnabled = !!draft && isEditorValid && !saving

  const draftRef = useRef<FormDraft | undefined>()
  draftRef.current = draft || undefined

  useEffect(() => {
    const saveToLocalStorage = () => {
      if (draftRef.current) {
        saveDraft({
          formDraftKey: FORM_DRAFT_KEY,
          draft: draftRef.current,
          setSavingDraft
        })
      }
    }

    const draftSaveInterval = setInterval(saveToLocalStorage, 5000)
    return () => {
      clearInterval(draftSaveInterval)
    }
  }, [])

  const onClickSave = async () => {
    if (!isSaveEnabled) {
      return
    }
    setSaving(true)
    try {
      await onSave({ content: draft?.content })
      //Once we've persisted the form draft to the database, there's no need to keep it in local storage.
      //Future drafts will have different FORM_DRAFT_KEYs anyway, as they're based on the form version number.
      deleteDraft({ formDraftKey: FORM_DRAFT_KEY })
      setDraft(undefined)
    } finally {
      setSaving(false)
    }
  }

  const onClickCancel = () => {
    if (draft) {
      setShowDiscardDraftModal(true)
    } else {
      onCancel()
    }
  }

  return (
    <div className="SurveyView d-flex flex-column flex-grow-1 mx-1 mb-1">
      <div className="d-flex p-2 align-items-center">
        <div className="d-flex flex-grow-1">
          <h5>{currentForm.name}
            <span className="detail me-2 ms-2">version {currentForm.version}</span>
          </h5>
        </div>
        { savingDraft && <span className="detail me-2 ms-2">Saving draft...</span> }
        {!readOnly && (
          <Button
            disabled={!isSaveEnabled}
            className="me-md-2"
            tooltip={(() => {
              if (!draft) {
                return 'Form is unchanged. Make changes to save.'
              }
              if (!isEditorValid) {
                return 'Form is invalid. Correct to save.'
              }
              return 'Save changes'
            })()}
            variant="primary"
            onClick={onClickSave}
          >
            Save
          </Button>
        )}
        <button className="btn btn-secondary" type="button"
          onClick={onClickCancel}>Cancel</button>
        { showLoadedDraftModal && draft &&
            <LoadedLocalDraftModal
              onDismiss={() => setShowLoadedDraftModal(false)}
              lastUpdated={draft?.date}/>}
        { showDiscardDraftModal && draft &&
            <DiscardLocalDraftModal
              formDraftKey={FORM_DRAFT_KEY}
              onExit={() => onCancel()}
              onSaveDraft={() =>
                saveDraft({
                  formDraftKey: FORM_DRAFT_KEY,
                  draft,
                  setSavingDraft
                })}
              onDismiss={() => setShowDiscardDraftModal(false)}
            />}
      </div>
      <FormContentEditor
        initialContent={draft?.content || currentForm.content} //favor loading the draft, if we find one
        readOnly={readOnly}
        onChange={(isValid, newContent) => {
          if (isValid) {
            setDraft({ content: JSON.stringify(newContent), date: Date.now() })
          }
          setIsEditorValid(isValid)
        }}
      />
    </div>
  )
}

export default SurveyEditorView
