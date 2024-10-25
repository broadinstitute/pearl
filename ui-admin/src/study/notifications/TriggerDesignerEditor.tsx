import {
  ParticipantTaskStatus,
  Trigger,
  TriggerActionType,
  TriggerDeliveryType,
  TriggerScope,
  TriggerType
} from '@juniper/ui-core'
import React from 'react'
import Select from 'react-select'
import useReactSingleSelect from 'util/react-select-utils'
import {
  InfoCard,
  InfoCardBody,
  InfoCardHeader,
  InfoCardTitle
} from 'components/InfoCard'
import EmailTemplateEditor from 'study/notifications/EmailTemplateEditor'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import InfoPopup from 'components/forms/InfoPopup'
import { TextInput } from 'components/forms/TextInput'


export const TriggerDesignerEditor = (
  {
    studyEnvContext,
    trigger,
    updateTrigger,
    baseFieldsOnly = false,
    sendTestEmail
  } : {
    studyEnvContext: StudyEnvContextT,
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
    baseFieldsOnly?: boolean
    sendTestEmail?: () => void
  }) => {
  return <div>
    <TriggerWhenEditor
      baseFieldsOnly={baseFieldsOnly}
      trigger={trigger}
      updateTrigger={updateTrigger}
    />
    <TriggerActionEditor
      baseFieldsOnly={baseFieldsOnly}
      studyEnvContext={studyEnvContext}
      trigger={trigger}
      updateTrigger={updateTrigger}
      sendTestEmail={sendTestEmail}
    />
  </div>
}


const triggerTypeLabels = {
  EVENT: 'Event',
  TASK_REMINDER: 'Task Reminder',
  AD_HOC: 'Ad hoc'
}

const TriggerWhenEditor = (
  {
    baseFieldsOnly,
    trigger,
    updateTrigger
  }: {
    baseFieldsOnly: boolean
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
  }
) => {
  const updateTriggerType = (val: TriggerType | undefined) => {
    updateTrigger('triggerType', val)
  }

  const {
    onChange: onTriggerTypeChange,
    options: triggerTypeOptions,
    selectedOption: triggerType,
    selectInputId: triggerTypeSelectInputId
  } = useReactSingleSelect(
    ['EVENT', 'TASK_REMINDER', 'AD_HOC'],
    val => {
      return {
        value: val,
        label: triggerTypeLabels[val]
      }
    },
    updateTriggerType,
    trigger.triggerType)

  return <InfoCard>
    <InfoCardHeader>
      <InfoCardTitle title={'Trigger'}/>
    </InfoCardHeader>
    <InfoCardBody>
      <div className='w-100'>
        <div className='d-flex align-items-baseline w-100'>
          <span className={'me-1'}>
            Trigger on
          </span>
          <Select
            options={triggerTypeOptions}
            value={triggerType}
            onChange={onTriggerTypeChange}
            inputId={triggerTypeSelectInputId}
          />
          {trigger.triggerType === 'EVENT'
            && <EventTriggerEditor trigger={trigger} updateTrigger={updateTrigger}/>}
          {trigger.triggerType === 'TASK_REMINDER'
           && <TaskReminderTypeEditor trigger={trigger} updateTrigger={updateTrigger}/>}

        </div>


        {!baseFieldsOnly && <>
          {trigger.triggerType === 'TASK_REMINDER'
            && <>
              <HorizontalBar/>
              <TaskReminderEditor trigger={trigger} updateTrigger={updateTrigger}/>
            </>}
        </>
        }

      </div>
    </InfoCardBody>
  </InfoCard>
}

const HorizontalBar = () => {
  return <div
    className='w-100 border-top border-1 my-3'
  />
}

const eventTypeOptions = [
  { label: 'Study Enrollment', value: 'STUDY_ENROLLMENT' },
  { label: 'Study Consent', value: 'STUDY_CONSENT' },
  { label: 'Survey Response', value: 'SURVEY_RESPONSE' },
  { label: 'Kit Sent', value: 'KIT_SENT' },
  { label: 'Kit Returned', value: 'KIT_RECEIVED' }
]

