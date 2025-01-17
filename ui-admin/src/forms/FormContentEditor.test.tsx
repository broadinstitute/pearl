import {
  render,
  screen, waitFor
} from '@testing-library/react'
import React from 'react'

import { FormContentEditor } from './FormContentEditor'
import { userHasPermission } from '../user/UserProvider'

jest.mock('user/UserProvider', () => ({
  ...jest.requireActual('user/UserProvider'),
  userHasPermission: jest.fn()
}))

//This is valid JSON, but invalid survey JSON
const formContent: string = JSON.stringify({
  title: 'Test survey',
  pages: [
    {}
  ]
})

describe('FormContentEditor', () => {
  it('should trap FormDesigner errors in an ErrorBoundary', () => {
    (userHasPermission as jest.Mock).mockImplementation(() => {
      return true
    })
    // avoid cluttering the console with the error message from the expected error
    jest.spyOn(console, 'error').mockImplementation(jest.fn())
    const { container } = render(<FormContentEditor
      initialAnswerMappings={[]}
      currentLanguage={{ languageCode: 'en', languageName: 'English', id: '' }}
      supportedLanguages={[]}
      initialContent={formContent}
      readOnly={false}
      onFormContentChange={jest.fn()}
      onAnswerMappingChange={jest.fn()}
    />)

    screen.getAllByText('Designer')[0].click()

    // Our custom ErrorBoundary text
    waitFor(() => {
      expect(container).toHaveTextContent('Something went wrong')
    })
    // JSON Editor and Preview tabs should still be visible
    expect(container).toHaveTextContent('JSON Editor')
    expect(container).toHaveTextContent('Preview')
  })
})
