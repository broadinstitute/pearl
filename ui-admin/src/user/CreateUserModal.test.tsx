import React from 'react'
import { mockPortal, renderInPortalRouter } from 'test-utils/mocking-utils'
import { screen } from '@testing-library/react'
import { mockAdminUser } from '../test-utils/user-mocking-utils'
import Api from 'api/api'
import CreateUserModal from './CreateUserModal'
import { userEvent } from '@testing-library/user-event'
import { select } from 'react-select-event'

describe('CreateUserModal', () => {
  test('Portal select functions', async () => {
    jest.spyOn(Api, 'fetchRoles').mockResolvedValue([])
    const createSpy = jest.spyOn(Api, 'createPortalUser').mockResolvedValue(mockAdminUser(false))
    const portals = [{
      ...mockPortal(),
      shortcode: 'test1',
      name: 'P1'
    }, {
      ...mockPortal(),
      shortcode: 'test2',
      name: 'P2'
    }]

    renderInPortalRouter(portals[1],
      <CreateUserModal onDismiss={jest.fn()} portals={portals} userCreated={jest.fn()} />)
    await userEvent.type(screen.getByLabelText('Email'), 'foo@bar.com')
    await userEvent.click(screen.getByText('Create'))

    expect(createSpy).toHaveBeenCalledWith({
      username: 'foo@bar.com', superuser: false, portalShortcode: 'test1', roleNames: []
    })

    createSpy.mockClear()
    await select(screen.getByLabelText('Portal'), ['P2'])
    await userEvent.click(screen.getByText('Create'))
    expect(createSpy).toHaveBeenCalledWith({
      username: 'foo@bar.com', superuser: false, portalShortcode: 'test2', roleNames: []
    })
  })
})
