package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class UserTermParser extends SearchTermParser<UserTerm> {
    private final ParticipantUserDao participantUserDao;

    public UserTermParser(ParticipantUserDao participantUserDao) {
        this.participantUserDao = participantUserDao;
    }

    @Override
    public UserTerm parse(String field) {
        return new UserTerm(participantUserDao, field);
    }

    @Override
    public String getTermName() {
        return "user";
    }

    @Override
    public Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId) {
        return addTermPrefix(UserTerm.FIELDS);
    }
}
