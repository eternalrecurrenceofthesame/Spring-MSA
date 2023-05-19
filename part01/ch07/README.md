# 리액티브 마이크로서비스 개발하기

논블로킹 동기 REST API 및 비동기 이벤트 기반 리액티브 마이크로서비스를 개발한다! 리액티브를 기반으로 마이크로서비스를 설계하면

유연성,확장성,탄력성을 확보할 수 있다. 

## 논블로킹 동기 API 와 이벤트 기반 비동기 서비스의 선택 기준 
```
* 동기와 비동기란?

동기는 요청후 스레드가 응답을 기다리는 형태를 말하고 비동기란 요청후 스레드가 응답을 기다리지 않고 다른 작업을 수행하는 것을 의미한다. 
```
```
* 런타임 의존성이란?

코드(애플리케이션) 를 실행하는 시점에 결정되는 의존성으로써 객체 사이의 의존성을 의미한다. 일반적으로 추상화된 클래스나 인터페이스에
의존할 때 런타임 의존성을 갖게 된다. https://mangkyu.tistory.com/226 참고 
```
```
리액티브 마이크로서비스를 개발할 때 두 가지 중 어느 하나를 선택해서 개발해야 하는 것은 아니다. 런타임 의존성을 최소화 (느슨한 결합)
해야하기 때문에 동기 API 방식 보다는 이벤트 기반의 비동기 메시지 전달 방식이 선호된다. 

다른 마이크로서비스에 동기로 서비스에 접근하는 대신 런타임에 메시징 시스템에 접근해서 필요한 정보를 얻을 수 있으면 느슨한 결합을 
유지할 수 있다.
```
```
* 논블로킹(리액티브) 동기 API 를 사용하는 것이 유리한 경우

- 최종 사용자가 응답을 기다리는 작업일 때
- 모바일 앱이나 SPA 웹 애플리케이션처럼 동기 API 가 알맞은 클라이언트 플랫폼일때
- 클라이언트가 다른 조직의 서비스에 연결할 때(여러 조직이 공통 메시징 시스템을 공유해서 사용할 수 없는 경우) 
```
### 시스템 환경 구성 
```
- 복합 마이크로서비스가 공개하는 생성, 읽기, 삭제 서비스는 동기 API 를 기반으로 한다. 복합 마이크로서비스는 웹 및 모바일 플랫폼을 대상으로,
시스템 환경을 운영하는 조직이 아닌 다른 조직의 클라이언트를 주로 상대한다고 가정하기 때문에 동기 API 가 적합하다.

- 핵심 마이크로서비스가 제공하는 읽기 서비스는 응답을 기다리는 최종 사용자가 있기 때문에 동기 API 로 개발한다.

- 핵심 마이크로서비스의 생성 및 삭제 서비스는 이벤트 기반 비동기 서비스로 개발한다. 복합 마이크로서비스가 제품 집계정보의 생성 및 삭제를 위해
제공하는 동기 API 는 핵심 서비스가 수신하는 토픽에 생성 및 삭제 이벤트를 게시한 후 바로 200 응답을 반환한다. 254 p

정리하자면

복합 마이크로서비스는 논블로킹 동기 API 로 개발한다
핵심 마이크로서비스는 읽기 API 는 논블로킹 동기 API 로 개발하고, 생성 및 삭제 API 는 이벤트 기반의 비동기 서비스로 개발한다.
```
## 논블로킹(리액티브) 동기 API 개발하기 
```
앞서 설명한 논블로킹 동기 API 부터 개발을 시작한다.
```
### 스프링 데이터 MongoDB 를 사용한 논블로킹(리액티브) 영속성 구현 260 p
```
핵심 마이크로서비스의 리포지토리를 Reactive 스프링 데이터 타입으로 리팩토링한다. persistence (몽고 DB 를 사용하는 핵심 MSA product, recommendation)
```
```
* 스프링 부트 3.0 에서 리액티브 몽고DB 및 임베디드 몽고 사용하는 방법

리액티브 몽고DB 의존성 추가
implementation 'org.springframework.boot:spring-boot-starter-data-mongodb-reactive'
testImplementation 'io.projectreactor:reactor-test'

임베디드 의존성 추가
testImplementation 'de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x:4.5.2'

yml 버전 설정
de:
  flapdoodle:
    mongodb:
      embedded:
        version: 4.0.2

https://stackoverflow.com/questions/74734106/how-to-use-embedded-mongodb-with-springboot-v3-0-0
```
#### + 리액티브 리포지토리 테스트
```
* ProductPersistenceTest 참고 

Mono, Flux 같은 리액티브 스트림 타입을 반환하는 메서드를 테스트하려면 테스트 메서드는 반환된 리액티브 객체에서 결과를 받을 때까지 
기다려야한다.

Mono, Flux 객체의 block() 메서드를 호출해서 결과를 받을 때까지 기다리거나, StepVerifier 헬퍼 클래스를 사용해서 검증 가능한 비동기 
이벤트 시퀀스를 선언하고 verfyComplet() 메서드로 검증 시퀀스를 호출하는 방법을 선택할 수 있다. 259 p
```
```
* 테스트 메서드 설명

StepVerifier.create(repository.deleteAll()).verifyComplete(); 
이런식으로 발행자가 검증 시퀀스를 만들고 트리거메서드를 이용해서 테스트를 진행한다. 

.verfiyCompelte(); : Trigger the verification, expecting a completion signal as terminal event

StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify(); 
예외 검증시 verify() 메서드를 사용하면 구독자가 던진 예외를 검증할 수 있다.

.verfiy(); : Verify the signals received by this subscriber
```
```
* 설정 정보 주입하기 

@DataMongoTest(properties = {"spring.data.mongodb.auto-index-creation: true"}

스프링부트 통합 테스트 애노테이션 처럼 (@SpringBootTest) MongoTest 를 사용할 때도 설정 정보를 주입해줄 수 있다.
필드값에 인덱스 true 를 검증하고 싶다면 위 애노테이션 설정 정보를 추가한다.
```
### 스프링 데이터 JPA 를 사용한 논블로킹(리액티브) 영속성 구현하기
```
리뷰 핵심 마이크로서비스는 JPA 를 사용하기 때문에 구현, 테스트 방식이 다르다.

```
## 핵심 마이크로서비스 논블로킹(리액티브) REST API 개발하기
```
* product,review,recommendation - serviceImpl 참고 
```

