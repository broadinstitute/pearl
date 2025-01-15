import React, {
  useEffect,
  useMemo,
  useRef,
  useState
} from 'react'
import {
  faCheck,
  faPlus,
  faX
} from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  LanguageText,
  useI18n
} from '@juniper/ui-core'
import {
  isEmpty,
  isNil
} from 'lodash'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
  ModalTitle
} from 'react-bootstrap'
import {
  ColumnDef,
  getCoreRowModel,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout } from '../../util/tableUtils'
import { faTrashCan } from '@fortawesome/free-solid-svg-icons/faTrashCan'
import { TextInput } from 'components/forms/TextInput'
import Select from 'react-select'


type EditableLanguageText = Partial<LanguageText> & { isEditing: boolean }
type LanguageTextRow = LanguageText | EditableLanguageText
const isEditable = (row: LanguageTextRow): row is EditableLanguageText => {
  return !isNil((row as EditableLanguageText).isEditing)
}

/**
 * Table which allows viewing, deleting, and creating new answer mappings.
 */
export default function LanguageTextOverridesEditor(
  {
    initialLanguageTextOverrides, onChange
  } : {
    initialLanguageTextOverrides: LanguageText[], onChange: (newLanguageTexts: LanguageText[]) => void
  }
) {
  const {
    languageTexts,
    selectedLanguage
  } = useI18n()

  const languageKeys = Object.keys(languageTexts)

  const [languageTextOverrides, setLanguageTextOverrides] = useState<LanguageText[]>(initialLanguageTextOverrides || [])

  const [overrideSelectedForDeletion, setOverrideSelectedForDeletion] = useState<LanguageText | null>(null)


  useEffect(() => {
    for (const override of languageTextOverrides) {
      languageTexts[override.keyName] = override.text
    }
  }, [languageTextOverrides])

  // state for new mapping
  const [newOverride, setNewOverride] = useState<EditableLanguageText>({ isEditing: false })


  const deleteLanguageTextOverride = async () => {
    if (overrideSelectedForDeletion) {
      const newOverrides = languageTextOverrides.filter(m => m.keyName !== overrideSelectedForDeletion.keyName)
      onChange(newOverrides)
      setLanguageTextOverrides(newOverrides)
      setOverrideSelectedForDeletion(null)
    }
  }

  const saveNewLanguageTextOverride = async (override: LanguageText) => {
    const newOverrides = [...languageTextOverrides, override]

    onChange(newOverrides)
    setLanguageTextOverrides(newOverrides)
    setNewOverride({ isEditing: false })
  }

  const onChangeNewOverrideKey = (key: string) => {
    if (languageKeys.includes(key)) {
      setNewOverride({ ...newOverride, keyName: key, text: languageTexts[key] })
    }
  }

  const onChangeNewOverrideText = (text: string) => {
    setNewOverride({ ...newOverride, text })
  }

  const setIsEditing = (isEditing: boolean) => {
    setNewOverride({ ...newOverride, isEditing })
  }

  const languageTextOverrideTextRef = useRef(null)

  const columns = useMemo<ColumnDef<LanguageTextRow>[]>(() => [
    {
      header: 'Key Name',
      accessorKey: 'keyName',
      cell: ({ row }) => {
        const value = row.original.keyName
        if (isEditable(row.original)) {
          return row.original.isEditing && <Select
            aria-label={'New language text override key'}
            options={languageKeys.map(key => {
              return {
                value: key,
                label: key,
                isDisabled: languageTextOverrides.some(m => m.keyName === key)
              }
            })}
            value={row.original.keyName && {
              value: row.original.keyName,
              label: row.original.keyName
            }}
            onChange={e => e && onChangeNewOverrideKey(e.value)}
          />
        }
        return value
      }
    },
    {
      header: 'Text',
      accessorKey: 'text',
      cell: ({ row }) => {
        const value = row.original.text
        if (isEditable(row.original)) {
          return row.original.isEditing && <TextInput
            ref={languageTextOverrideTextRef}
            disabled={isEmpty(newOverride.keyName)}
            readOnly={isEmpty(newOverride.keyName)}
            autoFocus={!isEmpty(newOverride.keyName)}
            key='new-override-text'
            aria-label={'New language text override text'}
            onChange={val => onChangeNewOverrideText(val)}
            value={row.original.text || ''}
          />
        }
        return value
      }
    },
    {
      header: 'Language',
      accessorKey: 'language',
      cell: ({ row }) => row.original?.language
    },
    {
      header: 'Actions',
      id: 'actions',
      cell: ({ row }) => {
        if (isEditable(row.original)) {
          if (!row.original.isEditing) {
            return <button
              className='btn btn-primary border-0'
              onClick={() => setIsEditing(true)}>
              <FontAwesomeIcon icon={faPlus} aria-label={'Create New Answer Mapping'}/>
            </button>
          }

          return <>
            <button
              className='btn btn-success me-2'
              onClick={() => saveNewLanguageTextOverride({
                keyName: newOverride.keyName as string,
                text: newOverride.text as string,
                language: selectedLanguage
              })}>
              <FontAwesomeIcon icon={faCheck} aria-label={'Accept new language text override'}/>
            </button>
            <button className='btn btn-danger' onClick={() => setIsEditing(false)}>
              <FontAwesomeIcon icon={faX} aria-label={'Cancel new language text override'}/>
            </button>
          </>
        }
        return <button className='btn btn-outline-danger border-0' onClick={() => {
          setOverrideSelectedForDeletion(row.original as LanguageText)
        }}>
          <FontAwesomeIcon icon={faTrashCan} aria-label={'Delete language text override'}/>
        </button>
      }
    }
  ], [newOverride, languageTextOverrides])

  const data = useMemo(
    () => (languageTextOverrides as LanguageTextRow[]).concat(newOverride),
    [languageTextOverrides, newOverride])

  const table = useReactTable<LanguageTextRow>({
    data,
    columns,
    getCoreRowModel: getCoreRowModel()
  })

  return <div className='px-3 pt-1'>
    <p>
      This page allows you to override any default text across the participant website.
    </p>
    {basicTableLayout(table, { tdClass: 'col-1 ' })}
    {overrideSelectedForDeletion && <DeleteOverrideModal
      onConfirm={deleteLanguageTextOverride}
      onCancel={() => setOverrideSelectedForDeletion(null)}/>}
    <div>
    </div>
  </div>
}

const LanguageTextInput = (
  { value, onChange } : { value: string, onChange: (val: string) => void }
) => {
  const [val, setVal] = useState(value)
  return <TextInput
    aria-label={'Language text override text'}
    onChange={newVal => {
      setVal(newVal)
      onChange(newVal)
    }}
    value={val}
  />
}

const DeleteOverrideModal = (
  { onConfirm, onCancel } : { onConfirm: () => void, onCancel: () => void }
) => {
  return <Modal onHide={onCancel} show={true}>
    <ModalHeader>
      <ModalTitle>
        Are you sure you want to delete this language text override?
      </ModalTitle>
    </ModalHeader>
    <ModalBody>
      This action cannot be undone.
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
