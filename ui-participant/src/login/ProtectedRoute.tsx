import React, { ReactNode } from 'react'
import { Outlet } from 'react-router-dom'
import { useUser } from 'providers/UserProvider'
import Login from 'login/Login'
import LoginUnauthed from './LoginUnauthed'

/* Inspired by https://www.robinwieruch.de/react-router-private-routes/ */
export const ProtectedRoute = ({ children }: { children?: ReactNode }) => {
  const { user } = useUser()

  const loginComponent = process.env.REACT_APP_UNAUTHED_LOGIN ? <LoginUnauthed/> : <Login/>
  console.log(process.env)

  if (user.isAnonymous) {
    return loginComponent
  }

  return children ? <>{children}</> : <Outlet/>
}
