import {
  render,
  screen
} from '@testing-library/react'
import React from 'react'

import {
  getDisplayValue,
  ItemDisplay,
  QuestionMetadata,
  toQuestionMetadata
} from './SurveyFullDataView'
import { Answer } from '@juniper/ui-core'
import { DataChangeRecord } from 'api/api'
import { AnswerEditHistory } from './AnswerEditHistory'
import { userEvent } from '@testing-library/user-event'
import {
  CalculatedValue,
  Question
} from 'survey-core'

const mockQuestionMetadata: QuestionMetadata = {
  visible: true, type: 'text', stableId: 'testQ', derived: false, title: 'test question'
}

describe('getDisplayValue', () => {
  it('renders a plaintext value', async () => {
    const question: QuestionMetadata = mockQuestionMetadata
    const answer: Answer = { stringValue: 'test123', questionStableId: 'testQ' } as Answer
    render(<span>{getDisplayValue(answer, question)}</span>)
    expect(screen.getByText('test123')).toBeTruthy()
  })

  it('renders a choice value', async () => {
    const question: QuestionMetadata = {
      ...mockQuestionMetadata,
      type: 'radiogroup',
      choices: [{
        text: 'option 1', value: 'option1Val'
      }, {
        text: 'option 2', value: 'option2Val'
      }]
    }

    const answer: Answer = { stringValue: 'option2Val', questionStableId: 'testQ' } as Answer
    render(<span>{getDisplayValue(answer, question)}</span>)
    expect(screen.getByText('option 2')).toBeTruthy()
  })

  it('renders a free text other description', async () => {
    const question: QuestionMetadata = {
      ...mockQuestionMetadata,
      type: 'radiogroup',
      choices: [{
        text: 'option 1', value: 'option1Val'
      }, {
        text: 'option 2', value: 'option2Val'
      }]
    }

    const answer: Answer = {
      stringValue: 'option2Val', questionStableId: 'testQ',
      otherDescription: 'more detail'
    } as Answer
    render(<span>{getDisplayValue(answer, question)}</span>)
    expect(screen.getByText('option 2 - more detail')).toBeTruthy()
  })

  it('renders a choice array value', async () => {
    const question: QuestionMetadata = {
      ...mockQuestionMetadata,
      type: 'checkbox',
      choices: [{
        text: 'option 1', value: 'option1Val'
      }, {
        text: 'option 2', value: 'option2Val'
      }, {
        text: 'option 3', value: 'option3Val'
      }, {
        text: 'option 4', value: 'option4Val'
      }]
    }

    const answer: Answer = {
      objectValue: JSON.stringify(['option2Val', 'option4Val']),
      questionStableId: 'testQ'
    } as Answer
    render(<span>{getDisplayValue(answer, question)}</span>)
    expect(screen.getByText('["option 2","option 4"]')).toBeTruthy()
  })

  it('renders a signaturepad as an image', async () => {
    const question: QuestionMetadata = {
      ...mockQuestionMetadata,
      type: 'signaturepad'
    }

    const answer: Answer = { stringValue: 'data:image/png;base64, test123', questionStableId: 'testQ' } as Answer
    render(<span>{getDisplayValue(answer, question)}</span>)
    expect(screen.getByRole('img')).toHaveAttribute('src', 'data:image/png;base64, test123')
    expect(screen.queryByText('data:image/png;base64, test123')).not.toBeInTheDocument()
  })

  it('renders a malformed object value as an error', async () => {
    const question: QuestionMetadata = mockQuestionMetadata
    const answer: Answer = { objectValue: '{dfaf }}', questionStableId: 'testQ' } as Answer
    render(<span>{getDisplayValue(answer, question)}</span>)
    expect(screen.getByText('[[ parse error ]]')).toBeInTheDocument()
  })
})

