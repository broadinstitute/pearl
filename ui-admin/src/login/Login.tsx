import React, { SyntheticEvent, useState } from 'react'
import microsoftLogo from 'images/microsoft_logo.png'
import googleLogo from 'images/googleLogo.png'
import Api, { AdminUser } from 'api/api'
import { useUser } from 'user/UserProvider'
import { useAuth } from 'react-oidc-context'

/** component for showing a login dialog that hides other content on the page */
function Login() {
  const [emailAddress, setEmailAddress] = useState('')
  const [isError, setIsError] = useState(false)
  const { loginUser } = useUser()
  const [showDevLogin, setShowDevLogin] = useState(false)
  const auth = useAuth()

  const signIn = async () => {
    const user = await auth.signinPopup()
    return user
  }

  /** log in with just an email, ignoring auth */
  function unauthedLogin(event: SyntheticEvent) {
    event.preventDefault()
    Api.unauthedLogin(emailAddress).then((adminUser: AdminUser) => {
      loginUser(adminUser)
    }).catch(() => {
      setIsError(true)
    })
  }

  return <div className="Login">
    <div className="App-splash-background"/>
    <div className="Login-overlay h-100 w-100" style={{
      top: 0,
      left: 0,
      position: 'fixed',
      zIndex: 1,
      opacity: 0.4,
      backgroundColor: '#888'
    }}></div>
    <div className="Login-dialog position-absolute top-50 start-50 translate-middle p-4 text-white" style={{
      borderRadius: '10px',
      zIndex: 2,
      minWidth: '300px',
      backgroundColor: '#333F52'
    }}>
      <h1 className="h5 text-center mb-4">Juniper</h1>
      <form onSubmit={unauthedLogin} className="d-flex flex-column justify-content-center">
        <button type="button" className="btn btn-primary border-white text-white
         fw-bold d-flex align-items-center justify-content-center fs-5 mb-3"
        style={{ backgroundColor: '#4e617e' }}
        onClick={() => signIn()}>
          Login <img className="ms-3" style={{ maxHeight: '1em' }} src={microsoftLogo}/>
          <img className="ms-1" style={{ maxHeight: '1em' }} src={googleLogo}/>
        </button>
        { isError && <div className="text-danger text-center">Login failed</div> }
        <hr className="mt-2"/>
        <button type="button" className="btn btn-secondary text-white" onClick={() => setShowDevLogin(!showDevLogin)}>
          developer login
        </button>
        { showDevLogin && <div className="mb-3">
          <input type="email" className="form-control" id="inputLoginEmail" aria-describedby="emailHelp"
            value={emailAddress}
            onChange={event => setEmailAddress(event.target.value)}/>

          <button type="submit" className="btn btn-secondary-outline border-white text-white mt-2 w-100">Login</button>
        </div> }

      </form>
    </div>
  </div>
}

export default Login
