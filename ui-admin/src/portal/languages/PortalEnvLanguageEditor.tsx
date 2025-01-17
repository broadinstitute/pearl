import React, { useMemo, useState } from 'react'
import { faCheck, faPlus, faX } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import Creatable from 'react-select/creatable'
import {
  PortalEnvironmentLanguage, PortalEnvironmentLanguageOpt
} from '@juniper/ui-core'
import { isEmpty, isNil } from 'lodash'
import { Modal, ModalBody, ModalFooter } from 'react-bootstrap'
import { ColumnDef, getCoreRowModel, useReactTable } from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import { faTrashCan } from '@fortawesome/free-solid-svg-icons/faTrashCan'
import { Button } from 'components/forms/Button'
import { TextInput } from 'components/forms/TextInput'

type EditablePortalEnvironmentLanguage = PortalEnvironmentLanguage & { isEditing: boolean }
type PortalEnvironmentLanguageRow = PortalEnvironmentLanguage | EditablePortalEnvironmentLanguage
const makeEmptyNewItem = () => ({ languageCode: '', languageName: '', isEditing: false, id: '' })

const isEditable = (row: PortalEnvironmentLanguageRow): row is EditablePortalEnvironmentLanguage => {
  return !isNil((row as EditablePortalEnvironmentLanguage).isEditing)
}

/**
 * some example languages options taken from A-T -- we might want to expand these.
 * But a creatable select is used so users can add their own.
 */
const LANGUAGE_OPTIONS: PortalEnvironmentLanguageOpt[] = [{
  'languageName': 'English',
  'languageCode': 'en'
}, {
  'languageName': 'Español',
  'languageCode': 'es'
}, {
  'languageName': 'Deutsch',
  'languageCode': 'de'
}, {
  'languageName': 'हिंदी',
  'languageCode': 'hi'
}, {
  'languageName': 'Pусский',
  'languageCode': 'ru'
}, {
  'languageName': '日本語',
  'languageCode': 'ja'
}, {
  'languageName': 'Italiano',
  'languageCode': 'it'
}, {
  'languageName': 'Française',
  'languageCode': 'fr'
}, {
  'languageName': 'Polski',
  'languageCode': 'pl'
}, {
  'languageName': '中文',
  'languageCode': 'zh'
}, {
  'languageName': 'Türk',
  'languageCode': 'tr'
}]

/**
 * Table which allows viewing, deleting, and creating new answer mappings.
 */
export default function PortalEnvLanguageEditor({ items, setItems, readonly } : {
    items: PortalEnvironmentLanguage[],
    setItems:  React.Dispatch<React.SetStateAction<PortalEnvironmentLanguage[]>>,
  readonly: boolean
  }
) {
  const [itemSelectedForDeletion, setItemSelectedForDeletion] = useState<PortalEnvironmentLanguage | null>(null)
  // state for new item
  const [newItem, setNewItem] =
    useState<EditablePortalEnvironmentLanguage>(makeEmptyNewItem())
  const deleteItem = async () => {
    if (!itemSelectedForDeletion) {
      return
    }
    setItems(items => {
      const filteredItems = items.filter(m => m.languageCode !== itemSelectedForDeletion.languageCode)
      return filteredItems
    })
    setItemSelectedForDeletion(null)
  }

  const addNewItem = (item: PortalEnvironmentLanguage) => {
    setNewItem(makeEmptyNewItem())
    setItems(items => {
      return [...items, item]
    })
  }

  const columns: ColumnDef<PortalEnvironmentLanguage>[] = useMemo(() => {
    const baseCols: ColumnDef<PortalEnvironmentLanguage>[] = [{
      header: 'Name',
      accessorKey: 'languageName',
      cell: ({ row }) => {
        const value = row.original.languageName
        if (isEditable(row.original)) {
          return row.original.isEditing && <Creatable
            aria-label={'Language name'}
            options={LANGUAGE_OPTIONS.map(name => {
              return {
                value: name.languageName,
                label: name.languageName
              }
            })}
            value={row.original.languageName && {
              value: row.original.languageName,
              label: row.original.languageName
            }}
            onChange={e => e && setNewItem(
              {
                ...row.original,
                languageName: e.value,
                languageCode: LANGUAGE_OPTIONS.find(lang => lang.languageName === e.value)?.languageCode || '',
                isEditing: true
              })}
          />
        }
        return value
      }
    }, {
      header: 'Code',
      accessorKey: 'languageCode',
      cell: ({ row }) => {
        const value = row.original.languageCode
        if (isEditable(row.original)) {
          return row.original.isEditing && <TextInput
            size={2}
            maxLength={2}
            aria-label={'Language code'}
            onChange={textVal => setNewItem({
              ...row.original,
              languageCode: textVal,
              isEditing: true
            })}
            value={row.original.languageCode}
          />
        }
        return value
      }
    }]
    return readonly ? baseCols : baseCols.concat({
      header: 'Actions',
      id: 'actions',
      cell: ({ row }) => {
        if (isEditable(row.original)) {
          if (!row.original.isEditing) {
            return <button
              className='btn btn-primary border-0'
              onClick={() => setNewItem({
                ...makeEmptyNewItem(),
                isEditing: true
              })}>
              <FontAwesomeIcon icon={faPlus} aria-label={'Add New'}/>
            </button>
          }

          return <>
            <Button
              className='btn btn-success me-2'
              disabled={
                isEmpty(row.original.languageName)
                || isEmpty(row.original.languageCode)}
              onClick={() => addNewItem({
                languageCode: row.original.languageCode,
                languageName: row.original.languageName,
                id: ''
              })}>
              <FontAwesomeIcon icon={faCheck} aria-label={'Accept'}/>
            </Button>
            <Button className='btn btn-danger' onClick={() => setNewItem(makeEmptyNewItem())}>
              <FontAwesomeIcon icon={faX} aria-label={'Cancel'}/>
            </Button>
          </>
        }
        return <Button className='btn btn-outline-danger border-0' onClick={() => {
          setItemSelectedForDeletion(row.original)
        }}>
          <FontAwesomeIcon icon={faTrashCan} aria-label={'Delete'}/>
        </Button>
      }
    })
  }, [])

  const data = useMemo(
    () => (items as PortalEnvironmentLanguageRow[]).concat(newItem),
    [items, newItem])

  const table = useReactTable<PortalEnvironmentLanguageRow>({
    data,
    columns,
    getCoreRowModel: getCoreRowModel()
  })

  return <div className='px-3 pt-1'>
    {basicTableLayout(table, { tdClass: 'col-1 ' })}
    {itemSelectedForDeletion && <DeleteItemModal
      onConfirm={deleteItem}
      onCancel={() => setItemSelectedForDeletion(null)}/>}
    <div>
    </div>
  </div>
}

const DeleteItemModal = (
  { onConfirm, onCancel } : { onConfirm: () => void, onCancel: () => void }
) => {
  return <Modal onHide={onCancel} show={true}>
    <ModalBody>
      <div>Remove this language from the dropdown?</div>
    </ModalBody>
    <ModalFooter>
      <button className='btn btn-danger' onClick={onConfirm}>
        Yes
      </button>
      <button className='btn btn-secondary' onClick={onCancel}>
        No
      </button>
    </ModalFooter>
  </Modal>
}
