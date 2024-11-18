import { useEffect, useRef } from 'react'

/** Executes the save function at the specified interval, but only starting the interval after the
 * previous call returns, guaranteed no overlapping saves */
export function useAutosaveEffect(saveFn: () => void, autoSaveInterval: number) {
  const timeoutHandleRef = useRef<number | null>(null)

  useEffect(() => {
    // run saveFn at the specified interval
    (function loop() {
      timeoutHandleRef.current = window.setTimeout(() => {
        saveFn()
        loop()
      }, autoSaveInterval)
    })()

    return () => {
      if (timeoutHandleRef.current !== null) {
        window.clearTimeout(timeoutHandleRef.current)
      }
    }
  }, [saveFn])

  // Return a function to cancel any queued saveFn
  return () => {
    if (timeoutHandleRef.current !== null) {
      window.clearTimeout(timeoutHandleRef.current)
    }
  }
}
