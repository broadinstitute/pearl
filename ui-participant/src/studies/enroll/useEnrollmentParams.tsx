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

  function clearStoredEnrollmentParams() {
    sessionStorage.removeItem('skipPreEnroll')
    sessionStorage.removeItem('referralSource')
    sessionStorage.removeItem('preFilledAnswers')
  }

  function captureEnrollmentParams() {
    const skipPreEnrollParam = searchParams.get('skipPreEnroll')
    const referralSourceParam = searchParams.get('referralSource')
    const proxyEnrollmentParam = searchParams.get('isProxyEnrollment')
    const ppUserIdParam = searchParams.get('ppUserId')
    const preFilledAnswersParam = searchParams.get('preFilledAnswers')

    if (proxyEnrollmentParam === 'true') {
      setIsProxyEnrollment(true)
    }

    if (ppUserIdParam) {
      setPpUserId(ppUserIdParam)
    }

    // The following three params are stored in sessionStorage, so they persist across page reloads.
    // We want to do our best to avoid losing any information coming from referring sites

    if (skipPreEnrollParam === 'true') {
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
  }

  useEffect(() => {
    captureEnrollmentParams()
  }, [searchParams])

  return {
    skipPreEnroll, referralSource, isProxyEnrollment, ppUserId, preFilledAnswers,
    captureEnrollmentParams, clearStoredEnrollmentParams
  }
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