describe('ItemDisplay', () => {
  it('renders the language used to answer a question', async () => {
    const question = mockQuestionMetadata
    const answer: Answer = {
      stringValue: 'test123',
      questionStableId: 'testQ',
      surveyVersion: 1,
      viewedLanguage: 'es'
    } as Answer
    const answerMap: Record<string, Answer> = {}
    answerMap[answer.questionStableId] = answer
    render(<ItemDisplay
      question={question}
      answerMap={answerMap}
      surveyVersion={1}
      supportedLanguages={[{ languageCode: 'es', languageName: 'Spanish', id: '' }]}
      showFullQuestions={true}/>)
    userEvent.click(screen.getAllByText('test123')[0])

    expect(screen.getByText('answered in Spanish')).toBeInTheDocument()
  })

  it('renders correctly if a viewedLanguage is not specified', async () => {
    const question = mockQuestionMetadata
    const answer: Answer = {
      stringValue: 'test123',
      questionStableId: 'testQ',
      surveyVersion: 1
    } as Answer
    const answerMap: Record<string, Answer> = {}
    answerMap[answer.questionStableId] = answer
    render(<ItemDisplay
      question={question}
      answerMap={answerMap}
      surveyVersion={1}
      supportedLanguages={[{ languageCode: 'en', languageName: 'English', id: '' }]}
      showFullQuestions={true}/>)

    expect(screen.getByText('(testQ)')).toBeInTheDocument()
  })

  it('does not render a language name if it doesnt match a supported language', async () => {
    const question = mockQuestionMetadata
    const answer: Answer = {
      stringValue: 'test123',
      questionStableId: 'testQ',
      surveyVersion: 1,
      viewedLanguage: 'fake'
    } as Answer
    const answerMap: Record<string, Answer> = {}
    answerMap[answer.questionStableId] = answer
    render(<ItemDisplay
      question={question}
      answerMap={answerMap}
      surveyVersion={1}
      supportedLanguages={[{ languageCode: 'es', languageName: 'Spanish', id: '' }]}
      showFullQuestions={true}/>)

    expect(screen.getByText('(testQ)')).toBeInTheDocument()
  })

  it('renders an edit history button if the question has been edited', async () => {
    const question = mockQuestionMetadata
    const answer: Answer = {
      stringValue: 'test123',
      questionStableId: 'testQ',
      surveyVersion: 1,
      viewedLanguage: 'es',
      lastUpdatedAt: 1234
    } as Answer

    const changeRecord: DataChangeRecord = {
      id: 'test_id',
      createdAt: 1234,
      modelName: 'test_survey',
      fieldName: 'testQ',
      oldValue: 'test123',
      newValue: 'test456',
      responsibleUserId: 'test_user'
    }

    const answerMap: Record<string, Answer> = {}
    answerMap[answer.questionStableId] = answer
    render(<ItemDisplay
      question={question}
      answerMap={answerMap}
      surveyVersion={2}
      editHistory={[changeRecord]}
      supportedLanguages={[{ languageCode: 'es', languageName: 'Spanish', id: '' }]}
      showFullQuestions={true}/>)

    expect(screen.getByLabelText('View history')).toBeInTheDocument()

    const viewHistoryButton = screen.getByLabelText('View history')
    viewHistoryButton.click()
    expect(screen.getByText('test456')).toBeInTheDocument()
  })

  it('renders the full edit history for a question', async () => {
    const question = mockQuestionMetadata

    const answer: Answer = {
      stringValue: 'test123',
      questionStableId: 'testQ',
      surveyVersion: 1,
      viewedLanguage: 'es',
      lastUpdatedAt: 1577854800,
      createdAt: 1577854800
    } as Answer

    const firstChangeRecord: DataChangeRecord = {
      id: 'test_id',
      createdAt: 1577858400,
      modelName: 'test_survey',
      fieldName: 'testQ',
      oldValue: 'test123',
      newValue: 'test456',
      responsibleUserId: 'test_user'
    }

    const secondChangeRecord: DataChangeRecord = {
      id: 'test_id',
      createdAt: 1577862000,
      modelName: 'test_survey',
      fieldName: 'testQ',
      oldValue: 'test456',
      newValue: 'test789',
      responsibleUserId: 'test_user'
    }

    const answerMap: Record<string, Answer> = {}
    answerMap[answer.questionStableId] = answer
    render(<AnswerEditHistory supportedLanguages={[]} question={question}
      answer={answer} editHistory={[firstChangeRecord, secondChangeRecord]}/>)

    expect(screen.getByText('Answered on', { exact: false })).toBeInTheDocument()
    expect(screen.getAllByText('Edited on', { exact: false })).toHaveLength(2)
  })
})

describe('toQuestionMetadata', () => {
  it('converts basic question', async () => {
    const question = {
      name: 'testQ',
      title: 'test question',
      getType: () => 'text',
      isVisible: true
    } as Question

    expect(toQuestionMetadata(question)).toEqual([
      {
        stableId: 'testQ',
        title: 'test question',
        type: 'text',
        visible: true,
        derived: false
      }
    ])
  })

  it('converts calculated values', async () => {
    const question = {
      name: 'testQ',
      getType: () => 'calculatedvalue'
    } as unknown as CalculatedValue

    expect(toQuestionMetadata(question)).toEqual([
      {
        stableId: 'testQ',
        title: 'testQ',
        type: 'calculatedvalue',
        visible: true,
        derived: true
      }
    ])
  })

  it('adds choices to question', async () => {
    const question = {
      name: 'testChoiceQ',
      title: 'test question w choices',
      getType: () => 'checkbox',
      isVisible: true,
      choices: [
        { value: 'option1', text: 'Option 1' },
        { value: 'option2', text: 'Option 2' }
      ]
    } as unknown as Question

    expect(toQuestionMetadata(question)).toEqual([
      {
        stableId: 'testChoiceQ',
        title: 'test question w choices',
        type: 'checkbox',
        visible: true,
        derived: false,
        choices: [
          { value: 'option1', text: 'Option 1' },
          { value: 'option2', text: 'Option 2' }
        ]
      }
    ])
  })

  it('adds other questions', async () => {
    const question = {
      name: 'testChoiceQ',
      title: 'test question w choices',
      getType: () => 'checkbox',
      isVisible: true,
      choices: [
        { value: 'option1', text: 'Option 1', jsonObj: { otherStableId: 'otherQ1', otherText: 'Other question 1' } },
        { value: 'option2', text: 'Option 2', jsonObj: { otherStableId: 'otherQ2', otherText: 'Other question 2' } }
      ]
    } as unknown as Question

    expect(toQuestionMetadata(question)).toEqual([
      {
        stableId: 'testChoiceQ',
        title: 'test question w choices',
        type: 'checkbox',
        visible: true,
        derived: false,
        choices: [
          { value: 'option1', text: 'Option 1' },
          { value: 'option2', text: 'Option 2' }
        ]
      },
      {
        stableId: 'otherQ1',
        title: 'Other question 1',
        type: 'text',
        visible: true,
        derived: false
      },
      {
        stableId: 'otherQ2',
        title: 'Other question 2',
        type: 'text',
        visible: true,
        derived: false
      }
    ])
  })
})
