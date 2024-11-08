import { PortalContext } from 'portal/PortalProvider'
import { useContext } from 'react'
import { usePortalEnvParamsFromPath } from 'portal/PortalRouter'
import { useStudyEnvParamsFromPath } from 'study/StudyEnvironmentRouter'

/**
 * Returns the default language for the current portal environment.
 */
export function usePortalLanguage() {
  const { portal } = useContext(PortalContext)
  //The language selector can be used within the context of a portal environment or a study environment,
  //so we'll check for the presence of either environment in the path
  const portalEnvParams = usePortalEnvParamsFromPath()
  const studyEnvParams = useStudyEnvParamsFromPath()
  const envName: string | undefined = portalEnvParams.portalEnv || studyEnvParams.envName

  const defaultLanguage = portal?.portalEnvironments.find(env =>
    env.environmentName === envName)?.portalEnvironmentConfig.defaultLanguage

  const supportedLanguages = portal?.portalEnvironments.find(env =>
    env.environmentName === envName)?.supportedLanguages || []

  const language = supportedLanguages.find(lang => lang.languageCode === defaultLanguage)

  if (!language) {
    return {
      defaultLanguage: {
        languageCode: 'en',
        languageName: 'English',
        id: ''
      },
      supportedLanguages
    }
  }

  return { defaultLanguage: language, supportedLanguages }
}
