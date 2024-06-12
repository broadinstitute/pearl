import React from 'react'
import {
  Link,
  Route,
  Routes
} from 'react-router-dom'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import { StudyEnvParams } from '@juniper/ui-core'
import FamilyView from './FamilyView'

/** routes to family views as appropriate */
export default function FamilyRouter({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  return <>
    <NavBreadcrumb value={studyEnvContext.currentEnvPath}>
      <Link to={`${studyEnvContext.currentEnvPath}/participants`} className="me-1">
          participants</Link>
    </NavBreadcrumb>
    <Routes>
      <Route path=":familyShortcode/*" element={<FamilyView studyEnvContext={studyEnvContext}/>}/>
      <Route path="*" element={<div>Unknown family page</div>}/>
    </Routes>
  </>
}

/** helper to get participant list page path */
export const studyEnvFamilyPath = (studyEnvParams: StudyEnvParams, shortcode: string) => {
  const { portalShortcode, studyShortcode, envName } = studyEnvParams
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/families/${shortcode}`
}