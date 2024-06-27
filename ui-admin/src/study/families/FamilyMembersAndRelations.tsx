import React from 'react'
import { Family } from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { FamilyRelations } from 'study/families/FamilyRelations'
import { FamilyMembers } from 'study/families/FamilyMembers'

/**
 * Renders editable page with all family members and relations.
 */
export const FamilyMembersAndRelations = (
  { family, studyEnvContext, reloadFamily }: {
    family: Family, studyEnvContext: StudyEnvContextT, reloadFamily: () => void
  }
) => {
  return <div>
    <h4>Members</h4>
    <FamilyMembers family={family} studyEnvContext={studyEnvContext} reloadFamily={reloadFamily}/>
    <div className="my-4"/>
    <h4>Relations</h4>
    <FamilyRelations family={family} studyEnvContext={studyEnvContext} reloadFamily={reloadFamily}/>
  </div>
}
