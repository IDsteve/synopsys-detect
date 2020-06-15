package com.synopsys.integration.detect.workflow.blackduck.analytics;

import java.io.IOException;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class AnalyticsConfigurationService {
    private static final BlackDuckPath INTEGRATION_SETTINGS_PATH = new BlackDuckPath("/api/internal/integration-settings");
    private static final String MIME_TYPE = "application/vnd.blackducksoftware.integration-setting-1+json";

    private final Gson gson;

    public AnalyticsConfigurationService(Gson gson) {
        this.gson = gson;
    }

    public AnalyticsSetting fetchAnalyticsSetting(BlackDuckService blackDuckService) throws IntegrationException, IOException {
        String uri = blackDuckService.getUri(INTEGRATION_SETTINGS_PATH) + "/analytics";

        Request request = new Request.Builder()
                              .uri(uri)
                              .method(HttpMethod.GET)
                              .mimeType(MIME_TYPE)
                              .build();
        try (Response response = blackDuckService.execute(request)) {
            response.throwExceptionForError();
            return gson.fromJson(response.getContentString(), AnalyticsSetting.class);
        }
    }
}