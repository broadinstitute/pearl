import React, { useEffect } from 'react'
import { ParticipantFile } from 'src/types/participantFile'
import { useDropzone } from 'react-dropzone'
import './DocumentRequestUpload.css'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faFile,
  faFilePdf,
  faImage,
  faUpload
} from '@fortawesome/free-solid-svg-icons'

export const DocumentRequestUpload = (
  {
    uploadNewFile,
    fetchFileLibrary,
    selectedFiles,
    setSelectedFiles
  } : {
    uploadNewFile: (file: File) => Promise<ParticipantFile>,
    fetchFileLibrary: () => Promise<ParticipantFile[]>,
    selectedFiles: ParticipantFile[]
    setSelectedFiles: (files: ParticipantFile[]) => void
  }) => {
  const [files, setFiles] = React.useState<ParticipantFile[]>([])

  useEffect(() => {
    fetchFileLibrary().then(setFiles)
  }, [])

  const selectFile = (file: ParticipantFile) => {
    if (!selectedFiles.find(f => f.id === file.id)) {
      setSelectedFiles([...selectedFiles, file])
    }
  }

  const uploadAndSelectFile = async (file: File) => {
    const newFile = await uploadNewFile(file)
    setSelectedFiles([...selectedFiles, newFile])
    setFiles([...files, newFile])
  }

  const removeFile = (file: ParticipantFile) => {
    setSelectedFiles(selectedFiles.filter(f => f.id !== file.id))
  }

  return <div className='p-3'>
    <div className='mb-2'>
      <SelectedFiles selectedFiles={selectedFiles} removeFile={removeFile}/>
    </div>
    {/*<div className='w-100 d-flex'>*/}
    {/*  <div className="w-25 p-3">*/}
    <div className='mb-2'>
      <DragAndDrop uploadNewFile={uploadAndSelectFile}/>

    </div>
    {/*</div>*/}
    {/*<div className="w-75 p-3">*/}
    <Library files={files} selectFile={selectFile}/>

    {/*  </div>*/}
    {/*</div>*/}
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
    {selectedFiles.map(selectedFile => {
      return <div key={selectedFile.id} className='w-100 d-flex'>
        <span>{selectedFile.fileName}</span>
        <button onClick={() => removeFile(selectedFile)}>x</button>
      </div>
    })}
  </div>
}

const Library = (
  {
    files,
    selectFile
  }: {
    files: ParticipantFile[],
    selectFile: (file: ParticipantFile) => void
  }
) => {
  return <div className='card'>
    <div className='card-body'>
      <p className='card-title'>My files <span>({files.length})</span></p>

      {files.map(file => {
        return <div key={file.id}
          className={'border border-1 rounded-1 bg-light-subtle p-2 d-flex align-items-center justify-content-between'}>
          <div className='d-flex align-items-center justify-content-start'>
            <FileIcon mimeType={file.fileType}/>
            <button className='btn btn-link'>{file.fileName}</button>

          </div>
          <button onClick={() => selectFile(file)} className='float-end btn btn-link text-decoration-none'>+</button>
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
