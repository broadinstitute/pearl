import React, { useEffect, useState } from 'react'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faScrewdriverWrench } from '@fortawesome/free-solid-svg-icons'
import { SystemSettings } from 'src/types/maintenance'
import { Markdown } from '../participant/landing/Markdown'
import { useApiContext } from '../participant/ApiProvider'

export function MaintenanceMode({ children }: { children: React.ReactNode }) {
  const [settings, setSettings] = useState<SystemSettings>()
  const [bypassMaintenanceMode, setBypassMaintenanceMode] = useState(false)
  const [password, setPassword] = useState('')
  const Api = useApiContext()

  useEffect(() => {
    const loadMaintenanceSettings = async () => {
      const response = await Api.loadSystemSettings()
      setSettings(response)
    }

    loadMaintenanceSettings()

    // Poll maintenance status settings every 5 minutes
    // This means that users can actively use the website for up to 5 minutes after maintenance mode is enabled
    // After that, they'll see the maintenance mode message page pop up
    const intervalId = setInterval(loadMaintenanceSettings, 5 * 60 * 1000)

    return () => clearInterval(intervalId)
  }, [])

  if (!settings?.maintenanceModeEnabled || bypassMaintenanceMode) {
    return <>
      { children }
    </>
  }

  return (
    <div>
      <div className={'d-flex justify-content-center align-items-center vh-100'}>
        <div className={'text-center w-75'}>
          <h1><FontAwesomeIcon icon={faScrewdriverWrench}/> This application is currently unavailable</h1>
          <Markdown className={'py-2'}>{settings?.maintenanceModeMessage}</Markdown>
          { settings.maintenanceModeBypassPhrase  && <>
            <div className={'mt-5 mb-2'}>If you are a system administrator, you may login below:</div>

            <div className={'d-flex justify-content-center'}>
              <input
                className={'form-control'}
                style={{ width: '200px' }}
                value={password}
                onChange={e => {
                  setPassword(e.target.value)
                }
                }/>
              <button
                className={'btn btn-primary mx-2 border border-1'}
                onClick={() => {
                  if (password === settings?.maintenanceModeBypassPhrase) {
                    setBypassMaintenanceMode(true)
                  }
                }}
              >
              Enter
              </button>
            </div>
          </>
          }
        </div>
      </div>
    </div>
  )
}
