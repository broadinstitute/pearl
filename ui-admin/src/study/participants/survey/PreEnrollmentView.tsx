import React from 'react'
import SurveyFullDataView from './SurveyFullDataView'
import { Answer, Survey } from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircleNodes } from '@fortawesome/free-solid-svg-icons'
import { PreEnrollmentResponse, ReferralSource } from '@juniper/ui-core'

/** show a preEnrollment response */
export default function PreEnrollmentView({ studyEnvContext, preEnrollResponse, preEnrollSurvey }: {
  studyEnvContext: StudyEnvContextT, preEnrollResponse?: PreEnrollmentResponse, preEnrollSurvey: Survey
}) {
  if (!preEnrollResponse) {
    return <span className="text-muted fst-italic"> no pre-enrollment data</span>
  }
  const answers: Answer[] = JSON.parse(preEnrollResponse.fullData)

  let parsedReferralSource
  if (preEnrollResponse.referralSource) {
    parsedReferralSource = JSON.parse(preEnrollResponse.referralSource) as ReferralSource
  }

  return <div>
    <h5>Pre-enrollment response</h5>
    {parsedReferralSource && <div className="border p-3 rounded-3 my-3">
      <span><FontAwesomeIcon className={'fa-lg'} icon={faCircleNodes}/> This participant was referred by </span>
      <code>{parsedReferralSource.referringSite}</code>
    </div>}
    {preEnrollResponse &&
      <SurveyFullDataView answers={answers} survey={preEnrollSurvey} studyEnvContext={studyEnvContext}/>
    }
  </div>
}
