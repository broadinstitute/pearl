import React, { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import {
  paramsFromContext,
  StudyEnvContextT,
  studyEnvPreEnrollPath, studyEnvSurveyPath,
  studyEnvTriggerPath,
  triggerPath
} from '../StudyEnvironmentRouter'
import { renderPageHeader } from 'util/pageUtils'
import { LoadedPortalContextT } from '../../portal/PortalProvider'
import { StudyEnvironmentSurvey, StudyEnvParams, Survey, Trigger } from '@juniper/ui-core'
import Api from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import CreateTriggerModal from './CreateTriggerModal'
import { faCheckSquare, faClipboard, faEnvelope, faTasks } from '@fortawesome/free-solid-svg-icons'

const boxClasses = 'border border-1 rounded p-2 my-2'
const itemClasses = 'bg-dark-subtle m-1 py-2 rounded-2 px-3'

/** shows configuration of notifications for a study */
export default function TriggerList({ studyEnvContext, portalContext }:
  {studyEnvContext: StudyEnvContextT, portalContext: LoadedPortalContextT}) {
  const currentEnv = studyEnvContext.currentEnv
  const navigate = useNavigate()
  const [triggers, setTriggers] = useState<Trigger[]>([])
  const [studyEnvSurveys, setStudyEnvSurveys] = useState<StudyEnvironmentSurvey[]>([])
  const [preEnrollSurvey, setPreEnrollSurvey] = useState<Survey>()
  const [previousEnv, setPreviousEnv] = useState<string>(currentEnv.environmentName)
  const [showCreateModal, setShowCreateModal] = useState(false)

  const { isLoading, reload } = useLoadingEffect(async () => {
    const [triggerList, surveys] = await Promise.all([
      Api.findTriggersForStudyEnv(portalContext.portal.shortcode,
        studyEnvContext.study.shortcode, currentEnv.environmentName),
      Api.findConfiguredSurveys(portalContext.portal.shortcode, studyEnvContext.study.shortcode,
        currentEnv.environmentName, true, undefined)
    ])
    if (currentEnv.preEnrollSurveyId) {
      const preEnrollSurvey = await Api.getSurveyById(portalContext.portal.shortcode, currentEnv.preEnrollSurveyId)
      setPreEnrollSurvey(preEnrollSurvey)
    }
    setTriggers(triggerList)
    setStudyEnvSurveys(surveys)
  }, [currentEnv.environmentName, studyEnvContext.study.shortcode])

  useEffect(() => {
    if (previousEnv !== currentEnv.environmentName) {
      // the user has changed the environment -- we need to clear the id off the path if there
      navigate(`${studyEnvContext.currentEnvPath}/notificationContent`)
      setPreviousEnv(currentEnv.environmentName)
    }
  }, [currentEnv.environmentName])

  const onCreate = (createdConfig: Trigger) => {
    reload()
    navigate(triggerPath(createdConfig, studyEnvContext.currentEnvPath))
    setShowCreateModal(false)
  }


  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Workflow') }
    <ul className="list-unstyled">
      {isLoading && <LoadingSpinner/>}
      <li className={boxClasses} >
        <h3 className="h6">Pre-enroll</h3>
        <div className={itemClasses}>
          { preEnrollSurvey &&
          <Link to={studyEnvPreEnrollPath(paramsFromContext(studyEnvContext), preEnrollSurvey.stableId)}>
            <FontAwesomeIcon icon={faClipboard} className="me-2"/>{preEnrollSurvey.name}
          </Link> }
          { !preEnrollSurvey && <><span className="fst-italic">None</span>
            <button className="btn btn-secondary" onClick={() => alert('go to forms page')}>
              <FontAwesomeIcon icon={faPlus}/> Add
            </button>
          </> }
        </div>
      </li>
      <li className={boxClasses} >
        <h3 className="h6">Enrollment</h3>
        <ul className="list-unstyled">
          { triggers.filter(trigger => trigger.eventType === 'STUDY_ENROLLMENT')
            .map(trigger =>
              <TriggerListItem trigger={trigger} key={trigger.id} studyEnvParams={paramsFromContext(studyEnvContext)}/>
            )
          }
        </ul>
      </li>
      <li className={boxClasses}>
        <h3 className="h6">Consents</h3>
        <ul className="list-unstyled">
          { studyEnvSurveys.filter(studyEnvSurveys => studyEnvSurveys.survey.surveyType === 'CONSENT')
            .map(studyEnvSurvey =>
              <SurveyListItem studyEnvSurvey={studyEnvSurvey} key={studyEnvSurvey.id}
                studyEnvParams={paramsFromContext(studyEnvContext)}/>
            )
          }
        </ul>
        <ul className="list-unstyled ms-3">
          { triggers.filter(trigger => trigger.eventType === 'STUDY_CONSENT' || trigger.taskType === 'CONSENT')
            .map(trigger =>
              <TriggerListItem trigger={trigger} key={trigger.id} studyEnvParams={paramsFromContext(studyEnvContext)}/>
            )
          }
        </ul>
      </li>
      <li className={boxClasses}>
        <h3 className="h6">Surveys</h3>
        <ul className="list-unstyled">
          { studyEnvSurveys.filter(studyEnvSurveys => studyEnvSurveys.survey.surveyType === 'RESEARCH')
            .map(studyEnvSurvey =>
              <SurveyListItem studyEnvSurvey={studyEnvSurvey} key={studyEnvSurvey.id}
                studyEnvParams={paramsFromContext(studyEnvContext)}/>
            )
          }
        </ul>
        <ul className="list-unstyled ms-3">
          { triggers.filter(trigger => trigger.eventType === 'SURVEY_RESPONSE' || trigger.taskType === 'SURVEY')
            .map(trigger =>
              <TriggerListItem trigger={trigger} key={trigger.id} studyEnvParams={paramsFromContext(studyEnvContext)}/>
            )
          }
        </ul>
      </li>
    </ul>
    { showCreateModal && <CreateTriggerModal studyEnvParams={paramsFromContext(studyEnvContext)}
      onDismiss={() => setShowCreateModal(false)} onCreate={onCreate}
    /> }
  </div>
}

const SurveyListItem = ({ studyEnvSurvey, studyEnvParams }:
  { studyEnvSurvey: StudyEnvironmentSurvey, studyEnvParams: StudyEnvParams }) => {
  return <li className={itemClasses}>
    <Link to={studyEnvSurveyPath(studyEnvParams, studyEnvSurvey.survey.stableId, studyEnvSurvey.survey.version)}>
      { studyEnvSurvey.survey.surveyType === 'CONSENT' && <FontAwesomeIcon icon={faCheckSquare} className="me-2"/> }
      { studyEnvSurvey.survey.surveyType === 'RESEARCH' && <FontAwesomeIcon icon={faClipboard} className="me-2"/> }
      {studyEnvSurvey.survey.name}
    </Link>
  </li>
}


const TriggerListItem = ({ trigger, studyEnvParams }:
  { trigger: Trigger, studyEnvParams: StudyEnvParams }) => {
  return <li className={itemClasses}>
    { trigger.actionType === 'NOTIFICATION' && <Link to={studyEnvTriggerPath(studyEnvParams, trigger)}>
      <FontAwesomeIcon icon={faEnvelope} className="me-2"/>{trigger.emailTemplate.name}
    </Link>
    }
    { trigger.actionType === 'TASK_STATUS_CHANGE' && <Link to={studyEnvTriggerPath(studyEnvParams, trigger)}>
      <FontAwesomeIcon icon={faTasks} className="me-2"/>
      Mark {trigger.updateTaskTargetStableId} as {trigger.statusToUpdateTo}
    </Link>
    }
  </li>
}
