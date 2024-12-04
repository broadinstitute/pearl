import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircleExclamation } from '@fortawesome/free-solid-svg-icons'
import { SUPPORT_EMAIL_ADDRESS } from 'src/util/supportUtils'

export function ServiceUnavailable({ error }: { error?: string }) {
  return (
    <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0 w-75">
      <main className="flex-grow-1 p-2 d-flex flex-column justify-content-center">
        <div className="fs-1 fw-bold d-flex justify-content-center">
          <div>
            <FontAwesomeIcon className="me-2" icon={faCircleExclamation}/>
            <span>Application unavailable</span>
          </div>
        </div>
        <div className="fs-2 fw-light d-flex justify-content-center text-center mt-2">
          <div>
            <span>
              This application is currently unavailable. We apologize for the inconvenience.
              Please try again later, or
              contact <a href={`mailto:${SUPPORT_EMAIL_ADDRESS}`}>{SUPPORT_EMAIL_ADDRESS}</a> if the issue persists.
            </span>
          </div>
        </div>
        { error && <div className="fs-5 fw-light d-flex justify-content-center text-center mt-3 bg-light">
          <code className="p-3">{error}</code>
        </div> }
      </main>
    </div>
  )
}
