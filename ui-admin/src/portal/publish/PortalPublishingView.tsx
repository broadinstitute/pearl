import React from 'react'
import { Portal, PortalEnvironment } from '@juniper/ui-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faHistory } from '@fortawesome/free-solid-svg-icons'
import { Link } from 'react-router-dom'
import Api from '../../api/api'
import { faExternalLink } from '@fortawesome/free-solid-svg-icons/faExternalLink'
import { useConfig } from '../../providers/ConfigProvider'
import { portalPublishHistoryPath, studyEnvSiteContentPath } from '../../study/StudyEnvironmentRouter'
import { ENVIRONMENT_ICON_MAP } from '../../util/publishUtils'
import { studyDiffPath } from '../../study/StudyRouter'
import { renderPageHeader } from '../../util/pageUtils'
import { navListItemStyle } from '../../util/subNavStyles'


const ENV_SORT_ORDER = ['sandbox', 'irb', 'live']
/** Page an admin user sees immediately after logging in */
export default function PortalPublishingView({ portal, studyShortcode }: {portal: Portal, studyShortcode: string}) {
  const sortedEnvs = portal.portalEnvironments.sort((pa, pb) =>
    ENV_SORT_ORDER.indexOf(pa.environmentName) - ENV_SORT_ORDER.indexOf(pb.environmentName))
  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Publish Content') }
    <div className="mb-3">
      <Link to={portalPublishHistoryPath(portal.shortcode, studyShortcode)}>
        <FontAwesomeIcon icon={faHistory}/> Publishing history
      </Link>
    </div>
    <div>
      <p>
        Juniper manages three environments for each study: sandbox, irb, and live.
        The sandbox environment is for testing, the irb environment is for IRB review,
        and the live environment is for real participant use.
      </p>
      <p>
       Once you have made changes to your study in the sandbox, you can publish those changes to the irb environment.
       If these changes require IRB review, you can then continue to make other changes in the sandbox while
       you wait for approval.  Once the changes are approved (or if they do not require IRB review), you can publish
       from the IRB environment to the live environment.
      </p>

    </div>
    <div className="row">
      { sortedEnvs.map(portalEnv =>
        <PortalEnvPublishingView portalEnv={portalEnv} portal={portal}
          studyShortcode={studyShortcode} key={portalEnv.environmentName}/>)}
    </div>

  </div>
}


/** shows publishing related info and controls for a given environment */
function PortalEnvPublishingView({ portal, portalEnv, studyShortcode }:
                                          {portal: Portal, portalEnv: PortalEnvironment, studyShortcode: string}) {
  const envIcon = ENVIRONMENT_ICON_MAP[portalEnv.environmentName]
  const zoneConfig = useConfig()
  const isInitialized = portalEnv.portalEnvironmentConfig.initialized
  const destName = portalEnv.environmentName === 'sandbox' ? 'irb' : 'live'

  return <>
    <div className="p-3 rounded-3 mb-3" style={navListItemStyle}>
      <div>
        <h3 className="h5 text-capitalize me-4">{envIcon} {portalEnv.environmentName}</h3>
        { isInitialized && <a href={Api.getParticipantLink(portalEnv.portalEnvironmentConfig,
          zoneConfig.participantUiHostname,
          portal.shortcode, portalEnv.environmentName)}
        target="_blank">
          Participant view <FontAwesomeIcon icon={faExternalLink}/>
        </a> }
      </div>
      <div>
        { !isInitialized && <div className="fst-italic text-muted">Not initialized</div> }
        { isInitialized && <div>
                  Website:
          {portalEnv.siteContent && <span>
            <Link to={studyEnvSiteContentPath(portal.shortcode, studyShortcode,
              portalEnv.environmentName)}
            className="ms-2 fw-normal">
              v{portalEnv.siteContent.version}
            </Link>
          </span>
          }
        </div>}
      </div>
      <div className="mt-2">
        {(isInitialized && portalEnv.environmentName !== 'live') &&
          <Link to={
            studyDiffPath(portal.shortcode, studyShortcode, portalEnv.environmentName, destName)}
          className="btn btn-primary me-2 px-5">
            Publish {portalEnv.environmentName} to {destName}
          </Link>
        }
      </div>
    </div>
  </>
}

