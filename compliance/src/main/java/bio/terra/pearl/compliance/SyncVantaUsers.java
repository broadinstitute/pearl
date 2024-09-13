package bio.terra.pearl.compliance;

import bio.terra.pearl.compliance.exception.RateLimitException;
import bio.terra.pearl.compliance.exception.VantaUpdateException;
import bio.terra.pearl.compliance.model.AccessToken;
import bio.terra.pearl.compliance.model.GithubAccount;
import bio.terra.pearl.compliance.model.JiraAccount;
import bio.terra.pearl.compliance.model.PersonInScope;
import bio.terra.pearl.compliance.model.SlackUser;
import bio.terra.pearl.compliance.model.UpdateVantaMetadata;
import bio.terra.pearl.compliance.model.UserSyncConfig;
import bio.terra.pearl.compliance.model.VantaCredentials;
import bio.terra.pearl.compliance.model.VantaIntegration;
import bio.terra.pearl.compliance.model.VantaObject;
import bio.terra.pearl.compliance.model.VantaResults;
import bio.terra.pearl.compliance.model.VantaResultsResponse;
import bio.terra.pearl.compliance.model.WorkdayAccount;
import com.google.cloud.functions.CloudEventsFunction;
import com.google.cloud.spring.secretmanager.SecretManagerTemplate;
import com.google.gson.Gson;
import com.slack.api.Slack;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Run this as a main() or as a cloud function to set
 * various vanta integrations to use only the team members
 * needed for our audit.  When running locally, set the
 * GOOGLE_CLOUD_PROJECT and VANTA_CONFIG_SECRET environment vars.
 */
@Slf4j
@SpringBootApplication
public class SyncVantaUsers implements CommandLineRunner, CloudEventsFunction {

    public static final String JIRA_INTEGRATION_ID = "jira";
    public static final String SLACK_INTEGRATION_ID = "slack";
    public static final String GITHUB_INTEGRATION_ID = "github";
    @Autowired
    private SecretManagerTemplate secretManagerTemplate;

    @Value("#{environment.VANTA_CONFIG_SECRET}")
    private String vantaConfigSecret;

    private UserSyncConfig userSyncConfig;

    private final Collection<PersonInScope> peopleInScope = new ArrayList<>();

    private final SpringApplication app = new SpringApplicationBuilder(SyncVantaUsers.class).web(WebApplicationType.NONE).bannerMode(Banner.Mode.OFF).build();

    @Override
    public void accept(CloudEvent event) throws Exception {
        try {
            app.run("");
        } catch (Exception e) {
            log.error("Vanta sync failed", e);
        }
    }

    public static void main(String[] args) {
        try {
            new SyncVantaUsers().accept(null);
        } catch (Exception e) {
            log.error("Could not run sync", e);
        }
    }

    private UserSyncConfig loadConfig() {
        userSyncConfig = new Gson().fromJson(secretManagerTemplate.getSecretString(vantaConfigSecret), UserSyncConfig.class);
        peopleInScope.clear();
        peopleInScope.addAll(userSyncConfig.getPeopleInScope());
        return userSyncConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting vanta sync app");
        loadConfig();
        // now that we've loaded the config, perform the sync
        Instant start = Instant.now();
        String summaryMessage = syncVantaAccounts();
        Duration duration = Duration.between(start, Instant.now());

        log.info("Vanta sync completed after {}m.  Posting update to slack.", duration.toMinutes());

        try (Slack slack = Slack.getInstance()) {
            ChatPostMessageResponse response = slack.methods(userSyncConfig.getSlackToken()).chatPostMessage(req -> req
                    .channel(userSyncConfig.getSlackChannel())
                    .text("Vanta sync complete after " + duration.toMinutes() + "m.\n" + summaryMessage));

            if (!response.isOk()) {
                log.info("Slack message returned {} {}", response.getMessage(), response.getError());
            }
        }
    }

