import React from 'react'
import {
  ColumnDef,
  getCoreRowModel,
  useReactTable
} from '@tanstack/react-table'
import {
  Enrollee,
  ParticipantFile,
  SurveyResponse
} from '@juniper/ui-core'
import { basicTableLayout } from 'util/tableUtils'
import { createdAtColumn } from 'util/tableColumnUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDownload } from '@fortawesome/free-solid-svg-icons'
import Api from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { saveBlobAsDownload } from 'util/downloadUtils'

export const ParticipantFileSurveyResponseView = ({
  studyEnvContext,
  enrollee,
  surveyResponse
}: {
  studyEnvContext: StudyEnvContextT,
  enrollee: Enrollee,
  surveyResponse: SurveyResponse
}) => {
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
      cell: ({ row }) => {
        return <button className='btn btn-secondary' onClick={() => download(row.original)}>
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

  const download = async (file: ParticipantFile) => {
    const response = await Api.downloadParticipantFile(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      enrollee.shortcode,
      file.fileName
    )

    saveBlobAsDownload(await response.blob(), file.fileName)
  }

  return <>
    <span className="fs-5 fw-bold">
      Document Uploads
    </span>
    {basicTableLayout(table)}
  </>
}
