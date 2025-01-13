import React, { useState } from 'react'
import { LoadedPortalContextT } from 'portal/PortalProvider'
import {
  StudyEnvContextT,
  triggerPath
} from 'study/StudyEnvironmentRouter'
import Api from 'api/api'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import {
  doApiLoad,
  useLoadingEffect
} from 'api/api-utils'
import { Trigger } from '@juniper/ui-core'
import {
  useNavigate,
  useParams
} from 'react-router-dom'
import LoadingSpinner from 'util/LoadingSpinner'
import { TriggerDesignerEditor } from 'study/notifications/TriggerDesignerEditor'
import { Modal } from 'react-bootstrap'
import TestEmailSender from 'study/notifications/TestEmailSender'

export const TriggerDesigner = (
  {
    studyEnvContext,
    portalContext,
    onDelete
  } : {
    studyEnvContext: StudyEnvContextT,
    portalContext: LoadedPortalContextT,
    onDelete: () => void
  }
) => {
  const { currentEnv, portal, study, currentEnvPath } = studyEnvContext
  const [showSendModal, setShowSendModal] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const navigate = useNavigate()

  const triggerId = useParams().triggerId
  const [workingTrigger, setWorkingTrigger] = useState<Trigger>()

  const { isLoading, setIsLoading } = useLoadingEffect(async () => {
    if (!triggerId) { return }
    const loadedConfig = await Api.findTrigger(portal.shortcode, study.shortcode, currentEnv.environmentName,
      triggerId)
    setWorkingTrigger(loadedConfig)
  }, [triggerId])

  const saveConfig = async () => {
    if (!workingTrigger) { return }
    doApiLoad(async () => {
      const savedConfig = await Api.updateTrigger(portal.shortcode,
        currentEnv.environmentName, study.shortcode, workingTrigger.id, workingTrigger)
      Store.addNotification(successNotification('Notification saved'))
      await portalContext.reloadPortal(portal.shortcode)
      navigate(triggerPath(savedConfig, currentEnvPath))
    }, { setIsLoading })
  }

  const deleteConfig = async () => {
    if (triggerId) {
      await Api.deleteTrigger(portal.shortcode, study.shortcode, currentEnv.environmentName, triggerId)
      onDelete()
    }
  }

  const updateTrigger = (key: keyof Trigger, value: unknown) => {
    setWorkingTrigger(old => {
      if (!old) { return old }
      return {
        ...old,
        [key]: value
      }
    })
  }

  if (isLoading) {
    return <LoadingSpinner />
  }

  if (!workingTrigger) {
    return <div>Trigger not found</div>
  }

  return <div>
    {!workingTrigger.active &&
        <div className="alert alert-warning mt-0">
            This trigger is no longer active. Inactive triggers cannot be edited.
        </div>}

    <TriggerDesignerEditor
      studyEnvContext={studyEnvContext}
      trigger={workingTrigger}
      updateTrigger={updateTrigger}
      sendTestEmail={() => setShowSendModal(true)}
    />

    <div className="d-flex justify-content-center mt-2">
      <button type="button" className="btn btn-primary" disabled={!workingTrigger.active} onClick={saveConfig}>
        Save
      </button>
      <button
        type="button"
        className="btn btn-danger ms-4"
        disabled={!workingTrigger.active}
        onClick={() => setShowDeleteModal(true)}
      >
        Delete
      </button>
    </div>

    {showDeleteModal && (
      <Modal show className="modal" onHide={() => setShowDeleteModal(false)}>
        <Modal.Header closeButton className="danger"><strong>Delete Trigger</strong></Modal.Header>
        <Modal.Body>
          <p className="fst-italic">
            Are you sure you want to delete this trigger? This cannot be undone.
          </p>
        </Modal.Body>
        <Modal.Footer>
          <button type="button" className="btn btn-secondary" onClick={() => setShowDeleteModal(false)}>Cancel
          </button>
          <button type="button" className="btn btn-danger" onClick={deleteConfig}>Delete</button>
        </Modal.Footer>
      </Modal>
    )}

    {showSendModal && <TestEmailSender studyEnvParams={{
      portalShortcode: portal.shortcode,
      envName: currentEnv.environmentName, studyShortcode: study.shortcode
    }}
    onDismiss={() => setShowSendModal(false)} trigger={workingTrigger}/> }
  </div>
}
