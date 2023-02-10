import React from 'react'
import { Link } from 'react-router-dom'
import { ParticipantTask } from 'api/api'
import { faChevronRight, faCircleCheck, faCircleHalfStroke, faTimesCircle } from '@fortawesome/free-solid-svg-icons'
import { faCircle } from '@fortawesome/free-regular-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

export type StatusDisplayInfo = {
  icon: React.ReactNode,
  statusDisplay: string,
  actionDisplay?: React.ReactNode
}

const statusDisplayMap: Record<string, StatusDisplayInfo> = {
  'COMPLETE': {
    icon: <FontAwesomeIcon className="me-1" icon={faCircleCheck}/>,
    statusDisplay: 'Completed', actionDisplay: 'View'
  },
  'IN_PROGRESS': {
    icon: <FontAwesomeIcon className="me-1" icon={faCircleHalfStroke}/>,
    statusDisplay: 'In progress',
    actionDisplay: <span>Start <FontAwesomeIcon icon={faChevronRight}/></span>
  },
  'NEW': {
    icon: <FontAwesomeIcon icon={faCircle}/>,
    statusDisplay: 'Not started',
    actionDisplay: <span>Start <FontAwesomeIcon className="me-1" icon={faChevronRight}/></span>
  },
  'REJECTED': {
    icon: <FontAwesomeIcon className="me-1" icon={faTimesCircle}/>,
    statusDisplay: 'Declined', actionDisplay: 'View'
  }
}

/**
 *  Renders a link allowing a participant to complete a task
 *  TODO-i18n this uses the task name, which is pulled from the consent/survey name, which is not i18n-able
 *  when we upgrade this UI to support i18n, we'll have to pull task titles by loading parts of the forms themselves,
 *  which do support i18n, and then loading from there.
 * */
export default function TaskLink({ task, studyShortcode, enrolleeShortcode }:
                                   { task: ParticipantTask, studyShortcode: string, enrolleeShortcode: string }) {
  return <div className="d-flex flex-row" style={{
    padding: '1.25em 2em',
    borderBottom: '1px solid #e4e4e4',
    width: '100%'
  }}>
    <div className="flex-grow-1">{task.targetName}</div>
    <div className="ms-3">
      {statusDisplayMap[task.status].icon} {statusDisplayMap[task.status].statusDisplay}
    </div>
    <div className="fw-bold ms-3 text-end" style={{ minWidth: '8em' }}><Link
      to={getTaskPath(task, enrolleeShortcode, studyShortcode)}>{statusDisplayMap[task.status].actionDisplay}</Link>
    </div>
  </div>
}

/** returns a string for including in a <Link to={}> link to be navigated by the participant */
export function getTaskPath(task: ParticipantTask, enrolleeShortcode: string, studyShortcode: string): string {
  if (task.taskType === 'CONSENT') {
    return `study/${studyShortcode}/enrollee/${enrolleeShortcode}/consent/${task.targetStableId}`
      + `/${task.targetAssignedVersion}?taskId=${task.id}`
  } else if (task.taskType === 'SURVEY') {
    return `/hub/study/${studyShortcode}/enrollee/${enrolleeShortcode}/survey/${task.targetStableId}`
      + `/${task.targetAssignedVersion}?taskId=${task.id}`
  }
  return ''
}


