import React from 'react'
import { usePortalEnv } from 'providers/PortalProvider'
import { render, screen, waitFor } from '@testing-library/react'
import LandingPage from './LandingPage'
import {
  mockLocalSiteContent, mockUsePortalEnv
} from 'test-utils/test-portal-factory'
import { asMockedFn, expectNever, MockI18nProvider, setupRouterTest } from '@juniper/ui-core'

jest.mock('providers/PortalProvider', () => {
  return {
    ...jest.requireActual('providers/PortalProvider'),
    usePortalEnv: jest.fn()
  }
})

describe('LandingPage', () => {
  beforeEach(() => {
    asMockedFn(usePortalEnv).mockReturnValue(mockUsePortalEnv())
  })

  it('handles trivial landing page', () => {
    const { RoutedComponent } =
            setupRouterTest(
              <MockI18nProvider>
                <LandingPage localContent={mockLocalSiteContent()}/>
              </MockI18nProvider>
            )
    render(RoutedComponent)
    // mailing list modal is hidden by default
    expectNever(() =>
      expect(screen.getByLabelText('Join mailing list')).toHaveAttribute('aria-hidden', 'false')
    )
  })

  it('shows mailing list modal if url param is present', () => {
    const { RoutedComponent } =
            setupRouterTest(
              <MockI18nProvider>
                <LandingPage localContent={mockLocalSiteContent()}/>
              </MockI18nProvider>, ['?showJoinMailingList=true'])
    render(RoutedComponent)
    // mailing list modal is hidden by default
    waitFor(() => expect(screen.getByLabelText('Join mailing list'))
      .toHaveAttribute('aria-hidden', 'false'))
  })
})
