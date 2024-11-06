import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faEye, faGear, faImage, faPalette, faPencil } from '@fortawesome/free-solid-svg-icons'
import React from 'react'
import { Portal } from '@juniper/ui-core'
import { DropdownButton } from 'study/participants/survey/SurveyResponseView'
import { useNavigate } from 'react-router-dom'
import { siteContentPath, siteMediaPath } from 'portal/PortalRouter'
import { InfoCard, InfoCardBody, InfoCardHeader } from 'components/InfoCard'

export const WebsiteWidget = ({ portal }: { portal: Portal }) => {
  const livePortalUrl = portal.portalEnvironments.find(env =>
    env.environmentName === 'live')?.portalEnvironmentConfig.participantHostname

  return (
    <InfoCard classNames={''}>
      <InfoCardHeader>
        <div className="d-flex align-items-center justify-content-between w-100">
          <span className="fw-bold">Website</span>
          <div className="d-flex">
            <Button onClick={() => {
              const url = livePortalUrl?.startsWith('http') ? livePortalUrl : `https://${livePortalUrl}`
              window.open(url, '_blank')
            }}
            tooltip={'Preview live website'}
            variant="light" className="border m-1">
              <FontAwesomeIcon icon={faEye} className="fa-lg"/> Preview
            </Button>
            <CustomizeWebsiteDropdown portal={portal}/>
          </div>
        </div>
      </InfoCardHeader>
      <InfoCardBody>
        <div className="d-flex">
          <div style={{ minHeight: '200px' }}>
            { livePortalUrl ?
              <img className="border rounded-3"
                style={{ maxWidth: '100%', height: 'auto' }}
                src={`https://api.urlbox.io/v1/A8hJem0DKIAE8mDm/png?url=${livePortalUrl}`}
                alt={'Website preview'}/> :
              <span className="text-muted bg-gray">Website not yet published</span>
            }
          </div>
        </div>
      </InfoCardBody>
    </InfoCard>
  )
}

const CustomizeWebsiteDropdown = ({ portal }: { portal: Portal }) => {
  const navigate = useNavigate()

  return (
    <div className="dropdown">
      <Button
        data-bs-toggle='dropdown'
        className="dropdown-toggle border m-1"
        type="button"
        id="customizeWebsiteMenu"
        variant="light"
        aria-haspopup="true"
        aria-expanded="false"
        aria-label={'Customize website'}
      >
        <FontAwesomeIcon icon={faGear} className="fa-lg"/> Customize
      </Button>
      <div className="dropdown-menu" aria-labelledby="customizeWebsiteMenu">
        <DropdownButton
          onClick={() => navigate(siteContentPath(portal.shortcode, 'sandbox'))}
          icon={faPencil}
          label="Edit website"
          description="Design your portal website"
        />
        <div className="dropdown-divider my-1"></div>
        {/* rendering the existing randing modal within the context of the portal dashboard
            isn't particularly clean, from a user experience perspective
            for now, we'll direct users to the sandbox site editor to edit branding */}
        <DropdownButton
          onClick={() => navigate(siteContentPath(portal.shortcode, 'sandbox'))}
          icon={faPalette}
          label="Edit branding"
          description="Customize styling and colors"
        />
        <div className="dropdown-divider my-1"></div>
        <DropdownButton
          onClick={() => navigate(siteMediaPath(portal.shortcode, 'sandbox'))}
          icon={faImage}
          label="Manage media"
          description="Add or update images and files"
        />
      </div>
    </div>
  )
}
