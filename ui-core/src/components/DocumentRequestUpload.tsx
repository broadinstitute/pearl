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
  faUpload,
  faX
} from '@fortawesome/free-solid-svg-icons'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import { isNil } from 'lodash'
import { useApiContext } from 'src/participant/ApiProvider'
import { StudyEnvParams } from 'src/types/study'
import { LoadingSpinner } from '@juniper/ui-participant/src/util/LoadingSpinner'

export const DocumentRequestUpload = (
  {
    studyEnvParams,
    enrolleeShortcode,
    selectedFileNames,
    setSelectedFileNames
  } : {
    studyEnvParams: StudyEnvParams,
    enrolleeShortcode: string,
    selectedFileNames: string[]
    setSelectedFileNames: (fileNames: string[]) => void
  }) => {
  const [files, setFiles] = React.useState<ParticipantFile[]>([])
  const [selectedFiles, setSelectedFiles] = React.useState<ParticipantFile[]>([])

  const Api = useApiContext()

  const [isUploading, setIsUploading] = React.useState(false)

  useEffect(() => {
    Api.getParticipantFiles({ studyEnvParams, enrolleeShortcode }).then(files => {
      setFiles(files)
      setSelectedFiles(files.filter(f => selectedFileNames.includes(f.fileName)))
    })
  }, [])

  const updateSelectedFiles = (newSelectedFiles: ParticipantFile[]) => {
    console.log('newSelectedFiles', newSelectedFiles)
    setSelectedFiles(newSelectedFiles)
    setSelectedFileNames(newSelectedFiles.map(f => f.fileName))
  }

  const selectFile = (file: ParticipantFile) => {
    if (!selectedFiles.find(f => f.id === file.id)) {
      updateSelectedFiles([...selectedFiles, file])
    }
  }

  const uploadAndSelectFile = async (fileData: File) => {
    setIsUploading(true)
    const newFile = await Api.uploadParticipantFile({ studyEnvParams, enrolleeShortcode, file: fileData })
    setIsUploading(false)
    updateSelectedFiles([...selectedFiles, newFile])
  }

  const removeFile = (file: ParticipantFile) => {
    updateSelectedFiles(selectedFiles.filter(f => f.id !== file.id))
  }

  if (isUploading) {
    return <LoadingSpinner/>
  }

  return <div className='p-3'>
    <div className='mb-2'>
      <SelectedFiles selectedFiles={selectedFiles} removeFile={removeFile}/>
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
    <Library files={files} selectFile={selectFile} removeFile={removeFile} selectedFiles={selectedFiles}/>
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
    files,
    selectedFiles,
    selectFile,
    removeFile
  }: {
    files: ParticipantFile[],
    selectedFiles: ParticipantFile[],
    selectFile: (file: ParticipantFile) => void,
    removeFile: (file: ParticipantFile) => void
  }
) => {
  const isSelected = (file: ParticipantFile) => {
    return selectedFiles.find(f => f.id === file.id)
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

      {expanded && files.map(file => {
        return <div key={file.id}
          className={'border border-1 rounded-1 bg-light-subtle p-2 d-flex align-items-center justify-content-between'}>
          <div className='d-flex align-items-center justify-content-start'>
            <FileIcon mimeType={file.fileType}/>
            <button className='btn btn-link'>{file.fileName}</button>

          </div>
          {isSelected(file)
            ? <button
              onClick={() => removeFile(file)}
              className='float-end btn btn-outline-danger text-decoration-none border-0'>
              <FontAwesomeIcon icon={faX}/>
            </button>
            : <button
              onClick={() => selectFile(file)}
              className='float-end btn btn-outline-primary text-decoration-none border-0'>
              <FontAwesomeIcon icon={faPlus}/>
            </button>}
        </div>
      })}
    </div>

  </div>
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
