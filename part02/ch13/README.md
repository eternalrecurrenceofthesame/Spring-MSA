# Resilience4j 를 사용해서 탄력성 개선하기
```
Resilience4j 를 사용해서 대규모 마이크로서비스 시스템에서 발생하는 느리거나 응답하지 않는 downstream 마이크로서비스로부터
오는 피해를 최소화 할 수 있으며 시간 초과, 재시도 메커니즘을 사용해서 빈번하게 발생하는 오류를 방지할 수 있다.
```
```
참고로 에지 서버에서는 Resilience4j 를 사용할 수 없다. 에지서버에 대한 지원을 추가한다는 소식이 있다.
spring.io/blog/2019/04/16/introducing-spring-cloud-circuit-breaker 참고 
```
## Resilience4j 의 서킷 브레이커와 재시도 메커니즘 소개

재시도와 서킷 브레이커 메커니즘은 마이크로서비스와 같은 동기 방식으로 연결되는 소프트웨어 컴포넌트에 특히 유용하다. 455 p 

### 서킷 브레이커 소개
```
서킷 브레이커는 다량의 오류 감지시 서킷을 열어 새 호출을 받지 않는다. 서킷이 열려있는 상태에서 새 
요청을 하면 폴백 메서드로 호출을 리디렉션한다.

폴백 메서드로 비즈니스로직을 구현해서 로컬 캐시의 데이터를 반환하거나 오류 메시지를 반환할 수 있다.

일정 시간이 지나면 서킷 브레이커는 반 열림 상태로 전환돼 새로운 호출을 허용한다. 이를 통해 문제를
일으킨 원인이 사라졌는지 확인하고 새로운 오류를 감지하면 다시 서킷을 열고 실패 로직을 수행한다.

오류가 사라졌다면 서킷을 닫고 정상 작동 상태로 돌아간다. 마이크로서비스는 이런 방법으로 문제에 대한
탄력성을 가지며 이는 동기 방식으로 통신하는 MSA 환경의 필수 기능이다. 457 p

서킷 브레이커의 현재 상태는 마이크로서비스의 액추에이터의 상태 점검 엔드포인트 (actuator/health) 를
사용해서 모니터링 할 수 있다.

서킷 브레이커는 상태 전이 등의 이벤트를 액추에이터 엔드포인트(/actuator/circuitbreakerevents) 에
게시한다.

서킷 브레이커는 스프링 부트의 메트릭 시스템과 통합돼 있으며, 이를 이용해 프로메테우스와 같은 모니터링
도구에 메트릭을 게시할 수 있다. 
```
### 재시도 메커니즘 소개
```
재시도는 일시적인 네트워크 결함과 같은 무작위로 드물게 발생하는 오류에 매우 유용하다. 재시도를 사용하기
위한 조건은 대상 서비스에 멱등성이 있어야 한다는 점이다.

같은 요청 매개 변수로 서비스를 여러번 호출하더라도 항상 같은 결과를 반환해야 한다 일반적으로 정보를 읽는
작업은 멱등성이 있지만 정보를 생성하는 작업에는 멱등성이 없다.

예를 들면 첫번째 주문을 생성한 후 네트워크 문제로 응답을 받지 못했더라도 재시도 매커니즘으로 인해 2 개의
주문이 생성되면 안 된다.

Res4j 는 서킷 브레이커와 같은 방식으로 재시도와 관련된 이벤트 및 메트릭 정보를 공개하지만 상태 정보는
제공하지 않고 이벤트에 관한 정보는 액추에이터 엔드포인트(/actuator/retryevents) 에서 얻을 수 있다.
```
```
(주의) 재시도 및 서킷 브레이커 설정을 구성할 때 의도한 재시도 횟수가 완료되기 전에 서킷 브레이커가 서킷을
열게 하면 안 된다.
```
## 서킷 브레이커 및 재시도 API 추가
```
우선 서킷 브레이커 및 재시도 메커니즘을 테스트하기 위해 임의로 오류를 발생시키는 코드를 API 인터페이스에 추가한다.
파라미터 값으로 지연 시간과 실패 퍼센트를 받아서 임의로 지연 및 실패를 구현한다.

Product-composite, Product 의 getCompositeProduct, getProduct 메서드 참고
```
### 서킷 브레이커 및 재시도 구현하기 
```
1. 의존성 추가

서킷 브레이커 논리를 적용할 Product-composite 에 의존성을 추가한다.

	// resilience4 추가
	implementation "io.github.resilience4j:resilience4j-spring-boot3:${resilience4jVersion}"
	implementation "io.github.resilience4j:resilience4j-reactor:${resilience4jVersion}"
	implementation 'org.springframework.boot:spring-boot-starter-aop'
```
```
2. Product-composite 및 Product 모듈의 getProduct 메서드 리팩토링

앞서 파라미터 값으로 delay, faultPercent 를 추가했다. 상품을 호출할 때 서킷 브레이커를 테스트 할 수 있도록
이 값을 사용할 수 있도록 메서드를 리팩토링 한다.
```
```
3. 서킷 브레이커 구성 설정 만들기

각 구성 설정을 간단하게 설명하고 넘어간다. product-composite.yml 참고 

slideWindowType : COUNT_BASED
- If the sliding window is COUNT_BASED, the last slidingWindowSize calls are recorded and aggregated.

https://resilience4j.readme.io/docs/circuitbreaker#create-and-configure-a-circuitbreaker 참고
```
#### + 슬라이딩 윈도우란?
```
슬라이딩 윈도(Sliding window)는 두 개의 네트워크 호스트간의 패킷의 흐름을 제어하기 위한 방법이다.

슬라이딩 윈도는 일단 '윈도(메모리 버퍼의 일정 영역)'에 포함되는 모든 패킷을 전송하고, 그 패킷들의 전달이
확인되는대로 이 윈도를 옆으로 옮김(slide)으로서 그 다음 패킷들을 전송하는 방식이다.

https://ko.wikipedia.org/wiki/%EC%8A%AC%EB%9D%BC%EC%9D%B4%EB%94%A9_%EC%9C%88%EB%8F%84 참고
```
```
* @Retry 애노테이션 예외 470p

참고로 yml 구성 설정에서 예외를 지정하지 않으면 @Retry 애노테이션이 붙은 메서드에서 던진 예외는 RetryExceptionWrapper
예외로 wrapping 된다.
```
## 서킷 브레이커 및 재시도 메커니즘 테스트하기

