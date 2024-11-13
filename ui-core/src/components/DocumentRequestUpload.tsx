import React from 'react'
import { EnvironmentName } from 'src/types/study'
import { ParticipantFile } from 'src/types/participantFile'

const DocumentRequestUpload = (
  {
    portalShortcode,
    studyShortcode,
    environmentName,
    enrolleeShortcode,
    uploadNewFile,
    fileLibrary
  } : {
    portalShortcode: string,
    studyShortcode: string,
    environmentName: EnvironmentName,
    enrolleeShortcode: string,
    uploadNewFile: (file: File) => void,
    fileLibrary: ParticipantFile[]
  }) => {
  return <>
  </>
}
