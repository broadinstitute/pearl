import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faUserPen } from '@fortawesome/free-solid-svg-icons'
import React, { useState } from 'react'
import { Portal } from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import { portalUsersPath } from 'user/AdminUserRouter'
import { useLoadingEffect } from 'api/api-utils'
import Api from 'api/api'
import { AdminUser, Role } from 'api/adminUser'
import LoadingSpinner from 'util/LoadingSpinner'
import { InfoCard, InfoCardBody, InfoCardHeader } from 'components/InfoCard'
import { useStudyEnvParamsFromPath } from '../../../study/StudyEnvironmentRouter'

export const StudyTeamWidget = ({ portal }: { portal: Portal }) => {
  const [users, setUsers] = useState<AdminUser[]>([])
  const [roles, setRoles] = useState<Role[]>([])
  const studyEnvParams = useStudyEnvParamsFromPath()
  const { isLoading: isLoadingRoles } = useLoadingEffect(async () => {
    const fetchedRoles = await Api.fetchRoles()
    setRoles(fetchedRoles)
  })

  const { isLoading: isLoadingUsers } = useLoadingEffect(async () => {
    const result = await Api.fetchAdminUsersByPortal(portal.shortcode)
    setUsers(result)
  })

  return (
    <InfoCard>
      <InfoCardHeader>
        <div className="d-flex align-items-center justify-content-between w-100">
          <span className="fw-bold">Team Members</span>
          <Link to={portalUsersPath(studyEnvParams)}>
            <Button tooltip={'View all team members'}
              variant="light" className="border">
              <FontAwesomeIcon icon={faUserPen} className="fa-lg"/> Manage
            </Button>
          </Link>
        </div>
      </InfoCardHeader>
      <InfoCardBody>
        <LoadingSpinner isLoading={isLoadingRoles || isLoadingUsers}>
          {users.length === 0 ?
            <div className={'text-muted fst-italic'}>No team members</div> :
            <ul className='list-unstyled mb-0'>
              { /* filter out superusers; they transcend portals */ }
              {users.filter(u => !u.superuser).map(user => {
                return (
                  <li className={'mb-1'} key={user.id}>{user.username}
                    {user.portalAdminUsers![0].portalAdminUserRoles.map(role => {
                      return <span key={`${user.id}-${role.id}`} className="ms-1 badge bg-primary">
                        {roles.find(r => r.id === role.roleId)?.displayName}</span>
                    })}
                  </li>
                )
              })}
            </ul>}
        </LoadingSpinner>
      </InfoCardBody>
    </InfoCard>
  )
}
