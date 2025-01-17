import React from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import {
  Enrollee,
  Family
} from '@juniper/ui-core'
import { EnrolleeSearchbar } from 'study/participants/enrolleeView/EnrolleeSearchbar'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faPencil,
  faX
} from '@fortawesome/free-solid-svg-icons'
import JustifyChangesModal from 'study/participants/JustifyChangesModal'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'


/**
 * Simple editor that allows you to change the proband of a family.
 */
export const ProbandEditor = (
  {
    family,
    studyEnvContext,
    reloadFamily
  }: {
    family: Family,
    studyEnvContext: StudyEnvContextT,
    reloadFamily: () => void
  }) => {
  const [proband, setProband] = React.useState<Enrollee>(family.proband!)
  const [editMode, setEditMode] = React.useState<boolean>(false)
  const [openSaveNewProbandModal, setOpenSaveNewProbandModal] = React.useState<boolean>(false)

  const saveProband = async (justification: string) => {
    try {
      await Api.updateProband(studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        family.shortcode,
        proband.shortcode,
        justification)
      reloadFamily()
      setEditMode(false)
      setOpenSaveNewProbandModal(false)
    } catch (e) {
      Store.addNotification(failureNotification('Could not update proband.'))
    }
  }
  return <div>
    <h4>Proband</h4>
    <div className="d-flex">
      <div style={{ width: '250px' }}>
        <EnrolleeSearchbar
          disabled={!editMode}
          selectedEnrollee={proband}
          studyEnvContext={studyEnvContext}
          onEnrolleeSelected={newProband => setProband(newProband || family.proband!)}
          searchExpFilter={`{family.shortcode} = '${family.shortcode}'`}
        />
      </div>


      {!editMode && <button
        className="btn btn-secondary ms-2"
        onClick={() => {
          setEditMode(!editMode)
          if (!editMode) {
            setProband(family.proband!)
          }
        }}>
        <FontAwesomeIcon icon={faPencil} />
      </button>}
      {editMode && <>
        <button
          className="btn btn-primary ms-2"
          onClick={async () => {
            setOpenSaveNewProbandModal(true)
          }}>
            Save
        </button>
        <button
          className="btn btn-secondary"
          onClick={() => {
            setProband(family.proband!)
            setEditMode(false)
          }}>
          <FontAwesomeIcon icon={faX}/>
        </button>
      </>}
      {openSaveNewProbandModal && <JustifyChangesModal
        saveWithJustification={saveProband}
        onDismiss={() => setOpenSaveNewProbandModal(false)}
      />}
    </div>
  </div>
}
