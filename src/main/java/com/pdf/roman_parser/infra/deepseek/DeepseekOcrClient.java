package com.pdf.roman_parser.infra.deepseek;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Map;

@Component
public class DeepseekOcrClient {

    private final WebClient webClient;

    public DeepseekOcrClient(WebClient.Builder builder,
                             @Value("${deepseek.base-url:https://api.deepseek.com}") String baseUrl,
                             @Value("${deepseek.api-key:}") String apiKey) {
        this.webClient = builder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public Mono<String> ocr(byte[] pngBytes) {
        if (pngBytes == null || pngBytes.length == 0) {
            return Mono.just("");
        }
        String base64 = Base64.getEncoder().encodeToString(pngBytes);
        Map<String, Object> payload = Map.of(
                "model", "deepseek-ocr-2",
                "image", base64,
                "output", "text"
        );

        return webClient.post()
                .uri("/v1/ocr")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .mapNotNull(response -> (String) response.get("text"));
    }
}
