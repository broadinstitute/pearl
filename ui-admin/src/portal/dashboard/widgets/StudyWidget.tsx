import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faGear, faPlus, faTrash } from '@fortawesome/free-solid-svg-icons'
import CreateNewStudyModal from 'study/CreateNewStudyModal'
import { Link } from 'react-router-dom'
import { studyParticipantsPath } from 'portal/PortalRouter'
import { getMediaUrl } from 'api/api'
import { Portal, Study } from '@juniper/ui-core'
import React, { useState } from 'react'
import { useUser } from 'user/UserProvider'
import { DropdownButton } from 'study/participants/survey/SurveyResponseView'
import DeleteStudyModal from 'study/adminTasks/DeleteStudyModal'
import { useNavContext } from 'navbar/NavContextProvider'
import { InfoCard, InfoCardBody, InfoCardHeader } from 'components/InfoCard'

export const StudyWidget = ({ portal }: { portal: Portal }) => {
  const [showNewStudyModal, setShowNewStudyModal] = useState(false)
  const { user } = useUser()

  const primaryStudy = portal.portalEnvironments.find(env =>
    env.environmentName === 'live')?.portalEnvironmentConfig.primaryStudy

  const sortedStudies = portal.portalStudies.sort((a, b) => {
    if (a.study.shortcode === primaryStudy) { return -1 }
    if (b.study.shortcode === primaryStudy) { return 1 }
    return a.study.name.localeCompare(b.study.name)
  })

  return (
    <InfoCard classNames={''}>
      <InfoCardHeader>
        <div className="d-flex align-items-center justify-content-between w-100">
          <span className="fw-bold">Studies</span>
          <Button onClick={() => setShowNewStudyModal(true)}
            disabled={!user?.superuser}
            tooltip={user?.superuser ? 'Create a new study' : 'You do not have permission to create a new study'}
            variant="light" className="border">
            <FontAwesomeIcon icon={faPlus} className="fa-lg"/> Create
          </Button>
        </div>
        {showNewStudyModal && <CreateNewStudyModal portal={portal} onDismiss={() => setShowNewStudyModal(false)}/>}
      </InfoCardHeader>
      <InfoCardBody>
        <ul className="list-group list-group-flush w-100">
          {sortedStudies.map((portalStudy, i) => {
            const study = portalStudy.study
            return <StudyControls
              key={i}
              portal={portal}
              study={study}
              primaryStudy={primaryStudy}
            />
          })}
        </ul>
      </InfoCardBody>
    </InfoCard>
  )
}

const StudyControls = ({ portal, study, primaryStudy }: {
  portal: Portal,
  study: Study,
  primaryStudy?: string,
}) => {
  const { reload } = useNavContext()
  const [showDeleteStudyModal, setShowDeleteStudyModal] = useState(false)

  return (
    <li key={`${portal.shortcode}-${study.shortcode}`}
      className="list-group-item my-1 border border-secondary-subtle rounded py-1">
      <div className="d-flex justify-content-between align-items-center">
        <div className="py-2">
          <Link to={studyParticipantsPath(portal.shortcode, study.shortcode, 'live')}>
            <img
              src={getMediaUrl(portal.shortcode, 'favicon.ico', 'latest')}
              className="me-2" style={{ maxHeight: '1.5em' }} alt={study.name}/>
            {study.name}
          </Link>
          {study.shortcode === primaryStudy &&
            <span className="badge bg-secondary ms-2">Primary Study</span>}
        </div>
        <div className="dropdown">
          <Button
            data-bs-toggle='dropdown'
            className="dropdown-toggle border m-1"
            type="button"
            id={`editStudyMenu-${study.shortcode}`}
            variant="light"
            aria-haspopup="true"
            aria-expanded="false"
            aria-label={'Customize website'}
          >
            <FontAwesomeIcon icon={faGear} className="fa-lg"/>
          </Button>
          <div className="dropdown-menu" aria-labelledby={`editStudyMenu-${study.shortcode}`}>
            <DropdownButton
              onClick={() => setShowDeleteStudyModal(true)}
              className="text-danger"
              icon={faTrash}
              label="Delete study"
            />
            {showDeleteStudyModal &&
              <DeleteStudyModal
                study={study}
                portal={portal}
                reload={reload}
                onDismiss={() => setShowDeleteStudyModal(false)}/>
            }
          </div>
        </div>
      </div>
    </li>
  )
}