    /**
     * Given a ready-to-retrieve rest client, fetches
     * all data in the results.data[] array and converts
     * it to the appropriate types
     */
    private <T extends VantaObject> Collection<T> collectAllData(WebClient wc, ParameterizedTypeReference<VantaResultsResponse<T>> type) {
        final AtomicReference<VantaResults<T>> results = new AtomicReference<>();
        AtomicReference<List<T>> elements = new AtomicReference<>(new ArrayList<>());
        AtomicInteger cursorCount = new AtomicInteger(0);

        while (cursorCount.get() == 0 || results.get().getPageInfo().isHasNextPage()) {
            log.debug("Getting cursor number {}", cursorCount);
            VantaResultsResponse<T> response = wc.get().uri(uriBuilder -> {
                        if (cursorCount.get() > 0) {
                            uriBuilder.queryParam("pageCursor", results.get().getPageInfo().getEndCursor());
                        }
                        uriBuilder.queryParam("pageSize", 100);
                        return uriBuilder.build();
                    }).retrieve()
                    .onStatus(HttpStatus.TOO_MANY_REQUESTS::equals, res -> {
                        List<String> header = res.headers().header("Retry-After");
                        int delayInSeconds;
                        if (!header.isEmpty()) {
                            delayInSeconds = Integer.parseInt(header.get(0));
                        } else {
                            delayInSeconds = 70;
                        }
                        log.info("Vanta rate limit exceeded; waiting for {}s with {} {} objects", delayInSeconds, elements.get().size(), type.getType().getTypeName());
                        try {
                            Thread.sleep(Duration.ofSeconds(delayInSeconds));
                        } catch (InterruptedException e) {
                            log.error("Interrupted while sleeping in response to rate limit", e);
                            Thread.currentThread().interrupt();
                        }
                        return res.bodyToMono(String.class).map(msg -> new RateLimitException(msg, delayInSeconds));
                    })
                    .bodyToMono(type)
                    .retryWhen(Retry.withThrowable(throwableFlux -> throwableFlux.filter(RateLimitException.class::isInstance).map(t ->
                            Retry.fixedDelay(5, ((RateLimitException)t).getRetryAfterDelayDuration())))).block();
            cursorCount.incrementAndGet();
            results.set(response.getResults());

            if (response != null) {
                if (response.getResults() != null) {
                    elements.get().addAll(response.getResults().getData());
                }
            } else {
                break;
            }
        }
        return elements.get();
    }

    private WebClient getWebClientForIntegration(String accessToken, String integrationId, String resourceKind) {
        WebClient.Builder webClientBuilder = WebClient.builder().defaultHeaders(h -> {
            h.setBearerAuth(accessToken);
            h.setContentType(MediaType.APPLICATION_JSON);
        });

        return webClientBuilder.baseUrl(userSyncConfig.getVantaBaseUrl() + "v1/integrations/" + integrationId + "/resource-kinds/" + resourceKind + "/resources").build();
    }

    private List<VantaIntegration> getIntegrations() {
        List<VantaIntegration> integrationsToSync = new ArrayList<>();
        integrationsToSync.add(new VantaIntegration(JIRA_INTEGRATION_ID, "JiraAccount", new ParameterizedTypeReference<VantaResultsResponse<JiraAccount>>() {},
                userSyncConfig.getResourceIdsToIgnore(JIRA_INTEGRATION_ID)));
        integrationsToSync.add(new VantaIntegration(SLACK_INTEGRATION_ID, "SlackAccount", new ParameterizedTypeReference<VantaResultsResponse<SlackUser>>() {},
                userSyncConfig.getResourceIdsToIgnore(SLACK_INTEGRATION_ID)));
        integrationsToSync.add(new VantaIntegration(GITHUB_INTEGRATION_ID, "GithubAccount", new ParameterizedTypeReference<VantaResultsResponse<GithubAccount>>() {},
                userSyncConfig.getResourceIdsToIgnore(GITHUB_INTEGRATION_ID)));
        integrationsToSync.add(new VantaIntegration("workday", "WorkdayHrUser", new ParameterizedTypeReference<VantaResultsResponse<WorkdayAccount>>() {},
                userSyncConfig.getResourceIdsToIgnore("workday")));
        return integrationsToSync;
    }

