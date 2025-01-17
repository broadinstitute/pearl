import React from 'react'
import Api, { PreregistrationResponse, Survey } from 'api/api'
import { RegistrationContextT } from './PortalRegistrationRouter'
import { useNavigate } from 'react-router-dom'
import { EnvironmentName, getResumeData, getSurveyJsAnswerList, useI18n, useSurveyJSModel } from '@juniper/ui-core'
import { usePortalEnv } from 'providers/PortalProvider'

/** Renders a preregistration form, and handles submitting the user-inputted response */
export default function PreRegistration({ registrationContext }: { registrationContext: RegistrationContextT }) {
  const { preRegSurvey, updatePreRegResponseId } = registrationContext
  const navigate = useNavigate()
  const { selectedLanguage } = useI18n()
  const { portalEnv } = usePortalEnv()
  const survey = preRegSurvey as Survey
  // for now, we assume all pre-screeners are a single page
  const pager = { pageNumber: 0, updatePageNumber: () => 0 }
  const { surveyModel, refreshSurvey, SurveyComponent } =
    useSurveyJSModel(survey, null, handleComplete, pager, portalEnv.environmentName as EnvironmentName)

  surveyModel.locale = selectedLanguage || 'default'

  /** submit the form */
  function handleComplete() {
    if (!surveyModel) {
      return
    }
    const responseDto: Partial<PreregistrationResponse> = {
      resumeData: getResumeData(surveyModel, null),
      answers: getSurveyJsAnswerList(surveyModel, selectedLanguage),
      surveyId: survey.id,
      qualified: surveyModel.getCalculatedValueByName('qualified').value
    }
    const qualified = surveyModel.getCalculatedValueByName('qualified').value
    const preRegResponse = { ...responseDto, qualified } as PreregistrationResponse
    // submit the form even if it isn't eligible, so we can track stats on exclusions
    Api.submitPreRegResponse({
      surveyStableId: survey.stableId,
      surveyVersion: survey.version,
      preRegResponse
    }).then(result => {
      if (!qualified) {
        updatePreRegResponseId(null)
        navigate('../ineligible')
      } else {
        updatePreRegResponseId(result.id as string)
      }
    }).catch(() => {
      updatePreRegResponseId(null)
      // SurveyJS doesn't support "uncompleting" surveys, so we have to reinitialize it
      // (for now we assume prereg is only a single page)
      refreshSurvey(surveyModel.data, 1)
    })
  }

  return <div>
    {SurveyComponent}
  </div>
}
