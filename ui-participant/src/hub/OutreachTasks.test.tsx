import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import { mockEnrollee, mockParticipantTask, mockSurvey } from 'test-utils/test-participant-factory'
import OutreachTasks from './OutreachTasks'
import { mockStudy, mockStudyEnv } from 'test-utils/test-portal-factory'
import Api, { TaskWithSurvey } from 'api/api'
import { MockI18nProvider, setupRouterTest } from '@juniper/ui-core'

jest.mock('providers/PortalProvider', () => ({ usePortalEnv: jest.fn() }))

describe('OutreachTasks', () => {
  it('show tasks with blurbs', async () => {
    const enrollee = mockEnrollee()
    const study = {
      ...mockStudy(),
      studyEnvironments: [{ ...mockStudyEnv(), id: 'studyEnv1' }]
    }
    const tasksWithSurvey: TaskWithSurvey[] = [
      {
        task: {
          ...mockParticipantTask('OUTREACH', 'NEW'),
          targetStableId: 'outreach1',
          studyEnvironmentId: 'studyEnv1'
        },
        survey: {
          ...mockSurvey('outreach1'),
          surveyType: 'OUTREACH',
          blurb: 'Survey 1 blurb'
        }
      },
      {
        task: {
          ...mockParticipantTask('OUTREACH', 'NEW'),
          targetStableId: 'outreach2',
          studyEnvironmentId: 'studyEnv1'
        },
        survey: {
          ...mockSurvey('outreach2'),
          surveyType: 'OUTREACH',
          blurb: 'Survey 2 blurb'
        }
      }
    ]
    jest.spyOn(Api, 'listOutreachActivities').mockResolvedValue(tasksWithSurvey)
    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider>
        <OutreachTasks enrollees={[enrollee]} studies={[study]}/>
      </MockI18nProvider>
    )
    render(RoutedComponent)
    await waitFor(() => expect(screen.getByText('Survey 1 blurb')).toBeInTheDocument())
    await waitFor(() => expect(screen.getByText('Survey 2 blurb')).toBeInTheDocument())
  })

  it('excludes completed tasks', async () => {
    const enrollee = mockEnrollee()
    const study = {
      ...mockStudy(),
      studyEnvironments: [{ ...mockStudyEnv(), id: 'studyEnv1' }]
    }
    const tasksWithSurvey: TaskWithSurvey[] = [
      {
        task: {
          ...mockParticipantTask('OUTREACH', 'NEW'),
          targetStableId: 'outreach1',
          studyEnvironmentId: 'studyEnv1',
          status: 'COMPLETE'
        },
        survey: {
          ...mockSurvey('outreach1'),
          surveyType: 'OUTREACH',
          blurb: 'Survey 1 blurb'
        }
      },
      {
        task: {
          ...mockParticipantTask('OUTREACH', 'NEW'),
          targetStableId: 'outreach2',
          studyEnvironmentId: 'studyEnv1'
        },
        survey: {
          ...mockSurvey('outreach2'),
          surveyType: 'OUTREACH',
          blurb: 'Survey 2 blurb'
        }
      }
    ]
    jest.spyOn(Api, 'listOutreachActivities').mockResolvedValue(tasksWithSurvey)
    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider>
        <OutreachTasks enrollees={[enrollee]} studies={[study]}/>
      </MockI18nProvider>
    )
    render(RoutedComponent)
    await waitFor(() => expect(screen.getByText('Survey 2 blurb')).toBeInTheDocument())
    expect(screen.queryByText('Survey 1 blurb')).toBeNull()
  })
})
