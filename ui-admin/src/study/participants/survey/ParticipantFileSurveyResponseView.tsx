import React from 'react'
import {
  ColumnDef,
  getCoreRowModel,
  useReactTable
} from '@tanstack/react-table'
import {
  ParticipantFile,
  SurveyResponse
} from '@juniper/ui-core'
import { basicTableLayout } from 'util/tableUtils'
import { createdAtColumn } from 'util/tableColumnUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDownload } from '@fortawesome/free-solid-svg-icons'

export const ParticipantFileSurveyResponseView = ({ surveyResponse } : { surveyResponse: SurveyResponse}) => {
  const columns: ColumnDef<ParticipantFile>[] = [
    {
      ...createdAtColumn(),
      header: 'Uploaded At'
    },
    {
      header: 'File Name',
      accessorKey: 'fileName'
    },
    {
      header: 'File Type',
      accessorKey: 'fileType'
    },
    {
      header: 'Actions',
      cell: () => {
        return <button className='btn btn-secondary'>
          <FontAwesomeIcon icon={faDownload}/>
        </button>
      }
    }
  ]

  const data: ParticipantFile[] = surveyResponse.participantFiles

  const table = useReactTable({
    columns,
    data,
    getCoreRowModel: getCoreRowModel()
  })

  return <>
    <span className="fs-5 fw-bold">
      Document Uploads
    </span>
    {basicTableLayout(table)}
  </>
}
