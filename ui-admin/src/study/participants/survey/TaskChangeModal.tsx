import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api, { ParticipantTask } from 'api/api'
import { doApiLoad } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { Enrollee, ParticipantTaskStatus, StudyEnvParams } from '@juniper/ui-core'
import Select from 'react-select'
import { useNonNullReactSingleSelect } from '../../../util/react-select-utils'


const statusOpts: {label: string, value: ParticipantTaskStatus}[] = [
  { label: 'New', value: 'NEW' },
  { label: 'In Progress', value: 'IN_PROGRESS' },
  { label: 'Complete', value: 'COMPLETE' },
  { label: 'Removed', value: 'REMOVED' }
]

/** Renders a modal for an admin to submit a sample collection kit request. */
export default function TaskChangeModal({
  studyEnvParams, enrollee, task,
  onDismiss, onSubmit
}: {
  studyEnvParams: StudyEnvParams,
  onDismiss: () => void,
  task: ParticipantTask,
  enrollee: Enrollee,
  onSubmit: () => void }) {
  const [isLoading, setIsLoading] = useState(false)
  const [updatedStatus, setUpdatedStatus] = useState(task.status)
  const handleSubmit = async () => {
    doApiLoad(async () => {
      await Api.updateTask(studyEnvParams, {
        ...task,
        status: updatedStatus
      })
      Store.addNotification(successNotification(`Task updated`))
      onSubmit()
    }, { setIsLoading })
  }

  const { onChange, options, selectedOption, selectInputId } = useNonNullReactSingleSelect(
    statusOpts.map(opt => opt.value),
    val => statusOpts.find(opt => opt.value === val)!,
    setUpdatedStatus, updatedStatus)

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Update task</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <label htmlFor={selectInputId}>Status</label>
        <Select options={options} value={selectedOption} inputId={selectInputId}
          styles={{ control: baseStyles => ({ ...baseStyles }) }}
          onChange={onChange}/>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className='btn btn-primary' onClick={handleSubmit}>Update</button>
        <button className='btn btn-secondary' onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}


type TaskUpdateSpec = {
  targetStableId: string
  updateToVersion: number
  updateFromVersion?: number // if absent, any other versions will be updated
  newStatus?: string // if specified, will change the status -- if, e.g. you want to make the updated tasks incomplete
}

type ParticipantTaskUpdateDto = {
  updates: TaskUpdateSpec[]
  portalParticipantUserIds?: string[]
  updateAll: boolean // if true, the portalParticipantUserIds list will be ignored and all participants will be updated
}


