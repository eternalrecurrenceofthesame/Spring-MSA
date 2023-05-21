# 리액티브 마이크로서비스 개발하기
```
논블로킹 동기 REST API 및 비동기 이벤트 기반 리액티브 마이크로서비스를 개발한다! 리액티브를 기반으로 마이크로서비스를 설계하면 
유연성,확장성,탄력성을 확보할 수 있다. 

여기서 설계하는 리액티브 기반의 마이크로서비스는 순수 리액티브만을 사용하는 것이 아니다 기본적으로 동기 REST API 를 기반으로 하면서
비동기 이벤트 처리를 추가한 마이크로서비스를 혼합한 형태가 된다. 
```
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
리액티브 마이크로서비스를 개발할 때 앞서 설명한 두 가지 설계 방법 중 하나만 선택해서 개발해야 하는 것은 아니다. 런타임 의존성을 
최소화 (느슨한 결합) 해야하기 때문에 동기 API 방식 보다는 이벤트 기반의 비동기 메시지 전달 방식이 선호된다. 

다른 마이크로서비스에 동기로 서비스에 접근하는 대신 런타임에 메시징 시스템에 접근해서 필요한 정보를 얻을 수 있으면 느슨한 결합을 
유지할 수 있다.
```
```
* 리액티브 동기 API 방식을 사용하는 것이 유리한 경우 254 p 

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

복합 마이크로서비스는 동기 API 로 개발한다
핵심 마이크로서비스는 읽기 API 는 동기 API 로 개발하고, 생성 및 삭제 API 는 이벤트 기반의 비동기 서비스로 개발한다.
```
## 논블로킹 동기 API 개발하기 
```
* composite-microservice ,product,review,recommendation - serviceImpl 참고 

복합 마이크로서비스 & 핵심 마이크로서비스의 읽기 메서드를 구현한다. 

참고로 리액티브 스트림 타입을 자료형으로 받는 메서드의 경우 구독을 하지 않아도 프레임워크가 자동으로 호출해준다. 
https://github.com/eternalrecurrenceofthesame/Spring5/tree/main/part3/ch11
```
## 복합 마이크로서비스의 논블로킹 동기 REST API 개발하기 
```
* product-service. serviceImpl 생성 및 삭제 구현 참고 
```
### 스프링 데이터 MongoDB 를 사용해서 영속성 구현 260 p
```
핵심 마이크로서비스의 리포지토리를 Reactive 스프링 데이터 타입으로 리팩토링한다. persistence (몽고 DB 를 사용하는 핵심 MSA product, recommendation)
```
```
* 스프링 부트 3.0 에서 도커 이미지를 테스트 컨테이너로 사용하는 몽고 DB 테스트하기 (새로운 방법)

의존관계 추가
implementation platform('org.testcontainers:testcontainers-bom:1.17.6')
testImplementation 'org.testcontainers:testcontainers'
testImplementation 'org.testcontainers:junit-jupiter'
testImplementation 'org.testcontainers:mongodb'

yml 설정
spring.data.mongodb:
  host: localhost
  port: 27017
  database: product-db
  
도커 추가후 리팩토링 예정 
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
#### + 몽고 DB 를 사용한 리포지토리 테스트
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
#### + product-service 테스트하기
```
* product-service  
```
### 스프링 데이터 JPA 를 사용해서 영속성 구현하기
```
review 핵심 마이크로서비스는 JPA 를 사용하기 때문에 서비스 구현 및 테스트 방식이 다르다. review-service 참고
```
```
* Scheduler 를 사용해서 스프링 데이터 JPA 블로킹 엔티티 조회하기

리액티브 스트림을 사용하면 스프링 데이터 JPA 에서 조회한 블로킹 상태의 엔티티를 리액티브 스트림으로 바꿔서 반환해야한다.
논블로킹 리액티브 스트림에 영향을 주지 않으려면 스케줄러를 사용해야한다.

스케줄러란? 일정 수의 스레드를 보유한 전용 스레드풀의 스레드에서 블로킹 상태의 코드를 실행하는 브로커로써 조회한 블로킹 엔티티를 
플럭스로 매핑하는 과정에서 블로킹 상태로 방치되는 것을 별도의 스레드풀을 사용해서 처리 함으로써 .subscribeOn(scheduler);

마이크로서비스에서 사용할 스레드의 고갈을 방지하고 마이크로서비스의 논블로킹 처리에 영향을 주지 않게 한다. 264 p
스케줄러를 사용하는 review - getReviews 메서드 참고 
```
```
* 자바 설정으로 스레드 풀 구성하기

review 부트스트랩 클래스 참고
```
#### + review-service 테스트하기 
```
review-service PersistenceTests 참고 
```
## 이벤트 기반 비동기 서비스 개발하기

스프링 부트 3.0 기반의 스프링 클라우드 스트림을 이용해서 비동기 메시지를 처리한다. 이 프로그래밍 모델은 사용하는 

