import { concatSearchExpressions } from 'util/searchExpressionUtils'

export type ExportFilterOptions = {
  includeProxiesAsRows?: boolean,
  includeUnconsented?: boolean,
  enrolledBefore?: Date,
  enrolledAfter?: Date
}

/**
 * Builds a filter string for the export API. Defaults to only include consented subjects.
 */
export const buildFilter = (
  opts: ExportFilterOptions = {
    includeProxiesAsRows: false,
    includeUnconsented: false,
    enrolledBefore: undefined,
    enrolledAfter: undefined
  }): string => {
  const facets: string[] = []
  if (!opts.includeProxiesAsRows) {
    facets.push('{enrollee.subject} = true')
  }
  if (!opts.includeUnconsented) {
    facets.push('{enrollee.consented} = true')
  }
  if (opts.enrolledBefore) {
    facets.push(`{enrollee.createdAt} < '${opts.enrolledBefore.toISOString()}'`)
  }
  if (opts.enrolledAfter) {
    facets.push(`{enrollee.createdAt} > '${opts.enrolledAfter.toISOString()}'`)
  }
  return concatSearchExpressions(facets)
}
