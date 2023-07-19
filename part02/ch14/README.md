# 분산 추적

집킨을 사용해서 외부에서의 API 호출을 추적하고 추적 정보를 저장 및 시각화 한다.

## 집킨을 사용한 분산추적 메커니즘
```
전체 워크 플로 (workflow) 에 대한 추적 정보는 추적 (trace) 혹은 추적 트리 (trace tree) 라고 부르고, 기본 작업 단위라고 할 수 있는 트리의
일부분을 스팬(span) 이라고 부른다. 스팬은 하위 스팬으로 구성돼서 추적 트리를 형성한다.

집킨은 추적 정보를 메모리나 아파치 카산드라, 일래스틱서치, MySQL 에 저장할 수 있으며 이번 단원에서는 메모리에 추적 정보를 저장한다.
zipkin.io/pages/extensions_choices 참고
```
## 마이크로서비스에 집킨 추가하기
```
1. 의존성 추가 (스프링 부트 3.0.6 버전 기준)

implementation 'io.micrometer:micrometer-tracing-bridge-otel'
implementation 'io.opentelemetry:opentelemetry-exporter-zipkin'

추적하고 싶은 마이크로서비스에 위 의존성을 추가한다. 스프링 클라우드에서는 게이트웨이, 유레카, 권한 부여서버에 추가했으며 그외 각각의
마이크로서비스에 위 의존성을 추가한다.

implementation 'net.ttddyy.observation:datasource-micrometer-spring-boot:1.0.0'
JDBC 를 사용하는 review 마이크로서비스는 위 의존성과 이것을 추가로 설정한다. 
```
```
2. 도커 컴포즈에 집킨 추가하기

docker-compose 참고 
```
```
3. 공통 appliation.yml 파일에 집킨 구성 추가

application.yml 참고 
```
## 분산 추적 테스트하기
```
이전 챕터에서 설명한 것 처럼 데이터를 만들고 조회한다. http://localhost:9411/zipkin/ 집킨 ui 에서 요청을 추적할 수 있다.
도커 컴포즈에 RabbitMQ 관련 설정을 추가하면 래빗 ui 에서도 집킨으로 전송된 추적 정보를 모니터링 할 수 있다.

docker-compose 참고 
```