메시징 시스템(카프카, 래빗) 과 독립적으로 사용할 수 있다. **교재에는 없는 최신 버전!!** 
```
* composite-microservice ,product 참고 

핵심 마이크로서비스의 생성, 삭제 서비스는 이벤트 기반의 비동기 서비스로 개발. 복합 마이크로서비스가 생성 및 삭제
이벤트를 각 핵심 서비스의 토픽에 게시하고 핵심 마이크로서비스의 처리를 기다리지 않고 호출자에게 OK 응답을 반환한다.  

270 그림 참고 
```
### 토픽 및 이벤트 정의하기
```
복합 마이크로서비스는 토픽에 메시지를 게시하고 핵심 마이크로서비스는 관심있는 토픽을 구독해서 메시지를 수신한다.

메시징 시스템은 보통 헤더와 본문으로 구성된 메시지를 다루는데 어떤 상황이 발생했다는 것을 설명하는 메시지가 이벤트이다.
이벤트 메시지 본문에는 이벤트 유형, 이벤트 데이터, 타임 스탬프가 들어 있다.
```
```
* api 통합 모듈에 이벤트 정의하기 api event참고

type: 이벤트 유형(ex: 생성, 삭제)
key: 데이터 식별을 위한 키(ex: 제품 ID)
data: 실제 이벤트 데이터
timestamp: 이벤트 발생 시간 
```
```
* 스프링 클라우드 의존성 추가

spring starter io - cloud stream, kafka, rabbit 의존성을 복합 서비스에 추가한다 
```
### 복합 서비스 컴포넌트에서 이벤트 생성하고 게시하기 
```
* productcompositeservice - ProductCompositeIntegration 참고 
```
```
1. StreamBridge 만들기

StreamBridge - A class which allows user to send data to an output(출력) binding  
Json 바디를 메시지로 생산하는 역할을 한다. 생산자(Output Binding)

https://kouzie.github.io/spring-cloud/Spring-Cloud-spring-cloud-stream/#spring-cloud-stream 참고

StreamBridge 는 부트스트랩 클래스에서 생성해서 복합서비스에서 주입 받아서 사용했다. 
```
```
+ OpenApi 를 사용한 문서화 

앞선 단원에서 스웨거를 사용해서 api 문서를 만들었다면 이번에는 OpenApi 를 사용해서 조금 더 쉽게 API 문서를 만들어본다!

implementation 'org.springdoc:springdoc-openapi-starter-webflux-ui:2.0.2' 추가
api, 복합 마이크로서비스 부트스트랩 클래스, yml 구성 정보 참고.
```
```
2. 스케줄러 정의하기

스케줄러란 앞서도 설명했지만 블로킹 상태의 객체를 스레드 풀에서 작업하는 역할을 한다. 이렇게 함으로써 다른 마이크로서비스에서
사용할 스레드의 고갈을 방지할 수 있다. 

스케줄러는 StreamBridge 가 이벤트 메시지를 생성하는 것을 실행하는 역할을 한다. 스케줄러는 부트스트랩 클래스에 정의하고 빈으로 
주입받아서 통합 컴포넌트 클래스에서 사용한다. 

ProductCompositeIntegration 참고  // 전체적인 이해가 필요하면 createProduct 메서드를 참고한다.
```
### 복합 서비스 API 구현하기
```
* productcompositeservice - ProductCompositeServiceImpl 참고
```
```
앞서 설명했듯 복합 마이크로서비스 클라이언트는 다른 조직의 서비스에 연결해야 하므로 동기 API 로 개발한다. 
```

여기까지 해서 각각의 핵심마이크로 서비스와 복합 마이크로서비스의 기본 토대를 구현했다. 지금부터는 각각의 핵심 마이크로서비스가

사용하는 메시지 프로세서를 자바빈 설정으로 구현한다.

