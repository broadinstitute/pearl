import classNames from 'classnames'
import _ from 'lodash'
import React from 'react'
import ReactMarkdown from 'react-markdown'

const idFor = (question: string): string => {
  return _.kebabCase(question)
}

const targetFor = (question: string): string => {
  return `#${idFor(question)}`
}

type FaqQuestion = {
  question: string,
  answer: string
}

type FrequentlyAskedQuestionsProps = {
  backgroundColor?: string, // background color for the block
  blurb?: string, //  text below the title
  questions?: FaqQuestion[], // the questions
  title?: string, // large heading text
  color?: string // foreground text color
}

/**
 * Template for rendering a Frequently Asked Questions block.
 */
function FrequentlyAskedQuestionsTemplate({
  anchorRef,
  config: {
    backgroundColor,
    blurb,
    color,
    questions,
    title = 'Frequently Asked Questions'
  }
}: { anchorRef?: string, config: FrequentlyAskedQuestionsProps }) {
  return <div id={anchorRef} className="row mx-0 justify-content-center" style={{ backgroundColor, color }}>
    <div className="col-12 col-sm-8 col-lg-6">
      <h1 className="fs-1 fw-normal lh-sm mt-5 mb-4 text-center">{title}</h1>
      <div className='fs-5 fw-normal mb-4 text-center'>
        {blurb && <ReactMarkdown>{blurb}</ReactMarkdown>}
      </div>
      <ul className="mx-0 px-0 border-top" style={{ listStyle: 'none' }}>
        {
          _.map(questions, ({ question, answer }, i) => {
            return <li key={i} className="border-bottom">
              <button
                type="button"
                className={classNames(
                  'btn btn-link btn-lg',
                  'w-100 py-3 px-0 px-sm-2',
                  'd-flex',
                  'text-black fw-bold text-start text-decoration-none'
                )}
                data-bs-toggle="collapse" data-bs-target={targetFor(question)}>
                <span aria-hidden="true" className="me-2">+ </span>
                {question}
              </button>
              <div className="collapse" id={idFor(question)}>
                <p>
                  {answer}
                </p>
              </div>
            </li>
          })
        }
      </ul>
    </div>
  </div>
}

export default FrequentlyAskedQuestionsTemplate
