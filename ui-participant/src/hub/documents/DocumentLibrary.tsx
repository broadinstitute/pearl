import { Enrollee, instantToDateString, ParticipantFile, saveBlobAsDownload, useI18n } from '@juniper/ui-core'
import React, { useEffect, useState } from 'react'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { usePortalEnv } from 'providers/PortalProvider'
import Api from '../../api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faFile, faFileImage, faFileLines, faFilePdf } from '@fortawesome/free-solid-svg-icons'
import Modal from 'react-bootstrap/Modal'
import ThemedModal from '../../components/ThemedModal'

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
  const { portal, portalEnv } = usePortalEnv()
  const [showConfirmDelete, setShowConfirmDelete] = useState<string>()
  const { i18n } = useI18n()

  return <div className="mb-3 rounded round-3 py-4 bg-white px-md-5 shadow-sm px-2">
    <h1 className="pb-3">
      Documents
    </h1>
    <div className="pb-4">
      While participating in a study, you may be asked to upload documents as part of a form or survey.
      Below, you can manage the documents you have uploaded.
    </div>
    <h3>Uploaded documents ({participantFiles.length})</h3>
    <div className="d-flex flex-column">
      { participantFiles.length > 0 && <table className="table">
        <thead>
          <tr>
            <th>File Name</th>
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
                    {`Created on ${  instantToDateString(participantFile.createdAt)}`}
                  </div>
                </div>
              </td>
              <td>
                <div className="d-flex align-items-center btn-group-vertical">
                  <button className="btn btn-sm rounded-pill fw-bold btn-danger"
                    onClick={() => setShowConfirmDelete(participantFile.fileName)}>
                    Delete
                  </button>
                  <button className="btn btn-sm rounded-pill fw-bold btn-primary mt-2" onClick={async () => {
                    const response = await Api.downloadParticipantFile(
                      'demo', enrollee.shortcode, participantFile.fileName)
                    saveBlobAsDownload(await response.blob(), participantFile.fileName)
                  }}>
                    Download
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table> }
      {participantFiles.length === 0 &&
        <div className="text-muted fst-italic">You have not uploaded any documents yet</div>
      }
    </div>
    {showConfirmDelete && <ThemedModal show={true}
      onHide={() => console.log('')} size={'lg'} animation={false}> <Modal.Header>
        <Modal.Title>
          <h2 className="fw-bold pb-0 mb-0">Are you sure?</h2>
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p className="m-0">Are you sure you want to delete this document? This cannot be undone.</p>
      </Modal.Body>
      <Modal.Footer>
        <div className={'d-flex w-100'}>
          <button className={'btn btn-primary m-2'} onClick={async () => {
            await Api.deleteParticipantFile('demo', enrollee.shortcode, showConfirmDelete)
          }}>{i18n('yesDelete')}</button>
          <button className={'btn btn-outline-secondary m-2'}
            onClick={() => setShowConfirmDelete(undefined)}>{i18n('cancel')}</button>
        </div>
      </Modal.Footer>
    </ThemedModal> }
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
