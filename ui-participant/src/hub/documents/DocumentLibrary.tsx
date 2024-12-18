import { Enrollee, ParticipantFile } from '@juniper/ui-core'
import React, { useEffect, useState } from 'react'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { usePortalEnv } from 'providers/PortalProvider'
import Api from '../../api/api'

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
            <Documents enrollee={enrollees.find(enrollee => enrollee.profileId === ppUser?.profileId)!}
              participantFiles={participantFiles}/>
          </div>
        </div>
      </div>
    </div>
  </div>
}

const Documents = ({ enrollee, participantFiles }: { enrollee: Enrollee, participantFiles: ParticipantFile[] }) => {
  const { portal, portalEnv } = usePortalEnv()

  return <div className="mb-3 rounded round-3 py-4 bg-white px-md-5 shadow-sm px-2">
    <h1 className="pb-3">
      Documents
    </h1>
    <div className="pb-4">
      Below you will find all of the documents that you have uploaded to your studies. You can also view
        documents that you have completed as part of your studies, such as consent forms.
    </div>
    <h3>Your documents ({participantFiles.length})</h3>
    <div className="d-flex flex-column">
      {participantFiles.map((participantFile, index) => {
        return <div key={index} className="d-flex justify-content-between align-items-center py-2">
          {participantFile.fileName}
        </div>
      })}
    </div>
  </div>
}
