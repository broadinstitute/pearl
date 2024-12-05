import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCaretUp, faExpand, faMinus } from '@fortawesome/free-solid-svg-icons'
import React, { useState } from 'react'
import { Portal, Study } from '@juniper/ui-core'
import Api, { BasicMetricDatum } from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { studyEnvMetricsPath, useStudyEnvParamsFromPath } from 'study/StudyEnvironmentRouter'
import { Link } from 'react-router-dom'
import classNames from 'classnames'
import { InfoCard, InfoCardBody, InfoCardHeader } from 'components/InfoCard'

export const StudyTrendsWidget = ({ portal, study }: { portal: Portal, study: Study }) => {
  const [enrollments, setEnrollments] = useState<BasicMetricDatum[]>([])
  const [consents, setConsents] = useState<BasicMetricDatum[]>([])
  const [surveysCompleted, setSurveysCompleted] = useState<BasicMetricDatum[]>([])

  const { isLoading } = useLoadingEffect(async () => {
    await getMetricsLast7Days(study, 'STUDY_ENROLLMENT', setEnrollments)
    await getMetricsLast7Days(study, 'STUDY_ENROLLEE_CONSENTED', setConsents)
    await getMetricsLast7Days(study, 'STUDY_SURVEY_COMPLETION', setSurveysCompleted)
  }, [])

  const getMetricsLast7Days = async (
    study: Study, metric: string, setMetricData: (data: BasicMetricDatum[], envName: string) => void
  ) => {
    const result = await Api.fetchMetric(portal.shortcode, study.shortcode, 'live', metric)
    // TODO: api doesn't currently honor time ranges, so we'll filter down after fetching
    setMetricData(result.filter(datum => {
      const lastWeek = new Date()
      lastWeek.setDate(lastWeek.getDate() - 7)
      return new Date((datum.time || 0) * 1000) > lastWeek
    }))
  }

  return (
    <InfoCard>
      <InfoCardHeader>
        <div className="d-flex align-items-center justify-content-between w-100">
          <span className="fw-bold text-wrap text-break">{study.name}</span>
          <Link to={studyEnvMetricsPath(portal.shortcode, study.shortcode, 'live')}>
            <Button
              tooltip={'View all study trends'}
              variant="light" className="border">
              <FontAwesomeIcon icon={faExpand} className="fa-lg"/>
            </Button>
          </Link>
        </div>
      </InfoCardHeader>
      <InfoCardBody>
        <LoadingSpinner isLoading={isLoading}>
          <div className={'w-100'}>
            <div className={'fst-italic text-muted mb-2'}>Last 7 days</div>
            <MetricSummary name={'Enrollments'} count={enrollments.length}/>
            <MetricSummary name={'Consents'} count={consents.length}/>
            <MetricSummary name={'Surveys Completed'} count={surveysCompleted.length}/>
          </div>
        </LoadingSpinner>
      </InfoCardBody>
    </InfoCard>
  )
}

const MetricSummary = ({ name, count } : { name: string, count: number }) => (
  <div className="d-flex justify-content-between mb-1">
    <span>{name}</span>
    <span className="fw-bold">
      {count}<FontAwesomeIcon className={classNames('ms-1', {
        'text-success': count > 0,
        'text-secondary': count === 0,
        'text-danger': count < 0
      })}
      icon={count > 0 ? faCaretUp : faMinus}/>
    </span>
  </div>
)
