import { Enrollee, instantToDateString, ParticipantFile, saveBlobAsDownload, useI18n } from '@juniper/ui-core'
import React, { useEffect, useState } from 'react'
import { useActiveUser } from 'providers/ActiveUserProvider'
import Api from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faFile,
  faFileImage,
  faFileLines,
  faFilePdf
} from '@fortawesome/free-solid-svg-icons'
import Modal from 'react-bootstrap/Modal'
import ThemedModal from 'components/ThemedModal'

export default function DocumentLibrary() {
  const { enrollees, ppUser } = useActiveUser()
  const [participantFiles, setParticipantFiles] = useState<ParticipantFile[]>([])

  const loadDocuments = async () => {
    const enrolleShortcode = enrollees.find(enrollee => enrollee.profileId === ppUser?.profileId)!.shortcode
    const foo = await Api.listParticipantFiles('demo', enrolleShortcode)
    setParticipantFiles(foo)
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
            <DocumentsList enrollee={enrollees.find(enrollee => enrollee.profileId === ppUser?.profileId)!}
              participantFiles={participantFiles}/>
          </div>
        </div>
      </div>
    </div>
  </div>
}

const DocumentsList = ({ enrollee, participantFiles }: { enrollee: Enrollee, participantFiles: ParticipantFile[] }) => {
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
            <th>{i18n('documentsListFileName')}</th>
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
              <td>
                <FileOptionsDropdown participantFile={participantFile} enrollee={enrollee}/>
              </td>
            </tr>
          ))}
        </tbody>
      </table> }
      {participantFiles.length === 0 &&
        <div className="text-muted fst-italic my-3">{i18n('documentsNone')}</div>
      }
    </div>
  </div>
}

const FileOptionsDropdown = ({ participantFile, enrollee }: {
  participantFile: ParticipantFile, enrollee: Enrollee
}) => {
  const [showConfirmDelete, setShowConfirmDelete] = useState(false)
  const { i18n } = useI18n()
  return (<>
    <li className="nav-item dropdown d-flex flex-column">
      <button className="btn btn-outline-primary dropdown-toggle" id="fileOptionsDropdown"
        data-bs-toggle="dropdown" aria-expanded="false">
        {i18n('documentOptionsButton')}
      </button>
      <ul className="dropdown-menu" aria-labelledby="fileOptionsDropdown">
        <li>
          <a role={'button'} className="dropdown-item"
            onClick={() => setShowConfirmDelete(true)}
          >
            {i18n('documentDeletionDelete')}
          </a>
        </li>
        <li>
          <a className="dropdown-item" role={'button'} onClick={async () => {
            const response = await Api.downloadParticipantFile(
              'demo', enrollee.shortcode, participantFile.fileName)
            saveBlobAsDownload(await response.blob(), participantFile.fileName)
          }}>
            {i18n('documentDownloadButton')}
          </a>
        </li>
      </ul>
    </li>
    {showConfirmDelete && <ThemedModal show={true}
      onHide={() => setShowConfirmDelete(false)} size={'lg'} animation={true}>
      <Modal.Header>
        <Modal.Title>
          <h2 className="fw-bold pb-0 mb-0">{i18n('documentDeletionConfirmationTitle')}</h2>
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p className="m-0">{i18n('documentDeletionConfirmationMessage')}</p>
      </Modal.Body>
      <Modal.Footer>
        <div className={'d-flex w-100'}>
          <button className={'btn btn-primary m-2'}
            onClick={async () => {
              await Api.deleteParticipantFile('demo', enrollee.shortcode, participantFile.fileName)
            }}>
            {i18n('documentDeletionDelete')}
          </button>
          <button className={'btn btn-outline-secondary m-2'}
            onClick={() => setShowConfirmDelete(false)}>
            {i18n('cancel')}
          </button>
        </div>
      </Modal.Footer>
    </ThemedModal> }
  </>
  )
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