### closed 테스트(정상 요청)
```
1. 액세스 토큰 획득 및 테스트 데이터 추가 (포스트맨)

https://my.redirect.uri 
https://localhost:8443/oauth2/authorize : 권한 부여 서버 엔드포인트
https://localhost:8443/oauth2/token
writer
secret-writer
product:write

username, password


{"productId":1, "name":"product name C", "weight":300, 
"recommendations":
[{"recommendationId":1, "author":"author 1","rate":1,"content":"content 1"},
{"recommendationId":2, "author":"author 2","rate":2,"content":"content 2"},
{"recommendationId":3, "author":"author 3","rate":3,"content":"content 3"}],
"reviews":
[{"reviewId":1, "author":"author 1", "subject":"subject 1","content":"content 1"},
{"reviewId":2, "author":"author 2", "subject":"subject 2","content":"content 2"},
{"reviewId":3, "author":"author 3", "subject":"subject 3","content":"content 3"}]}
```
```
2. 저장한 데이터 조회하기

https://localhost:8443/product-composite/1

docker-compose exec -T product-composite curl -s http://product-composite:8080/actuator/health | jq -r
.components.circuitBreakers.details.product.details.state

조회 후 서킷 브레이커를 확인하는 커맨드 exec -T 를 사용하면 컨테이너 내부에 직접 커맨드 할 수 있다.
데이터를 여러 번 조회하고 서킷이 닫혀있는지 체크한다. (조회시 조회용 토큰을 새로 발급받아야 한다!)
```

### open, half-open 테스트(실패, 반열림)
```
1. 실패 요청

https://locahost:8443/product-composite/1?delay=3 

서비스 API 를 세 번 호출하면서 응답을 3 초간 지연시키면 시간 초과가 발생한다.
(일부러 시간 초과를 발생시키기 위해 3 번 호출 및 3 초의 딜레이를 가진다.)

시간 초과로 인해 서킷은 open 상태가 된다.
```
```
2. 빠른 실패 및 폴백 메서드 작동 체크

https://locahost:8443/product-composite/1?delay=3 

앞서 서킷을 open 으로 만들었고 반열림 전환 대기 시간 10 초 전에 네 번째 시간 초과 호출을 보내서 빠른 실패 및
폴백 메서드가 작동하는지 확인한다.

폴백 메서드 호출 후 10 초간 기다린 후 반열림 상태가 되었는지 확인한다.

docker-compose exec -T product-composite curl -s http://product-composite:8080/actuator/health | jq -r
.components.circuitBreakers.details.product.details.state
```
### 서킷 브레이커 다시 닫기
```
서킷 브레이커가 반열림 상태에 있을 때 서킷을 다시 열지, 서킷을 닫아서 정상 상태로 되돌리지 판단하고자
세 번의 재시도 호출을 기다린다. (재시도 구성 설정)

https://localhost:8443/product-composite/1
정상 조회 요청을 3 번 보내서 서킷 브레이커를 닫아준다.

docker-compose exec -T product-composite curl -s http://product-composite:8080/actuator/health | jq -r
.components.circuitBreakers.details.product.details.state
서킷 상태 확인
```
### 무작위 오류로 재시도 메커니즘 테스트


