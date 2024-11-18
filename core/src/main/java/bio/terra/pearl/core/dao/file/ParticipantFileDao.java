package bio.terra.pearl.core.dao.file;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.file.ParticipantFile;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ParticipantFileDao extends BaseJdbiDao<ParticipantFile> {
    public ParticipantFileDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ParticipantFile> getClazz() {
        return ParticipantFile.class;
    }

    public List<ParticipantFile> findBySurveyResponseId(UUID surveyResponseId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select file.* from %s file
                                inner join answer a on file.enrollee_id = a.enrollee_id
                                where file.file_name = a.string_value and a.format = 'FILE_NAME'
                                and a.survey_response_id = :surveyResponseId
                                """.formatted(tableName))
                        .bind("surveyResponseId", surveyResponseId)
                        .mapTo(clazz)
                        .stream()
                        .toList()
        );
    }

    public List<ParticipantFile> findByEnrolleeId(UUID enrolleeId) {
        return findAllByProperty("enrollee_id", enrolleeId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        deleteByProperty("enrollee_id", enrolleeId);
    }

    public Optional<ParticipantFile> findByEnrolleeIdAndFileName(UUID enrolleeId, String fileName) {
        return findByTwoProperties("enrollee_id", enrolleeId, "file_name", fileName);
    }

    public List<ParticipantFile> findAllByFileNameForEnrollee(UUID enrolleeId, List<String> participantFileNames) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select * from %s
                                where enrollee_id = :enrolleeId
                                and file_name in (<fileNames>)
                                """.formatted(tableName))
                        .bind("enrolleeId", enrolleeId)
                        .bindList("fileNames", participantFileNames)
                        .mapTo(clazz)
                        .stream()
                        .toList()
        );
    }

    @Transactional
    public ParticipantFile createOrReplace(ParticipantFile participantFile) {
        Optional<ParticipantFile> existingFileOpt = findByEnrolleeIdAndFileName(participantFile.getEnrolleeId(), participantFile.getFileName());

        existingFileOpt.ifPresent(existingFile -> {
            delete(existingFile.getId());
        });

        return create(participantFile);
    }
}
