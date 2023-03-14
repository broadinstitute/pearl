import _ from 'lodash'
import React from 'react'
import { ButtonConfig } from 'api/api'
import PearlImage from '../../util/PearlImage'

type SocialMediaTemplateConfig = {
  blurb?: string, //  text below the title
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  facebookHref?: string, // URL of Facebook page
  instagramHref?: string, // URL of Instagram page
  twitterHref?: string, // URL of Twitter page
}

type SocialMediaTemplateProps = {
  anchorRef?: string
  config: SocialMediaTemplateConfig
}

/**
 * Template for a hero with social media links.
 * TODO -- implement images
 */
function SocialMediaTemplate(props: SocialMediaTemplateProps) {
  const {
    anchorRef,
    config: {
      blurb,
      buttons,
      facebookHref,
      instagramHref,
      title,
      twitterHref
    }
  } = props

  return <div id={anchorRef} className="container py-5">
    <div className="d-flex justify-content-center mt-5 mb-4">
      {twitterHref &&
        <PearlImage image={{ cleanFileName: 'twitter.png', version: 1, alt: 'Twitter' }}
          className="m-3" style={{ width: '56px' }}/>
      }
      {facebookHref &&
        <PearlImage image={{ cleanFileName: 'facebook.png', version: 1, alt: 'Facebook' }}
          className="m-3" style={{ width: '54px' }}/>
      }
      {instagramHref &&
        <PearlImage image={{ cleanFileName: 'instagram.png', version: 1, alt: 'Instagram' }}
          className="m-3" style={{ width: '49px' }}/>
      }
    </div>
    <h1 className="fs-1 fw-normal lh-sm text-center">
      {title}
    </h1>
    <p className="fs-5 fw-normal text-center">
      {blurb}
    </p>
    <div className="d-grid gap-2 d-sm-flex justify-content-sm-center">
      {
        _.map(buttons, ({ text, href }) => {
          return <a href={href} role={'button'} className="btn btn-primary btn-lg px-4 me-md-2">{text}</a>
        })
      }
    </div>
  </div>
}

export default SocialMediaTemplate
