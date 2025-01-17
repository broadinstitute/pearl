import React from 'react'
import { isEmpty } from 'lodash'
import classNames from 'classnames'


/**
 * Stylized card component for displaying read-only or editable data.
 * The components in this file can/should be nested similarly to the
 * React modal component.
 *
 * Example usage:
 * ```tsx
 * <InfoCard>
 *   <InfoCardHeader>
 *     <InfoCardTitle title={'Basic Information'}/>
 *   </InfoCardHeader>
 *   <InfoCardBody>
 *     <InfoCardValue
 *       title={'Name'}
 *       values={[formatName(enrollee.profile)]}
 *     />
 *     <InfoCardValue
 *       title={'Birthdate'}
 *       values={[dateToDefaultString(enrollee.profile.birthDate) || '']}
 *     />
 *   </InfoCardBody>
 * </InfoCard>
 * ```
 *
 * If you want to customize the way a row looks, you can use the `InfoCardRow` component
 * directly. For example:
 * ```tsx
 * <InfoCard>
 *   <InfoCardHeader>
 *     <InfoCardTitle title={'Basic Information'}/>
 *   </InfoCardHeader>
 *   <InfoCardBody>
 *     <InfoCardValue
 *       title={'Name'}
 *       values={[formatName(enrollee.profile)]}
 *     />
 *     <InfoCardRow title={'Birthdate'}>
 *         <p>My custom birthday row:</p>
 *         <p>{dateToDefaultString(enrollee.profile.birthDate) || ''}</p>
 *     <InfoCardRow/>
 *   </InfoCardBody>
 * </InfoCard>
 * ```
 */
export function InfoCard({ children }: { children: React.ReactNode }) {
  return <div className="card w-100 border shadow-sm mb-3">
    {children}
  </div>
}

/**
 * Header of the card, usually used with CardTitle to display the title of the card.
 */
export function InfoCardHeader({ children }: { children: React.ReactNode }) {
  return <div className="card-header border-bottom d-flex flex-row align-items-center"
    style={{ backgroundColor: '#ededed' }}>
    {children}
  </div>
}

/**
 * Title of the card.
 */
export function InfoCardTitle({ title }: { title: string }) {
  return <div className="fw-bold lead my-1">{title}</div>
}

/**
 * Body of the card, usually used with CardRow or CardValueRow to display the data.
 */
export function InfoCardBody({ children }: { children: React.ReactNode }) {
  return <div className="card-body d-flex flex-row flex-wrap">
    {children}
  </div>
}

/**
 * One row of data in the card, where the title is on the left-hand side and the values are on the right.
 */
export function InfoCardRow(
  { title, children, condensed }: {
        title: string,
        children: React.ReactNode,
        condensed?: boolean
    }
) {
  const marginBottom = condensed ? 'mb-2' : 'mb-4'
  return <>
    <div className={classNames('w-25 fw-bold mt-2', marginBottom)} aria-label={title}>
      {title}
    </div>
    <div className={classNames('w-75', marginBottom)}>
      {children}
    </div>
  </>
}


/**
 * Row of readonly data, where the title takes the leftmost portion and the values are on the rightmost.
 * If the value(s) provided are empty, then "None provided" is displayed.
 */
export function InfoCardValue(
  { title, values, condensed }: {
        title: string,
        values: string[],
        condensed?: boolean
    }
) {
  return <InfoCardRow title={title} condensed={condensed}>
    {(isEmpty(values) || values.every(isEmpty)) && <p className="fst-italic mb-0 mt-2 text-muted">None provided</p>}
    {
      values.filter(val => !isEmpty(val)).map((val, idx) => (
        <p key={idx} className={`mb-0 ${idx == 0 ? 'mt-2' : ''}`}>{val}</p>
      ))
    }
  </InfoCardRow>
}
