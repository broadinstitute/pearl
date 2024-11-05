package bio.terra.pearl.core.service.document;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.document.DocumentRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.document.DocumentRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentRequestTaskDispatcherTest extends BaseSpringBootTest {

    @Autowired
    DocumentRequestTaskDispatcher documentRequestTaskDispatcher;

    @Autowired
    DocumentRequestFactory documentRequestFactory;

    @Autowired
    StudyEnvironmentFactory studyEnvironmentFactory;

    @Autowired
    EnrolleeFactory enrolleeFactory;

    @Autowired
    ParticipantTaskService participantTaskService;

    @Test
    @Transactional
    public void testAutoAssign(TestInfo testInfo) {
        StudyEnvironmentBundle studyEnvironmentBundle = studyEnvironmentFactory.buildBundle(testInfo.getDisplayName(), EnvironmentName.sandbox);

        // attach document request to study env with auto assign = true
        DocumentRequest documentRequest = documentRequestFactory.buildPersisted(getTestName(testInfo), studyEnvironmentBundle.getPortal().getId());
        documentRequestFactory.attachToStudyEnvironment(getTestName(testInfo), documentRequest, studyEnvironmentBundle.getStudyEnv().getId());


        // enroll in study env
        EnrolleeBundle enrolleeBundle = enrolleeFactory.enroll("test@test.com", studyEnvironmentBundle.getPortal().getShortcode(), studyEnvironmentBundle.getStudy().getShortcode(), studyEnvironmentBundle.getStudyEnv().getEnvironmentName());
        Enrollee enrollee = enrolleeBundle.enrollee();

        // given task by default
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        assertEquals(1, tasks.size());

        ParticipantTask task = tasks.get(0);

        assertEquals(TaskType.DOCUMENT_REQUEST, task.getTaskType());
        assertEquals(TaskStatus.NEW, task.getStatus());
    }
}
