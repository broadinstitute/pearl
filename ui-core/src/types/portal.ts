import { Survey } from './forms'
import { SiteContent } from './landingPageConfig'
import { Study } from './study'

export type Portal = {
  id: string
  name: string
  shortcode: string
  portalEnvironments: PortalEnvironment[]
  portalStudies: PortalStudy[]
}

export type PortalEnvironmentLanguageOpt = {
  languageCode: string
  languageName: string
}

export type PortalEnvironmentLanguage = PortalEnvironmentLanguageOpt & {
  id: string
}

export type PortalStudy = {
  study: Study
  createdAt: number
}

export type PortalEnvironment = {
  environmentName: string
  portalEnvironmentConfig: PortalEnvironmentConfig
  supportedLanguages: PortalEnvironmentLanguage[]
  siteContent?: SiteContent
  preRegSurvey?: Survey
  preRegSurveyId?: string,
  createdAt: number
}

export type PortalEnvironmentConfig = {
  acceptingRegistration: boolean
  initialized: boolean
  password: string
  passwordProtected: boolean
  participantHostname?: string
  emailSourceAddress?: string
  defaultLanguage: string
  primaryStudy?: string
}

export {}
