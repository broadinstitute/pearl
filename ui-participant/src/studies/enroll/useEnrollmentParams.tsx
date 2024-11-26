import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Answer } from '@juniper/ui-core'

export function useEnrollmentParams() {
  const [searchParams] = useSearchParams()
  const [skipPreEnroll, setSkipPreEnroll] = useState(() => {
    return sessionStorage.getItem('skipPreEnroll') === 'true'
  })
  const [referralSource, setReferralSource] = useState<string | null>(() => {
    return sessionStorage.getItem('referralSource')
  })
  const [isProxyEnrollment, setIsProxyEnrollment] = useState(false)
  const [ppUserId, setPpUserId] = useState<string | null>(null)
  const [preFilledAnswers, setPreFilledAnswers] = useState<Answer[]>(
    parsePreFilledAnswers(sessionStorage.getItem('preFilledAnswers')))

  useEffect(() => {
    const skipPreEnrollParam = searchParams.get('skipPreEnroll') === 'true'
    const referralSourceParam = searchParams.get('referralSource')
    const proxyEnrollmentParam = searchParams.get('isProxyEnrollment') === 'true'
    const ppUserIdParam = searchParams.get('ppUserId')
    const preFilledAnswersParam = searchParams.get('preFilledAnswers')

    if (proxyEnrollmentParam) {
      setIsProxyEnrollment(true)
    }

    if (ppUserIdParam) {
      setPpUserId(ppUserIdParam)
    }

    if (skipPreEnrollParam) {
      sessionStorage.setItem('skipPreEnroll', 'true')
      setSkipPreEnroll(true)
    }

    if (referralSourceParam) {
      sessionStorage.setItem('referralSource', referralSourceParam)
      setReferralSource(referralSourceParam)
    }

    if (preFilledAnswersParam) {
      const answers = parsePreFilledAnswers(preFilledAnswersParam)
      sessionStorage.setItem('preFilledAnswers', JSON.stringify(answers))
      setPreFilledAnswers(answers)
    }
  }, [searchParams])

  return { skipPreEnroll, referralSource, isProxyEnrollment, ppUserId, preFilledAnswers }
}

const parsePreFilledAnswers = (preFilledAnswers: string | null): Answer[] => {
  if (!preFilledAnswers) {
    return []
  }

  try {
    return JSON.parse(preFilledAnswers) as Answer[]
  } catch (error) {
    console.error('Failed to parse preFilledAnswers:', error)
    return []
  }
}
