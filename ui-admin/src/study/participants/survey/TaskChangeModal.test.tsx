import {
  mockParticipantTask,
  mockStudyEnvParams
} from 'test-utils/mocking-utils'
import { render, screen } from '@testing-library/react'
import React from 'react'
import TaskChangeModal from './TaskChangeModal'
import { userEvent } from '@testing-library/user-event'

describe('TaskChangeModal', () => {
  test('disables submit if no justification', async () => {
    render(<TaskChangeModal studyEnvParams={mockStudyEnvParams()}
      task={mockParticipantTask('SURVEY', 'NEW')}
      onSubmit={jest.fn()}
      onDismiss={jest.fn()}/>)
    expect(screen.getByText('Justification')).toBeVisible()
    expect(screen.getByText('Update')).toHaveAttribute('aria-disabled', 'true')
    await userEvent.type(screen.getByLabelText('Justification*'), 'reason')
    expect(screen.getByText('Update')).toHaveAttribute('aria-disabled', 'false')
  })
})
