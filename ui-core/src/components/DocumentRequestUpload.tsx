import React, { useEffect, useState } from 'react'
import { ParticipantFile } from '../types/participantFile'
import { useDropzone } from 'react-dropzone'
import './DocumentRequestUpload.css'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCaretDown,
  faCaretUp,
  faFile, faFileImage,
  faFilePdf, faSquareMinus, faSquarePlus,
  faUpload
} from '@fortawesome/free-solid-svg-icons'
import { isNil } from 'lodash'
import { useApiContext } from '../participant/ApiProvider'
import { StudyEnvParams } from '../types/study'
import LoadingSpinner from '@juniper/ui-admin/src/util/LoadingSpinner'
import { saveBlobAsDownload } from '../util/downloadUtils'

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
  const [files, setFiles] = useState<ParticipantFile[]>([])
  const [selectedFiles, setSelectedFiles] = useState<ParticipantFile[]>([])

  const Api = useApiContext()

  const [uploadingFile, setUploadingFile] = useState<string>()

  useEffect(() => {
    Api.listParticipantFiles({ studyEnvParams, enrolleeShortcode }).then(files => {
      setFiles(files)
      setSelectedFiles(files.filter(f => selectedFileNames.includes(f.fileName)))
    })
  }, [studyEnvParams])

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

  const downloadFile = async (file: ParticipantFile) => {
    const response = await Api.downloadParticipantFile({ studyEnvParams, enrolleeShortcode, fileName: file.fileName })

    saveBlobAsDownload(await response.blob(), file.fileName)
  }

  return <div className='pt-2'>
    <div className='mb-2'>
      <SelectedFiles selectedFiles={selectedFiles} removeFile={unselectFile} onDownload={downloadFile}/>
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
      downloadFile={downloadFile}
    />
  </div>
}

const SelectedFiles = (
  {
    selectedFiles,
    removeFile,
    onDownload
  }: {
        selectedFiles: ParticipantFile[],
        removeFile: (file: ParticipantFile) => void,
        onDownload?: (file: ParticipantFile) => void
    }
) => {
  return <div className='card'>
    <div className='card-body'>
      <p className='card-title mb-2'>Selected documents ({selectedFiles.length})</p>
      {selectedFiles.length === 0 && <div className='fst-italic text-muted mt-3'>No documents selected</div>}
      {selectedFiles.map(selectedFile => {
        return <FileRow
          fileType={selectedFile.fileType}
          fileName={selectedFile.fileName}
          isUploading={false}
          isSelected={true}
          onUnselect={() => removeFile(selectedFile)}
          onSelect={() => removeFile(selectedFile)}
          onDownload={onDownload ? () => onDownload(selectedFile) : undefined}
          key={selectedFile.id}
        />
      })}
    </div>
  </div>
}

const Library = (
  {
    uploadingFile,
    files,
    selectedFiles,
    selectFile,
    unselectFile,
    downloadFile
  }: {
        uploadingFile?: string,
        files: ParticipantFile[],
        selectedFiles: ParticipantFile[],
        selectFile: (file: ParticipantFile) => void,
        unselectFile: (file: ParticipantFile) => void,
        downloadFile: (file: ParticipantFile) => void
    }
) => {
  const isSelected = (file: ParticipantFile) => {
    return !!selectedFiles.find(f => f.id === file.id)
  }

  const [expanded, setExpanded] = React.useState(true)

  const unselectedFiles = files.filter(f => !isSelected(f))

  return <div className='card'>
    <div className='card-body'>
      <p className='card-title mb-2'>
            Available documents <span>({unselectedFiles.length})</span>
        <button className='btn btn-link p-0 ps-2' onClick={() => setExpanded(!expanded)}>
          <FontAwesomeIcon icon={expanded ? faCaretUp : faCaretDown}/>
        </button>
      </p>

      {uploadingFile && <FileRow
        fileType={''}
        fileName={uploadingFile}
        isUploading={true}
        isSelected={false}
      />}
      {expanded && <>
        {unselectedFiles.length === 0 && <div className='fst-italic text-muted'>No documents available</div>}
        {unselectedFiles.map(file => {
          return <FileRow
            fileType={file.fileType}
            fileName={file.fileName}
            isUploading={false}
            isSelected={isSelected(file)}
            onUnselect={() => unselectFile(file)}
            onSelect={() => selectFile(file)}
            onDownload={() => {
              console.log(file)
              downloadFile(file)
            }}
            key={file.id}
          />
        })}</>}
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
  onDownload
}: {
    fileType: string,
    fileName: string,
    isUploading: boolean,
    isSelected: boolean,
    onSelect?: () => void,
    onUnselect?: () => void,
    onDownload?: () => void
}) => {
  return <div
    className={'border border-1 rounded-1 bg-light-subtle p-2 d-flex align-items-center justify-content-between mb-2'}>
    <div className='d-flex align-items-center justify-content-between'>
      <FileIcon mimeType={fileType}/>
      <button onClick={onDownload} className='btn btn-link text-wrap text-start'>{fileName}</button>
    </div>
    {isUploading && <LoadingSpinner/>}

    <div className='d-flex justify-content-end'>
      {isSelected
        ? <button onClick={onUnselect}
          className='float-end btn btn-outline-primary text-decoration-none border-0'>
          <FontAwesomeIcon icon={faSquareMinus}/>
        </button>
        : <button onClick={onSelect}
          className='float-end btn btn-outline-primary text-decoration-none border-0'>
          <FontAwesomeIcon icon={faSquarePlus}/>
        </button>}
    </div>
  </div>
}

const getIcon = (mimeType: string) => {
  if (mimeType.startsWith('image')) {
    return faFileImage
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
    <div className="file-dropper w-100 my-4">
      <div className={'d-flex w-100 h-100 align-items-center justify-content-center flex-column'}>
        <span className='py-5'>
          <FontAwesomeIcon icon={faUpload} className={'text-primary me-1'}/>
            Drop files here or <a type="button" className='text-decoration-underline'>
          click to upload
          </a>
        </span>
      </div>
    </div>
  </div>
}

const truncateFileName = (text: string, maxLength: number) => {
  const extension = text.substring(text.lastIndexOf('.'))
  const nameWithoutExtension = text.substring(0, text.lastIndexOf('.'))

  if (nameWithoutExtension.length > maxLength) {
    return `${nameWithoutExtension.substring(0, maxLength)  }...${  extension}`
  }
  return text
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
