import classNames from 'classnames'
import _ from 'lodash'
import React from 'react'
import ReactMarkdown from 'react-markdown'

import { ButtonConfig } from 'api/api'
import PearlImage, { PearlImageConfig } from 'util/PearlImage'
import { getSectionStyle } from 'util/styleUtils'

import ConfiguredButton from './ConfiguredButton'

type HeroWithImageTemplateConfig = {
  blurb?: string, //  text below the title
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  fullWidth?: boolean, // span the full page width or not
  image?: PearlImageConfig, // image
  imagePosition?: string, // left or right.  Default is right
  imageWidthPercentage?: number, // number between 0 and 100. Percentage of row width given to image.
  logos?: PearlImageConfig[],
  title?: string // large heading text
}

type HeroWithImageTemplateProps = {
  anchorRef?: string
  config: HeroWithImageTemplateConfig
}

/**
 * Template for a hero with text content on the left and an image on the right.
 */
function HeroWithImageTemplate(props: HeroWithImageTemplateProps) {
  const { anchorRef, config } = props
  const {
    blurb,
    buttons,
    fullWidth = false,
    image,
    imagePosition,
    imageWidthPercentage: configuredImageWidthPercentage,
    logos,
    title
  } = config

  const isLeftImage = imagePosition === 'left' // default is right, so left has to be explicitly specified
  const imageWidthPercentage = _.isNumber(configuredImageWidthPercentage)
    ? _.clamp(configuredImageWidthPercentage, 0, 100)
    : (fullWidth ? 50 : 33)
  const imageCols = Math.max(Math.floor(imageWidthPercentage / 100 * 12), 1)

  return (
    <div
      className={classNames('row', 'mx-0', isLeftImage ? 'flex-row' : 'flex-row-reverse')}
      id={anchorRef}
      style={getSectionStyle(config)}
    >
      <div
        className={classNames(
          'row',
          'col-12',
          fullWidth ? 'mx-0' : 'col-sm-10 mx-auto',
          isLeftImage ? 'flex-row' : 'flex-row-reverse'
        )}
      >
        {!!image && (
          <div
            className={classNames(
              'col-12', `col-lg-${imageCols}`,
              'd-flex justify-content-center align-items-center p-0'
            )}
          >
            <PearlImage image={image} className="img-fluid"/>
          </div>
        )}
        <div
          className={classNames(
            'col-12', `col-lg-${12 - imageCols}`,
            'py-3 p-sm-3 p-lg-5',
            'd-flex flex-column flex-grow-1 justify-content-around'
          )}
        >
          {!!title && (
            <h1 className="fs-1 fw-normal lh-sm">
              <ReactMarkdown>{title}</ReactMarkdown>
            </h1>
          )}
          {!!blurb && (
            <div className="fs-4">
              <ReactMarkdown>{blurb}</ReactMarkdown>
            </div>
          )}
          {(buttons || []).length > 0 && (
            <div className="d-grid gap-2 d-md-flex justify-content-md-start">
              {
                _.map(buttons, (buttonConfig, i) =>
                  <ConfiguredButton key={i} config={buttonConfig} className="btn-lg px-4 me-md-2"/>
                )
              }
            </div>
          )}
          {(logos || []).length > 0 && (
            <div
              className={classNames(
                'd-flex',
                'flex-column align-items-center',
                'flex-sm-row align-items-sm-start flex-sm-wrap'
              )}
            >
              {_.map(logos, logo => {
                return <PearlImage key={logo.cleanFileName} image={logo} className="mt-4 me-sm-4" />
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default HeroWithImageTemplate
