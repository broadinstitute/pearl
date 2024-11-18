import { act, render, screen } from '@testing-library/react'
import React from 'react'

import { FormContent } from '@juniper/ui-core'

import { FormContentJsonEditor } from './FormContentJsonEditor'

const formContent: FormContent = {
  title: 'Test survey',
  pages: [
    {
      elements: [
        {
          name: 'test_firstName',
          type: 'text',
          title: 'First name',
          isRequired: true
        },
        {
          name: 'test_lastName',
          type: 'text',
          title: 'Last name',
          isRequired: true
        }
      ]
    }
  ]
}

describe('FormContentJsonEditor', () => {
  // eslint-disable-next-line jest/expect-expect
  it('renders form content as JSON', async () => {
    // Act
    await act(async () => render(<FormContentJsonEditor initialValue={formContent} onChange={jest.fn()}/>))
    expect(screen.getByText('"Last name"')).toBeInTheDocument()
  })
})
