import { useNavContext } from '../navbar/NavContextProvider'
import React, { useEffect, useState } from 'react'
import { Portal, PortalStudy, StudyEnvironmentSurvey } from '@juniper/ui-core'
import Modal from 'react-bootstrap/Modal'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { Button } from '../components/forms/Button'
import Select from 'react-select'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import useReactSingleSelect from '../util/react-select-utils'
import Api from '../api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { doApiLoad } from 'api/api-utils'

type CohortCriteriaOperator =
  'EQUALS' |
  'NOT_EQUALS' |
  'CONTAINS' |
  'NOT_CONTAINS' |
  'GREATER_THAN' |
  'GREATER_THAN_EQUAL' |
  'LESS_THAN' |
  'LESS_THAN_EQUAL'

type CohortCriteria = {
  studyShortcode?: string,
  surveyStableId?: string,
  questionStableId?: string
  operator?: CohortCriteriaOperator
  value?: string
}

/**
 *
 */
export default function CreateNewCohortModal({ onDismiss }: {onDismiss: () => void}) {
  const { portalList } = useNavContext()
  const [cohortName, setCohortName] = useState('')
  const [selectedPortal, setSelectedPortal] = useState<Portal>()
  const [selectedStudy, setSelectedStudy] = useState<PortalStudy>()
  const [cohortCriteria, setCohortCriteria] = useState<CohortCriteria[]>([])
  const [selectedStudySurveys, setSelectedStudySurveys] = useState<StudyEnvironmentSurvey[]>([])
  const [isLoading, setIsLoading] = useState(false)

  const [loadedPortal, setLoadedPortal] = useState<Portal>()

  //TODO: for development, this is currently hardcoded to only use sandbox environments. users will most
  // likely want to work off the live environment when building their cohort, but survey content may differ
  // between envs so there is some UX work to do here to make those differences obvious
  const currentEnv = 'sandbox'

  useEffect(() => {
    if (selectedPortal) {
      doApiLoad(async () => {
        const portal = await Api.getPortal(selectedPortal.shortcode)
        setCohortCriteria([])
        setLoadedPortal(portal)
      }, { setIsLoading })
    }
  }, [selectedPortal])

  useEffect(() => {
    if (selectedStudy && loadedPortal) {
      const selectedPortalStudy = loadedPortal.portalStudies.find(pStudy =>
        pStudy.study.shortcode === selectedStudy.study.shortcode)
      const studyEnv = selectedPortalStudy!.study.studyEnvironments.find(env => env.environmentName === currentEnv)
      setSelectedStudySurveys(studyEnv?.configuredSurveys || [])
    }
  }, [selectedStudy])

  const emptyCriteriaEntry = () => ({
    studyShortcode: selectedStudy?.study.shortcode,
    surveyStableId: undefined,
    questionStableId: undefined,
    operator: undefined,
    value: undefined
  })

  const {
    onChange: portalOnChange, options: portalOptions,
    selectedOption: selectedPortalOption, selectInputId: selectPortalInputId
  } =
    useReactSingleSelect(
      portalList,
      (portal: Portal) => ({ label: portal.name, value: portal }),
      setSelectedPortal,
      selectedPortal)

  const {
    onChange: studyOnChange, options: studyOptions,
    selectedOption: selectedStudyOption, selectInputId: selectStudyInputId
  } =
    useReactSingleSelect(
      selectedPortal?.portalStudies || [],
      (study: PortalStudy) => ({ label: study.study.name, value: study }),
      setSelectedStudy,
      selectedStudy)

  return <Modal show={true} className="modal-xl" onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Create a cohort</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <h5>Cohort Name</h5>
      <span className="text-muted mb-2">Give your cohort a unique name. You can reuse this cohort when targeting
        this same subset of participants for new opportunities.</span>
      <div className="container">
        <div className="row my-3">
          <input type="text" className="form-control" id="cohortName"
            onChange={e => setCohortName(e.target.value)} value={cohortName}/>
        </div>
      </div>
      <h5>Cohort Source</h5>
      <span className="text-muted mb-2">Choose the source of your cohort participants.</span>
      <div className="my-3">
        <label>Portal</label>
        <Select inputId={selectPortalInputId} options={portalOptions}
          value={selectedPortalOption} onChange={portalOnChange}/>
        <label className="mt-3">Study</label>
        <Select isClearable={true} isDisabled={!selectedPortal} inputId={selectStudyInputId} options={studyOptions}
          value={selectedStudyOption} onChange={studyOnChange}/>
      </div>
      <hr/>
      <h5>Cohort Criteria</h5>
      <span className="text-muted mb-2">Choose the criteria for your cohort based on previous
        survey responses from the participants in your studies. You may define as many rules
        as you would like to narrow down your cohort.
      </span>
      <div className="mt-3">
        {/*TODO: this is currently only set up to AND all of the criteria rules,
          * but in reality these will need to support ORs and groupings of criteria*/}
        {cohortCriteria.map((criteria, i) =>
          <CriteriaRow key={i} selectedStudySurveys={selectedStudySurveys} criteria={criteria}/>
        )}
      </div>
      <div className="my-3">
        <LoadingSpinner isLoading={isLoading}>
          <button className="btn btn-secondary" disabled={!selectedStudy} onClick={() =>
            setCohortCriteria([...cohortCriteria, emptyCriteriaEntry()])
          }>
            <FontAwesomeIcon icon={faPlus}/> Add criteria
          </button>
        </LoadingSpinner>
      </div>

    </Modal.Body>
    <Modal.Footer>
      <Button variant="primary"
        disabled={!cohortName || !selectedStudy}
        onClick={() => alert('not yet implemented')}
      >Create</Button>
      <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

/**
 * Returns a row of inputs for a single criteria rule
 */
export const CriteriaRow = ({ criteria, selectedStudySurveys }: {
  criteria: CohortCriteria, selectedStudySurveys: StudyEnvironmentSurvey[]
}) => {
  const [selectedSurvey, setSelectedSurvey] = useState<StudyEnvironmentSurvey>()

  const operatorOptions: Record<string, CohortCriteriaOperator>[] = [
    { label: 'CONTAINS', value: 'CONTAINS' },
    { label: 'NOT_CONTAINS', value: 'NOT_CONTAINS' },
    { label: 'EQUALS', value: 'EQUALS' },
    { label: 'NOT_EQUALS', value: 'NOT_EQUALS' },
    { label: 'GREATER_THAN', value: 'GREATER_THAN' },
    { label: 'GREATER_THAN_EQUAL', value: 'GREATER_THAN_EQUAL' },
    { label: 'LESS_THAN', value: 'LESS_THAN' },
    { label: 'LESS_THAN_EQUAL', value: 'LESS_THAN_EQUAL' }
  ]

  const {
    options: surveyOptions, selectedOption: selectedSurveyOption
  } =
    useReactSingleSelect(
      selectedStudySurveys,
      (survey: StudyEnvironmentSurvey) => ({ label: survey.survey.name, value: survey }),
      setSelectedSurvey,
      selectedSurvey)

  //TODO: this form is still a work in progress, and it also needs to set the criteria object
  return <div className="row">
    <div className="col">
      <label>Survey</label>
      <Select className="mb-2" options={surveyOptions} value={selectedSurveyOption}/>
    </div>
    <div className="col">
      <label>Question</label>
      <Select className="mb-2" options={[]} value={undefined}/>
    </div>
    <div className="col">
      <label>Operator</label>
      <Select className="mb-2" options={operatorOptions} value={undefined}/>
    </div>
    <div className="col">
      <label>Value</label>
      <input type="text" className="form-control"/>
    </div>
  </div>
}
