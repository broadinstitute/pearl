import React, { useState } from 'react'
import Api from 'api/api'
import { currentIsoDate, saveBlobAsDownload } from '@juniper/ui-core'
import { PortalShortcodeControl } from './PopulateControls'
import LoadingSpinner from 'util/LoadingSpinner'
import { Button } from 'components/forms/Button'
import { doApiLoad } from 'api/api-utils'
import { successNotification } from '../util/notifications'
import { Store } from 'react-notifications-component'

/** control for downloading portal configs as a zip file */
export default function ExtractPortal({ initialPortalShortcode }: {initialPortalShortcode: string}) {
  const [portalShortcode, setPortalShortcode] = useState(initialPortalShortcode)
  const [isLoading, setIsLoading] = useState(false)

  const [onlyExtractActiveVersions, setOnlyExtractActiveVersions] = useState(false)

  const doExport = async () => {
    doApiLoad(async () => {
      const response = await Api.extractPortal(portalShortcode, onlyExtractActiveVersions)
      const blob = await response.blob()
      const fileName = `${currentIsoDate()}-${portalShortcode}-config.zip`
      saveBlobAsDownload(blob, fileName)
      Store.addNotification(successNotification('Portal config downloaded'))
    }, { setIsLoading })
  }

  return <form onSubmit={e => {
    e.preventDefault()
    if (!isLoading) {
      doExport()
    }
  }}>
    <h3>Extract portal</h3>
    <PortalShortcodeControl portalShortcode={portalShortcode} setPortalShortcode={setPortalShortcode}/>
    <br/>
    <div className="form-check">
      <input
        className="form-check-input"
        type="checkbox"
        value={onlyExtractActiveVersions.toString()}
        onChange={e => setOnlyExtractActiveVersions(e.target.checked)}
        id="onlyPublishedVersions"/>
      <label className="form-check-label" htmlFor="onlyPublishedVersions">
        Only extract active content
      </label>
    </div>
    <br/>
    <Button variant="primary" type="button" onClick={doExport} disabled={isLoading}>
      {isLoading ? <LoadingSpinner/> : 'Download configs'}
    </Button>
  </form>
}
