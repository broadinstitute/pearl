import React, { useState } from 'react'
import { instantToDefaultString } from '@juniper/ui-core'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { useLoadingEffect } from '../../api/api-utils'
import Api, { Notification } from '../../api/api'
import { basicTableLayout } from '../../util/tableUtils'
import LoadingSpinner from 'util/LoadingSpinner'
import { NavLink, useParams } from 'react-router-dom'

/** loads the list of notifications for a given trigger config */
export default function TriggerNotifications({ studyEnvContext }:
                                           { studyEnvContext: StudyEnvContextT }) {
  const { currentEnv, study, portal } = studyEnvContext
  const [tableData, setTableData] = useState<Notification[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])

  const configId = useParams().configId as string

  const config = currentEnv.triggers.find(config => config.id === configId)

  const columns: ColumnDef<Notification>[] = [
    {
      header: 'Enrollee',
      id: 'enrollee',
      cell: ({ row }) => <>
        {row.original.enrollee && <NavLink
          to={`../../participants/${row.original.enrollee?.shortcode}`}>
          {row.original.enrollee?.shortcode}
        </NavLink>}
      </>
    },
    {
      header: 'Sent To',
      accessorKey: 'sentTo'
    },
    {
      header: 'delivery type',
      accessorKey: 'deliveryType'
    },
    {
      header: 'delivery status',
      accessorKey: 'deliveryStatus'
    },
    {
      header: 'time',
      accessorKey: 'createdAt',
      cell: info => instantToDefaultString(info.getValue() as number)
    }
  ]


  const { isLoading } = useLoadingEffect(async () => {
    const notifications = await Api.fetchTriggerNotifications(
      portal.shortcode,
      study.shortcode,
      currentEnv.environmentName,
      configId)
    setTableData(notifications)
  }, [configId])


  const table = useReactTable({
    data: tableData,
    columns,
    state: {
      sorting
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  if (!config) {
    return <div>Trigger config not found</div>
  }

  return <div className={'w-100'}>
    <div className='float-end'>
      <NavLink to={`../configs/${configId}`}>Go Back</NavLink>
    </div>
    <h5>Notifications</h5>
    {/* eslint-disable-next-line react/jsx-no-undef */}
    <LoadingSpinner isLoading={isLoading}>
      {basicTableLayout(table)}
    </LoadingSpinner>
  </div>
}

