import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faUserPen } from '@fortawesome/free-solid-svg-icons'
import React, { useState } from 'react'
import { Portal } from '@juniper/ui-core'
import { useNavigate } from 'react-router-dom'
import { portalUsersPath } from 'user/AdminUserRouter'
import { useLoadingEffect } from 'api/api-utils'
import Api from 'api/api'
import { AdminUser, Role } from 'api/adminUser'
import LoadingSpinner from 'util/LoadingSpinner'
import { InfoCard, InfoCardBody, InfoCardHeader } from 'components/InfoCard'

export const StudyTeamWidget = ({ portal }: { portal: Portal }) => {
  const [users, setUsers] = useState<AdminUser[]>([])
  const navigate = useNavigate()
  const [roles, setRoles] = useState<Role[]>([])

  const { isLoading: isLoadingRoles } = useLoadingEffect(async () => {
    const fetchedRoles = await Api.fetchRoles()
    setRoles(fetchedRoles)
  })

  const { isLoading: isLoadingUsers } = useLoadingEffect(async () => {
    const result = await Api.fetchAdminUsersByPortal(portal.shortcode)
    setUsers(result)
  })

  return (
    <InfoCard classNames={''}>
      <InfoCardHeader>
        <div className="d-flex align-items-center justify-content-between w-100">
          <span className="fw-bold">Team Members</span>
          <Button onClick={() => navigate(portalUsersPath(portal.shortcode))}
            tooltip={'View all team members'}
            variant="light" className="border">
            <FontAwesomeIcon icon={faUserPen} className="fa-lg"/> Manage
          </Button>
        </div>
      </InfoCardHeader>
      <InfoCardBody>
        <LoadingSpinner isLoading={isLoadingRoles || isLoadingUsers}>
          { /* filter out superusers; they transcend portals */ }
          {users.filter(u => !u.superuser).map(user => {
            return (
              <div className={'mb-1'} key={user.id}>{user.username}
                {user.portalAdminUsers![0].portalAdminUserRoles.map(role => {
                  return <span key={`${user.id}-${role.id}`} className="ms-1 badge bg-primary">
                    {roles.find(r => r.id === role.roleId)?.displayName}</span>
                })}
              </div>
            )
          })}
        </LoadingSpinner>
      </InfoCardBody>
    </InfoCard>
  )
}
