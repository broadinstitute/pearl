package bio.terra.pearl.api.participant.controller.file;

import bio.terra.pearl.api.participant.api.ParticipantFileApi;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.api.participant.service.file.ParticipantFileExtService;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ParticipantFileController implements ParticipantFileApi {
  private final ParticipantFileExtService participantFileExtService;
  private final HttpServletRequest request;
  private final ObjectMapper objectMapper;
  private final RequestUtilService requestUtilService;

  public ParticipantFileController(
      ParticipantFileExtService participantFileExtService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      RequestUtilService requestUtilService) {
    this.participantFileExtService = participantFileExtService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.requestUtilService = requestUtilService;
  }

  @Override
  public ResponseEntity<Resource> download(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String enrolleeShortcode,
      String fileName) {
    ParticipantUser participantUser = requestUtilService.requireUser(request);

    // todo verify portalShortcode, studyShortcode, envName
    ParticipantFile participantFile =
        participantFileExtService.get(participantUser, enrolleeShortcode, fileName);
    InputStream content =
        participantFileExtService.downloadFile(participantUser, enrolleeShortcode, fileName);

    MediaType mediaType;
    try {
      mediaType = MediaType.parseMediaType(participantFile.getFileType());
    } catch (Exception e) {
      mediaType = MediaType.APPLICATION_OCTET_STREAM;
    }

    return ResponseEntity.ok().contentType(mediaType).body(new InputStreamResource(content));
  }

  @Override
  public ResponseEntity<Object> upload(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String enrolleeShortcode,
      MultipartFile participantFile) {
    ParticipantUser participantUser = requestUtilService.requireUser(request);

    // todo verify portalShortcode, studyShortcode, envName
    ParticipantFile created =
        participantFileExtService.uploadFile(participantUser, enrolleeShortcode, participantFile);

    return ResponseEntity.ok(created);
  }

  @Override
  public ResponseEntity<Object> list(
      String portalShortcode, String envName, String studyShortcode, String enrolleeShortcode) {
    ParticipantUser participantUser = requestUtilService.requireUser(request);

    // todo verify portalShortcode, studyShortcode, envName
    return ResponseEntity.ok(participantFileExtService.list(participantUser, enrolleeShortcode));
  }
}
