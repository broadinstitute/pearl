import React from 'react'
import { screen } from '@testing-library/react'
import { MailingListWidget } from './MailingListWidget'
import Api from 'api/api'
import { act } from 'react-dom/test-utils'
import { mockPortal } from 'test-utils/mocking-utils'
import { asMockedFn, renderWithRouter } from '@juniper/ui-core'

jest.mock('api/api')

describe('MailingListWidget', () => {
  it('renders empty mailing list message', async () => {
    asMockedFn(Api.fetchMailingList).mockResolvedValue([])

    await act(async () => {
      renderWithRouter(<MailingListWidget portal={mockPortal()} />)
    })

    expect(screen.getByText('Your mailing list does not have any subscribers')).toBeInTheDocument()
  })

  it('renders mailing list with 2 members, 1 in the past week', async () => {
    const now = Date.now() / 1000
    const twoWeeksAgo = now - (7 * 24 * 60 * 60 * 2)
    const contacts = [
      { createdAt: now, name: 'Foo Bar', email: 'test@example.com' },
      { createdAt: twoWeeksAgo - 1, name: 'Baz Bar', email: 'test2@example.com' }
    ]

    asMockedFn(Api.fetchMailingList).mockResolvedValue(contacts)

    await act(async () => {
      renderWithRouter(<MailingListWidget portal={mockPortal()} />)
    })

    expect(
      screen.getByTestId('mailing-list-content').textContent)
      .toBe('Your mailing list has 2 total subscribers and 1new subscribers in the last week.' +
          'Keep potential participants engaged with your study by sending them updates and newsletters.')
  })
})
