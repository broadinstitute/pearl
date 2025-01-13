import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowUpRightFromSquare, faEye, faGear, faImage, faPencil } from '@fortawesome/free-solid-svg-icons'
import React from 'react'
import { Portal } from '@juniper/ui-core'
import { DropdownButton } from 'study/participants/survey/SurveyResponseView'
import { useNavigate } from 'react-router-dom'
import { siteContentPath, siteMediaPath } from 'portal/PortalRouter'
import { InfoCard, InfoCardBody, InfoCardHeader } from 'components/InfoCard'
import {
  studyEnvSiteContentPath,
  studyEnvSiteMediaPath,
  useStudyEnvParamsFromPath
} from '../../../study/StudyEnvironmentRouter'

export const WebsiteWidget = ({ portal }: { portal: Portal }) => {
  const livePortalUrl = portal.portalEnvironments.find(env =>
    env.environmentName === 'live')?.portalEnvironmentConfig.participantHostname
  const { studyShortcode } = useStudyEnvParamsFromPath()

  return (
    <InfoCard>
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
            <CustomizeWebsiteDropdown portal={portal} studyShortcode={studyShortcode}/>
          </div>
        </div>
      </InfoCardHeader>
      <InfoCardBody>
        <div className="container">
          <div className="w-100">
            <div style={{ minHeight: '200px' }} className="d-flex justify-content-center align-items-center">
              {livePortalUrl ?
                <Button
                  variant="light"
                  className="border"
                  onClick={() => {
                    const url = livePortalUrl?.startsWith('http') ? livePortalUrl : `https://${livePortalUrl}`
                    window.open(url, '_blank')
                  }}
                >
                  <FontAwesomeIcon icon={faArrowUpRightFromSquare}/> Visit website
                </Button> :
                <span className="text-muted bg-gray">Website not yet published</span>
              }
            </div>
          </div>
        </div>
      </InfoCardBody>
    </InfoCard>
  )
}

const CustomizeWebsiteDropdown = ({ portal, studyShortcode }: { portal: Portal, studyShortcode?: string }) => {
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
          onClick={() => navigate(studyShortcode ?
            studyEnvSiteContentPath({
              portalShortcode: portal.shortcode, studyShortcode, envName: 'sandbox'
            }) :
            siteContentPath(portal.shortcode, 'sandbox'))}
          icon={faPencil}
          label="Edit website"
          description="Design your portal website"
        />
        <div className="dropdown-divider my-1"></div>
        <DropdownButton
          onClick={() => navigate(studyShortcode ?
            studyEnvSiteMediaPath({
              portalShortcode: portal.shortcode, studyShortcode, envName: 'sandbox'
            }) :
            siteMediaPath(portal.shortcode, 'sandbox')
          )}
          icon={faImage}
          label="Manage media"
          description="Add or update images and files"
        />
      </div>
    </div>
  )
}
