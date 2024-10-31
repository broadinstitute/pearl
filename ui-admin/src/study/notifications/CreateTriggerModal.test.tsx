import {
  render,
  screen
} from '@testing-library/react'
import React from 'react'
import CreateTriggerModal from './CreateTriggerModal'
import { select } from 'react-select-event'
import { setupRouterTest } from '@juniper/ui-core'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'

describe('CreateTriggerModal', () => {
  test('renders type and event options', async () => {
    const { RoutedComponent } = setupRouterTest(<CreateTriggerModal
      studyEnvContext={mockStudyEnvContext()}
      onDismiss={jest.fn()} onCreate={jest.fn()}/>)
    render(RoutedComponent)
    // confirm options are branched based on the config type
    await select(screen.getByLabelText('Trigger action on'), 'Event')
    expect(screen.queryByLabelText('Task type')).not.toBeInTheDocument()
    expect(screen.getByLabelText('Event type')).toBeInTheDocument()

    await select(screen.getByLabelText('Trigger action on'), 'Task Reminder')
    expect(screen.getByLabelText('Task type')).toBeInTheDocument()
    expect(screen.queryByLabelText('Event type')).not.toBeInTheDocument()
  })
})
