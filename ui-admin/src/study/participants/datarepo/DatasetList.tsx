import React, { useEffect, useState } from 'react'
import { datasetDashboardPath, StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api, { DatasetDetails } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { instantToDefaultString } from '../../../util/timeUtils'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout } from '../../../util/tableUtils'
import { Link } from 'react-router-dom'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import { useUser } from '../../../user/UserProvider'
import CreateDatasetModal from './CreateDatasetModal'

const datasetColumns = (currentEnvPath: string): ColumnDef<DatasetDetails>[] => [{
  id: 'datasetName',
  header: 'Dataset Name',
  accessorKey: 'datasetName',
  cell: info => {
    return info.row.original.status !== 'DELETING' ?
      <Link to={datasetDashboardPath(info.row.original.datasetName, currentEnvPath)} className="mx-2">
        {info.getValue() as unknown as string}
      </Link> : <span className="mx-2">{info.row.original.datasetName}</span>
  }
}, {
  id: 'description',
  header: 'Description',
  accessorKey: 'description',
  cell: info => info.getValue() ? info.getValue() : <span className="fst-italic">N/A</span>
}, {
  id: 'created',
  header: 'Date Created',
  accessorKey: 'createdAt',
  cell: info => instantToDefaultString(info.row.original.createdAt)
}, {
  id: 'status',
  header: 'Status',
  accessorKey: 'status'
}]

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
const DatasetList = ({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) => {
  const { currentEnvPath } = studyEnvContext
  const [showCreateDatasetModal, setShowCreateDatasetModal] = useState(false)
  const [datasets, setDatasets] = useState<DatasetDetails[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [datasetsSorting, setDatasetsSorting] = React.useState<SortingState>([])
  const { user } = useUser()

  const datasetTable = useReactTable({
    data: datasets,
    columns: datasetColumns(currentEnvPath),
    state: {
      sorting: datasetsSorting
    },
    onSortingChange: setDatasetsSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  const loadData = async () => {
    //Fetch datasets
    await Api.listDatasetsForStudyEnvironment(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName).then(result => {
      setDatasets(result)
    }).catch(e =>
      Store.addNotification(failureNotification(`Error loading datasets: ${e.message}`))
    )
    setIsLoading(false)
  }

  useEffect(() => {
    loadData()
  }, [studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName])
  return <div className="container-fluid pt-2">
    <div className="row ps-3">
      <div className="col-12 align-items-baseline d-flex mb-2">
        <h2 className="text-center me-4 fw-bold">Terra Data Repo</h2>
      </div>
      <h3>Datasets</h3>
      {/*{ user.superuser &&*/}
      {/*    <button className="btn btn-secondary" onClick={() => setShowCreateDatasetModal(!showCreateDatasetModal)}*/}
      {/*      aria-label="show or hide export modal">*/}
      {/*      <FontAwesomeIcon icon={faPlus}/> Create new dataset*/}
      {/*    </button>*/}
      {/*}*/}
      <CreateDatasetModal studyEnvContext={studyEnvContext}
        show={showCreateDatasetModal}
        setShow={setShowCreateDatasetModal}
        loadDatasets={loadData}/>
      <div className="col-12">
        <LoadingSpinner isLoading={isLoading}>
          {basicTableLayout(datasetTable)}
          { datasets.length === 0 &&
            <span className="d-flex justify-content-center text-muted fst-italic">No datasets</span> }
        </LoadingSpinner>
      </div>
    </div>
  </div>
}

export default DatasetList
