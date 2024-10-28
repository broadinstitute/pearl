package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.participant.PortalParticipantUserDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class PortalUserTermParser implements SearchTermParser<PortalUserTerm> {
    private final PortalParticipantUserDao portalParticipantUserDao;
    public PortalUserTermParser(PortalParticipantUserDao portalParticipantUserDao) {
        this.portalParticipantUserDao = portalParticipantUserDao;
    }

    @Override
    public PortalUserTerm parse(String term) {
        return new PortalUserTerm(portalParticipantUserDao, getArgument(term));
    }

    @Override
    public String getTermName() {
        return "portalUser";
    }

    @Override
    public Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId) {
        return addTermPrefix(PortalUserTerm.FIELDS);
    }
}
