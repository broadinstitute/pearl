
import { useAutosaveEffect } from './autoSaveUtils'
import { act, renderHook } from '@testing-library/react'

jest.useFakeTimers()

describe('useAutosaveEffect', () => {
  it('should allow an autosave saveFn to be cancelled', () => {
    const saveFn = jest.fn()
    const autoSaveInterval = 1000

    const { result } = renderHook(() => useAutosaveEffect(saveFn, autoSaveInterval))

    // Fast-forward until the first autosave is called
    act(() => { jest.advanceTimersByTime(autoSaveInterval) })

    // Verify the autosave was called
    expect(saveFn).toHaveBeenCalledTimes(1)

    // Call the cancel function
    act(() => { result.current() })

    // Fast-forward again (advance by 2 intervals to be extra sure)
    act(() => { jest.advanceTimersByTime(autoSaveInterval * 2) })

    // Verify the autosave function was not called again after being cancelled
    expect(saveFn).toHaveBeenCalledTimes(1)
  })
})
