import { Survey } from '@juniper/ui-core'


const DEFAULT_TEMPLATE = '{"pages":[{"elements":[]}]}'
const OUTREACH_TEMPLATE = (survey: Survey) => `
{"pages":[{"elements":[{"type":"html","name":"outreach_content_${survey.stableId}"}]}]}
`
// eslint-disable-next-line max-len
const DOC_REQUEST_TEMPLATE = (survey: Survey) => `
{
"pages":
  [
    {"elements":
      [
        {
          "type":"html",
          "name":"doc_request_content_${survey.stableId}",
          "html": "<h3>${survey.name}</h3><p>Please upload your document here.</p>"
        }
      ]
    }
  ]
}
`

export const getSurveyContentTemplate = (survey: Survey): string => {
  if (survey.surveyType === 'OUTREACH') {
    return OUTREACH_TEMPLATE(survey)
  }
  if (survey.surveyType === 'DOCUMENT_REQUEST') {
    return DOC_REQUEST_TEMPLATE(survey)
  }
  return DEFAULT_TEMPLATE
}
