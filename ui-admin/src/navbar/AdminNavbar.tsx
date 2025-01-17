import React, { useEffect, useState } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { useUser } from 'user/UserProvider'

import { Link } from 'react-router-dom'
import { useNavContext } from './NavContextProvider'
import { faChevronRight, faQuestionCircle, faUserCircle } from '@fortawesome/free-solid-svg-icons'
import ContactSupportInfoModal from '../help/ContactSupportInfoModal'
import { Checkbox } from '../components/forms/Checkbox'

/** note we name this adminNavbar to avoid naming conflicts with bootstrap navbar */
function AdminNavbar() {
  const { breadCrumbs } = useNavContext()
  const { user, logoutUser, toggleSuperuserOverride } = useUser()
  const [showContactModal, setShowContactModal] = useState(false)

  if (!user) {
    return <div></div>
  }
  return <>
    <nav className="Navbar navbar navbar-expand-lg navbar-light">
      <ul className="navbar-nav">
        {breadCrumbs.map((crumb, index) => <li key={index}
          className="ms-2 d-flex align-items-center">
          {crumb} {(index < breadCrumbs.length - 1) &&
            <FontAwesomeIcon icon={faChevronRight} className="fa-xs text-muted"/>}
        </li>)}
      </ul>
      <button className="navbar-toggler mx-2" type="button" data-bs-toggle="collapse"
        data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false"
        aria-label="Toggle navigation">
        <span className="navbar-toggler-icon"></span>
      </button>
      <div className="collapse navbar-collapse z-1" id="navbarSupportedContent">
        <ul className="navbar-nav ms-auto" style={{
          position: 'sticky',
          right: 10
        }}>
          <li className="nav-item dropdown">
            <a className="d-flex nav-link dropdown-toggle align-items-center"
              href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
              <FontAwesomeIcon icon={faQuestionCircle} className="d-none d-lg-inline fa-2x nav-icon" title="help menu"/>
              <span className="d-lg-none ms-2">Help</span>
            </a>
            <div className="dropdown-menu dropdown-menu-end p-3">
              <ul className="list-unstyled">
                <li>
                  <Link className="dropdown-item" to="https://broad-juniper.zendesk.com" target="_blank">
                    Help pages
                  </Link>
                </li>
                <li className="pt-2">
                  <a className="dropdown-item" onClick={() => setShowContactModal(!showContactModal)}>
                    Contact support
                  </a>
                </li>
              </ul>
            </div>
          </li>
          {user && <li className="nav-item dropdown">
            <a className="d-flex nav-link dropdown-toggle align-items-center"
              href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
              <FontAwesomeIcon icon={faUserCircle} className="d-none d-lg-inline fa-2x nav-icon" title="user menu"/>
              <span className="d-lg-none ms-2">{user.username}</span>
            </a>
            <div className="dropdown-menu dropdown-menu-end p-3">
              <h3 className="h6">{user.username}</h3>
              { user.superuser &&
                <Checkbox label={'Enable superuser view'} onClick={e => e.preventDefault()}
                  checked={user.superuser}
                  onChange={() => toggleSuperuserOverride()}/>
              }
              <hr/>
              <ul className="list-unstyled">
                <li>
                  <a className="dropdown-item" onClick={logoutUser}>Logout</a>
                </li>
              </ul>
            </div>
          </li>}
        </ul>
      </div>
    </nav>
    { showContactModal && <ContactSupportInfoModal onHide={() => setShowContactModal(false)}/> }
  </>
}

/**
 * Component for adding a breadcrumb into the navbar when a component is rendered.
 * 'value' is used to determine whether the crumb needs updating
 *
 * The breadcrumb will be removed when the component is.
 * This component does not render anything directly, but is still structured as a component rather than a pure hook
 * so that order rendering will be in-order rather than reversed.  See https://github.com/facebook/react/issues/15281
 * */
export function NavBreadcrumb({ value, children }: {value: string, children: React.ReactNode}) {
  const { setBreadCrumbs } = useNavContext()
  useEffect(() => {
    /** use the setState arg that takes a function to avoid race conditions */
    setBreadCrumbs((oldCrumbs: React.ReactNode[]) => {
      return  [...oldCrumbs, children]
    })
    /** return the function that will remove the breadcrumb */
    return () => {
      setBreadCrumbs((oldCrumbs: React.ReactNode[]) => {
        return oldCrumbs.slice(0, -1)
      })
    }
  }, [value])
  return null
}

export default AdminNavbar
