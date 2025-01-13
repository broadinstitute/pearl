import { Enrollee, instantToDateString, ParticipantFile, saveBlobAsDownload, Study, useI18n } from '@juniper/ui-core'
import React, { useEffect, useState } from 'react'
import { useActiveUser } from 'providers/ActiveUserProvider'
import Api from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faDownload,
  faFile,
  faFileImage,
  faFileLines,
  faFilePdf
} from '@fortawesome/free-solid-svg-icons'
import { usePortalEnv } from 'providers/PortalProvider'

export default function DocumentLibrary() {
  const { portal, portalEnv } = usePortalEnv()
  const { enrollees, ppUser } = useActiveUser()
  const [participantFiles, setParticipantFiles] = useState<ParticipantFile[]>([])
  const currentStudy = portal.portalStudies.find(pStudy =>
    pStudy.study.studyEnvironments.find(studyEnv =>
      studyEnv.environmentName === portalEnv.environmentName))?.study

  const loadDocuments = async () => {
    if (!currentStudy) { return }
    const enrolleeShortcode = enrollees.find(enrollee => enrollee.profileId === ppUser?.profileId)!.shortcode
    const documents = await Api.listParticipantFiles(currentStudy.shortcode, enrolleeShortcode)
    setParticipantFiles(documents)
  }

  useEffect(() => {
    loadDocuments()
  }, [])

  return <div
    className="hub-dashboard-background flex-grow-1 pb-2"
    style={{ background: 'var(--dashboard-background-color)' }}>
    <div className="row mx-0 justify-content-center">
      <div className="my-md-4 mx-auto px-0" style={{ maxWidth: 768 }}>
        <div className="card-body">
          <div className="align-items-center">
            <DocumentsList
              currentStudy={currentStudy!}
              enrollee={enrollees.find(enrollee => enrollee.profileId === ppUser?.profileId)!}
              participantFiles={participantFiles}/>
          </div>
        </div>
      </div>
    </div>
  </div>
}

const DocumentsList = ({ currentStudy, enrollee, participantFiles }: {
  currentStudy: Study, enrollee: Enrollee, participantFiles: ParticipantFile[]
}) => {
  const { i18n } = useI18n()

  return <div className="mb-3 rounded round-3 py-4 bg-white px-md-5 shadow-sm px-2">
    <h1 className="pb-3">
      {i18n('documentsPageTitle')}
    </h1>
    <div className="pb-4">
      {i18n('documentsPageMessage')}
    </div>
    <h3>{i18n('documentsPageUploadedDocumentsTitle')} ({participantFiles.length})</h3>
    <div className="d-flex flex-column">
      { participantFiles.length > 0 && <table className="table">
        <thead>
          <tr>
            <th></th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {participantFiles.map((participantFile, index) => (
            <tr key={index}>
              <td>
                <div>
                  {fileTypeToIcon(participantFile.fileType)}
                  {participantFile.fileName}
                  <div className={'text-muted fst-italic'}>
                    {`${i18n('documentsListCreatedOn')} ${  instantToDateString(participantFile.createdAt)}`}
                  </div>
                </div>
              </td>
              <td className="align-middle">
                <div className={'d-flex justify-content-end'}>
                  <button className="btn btn-outline-primary" onClick={async () => {
                    const response = await Api.downloadParticipantFile(
                      currentStudy.shortcode, enrollee.shortcode, participantFile.fileName)
                    saveBlobAsDownload(await response.blob(), participantFile.fileName)
                  }}>
                    <span className="d-flex align-items-center">
                      <FontAwesomeIcon className="pe-1" icon={faDownload}/>{i18n('documentDownloadButton')}
                    </span>
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>}
      {participantFiles.length === 0 &&
          <div className="text-muted fst-italic my-3">{i18n('documentsListNone')}</div>
      }
    </div>
  </div>
}

const fileTypeToIcon = (fileType: string) => {
  if (fileType.startsWith('image/')) {
    return <FontAwesomeIcon className="me-2" icon={faFileImage}/>
  }
  switch (fileType) {
    case 'text/plain':
      return <FontAwesomeIcon className="me-2" icon={faFileLines}/>
    case 'application/pdf':
      return <FontAwesomeIcon className="me-2" icon={faFilePdf}/>
    default:
      return <FontAwesomeIcon className="me-2" icon={faFile}/>
  }
}
