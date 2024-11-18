import React, { useEffect } from 'react'
import { ParticipantFile } from 'src/types/participantFile'
import { useDropzone } from 'react-dropzone'
import './DocumentRequestUpload.css'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCaretDown,
  faCaretUp,
  faFile,
  faFilePdf,
  faImage,
  faTrashCan,
  faUpload,
  faX
} from '@fortawesome/free-solid-svg-icons'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import { isNil } from 'lodash'
import { useApiContext } from 'src/participant/ApiProvider'
import { StudyEnvParams } from 'src/types/study'
import LoadingSpinner from '@juniper/ui-admin/src/util/LoadingSpinner'
import { saveBlobAsDownload } from '@juniper/ui-admin/src/util/downloadUtils'
import Modal from 'react-bootstrap/Modal'
import { ModalProps } from 'react-bootstrap'

export const DocumentRequestUpload = (
  {
    studyEnvParams,
    enrolleeShortcode,
    selectedFileNames,
    setSelectedFileNames,
    ModalComponent
  } : {
    studyEnvParams: StudyEnvParams,
    enrolleeShortcode: string,
    selectedFileNames: string[]
    setSelectedFileNames: (fileNames: string[]) => void,
    ModalComponent: React.ElementType<ModalProps>
  }) => {
  const [files, setFiles] = React.useState<ParticipantFile[]>([])
  const [selectedFiles, setSelectedFiles] = React.useState<ParticipantFile[]>([])

  const Api = useApiContext()

  const [uploadingFile, setUploadingFile] = React.useState<string>()

  useEffect(() => {
    Api.getParticipantFiles({ studyEnvParams, enrolleeShortcode }).then(files => {
      setFiles(files)
      setSelectedFiles(files.filter(f => selectedFileNames.includes(f.fileName)))
    })
  }, [])

  const selectFile = (newFile: ParticipantFile) => {
    if (!selectedFiles.find(f => f.id === newFile.id)) {
      setSelectedFiles(oldSelected => [...oldSelected.filter(old => old.fileName !== newFile.fileName), newFile])
      setSelectedFileNames([...selectedFileNames.filter(old => old !== newFile.fileName), newFile.fileName])
    }
  }

  const unselectFile = (file: ParticipantFile) => {
    setSelectedFiles(oldFiles => oldFiles.filter(f => f.id !== file.id))
    setSelectedFileNames(selectedFileNames.filter(f => f !== file.fileName))
  }


  const uploadAndSelectFile = async (fileData: File) => {
    setUploadingFile(fileData.name)
    const newFile = await Api.uploadParticipantFile({ studyEnvParams, enrolleeShortcode, file: fileData })
    setUploadingFile(undefined)

    setFiles(oldFiles => [newFile, ...oldFiles.filter(f => f.fileName !== newFile.fileName)])
    selectFile(newFile)
  }

  const deleteFile = async (file: ParticipantFile) => {
    await Api.deleteParticipantFile({ studyEnvParams, enrolleeShortcode, fileName: file.fileName })
    setFiles(oldFiles => oldFiles.filter(f => f.id !== file.id))
    setSelectedFiles(oldFiles => oldFiles.filter(f => f.id !== file.id))
    setSelectedFileNames(selectedFileNames.filter(f => f !== file.fileName))
  }

  const downloadFile = async (file: ParticipantFile) => {
    const response = await Api.downloadParticipantFile({ studyEnvParams, enrolleeShortcode, fileName: file.fileName })

    saveBlobAsDownload(await response.blob(), file.fileName)
  }

  return <div className='p-3'>
    <div className='mb-2'>
      <SelectedFiles selectedFiles={selectedFiles} removeFile={unselectFile}/>
    </div>
    <div className='mb-2'>
      {/* show on desktops */}
      <div className='d-none d-lg-block'>
        <DragAndDrop uploadNewFile={uploadAndSelectFile}/>
      </div>
      {/* show on mobile */}
      <div className={'d-lg-none'}>
        <FileUpload uploadNewFile={uploadAndSelectFile}/>
      </div>

    </div>
    <Library
      uploadingFile={uploadingFile}
      files={files}
      selectFile={selectFile}
      unselectFile={unselectFile}
      selectedFiles={selectedFiles}
      deleteFile={deleteFile}
      downloadFile={downloadFile}
      ModalComponent={ModalComponent}
    />
  </div>
}

const SelectedFiles = (
  {
    selectedFiles,
    removeFile
  }: {
    selectedFiles: ParticipantFile[],
    removeFile: (file: ParticipantFile) => void
  }
) => {
  return <div>
    <p>Selected files ({selectedFiles.length})</p>
    {selectedFiles.map(selectedFile => {
      return <div key={selectedFile.id} className='w-100 justify-content-between d-flex'>
        <div>
          <FileIcon mimeType={selectedFile.fileType}/>
          <span> {selectedFile.fileName}</span>
        </div>
        <button
          className={'btn btn-link'}
          onClick={() => removeFile(selectedFile)}
        >
          <FontAwesomeIcon icon={faX}/>
        </button>
      </div>
    })}
  </div>
}

