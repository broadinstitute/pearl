package bio.terra.pearl.core.service.terra;

import bio.terra.common.tracing.OkHttpClientTracingInterceptor;
import bio.terra.pearl.core.config.SamConfiguration;
import io.opencensus.trace.Tracing;
import okhttp3.OkHttpClient;
import org.broadinstitute.dsde.workbench.client.sam.ApiClient;
import org.broadinstitute.dsde.workbench.client.sam.api.ResourcesApi;
import org.broadinstitute.dsde.workbench.client.sam.api.StatusApi;
import org.broadinstitute.dsde.workbench.client.sam.api.UsersApi;
import org.springframework.stereotype.Component;

@Component
public class SamClient {
  // TODO: autowire
  private final SamConfiguration samConfig = new SamConfiguration("https://sam.dsde-dev.broadinstitute.org");
  private final OkHttpClient okHttpClient;

  public SamClient() {
    this.okHttpClient = new ApiClient().getHttpClient();
  }

  private ApiClient getApiClient(String accessToken) {
    ApiClient apiClient = getApiClient();
    apiClient.setAccessToken(accessToken);
    return apiClient;
  }

  private ApiClient getApiClient() {
    var okHttpClientWithTracing =
        this.okHttpClient
            .newBuilder()
            .addInterceptor(new OkHttpClientTracingInterceptor(Tracing.getTracer()))
            .build();
    return new ApiClient().setHttpClient(okHttpClientWithTracing).setBasePath(samConfig.basePath());
  }

  UsersApi usersApi(String accessToken) {
    return new UsersApi(getApiClient(accessToken));
  }

  ResourcesApi resourcesApi(String accessToken) {
    return new ResourcesApi(getApiClient(accessToken));
  }

  StatusApi statusApi() {
    return new StatusApi(getApiClient());
  }
}
