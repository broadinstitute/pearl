import React, { useState } from 'react'
import Api, { EnrolleeSearchExpressionResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { useSearchParams } from 'react-router-dom'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { useLoadingEffect } from 'api/api-utils'
import { renderPageHeader } from 'util/pageUtils'
import ParticipantSearch from './search/ParticipantSearch'
import { useParticipantSearchState } from 'util/participantSearchUtils'
import { concatSearchExpressions } from 'util/searchExpressionUtils'
import ParticipantListTableGroupedByFamily from 'study/participants/participantList/ParticipantListTableGroupedByFamily'
import ParticipantListTable from 'study/participants/participantList/ParticipantListTable'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faPeopleGroup,
  faPerson
} from '@fortawesome/free-solid-svg-icons'

/** Shows a list of (for now) enrollees */
function ParticipantList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv } = studyEnvContext
  const [participantList, setParticipantList] = useState<EnrolleeSearchExpressionResult[]>([])

  const [searchParams, setSearchParams] = useSearchParams()

  const groupByFamily = searchParams.get('groupByFamily') === 'true'
  const setGroupByFamily = (groupByFamily: boolean) => {
    setSearchParams(params => {
      params.set('groupByFamily', groupByFamily.toString())
      return params
    })
  }

  const familyLinkageEnabled = studyEnvContext.currentEnv.studyEnvironmentConfig.enableFamilyLinkage

  const {
    searchState,
    updateSearchState,
    setSearchState,
    searchExpression
  } = useParticipantSearchState()

  const { isLoading } = useLoadingEffect(async () => {
    const results = await Api.executeSearchExpression(
      portal.shortcode,
      study.shortcode,
      currentEnv.environmentName,
      // if families exist, adding these expression guarantees that we get all families.
      // might be a better way to do it, but this works for now
      familyLinkageEnabled
        ? concatSearchExpressions([`include({family.shortcode})`, searchExpression])
        : searchExpression)
    setParticipantList(results)
  }, [portal.shortcode, study.shortcode, currentEnv.environmentName, searchExpression])


  return <div className="ParticipantList container-fluid px-4 py-2">
    {renderPageHeader('Participant List')}
    <div className="d-flex align-content-center">
      <ParticipantSearch
        key={currentEnv.environmentName}
        studyEnvContext={studyEnvContext}
        searchState={searchState}
        updateSearchState={updateSearchState}
        setSearchState={setSearchState}
      />
      {
        familyLinkageEnabled && <div className="d-flex align-content-center ms-2">
          <button
            className="btn-secondary btn-sm"
            aria-label={groupByFamily ? 'Participant view' : 'Family view'}
            onClick={() => setGroupByFamily(!groupByFamily)}>
            {groupByFamily
              ? <><FontAwesomeIcon size={'sm'} icon={faPerson}/> Participant view</>
              : <><FontAwesomeIcon size={'sm'} icon={faPeopleGroup}/> Family view</>}
          </button>
        </div>
      }

    </div>


    <LoadingSpinner isLoading={isLoading}>
      {groupByFamily
        ? <ParticipantListTableGroupedByFamily participantList={participantList} studyEnvContext={studyEnvContext}/>
        : <ParticipantListTable participantList={participantList} studyEnvContext={studyEnvContext}/>}
    </LoadingSpinner>
  </div>
}


export default ParticipantList
