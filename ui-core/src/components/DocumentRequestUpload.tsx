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
import LoadingSpinner from '@juniper/ui-admin/src/util/LoadingSpinner'

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
    <Library uploadingFile={uploadingFile} files={files} selectFile={selectFile} removeFile={unselectFile}
      selectedFiles={selectedFiles}/>
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
    removeFile
  }: {
    uploadingFile?: string,
    files: ParticipantFile[],
    selectedFiles: ParticipantFile[],
    selectFile: (file: ParticipantFile) => void,
    removeFile: (file: ParticipantFile) => void
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
        fileType={'text/plain'}
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
          onRemove={() => removeFile(file)}
          onSelect={() => selectFile(file)}/>
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
  onRemove
}: {
  fileType: string,
  fileName: string,
  isUploading: boolean,
  isSelected: boolean,
  onSelect?: () => void,
  onRemove?: () => void
}) => {
  return <div
    className={'border border-1 rounded-1 bg-light-subtle p-2 d-flex align-items-center justify-content-between'}>
    <div className='d-flex align-items-center justify-content-start'>
      <FileIcon mimeType={fileType}/>
      <button className='btn btn-link'>{fileName}</button>

    </div>
    {isUploading && <LoadingSpinner/>}
    {isSelected
      ? <button
        onClick={onRemove}
        className='float-end btn btn-outline-danger text-decoration-none border-0'>
        <FontAwesomeIcon icon={faX}/>
      </button>
      : <button
        onClick={onSelect}
        className='float-end btn btn-outline-primary text-decoration-none border-0'>
        <FontAwesomeIcon icon={faPlus}/>
      </button>}
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
