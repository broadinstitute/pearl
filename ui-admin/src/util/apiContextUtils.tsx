import {
  ApiContextT,
  EnvironmentName,
  MailingAddress
} from '@juniper/ui-core'
import Api from '../api/api'

/** uses the admin image retrieval endpoint */
const createGetImageUrl = (portalShortcode: string, portalEnvName: string) => {
  return (cleanFileName: string, version: number) =>
        `/api/public/portals/v1/${portalShortcode}/env/${portalEnvName}` +
        `/siteMedia/${version}/${cleanFileName}`
}

/**
 * Returns an ApiContextT for rendering images in the admin UI
 */
export const previewApi = (portalShortcode: string, portalEnvName: string): ApiContextT => {
  const getImageUrl = createGetImageUrl(portalShortcode, portalEnvName)
  return {
    getImageUrl,
    submitMailingListContact: () => Promise.resolve({}),
    getLanguageTexts: (selectedLanguage: string) => {
      return Api.getLanguageTexts(selectedLanguage, portalShortcode, portalEnvName as EnvironmentName)
    },
    updateSurveyResponse: ({ studyEnvParams, stableId, version, enrolleeShortcode, response, taskId }) => {
      return Api.updateSurveyResponse({
        studyEnvParams, stableId, version, enrolleeShortcode, response, taskId
      })
    },
    validateAddress: (address: MailingAddress) => {
      return Api.validateAddress(address)
    },
    loadSystemSettings: () => {
      return Api.loadSystemSettings()
    }
  }
}
