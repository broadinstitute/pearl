import React from 'react'
import { Link, Route, Routes } from 'react-router-dom'
import UserList from './UserList'
import AdminUserDetail from './AdminUserDetail'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import { Portal, StudyEnvParams } from '@juniper/ui-core'
import RolesList from './RolesList'
import { OptionalStudyEnvParams } from '@juniper/ui-core/build'
import { baseStudyEnvPath } from '../study/StudyEnvironmentRouter'

/**
 * Handles user management paths across all users
 */
export default function AdminUserRouter() {
  return <>
    <NavBreadcrumb value="users">
      <Link to="users">Users</Link>
    </NavBreadcrumb>
    <Routes>
      <Route path="roles" element={<RolesList/>}/>
      <Route path=":adminUserId" element={<AdminUserDetail/>}/>
      <Route index element={<UserList/>}/>
      <Route path="*" element={<div>Unknown admin user page</div>}/>
    </Routes>
  </>
}

/**
 * handles user management paths for the given portal & study
 */
export function PortalAdminUserRouter({ portal, studyEnvParams }:
  {portal: Portal, studyEnvParams: OptionalStudyEnvParams}) {
  return <>
    <NavBreadcrumb value="users">
      <Link to={portalUsersPath(studyEnvParams)}>Users</Link>
    </NavBreadcrumb>
    <Routes>
      <Route path="roles" element={<RolesList/>}/>
      <Route path=":adminUserId" element={<AdminUserDetail portalShortcode={portal.shortcode}/>}/>
      <Route index element={<UserList portal={portal}/>}/>
      <Route path="*" element={<div>Unknown portal admin user page</div>}/>
    </Routes>
  </>
}

/** path to portal-specific user list, but keeps study in-context */
export const portalUsersPath = (studyEnvParams: OptionalStudyEnvParams) => {
  if (studyEnvParams.studyShortcode) {
    return `${baseStudyEnvPath(studyEnvParams as StudyEnvParams)}/users`
  }
  return `/${studyEnvParams.portalShortcode}/users`
}
