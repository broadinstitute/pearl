import React, { useState } from 'react'
import { NavbarItemInternal } from 'api/api'
import Select from 'react-select'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faClipboard, faClockRotateLeft, faImage, faPlus } from '@fortawesome/free-solid-svg-icons'
import HtmlPageEditView from './HtmlPageEditView'
import { HtmlPage, LocalSiteContent, ApiProvider, SiteContent, ApiContextT } from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import SiteContentVersionSelector from './SiteContentVersionSelector'
import { Button } from '../../components/forms/Button'
import AddPageModal from './AddPageModal'
import CreatePreRegSurveyModal from '../CreatePreRegSurveyModal'
import { PortalEnvContext } from '../PortalRouter'

type NavbarOption = {label: string, value: string}
const landingPageOption = { label: 'Landing page', value: 'Landing page' }

type InitializedSiteContentViewProps = {
  siteContent: SiteContent
  previewApi: ApiContextT
  loadSiteContent: (stableId: string, version: number) => void
  createNewVersion: (content: SiteContent) => void
  switchToVersion: (id: string, stableId: string, version: number) => void
  portalEnvContext: PortalEnvContext
  readOnly: boolean
}

/** shows a site content in editable form with a live preview.  Defaults to english-only for now */
const SiteContentEditor = (props: InitializedSiteContentViewProps) => {
  const {
    siteContent, previewApi, portalEnvContext, loadSiteContent, switchToVersion, createNewVersion, readOnly
  } = props
  const { portalEnv } = portalEnvContext
  const selectedLanguage = 'en'
  const [selectedNavOpt, setSelectedNavOpt] = useState<NavbarOption>(landingPageOption)
  const [workingContent, setWorkingContent] = useState<SiteContent>(siteContent)
  const [showVersionSelector, setShowVersionSelector] = useState(false)
  const [showAddPageModal, setShowAddPageModal] = useState(false)
  const [showAddPreRegModal, setShowAddPreRegModal] = useState(false)
  const localContent = workingContent.localizedSiteContents.find(lsc => lsc.language === selectedLanguage)
  if (!localContent) {
    return <div>no content for language {selectedLanguage}</div>
  }
  const navBarInternalItems = localContent.navbarItems
    .filter((navItem): navItem is NavbarItemInternal => navItem.itemType === 'INTERNAL')

  /** updates the global SiteContent object with the given LocalSiteContent */
  const updateLocalContent = (localContent: LocalSiteContent) => {
    const updatedLocalContents = [...workingContent.localizedSiteContents]
    const matchedIndex = workingContent.localizedSiteContents
      .findIndex(lsc => lsc.language === localContent.language)
    updatedLocalContents[matchedIndex] = localContent
    const newWorkingContent: SiteContent = {
      ...workingContent,
      localizedSiteContents: updatedLocalContents
    }
    setWorkingContent(newWorkingContent)
  }

  const insertNewPage = (page: HtmlPage) => {
    if (!localContent) {
      return
    }
    const newNavBarItem: NavbarItemInternal = {
      itemType: 'INTERNAL',
      itemOrder: localContent.navbarItems.length,
      text: page.title,
      htmlPage: page
    }
    const updatedLocalContent = {
      ...localContent,
      navbarItems: [...localContent.navbarItems, newNavBarItem]
    }
    updateLocalContent(updatedLocalContent)
    setSelectedNavOpt({ label: newNavBarItem.text, value: newNavBarItem.text || 'Landing page' })
  }

  /** updates the global SiteContent object with the given HtmlPage, which may be associated with a navItem */
  const updatePage = (page: HtmlPage, navItemText?: string) => {
    if (!localContent) {
      return
    }
    let updatedLocalContent
    if (!navItemText) {
      updatedLocalContent = {
        ...localContent,
        landingPage: page
      }
    } else {
      const updatedNavBarItems = [...localContent.navbarItems]
      const matchedNavItem = navBarInternalItems.find(navItem => navItem.text === navItemText)
      if (!matchedNavItem) {
        return
      }
      updatedNavBarItems[matchedNavItem.itemOrder] = {
        ...matchedNavItem,
        htmlPage: page
      }
      updatedLocalContent = {
        ...localContent,
        navbarItems: updatedNavBarItems
      }
    }
    updateLocalContent(updatedLocalContent)
  }
  const isEditable = !readOnly && portalEnv.environmentName === 'sandbox'

  const currentNavBarItem = selectedNavOpt.value ? navBarInternalItems
    .find(navItem => navItem.text === selectedNavOpt.value) : null
  const pageToRender = currentNavBarItem ? currentNavBarItem.htmlPage : localContent.landingPage

  const pageOpts: {label: string, value: string}[] = navBarInternalItems
    .map(navItem => ({ label: navItem.text, value: navItem.text }))
  pageOpts.unshift(landingPageOption)

  return <div className="d-flex bg-white">
    <div className="d-flex flex-column flex-grow-1 mx-1 mb-1">
      <div className="d-flex p-2 align-items-center">
        <div className="d-flex flex-grow-1">
          <h5>Website Content
            <span className="fs-6 text-muted fst-italic me-2 ms-2">
            (v{siteContent.version})
            </span>
            {isEditable && <button className="btn btn-secondary"
              onClick={() => setShowVersionSelector(!showVersionSelector)}>
              <FontAwesomeIcon icon={faClockRotateLeft}/> History
            </button> }

          </h5>
        </div>
        {
          isEditable && <div className="d-flex flex-grow-1">
            <Button className="ms-auto me-md-2" variant="primary"
              disabled={readOnly}
              onClick={() => createNewVersion(workingContent)}>
                  Save
            </Button>
            {
            // eslint-disable-next-line
            // @ts-ignore  Link to type also supports numbers for back operations
              <Link className="btn btn-cancel" to={-1}>Cancel</Link>
            }
          </div>
        }
      </div>
      <div className="px-2">
        <div className="d-flex flex-grow-1 mb-1">
          <div style={{ width: 250 }}>
            <Select options={pageOpts} value={selectedNavOpt}
              onChange={e => {
                setSelectedNavOpt(e ?? landingPageOption)
              }}/>
          </div>
          <Button className="btn btn-secondary"
            tooltip={'Add a new page'}
            disabled={readOnly || !isEditable}
            onClick={() => setShowAddPageModal(!showAddPageModal)}>
            <FontAwesomeIcon icon={faPlus}/> Add page
          </Button>
          <Link to="../images" className="btn btn-light ms-auto border m-1">
            <FontAwesomeIcon icon={faImage} className="fa-lg"/> Manage images
          </Link>
          { portalEnv.preRegSurveyId &&
            <Link to={'../forms/preReg'} className="btn btn-light border m-1">
              <FontAwesomeIcon icon={faClipboard} className="fa-lg"/> Pre-registration
            </Link> }
          { !portalEnv.preRegSurveyId &&
            <Button variant="light"  className="border m-1" tooltip={'Add a pre-registration survey that' +
                ' users must complete before being able to sign up for the portal.'}
            onClick={() => setShowAddPreRegModal(true)}
            >
              <FontAwesomeIcon icon={faClipboard} className="fa-lg"/> Pre-registration
            </Button> }
        </div>

      </div>
      <div>
        {pageToRender &&
          <ApiProvider api={previewApi}>
            <HtmlPageEditView htmlPage={pageToRender} readOnly={readOnly}
              updatePage={page => updatePage(page, currentNavBarItem?.text)}/>
          </ApiProvider>}
      </div>
    </div>
    { showVersionSelector &&
        <SiteContentVersionSelector portalShortcode={portalEnvContext.portal.shortcode} stableId={siteContent.stableId}
          current={siteContent} loadSiteContent={loadSiteContent} portalEnv={portalEnv}
          switchToVersion={switchToVersion}
          onDismiss={() => setShowVersionSelector(false)}/>
    }
    { showAddPageModal &&
        <AddPageModal portalEnv={portalEnv} portalShortcode={portalEnvContext.portal.shortcode}
          insertNewPage={insertNewPage}
          show={showAddPageModal} setShow={setShowAddPageModal}/>
    }
    { showAddPreRegModal &&
        <CreatePreRegSurveyModal portalEnvContext={portalEnvContext} onDismiss={() => setShowAddPreRegModal(false)}/>
    }
  </div>
}

export default SiteContentEditor

