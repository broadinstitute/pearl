package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.participant.PortalParticipantUserDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class PortalUserTermParser extends SearchTermParser<PortalUserTerm> {
    private final PortalParticipantUserDao portalParticipantUserDao;
    public PortalUserTermParser(PortalParticipantUserDao portalParticipantUserDao) {
        this.portalParticipantUserDao = portalParticipantUserDao;
    }

    @Override
    public PortalUserTerm parse(String field) {
        return new PortalUserTerm(portalParticipantUserDao, field);
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
