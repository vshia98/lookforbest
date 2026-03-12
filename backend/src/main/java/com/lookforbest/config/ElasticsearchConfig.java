package com.lookforbest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.lookforbest.repository.es")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${app.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUri;

    @Value("${app.elasticsearch.username:}")
    private String username;

    @Value("${app.elasticsearch.password:}")
    private String password;

    @Override
    public ClientConfiguration clientConfiguration() {
        String host = elasticsearchUri.replace("http://", "").replace("https://", "");
        boolean useSsl = elasticsearchUri.startsWith("https://");

        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder =
                ClientConfiguration.builder().connectedTo(host);

        if (useSsl) {
            builder.usingSsl();
        }

        if (!username.isBlank() && !password.isBlank()) {
            builder.withBasicAuth(username, password);
        }

        return builder
                .withConnectTimeout(java.time.Duration.ofSeconds(5))
                .withSocketTimeout(java.time.Duration.ofSeconds(30))
                .build();
    }
}
