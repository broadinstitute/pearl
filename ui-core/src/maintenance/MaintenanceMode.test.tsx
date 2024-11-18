import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import { MaintenanceMode } from './MaintenanceMode'
import { userEvent } from '@testing-library/user-event'
import { ApiProvider, emptyApi } from 'src/participant/ApiProvider'

const mockLoadSystemSettings = jest.fn()

const mockApi = {
  ...emptyApi,
  loadSystemSettings: mockLoadSystemSettings
}

describe('MaintenanceMode', () => {
  it('should display maintenance mode message when maintenanceModeEnabled is true', async () => {
    mockLoadSystemSettings.mockResolvedValue({
      maintenanceModeEnabled: true,
      maintenanceModeMessage: 'Maintenance in progress',
      maintenanceModeBypassPhrase: 'bypass'
    })

    render(
      <ApiProvider api={mockApi}>
        <MaintenanceMode>
          <div>Welcome to the Juniper Heart study</div>
        </MaintenanceMode>
      </ApiProvider>
    )

    await waitFor(() => {
      expect(screen.getByText('This application is currently unavailable')).toBeInTheDocument()
      expect(screen.getByText('Maintenance in progress')).toBeInTheDocument()
      expect(screen.queryByText('Welcome to the Juniper Heart study')).not.toBeInTheDocument()
    })
  })

  it('should display child components when maintenanceModeEnabled is false', async () => {
    mockLoadSystemSettings.mockResolvedValue({
      maintenanceModeEnabled: false,
      maintenanceModeMessage: '',
      maintenanceModeBypassPhrase: ''
    })

    render(
      <ApiProvider api={mockApi}>
        <MaintenanceMode>
          <div>Welcome to the Juniper Heart study</div>
        </MaintenanceMode>
      </ApiProvider>
    )

    await waitFor(() => {
      expect(screen.getByText('Welcome to the Juniper Heart study')).toBeInTheDocument()
      expect(screen.queryByText('This application is currently unavailable')).not.toBeInTheDocument()
    })
  })

  it('should bypass maintenance mode with correct pass phrase', async () => {
    mockLoadSystemSettings.mockResolvedValue({
      maintenanceModeEnabled: true,
      maintenanceModeMessage: 'Maintenance in progress',
      maintenanceModeBypassPhrase: 'bypass'
    })

    render(
      <ApiProvider api={mockApi}>
        <MaintenanceMode>
          <div>Welcome to the Juniper Heart study</div>
        </MaintenanceMode>
      </ApiProvider>
    )

    await waitFor(() => {
      expect(screen.getByText('This application is currently unavailable')).toBeInTheDocument()
      expect(screen.queryByText('Welcome to the Juniper Heart study')).not.toBeInTheDocument()
    })

    await userEvent.type(screen.getByRole('textbox'), 'bypass')
    await userEvent.click(screen.getByText('Enter'))

    await waitFor(() => {
      expect(screen.getByText('Welcome to the Juniper Heart study')).toBeInTheDocument()
      expect(screen.queryByText('This application is currently unavailable')).not.toBeInTheDocument()
    })
  })
})
