import _ from 'lodash'
import React, { CSSProperties } from 'react'
import { ButtonConfig, getImageUrl } from 'api/api'
import PearlImage, { PearlImageProps } from '../../util/PearlImage'
import ConfiguredButton from './ConfiguredButton'
import ReactMarkdown from 'react-markdown'

type HeroLeftWithImageTemplateProps = {
  background?: string, // background CSS style (e.g. `linear-gradient(...)`)
  backgroundColor?: string, // background color for the block
  backgroundImage?: PearlImageProps, // background image
  blurb?: string, //  text below the title
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  image?: PearlImageProps, // image
  imagePosition?: string, // left or right.  Default is right
  logos?: PearlImageProps[]
}

/**
 * Template for a hero with text content on the left and an image on the right.
 */
function HeroWithImageTemplate({
  config: {
    background,
    blurb,
    buttons,
    image,
    imagePosition,
    backgroundImage,
    logos,
    title
  }
}: { config: HeroLeftWithImageTemplateProps }) {
  const styleProps: CSSProperties = { background }
  if (backgroundImage) {
    styleProps.backgroundImage = `url('${getImageUrl(backgroundImage.cleanFileName, backgroundImage.version)}')`
  }
  const isLeftImage = imagePosition === 'left' // default is right, so left has to be explicitly specified
  return <div className="row" style={styleProps}>
    <div className={`col-12 d-flex ${isLeftImage ? 'flex-row' : 'flex-row-reverse'}`}>
      <div>
        <PearlImage image={image} className="img-fluid"/>
      </div>
      <div className="p-5 d-flex flex-column flex-grow-1 justify-content-around" style={{ minWidth: '50%' }}>
        <h1 className="fs-1 fw-normal lh-sm">
          <ReactMarkdown>{title ? title : ''}</ReactMarkdown>
        </h1>
        <div className="fs-5">
          <ReactMarkdown>{blurb ? blurb : ''}</ReactMarkdown>
        </div>
        <div className="d-grid gap-2 d-md-flex justify-content-md-start">
          {
            _.map(buttons, (buttonConfig, i) =>
              <ConfiguredButton key={i} config={buttonConfig} className="btn btn-primary btn-lg px-4 me-md-2"/>
            )
          }
        </div>
        <div className="d-flex flex-wrap align-items-center justify-content-between">
          {_.map(logos, logo => {
            return <PearlImage key={logo.cleanFileName} image={logo} className={'m-1'}/>
          })}
        </div>
      </div>
    </div>
  </div>
}

export default HeroWithImageTemplate
