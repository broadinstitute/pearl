import React from 'react'
import { AdminUser } from 'api/adminUser'
import {
  UserContext,
  UserContextT
} from 'user/UserProvider'

/** returns simple admin user for testing */
export const mockAdminUser = (superuser: boolean): AdminUser => {
  return {
    id: 'adminUser1',
    createdAt: 0,
    lastLogin: 0,
    username: 'blah',
    superuser,
    token: 'fakeToken',
    portalAdminUsers: [],
    portalPermissions: {}
  }
}

export const mockAdminUserWithPerms = (portalId: string, perms: string[]): AdminUser => {
  return {
    id: 'adminUser1',
    createdAt: 0,
    lastLogin: 0,
    username: 'blah',
    superuser: false,
    token: 'fakeToken',
    portalPermissions: {
      [portalId]: perms
    },
    portalAdminUsers: [
      {
        portalId,
        portalAdminUserRoles: [],
        roles: [{
          id: 'role1',
          name: 'role1',
          description: 'role1',
          displayName: 'role1',
          permissions: perms.map(perm => ({
            name: perm,
            description: perm,
            displayName: perm,
            permissionId: perm,
            permissionName: perm,
            permissionDescription: perm
          }))
        }]
      }
    ]
  }
}

/** component for wrapping test components that require a superuser from context */
export const MockSuperuserProvider = ({ children }: { children: React.ReactNode }) => {
  return <MockUserProvider user={mockAdminUser(true)}>{children}</MockUserProvider>
}

/** component for wrapping test components that require a non-superuser from context */
export const MockRegularUserProvider = ({ children }: { children: React.ReactNode }) => {
  return <MockUserProvider user={mockAdminUser(false)}>{children}</MockUserProvider>
}

/** component for wrapping test components that require a user from context */
export const MockUserProvider = ({ children, user }: { children: React.ReactNode, user: AdminUser }) => {
  const fakeUserContext: UserContextT = {
    user,
    loginUser: () => null,
    loginUserUnauthed: () => null,
    logoutUser: () => null,
    toggleSuperuserOverride: () => null
  }
  return <UserContext.Provider value={fakeUserContext}>
    {children}
  </UserContext.Provider>
}

