import React from 'react'
import { screen } from '@testing-library/react'
import PortalDashboard from './PortalDashboard'
import { Portal } from 'api/api'
import { makeMockPortalStudy, mockPortal } from 'test-utils/mocking-utils'
import { renderWithRouter } from '@juniper/ui-core'

const mockMultiStudyPortal: Portal = {
  ...mockPortal(),
  portalStudies: [
    makeMockPortalStudy('Study 1', 'study1'),
    makeMockPortalStudy('Study 2', 'study2')
  ]
}

describe('PortalDashboard', () => {
  it('renders all widgets', () => {
    renderWithRouter(<PortalDashboard portal={mockMultiStudyPortal} />)

    expect(screen.getByText('Studies')).toBeInTheDocument()
    expect(screen.getByText('Website')).toBeInTheDocument()
    expect(screen.getByText('Help & Tutorials')).toBeInTheDocument()
    expect(screen.getByText('Mailing List')).toBeInTheDocument()
    expect(screen.getByText('Team Members')).toBeInTheDocument()
    expect(screen.getAllByText('Study 1')).toHaveLength(2)
    expect(screen.getAllByText('Study 2')).toHaveLength(2)
  })
})
