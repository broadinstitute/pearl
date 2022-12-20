import React, { useContext } from 'react'
import { Study } from 'api/api'
import EnvironmentSelector from './EnvironmentSelector'
import { Link, NavLink } from 'react-router-dom'
import { StudyEnvironment } from 'api/api'

/** Sidebar for navigating around configuration of a study environment */
function StudyEnvironmentSidebar({ study, currentEnv, currentEnvPath, setShow }: {study: Study,
  currentEnv: StudyEnvironment | undefined, currentEnvPath: string, setShow: (show: boolean) => void}) {
  function getLinkPath(path: string): string {
    if (!currentEnvPath) {
      return '#'
    }
    return `${currentEnvPath}/${path}`
  }

  function getLinkStyle({ isActive }: {isActive: boolean}) {
    return `nav-link ${isActive ? 'active' : ''}`
  }


  return <div className="StudySidebar d-flex flex-column flex-shrink-0 p-3 text-white">
    <h5>
      <Link className="nav-link" to={`/${study.shortcode}`}>{study.name}</Link>
    </h5>
    <hr/>
    <ul className="nav nav-pills flex-column mb-auto">
      <li>
        <label className="form-label">Environment</label>
        <EnvironmentSelector study={study} currentEnv={currentEnv}/>
      </li>
      <li>
        <hr/>
      </li>
      <li>
        <NavLink to={getLinkPath('participants')} className={getLinkStyle} onClick={() => setShow(false)}>
          Participants
        </NavLink>
      </li>
      <li>
        <NavLink to={getLinkPath('content')} className={getLinkStyle} onClick={() => setShow(false)}>
          Content
        </NavLink>
      </li>
      <li>
        <NavLink to={getLinkPath('users')} className={getLinkStyle} onClick={() => setShow(false)}>
          Users
        </NavLink>
      </li>
      <li>
        <NavLink to={getLinkPath('theme')} className={getLinkStyle} onClick={() => setShow(false)}>
          Theme
        </NavLink>
      </li>
      <li>
        <NavLink to={getLinkPath('advanced')} className={getLinkStyle} onClick={() => setShow(false)}>
          Advanced Options
        </NavLink>
      </li>
    </ul>
  </div>
}


export default StudyEnvironmentSidebar
