import { asMockedFn, MockI18nProvider, setupRouterTest } from '@juniper/ui-core'
import { usePortalEnv } from 'providers/PortalProvider'
import { mockUsePortalEnv } from 'test-utils/test-portal-factory'
import { act, render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import DocumentLibrary from './DocumentLibrary'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { mockUseActiveUser } from 'test-utils/user-mocking-utils'
import Api from 'api/api'

jest.mock('providers/PortalProvider', () => ({ usePortalEnv: jest.fn() }))

jest.mock('providers/ActiveUserProvider', () => ({
  useActiveUser: jest.fn()
}))

jest.mock('api/api', () => ({
  listParticipantFiles: jest.fn()
}))

beforeEach(() => {
  asMockedFn(usePortalEnv).mockReturnValue(mockUsePortalEnv())
  asMockedFn(useActiveUser).mockReturnValue(mockUseActiveUser())
})

describe('DocumentLibrary', () => {
  it('renders no documents message', () => {
    const { RoutedComponent } = setupRouterTest(<MockI18nProvider><DocumentLibrary/></MockI18nProvider>)
    render(RoutedComponent)
    expect(screen.getByText('{documentsPageTitle}')).toBeInTheDocument()
    expect(screen.getByText('{documentsListNone}')).toBeInTheDocument()
  })

  it('renders documents', async () => {
    asMockedFn(Api.listParticipantFiles).mockResolvedValue([
      { id: 'file1', fileName: 'file1.pdf', fileType: 'application/pdf', createdAt: 0, lastUpdatedAt: 0 },
      { id: 'file2', fileName: 'file2.png', fileType: 'image/png', createdAt: 0, lastUpdatedAt: 0 }
    ])

    const { RoutedComponent } = setupRouterTest(<MockI18nProvider><DocumentLibrary/></MockI18nProvider>)
    render(RoutedComponent)
    expect(screen.getByText('{documentsPageTitle}')).toBeInTheDocument()
    await waitFor(() => {
      expect(screen.getByText('file1.pdf')).toBeInTheDocument()
      expect(screen.getByText('file2.png')).toBeInTheDocument()
    })
  })

  it('renders document options dropdown', async () => {
    asMockedFn(Api.listParticipantFiles).mockResolvedValue([
      { id: 'file1', fileName: 'file1.pdf', fileType: 'application/pdf', createdAt: 0, lastUpdatedAt: 0 }
    ])

    const { RoutedComponent } = setupRouterTest(<MockI18nProvider><DocumentLibrary/></MockI18nProvider>)
    render(RoutedComponent)
    await waitFor(() => {
      expect(screen.getByText('file1.pdf')).toBeInTheDocument()
    })

    await act(async () => {
      screen.getByText('{documentOptionsButton}').click()
    })

    expect(screen.getByText('{documentDeletionDelete}')).toBeInTheDocument()
    expect(screen.getByText('{documentDownloadButton}')).toBeInTheDocument()
  })
})
