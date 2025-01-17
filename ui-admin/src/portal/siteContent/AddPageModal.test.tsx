import { userEvent } from '@testing-library/user-event'
import { mockPortalEnvironment } from 'test-utils/mocking-utils'
import AddPageModal from 'portal/siteContent/AddPageModal'
import {
  render,
  screen
} from '@testing-library/react'
import React from 'react'
import { setupRouterTest } from '@juniper/ui-core'

describe('AddPageModal', () => {
  test('disables Create button when title and path aren\'t filled out', async () => {
    //Arrange
    const { RoutedComponent } = setupRouterTest(<AddPageModal
      onDismiss={jest.fn()}
      insertNewPage={jest.fn()}
      portalEnv={mockPortalEnvironment('sandbox')}
      portalShortcode={'test'}
    />)

    render(RoutedComponent)

    //Assert
    const createButton = screen.getByText('Create')
    expect(createButton).toBeDisabled()
  })

  test('enables Create button when title and path are filled out', async () => {
    //Arrange
    const { RoutedComponent } = setupRouterTest(<AddPageModal
      onDismiss={jest.fn()}
      insertNewPage={jest.fn()}
      portalEnv={mockPortalEnvironment('sandbox')}
      portalShortcode={'test'}
    />)

    render(RoutedComponent)

    //Act
    const pageTitleInput = screen.getByLabelText('Page Title')
    const pagePathInput = screen.getByLabelText('Page Path')
    const createButton = screen.getByText('Create')

    await userEvent.type(pageTitleInput, 'test')
    await userEvent.type(pagePathInput, 'test')

    //Assert
    expect(createButton).toBeEnabled()
  })

  test('Create button calls insertNewPage with a new page', async () => {
    //Arrange
    const mockInsertNewPageFn = jest.fn()
    const { RoutedComponent } = setupRouterTest(<AddPageModal
      onDismiss={jest.fn()}
      insertNewPage={mockInsertNewPageFn}
      portalEnv={mockPortalEnvironment('sandbox')}
      portalShortcode={'test'}
    />)

    render(RoutedComponent)

    //Act
    const pageTitleInput = screen.getByLabelText('Page Title')
    const pagePathInput = screen.getByLabelText('Page Path')
    const createButton = screen.getByText('Create')

    await userEvent.type(pageTitleInput, 'My New Page')
    await userEvent.type(pagePathInput, 'newPage')
    await userEvent.click(createButton)

    //Assert
    expect(mockInsertNewPageFn).toHaveBeenCalledWith({
      title: 'My New Page',
      path: 'newPage',
      minimalNavbar: false,
      sections: [{
        'id': '',
        'sectionConfig': '{"title":"My New Page","blurb":"Add content here","blurbAlign":"center","buttons":[]}',
        'sectionType': 'HERO_WITH_IMAGE'
      }]
    })
  })

  test('validates page path characters', async () => {
    const { RoutedComponent } = setupRouterTest(<AddPageModal
      onDismiss={jest.fn()}
      insertNewPage={jest.fn()}
      portalEnv={mockPortalEnvironment('sandbox')}
      portalShortcode={'test'}
    />)

    render(RoutedComponent)

    const pageTitleInput = screen.getByLabelText('Page Title')
    const pagePathInput = screen.getByLabelText('Page Path')
    const createButton = screen.getByText('Create')

    await userEvent.type(pageTitleInput, 'My New Page')
    await userEvent.type(pagePathInput, 'new-page-path')

    expect(createButton).toBeEnabled()

    await userEvent.clear(pagePathInput)
    await userEvent.type(pagePathInput, 'new_page_path')

    expect(createButton).toBeDisabled()
  })
})
