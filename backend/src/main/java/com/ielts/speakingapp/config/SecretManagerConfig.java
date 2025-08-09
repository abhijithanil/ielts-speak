package com.ielts.speakingapp.config;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Configuration class for managing secrets from Google Cloud Secret Manager.
 * This class defines the beans for the Secret Manager client and the specific
 * secrets required by the application.
 */
@Configuration
@Slf4j
public class SecretManagerConfig {

    /**
     * The project ID for Google Cloud services.
     * This is typically set in application.properties or environment variables.
     */
    @Value("${gcp.project-id}")
    private String projectId;

    /**
     * Creates a singleton bean for the SecretManagerServiceClient.
     * This bean is created first and its lifecycle (including closing the client)
     * is managed automatically by Spring.
     *
     * @return An initialized instance of SecretManagerServiceClient.
     * @throws IOException If the client fails to initialize.
     */
    @Bean
    public SecretManagerServiceClient secretManagerServiceClient() throws IOException {
        // This creates a client with default credentials and settings.
        return SecretManagerServiceClient.create();
    }

    /**
     * Creates a bean for the DeepSeek API key by fetching it from Secret Manager.
     * This method depends on the secretManagerServiceClient bean, which Spring
     * will automatically inject as a parameter.
     *
     * @param client The singleton SecretManagerServiceClient bean.
     * @return The DeepSeek API key as a String.
     */
    @Bean("deepSeekApiKey")
    public String getDeepSeekApiKey(SecretManagerServiceClient client) {
        log.info("Fetching DeepSeek API key from Secret Manager");
        try {
            String secretId = "DEEPSEEK_API_KEY";
            String versionId = "latest";
            String resourceName = String.format("projects/%s/secrets/%s/versions/%s", projectId, secretId, versionId);

            return client.accessSecretVersion(resourceName)
                    .getPayload()
                    .getData()
                    .toStringUtf8();

        } catch (Exception e) {
            // It's better to fail fast if a critical secret can't be loaded.
            throw new RuntimeException("Failed to access DeepSeek API key from Secret Manager", e);
        }
    }

    /**
     * Creates a bean for the JWT secret key by fetching it from Secret Manager.
     * This method also depends on the secretManagerServiceClient bean.
     *
     * @param client The singleton SecretManagerServiceClient bean.
     * @return The JWT secret key as a String.
     */
    @Bean("secretJwtKey")
    public String getJwtSecretKey(SecretManagerServiceClient client) {
        log.info("Fetching JWT Secret Key from Secret Manager");
        try {
            String secretId = "JWT_SECRET";
            String versionId = "latest";
            String resourceName = String.format("projects/%s/secrets/%s/versions/%s", projectId, secretId, versionId);

            return client.accessSecretVersion(resourceName)
                    .getPayload()
                    .getData().
                    toStringUtf8();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access JWT secret key from Secret Manager", e);
        }
    }

    @Bean("publicUiUrl")
    public String getPublicUiUrl(SecretManagerServiceClient client) {
        log.info("Fetching Public UI URL from Secret Manager");
        try {
            String secretId = "PUBLIC_UI_URL";
            String versionId = "latest";
            String resourceName = String.format("projects/%s/secrets/%s/versions/%s", projectId, secretId, versionId);

            return client.accessSecretVersion(resourceName)
                    .getPayload()
                    .getData()
                    .toStringUtf8();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access Public UI URL from Secret Manager", e);
        }
    }
}
