import Api from '../../api/api'


import React from 'react'
import { screen, waitFor } from '@testing-library/react'
import {
  mockEmailTemplate,
  mockPortal, mockPortalContext,
  mockStudyEnvContext, mockSurvey, mockTrigger, renderInPortalRouter
} from 'test-utils/mocking-utils'
import WorkflowView from './WorkflowView'
import { mockConfiguredSurvey } from '@juniper/ui-core'

describe('WorkflowView', () => {
  test('renders empty portal', async () => {
    Element.prototype.scrollIntoView = jest.fn()
    jest.spyOn(Api, 'findTriggersForStudyEnv').mockResolvedValue([])
    jest.spyOn(Api, 'findConfiguredSurveys').mockResolvedValue([])

    renderInPortalRouter(mockPortal(),
      <WorkflowView
        studyEnvContext={mockStudyEnvContext()}
        portalContext={mockPortalContext()}
      />)
    await waitFor(expect(screen.getByText('Pre-enroll')).toBeInTheDocument)
    expect(screen.getByText('Pre-enroll')).toBeInTheDocument()
  })

  test('renders enrollment trigger', async () => {
    Element.prototype.scrollIntoView = jest.fn()
    jest.spyOn(Api, 'findTriggersForStudyEnv').mockResolvedValue([
      {
        ...mockTrigger(), eventType: 'STUDY_ENROLLMENT', emailTemplate: {
          ...mockEmailTemplate(),
          name: 'Enrollment welcome email'
        }
      }
    ])
    jest.spyOn(Api, 'findConfiguredSurveys').mockResolvedValue([])

    renderInPortalRouter(mockPortal(),
      <WorkflowView
        studyEnvContext={mockStudyEnvContext()}
        portalContext={mockPortalContext()}
      />)
    await waitFor(expect(screen.getByText('Pre-enroll')).toBeInTheDocument)
    expect(screen.getByText('Enrollment welcome email')).toBeInTheDocument()
  })

  test('renders form-specific trigger', async () => {
    Element.prototype.scrollIntoView = jest.fn()
    jest.spyOn(Api, 'findTriggersForStudyEnv').mockResolvedValue([
      {
        ...mockTrigger(), eventType: 'SURVEY_RESPONSE',
        filterTargetStableIds: ['surveyAbc'],
        emailTemplate: {
          ...mockEmailTemplate(),
          name: 'specific email'
        }
      }
    ])
    jest.spyOn(Api, 'findConfiguredSurveys').mockResolvedValue([{
      ...mockConfiguredSurvey(),
      survey: { ...mockSurvey(), name: 'Survey ABC', stableId: 'surveyAbc' }
    }])

    renderInPortalRouter(mockPortal(),
      <WorkflowView
        studyEnvContext={mockStudyEnvContext()}
        portalContext={mockPortalContext()}
      />)
    await waitFor(expect(screen.getByText('Pre-enroll')).toBeInTheDocument)
    expect(screen.getByText('Survey ABC')).toBeInTheDocument()
    expect(screen.getByText('specific email')).toBeInTheDocument()
  })
})
