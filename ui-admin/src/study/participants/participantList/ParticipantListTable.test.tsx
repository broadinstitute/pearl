import { renderWithRouter } from '@juniper/ui-core'
import {
  mockEnrolleeSearchExpressionResult,
  mockStudyEnvContext
} from 'test-utils/mocking-utils'
import {
  act,
  screen,
  waitFor
} from '@testing-library/react'
import React from 'react'
import ParticipantListTable from 'study/participants/participantList/ParticipantListTable'
import { userEvent } from '@testing-library/user-event'


describe('Participant List Table', () => {
  test('shows expected columns', async () => {
    renderWithRouter(<ParticipantListTable
      studyEnvContext={mockStudyEnvContext()}
      participantList={[
        mockEnrolleeSearchExpressionResult()
      ]}
      reload={jest.fn()}
    />)


    await waitFor(async () => {
      expect(screen.queryByText('Shortcode')).toBeVisible()
    })
    const expectedCols = ['Created', 'Last login', 'Consented']
    expectedCols.forEach(colName => {
      expect(screen.getByText(colName)).toBeInTheDocument()
    })

    const hiddenCols = ['Given Name', 'Family Name', 'Username', 'Contact Email', 'Is subject']
    hiddenCols.forEach(colName => {
      expect(screen.queryByText(colName)).not.toBeInTheDocument()
    })
  })

  test('should be able to download participant list', async () => {
    renderWithRouter(<ParticipantListTable
      studyEnvContext={mockStudyEnvContext()}
      participantList={[
        mockEnrolleeSearchExpressionResult()
      ]}
      reload={jest.fn()}
    />)

    await act(async () => {
      await userEvent.click(await screen.findByLabelText('Download table'))
    })

    await waitFor(async () => {
      expect(await screen.findByLabelText('Confirm download')).toBeInTheDocument()
    })


    window.URL.createObjectURL = jest.fn()
    document.createElement = jest.fn().mockReturnValue({
      click: jest.fn()
    })

    await act(async () => {
      await userEvent.click(await screen.findByLabelText('Confirm download'))
    })

    expect(window.URL.createObjectURL).toHaveBeenCalled()
  })
})
