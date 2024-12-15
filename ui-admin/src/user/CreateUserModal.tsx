import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api, { Portal } from 'api/api'
import { AdminUser, AdminUserParams, Role } from 'api/adminUser'
import { useUser } from './UserProvider'
import LoadingSpinner from 'util/LoadingSpinner'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import Select from 'react-select'
import { doApiLoad, useLoadingEffect } from '../api/api-utils'
import { RoleSelector } from './AdminUserDetail'
import useReactSingleSelect from '../util/react-select-utils'

const DEFAULT_ROLE = 'study_admin'

/** creates a new admin user */
const CreateUserModal = ({ onDismiss, portals, userCreated }:
                           { onDismiss: () => void,
                             portals: Portal[], userCreated: () => void
                           }) => {
  const [isLoading, setIsLoading] = useState(false)
  const { user } = useUser()
  const [newUser, setNewUser] = useState<AdminUserParams>({
    username: '',
    superuser: false,
    portalShortcode: portals.length > 0 ? portals[0].shortcode : null,
    roleNames: []
  })
  const [roles, setRoles] = useState<Role[]>([])

  const { isLoading: rolesLoading } = useLoadingEffect(async () => {
    const fetchedRoles = await Api.fetchRoles()
    setNewUser({
      ...newUser,
      roleNames: fetchedRoles.filter(role => role.name === DEFAULT_ROLE).map(role => role.name)
    })
    setRoles(fetchedRoles)
  })

  useReactSingleSelect<Portal>(portals,
    portal => ({ label: portal.name, value: portal }),
    (opt: Portal | undefined) => setNewUser({ ...newUser, portalShortcode: opt?.shortcode ?? null }),
    portals.find(portal => portal.shortcode === newUser.portalShortcode))

  const createUser = async () => {
    await doApiLoad(async () => {
      let createdUser: AdminUser
      if (!newUser.superuser) {
        createdUser = await Api.createPortalUser(newUser)
      } else {
        createdUser = await Api.createSuperuser(newUser)
      }
      Store.addNotification(successNotification(`${createdUser.username} created`))
      userCreated()
      onDismiss()
    }, { setIsLoading })
  }
  // username must be email-like, and either be a superuser or associated with a Portal (we're not yet supporting
  // mutliselect for users spanning multiple portals)
  const isUserValid = /^\S+@\S+\.\S+$/.test(newUser.username) && (newUser.superuser || newUser.portalShortcode)

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Add admin user</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div className="py-2">
          <div className="mb-3">
            <label className="form-label d-block">
              Email
              <input type="email" value={newUser.username} className="form-control"
                onChange={e => setNewUser({ ...newUser, username: e.target.value })}/>
            </label>
            <span className="form-text ps-3">Email must be a Microsoft- or Google-based account</span>
          </div>
          {user?.superuser && <div className="mb-3">
            <span>Superuser</span><br/>
            <label className="me-3">
              <input type="radio" name="superuser" value="true" checked={newUser.superuser}
                onChange={() => setNewUser({ ...newUser, portalShortcode: null, superuser: true })}
                className="me-1"/> Yes
            </label>
            <label>
              <input type="radio" name="superuser" value="false" checked={!newUser.superuser} className="me-1"
                onChange={() => setNewUser({ ...newUser, portalShortcode: null, superuser: false })}/> No
            </label>
          </div> }
          { !newUser.superuser && <div>
            <div className="mb-3">
              <PortalSelector portals={portals}
                selectedPortal={portals.find(p => p.shortcode === newUser.portalShortcode)}
                setSelectedPortal={portal => setNewUser({
                  ...newUser,
                  portalShortcode: portal?.shortcode ?? null
                })}/>
            </div>
            <div>
              <RoleSelector roles={roles} selectedRoleNames={newUser.roleNames} setSelectedRoleNames={roleNames =>
                setNewUser({ ...newUser, roleNames })}/>
            </div>
          </div> }
        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading || rolesLoading}>
        <button className="btn btn-primary" onClick={createUser} disabled={!isUserValid}>Create</button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default CreateUserModal

export const PortalSelector = ({ portals, selectedPortal, setSelectedPortal }: {
    portals: Portal[], selectedPortal?: Portal, setSelectedPortal: (portal?: Portal) => void
    }) => {
  const { onChange, options, selectedOption, selectInputId } = useReactSingleSelect<Portal>(portals,
    portal => ({ label: portal.name, value: portal }),
    (opt: Portal | undefined) => setSelectedPortal(opt),
    portals.find(portal => portal.shortcode === selectedPortal?.shortcode))

  return <>
    <label className="form-label" htmlFor={selectInputId}>
        Portal
    </label>
    <Select options={options} value={selectedOption} onChange={onChange} inputId={selectInputId}/>
  </>
}
