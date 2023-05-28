package se.magnus.util.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

public class Event<K, T> { // 이벤트 클래스는 키 및 데이터 필드의 유형을 매개 변수화한 제네릭 클래스이다.

    public enum Type {CREATE, DELETE} // 생성과 삭제를 비동기 이벤트로 관리한다.

    private Event.Type eventType; // 이벤트 유형
    private K key; // 파티션 키가 된다.
    private T data;
    @JsonSerialize(using = LocalDateTimeSerializer.class) // LocalDateTime 직렬화 문제를 해결하기 위한 애노테이션
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime eventCreatedAt;

    public Event() {
        this.eventType = null;
        this.key = null;
        this.data = null;
        this.eventCreatedAt = null;
    }
    public Event(Type eventType, K key, T data){
        this.eventType = eventType;
        this.key = key;
        this.data = data;
        this.eventCreatedAt = LocalDateTime.now();
    }

    /**
     * 메시지 이벤트는 조회만 가능하도록 get 만 구현한다.
     */
    public Type getEventType() {
        return eventType;
    }

    public K getKey() {
        return key;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getEventCreatedAt() {
        return eventCreatedAt;
    }
}
