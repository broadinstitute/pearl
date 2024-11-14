import React, { useEffect, useState } from 'react'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faScrewdriverWrench } from '@fortawesome/free-solid-svg-icons'
import { MaintenanceModeSettings } from 'src/types/maintenance'
import { useApiContext } from 'src/participant/ApiProvider'
import { Markdown } from 'src/participant/landing/Markdown'

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
        <div className={'text-center w-75'}>
          <h1><FontAwesomeIcon icon={faScrewdriverWrench}/> This application is currently unavailable</h1>
          <Markdown className={'py-2'}>{settings?.message}</Markdown>
          <div className={'mt-5 mb-2'}>If you are a system administrator, you may login below:</div>
          <div className={'d-flex justify-content-center'}>
            <input
              className={'form-control'}
              style={{ width: '200px' }}
              value={password}
              onChange={e => { setPassword(e.target.value) }
              }/>
            <button
              className={'btn btn-primary mx-2 border border-1'}
              onClick={() => {
                if (password === settings?.bypassPhrase) {
                  setBypassMaintenanceMode(true)
                }
              }}
            >
              Enter
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
