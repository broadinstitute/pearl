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
import {
  isNil,
  join
} from 'lodash'

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

  get fileNames() {
    const fileNames = []

    let index = 0
    const survey = this.question.survey as SurveyModel
    while (!isNil(survey.getValue(formatFileIndex(this.question.name, index)))) {
      const value = survey.getValue(formatFileIndex(this.question.name, index))
      fileNames.push(value)
      index++
    }

    return fileNames
  }

  renderElement() {
    const survey = this.question.survey as SurveyModel

    const studyEnvParams: StudyEnvParams = {
      portalShortcode: 'demo',
      studyShortcode: 'heartdemo',
      envName: 'sandbox'
    }//survey.getVariable('studyEnvParams') as StudyEnvParams
    const enrolleeShortcode = 'HDSALK'//survey.getVariable('enrolleeShortcode') as string

    return <DocumentRequestUpload
      studyEnvParams={studyEnvParams}
      enrolleeShortcode={enrolleeShortcode}
      selectedFileNames={this.fileNames}
      setSelectedFileNames={fileNames => {
        // clear files
        const numOldFiles = this.fileNames.length
        const numNewFiles = fileNames.length

        const numFiles = Math.max(numOldFiles, numNewFiles)

        for (let i = 0; i < numFiles; i++) {
          if (i < fileNames.length) {
            survey.setValue(formatFileIndex(this.question.name, i), fileNames[i])
          } else {
            survey.setValue(formatFileIndex(this.question.name, i), undefined)
          }
        }

        this.question.value = join(fileNames, ',')
      }}
    />
  }
}

const formatFileIndex = (stableId: string, index: number) => {
  return `${stableId}[${index}]`
}

ReactQuestionFactory.Instance.registerQuestion(DOCUMENT_REQUEST_TYPE, props => {
  return React.createElement(SurveyQuestionDocumentRequest, props)
})
