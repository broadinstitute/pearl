import React from 'react'
import { emptyStudyEnvChange } from 'util/publishUtils'
import StudyEnvDiff from './StudyEnvDiff'
import { screen } from '@testing-library/react'
import { mockPortal, renderInPortalRouter } from 'test-utils/mocking-utils'

test('StudyEnvDiff renders the name of the study', () => {
  renderInPortalRouter(mockPortal(), <StudyEnvDiff studyName="Test Study"
    studyEnvChange={emptyStudyEnvChange}
    selectedChanges={emptyStudyEnvChange}
    setSelectedChanges={jest.fn()}
  />)
  expect(screen.getByText('Test Study')).toBeInTheDocument()
})
