package bio.terra.pearl.core.dao.kit;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.kit.KitTypeFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.samePropertyValuesAs;

public class KitRequestDaoTest extends BaseSpringBootTest {

    @Autowired
    private KitRequestDao kitRequestDao;

    @Transactional
    @Test
    public void testCreatSampleKit(TestInfo info) {
        AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(info));
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
        KitType kitType = kitTypeFactory.buildPersisted(getTestName(info));

        KitRequest kitRequest = KitRequest.builder()
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee.getId())
                .kitTypeId(kitType.getId())
                .sentToAddress("{ firstName:\"Alex\", lastName:\"Jones\", street1:\"123 Fake Street\" }")
                .status(KitRequestStatus.CREATED)
                .build();

        KitRequest savedKitRequest = kitRequestDao.create(kitRequest);

        DaoTestUtils.assertGeneratedProperties(savedKitRequest);
        assertThat(savedKitRequest.getCreatingAdminUserId(), equalTo(adminUser.getId()));
        assertThat(savedKitRequest.getEnrolleeId(), equalTo(enrollee.getId()));
        assertThat(savedKitRequest.getKitTypeId(), equalTo(kitType.getId()));
        assertThat(savedKitRequest, samePropertyValuesAs(kitRequest, "id", "createdAt", "lastUpdatedAt"));
        assertThat(savedKitRequest.getStatus(), equalTo(KitRequestStatus.CREATED));
    }

    @Transactional
    @Test
    public void testFindByStatus(TestInfo info) throws Exception {
        // Arrange
        AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        KitType kitType = kitTypeFactory.buildPersisted(getTestName(info));

        Function<KitRequestStatus, KitRequest> makeKit = status -> {
            KitRequest kit = kitRequestFactory.builder(getTestName(info) + " " + status.name())
                    .creatingAdminUserId(adminUser.getId())
                    .enrolleeId(enrollee.getId())
                    .kitTypeId(kitType.getId())
                    .status(status)
                    .build();
            return kitRequestDao.create(kit);

        };
        List<KitRequest> incompleteKits = Stream.of(KitRequestStatus.CREATED, KitRequestStatus.SENT).map(makeKit).toList();
        List<KitRequest> completeKits = Stream.of(KitRequestStatus.RECEIVED, KitRequestStatus.ERRORED).map(makeKit).toList();

        // Act
        List<KitRequest> fetchedIncompleteKits = kitRequestDao.findByStatus(
                studyEnvironment.getId(),
                List.of(KitRequestStatus.CREATED, KitRequestStatus.SENT));
        List<KitRequest> fetchedCompleteKits = kitRequestDao.findByStatus(
                studyEnvironment.getId(),
                List.of(KitRequestStatus.RECEIVED, KitRequestStatus.ERRORED));

        // Assert
        assertThat(fetchedIncompleteKits, containsInAnyOrder(incompleteKits.toArray()));
        assertThat(fetchedCompleteKits, containsInAnyOrder(completeKits.toArray()));
    }

    @Transactional
    @Test
    public void testFindByStudyEnvironment(TestInfo info) throws Exception {
        AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(info));
        KitType kitType = kitTypeFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnvironment1 = studyEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment studyEnvironment2 = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee enrollee1 = enrolleeFactory.buildPersisted(getTestName(info) + " 1", studyEnvironment1);
        Enrollee enrollee2 = enrolleeFactory.buildPersisted(getTestName(info) + " 2", studyEnvironment2);

        KitRequest kit1 = kitRequestFactory.builder(getTestName(info) + " 1")
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee1.getId())
                .kitTypeId(kitType.getId())
                .build();
        kit1 = kitRequestDao.create(kit1);
        KitRequest kit2 = kitRequestFactory.builder(getTestName(info) + " 2")
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee2.getId())
                .kitTypeId(kitType.getId())
                .build();
        kit2 = kitRequestDao.create(kit2);

        List<KitRequest> kits1 = kitRequestDao.findByStudyEnvironment(studyEnvironment1.getId());
        assertThat(kits1, contains(kit1));

        List<KitRequest> kits2 = kitRequestDao.findByStudyEnvironment(studyEnvironment2.getId());
        assertThat(kits2, contains(kit2));
    }

    @Autowired
    private AdminUserFactory adminUserFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private KitRequestFactory kitRequestFactory;
    @Autowired
    private KitTypeFactory kitTypeFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
}
