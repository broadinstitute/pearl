import React from 'react'

import { Accordion } from 'react-bootstrap'
import { toNumber } from 'lodash'
import Creatable from 'react-select/creatable'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Select from 'react-select'
import { ParticipantSearchState } from 'util/participantSearchUtils'
import { LazySearchQueryBuilder } from 'search/LazySearchQueryBuilder'

/**
 * Renders the facets that you can search upon in the participant list.
 */
export default function EnrolleeSearchFacets({
  studyEnvContext,
  searchState,
  updateSearchState,
  reset
}: {
  studyEnvContext: StudyEnvContextT,
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void,
  reset: () => void
}) {
  return <div>
    <button className="btn btn-secondary float-end" onClick={reset}>Clear all</button>
    <Accordion alwaysOpen flush>
      <Accordion.Item eventKey={'keyword'} key={'keyword'}>
        <Accordion.Header>Keyword</Accordion.Header>
        <Accordion.Body>
          <KeywordFacet searchState={searchState} updateSearchState={updateSearchState}/>
        </Accordion.Body>
      </Accordion.Item>
      <Accordion.Item eventKey={'enrollee'} key={'enrollee'}>
        <Accordion.Header>Enrollee</Accordion.Header>
        <Accordion.Body>
          <EnrolleeFacet studyEnvContext={studyEnvContext} searchState={searchState}
            updateSearchState={updateSearchState}/>
        </Accordion.Body>
      </Accordion.Item>
      <Accordion.Item eventKey={'age'} key={'age'}>
        <Accordion.Header>Age</Accordion.Header>
        <Accordion.Body>
          <AgeFacet searchState={searchState} updateSearchState={updateSearchState}/>
        </Accordion.Body>
      </Accordion.Item>
      <Accordion.Item eventKey={'sexAtBirth'} key={'sexAtBirth'}>
        <Accordion.Header>Sex at birth</Accordion.Header>
        <Accordion.Body>
          <SexAssignedAtBirthFacet searchState={searchState} updateSearchState={updateSearchState}/>
        </Accordion.Body>
      </Accordion.Item>
      <Accordion.Item eventKey={'taskStatus'} key={'taskStatus'}>
        <Accordion.Header>Task status</Accordion.Header>
        <Accordion.Body>
          <TaskStatusFacet studyEnvContext={studyEnvContext} searchState={searchState}
            updateSearchState={updateSearchState}/>
        </Accordion.Body>
      </Accordion.Item>
      <Accordion.Item eventKey={'latestkit'} key={'latestkit'}>
        <Accordion.Header>Latest kit</Accordion.Header>
        <Accordion.Body>
          <LatestKitFacet searchState={searchState}
            updateSearchState={updateSearchState}/>
        </Accordion.Body>
      </Accordion.Item>
      <Accordion.Item eventKey={'custom'} key={'custom'}>
        <Accordion.Header>Custom Search Expression</Accordion.Header>
        <Accordion.Body>
          <CustomFacet
            studyEnvContext={studyEnvContext}
            searchState={searchState}
            updateSearchState={updateSearchState}/>
        </Accordion.Body>
      </Accordion.Item>

    </Accordion>
  </div>
}

const KeywordFacet = ({ searchState, updateSearchState }: {
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void
}) => {
  return <div>
    <div>
      <input
        className='form-control'
        type="text"
        value={searchState.keywordSearch || ''}
        placeholder='Search by name, email, or shortcode'
        onChange={e => updateSearchState('keywordSearch', e.target.value)}/>
    </div>
  </div>
}

const EnrolleeFacet = ({ studyEnvContext, searchState, updateSearchState }: {
  studyEnvContext: StudyEnvContextT,
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void
}) => {
  const subjectOptions = [
    { label: 'Participant', value: true },
    { label: 'Non-participant (e.g., proxy)', value: false }
  ]
  const consentedOptions = [
    { label: 'Consented', value: true },
    { label: 'Not consented', value: false }
  ]


  const selectedSubjectOption = subjectOptions.find(o => o.value === searchState.subject)
  const selectedConsentOption = consentedOptions.find(o => o.value === searchState.consented)
  return <div>
    {studyEnvContext.currentEnv.studyEnvironmentConfig.acceptingProxyEnrollment
      && <>
        <label>User type</label>
        <Select
          options={subjectOptions}
          isClearable={true}
          value={selectedSubjectOption ? selectedSubjectOption : null}
          onChange={selectedOption => {
            updateSearchState('subject', selectedOption?.value)
          }}
        />
      </>}
    <label className={'mt-2'}>Consented</label>
    <Select
      options={consentedOptions}
      isClearable={true}
      value={selectedConsentOption ? selectedConsentOption : null}
      onChange={selectedOption => {
        updateSearchState('consented', selectedOption?.value)
      }}
    />
  </div>
}

