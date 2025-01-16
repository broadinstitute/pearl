import React from 'react'
import {
  render,
  screen,
  waitFor
} from '@testing-library/react'

import {
  mockEmailTemplate,
  mockPortalContext,
  mockStudyEnvContext,
  mockTrigger
} from 'test-utils/mocking-utils'
import TriggerList from './TriggerList'
import { userEvent } from '@testing-library/user-event'
import Api, { Trigger } from 'api/api'
import { ReactNotifications } from 'react-notifications-component'
import { setupRouterTest } from '@juniper/ui-core'

test('renders routable trigger list', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const enrollEmailConfig: Trigger = {
    ...mockTrigger(),
    id: 'event1',
    triggerType: 'EVENT',
    eventType: 'STUDY_ENROLLMENT',
    emailTemplate: {
      ...mockEmailTemplate(),
      name: 'Enrollment welcome email'
    }
  }
  const triggers: Trigger[] = [
    enrollEmailConfig,
    {
      ...mockTrigger(),
      id: 'reminder1',
      triggerType: 'TASK_REMINDER',
      taskType: 'CONSENT',
      emailTemplate: {
        ...mockEmailTemplate(),
        name: 'Consent reminder'
      }
    },
    {
      ...mockTrigger(),
      id: 'reminder2',
      triggerType: 'TASK_REMINDER',
      taskType: 'SURVEY',
      emailTemplate: {
        ...mockEmailTemplate(),
        name: 'Survey reminder'
      }
    }
  ]
  jest.spyOn(Api, 'findTriggersForStudyEnv')
    .mockImplementation(() => Promise.resolve(triggers))
  jest.spyOn(Api, 'findTrigger')
    .mockImplementation(jest.fn())

  const { RoutedComponent, router } =
      setupRouterTest(<>
        <ReactNotifications/>
        <TriggerList studyEnvContext={studyEnvContext} portalContext={mockPortalContext()}/>
      </>)
  render(RoutedComponent)
  expect(screen.getByText('Study Automation')).toBeInTheDocument()
  await waitFor(() => expect(screen.getByText('Enrollment welcome email')).toBeInTheDocument())
  expect(screen.getByText('Consent reminder')).toBeInTheDocument()
  expect(screen.getByText('Survey reminder')).toBeInTheDocument()

  await userEvent.click(screen.getByText('Enrollment welcome email'))
  expect(router.state.location.pathname).toEqual(`/${enrollEmailConfig.id}`)
})

test('allows deletion of trigger', async () => {
  const studyEnvContext = mockStudyEnvContext()
  const consentConfig: Trigger = {
    ...mockTrigger(),
    id: 'reminder1',
    triggerType: 'TASK_REMINDER',
    taskType: 'CONSENT',
    emailTemplate: {
      ...mockEmailTemplate(),
      name: 'Consent reminder'
    }
  }
  const notificationConfigs: Trigger[] = [
    {
      ...mockTrigger(),
      id: 'event1',
      triggerType: 'EVENT',
      eventType: 'STUDY_ENROLLMENT',
      emailTemplate: {
        ...mockEmailTemplate(),
        name: 'Enrollment welcome email'
      }
    },
    consentConfig,
    {
      ...mockTrigger(),
      id: 'reminder2',
      triggerType: 'TASK_REMINDER',
      taskType: 'SURVEY'
    }
  ]
  jest.spyOn(Api, 'findTriggersForStudyEnv')
    .mockImplementation(() => Promise.resolve(notificationConfigs))
  jest.spyOn(Api, 'findTrigger')
    .mockImplementation(() => Promise.resolve(consentConfig))
  jest.spyOn(Api, 'deleteTrigger').mockImplementation(() => Promise.resolve(new Response()))

  const { RoutedComponent } =
    setupRouterTest(<>
      <ReactNotifications/>
      <TriggerList studyEnvContext={studyEnvContext} portalContext={mockPortalContext()}/>
    </>)
  render(RoutedComponent)

  await waitFor(() => expect(screen.getByText('Enrollment welcome email')).toBeInTheDocument())

  expect(screen.getByText('Consent reminder')).toBeInTheDocument()
  await userEvent.click(screen.getByText('Consent reminder'))

  await waitFor(() => expect(screen.getByText('Delete')).toBeInTheDocument())
  await userEvent.click(screen.getByText('Delete'))
  await waitFor(() => expect(screen.getByText('Delete automatic action')).toBeInTheDocument())

  // modal has popped up now, so delete
  const deleteButtons = screen.getAllByText('Delete')

  // find the button which is inside a modal
  const modalButton = deleteButtons.find(btn => btn.closest('.modal-footer'))

  expect(modalButton).not.toBeUndefined()

  if (modalButton) {
    await userEvent.click(modalButton)
  }

  await waitFor(
    () =>
      expect(Api.deleteTrigger)
        .toHaveBeenCalledWith(
          studyEnvContext.portal.shortcode,
          studyEnvContext.study.shortcode,
          studyEnvContext.currentEnv.environmentName,
          consentConfig.id
        ))
})