const EventTriggerEditor = (
  {
    trigger,
    updateTrigger
  } : {
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
  }) => {
  return <div className="d-flex ms-2 align-items-baseline">
    <Select
      options={eventTypeOptions}
      inputId="eventName"
      aria-label='Event'
      value={eventTypeOptions.find(opt => opt.value === trigger.eventType)}
      onChange={opt =>
        updateTrigger('eventType', opt?.value ?? eventTypeOptions[0].value)}
    />

  </div>
}

const taskTypeOptions = [
  { label: 'Surveys', value: 'SURVEY' },
  { label: 'Outreach surveys', value: 'OUTREACH' },
  { label: 'Consent surveys', value: 'CONSENT' },
  { label: 'Kit requests', value: 'KIT_REQUEST' }
]
const TaskReminderTypeEditor = (
  {
    trigger,
    updateTrigger
  }: {
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
  }
) => {
  return <>
    <label className="form-label mx-2" htmlFor="taskType">for</label>
    <Select options={taskTypeOptions} inputId="taskType"
      value={taskTypeOptions.find(opt => opt.value === trigger.taskType)}
      onChange={opt => updateTrigger(
        'taskType',
        opt?.value ??
                taskTypeOptions[0].value)}/>
  </>
}

const TaskReminderEditor = (
  {
    trigger,
    updateTrigger
  }: {
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
  }) => {
  return <div>
    <div>
      <label className="form-label">Remind after
        <div className="d-flex align-items-center">
          <input className="form-control me-2" type="number" value={trigger.afterMinutesIncomplete / 60}
            onChange={e => updateTrigger(
              'afterMinutesIncomplete', parseInt(e.target.value) * 60 || 0
            )}/>
          hours
        </div>
      </label>
    </div>
    <div>
      <label className="form-label">Repeat reminder after
        <div className="d-flex align-items-center">
          <input className="form-control me-2" type="number" value={trigger.reminderIntervalMinutes / 60}
            onChange={e => updateTrigger(
              'reminderIntervalMinutes',
              parseInt(e.target.value) * 60 || 0)}/>
          hours</div>
      </label>
    </div>
    <div>
      <label className="form-label">Max reminders
        <input className="form-control" type="number" value={trigger.maxNumReminders}
          onChange={e => updateTrigger(
            'maxNumReminders', parseInt(e.target.value) || 0
          )}/>
      </label>
    </div>
  </div>
}

const actionTypeLabels = {
  NOTIFICATION: 'Send Notification to Participant',
  ADMIN_NOTIFICATION: 'Send Notification to Study Staff',
  TASK_STATUS_CHANGE: 'Change Task Status'
}

const TriggerActionEditor = (
  {
    baseFieldsOnly,
    studyEnvContext,
    trigger,
    updateTrigger,
    sendTestEmail
  }: {
    baseFieldsOnly: boolean
    studyEnvContext: StudyEnvContextT,
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
    sendTestEmail?: () => void
  }
) => {
  const updateActionType = (val: TriggerActionType | undefined) => {
    updateTrigger('actionType', val)
  }

  const {
    onChange: onActionTypeChange,
    options: actionTypeOptions,
    selectedOption: actionType,
    selectInputId: actionTypeSelectInputId
  } = useReactSingleSelect<TriggerActionType>(
    ['NOTIFICATION', 'ADMIN_NOTIFICATION', 'TASK_STATUS_CHANGE'],
    val => {
      return {
        value: val,
        label: actionTypeLabels[val]
      }
    },
    updateActionType,
    trigger.actionType)


  return <InfoCard>
    <InfoCardHeader>
      <InfoCardTitle title={'Action'}/>
    </InfoCardHeader>
    <InfoCardBody>
      <div className='w-100'>
        <div className='d-flex align-items-baseline'>
          <span className={'me-1'}>
            When triggered
          </span>
          <Select
            options={actionTypeOptions}
            value={actionType}
            onChange={onActionTypeChange}
            inputId={actionTypeSelectInputId}
          />
        </div>

        {!baseFieldsOnly && <>
          <div
            className='w-100 border-top border-1 my-3'
          />

          {(trigger.actionType === 'NOTIFICATION' || trigger.actionType === 'ADMIN_NOTIFICATION')
            && <NotificationEditor
              studyEnvContext={studyEnvContext}
              trigger={trigger}
              updateTrigger={updateTrigger}
              sendTestEmail={sendTestEmail}
            />}

          {trigger.actionType === 'TASK_STATUS_CHANGE'
            && <TaskStatusEditor trigger={trigger} updateTrigger={updateTrigger}/>
          }
        </>
        }

      </div>
    </InfoCardBody>
  </InfoCard>
}

