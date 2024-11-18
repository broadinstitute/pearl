import React, { useEffect, useState } from 'react'
import { Button } from 'components/forms/Button'
import { Checkbox } from 'components/forms/Checkbox'
import { Textarea } from 'components/forms/Textarea'
import { TextInput } from '../components/forms/TextInput'
import { renderPageHeader } from '../util/pageUtils'
import { SystemSettings } from '@juniper/ui-core'
import Api from 'api/api'
import { doApiLoad } from '../api/api-utils'
import { Store } from 'react-notifications-component'
import { successNotification } from '../util/notifications'
import LoadingSpinner from '../util/LoadingSpinner'

export default function SystemSettingsEditor() {
  const [settings, setSettings] = useState<SystemSettings>()
  const [isSaving, setIsSaving] = useState(false)

  const loadSettings = async () => {
    const response = await Api.loadMaintenanceModeSettings()
    setSettings(response)
  }

  useEffect(() => {
    loadSettings()
  }, [])

  const doSave = async () => {
    if (!settings) {
      return
    }
    doApiLoad(async () => {
      await Api.updateSystemSettings(settings)
      Store.addNotification(successNotification('System settings updated'))
    }, { setIsLoading: setIsSaving })
  }

  return <div className={'mx-3'}><form onSubmit={e => {
    e.preventDefault()
  }}>
    {renderPageHeader('System Settings')}
    <LoadingSpinner isLoading={isSaving}>
      { !settings ?
        <div className="alert alert-warning">
          <strong>Warning:</strong> Unable to load maintenance mode settings.
        </div> :
        <>
          <div className={'bg-light p-3 mb-3 rounded-3 border border-1'}>
            <h5 className={'mb-3'}>Maintenance Mode Settings</h5>
            <Checkbox
              label={'Enable Maintenance Mode'}
              description={'When enabled, users will see the maintenance message. ' +
                      'Study staff and participants will not be allowed' +
                      ' to log in or use the system.'}
              checked={settings?.maintenanceModeEnabled || false}
              onChange={e => setSettings({ ...settings, maintenanceModeEnabled: e })}/>
            <Textarea
              rows={4}
              disabled={!settings?.maintenanceModeEnabled}
              label="Maintenance Message"
              description={'Message to display to study staff and participants' +
                ' when maintenance mode is enabled. Markdown is supported.'}
              value={settings?.maintenanceModeMessage} onChange={e =>
                setSettings({ ...settings, maintenanceModeMessage: e })
              }
            />
            <TextInput
              label={'Maintenance Bypass Phrase'}
              disabled={!settings?.maintenanceModeEnabled}
              description={'Password required to bypass maintenance mode. This is not encrypted in any way and ' +
                      'is only intended to discourage access to the application.'}
              value={settings?.maintenanceModeBypassPhrase || ''}
              onChange={e => setSettings({ ...settings, maintenanceModeBypassPhrase: e })}
            />
          </div>
          <div className={'bg-light p-3 my-3 rounded-3 border border-1'}>
            <h5 className={'mb-3'}>General Settings</h5>
            <Checkbox
              className={'mb-3'}
              label={'Disable Scheduled Jobs'}
              description={'If checked, scheduled jobs will not run. ' +
              'This can be toggled independently of maintenance mode.'}
              checked={settings?.disableScheduledJobs || false}
              onChange={e => setSettings({ ...settings, disableScheduledJobs: e })}
            />
          </div>
          <Button variant="primary" type="button" onClick={doSave}>
            Save
          </Button>
        </>}
    </LoadingSpinner>
  </form></div>
}