const Library = (
  {
    uploadingFile,
    files,
    selectedFiles,
    selectFile,
    unselectFile,
    deleteFile,
    downloadFile,
    ModalComponent
  }: {
    uploadingFile?: string,
    files: ParticipantFile[],
    selectedFiles: ParticipantFile[],
    selectFile: (file: ParticipantFile) => void,
    unselectFile: (file: ParticipantFile) => void,
    deleteFile: (file: ParticipantFile) => void,
    downloadFile: (file: ParticipantFile) => void,
    ModalComponent: React.ElementType<ModalProps>
  }
) => {
  const isSelected = (file: ParticipantFile) => {
    return !!selectedFiles.find(f => f.id === file.id)
  }

  const [expanded, setExpanded] = React.useState(true)

  return <div className='card'>
    <div className='card-body'>
      <p className='card-title'>
        My files <span>({files.length})</span>
        <button className='btn btn-link' onClick={() => setExpanded(!expanded)}>
          <FontAwesomeIcon icon={expanded ? faCaretUp : faCaretDown}/>
        </button>
      </p>

      {uploadingFile && <FileRow
        fileType={''}
        fileName={uploadingFile}
        isUploading={true}
        isSelected={false}
      />}
      {expanded && files.map(file => {
        return <FileRow
          fileType={file.fileType}
          fileName={file.fileName}
          isUploading={false}
          isSelected={isSelected(file)}
          onUnselect={() => unselectFile(file)}
          onSelect={() => selectFile(file)}
          onDelete={() => deleteFile(file)}
          onDownload={() => downloadFile(file)}
          ModalComponent={ModalComponent}
          key={file.id}
        />
      })}
    </div>

  </div>
}

const FileRow = ({
  fileType,
  fileName,
  isUploading,
  isSelected,
  onSelect,
  onUnselect,
  onDelete,
  onDownload,
  ModalComponent
}: {
  fileType: string,
  fileName: string,
  isUploading: boolean,
  isSelected: boolean,
  onSelect?: () => void,
  onUnselect?: () => void,
  onDelete?: () => void,
  onDownload?: () => void,
  ModalComponent: React.ElementType<ModalProps>
}) => {
  const [showDeleteModal, setShowDeleteModal] = React.useState(false)

  return <div
    className={'border border-1 rounded-1 bg-light-subtle p-2 d-flex align-items-center justify-content-between'}>
    <div className='d-flex align-items-center justify-content-between'>
      <FileIcon mimeType={fileType}/>
      <button className='btn btn-link'>{fileName}</button>

    </div>
    {isUploading && <LoadingSpinner/>}

    <div className='d-flex justify-content-end'>
      {onDelete && <button
        onClick={() => setShowDeleteModal(true)}
        className='float-end btn btn-outline-danger text-decoration-none border-0'>
        <FontAwesomeIcon icon={faTrashCan}/>
      </button>}
      {isSelected
        ? <button
          onClick={onUnselect}
          className='float-end btn btn-outline-primary text-decoration-none border-0'>
          <FontAwesomeIcon icon={faX}/>
        </button>
        : <button
          onClick={onSelect}
          className='float-end btn btn-outline-primary text-decoration-none border-0'>
          <FontAwesomeIcon icon={faPlus}/>
        </button>}
    </div>

    {onDelete && showDeleteModal && <DeleteModal
      fileName={fileName}
      onDelete={() => {
        setShowDeleteModal(false)
        onDelete()
      }}
      onClose={() => setShowDeleteModal(false)}
      ModalComponent={ModalComponent}
    />}

  </div>
}

const DeleteModal = ({
  fileName,
  onDelete,
  onClose,
  ModalComponent
}: {
  fileName: string,
  onDelete: () => void,
  onClose: () => void,
  ModalComponent: React.ElementType<ModalProps>
}) => {
  return <ModalComponent show={true}>
    <Modal.Header>
      <Modal.Title>
        Delete {fileName}
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>Are you sure you want to delete {fileName}? This action cannot be undone.</p>
    </Modal.Body>

    <Modal.Footer>
      <button className='btn btn-danger' onClick={onDelete}>Delete</button>
      <button className='btn btn-link' onClick={onClose}>Cancel</button>
    </Modal.Footer>
  </ModalComponent>
}

const getIcon = (mimeType: string) => {
  if (mimeType.startsWith('image')) {
    return faImage
  } else if (mimeType === 'application/pdf') {
    return faFilePdf
  } else {
    return faFile
  }
}

const FileIcon = ({ mimeType }: { mimeType: string }) => {
  const icon = getIcon(mimeType)
  return <FontAwesomeIcon icon={icon}/>
}

const DragAndDrop = ({ uploadNewFile }: { uploadNewFile: (file: File) => void }) => {
  const onDrop = React.useCallback((acceptedFiles: File[]) => {
    acceptedFiles.forEach((file: File) => {
      uploadNewFile(file)
    })
  }, [])

  const { getRootProps, getInputProps } = useDropzone({ onDrop })

  return <div {...getRootProps()}>
    <input {...getInputProps()} />
    <div className="file-dropper w-100">
      <div className={'d-flex w-100 h-100 align-items-center justify-content-center flex-column'}>
        <span className='py-5'>
          <FontAwesomeIcon
            icon={faUpload} className={'text-primary'}
          /> Drop files here or <a type="button" className='text-decoration-underline'>
          click to upload
          </a>
        </span>
      </div>
    </div>
  </div>
}

const FileUpload = ({ uploadNewFile }: { uploadNewFile: (file: File) => void }) => {
  return <div>
    <input type="file" className={'form-control'} onChange={e => {
      const file = e.target.files?.item(0) || null
      if (!isNil(file)) {
        uploadNewFile(file)
      }
    }}/>
  </div>
}
