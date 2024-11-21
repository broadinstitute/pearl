import { NavBreadcrumb } from 'navbar/AdminNavbar'
import React from 'react'
import { IconButton } from 'components/forms/Button'
import { faThumbtack } from '@fortawesome/free-solid-svg-icons'
import Select from 'react-select'
import { usePinnedEnv } from './usePinnedEnv'
import { useNavigate } from 'react-router-dom'
import { ENVIRONMENT_ICON_MAP } from '../util/publishUtils'

const envOpts = ['live', 'irb', 'sandbox'].map(env => ({
  label: <div className={'d-flex align-items-center'}>
    <span className={'d-inline-flex me-2 align-items-center justify-content-center'} style={{  width: '1.5ch' }}>
      {ENVIRONMENT_ICON_MAP[env]}</span>
    {env}
  </div>,
  value: env
}))

interface StudyEnvironmentSwitcherProps {
    currentEnvPath: string
    envName: string
}

export const EnvironmentSwitcher = ({
  currentEnvPath,
  envName
}: StudyEnvironmentSwitcherProps) => {
  const navigate = useNavigate()
  const { pinnedEnv, setPinnedEnv } = usePinnedEnv()

  const changeEnv = (newEnv?: string) => {
    if (!newEnv) {
      return
    }
    const currentPath = window.location.pathname
    const newPath = currentPath
      .replace(`/env/${envName}`, `/env/${newEnv}`)

    // reset the pinned env
    setPinnedEnv(undefined)

    navigate(newPath)
  }

  const handlePinClick = () => {
    if (pinnedEnv) {
      setPinnedEnv(undefined)
    } else {
      setPinnedEnv(envName)
    }
    changeEnv()
  }

  return (
    <NavBreadcrumb value={currentEnvPath + pinnedEnv}>
      <IconButton
        icon={faThumbtack}
        iconClassNames={pinnedEnv ? 'fa-rotate-270' : ''}
        aria-label={
          pinnedEnv ?
            'Unpin this environment' :
            'Pin this environment to keep it selected when navigating to other pages'
        }
        tooltipPlacement={'bottom'} variant={pinnedEnv ? 'primary' : 'light'}
        className="border border-end-0 rounded-end-0"
        onClick={handlePinClick}
      />
      <Select options={envOpts}
        value={envOpts.find(opt => opt.value === envName)}
        className="me-2"
        styles={{
          control: baseStyles => ({
            ...baseStyles,
            minWidth: '9em',
            borderBottomLeftRadius: 0,
            borderTopLeftRadius: 0
          })
        }}
        onChange={opt => changeEnv(opt?.value)}
      />
    </NavBreadcrumb>
  )
}
