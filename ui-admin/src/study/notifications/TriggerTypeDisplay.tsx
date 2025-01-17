import React from 'react'
import { Trigger } from 'api/api'

export const deliveryTypeDisplayMap: Record<string, string> = {
  EMAIL: 'email'
}

export const eventTypeDisplayMap: Record<string, string> = {
  PORTAL_REGISTRATION: 'Portal registration',
  SURVEY_RESPONSE: 'Survey completion',
  STUDY_ENROLLMENT: 'Study enrollment',
  STUDY_CONSENT: 'Consent form submission',
  KIT_SENT: 'Kit sent',
  KIT_RECEIVED: 'Kit returned'
}

/** shows a summary of the notification config */
export default function TriggerTypeDisplay({ config }: {config?: Trigger}) {
  if (!config) {
    return <></>
  }
  if (config.triggerType === 'EVENT') {
    return <span>{eventTypeDisplayMap[config.eventType]}</span>
  } else if (config.triggerType === 'TASK_REMINDER') {
    return <span>
      Reminder: {config.taskType} {config.filterTargetStableIds?.length ?
      `(${config.filterTargetStableIds.join(', ')})` : ''}
    </span>
  } else if (config.triggerType === 'AD_HOC') {
    return <span>Ad-Hoc</span>
  }
  return <span>{config.taskType}</span>
}
