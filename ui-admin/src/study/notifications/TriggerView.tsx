import React, { useState } from 'react'

import {
  StudyEnvContextT,
  triggerPath
} from '../StudyEnvironmentRouter'
import {
  useNavigate,
  useParams
} from 'react-router-dom'
import Select from 'react-select'
import TestEmailSender from './TestEmailSender'
import Api, { Trigger } from 'api/api'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import {
  doApiLoad,
  useLoadingEffect
} from 'api/api-utils'
import { LoadedPortalContextT } from 'portal/PortalProvider'
import LoadingSpinner from 'util/LoadingSpinner'
import EmailTemplateEditor from './EmailTemplateEditor'
import { Modal } from 'react-bootstrap'
import {
  ParticipantTaskStatus,
  TriggerDeliveryType,
  TriggerScope
} from '@juniper/ui-core'
import InfoPopup from 'components/forms/InfoPopup'
import { TextInput } from 'components/forms/TextInput'
import TriggerBaseForm, {
  isAction,
  isAdminNotification,
  isNotification,
  isTaskReminder
} from './TriggerBaseForm'
import { triggerName } from '../workflow/WorkflowView'
import { Button, EllipsisDropdownButton } from '../../components/forms/Button'

const deliveryTypeOptions: { label: string, value: TriggerDeliveryType}[] = [
  { label: 'Email', value: 'EMAIL' }
]

const statusOptions: {label: string, value: ParticipantTaskStatus}[] = [
  { label: 'New', value: 'NEW' },
  { label: 'In progress', value: 'IN_PROGRESS' },
  { label: 'Completed', value: 'COMPLETE' },
  { label: 'Declined', value: 'REJECTED' }
]

const scopeOptions: {label: string, value: TriggerScope}[] = [
  { label: 'Portal', value: 'PORTAL' },
  { label: 'Study', value: 'STUDY' }
]

