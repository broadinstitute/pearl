package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.participant.MailingAddressDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class ProfileTermParser extends SearchTermParser<ProfileTerm> {
    private final ProfileDao profileDao;
    private final MailingAddressDao mailingAddressDao;

    public ProfileTermParser(ProfileDao profileDao, MailingAddressDao mailingAddressDao) {
        this.profileDao = profileDao;
        this.mailingAddressDao = mailingAddressDao;
    }

    @Override
    public ProfileTerm parse(String field) {
        return new ProfileTerm(profileDao, mailingAddressDao, field);
    }

    @Override
    public String getTermName() {
        return "profile";
    }

    @Override
    public Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId) {
        return addTermPrefix(ProfileTerm.FIELDS);
    }
}
