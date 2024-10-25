import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faWrench } from '@fortawesome/free-solid-svg-icons'
import { faClipboardCheck } from '@fortawesome/free-solid-svg-icons/faClipboardCheck'
import { faUsers } from '@fortawesome/free-solid-svg-icons/faUsers'

/** renders a page header with the appropriate styling and spacing */
export const renderPageHeader = (content: React.ReactNode) => {
  return <div className="d-flex mb-2">
    <h2 className="fw-bold">{content}</h2>
  </div>
}

/** returns a span with either the string as-is, or truncated and ellipsesed if over the maxLength */
export const renderTruncatedText = (text: string, maxLength: number) => {
  if (text && text.length > maxLength) {
    const truncatedText = `${text.substring(0, 100)  }...`
    return <span title={text}>{truncatedText}</span>
  }
  return <span>{text}</span>
}
