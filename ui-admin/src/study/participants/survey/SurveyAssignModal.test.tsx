import {
  mockEnrollee,
  mockStudyEnvParams, mockSurvey
} from 'test-utils/mocking-utils'
import { screen } from '@testing-library/react'
import React from 'react'
import { userEvent } from '@testing-library/user-event'
import SurveyAssignModal from './SurveyAssignModal'
import { renderWithRouter } from '@juniper/ui-core'

describe('SurveyAssignModal', () => {
  test('disables submit if no justification', async () => {
    renderWithRouter(<SurveyAssignModal studyEnvParams={mockStudyEnvParams()}
      enrollee={mockEnrollee()}
      survey={mockSurvey()}
      onSubmit={jest.fn()}
      onDismiss={jest.fn()}/>)
    expect(screen.getByText('Justification')).toBeVisible()
    expect(screen.getByText('Update')).toHaveAttribute('aria-disabled', 'true')
    await userEvent.type(screen.getByLabelText('Justification*'), 'reason')
    expect(screen.getByText('Update')).toHaveAttribute('aria-disabled', 'false')
  })
})
