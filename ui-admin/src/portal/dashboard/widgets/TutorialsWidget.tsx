import React from 'react'
import { Link } from 'react-router-dom'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowUpRightFromSquare, faBook } from '@fortawesome/free-solid-svg-icons'
import { DocsKey, SUPPORT_EMAIL_ADDRESS, zendeskUrl } from 'util/zendeskUtils'
import { InfoCard, InfoCardBody, InfoCardHeader } from 'components/InfoCard'

export const TutorialsWidget = () => {
  return (
    <InfoCard>
      <InfoCardHeader>
        <div className="d-flex align-items-center justify-content-between w-100">
          <span className="fw-bold">Help & Tutorials</span>
          <Link to={zendeskUrl(DocsKey.HOME_PAGE)} target="_blank">
            <Button tooltip={'View all help articles'}
              variant="light" className="border">
              <FontAwesomeIcon icon={faArrowUpRightFromSquare} className="fa-lg"/> View all
            </Button>
          </Link>
        </div>
      </InfoCardHeader>
      <InfoCardBody>
        <div>
          <span>Visit our ZenDesk hub to learn more about how to use Juniper</span>
          <ul className="mt-2">
            <HelpTutorialLink docsKey={DocsKey.PREREG_SURVEYS} title="How to create a preregistration survey"/>
            <HelpTutorialLink docsKey={DocsKey.SURVEY_EDIT} title="Creating and editing surveys"/>
            <HelpTutorialLink docsKey={DocsKey.SEARCH_EXPRESSIONS} title="Enrollee search expressions"/>
            <HelpTutorialLink docsKey={DocsKey.WITHDRAWAL} title="Participant withdrawal"/>
          </ul>
          <span className="fst-italic text-muted">
          Have additional questions? Please email <a
              href={`mailto:${SUPPORT_EMAIL_ADDRESS}`}>{SUPPORT_EMAIL_ADDRESS}</a>
          </span>
        </div>
      </InfoCardBody>
    </InfoCard>
  )
}

const HelpTutorialLink = ({ docsKey, title }: { docsKey: DocsKey, title: string }) => {
  return <li>
    <FontAwesomeIcon icon={faBook} className={'me-2'}/>
    <Link target="_blank" to={zendeskUrl(docsKey)}>{title}</Link>
  </li>
}
