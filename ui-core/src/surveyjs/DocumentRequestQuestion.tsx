import React from 'react'
import {
  ElementFactory,
  Question,
  Serializer,
  SurveyModel
} from 'survey-core'
import {
  ReactQuestionFactory,
  SurveyQuestionElementBase
} from 'survey-react-ui'
import { StudyEnvParams } from 'src/types/study'
import { DocumentRequestUpload } from 'src/components/DocumentRequestUpload'

const DOCUMENT_REQUEST_TYPE = 'documentrequest'

export class DocumentRequestModel extends Question {
  getType() {
    return DOCUMENT_REQUEST_TYPE
  }
}

// Add question type metadata for further serialization into JSON
Serializer.addClass(
  DOCUMENT_REQUEST_TYPE,
  [],
  () => {
    return new DocumentRequestModel('')
  },
  'question'
)

ElementFactory.Instance.registerElement(DOCUMENT_REQUEST_TYPE, name => {
  return new DocumentRequestModel(name)
})

// A class that renders questions of the new type in the UI
export class SurveyQuestionDocumentRequest extends SurveyQuestionElementBase {
  get question() {
    return this.questionBase
  }

  get value() {
    return this.question.value || []
  }

  renderElement() {
    const survey = this.question.survey as SurveyModel

    const studyEnvParams = survey.getVariable('studyEnvParams') as StudyEnvParams
    const enrolleeShortcode = survey.getVariable('enrolleeShortcode') as string

    survey.setValue(this.question.name, this.value)

    return <DocumentRequestUpload
      studyEnvParams={studyEnvParams}
      enrolleeShortcode={enrolleeShortcode}
      selectedFiles={this.value}
      setSelectedFiles={files => {
        this.question.value = files
      }}
    />
  }
}

// Register `SurveyQuestionColorPicker` as a class that renders `color-picker` questions
ReactQuestionFactory.Instance.registerQuestion(DOCUMENT_REQUEST_TYPE, props => {
  return React.createElement(SurveyQuestionDocumentRequest, props)
})