const AgeFacet = ({ searchState, updateSearchState }: {
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void
}) => {
  return <div className='d-flex flex-row align-items-center justify-content-center'>
    <input
      className='form-control form-control-sm w-25'
      type="number"
      value={searchState.minAge || ''}
      placeholder='Min age'
      onChange={e => updateSearchState('minAge', toNumber(e.target.value))}
    />
    <span className='mx-2'> to </span>
    <input
      className='form-control form-control-sm w-25'
      type="number"
      value={searchState.maxAge || ''}
      placeholder='Max age'
      onChange={e => updateSearchState('maxAge', toNumber(e.target.value))}/>
  </div>
}

const SexAssignedAtBirthFacet = ({ searchState, updateSearchState }: {
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void
}) => {
  return <div data-testid='select-sex-at-birth'>
    <Creatable
      isMulti={true}
      options={[
        { label: 'female', value: 'female' },
        { label: 'male', value: 'male' }
      ]}
      value={searchState.sexAtBirth.map(s => ({ label: s, value: s }))}
      onChange={selectedOptions => {
        updateSearchState('sexAtBirth', selectedOptions.map(o => o.value))
      }}
    />
  </div>
}

const TaskStatusFacet = ({ studyEnvContext, searchState, updateSearchState }: {
  studyEnvContext: StudyEnvContextT,
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void
}) => {
  const statusOptions = [
    { label: 'New', value: 'NEW' },
    { label: 'Viewed', value: 'VIEWED' },
    { label: 'In progress', value: 'IN_PROGRESS' },
    { label: 'Complete', value: 'COMPLETE' },
    { label: 'Rejected', value: 'REJECTED' }
  ]

  return <div>
    {
      studyEnvContext.currentEnv.configuredSurveys.map(
        configuredSurvey => {
          const stableId = configuredSurvey.survey.stableId
          const name = configuredSurvey.survey.name
          const selectedStatus = searchState.tasks.find(task => task.task === stableId)?.status

          return <div className={'mb-2'} key={selectedStatus ? stableId + selectedStatus : stableId}>
            <label>{name}</label>
            <div data-testid={`select-${stableId}-task-status`}>
              <Select
                key={stableId}
                aria-label={`Select status for ${name}`}
                options={statusOptions}
                value={statusOptions.find(opt => opt.value == selectedStatus)}
                onChange={selectedOption => {
                  const newSelectedStatus = selectedOption?.value
                  if (newSelectedStatus) {
                    updateSearchState('tasks', [
                      ...searchState.tasks.filter(task => task.task !== stableId),
                      { task: stableId, status: newSelectedStatus }
                    ])
                  } else {
                    updateSearchState('tasks', searchState.tasks.filter(task => task.task !== stableId))
                  }
                }}
              />
            </div>
          </div>
        }
      )
    }
  </div>
}

const LatestKitFacet = ({ searchState, updateSearchState }: {
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void
}) => {
  const statusOptions = [
    { label: 'New', value: 'NEW' },
    { label: 'Created', value: 'CREATED' },
    { label: 'Queued', value: 'QUEUED' },
    { label: 'Sent', value: 'SENT' },
    { label: 'Received', value: 'RECEIVED' },
    { label: 'Errored', value: 'ERRORED' },
    { label: 'Deactivated', value: 'DEACTIVATED' },
    { label: 'Unknown', value: 'UNKNOWN' }
  ]
  return <div>
    <Select
      isMulti={true}
      options={statusOptions}
      value={searchState.latestKitStatus.map(s => statusOptions.find(o => o.value === s) || { label: s, value: s })}
      onChange={selectedOptions => {
        updateSearchState('latestKitStatus', selectedOptions.map(o => o.value))
      }}
    />
  </div>
}

const CustomFacet = ({ studyEnvContext, searchState, updateSearchState }: {
  studyEnvContext: StudyEnvContextT,
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void
}) => {
  return <div>
    <LazySearchQueryBuilder
      studyEnvContext={studyEnvContext}
      onSearchExpressionChange={val => updateSearchState('custom', val)}
      searchExpression={searchState.custom}/>
  </div>
}
