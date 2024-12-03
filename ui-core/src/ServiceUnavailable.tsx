import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircleExclamation } from '@fortawesome/free-solid-svg-icons'
import { useI18n } from '@juniper/ui-core'
import { SUPPORT_EMAIL_ADDRESS } from 'src/util/supportUtils'

export function ServiceUnavailable({ error }: { error: string }) {
  const { i18n } = useI18n()

  return (
    <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0">
      <main className="flex-grow-1 p-2 d-flex flex-column justify-content-center">
        <div className="fs-1 fw-bold d-flex justify-content-center">
          <div>
            <FontAwesomeIcon className="me-2" icon={faCircleExclamation}/>
            <span>{i18n('applicationUnavailable')}</span>
          </div>
        </div>
        <div className="fs-2 fw-light d-flex justify-content-center text-center">
          <div>
            <span>
              {i18n('applicationUnavailableMessage', {
                substitutions: {
                  SUPPORT_EMAIL_ADDRESS
                }
              })}
            </span>
          </div>
        </div>
        <div className="fs-5 fw-light d-flex justify-content-center text-center mt-3 bg-light">
          <code className="p-3">{error}</code>
        </div>
      </main>
    </div>
  )
}
