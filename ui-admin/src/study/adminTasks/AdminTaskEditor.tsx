import React, { useId, useState } from 'react'
import Api from 'api/api'
import { AdminUser } from 'api/adminUser'
import { Modal } from 'react-bootstrap'
import Select from 'react-select'
import LoadingSpinner from 'util/LoadingSpinner'
import AdminUserSelect from 'user/AdminUserSelect'
import { doApiLoad } from 'api/api-utils'
import { instantToDefaultString, ParticipantTask, ParticipantTaskStatus, StudyEnvParams } from '@juniper/ui-core'


function AdminTaskEditor({ task, workingTask, setWorkingTask, users }: {
  task: ParticipantTask, workingTask: ParticipantTask,
  setWorkingTask: (task: ParticipantTask) => void, users: AdminUser[]}
) {
  const selectedUser = users.find(user => user.id === workingTask.assignedAdminUserId)
  const statusSelectId = useId()
  const userSelectId = useId()
  const setSelectedUser = ((user: AdminUser | undefined) => {
    setWorkingTask({
      ...workingTask,
      assignedAdminUserId: user?.id
    })
  })
  const statusOpts: {label: string, value: ParticipantTaskStatus}[] = [
    { label: 'New', value: 'NEW' },
    { label: 'In Progress', value: 'IN_PROGRESS' },
    { label: 'Complete', value: 'COMPLETE' }
  ]
  const statusValue = statusOpts.find(opt => opt.value === workingTask.status)
  return <div>
    <label className="mt-3" htmlFor={userSelectId}>Assigned to</label>
    <AdminUserSelect selectedUser={selectedUser} setSelectedUser={setSelectedUser} id={userSelectId}
      users={users} readOnly={task.status === 'COMPLETE'}/>

    { task.status !== 'COMPLETE' && <div className="mt-3">
      <label htmlFor={statusSelectId}>Status</label>
      <Select options={statusOpts} value={statusValue} inputId={statusSelectId}
        styles={{ control: baseStyles => ({ ...baseStyles }) }}
        onChange={opt => setWorkingTask({
          ...workingTask, status: opt?.value ?? 'NEW'
        })}/>
    </div> }
    { task.status === 'COMPLETE' && <div className="mt-3">
      Completed {instantToDefaultString(task.completedAt)}
    </div> }
  </div>
}

/**
 * shows a modal for editing the passed-in task.  this handles saving the task to the server.
 * If the task was saved, the updated task will be passed to the onDismiss handler
 */
export const AdminTaskEditModal = ({ task, users, onDismiss, studyEnvParams }: {
  task: ParticipantTask, users: AdminUser[], onDismiss: (task?: ParticipantTask) => void,
  studyEnvParams: StudyEnvParams
}) => {
  const [workingTask, setWorkingTask] = useState<ParticipantTask>(task)
  const [isLoading, setIsLoading] = useState(false)

  const saveTask = () => {
    doApiLoad(async () => {
      const updatedTask = await Api.updateTask(studyEnvParams,
        { task: workingTask, justification: 'admin update' })
      onDismiss(updatedTask)
    }, { setIsLoading })
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Update admin task</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <AdminTaskEditor task={task} workingTask={workingTask} setWorkingTask={setWorkingTask} users={users}/>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={saveTask}>Save</button>
        <button className="btn btn-secondary" onClick={() => onDismiss()}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}
