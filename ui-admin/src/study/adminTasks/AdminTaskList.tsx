import React, { useMemo, useState } from 'react'
import Api, { AdminTask, AdminTaskListDto } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import { instantToDateString, instantToDefaultString } from 'util/timeUtils'
import { paramsFromContext, StudyEnvContextT, StudyEnvParams } from '../StudyEnvironmentRouter'
import { useAdminUserContext } from 'providers/AdminUserProvider'
import _truncate from 'lodash/truncate'
import { studyEnvParticipantPath } from '../participants/ParticipantsRouter'
import { Link } from 'react-router-dom'
import { useLoadingEffect } from 'api/api-utils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck, faEdit } from '@fortawesome/free-solid-svg-icons'
import { useUser } from 'user/UserProvider'
import { IconButton } from '../../components/forms/Button'
import { AdminTaskEditModal } from './AdminTaskEditor'


/** show the lists of the user's tasks and all tasks */
export default function AdminTaskList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const [taskData, setTaskData] = useState<AdminTaskListDto>({
    tasks: [], enrollees: [], participantNotes: []
  })
  const [sorting, setSorting] = useState<SortingState>([
    { id: 'status', desc: true },
    { id: 'createdAt', desc: true }
  ])
  const { users } = useAdminUserContext()
  const [selectedTask, setSelectedTask] = useState<AdminTask>()
  const [showEditModal, setShowEditModal] = useState(false)


  const columns: ColumnDef<AdminTask>[] = [{
    header: 'Task',
    id: 'taskDescription',
    cell: info => taskDescription(info.row.original, paramsFromContext(studyEnvContext), taskData)
  }, {
    header: 'Status',
    accessorKey: 'status',
    cell: info => renderStatusColumn(info.row.original)
  }, {
    header: 'Created',
    accessorKey: 'createdAt',
    cell: info => instantToDefaultString(info.getValue() as number)
  }, {
    header: 'Assignee',
    accessorKey: 'assignedAdminUserId',
    cell: info => users.find(user => user.id === info.getValue())?.username
  }, {
    header: '',
    id: 'actions',
    cell: info => <IconButton icon={faEdit} onClick={() => editTask(info.row.original)} aria-label="edit"/>
  }]

  const allTasksTable = useReactTable({
    data: taskData.tasks,
    columns,
    state: { sorting },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  const { isLoading, reload } = useLoadingEffect(async () => {
    const result = await Api.fetchAdminTasksByStudyEnv(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      ['enrollee', 'participantNote'])

    setTaskData(result)
  }, [studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
    studyEnvContext.currentEnv.environmentName])

  const editTask = (task: AdminTask) => {
    setSelectedTask(task)
    setShowEditModal(true)
  }
  const onDoneEditing = (updatedTask?: AdminTask) => {
    setSelectedTask(undefined)
    setShowEditModal(false)
    if (updatedTask) {
      // for now, just reload the whole list if a task is updated
      reload()
    }
  }

  return <div className="container-fluid pt-2">
    <div className="row ps-3">
      <div className="col-12 align-items-baseline d-flex mb-2">
        <h2 className="text-center me-4 fw-bold">Tasks</h2>
      </div>
      <div className="col-12">
        <LoadingSpinner isLoading={isLoading}>
          <MyTaskList studyEnvContext={studyEnvContext} taskData={taskData}/>
          <h4 className="mt-5">All tasks</h4>
          {basicTableLayout(allTasksTable)}
          { !taskData.tasks.length &&
            <span className="d-flex justify-content-center text-muted fst-italic">No tasks</span> }
        </LoadingSpinner>
      </div>
      { (showEditModal && selectedTask) && <AdminTaskEditModal task={selectedTask} users={users}
        onDismiss={onDoneEditing} studyEnvContext={studyEnvContext}/> }
    </div>
  </div>
}

const MyTaskList = ({ studyEnvContext, taskData }: {studyEnvContext: StudyEnvContextT,
taskData: AdminTaskListDto}) => {
  const { user } = useUser()
  const [sorting, setSorting] = useState<SortingState>([
    { id: 'status', desc: true },
    { id: 'createdAt', desc: true }
  ])
  const myTasks = useMemo(() => taskData.tasks.filter(task =>
    task.assignedAdminUserId === user.id), [taskData.tasks.length])

  const columns: ColumnDef<AdminTask>[] = [{
    header: 'Task',
    id: 'taskDescription',
    cell: info => taskDescription(info.row.original, paramsFromContext(studyEnvContext), taskData)
  }, {
    header: 'Status',
    accessorKey: 'status',
    cell: info => renderStatusColumn(info.row.original)
  }, {
    header: 'Created',
    accessorKey: 'createdAta',
    cell: info => instantToDefaultString(info.getValue() as number)
  }]

  const table = useReactTable({
    data: myTasks,
    state: { sorting },
    columns,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })
  return <>
    <h4>My tasks</h4>
    { basicTableLayout(table) }
    { !myTasks.length &&
      <span className="d-flex justify-content-center text-muted fst-italic">No tasks</span> }
  </>
}


const taskDescription = (task: AdminTask, studyEnvParams: StudyEnvParams, taskData: AdminTaskListDto) => {
  const matchedNote = task.participantNoteId ?
    taskData.participantNotes.find(note => note.id === task.participantNoteId) : undefined
  const matchedEnrollee = task.enrolleeId ?
    taskData.enrollees.find(enrollee => enrollee.id === task.enrolleeId) : undefined
  return <>
    {matchedNote && <span>
      {_truncate(matchedNote.text, { length: 50 })}<br/>
      <span className="text-muted">Participant&nbsp;
        <Link to={studyEnvParticipantPath(studyEnvParams, matchedEnrollee?.shortcode as string)}>
          {matchedEnrollee?.shortcode}
        </Link>
      </span>
    </span> }
  </>
}

const renderStatusColumn = (task: AdminTask) => {
  if (task.status === 'COMPLETE') {
    return <div>
      <div><FontAwesomeIcon icon={faCheck}/> {instantToDateString(task.completedAt)} </div>
      <span>{task.dispositionNote}</span>
    </div>
  } else {
    return <span>{task.status}</span>
  }
}
