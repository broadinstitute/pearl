import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faEye, faGear, faImage, faPencil } from '@fortawesome/free-solid-svg-icons'
import React from 'react'
import { ApiProvider, HtmlSection, HtmlSectionView, I18nProvider, Portal } from '@juniper/ui-core'
import { DropdownButton } from 'study/participants/survey/SurveyResponseView'
import { useNavigate } from 'react-router-dom'
import { siteContentPath, siteMediaPath } from 'portal/PortalRouter'
import { InfoCard, InfoCardBody, InfoCardHeader } from 'components/InfoCard'
import ErrorBoundary from '../../../util/ErrorBoundary'
import { previewApi } from '../../../util/apiContextUtils'
import { NavbarPreview } from '../../siteContent/NavbarPreview'

export const WebsiteWidget = ({ portal }: { portal: Portal }) => {
  const livePortalUrl = portal.portalEnvironments.find(env =>
    env.environmentName === 'live')?.portalEnvironmentConfig.participantHostname

  const localContent = portal.portalEnvironments.find(env => env.environmentName === 'live')!
    .siteContent?.localizedSiteContents[0]

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
            <CustomizeWebsiteDropdown portal={portal}/>
          </div>
        </div>
      </InfoCardHeader>
      <InfoCardBody>
        <div className="container">
          <div className="w-100">
            <div style={{
              minHeight: '200px'
            }} className="d-flex justify-content-center align-items-center">
              {localContent ?
                <ErrorBoundary>
                  <ApiProvider api={previewApi(
                    portal.shortcode,
                    'live'
                  )}>
                    <I18nProvider defaultLanguage={'en'} portalShortcode={portal.shortcode}>
                      <div
                        style={{
                          transform: 'scale(0.67)', transformOrigin: 'top',
                          cursor: 'not-allowed', pointerEvents: 'none', height: 'auto', width: 'auto'
                        }}>
                        <NavbarPreview
                          portal={portal}
                          portalEnv={portal.portalEnvironments.find(env => env.environmentName === 'live')!}
                          localContent={
                            localContent
                          }
                        />
                        {localContent.landingPage.sections.slice(0, 2).map((section: HtmlSection) =>
                          <HtmlSectionView section={section} key={section.id}/>)
                        }
                      </div>
                    </I18nProvider>
                  </ApiProvider>
                </ErrorBoundary>
                :
                <span className="text-muted bg-gray">Website not yet published</span>
              }
            </div>
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
