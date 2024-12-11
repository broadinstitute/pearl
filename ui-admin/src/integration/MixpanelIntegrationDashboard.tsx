import React, { useState } from 'react'
import { useLoadingEffect } from 'api/api-utils'
import Api, { InternalConfig } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'


export default function MixpanelIntegrationDashboard() {
  const [config, setConfig] = useState<InternalConfig>()

  const { isLoading } = useLoadingEffect(async () => {
    const response = await Api.fetchInternalConfig()
    setConfig(response)
  })

  return <div>
    <h2 className="h3">Mixpanel</h2>
    <div className="mt-4">
      <h3 className="h5">Config</h3>
      {!isLoading && <dl>
        <dt>enabled</dt>
        <dd>{config?.mixpanel.enabled}</dd>
        <dt>token</dt>
        <dd>{config?.mixpanel.token}</dd>
      </dl>}
      {isLoading && <LoadingSpinner/>}
    </div>
  </div>
}
