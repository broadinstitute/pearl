import { PortalEnvironmentChange, StudyEnvironmentChange } from '../api/api'
import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faWrench } from '@fortawesome/free-solid-svg-icons'
import { faClipboardCheck } from '@fortawesome/free-solid-svg-icons/faClipboardCheck'
import { faUsers } from '@fortawesome/free-solid-svg-icons/faUsers'

export const emptyChangeSet: PortalEnvironmentChange = {
  siteContentChange: { changed: false },
  configChanges: [],
  preRegSurveyChanges: { changed: false },
  triggerChanges: { addedItems: [], removedItems: [], changedItems: [] },
  participantDashboardAlertChanges: [],
  studyEnvChanges: [],
  languageChanges: { addedItems: [], removedItems: [], changedItems: [] }
}

export const emptyStudyEnvChange: StudyEnvironmentChange = {
  studyShortcode: '',
  configChanges: [],
  preEnrollSurveyChanges: { changed: false },
  surveyChanges: { addedItems: [], removedItems: [], changedItems: [] },
  triggerChanges: { addedItems: [], removedItems: [], changedItems: [] },
  kitTypeChanges: { addedItems: [], removedItems: [], changedItems: [] }
}


export const ENVIRONMENT_ICON_MAP: Record<string, React.ReactNode> = {
  sandbox: <FontAwesomeIcon className="fa-sm text-gray text-muted" icon={faWrench}/>,
irb: <FontAwesomeIcon className="fa-sm text-gray text-muted" icon={faClipboardCheck}/>,
live: <FontAwesomeIcon className="fa-sm text-gray text-muted" icon={faUsers}/>
}
