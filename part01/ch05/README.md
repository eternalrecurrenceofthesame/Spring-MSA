# OpenAPI / 스웨거를 사용한 API 문서화

## 스프링 폭스 소개
```
스프링 폭스를 사용하면 API 를 구현하는 소스 코드와 연동해 API 를 문서화 할 수 있다. 연동해서 관리할 수 있다는 점이 중요하다. 
자바 코드와 API 문서의 수명주기는 어긋날 수 있기 때문에 서로 같은 주기로 운영해야 유지보수가 용이해진다.
```
### 복합 마이크로서비스에 스프링 폭스 의존성 추가
```
* ProductCompositeService 에 스프링 폭스 의존성 추가하기

implementation('io.springfox:springfox-boot-starter:3.0.0')

http://localhost:7000/swagger-ui/index.html 스웨거 ui 접속 url
```
### 스프링 폭스 운영은 어떻게 하는 것이 좋을까? 
```
스프링 폭스 의존성은 복합 마이크로 서비스에 추가한다. 복합 마이크로 서비스의 API 를 통해서 핵심 마이크로 서비스를 호출하고 
필요한 응답을 조립하기때문에 복합 마이크로서비스에서 스웨거를 운영하면 높은 응집성의 API 문서를 만들 수 있다.
```
```
* 복합 마이크로서비스 구조 분석

api  모듈
util 모듈
product-composite-service 복합 마이크로서비스 모듈

복합 마이크로서비스는 3 개의 모듈로 운영된다. 

1. api 모듈 

api 스펙 모듈은 프로토콜 주소값과 dto 모델을 구현한다. 복합 마이크로서비스는 프로토콜을 설계할때 앞서 설명한 것 처럼 
스프링 폭스와 연계해서 API 프로토콜을 문서화 해서 응집성과 유지보수성을 높인다. ProductCompositeService 참고 

2. util 모듈

유틸리티 모듈은 공통 로직 처리를 담당한다. (ex 예외 처리) @RestControllerAdvice 를 사용해서 @ExceptionHandler 로 
예외를 처리하는 로직을 만들었다. util 참고

3. product-composite-service

복합 마이크로 서비스는 발신요청을 처리하는 통합 컴포넌트와, 복합 서비스 자체구현 두 가지 역할로 구현된다.
통합 컴포넌트는 핵심 마이크로서비스의 프로토콜을 통해서 데이터를 받는다.

컴포넌트를 통해 전송받은 데이터는 복합 서비스에서 dto 모델로 매핑하고 api 프로토콜 모듈을 통해서 값을 반환한다
```
### 스프링 폭스 스웨거 운영
```
스웨거는 복합 마이크로서비스의 부트스트랩 클래스부터 만든다. 스웨거 문서에 매핑할 Docket 빈을 만들고 yml 구성 설정 
값을 참조하게 설계할 수 있다.  

api 모듈은 프로토콜 정보와 dto 모델 정보를 제공한다. 스웨거를 통해서 직접 api 프로토콜을 호출할 수도 있다.

yml, ProductCompositeServiceApplication, ProductCompositeService 참고
```





















