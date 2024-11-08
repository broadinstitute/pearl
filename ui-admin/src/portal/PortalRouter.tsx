import React, { useContext } from 'react'
import {
  Link,
  Route,
  Routes,
  useParams
} from 'react-router-dom'
import StudyRouter from '../study/StudyRouter'
import PortalDashboard from './dashboard/PortalDashboard'
import {
  LoadedPortalContextT,
  PortalContext,
  PortalParams
} from './PortalProvider'
import MailingListView from './MailingListView'
import PortalEnvView from './PortalEnvView'
import PortalParticipantsView from './PortalParticipantView'
import {
  ApiProvider,
  I18nProvider,
  Portal,
  PortalEnvironment
} from '@juniper/ui-core'
import SiteContentLoader from './siteContent/SiteContentLoader'
import { PortalAdminUserRouter } from 'user/AdminUserRouter'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import { portalEnvPath } from 'study/StudyEnvironmentRouter'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronRight, faHome } from '@fortawesome/free-solid-svg-icons'
import SiteMediaList from './media/SiteMediaList'
import { previewApi } from 'util/apiContextUtils'
import { EnvironmentSwitcher } from '../navbar/EnvironmentSwitcher'

export type PortalEnvContext = {
  portal: Portal
  updatePortal: (portal: Portal) => void  // this updates the UI -- it does not handle server-side operations
  reloadPortal: (shortcode: string) => Promise<Portal>
  portalEnv: PortalEnvironment
  updatePortalEnv: (portalEnv: PortalEnvironment) => void // this updates the UI -- not server-side operations
}

/** controls routes for within a portal */
export default function PortalRouter() {
  const portalContext = useContext(PortalContext) as LoadedPortalContextT
  return <>
    <NavBreadcrumb value={portalHomePath(portalContext.portal.shortcode)}>
      <Link className='me-2' to={''}>
        <FontAwesomeIcon icon={faHome}/> Home
      </Link>
      <FontAwesomeIcon icon={faChevronRight} className="fa-xs text-muted me-2"/>
      <Link className='me-2' to={portalHomePath(portalContext.portal.shortcode)}>
        {portalContext.portal.name}
      </Link>
    </NavBreadcrumb>
    <Routes>
      <Route path="studies">
        <Route path=":studyShortcode/*" element={<StudyRouter portalContext={portalContext}/>}/>
      </Route>
      <Route path="env/:portalEnv/*" element={<PortalEnvRouter portalContext={portalContext}/>}/>
      <Route path="users/*" element={<PortalAdminUserRouter portal={portalContext.portal}/>}/>
      <Route index element={<PortalDashboard portal={portalContext.portal}/>}/>
      <Route path="*" element={<div>Unmatched portal route</div>}/>
    </Routes>
  </>
}

export const usePortalEnvParamsFromPath = () => {
  const params = useParams<PortalParams>()
  return {
    portalShortcode: params.portalShortcode,
    portalEnv: params.portalEnv
  }
}

/** controls routes within a portal environment, such as config, mailing list, etc... */
function PortalEnvRouter({ portalContext }: {portalContext: LoadedPortalContextT}) {
  const params = useParams<PortalParams>()
  const portalEnvName: string | undefined = params.portalEnv
  const { portal } = portalContext
  const portalEnv = portal.portalEnvironments.find(env => env.environmentName === portalEnvName)
  if (!portalEnv) {
    return <div>No environment matches {portalEnvName}</div>
  }

  const portalEnvContext: PortalEnvContext = {
    ...portalContext,
    portalEnv
  }

  const currentEnvPath = portalEnvPath(portal.shortcode, portalEnvName || '')

  return <>
    <EnvironmentSwitcher currentEnvPath={currentEnvPath} envName={portalEnvName || ''}/>
    <ApiProvider api={previewApi(portal.shortcode, portalEnv.environmentName)}>
      <I18nProvider defaultLanguage={'en'} portalShortcode={portal.shortcode}>
        <Routes>
          <Route path="participants" element={<PortalParticipantsView portalEnv={portalEnv} portal={portal}/>}/>
          <Route path="siteContent" element={<SiteContentLoader portalEnvContext={portalEnvContext}/>}/>
          <Route path="media" element={<SiteMediaList portalContext={portalContext} portalEnv={portalEnv}/>}/>
          <Route path="mailingList" element={<MailingListView portalContext={portalContext}
            portalEnv={portalEnv}/>}/>
          <Route index element={<PortalEnvView portal={portal} portalEnv={portalEnv}/>}/>
          <Route path={'*'} element={<div>Unmatched portal environment route</div>}/>
        </Routes>
      </I18nProvider>
    </ApiProvider>
  </>
}

/** admin homepage for a given portal */
export const portalHomePath = (portalShortcode: string) => {
  return `/${portalShortcode}`
}

/** path to portal-specific user list */
export const usersPath = (portalShortcode: string) => {
  return `/${portalShortcode}/users`
}

/** gets absolute path to the portal mailing list page */
export const mailingListPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/mailingList`
}

/** path to edit the site content */
export const siteContentPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/siteContent`
}

export const siteMediaPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/media`
}

/** path to env config for the portal */
export const portalConfigPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/config`
}

/** path to study participant list */
export const studyParticipantsPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/participants`
}

/** Construct a path to a study's kit management interface. */
export const studyKitsPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/kits`
}

/** view study content, surveys, consents, etc... */
export const studyContentPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}`
}

/** list all participants in all studies for the portal */
export const portalParticipantsPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/participants`
}

