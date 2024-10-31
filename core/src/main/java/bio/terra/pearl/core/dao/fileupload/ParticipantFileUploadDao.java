package bio.terra.pearl.core.dao.fileupload;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.fileupload.ParticipantFileUpload;
import org.jdbi.v3.core.Jdbi;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ParticipantFileUploadDao extends BaseJdbiDao<ParticipantFileUpload> {

    private final String columnsWithNoFileData;

    public ParticipantFileUploadDao(Jdbi jdbi) {
        super(jdbi);

        columnsWithNoFileData = getQueryColumns.stream().filter(column -> !column.equals("temp_file_data")).collect(Collectors.joining(","));

    }

    @Override
    protected Class<ParticipantFileUpload> getClazz() {
        return ParticipantFileUpload.class;
    }

    public Optional<ParticipantFileUpload> findForParticipantNoFileData(UUID portalParticipantUser, String fileName, Integer version) {
        return jdbi.withHandle(handle ->
                handle.createQuery(
                        "select %s from survey where portal_participant_user_id = :portalParticipantUser and file_name = :fileName and version = :version"
                                .formatted(columnsWithNoFileData))
                                .bind("portalParticipantUser", portalParticipantUser)
                                .bind("fileName", fileName)
                                .bind("version", version)
                                .mapTo(clazz)
                                .findOne()
        );
    }

    public InputStream fetchFileContent(UUID portalParticipantUser, String fileName, Integer version) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select temp_file_data from participant_file_upload where portal_participant_user_id = :portalParticipantUser and file_name = :fileName and version = :version")
                        .bind("portalParticipantUser", portalParticipantUser)
                        .bind("fileName", fileName)
                        .bind("version", version)
                        .mapTo(InputStream.class)
                        .one()
        );
    }
}
