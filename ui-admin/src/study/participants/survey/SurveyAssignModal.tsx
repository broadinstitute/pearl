import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api, { Survey } from 'api/api'
import { doApiLoad } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { failureNotification, successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { Enrollee, StudyEnvParams } from '@juniper/ui-core'

/** Renders a modal for an admin to submit a sample collection kit request. */
export default function SurveyAssignModal({
  studyEnvParams, enrollee, survey,
  onDismiss, onSubmit
}: {
  studyEnvParams: StudyEnvParams,
  onDismiss: () => void,
  survey: Survey,
  enrollee: Enrollee,
  onSubmit: () => void }) {
  const [isLoading, setIsLoading] = useState(false)
  const [overrideEligibility, setOverrideEligibility] = useState(false)
  const handleSubmit = async () => {
    doApiLoad(async () => {
      const tasks = await Api.assignParticipantTasksToEnrollees(studyEnvParams, {
        taskType: 'SURVEY', // type is just used to determine the dispatcher, not the created task type
        targetStableId: survey.stableId,
        targetAssignedVersion: survey.version,
        enrolleeIds: [enrollee.id],
        assignAllUnassigned: false,
        overrideEligibility
      })
      if (tasks.length > 0) {
        Store.addNotification(successNotification(`Task assigned - ${survey.name}`))
      } else {
        Store.addNotification(failureNotification('Task not assigned - this may be because the participant ' +
          'is not eligible.'))
      }
      onSubmit()
    }, { setIsLoading })
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Assign task</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div>
          Assign <b>{survey.name}</b> to enrollee {enrollee.shortcode}
        </div>
        <div>
          <label className="form-control border-0">
            <input type="checkbox" name="includeUnconsented" checked={overrideEligibility}
              onChange={e => setOverrideEligibility(e.target.checked)}
              className="me-1"/>
            Assign even if they do not meet eligibility criteria
          </label>
        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className='btn btn-primary' onClick={handleSubmit}>Assign</button>
        <button className='btn btn-secondary' onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}
