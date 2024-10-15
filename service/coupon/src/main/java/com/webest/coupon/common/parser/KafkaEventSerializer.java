package com.webest.coupon.common.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaEventSerializer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private KafkaEventSerializer() {
        throw new UnsupportedOperationException("이 클래스는 인스턴스 생성을 지원하지 않습니다.");
    }

    // 직렬화 (객체 -> JSON 문자열)
    public static <T> String serialize(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object: {}", object, e);
            throw new RuntimeException("Serialization error", e);
        }
    }

    // 역직렬화 (JSON 문자열 -> 객체)
    public static <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON: {}", json, e);
            throw new RuntimeException("Deserialization error", e);
        }
    }
}