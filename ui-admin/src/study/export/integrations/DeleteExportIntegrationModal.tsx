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
        Are you sure you want to delete this export integration? This action cannot be undone.
        
        Note you can set integrations to inactive if you just want to pause them from running.
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
