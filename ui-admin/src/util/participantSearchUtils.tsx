import { isEmpty } from 'lodash/fp'
import { concatSearchExpressions } from './searchExpressionUtils'
import { useState } from 'react'
import { isArray } from 'lodash'

// reminder: if you add a new field to the search state,
// make sure to update the toExpression function
export type ParticipantSearchState = {
  basicSearch: string,
  subject?: boolean, // defaults to true, but is nullable in case you want to see everything
  consented?: boolean,
  minAge?: number,
  maxAge?: number,
  sexAtBirth: string[],
  tasks: { task: string, status: string }[],
  latestKitStatus: string[],
  custom: string
}

export const DefaultParticipantSearchState: ParticipantSearchState = {
  basicSearch: '',
  subject: true, // defaults to true, but is nullable in case you want to see everything
  sexAtBirth: [],
  tasks: [],
  latestKitStatus: [],
  custom: ''
}


export const ParticipantSearchStateLabels: { [key in keyof ParticipantSearchState]: string } = {
  basicSearch: 'Basic search',
  subject: 'User type',
  consented: 'Consented',
  minAge: 'Min age',
  maxAge: 'Max age',
  sexAtBirth: 'Sex at birth',
  tasks: 'Tasks',
  latestKitStatus: 'Latest Kit',
  custom: 'Expression'
}

/**
 * Hook for managing the participant search state.
 */
export const useParticipantSearchState = (initialState: ParticipantSearchState = DefaultParticipantSearchState) => {
  const [searchState, setSearchState] = useState<ParticipantSearchState>(initialState)

  const searchExpression = toExpression(searchState)

  const updateSearchState = (field: keyof ParticipantSearchState, value: unknown) => {
    setSearchState(oldState => {
      return { ...oldState, [field]: value }
    })
  }

  return { searchState, setSearchState, searchExpression, updateSearchState }
}

/**
 * Converts the search state to a search expression.
 */
export const toExpression = (searchState: ParticipantSearchState) => {
  const expressions: string[] = []
  if (!isEmpty(searchState.basicSearch)) {
    expressions.push(`({profile.name} contains '${searchState.basicSearch}' `
      + `or {profile.contactEmail} contains '${searchState.basicSearch}' `
      + `or {enrollee.shortcode} contains '${searchState.basicSearch}')`)
  }

  if (searchState.subject !== undefined) {
    expressions.push(`{enrollee.subject} = ${searchState.subject}`)
  }

  if (searchState.consented !== undefined) {
    expressions.push(`{enrollee.consented} = ${searchState.consented}`)
  }

  if (searchState.minAge) {
    expressions.push(`{age} >= ${searchState.minAge}`)
  }

  if (searchState.maxAge) {
    expressions.push(`{age} <= ${searchState.maxAge}`)
  }

  if (searchState.sexAtBirth.length > 0) {
    const sexAtBirthExpression = `(${
      concatSearchExpressions(
        searchState.sexAtBirth.map((sexAtBirth: string) => {
          return `{profile.sexAtBirth} = '${sexAtBirth}'`
        }),
        'or')
    })`

    expressions.push(sexAtBirthExpression)
  }

  if (searchState.tasks.length > 0) {
    const taskExpressions = `(${
      concatSearchExpressions(
        searchState.tasks.map(({ task, status }) => {
          return `{task.${task}.status} = '${status}'`
        }))
    })`

    expressions.push(taskExpressions)
  }

  if (searchState.latestKitStatus.length > 0) {
    const latestKitStatusExpression = `(${
      concatSearchExpressions(
        searchState.latestKitStatus.map((kitStatus: string) => {
          return `{latestKit.status} = '${kitStatus}'`
        }),
        'or')
    })`

    expressions.push(latestKitStatusExpression)
  }

  if (!isEmpty(searchState.custom)) {
    expressions.push(`(${searchState.custom})`)
  }

  return concatSearchExpressions(expressions)
}

/**
 * Returns the search expression state as a list of human-readable facets.
 */
export const getFacets = (searchState: ParticipantSearchState, opts?: { includeBasicSearch: boolean }): {
  label: string,
  value: string
}[] => {
  const facets = []

  for (const [key, value] of Object.entries(searchState)) {
    if (value !== DefaultParticipantSearchState[key as keyof ParticipantSearchState]) {
      if (key === 'basicSearch' && !opts?.includeBasicSearch) {
        continue
      }

      if (key === 'tasks') {
        for (const task of value as { task: string, status: string }[]) {
          facets.push({ label: task.task, value: task.status })
        }
      } else {
        facets.push({
          label: ParticipantSearchStateLabels[key as keyof ParticipantSearchState] || key,
          value: getValueAsString(key as keyof ParticipantSearchState, value)
        })
      }
    }
  }

  return facets
}

const getValueAsString = (key: keyof ParticipantSearchState, value: string | number | boolean | {
  task: string,
  status: string
}[] | string[]) => {
  if (key === 'subject') {
    if (value === undefined || value === null) {
      return 'Any'
    } else {
      return value ? 'Participant' : 'Non-Participant (e.g., proxy)'
    }
  }

  if (isArray(value)) {
    return value.join(', ')
  }

  return value.toString()
}