    public String syncVantaAccounts() {
        StringBuilder summary = new StringBuilder();
        WebClient wc = WebClient.builder().baseUrl(userSyncConfig.getVantaBaseUrl() + "oauth/token").build();
        AccessToken vantaToken= wc.post().bodyValue(new VantaCredentials("client_credentials", "vanta-api.all:read vanta-api.all:write", userSyncConfig.getVantaClientId(), userSyncConfig.getVantaClientSecret()))
                .header("Content-Type", "application/json").retrieve().bodyToMono(AccessToken.class).block();
        WebClient.builder().defaultHeaders(h -> {
            h.setBearerAuth(vantaToken.getAccessToken());
            h.setContentType(MediaType.APPLICATION_JSON);
        });

        // go through each integration and find scope changes that need to be made.
        int resourceBatchSize = 50;
        List<VantaIntegration> integrationsToSync = getIntegrations();

        for (VantaIntegration vantaIntegration : integrationsToSync) {
            String integrationId = vantaIntegration.getIntegrationId();
            log.debug("Synchronizing {}", integrationId);
            String resourceKind = vantaIntegration.getResourceKind();
            ParameterizedTypeReference resultsResponseType = vantaIntegration.getResultsResponseType();

            Collection<VantaObject> vantaObjects = collectAllData(vantaToken.getAccessToken(), integrationId, resourceKind, resultsResponseType);
            log.debug("Found {} {} objects.", vantaObjects.size(), integrationId);

            Collection<VantaObject> objectsToUpdate = new HashSet<>();
            Collection<String> idsRemovedFromScope = new HashSet<>();
            Collection<String> idsAddedToScope = new HashSet<>();

            // in bulk, update people who aren't in scope but should be
            vantaObjects.stream().filter(vantaObject -> !vantaIntegration.shouldIgnoreResource(vantaObject.getResourceId()) && vantaObject.shouldBeInScope(peopleInScope) && !vantaObject.isInScope()).forEach(objectToMarkInScope -> {
                objectsToUpdate.add(objectToMarkInScope);
                idsAddedToScope.add(objectToMarkInScope.getSimpleId());
                if (objectsToUpdate.size() == resourceBatchSize) {
                    setInScope(vantaToken.getAccessToken(), objectsToUpdate, true, integrationId, resourceKind);
                    objectsToUpdate.clear();
                }
            });
            if (!objectsToUpdate.isEmpty()) {
                setInScope(vantaToken.getAccessToken(), objectsToUpdate, true, integrationId, resourceKind);
                objectsToUpdate.clear();
            }

            // in bulk, update people who are in scope but who shouldn't be
            vantaObjects.stream().filter(vantaObject -> !vantaIntegration.shouldIgnoreResource(vantaObject.getResourceId()) && !vantaObject.shouldBeInScope(peopleInScope) && vantaObject.isInScope()).forEach(objectToMarkOutOfScope -> {
                objectsToUpdate.add(objectToMarkOutOfScope);
                idsRemovedFromScope.add(objectToMarkOutOfScope.getSimpleId());
                if (objectsToUpdate.size() == resourceBatchSize) {
                    setInScope(vantaToken.getAccessToken(), objectsToUpdate, false, integrationId, resourceKind);
                    objectsToUpdate.clear();
                }
            });
            if (!objectsToUpdate.isEmpty()) {
                setInScope(vantaToken.getAccessToken(), objectsToUpdate, false, integrationId, resourceKind);
                objectsToUpdate.clear();
            }

            String msg = "";
            if (idsAddedToScope.isEmpty() && idsRemovedFromScope.isEmpty()) {
                msg = String.format("No changes to scope in %s", integrationId);
                log.info(msg);
                summary.append(msg).append("\n");
            } else {
                if (idsAddedToScope.isEmpty()) {
                    msg = String.format("No objects added to scope in %s", integrationId);
                } else {
                    msg = String.format("Added the following %s objects to scope in %s: %s", idsAddedToScope.size(), integrationId,
                            StringUtils.join(idsAddedToScope, ","));
                }
                log.info(msg);
                summary.append(msg).append("\n");

                if (idsRemovedFromScope.isEmpty()) {
                    msg = String.format("No objects removed from scope in %s", integrationId);
                } else {
                    msg = String.format("Removed the following %s objects from scope in %s: %s", idsRemovedFromScope.size(), integrationId,
                            StringUtils.join(idsRemovedFromScope, ","));
                }
                log.info(msg);
                summary.append(msg).append("\n");
            }
        }
        return summary.toString();
    }

    private <T extends VantaObject> Collection<T> collectAllData(String accessToken, String integrationId, String resourceKind, ParameterizedTypeReference<VantaResultsResponse<T>> resultsResponseClass) {
        WebClient wc = getWebClientForIntegration(accessToken, integrationId, resourceKind);
        return collectAllData(wc, resultsResponseClass);
    }

    private void setInScope(String accessTokeen, Collection<VantaObject> vantaObjects, boolean isInScope, String integrationId, String resourceKind) {
        log.debug("Updating {} {} objects to scope={}", vantaObjects.size(), integrationId, isInScope);
        UpdateVantaMetadata updateMetadata = new UpdateVantaMetadata();
        updateMetadata.setInScope(isInScope);
        vantaObjects.forEach(vantaObject -> updateMetadata.addResourceId(vantaObject.getResourceId()));
        try {
            String updateResult = getWebClientForIntegration(accessTokeen, integrationId, resourceKind)
                    .patch().bodyValue(updateMetadata).retrieve().onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, res ->
                            res.bodyToMono(String.class).map(VantaUpdateException::new)
                    ).bodyToMono(String.class).block();
            log.debug("Updated {} {} objects to {} with response {}", updateMetadata.getResourceIds().size(), integrationId, isInScope, updateResult);
        } catch (VantaUpdateException e) {
            log.warn("Could not change scope to {} on some of {} {} objects due to {}", isInScope, integrationId, updateMetadata.getResourceIds().size(), e.getMessage());
        }
    }
}