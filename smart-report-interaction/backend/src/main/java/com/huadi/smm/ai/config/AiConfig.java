package com.huadi.smm.ai.config;

import com.huadi.smm.ai.AiClient;
import com.huadi.smm.ai.impl.MockAiClient;
import com.huadi.smm.ai.impl.OpenAiCompatibleClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AiConfig {

    @Bean
    public RestTemplate aiRestTemplate(AiProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getTimeout());
        factory.setReadTimeout(props.getTimeout());
        return new RestTemplate(factory);
    }

    @Bean
    public AiClient aiClient(AiProperties props, RestTemplate aiRestTemplate) {
        if (props.getApiKey() != null && !props.getApiKey().trim().isEmpty()) {
            return new OpenAiCompatibleClient(props, aiRestTemplate);
        }
        return new MockAiClient();
    }
}