const deliveryTypeOptions: { label: string, value: TriggerDeliveryType}[] = [
  { label: 'Email', value: 'EMAIL' }
]


const NotificationEditor = (
  {
    studyEnvContext,
    trigger,
    updateTrigger,
    sendTestEmail
  } : {
    studyEnvContext: StudyEnvContextT,
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
    sendTestEmail?: () => void
  }) => {
  const hasEmailTemplate = !!trigger?.emailTemplate

  return <>
    <label className="form-label mt-2">
      Notification Type <InfoPopup content={'Juniper only supports reminders via email.'}/>
      <Select options={deliveryTypeOptions} isDisabled={true}
        value={deliveryTypeOptions.find(opt => opt.value === trigger.deliveryType)}/>
    </label>
    {hasEmailTemplate &&
        <>
          <EmailTemplateEditor
            emailTemplate={trigger.emailTemplate}
            portalShortcode={studyEnvContext.portal.shortcode}
            updateEmailTemplate={updatedTemplate => updateTrigger('emailTemplate', {
              ...updatedTemplate,
              id: undefined,
              publishedVersion: undefined,
              version: trigger ? trigger.emailTemplate.version + 1 : 1
            })
            }
          />
          {sendTestEmail && <div className="d-flex justify-content-center mt-2">
            <button type="button" className="btn btn-secondary ms-4"
              onClick={() => sendTestEmail()}>Send test email
            </button>
          </div>}
        </>}


  </>
}

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

const TaskStatusEditor = (
  {
    trigger,
    updateTrigger
  } : {
    trigger: Trigger,
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
  }
) => {
  return <div>
    <div>
      <label className="form-label" htmlFor="triggerScope">Action scope</label> <InfoPopup content={
        'Whether the action is confined to the study, or can impact tasks in the portal.'}/>
      <Select options={scopeOptions} inputId="triggerScope"
        value={scopeOptions.find(opt => opt.value === trigger.actionScope)}
        onChange={opt =>
          updateTrigger('actionScope', opt?.value ?? scopeOptions[0].value)}
      />
    </div>
    <div>
      <label className="form-label mt-3" htmlFor="updateToStatus">Updated status</label> <InfoPopup content={
        'The status the task will be updated to when the trigger is activated.'}/>
      <Select options={statusOptions} inputId="updateToStatus"
        value={statusOptions.find(opt => opt.value === trigger.statusToUpdateTo)}
        onChange={opt => updateTrigger('statusToUpdateTo', opt?.value ?? statusOptions[0].value)}
      />
    </div>
    <div>
      <label className="form-label mt-3" htmlFor="updateTaskTargetStableId">Target stable id </label>
      <InfoPopup content={<span>
            the stable id of the task to update. For survey tasks, this is the survey stable id.
      </span>}/>
      <TextInput value={trigger.updateTaskTargetStableId} id="updateTaskTargetStableId"
        onChange={v => updateTrigger('updateTaskTargetStableId', v)}/>
    </div>
  </div>
}
