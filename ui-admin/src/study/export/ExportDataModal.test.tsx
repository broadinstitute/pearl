import React from 'react'

import { screen } from '@testing-library/react'
import ExportOptionsForm from './ExportOptionsForm'
import { userEvent } from '@testing-library/user-event'
import { renderWithRouter } from '@juniper/ui-core'
import { DEFAULT_EXPORT_OPTS } from './ExportDataBrowser'

test('renders the file types', async () => {
  renderWithRouter(<ExportOptionsForm exportOptions={DEFAULT_EXPORT_OPTS} setExportOptions={jest.fn()}/>)
  expect(screen.getByText('Tab-delimited (.tsv)')).toBeInTheDocument()
  expect(screen.getByText('Excel (.xlsx)')).toBeInTheDocument()
})

test('consented filter options work with search string', async () => {
  const setOptionsSpy = jest.fn()
  renderWithRouter(<ExportOptionsForm exportOptions={DEFAULT_EXPORT_OPTS} setExportOptions={setOptionsSpy}/>)
  await userEvent.click(screen.getByText('Advanced Options'))
  await userEvent.click(screen.getByLabelText('Include proxies as rows'))
  expect(setOptionsSpy).toHaveBeenCalledWith(expect.objectContaining({ filterString: '{enrollee.consented} = true' }))
})

test('proxy filter options work with search string', async () => {
  const setOptionsSpy = jest.fn()
  renderWithRouter(<ExportOptionsForm exportOptions={DEFAULT_EXPORT_OPTS} setExportOptions={setOptionsSpy}/>)
  await userEvent.click(screen.getByText('Advanced Options'))
  await userEvent.click(screen.getByLabelText('Include enrollees who have not consented'))
  expect(setOptionsSpy).toHaveBeenCalledWith(expect.objectContaining({ filterString: '{enrollee.subject} = true' }))
})
