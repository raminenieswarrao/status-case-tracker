package com.eswar.uscistracker.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;

@Component
@ConditionalOnProperty(name = "uscis.mode", havingValue = "api")
public class ApiUscisClient implements UscisClient {
    private final WebClient webClient;
    private final String baseUrl;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String demoId;

    private volatile String accessToken;
    private volatile long tokenExpiresAtEpochMs;

    public ApiUscisClient(WebClient.Builder builder,
                          @Value("${uscis.api.base-url}") String baseUrl,
                          @Value("${uscis.api.token-url}") String tokenUrl,
                          @Value("${uscis.api.client-id}") String clientId,
                          @Value("${uscis.api.client-secret}") String clientSecret,
                          @Value("${uscis.api.demo-id:}") String demoId) {
        this.webClient = builder.build();
        this.baseUrl = baseUrl;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.demoId = demoId;
    }

    @Override
    public UscisCaseStatusResult check(String receiptNumber) {
        try {
            String token = token();
            WebClient.RequestHeadersSpec<?> request = webClient.get()
                .uri(baseUrl + "/" + receiptNumber)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON);

            if (demoId != null && !demoId.isBlank()) {
                request = request.header("demo_id", demoId);
            }

            JsonNode root = request.retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            JsonNode caseNode = findCaseStatusNode(root);
            String status = firstText(caseNode,
                "current_case_status_text_en", "currentCaseStatusTextEn", "status", "case_status");
            String desc = firstText(caseNode,
                "current_case_status_desc_en", "currentCaseStatusDescEn", "description", "case_status_description");
            String formType = firstText(caseNode, "formType", "form_type");
            String receipt = firstText(caseNode, "receiptNumber", "receipt_number");
            String date = firstText(caseNode, "current_case_status_date", "currentCaseStatusDate",
                "updatedAt", "lastUpdated");

            if ((status == null || status.isBlank()) && root != null) {
                status = root.path("message").asText("");
            }
            return new UscisCaseStatusResult(true,
                receipt == null || receipt.isBlank() ? receiptNumber : receipt,
                formType,
                status == null || status.isBlank() ? "USCIS status received" : status,
                desc,
                parseDate(date),
                null);
        } catch (WebClientResponseException ex) {
            String body = ex.getResponseBodyAsString();
            if (ex.getStatusCode().is4xxClientError() && looksInvalid(body)) {
                return UscisCaseStatusResult.invalid(receiptNumber, extractError(body));
            }
            throw new UscisIntegrationException("USCIS API returned HTTP " + ex.getStatusCode().value()
                + ". " + extractError(body), ex);
        } catch (Exception ex) {
            if (ex instanceof UscisIntegrationException uie) throw uie;
            throw new UscisIntegrationException("Unable to contact USCIS: " + ex.getMessage(), ex);
        }
    }

    private synchronized String token() {
        long now = System.currentTimeMillis();
        if (accessToken != null && now < tokenExpiresAtEpochMs - 60_000) return accessToken;

        String basic = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        JsonNode tokenJson;
        try {
            tokenJson = webClient.post()
                .uri(tokenUrl)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (WebClientResponseException ex) {
            // Some Apigee client-credential configurations expect the credentials as form parameters.
            tokenJson = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials&client_id=" + url(clientId)
                    + "&client_secret=" + url(clientSecret))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        }

        if (tokenJson == null || tokenJson.path("access_token").asText().isBlank()) {
            throw new UscisIntegrationException("USCIS OAuth did not return an access token.");
        }
        accessToken = tokenJson.path("access_token").asText();
        long expiresIn = tokenJson.path("expires_in").asLong(3600);
        tokenExpiresAtEpochMs = now + expiresIn * 1000;
        return accessToken;
    }

    private JsonNode findCaseStatusNode(JsonNode root) {
        if (root == null) return null;
        if (root.has("case_status")) return root.get("case_status");
        if (root.has("caseStatus")) return root.get("caseStatus");
        if (root.has("data") && root.get("data").has("case_status")) return root.get("data").get("case_status");
        return root;
    }

    private String firstText(JsonNode node, String... names) {
        if (node == null) return null;
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull() && !value.asText().isBlank()) return value.asText();
        }
        return null;
    }

    private OffsetDateTime parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try { return OffsetDateTime.parse(value); }
        catch (DateTimeParseException ignored) { return null; }
    }

    private boolean looksInvalid(String body) {
        String b = body == null ? "" : body.toLowerCase();
        return b.contains("invalid") && b.contains("receipt");
    }

    private String extractError(String body) {
        if (body == null || body.isBlank()) return "USCIS returned an error.";
        try {
            JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
            if (root.has("errors") && root.get("errors").isArray() && !root.get("errors").isEmpty()) {
                return root.get("errors").get(0).path("message").asText("USCIS returned an error.");
            }
            if (root.has("message")) return root.get("message").asText();
        } catch (Exception ignored) { }
        return body.length() > 450 ? body.substring(0, 450) : body;
    }

    private String url(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
