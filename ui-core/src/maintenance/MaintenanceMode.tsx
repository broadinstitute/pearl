import React, { useEffect, useState } from 'react'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faScrewdriverWrench } from '@fortawesome/free-solid-svg-icons'
import { Markdown, useApiContext } from '@juniper/ui-core'
import { MaintenanceModeSettings } from 'src/types/maintenance'

export function MaintenanceMode({ children }: { children: React.ReactNode }) {
  const [settings, setSettings] = useState<MaintenanceModeSettings>()
  const [bypassMaintenanceMode, setBypassMaintenanceMode] = useState(false)
  const [password, setPassword] = useState('')
  const Api = useApiContext()

  useEffect(() => {
    const loadSettings = async () => {
      const response = await Api.loadMaintenanceModeSettings()
      setSettings(response)
    }
    loadSettings()
  }, [])

  if (!settings?.enabled || bypassMaintenanceMode) {
    return <>
      { children }
    </>
  }

  return (
    <div>
      <div className={'d-flex justify-content-center align-items-center vh-100'}>
        <div className={'text-center'}>
          <h1><FontAwesomeIcon icon={faScrewdriverWrench}/> This application is currently unavailable</h1>
          <Markdown className={'py-2'}>{settings?.message}</Markdown>
          <p>If you are a system administrator, you may login below:</p>
          <div className={'d-flex justify-content-center'}>
            <input
              className={'form-control'}
              style={{ width: '200px' }}
              value={password}
              onChange={e => { setPassword(e.target.value) }
              }/>
            <button
              className={'btn btn-primary mx-2'}
              onClick={() => {
                if (password === settings?.maintenancePassword) {
                  setBypassMaintenanceMode(true)
                }
              }}
            >
              Enter
            </button>
          </div>
        </div>
      </div>
      )
    </div>
  )
}