## 복합 마이크로서비스 yml 설정하기
```
* 소비자 그룹 만들기

메시지 소비자인 핵심 마이크로서비스 product 의 인스턴스가 늘어나면 product 의 모든 인스턴스가 같은 메시지를 소비하는
문제가 생긴다. 이런 문제를 해결하려면 인스턴스를 하나의 소비자 그룹으로 묶어서 사용하면 된다.

스프링 클라우드 스트림은 그룹화된 소비자 인스턴스 중 하나의 인스턴스로만 토픽에 게시된 메시지를 전달한다.

spring.cloud.stream:
  defaultBinder: // rabbit or kafka 
  default.contentType: application/json
  bindings.messageProcessor-in-0:
    destination: products
    group: productsGroup
    
```
```
* 재시도 및 데드 레터 대기열 

소비자인 핵심 마이크로서비스에서 메시지 내용이 잘못되거나 네트워크 오류로 데이터베이스에 연결할 수 없어서 메시지 
처리에 실패하면 실패한 메시지는 성공적으로 처리할 때까지 대기열로 보내지거나 사라진다. 

일시적인 네트워크 오류의경우 여러번의 재시도로 메시지 처리에 성공할 수 있다. 이런 경우를 대비해서 결함 분석 및 수정을 위한
메시지를 다른 저장소 (데드 레터 대기열) 로 이동하기 전 재시도 횟수를 소비자측에서 지정할 수 있어야 한다. 

재시도에 따른 과부하를 피하려면 재시도 횟수를 지정하고 재시도 간격을 넓히는 것이 바람직하다.

spring.cloud.stream.bindings.messageProcessor-in-0.consumer:
  maxAttempts: 3  // 재시도 3 번
  backOffInitialInterval: 500  // 첫 번째 실패 후 재시도 간격 500 ms
  backOffMaxInterval: 1000  // 두 번째 세 번째 재시도 간격 1000 ms
  backOffMultiplier: 2.0
  
spring.cloud.stream.rabbit.bindings.messageProcessor-in-0.consumer: // 래빗
  autoBindDlq: true
  republishToDlq: true
  
spring.cloud.stream.kafka.bindgs.messageProcessor-in-0.consumer:  // 카프카 
  enableDlq: true
```
```
* 순서 보장 및 파티션

파티션을 사용하면 성능과 확장성을 잃지 않고 전송됐을 때의 순서 그대로 메시지를 전달할 수 있다. 토픽에 메시지가 전송된 
대로 비즈니스 로직을 처리해야 한다면 여러 개의 소비자 인스턴스를 사용해서 성능을 높일 수 없다. (소비자 그룹을 사용할 수 없음)

이렇게되면 들어오는 메시지를 처리할 때 발생하는 지연 시간이 지나치게 길어질 수 있다.

복합 마이크로서비스에 파티션(하위 토픽) 을 도입하면 같은 키를 가진 메시지 사이의 순서를 보장하기 위한 파티션(하위 토픽)을 사용할 
수 있다. 같은 키를 가진 메시지는 언제나 같은 파티션에 배치되고 동일 파티션에 속한 메시지는 순서가 보장된다.

// 복합 마이크로 서비스설정

spring.config.activate.on-profile: streaming_partitioned

spring.cloud.stream.bindings.products-out-0.producer:
  partition-key-expression: headers['partitionKey']
  partition-count: 2

// 핵심 마이크로서비스 설정(소비자 파티션 지정)

spring.config.activate.on-profile: streaming_partitioned

spring.cloud.stream.bindings.messageProcessor-in-0.consumer:
  partitioned: true
  instanceCount: 2    

쉽게말해서 복합 마이크로서비스에서 메시지를 보낼때 아이디값을 같이 보내면 같은 아이디가 같은 파티션에 들어가게 되고
순차적으로 메시지 로직을 수행할 수 있다는 의미다. 275 그림 참고

ProductCompositeIntegration 헤더값 설정 참고 
```
```
+ 복합 마이크로서비스에서 래빗 상세 설정하기 

spring.cloud.stream:
  defaultBinder: rabbit
  default.contentType: application/json
  bindings:
    products-out-0:
      destination: products
      producer:
        required-groups: auditGroup 

래빗을 사용하면 성공적으로 처리된 이벤트는 제거된다. 래빗에서 각 토픽에 게시된 이벤트를 확인할 수 있도록 별도의
소비자 그룹인 auditGroup 에 저장하도록 구성하면 추후 검사를 위해 이벤트를 저장하는 별도의 대기열이 생성된다.
```
### 복합 마이크로서비스 메시징 테스트하기
```
* 
```
#### + LocalDateTime 직렬화에 실패하는 경우 해결방법
```
모듈 추가
implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310'
implementation 'com.fasterxml.jackson.core:jackson-databind' 

LocalDate 를 사용하는 필드에 애노테이션 추가
@JsonSerialize(using = LocalDateTimeSerializer.class)
@JsonDeserialize(using = LocalDateTimeDeserializer.class)

https://velog.io/@sago_mungcci/%EC%8A%A4%ED%94%84%EB%A7%81-Java-8-LocalDateTime-%EC%A7%81%EB%A0%AC%ED%99%94-%EC%97%AD%EC%A7%81%EB%A0%AC%ED%99%94-%EC%98%A4%EB%A5%98 참고

```

## 핵심 마이크로서비스 (메시지 소비자) yml 구성 설정하기  

복합 마이크로서비스 및 핵심 마이크로서비스의 yml 구성 파일을 참고한다. 

```
* spring cloud function 사용하기 

spring cloud function 은 빈으로 등록된 Consumer, Supplier, Function 타입을 구성한 메서드를 사용해서 이벤트를 
처리하기 전 핸들링 용도로 사용할 수 있다. (추가 설명 필요) 

yml 설정
spring.cloud.function.definition: messageProcessor  

자바 설정
MessageProcessorConfig 참고. 간단하게 이벤트가 만들어진 위치를 로그로 찍는 메서드를 추가했다. 

참고 
https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream.html#spring_cloud_function 
```
```
* 바인더, 데스티네이션, 그룹

바인더로 래빗을 호출할 수 있다. 바인더는 외부 메시지 시스템과 통합을 담당한다.

클라우드 스트림의 추가 정보는 아래 블로그를 참고 
https://coding-start.tistory.com/139 
```
