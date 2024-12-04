import React from 'react'
import { Modal, ModalFooter } from 'react-bootstrap'
import { Button } from 'components/forms/Button'

export const DeleteExportIntegrationModal = ({ onConfirm, onDismiss }: {
    onConfirm: () => void, onDismiss: () => void
}) => {
  return (
    <Modal show onHide={onDismiss}>
      <Modal.Header>
        <Modal.Title>Delete export integration</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        Are you sure you want to this export integration? This action cannot be undone.
      </Modal.Body>
      <ModalFooter>
        <Button variant="danger" onClick={() => { onConfirm() }}>
            Delete
        </Button>
        <Button variant="secondary" onClick={onDismiss}>
            Cancel
        </Button>
      </ModalFooter>
    </Modal>
  )
}
