import React from 'react'
import {
  Portal,
  Study
} from '@juniper/ui-core'
import Modal from 'react-bootstrap/Modal'
import Api from 'api/api'
import { RequireUserPermission } from 'util/RequireUserPermission'

export const UpdateStudyModal = ({
  study,
  portal,
  reload,
  onClose
}: {
  study: Study,
  portal: Portal,
  reload: () => void,
  onClose: () => void
}) => {
  const [name, setName] = React.useState(study.name)
  const [shortcode, setShortcode] = React.useState(study.shortcode)

  const updateStudy = async () => {
    // update the study
    await Api.updateStudy(portal.shortcode, study.shortcode, { name, shortcode, studyEnvironments: [] })
    reload()
    onClose()
  }

  return <Modal show={true} onHide={onClose}>
    <Modal.Header closeButton>
      <Modal.Title>Update Study</Modal.Title>
      <div className="ms-4">
        {study.name}
      </div>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
        <label className="form-label">Study Name</label>
        <input
          className="form-control"
          value={name}
          onChange={e => setName(e.target.value)}
        />
        <RequireUserPermission superuser>
          <label className="form-label">Shortcode</label>
          <input
            className="form-control"
            value={shortcode}
            onChange={e => setShortcode(e.target.value)}/>
        </RequireUserPermission>
      </div>
    </Modal.Body>
    <Modal.Footer>
      <button
        className="btn btn-primary"
        onClick={updateStudy}
      >Update Study</button>
      <button className="btn btn-secondary" onClick={onClose}>Close</button>
    </Modal.Footer>
  </Modal>
}
