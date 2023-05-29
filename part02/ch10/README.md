# 스프링 클라우드 게이트웨이를 에지 서버로 사용하기
```
마이크로서비스 환경을 보호하기 위해 에지 서버를 사용한다. 사설 서비스를 외부에서 접근하지 못하게 숨기고 외부 클라이언트가
공개 서비스를 사용할 때 보호한다.

스프링 클라우드 게이트웨이는 리액터 기반의 논블로킹 API 를 사용한다. 넷플릭스 주울(블로킹 API) 에 비해 더 많은 양의 동시 요청을 
처리할 수 있다. 이는 모든 외부 트래픽을 처리하는 에지 서버에 중요한 특성이다.

product-composite 서비스와 검색 서비스인 넷플릭스 유레카가 에지 서버를 통해 공개되고 핵심 마이크로서비스는 외부의 접근이 차단된다.
```

### 시스템 환경에 에지서버 추가하기
```
* docker-compose 참고

모든 수신요청은 에지 서버를 통해서 라우팅된다. 라우팅이란? 네트워크 안에서 통신 데이터를 전송할 때 최적의 경로를 선택하는 과정을 뜻한다. 
에지 서버는 URL 경로를 기반으로 들어오는 요청을 라우팅한다. 

Product Composite 요청은 복합 마이크로서비스로, eureka 요청은 유레카 검색 서버로 라우팅한다. 360 그림 참고 

앞서 도커 컴포즈를 구성할 때 복합 마이크로서비스와 유레카 서버를 외부로 노출했는데 게이트웨이를 사용해서 라우팅 하려면 공개한
서비스의 *포트*를 제거해야 한다. (포트만 제거해줌.)
```
### 스프링 클라우드 게이트웨이 설정
```
게이트웨이 모듈에 도커 파일을 추가하고, 도커 컴포즈에 게이트웨이 에지 서버를 추가했다. dockercompose 참고 
```
### 상태 점검을 게이트웨이에 추가하기
```
게이트웨이를 사용해서 모든 요청을 처리할 수 있도록 설정했으므로 복합 마이크로서비스에서 구현한 상태 점검 클래스(HealthCheckConfiguration) 
를 게이트웨이로 리팩토링한다. 게이트웨이를 에지서버로 추가하면 외부의 상태 점검 요청도 에지 서버를 거쳐야 한다. 

gateway - HealthCheckConfiguration, 부트스트랩 클래스 참고
```

## 스프링 클라우드 게이트웨이 설정 정보구성하기 
```
* 유레카 서버 등록 및 액추에이터와 로깅 레벨 설정하기

gateway yml 파일 참고 (게이트웨이도 유레카 서버에 등록해서 사용한다.)
```
### 라우팅 규칙
```
라우팅이란?

네트워크 안에서 통신 데이터를 전송할 때 최적의 경로를 선택하는 과정을 뜻한다. 에지 서버는 URL 경로를 기반으로 들어오는 
요청을 라우팅한다. 

라우팅 규칙은 자바 DSL 이나 yml 구성 방식을 이용할 수 있다. 보통 yml 구성 방식이 선호된다(편리함) 365 p

gateway yml 참고 
```
```
* 라우팅 규칙 정의

1. 조건자(predicate): 수신되는 HTTP 요청 정보를 바탕으로 경로를 선택한다.
2. 필터(filter): 요청이나 응답을 수정한다
3. 대상 URI(destination URI): 요청을 보낼 대상 (라우팅 되어 호출하는 URI 를 의미한다)
4. ID: 라우트 경로 이름

cloud.spring.io/spring-cloud-gateway/single/spring-cloud-gateway.html // 사용 가능한 조건자, 필터의 전체 목록
```
```
* product-composite API 로 요청 라우팅하기 

spring.cloud.gateway.routes:

  - id: product-composite # 경로의 이름 
    uri: lb://product-composite # 검색 서비스 넷플릭스 유레카에 요청할 주소 
    predicates: # 조건자 (조건에 맞는 경우 유레카 주소를 호출한다)
      - Path=/product-composite/**
      
lb:// 는 스프링 클라우드 게이트웨이가 클라이언트 측 로드 밸런스를 사용해 검색 서비스에서 대상을 찾도록 지시한다.
(참고로 로드 밸런싱은 현재 시점에서 게이트웨이와 복합 마이크로서비스에서 사용한다.)

요청 라우팅 예시
curl http://localhost:8080/product-composite/2 

Pattern "/product-composite/**" matches against value "product-composite/2"
Route matched: product-composite
LoadBalancerClientFilter url chosen: http://b8013440aea0:8080/product-composite/2

필터는 설정하지 않으면 자동으로 지정되는듯? 

8080 으로 호출하는 이유는 게이트웨이를 통한 호출이기 때문!! 게이트웨이 포트 8080 을 통해서 라우팅된다. 
```
```
* 유레카 서버의 API or 웹 페이지로 요청 라우팅하기

에지 서버 게이트웨이로 전송된 경로가 /eureka/api/ 로 시작하는 요청은 유레카 API 에 대한 호출로 처리한다
에지 서버 게이트웨이로 전송된 경로가 /eureka/web/ 으로 시작하는 요청은 유레카 웹 페이지에 대한 호출로 처리한다.

gateway yml 참고 

# 유레카 API 요청 라우팅 (유레카를 API 통신으로 요청함)
- id: eureka-api
  uri: http://${app.eureka-server}:8761
  predicates:
  - Path=/eureka/api/{segment} # path{segment} 부분은 0 개 이상의 문자와 일치
  filters:
  - SetPath=/eureka/{segment} # 필터로 요청이나 응답을 수정한다. Path {} 값이 Set {} 값을 대체한다.
    
요청 라우팅 예시
curl -H "accept:application/json" localhost:8080/eureka/api/apps -s | \ jq -r .applications.application[].
instance[].instanceId

요청시 유레카에 등록된 주소값을 반환 8080 으로 호출하는 이유는 게이트웨이를 통한 호출이기 때문!! 
게이트웨이 포트 8080 을 통해서 라우팅된다. 
```
```
* ${app.eureka-server}

도커 프로필 설정을 하지 않으면 localhost 로 등록된 유레카 서버로 요청한다.
도커 프로필 설정을 사용하면 프로필 설정으로 지정한 이름으로 검색하게 된다. 

참고로 도커 컨테이너로 서비스를 실행할 때는 DNS 이름이 eureka 인 컨테이너에서 넷플릭스 유레카 서버가
실행되며, docker 프로필을 사용하므로 속성 값 eureka 를 사용할 수 있게 된다 ? 367 p 
```

### 조건자와 필터를 이용한 요청 라우팅 처리
```
* https://httpstat.us/ 을 응용해서 호스트 헤더 기반 라우팅 실습하기

스프링 클라우드 게이트웨이는 수신 요청의 호스트 이름(localhost 부분) 을 사용해 요청을 라우팅할 곳을 결정한다.

- id: host_route_200
  uri: http://httpstat.us
  predicates:
    - Host=i.feel.lucky:8080
    - Path=/headerrouting/** 
  filters:
    - SetPath=/200

i.feel.lucky:8080/headerrouting 을 호출했을 때 라우팅되는 조건 https://httpstat.us/200 으로 라우팅된다.

gateway yml 참고 
```






