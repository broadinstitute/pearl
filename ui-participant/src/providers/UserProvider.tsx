import React, { useContext, useEffect, useState } from 'react'
import LoadingSpinner from '../util/LoadingSpinner'
import Api, { Enrollee, LoginResult, ParticipantUser } from 'api/api'

export type User = ParticipantUser & {
  isAnonymous: boolean
}

const anonymousUser: User = {
  token: '',
  isAnonymous: true,
  username: 'anonymous'
}

export type UserContextT = {
  user: User,
  enrollees: Enrollee[],  // this data is included to speed initial hub rendering.  it is NOT kept current
  loginUser: (result: LoginResult, accessToken: string) => void,
  loginUserInternal: (result: LoginResult) => void,
  logoutUser: () => void,
  updateEnrollee: (enrollee: Enrollee) => void
}

/** current user object context */
const UserContext = React.createContext<UserContextT>({
  user: anonymousUser,
  enrollees: [],
  loginUser: () => {
    throw new Error('context not yet initialized')
  },
  loginUserInternal: () => {
    throw new Error('context not yet initialized')
  },
  logoutUser: () => {
    throw new Error('context not yet initialized')
  },
  updateEnrollee: () => {
    throw new Error('context not yet initialized')
  }
})
const INTERNAL_LOGIN_TOKEN_KEY = 'internalLoginToken'
const OAUTH_ACCRESS_TOKEN_KEY = 'oauthAccessToken'

export const useUser = () => useContext(UserContext)

/** Provider for the current logged-in user. */
export default function UserProvider({ children }: { children: React.ReactNode }) {
  const [loginState, setLoginState] = useState<LoginResult | null>(null)
  const [isLoading, setIsLoadingX] = useState(true)

  const setIsLoading = (load: boolean) => {
    console.log('YOYOYOYOYO')
    setIsLoadingX((load))
  }

  /**
   * Sign in to the UI based on the result of signing in to the API.
   * Internal and OAuth sign-in are a little different. With OAuth sign-in, we get the token from B2C and use that for
   * an API call, so the API token must already be set. However, with internal sign-in, we get the token back from the
   * unauthedLogin API call. Therefore, unless unauthedLogin has a post-condition that the API token will be set, we
   * need to set it now.
   */
  const loginUser = (loginResult: LoginResult, accessToken: string) => {
    setLoginState(loginResult)
    localStorage.setItem(OAUTH_ACCRESS_TOKEN_KEY, accessToken)
  }

  const loginUserInternal = (loginResult: LoginResult) => {
    setLoginState(loginResult)
    localStorage.setItem(INTERNAL_LOGIN_TOKEN_KEY, loginResult.user.token)
  }

  /** Sign out of the UI. Does not invalidate any tokens, but maybe it should... */
  const logoutUser = () => {
    setLoginState(null)
    Api.logout()
    localStorage.removeItem(INTERNAL_LOGIN_TOKEN_KEY)
    localStorage.removeItem(OAUTH_ACCRESS_TOKEN_KEY)
  }

  /** updates a single enrollee in the list of enrollees -- the enrollee object should contain an updated task list */
  function updateEnrollee(enrollee: Enrollee) {
    setLoginState(oldState => {
      if (oldState == null) {
        return oldState
      }
      const updatedEnrollees = oldState.enrollees.filter(exEnrollee => exEnrollee.shortcode != enrollee.shortcode)
      updatedEnrollees.push(enrollee)
      return {
        user: oldState?.user,
        enrollees: updatedEnrollees
      }
    })
  }

  const userContext: UserContextT = {
    user: loginState ? { ...loginState.user, isAnonymous: false } : anonymousUser,
    enrollees: loginState ? loginState.enrollees : [],
    loginUser,
    loginUserInternal,
    logoutUser,
    updateEnrollee
  }

  useEffect(() => {
    // Recover state for a signed-in user (internal) that we might have lost due to a full page load
    const oauthAccessToken = localStorage.getItem(OAUTH_ACCRESS_TOKEN_KEY)
    const internalLogintoken = localStorage.getItem(INTERNAL_LOGIN_TOKEN_KEY)
    if (oauthAccessToken) {
      Api.refreshLogin(oauthAccessToken).then(loginResult => {
        loginUser(loginResult, loginResult.user.token)
        setIsLoading(false)
      }).catch(() => {
        setIsLoading(false)
        localStorage.removeItem(OAUTH_ACCRESS_TOKEN_KEY)
      })
    } else if (internalLogintoken) {
      Api.unauthedRefreshLogin(internalLogintoken).then(loginResult => {
        loginUser(loginResult, loginResult.user.token)
        setIsLoading(false)
      }).catch(() => {
        setIsLoading(false)
        localStorage.removeItem(INTERNAL_LOGIN_TOKEN_KEY)
      })
    } else {
      setIsLoading(false)
    }
  }, [])

  return (
    <UserContext.Provider value={userContext}>
      <LoadingSpinner isLoading={isLoading}>
        {children}
      </LoadingSpinner>
    </UserContext.Provider>
  )
}
