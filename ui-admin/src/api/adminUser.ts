
export type AdminUser = {
  id: string,
  createdAt: number,
  username: string,
  token: string,
  superuser: boolean,
  lastLogin: number,
  portalPermissions: Record<string, string[]>,
  portalAdminUsers?: PortalAdminUser[]
};

export type AdminUserParams = {
  username: string,
  superuser: boolean,
  portalShortcode: string | null,
  roleNames: string[]
}

export type PortalAdminUser = {
  portalId: string
  roles: Role[]
  portalAdminUserRoles: PortalAdminUserRole[]
}

export type PortalAdminUserRole = {
  id: string,
  roleId: string,
  createdAt: number,
  portalAdminUserId: string
  lastUpdatedAt: number
}

export type Role = {
  id: string
  name: string
  displayName: string
  description: string
  permissions: Permission[]
}

export type Permission = {
  displayName: string
  description: string
  name: string
}
