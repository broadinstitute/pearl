import React from 'react'
import { Survey as SurveyComponent } from 'survey-react-ui'
import Api, { Survey } from 'api/api'
import { RegistrationContextT } from './PortalRegistrationRouter'
import { useUser } from 'providers/UserProvider'
import { useNavigate } from 'react-router-dom'
import {
  defaultSurvey,
  EnvironmentName,
  useI18n,
  useSurveyJSModel
} from '@juniper/ui-core'
import { usePortalEnv } from 'providers/PortalProvider'

/** This registration survey is a hardcoded survey--will be deprecated soon */
const registrationSurvey = {
  'logoPosition': 'right',
  'pages': [
    {
      'name': 'page1',
      'elements': [
        {
          'type': 'html',
          'name': 'question1',
          /* eslint-disable max-len */
          'html': '<h3 class="mt-5 fw-bold text-center">Create your account</h3>\n<p class="p-4 text-center fs-5"> You will use this information to later sign in to your account.  We will use the email address provided to communicate with you.</p>'
        },
        {
          'type': 'text',
          'name': 'reg_email',
          'title': 'Email address',
          'isRequired': true,
          'inputType': 'email',
          'placeholder': 'name@email.com'
        }
      ]
    }
  ]
}

const now = Date.now() / 1000
const registrationSurveyModel: Survey = {
  ...defaultSurvey,
  id: '11111111-1111-1111-1111-111111111111',
  name: 'Registration',
  stableId: 'pearlDefaultRegistration',
  version: 1,
  createdAt: now,
  lastUpdatedAt: now,
  content: JSON.stringify(registrationSurvey),
  surveyType: 'RESEARCH',
  recurrenceType: 'NONE'
}

/**
 * Show the registration page for internal-only login.  Currently deprecated -- use only in cases where
 * B2C is down, or for automation
 * */
export default function RegistrationUnauthed({ registrationContext, returnTo }: {
  registrationContext: RegistrationContextT,
  returnTo: string | null
}) {
  const { preRegResponseId } = registrationContext
  const { portalEnv } = usePortalEnv()
  // for now, assume registration surveys are a single page
  const pager = { pageNumber: 0, updatePageNumber: () => 0 }
  const { surveyModel, refreshSurvey } = useSurveyJSModel(registrationSurveyModel, null, onComplete, pager, portalEnv.environmentName as EnvironmentName)
  const { loginUser } = useUser()
  const { selectedLanguage } = useI18n()
  const navigate = useNavigate()

  /** submit the response */
  function onComplete() {
    if (!surveyModel) {
      return
    }
    const registrationInfo = {
      email: surveyModel.data.reg_email
    }

    const resumeData = surveyModel?.data
    Api.internalRegister({
      preRegResponseId: preRegResponseId as string,
      preferredLanguage: selectedLanguage,
      fullData: registrationInfo
    }).then(response => {
      loginUser({
        user: response.user,
        ppUsers: response.ppUsers,
        profile: response.profile,
        enrollees: response.enrollees,
        proxyRelations: response.proxyRelations
      }, response.user.token)
      if (returnTo) {
        navigate(returnTo)
      }
    }).catch(() => {
      // if there's an error, reshow the survey (for now, assume registration is a single page)
      refreshSurvey(resumeData, 1)
    })
  }

  return <div>
    {(surveyModel) && <SurveyComponent model={surveyModel}/>}
  </div>
}
