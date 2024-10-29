
import { screen } from '@testing-library/react'
import React from 'react'
import {
  mockPortal,
  mockPortalEnvironment,
  mockPortalEnvironmentConfig,
  renderInPortalRouter
} from '../../test-utils/mocking-utils'
import PortalPublishingView from './PortalPublishingView'
import { Portal } from '@juniper/ui-core'

describe('PortalPublishingView', () => {
  test('enables publishing across environments', async () => {
    const portal: Portal = {
      ...mockPortal(),
      portalEnvironments: [
        { ...mockPortalEnvironment('sandbox'), portalEnvironmentConfig: mockPortalEnvironmentConfig() },
        { ...mockPortalEnvironment('irb'), portalEnvironmentConfig: mockPortalEnvironmentConfig() },
        { ...mockPortalEnvironment('live'), portalEnvironmentConfig: mockPortalEnvironmentConfig() }
      ]
    }

    renderInPortalRouter(portal, <PortalPublishingView portal={portal} studyShortcode="something"/>)
    expect(screen.getByText('Publish sandbox to irb')).toBeInTheDocument()
    expect(screen.getByText('Publish irb to live')).toBeInTheDocument()
  })
})