/** for viewing and editing a notification config.  saving not yet implemented */
export default function TriggerView({ studyEnvContext, portalContext }:
                                                 {
                                                   studyEnvContext: StudyEnvContextT,
                                                   portalContext: LoadedPortalContextT
                                                 }) {
  const { currentEnv, portal, study, currentEnvPath } = studyEnvContext
  const [showSendModal, setShowSendModal] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const navigate = useNavigate()

  const triggerId = useParams().triggerId
  const [trigger, setTrigger] = useState<Trigger>()
  const [workingTrigger, setWorkingTrigger] = useState<Trigger>()
  const hasTemplate = !!workingTrigger?.emailTemplate

  const { isLoading, setIsLoading } = useLoadingEffect(async () => {
    if (!triggerId) { return }
    const loadedConfig = await Api.findTrigger(portal.shortcode, study.shortcode, currentEnv.environmentName,
      triggerId)
    setTrigger(loadedConfig)
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
      await portalContext.reloadPortal(portal.shortcode)
      navigate(-1)
    }
  }

  // const triggerName = eventTypeOptions.find(opt => workingTrigger?.eventType +
  //   workingTrigger?.triggerType === 'TASK_REMINDER' ? 'Reminder' : 'Notification'

  return <div className="container row">
    {!isLoading && !!workingTrigger && <form className="bg-white my-2">
      <div className="d-flex justify-content-between">
        <h2 className="h3">{triggerName(workingTrigger)}</h2>
        <div className="d-flex">
          <Button variant="primary" onClick={saveConfig}>Save</Button>
          <EllipsisDropdownButton aria-label="form options menu" className="ms-3"/>
          <div className="dropdown-menu">
            <ul className="list-unstyled pt-3">
              { (isNotification(workingTrigger) || isAdminNotification(workingTrigger)) && <li>
                <button className="dropdown-item" type="button"
                  onClick={() => setShowSendModal(true)}>Send test email
                </button>
              </li> }
              { (isNotification(workingTrigger) && workingTrigger.id) && <li>
                <button className="dropdown-item"
                  onClick={() => navigate('notifications')}>
                  View sent notifications
                </button>
              </li> }
              <li>
                <button className="dropdown-item" type="button" onClick={() => setShowDeleteModal(true)}>
                  Delete
                </button>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <TriggerBaseForm trigger={workingTrigger} setTrigger={setWorkingTrigger} readOnly={!!workingTrigger.id}/>
      { isTaskReminder(workingTrigger) && <div>
        <div>
          <label className="form-label mt-3">Remind after
            <div className="d-flex align-items-center">
              <input className="form-control me-2" type="number" value={workingTrigger.afterMinutesIncomplete / 60}
                onChange={e => setWorkingTrigger(
                  { ...workingTrigger, afterMinutesIncomplete: parseInt(e.target.value) * 60 || 0 }
                )}/>
              hours
            </div>
          </label>
        </div>
        <div>
          <label className="form-label">Repeat reminder after
            <div className="d-flex align-items-center">
              <input className="form-control me-2" type="number" value={workingTrigger.reminderIntervalMinutes / 60}
                onChange={e => setWorkingTrigger(
                  { ...workingTrigger, reminderIntervalMinutes: parseInt(e.target.value) * 60 || 0 }
                )}
              />
              hours</div>
          </label>
        </div>
        <div>
          <label className="form-label">Max reminders
            <input className="form-control" type="number" value={workingTrigger.maxNumReminders}
              onChange={e => setWorkingTrigger(
                { ...workingTrigger, maxNumReminders: parseInt(e.target.value) || 0 }
              )}/>
          </label>
        </div>
      </div>
      }
      {(isNotification(workingTrigger) || isAdminNotification(workingTrigger)) && <div>
        <div>
          <label className="form-label">Delivery
            <Select options={deliveryTypeOptions} isDisabled={true}
              value={deliveryTypeOptions.find(opt => opt.value === workingTrigger.deliveryType)}/>
          </label>
        </div>
        {hasTemplate &&
            <EmailTemplateEditor emailTemplate={workingTrigger.emailTemplate}
              portalShortcode={portal.shortcode}
              updateEmailTemplate={updatedTemplate => setWorkingTrigger(currentConfig => {
                // we have to use currentConfig since the template editor might call a stale version of this handler
                // due to the unlayer event listener setup
                return {
                  ...currentConfig!,
                  emailTemplate: {
                    ...updatedTemplate,
                    id: undefined,
                    publishedVersion: undefined,
                    version: trigger ? trigger.emailTemplate.version + 1 : 1
                  }
                }
              })}/>}
      </div> }

      { isAction(workingTrigger) && <div className="mb-4">
        <div>
          <label className="form-label mt-3" htmlFor="triggerScope">Action scope</label> <InfoPopup content={
            'Whether the action is confined to the study, or can impact tasks in the portal.'}/>
          <Select options={scopeOptions} inputId="triggerScope"
            value={scopeOptions.find(opt => opt.value === workingTrigger?.actionScope)}
            onChange={opt =>
              setWorkingTrigger({ ...workingTrigger, actionScope: opt?.value ?? scopeOptions[0].value })}
          />
        </div>
        <div>
          <label className="form-label mt-3" htmlFor="updateToStatus">Updated status</label> <InfoPopup content={
            'The status the task will be updated to when the trigger is activated.'}/>
          <Select options={statusOptions} inputId="updateToStatus"
            value={statusOptions.find(opt => opt.value === workingTrigger?.statusToUpdateTo)}
            onChange={opt =>
              setWorkingTrigger({ ...workingTrigger, statusToUpdateTo: opt?.value ?? statusOptions[0].value })}
          />
        </div>
        <div>
          <label className="form-label mt-3" htmlFor="updateTaskTargetStableId">Target stable id </label>
          <InfoPopup content={<span>
            the stable id of the task to update. For survey tasks, this is the survey stable id.
          </span>}/>
          <TextInput value={workingTrigger.updateTaskTargetStableId} id="updateTaskTargetStableId"
            onChange={v => setWorkingTrigger({ ...workingTrigger, updateTaskTargetStableId: v })}/>
        </div>
      </div> }

      { isAction(workingTrigger) && <div className="mb-4">
        <div>
          <label className="form-label mt-3" htmlFor="triggerScope">Action scope</label> <InfoPopup content={
            'Whether the action is confined to the study, or can impact tasks in the portal.'}/>
          <Select options={scopeOptions} inputId="triggerScope"
            value={scopeOptions.find(opt => opt.value === workingTrigger?.actionScope)}
            onChange={opt =>
              setWorkingTrigger({ ...workingTrigger, actionScope: opt?.value ?? scopeOptions[0].value })}
          />
        </div>
        <div>
          <label className="form-label mt-3" htmlFor="updateToStatus">Updated status</label> <InfoPopup content={
            'The status the task will be updated to when the trigger is activated.'}/>
          <Select options={statusOptions} inputId="updateToStatus"
            value={statusOptions.find(opt => opt.value === workingTrigger?.statusToUpdateTo)}
            onChange={opt =>
              setWorkingTrigger({ ...workingTrigger, statusToUpdateTo: opt?.value ?? statusOptions[0].value })}
          />
        </div>
        <div>
          <label className="form-label mt-3" htmlFor="updateTaskTargetStableId">Target stable id </label>
          <InfoPopup content={<span>
            the stable id of the task to update. For survey tasks, this is the survey stable id.
          </span>}/>
          <TextInput value={workingTrigger.updateTaskTargetStableId} id="updateTaskTargetStableId"
            onChange={v => setWorkingTrigger({ ...workingTrigger, updateTaskTargetStableId: v })}/>
        </div>
      </div> }
      {showSendModal && <TestEmailSender studyEnvParams={{
        portalShortcode: portal.shortcode,
        envName: currentEnv.environmentName, studyShortcode: study.shortcode
      }}
      onDismiss={() => setShowSendModal(false)} trigger={workingTrigger}/> }
      {showDeleteModal && (
        <Modal show className="modal" onHide={() => setShowDeleteModal(false)}>
          <Modal.Header closeButton className="danger"><strong>Delete trigger</strong></Modal.Header>
          <Modal.Body>
            <p className="fst-italic">
              Are you sure you want to delete this {triggerName(workingTrigger)}? This cannot be undone.
            </p>
          </Modal.Body>
          <Modal.Footer>
            <button type="button" className="btn btn-secondary" onClick={() => setShowDeleteModal(false)}>Cancel
            </button>
            <button type="button" className="btn btn-danger" onClick={deleteConfig}>Delete</button>
          </Modal.Footer>
        </Modal>
      )}
    </form> }
    {(isNotification(workingTrigger) || isAdminNotification(workingTrigger)) && <div>
      Note the preview above does not guarantee how the email will appear in all browsers and clients. To test this,
      use the &apos;Send test email&apos; button to send test emails to a given email address.
    </div> }
    {isLoading && <LoadingSpinner/>}
  </div>
}
