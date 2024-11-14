import React, { useEffect, useState } from 'react'
import { Button } from 'components/forms/Button'
import { Checkbox } from 'components/forms/Checkbox'
import { Textarea } from 'components/forms/Textarea'
import { TextInput } from '../components/forms/TextInput'
import { renderPageHeader } from '../util/pageUtils'
import { MaintenanceModeSettings } from '@juniper/ui-core'
import Api from 'api/api'

export default function MaintenanceSettings() {
  const [settings, setSettings] = useState<MaintenanceModeSettings>()

  const loadSettings = async () => {
    const response = await Api.loadMaintenanceModeSettings()
    setSettings(response)
  }

  useEffect(() => {
    loadSettings()
  }, [])

  const doSave = () => {
    console.log('saving...')
  }

  return <div className={'mx-3'}><form onSubmit={e => {
    e.preventDefault()
  }}>
    {renderPageHeader('Maintenance Mode Settings')}
    { !settings ?
      <div className="alert alert-warning">
        <strong>Warning:</strong> Unable to load maintenance mode settings.
      </div> :
      <>
        <div className={'bg-light p-3 mb-3 rounded-3 border border-1'}>
          <h5 className={'mb-3'}>General Settings</h5>
          <Checkbox
            label={'Enable Maintenance Mode'}
            description={'When enabled, users will see the maintenance message. ' +
                      'Study staff and participants will not be allowed' +
                      ' to log in or use the system.'}
            checked={settings?.enabled || false}
            onChange={e => setSettings({ ...settings, enabled: e })}/>
          <Textarea
            rows={4}
            disabled={!settings?.enabled}
            label="Maintenance Message"
            description={'Message to display to study staff and participants' +
                ' when maintenance mode is enabled. Markdown is supported.'}
            value={settings?.message} onChange={e => setSettings({ ...settings, message: e })}
          />
          <TextInput
            label={'Maintenance Bypass Password'}
            disabled={!settings?.enabled}
            description={'Password required to bypass maintenance mode. This is not encrypted in any way and ' +
                      'is only intended to discourage access to the application.'}
            value={settings?.maintenancePassword || ''}
            onChange={e => setSettings({ ...settings, maintenancePassword: e })}
          />
        </div>
        <div className={'bg-light p-3 my-3 rounded-3 border border-1'}>
          <h5 className={'mb-3'}>Other Settings</h5>
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
  </form></div>
}
