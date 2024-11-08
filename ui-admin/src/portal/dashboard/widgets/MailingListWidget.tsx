import { Link } from 'react-router-dom'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowUpRightFromSquare, faCaretUp, faLightbulb } from '@fortawesome/free-solid-svg-icons'
import LoadingSpinner from 'util/LoadingSpinner'
import React, { useState } from 'react'
import Api, { MailingListContact } from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import { Portal } from '@juniper/ui-core'
import { mailingListPath } from 'portal/PortalRouter'
import pluralize from 'pluralize'
import { InfoCard, InfoCardBody, InfoCardHeader } from 'components/InfoCard'

export const MailingListWidget = ({ portal }: { portal: Portal }) => {
  const [contacts, setContacts] = useState<MailingListContact[]>([])

  const recentContacts = contacts.filter(contact => {
    const lastWeek = new Date()
    lastWeek.setDate(lastWeek.getDate() - 7)
    return new Date((contact.createdAt || 0) * 1000) > lastWeek
  })

  const { isLoading: isMailingListLoading } = useLoadingEffect(async () => {
    const result = await Api.fetchMailingList(portal.shortcode, 'live')
    setContacts(result)
  }, [portal.shortcode])

  return (
    <InfoCard>
      <InfoCardHeader>
        <div className="d-flex align-items-center justify-content-between w-100">
          <span className="fw-bold">Mailing List</span>
          <Link to={mailingListPath(portal.shortcode, 'live')}>
            <Button tooltip={'View mailing list'}
              variant="light" className="border">
              <FontAwesomeIcon icon={faArrowUpRightFromSquare} className="fa-lg"/>
            </Button>
          </Link>
        </div>
      </InfoCardHeader>
      <InfoCardBody>
        <LoadingSpinner isLoading={isMailingListLoading}>
          {contacts.length === 0 ?
            <span className={'fst-italic text-muted'}>Your mailing list does not have any subscribers</span> :
            <div data-testid={'mailing-list-content'}>
              <div>Your mailing list has
                <span className={'fw-semibold'}> {contacts.length} </span>
                  total {pluralize('subscriber', contacts.length)} and
                <span className={'fw-semibold text-success'}> {recentContacts.length}
                  <FontAwesomeIcon className='mx-1' icon={faCaretUp}/></span>
                  new subscribers in the last week.
              </div>
              <div className="mt-3 fst-italic text-muted">
                <FontAwesomeIcon className="me-1" icon={faLightbulb}/>
                Keep potential participants engaged with your study by sending them updates and newsletters.
              </div>
            </div>
          }
        </LoadingSpinner>
      </InfoCardBody>
    </InfoCard>
  )
}
