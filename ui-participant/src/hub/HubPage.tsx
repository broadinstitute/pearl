import React, { useEffect, useState } from 'react'
import { usePortalEnv } from 'providers/PortalProvider'

import Api, { Enrollee, Portal, Study } from '../api/api'
import { isTaskActive } from './TaskLink'
import { DocumentTitle } from 'util/DocumentTitle'

import { HubMessageAlert, HubUpdateMessage, useHubUpdate } from './hubUpdates'
import { ParticipantDashboardAlert, useI18n } from '@juniper/ui-core'
import KitBanner from './kit/KitBanner'
import StudyResearchTasks from './StudyResearchTasks'
import OutreachTasks from './OutreachTasks'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { useUser } from 'providers/UserProvider'
import ParticipantSelector from '../participant/ParticipantSelector'
import classNames from 'classnames'
import { NavLink } from 'react-router-dom'
import { getJoinLink } from '../Navbar'


/** renders the logged-in hub page */
export default function HubPage() {
  const { portal, portalEnv } = usePortalEnv()

  const {
    enrollees
  } = useActiveUser()

  const {
    relations
  } = useUser()

  const { i18n } = useI18n()

  const [noActivitiesAlert, setNoActivitiesAlert] = useState<ParticipantDashboardAlert>()

  useEffect(() => {
    loadDashboardAlerts()
  }, [])

  const loadDashboardAlerts = async () => {
    if (!portalEnv) {
      return
    }
    const alerts = await Api.getPortalEnvDashboardAlerts(portal.shortcode, portalEnv.environmentName)
    setNoActivitiesAlert({
      ...{
        title: i18n('hubUpdateNoActivitiesTitle'),
        detail: i18n('hubUpdateNoActivitiesDetail'),
        alertType: 'INFO'
      } as ParticipantDashboardAlert,
      ...alerts.find(msg => msg.trigger === 'NO_ACTIVITIES_REMAIN')
    })
  }

  const hubUpdate = useHubUpdate()
  const [showMessage, setShowMessage] = useState(true)
  const hasActiveTasks = enrollees.some(enrollee => enrollee.participantTasks.some(task => isTaskActive(task)))

  return (
    <>
      <DocumentTitle title={i18n('navbarDashboard')}/>
      <div
        className="hub-dashboard-background flex-grow-1 mb-2"
        style={{ background: 'var(--dashboard-background-color)' }}
      >
        {!hasActiveTasks && noActivitiesAlert && <HubMessageAlert
          message={{
            title: noActivitiesAlert.title,
            detail: noActivitiesAlert.detail,
            type: noActivitiesAlert.alertType
          } as HubUpdateMessage}
          className="mx-1 mx-md-auto my-1 my-md-5 shadow-sm"
          role="alert"
          style={{ maxWidth: 768 }}
        />}
        {!!hubUpdate?.message && showMessage && (
          <HubMessageAlert
            message={hubUpdate.message}
            className="mx-1 mx-md-auto my-1 my-md-5 shadow-sm"
            role="alert"
            style={{ maxWidth: 768 }}
            onDismiss={() => {
              setShowMessage(false)
            }}
          />
        )}


        <div className="my-md-4 mx-auto" style={{ maxWidth: 768 }}>
          <div className="w-100 mt-2 mb-0 d-flex mb-2">
            {relations.length > 0 && <ParticipantSelector/>}
          </div>
          <main
            className="hub-dashboard py-4 px-2 px-md-5 shadow-sm"
            style={{ background: '#fff' }}
          >
            {enrollees.map(enrollee => <StudySection key={enrollee.id} enrollee={enrollee} portal={portal}/>)}
          </main>
        </div>

        <div className="hub-dashboard mx-auto"
          style={{ maxWidth: 768 }}>
          <OutreachTasks enrollees={enrollees} studies={portal.portalStudies.map(pStudy => pStudy.study)}/>
        </div>
      </div>
    </>
  )
}

type StudySectionProps = {
  enrollee: Enrollee,
  portal: Portal,
}

const StudySection = (props: StudySectionProps) => {
  const {
    enrollee,
    portal
  } = props

  const { ppUsers } = useUser()
  const ppUser = ppUsers.find(ppUser => ppUser.profileId === enrollee.profileId)

  const { i18n } = useI18n()

  const matchedStudy = portal.portalStudies
    .find(pStudy => pStudy.study.studyEnvironments[0].id === enrollee.studyEnvironmentId)?.study as Study

  if (!ppUser) {
    return <></>
  }
  return (
    <>
      <h1 className="mb-4">{matchedStudy.name}</h1>
      {enrollee.kitRequests.length > 0 && <KitBanner kitRequests={enrollee.kitRequests}/>}
      {enrollee.subject
        ? <StudyResearchTasks enrollee={enrollee} studyShortcode={matchedStudy.shortcode}
          participantTasks={enrollee.participantTasks}/>
        : <div>
          <NavLink
            className={classNames(
              'btn btn-primary',
              'mb-3 mb-lg-0 ms-lg-3'
            )}
            to={`${getJoinLink(matchedStudy, { ppUserId: ppUser.id })}`}
          >
            {i18n('navbarJoin')}
          </NavLink>
        </div>}
    </>
  )
}
