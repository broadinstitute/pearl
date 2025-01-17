import {
  Portal,
  Study
} from '@juniper/ui-core'
import {
  NavLink,
  useNavigate
} from 'react-router-dom'
import {
  studyKitsPath,
  studyParticipantsPath
} from 'portal/PortalRouter'
import StudySelector from './StudySelector'
import React from 'react'
import {
  adminTasksPath, studyEnvMailingListPath, studyEnvSiteContentPath,
  studyEnvDataBrowserPath,
  studyEnvDatasetListViewPath,
  studyEnvExportIntegrationsPath,
  studyEnvFormsPath,
  studyEnvImportPath,
  studyEnvMetricsPath,
  studyEnvTriggersPath, studyEnvWorkflowPath,
  studyEnvSiteSettingsPath
} from 'study/StudyEnvironmentRouter'


import CollapsableMenu from './CollapsableMenu'
import {
  userHasPermission,
  useUser
} from 'user/UserProvider'
import {
  studyPublishingPath
} from 'study/StudyRouter'
import { sidebarNavLinkClasses } from './AdminSidebar'
import { portalUsersPath } from '../user/AdminUserRouter'


/** shows menu options related to the current study */
export const StudySidebar = ({ study, portalList, portalShortcode }:
                               { study: Study, portalList: Portal[], portalShortcode: string }) => {
  const navigate = useNavigate()
  const user = useUser()
  const portalId = portalList.find(p => p.shortcode === portalShortcode)?.id

  /** updates the selected study -- routes to that study's homepage */
  const setSelectedStudy = (portalShortcode: string, studyShortcode: string) => {
    navigate(studyParticipantsPath(portalShortcode, studyShortcode, 'live'))
  }
  const navStyleFunc = ({ isActive }: { isActive: boolean }) => {
    return isActive ? { background: 'rgba(255, 255, 255, 0.3)' } : {}
  }

  const studyParams = {
    portalShortcode,
    studyShortcode: study.shortcode
  }

  return <div className="pt-3">
    <StudySelector portalList={portalList} selectedShortcode={study.shortcode} setSelectedStudy={setSelectedStudy}/>
    <div className="text-white">
      <CollapsableMenu header={'Research Coordination'} content={<ul className="list-unstyled">
        <li className="mb-2">
          <NavLink to={studyParticipantsPath(portalShortcode, study.shortcode, 'live')}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Participants</NavLink>
        </li>
        <li className="mb-2">
          <NavLink to={studyKitsPath(portalShortcode, study.shortcode, 'live')}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Kits</NavLink>
        </li>
        <li className="mb-2">
          <NavLink to={adminTasksPath(portalShortcode, study.shortcode, 'live')}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Tasks</NavLink>
        </li>
        <li className="mb-2">
          <NavLink to={studyEnvImportPath(portalShortcode, study.shortcode, 'sandbox')}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Import Participants</NavLink>
        </li>
        <li className="mb-2">
          <NavLink to={studyEnvMailingListPath({ ...studyParams, envName: 'live' })}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Mailing List</NavLink>
        </li>
      </ul>}/>
      <CollapsableMenu header={'Analytics & Data'} content={<ul className="list-unstyled">
        <li className="mb-2">
          <NavLink to={studyEnvMetricsPath(portalShortcode, study.shortcode, 'live')}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Study Trends</NavLink>
        </li>
        <li className="mb-2">
          <NavLink to={studyEnvDataBrowserPath(portalShortcode, study.shortcode, 'live')}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Data Export</NavLink>
        </li>
        { portalId && userHasPermission(user.user, portalId, 'export_integration') && <li className="mb-2">
          <NavLink to={studyEnvExportIntegrationsPath({ ...studyParams, envName: 'live' })}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Export Integrations</NavLink>
        </li>
        }
        { portalId && userHasPermission(user.user, portalId, 'tdr_export') && <li>
          <NavLink to={studyEnvDatasetListViewPath(portalShortcode, study.shortcode, 'live')}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Terra Data Repo</NavLink>
        </li>
        }
      </ul>}/>
      <CollapsableMenu header={'Design & Build'} content={<ul className="list-unstyled">
        <li className="mb-2">
          <NavLink to={studyEnvSiteContentPath({ ...studyParams, envName: 'sandbox' })}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Website</NavLink>
        </li>
        <li className="mb-2">
          <NavLink to={studyEnvWorkflowPath({
            portalShortcode, studyShortcode: study.shortcode, envName: 'sandbox'
          })}
          className={sidebarNavLinkClasses} style={navStyleFunc}>Participant flow</NavLink>
        </li>
        <li className="mb-2">
          <NavLink to={studyEnvFormsPath(portalShortcode, study.shortcode, 'sandbox')}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Forms &amp; Surveys</NavLink>
        </li>
        <li className="mb-2">
          <NavLink to={studyEnvTriggersPath({
            portalShortcode, studyShortcode: study.shortcode, envName: 'sandbox'
          })}
          className={sidebarNavLinkClasses} style={navStyleFunc}>Emails &amp; Automation</NavLink>
        </li>
      </ul>}/>
      <CollapsableMenu header={'Publish'} content={<ul className="list-unstyled">
        <li className="mb-2">
          <NavLink to={studyPublishingPath(portalShortcode, study.shortcode)}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Publish Content</NavLink>
        </li>
        <li>
          <NavLink to={studyEnvSiteSettingsPath(portalShortcode, study.shortcode, 'live')}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Site Settings</NavLink>
        </li>
      </ul>}/>
      <CollapsableMenu header={'Manage'} content={<ul className="list-unstyled">
        <li className="mb-2">
          <NavLink to={portalUsersPath({ portalShortcode, studyShortcode: study.shortcode, envName: 'live' })}
            className={sidebarNavLinkClasses} style={navStyleFunc}>Team Members</NavLink>
        </li>
      </ul>}/>
    </div>
  </div>
}
