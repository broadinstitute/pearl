import React from 'react'

import { SectionConfig } from 'api/api'
import PearlImage, { PearlImageConfig, validatePearlImageConfig } from 'util/PearlImage'
import { withValidatedSectionConfig } from 'util/withValidatedSectionConfig'

type BannerImageConfig = {
  image: PearlImageConfig
}

const validateBannerImageConfig = (config: SectionConfig): BannerImageConfig => {
  const image = validatePearlImageConfig(config.image)
  return { image }
}

type BannerImageProps = {
  config: BannerImageConfig
}

/**
 * Template for a full width banner image.
 */
function BannerImage(props: BannerImageProps) {
  const {
    config: {
      image
    }
  } = props

  return (
    <div className="row mx-0">
      {!!image && (
        <div className="col-12 p-0">
          <PearlImage image={image} style={{ width: '100%', height: 'auto' }} />
        </div>
      )}
    </div>
  )
}

export default withValidatedSectionConfig(validateBannerImageConfig, BannerImage)
