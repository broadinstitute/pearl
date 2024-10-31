import React from 'react'
import { screen } from '@testing-library/react'
import { TriggerDesignerEditor } from 'study/notifications/TriggerDesignerEditor'
import {
  mockStudyEnvContext,
  mockTrigger
} from 'test-utils/mocking-utils'
import { renderWithRouter } from '@juniper/ui-core'

describe('TriggerDesignerEditor', () => {
  it('can edit base fields only', () => {
    renderWithRouter(<TriggerDesignerEditor
      trigger={mockTrigger()}
      updateTrigger={jest.fn()}
      studyEnvContext={mockStudyEnvContext()}
      sendTestEmail={jest.fn()}
      baseFieldsOnly={true}
    />)

    expect(screen.getByLabelText('Event type')).toBeInTheDocument()

    expect(screen.queryByLabelText('Only trigger if enrollee meets certain criteria')).not.toBeInTheDocument()
  })

  it('can edit all fields', () => {
    renderWithRouter(<TriggerDesignerEditor
      trigger={mockTrigger()}
      updateTrigger={jest.fn()}
      studyEnvContext={mockStudyEnvContext()}
      sendTestEmail={jest.fn()}
      baseFieldsOnly={false}
    />)

    expect(screen.getByLabelText('Event type')).toBeInTheDocument()

    expect(screen.getByLabelText('Only trigger if enrollee meets certain criteria')).toBeInTheDocument()
  })
})
