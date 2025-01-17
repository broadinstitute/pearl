import React from 'react'
import { render, screen } from '@testing-library/react'
import { RawEnrolleeSurveyView } from './SurveyResponseView'
import {
  mockAnswer,
  mockConfiguredSurvey,
  mockEnrollee, mockParticipantTask,
  mockStudyEnvContext,
  mockSurveyResponse
} from 'test-utils/mocking-utils'
import { setupRouterTest } from '@juniper/ui-core'

describe('RawEnrolleeSurveyView', () => {
  it('renders the survey version from the answers', async () => {
    const response = {
      ...mockSurveyResponse(),
      answers: [
        { ...mockAnswer(), surveyVersion: 2 }
      ]
    }
    const { RoutedComponent } = setupRouterTest(
      <RawEnrolleeSurveyView enrollee={mockEnrollee()}
        task={mockParticipantTask('SURVEY', 'NEW')}
        studyEnvContext={mockStudyEnvContext()}
        configSurvey={mockConfiguredSurvey()}
        updateResponseMap={jest.fn()}
        onUpdate={jest.fn()}
        response={response}/>)
    render(RoutedComponent)
    expect(screen.getByText('(version 2)', { exact: false })).toBeInTheDocument()
  })

  it('renders the multiple versions from the answers', async () => {
    const response = {
      ...mockSurveyResponse(),
      answers: [
        { ...mockAnswer(), surveyVersion: 2 },
        { ...mockAnswer(), surveyVersion: 3 }
      ]
    }
    const { RoutedComponent } = setupRouterTest(
      <RawEnrolleeSurveyView enrollee={mockEnrollee()}
        task={mockParticipantTask('SURVEY', 'NEW')}
        studyEnvContext={mockStudyEnvContext()}
        updateResponseMap={jest.fn()}
        configSurvey={mockConfiguredSurvey()}
        onUpdate={jest.fn()}
        response={response}/>)
    render(RoutedComponent)
    expect(screen.getByText('(versions 2, 3)', { exact: false })).toBeInTheDocument()
  })
})
