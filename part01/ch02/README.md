# 스프링 부트 소개 
```
스프링 부트는 스프링 프레임워크와 서드파티 제품으로 구성된 핵심 모듈의 설정 방식을 개선해 상용 스프링 애플리케이션을
빠르게 개발할 수 있도록 지원하는 프레임워크다. 

적용 구성을 최소화하고 필요한 경우에만 구성을 작성해 기존 규칙을 대신한다. 

구성이 필요할 때는 xml 보다 자바 코드, 애노테이션, yml 파일을 사용하는 것이 좋다. 
https://github.com/eternalrecurrenceofthesame/Spring5/blob/main/part1/ch05/README.md 참고
```
```
* JAR 

스프링 부트는 JAR 파일 기반의 런타임 모델을 지원한다. JAR 를 사용하면 아파치 톰캣과 같은 Java EE 웹 서버를 별도로 설치하지 않고도
사용할 수 있다. 76 p
```
```
* 부트 스트랩 클래스 예시

@SpringBootApplication
@ComponentScan({"se.magnus.myapp", "se.magnus.utils"}) // 컴포넌트 스캔 범위를 세밀하게 조정할 수 있다.
```
## 스프링 웹플럭스 
```
https://github.com/eternalrecurrenceofthesame/Spring5/tree/main/part3/ch10
및 교재 82 p 참고 
```
## 스프링 폭스 
```
RESTful 서비스를 만들 때 API 를 문서화 하기 위해 스웨거를 사용하는 경우가 많다. 스프링 폭스는 스프링 프레임워크와는 별개의 오픈소스
프로젝트로써 런타임에 스웨거 기반의 API 문서를 생성한다. 
```
## 스프링 클라우드 스트림 
```
스프링 클라우드 스트림은 게시-구독 통합 패턴을 기반으로 하는 메시징 방식의 스트리밍 추상화를 제공한다.
(클라우드 스트림은 아파치 카프카, RabbitMQ 를 기본 지원한다.)

메시지(Message): 메시징 시스템과 주고받는 데이터를 설명하는 데이터 구조
게시자(Publisher): 메시징 시스템에 메시지를 보낸다.
구독자(Subscriber): 메시징 시스템에서 메시지를 받는다.
채널(Channel): 메시징 시스템과 통신하는 데 사용한다. 게시자는 출력 채널을 사용하고 구독자는 입력 채널을 사용한다.
바인더(Binder): 특정 메시징 시스템과의 통합 기능을 제공한다 JDBC 가 특정 데이터베이스를 지원하는 것과 유사하다.
```
### 스프링 클라우드 스트림을 사용한 메시지 송수신 예제
```
* 메시지 클래스

public class MyMessage{
private String attribute1 = null;}

* 게시자 클래스

@EnableBinding(Source.class) // 스프링 클라우드 스트림이 제공하는 기본 출력 채널 (Source)
public class MyPublisher{

@Autowired private Source mySource;

public String processMessage(MyMessage message){ // 채널에 메시지를 게시하는 메서드
mysource.output().send(MessageBuilder.withPayload(message).build));}

* 구독자 클래스
@EnableBinding(Sink.class) // 스프링 클라우드 스트림이 제공하는 기본 입력 채널(Sink)
public class MySubscriber{

@StreamListener(target = Sink.INPUT)
public void receive(MyMessage message){
Log.ionfo("Received: {}", message); }
```
```
* 바인딩하기 (토픽과 채널을 연결하는 작업이다.)

바인딩 하려면 빌드 파일에 스타터 의존성을 추가하면 된다.
implementation 'org.springframework.cloud:spring-cloud-starter-stream-rabbit:4.0.2'
```
```
구독자가 게시자의 메시지를 수신하려면 동일한 목적지를 사용하도록 입력 채널과 출력 채널을 구성한다.

* 게시자
spring.cloud.stream:
  default.contentType: application/json
  bindings.output.destination: mydestination

* 구독자
spring.cloud.stream:
  default.contentType: application/json
  bindings.input.destination: mydestination

```



