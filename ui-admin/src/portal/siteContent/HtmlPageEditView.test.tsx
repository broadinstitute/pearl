import {
  act,
  screen
} from '@testing-library/react'
import React from 'react'
import { mockSiteContent } from 'test-utils/mock-site-content'
import HtmlPageEditView from './HtmlPageEditView'
import { userEvent } from '@testing-library/user-event'
import { sectionTemplates } from './sectionTemplates'
import {
  MockI18nProvider,
  renderWithRouter
} from '@juniper/ui-core'
import { mockPortalEnvContext } from 'test-utils/mocking-utils'
import { Store } from 'react-notifications-component'
import Api from 'api/api'

function mockStoreAndApi() {
  jest.spyOn(Api, 'getPortalMedia').mockResolvedValue([])
  jest.spyOn(Store, 'addNotification').mockImplementation(() => '')
}


test('readOnly disables insert new section button', async () => {
  mockStoreAndApi()
  const siteContent = mockSiteContent()
  await act(async () => renderWithRouter(
    <MockI18nProvider>
      <HtmlPageEditView
        htmlPage={siteContent.localizedSiteContents[0].pages[0]}
        localSiteContent={siteContent.localizedSiteContents[0]}
        readOnly={true}
        updatePage={jest.fn()}
        setSiteHasInvalidSection={jest.fn()}
        updateNavbarItems={jest.fn()}
        portalEnvContext={mockPortalEnvContext('sandbox')}
        siteHasInvalidSection={false} footerSection={undefined} updateFooter={jest.fn()}/>
    </MockI18nProvider>))
  expect(screen.getAllByLabelText('Insert a blank section')[0]).toHaveAttribute('aria-disabled', 'true')
})

test('Insert Section button calls updatePage with a new blank HERO_WITH_IMAGE section', async () => {
  mockStoreAndApi()
  const siteContent = mockSiteContent()
  const mockUpdatePageFn = jest.fn()
  const mockPage = siteContent.localizedSiteContents[0].pages[0]
  await act(async () => renderWithRouter(
    <MockI18nProvider>
      <HtmlPageEditView
        htmlPage={mockPage}
        localSiteContent={siteContent.localizedSiteContents[0]}
        readOnly={false} updatePage={mockUpdatePageFn}
        setSiteHasInvalidSection={jest.fn()}
        updateNavbarItems={jest.fn()}
        portalEnvContext={mockPortalEnvContext('sandbox')}
        siteHasInvalidSection={false} footerSection={undefined} updateFooter={jest.fn()}/>
    </MockI18nProvider>))

  //Act
  const insertSectionButton = screen.getAllByLabelText('Insert a blank section')[1]
  await userEvent.click(insertSectionButton)

  //Assert
  expect(insertSectionButton).toHaveAttribute('aria-disabled', 'false')
  expect(mockUpdatePageFn).toHaveBeenCalledWith({
    ...mockPage,
    sections: [
      ...mockPage.sections,
      { id: '', sectionType: 'HERO_WITH_IMAGE', sectionConfig: JSON.stringify(sectionTemplates['HERO_WITH_IMAGE']) }
    ]
  })
})

test('invalid JSON disables Insert Section button', async () => {
  mockStoreAndApi()
  const siteContent = mockSiteContent()
  await act(async () => renderWithRouter(
    <MockI18nProvider>
      <HtmlPageEditView
        htmlPage={siteContent.localizedSiteContents[0].pages[0]}
        localSiteContent={siteContent.localizedSiteContents[0]}
        readOnly={false} updatePage={jest.fn()}
        setSiteHasInvalidSection={jest.fn()}
        updateNavbarItems={jest.fn()}
        portalEnvContext={mockPortalEnvContext('sandbox')}
        siteHasInvalidSection={true} footerSection={undefined} updateFooter={jest.fn()}/>
    </MockI18nProvider>))
  const sectionButtons= await screen.findAllByLabelText('Insert a blank section')

  sectionButtons.forEach(button => {
    expect(button).toHaveAttribute('aria-disabled', 'true')
  })
})
