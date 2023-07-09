# Springdoc-openapi 를 사용해서 API 문서화 하기 

## Springdoc-openapi 소개
```
Springdoc-openapi 를 사용하면 API 를 구현하는 소스 코드와 연동해 API 를 문서화 할 수 있다. 연동해서 관리할 수 있다는 점이 중요하다. 
자바 코드와 API 문서의 수명주기는 어긋날 수 있기 때문에 서로 같은 주기로 운영해야 유지보수가 용이해진다.
```
### Springdoc-openapi 의존성 추가
```
implementation 'org.springdoc:springdoc-openapi-starter-common:2.0.2'
복합 서비스와 api 모듈에 위 의존성을 추가한다.

http://로컬주소/swagger-ui/index.html ui 접속 url
```
### Springdoc 운영은 어떻게 하는 것이 좋을까? 
```
복합 마이크로 서비스의 API 프로토콜을 통해서 핵심 마이크로 서비스를 호출하고 필요한 응답을 조립하기때문에 복합 마이크로서비스에서
openapi 를 운영해야 높은 응집성의 API 문서를 만들 수 있다.
```
### 공조 마이크로서비스 구조 분석
```
api  모듈
util 모듈
product-composite-service 복합 마이크로서비스 모듈

1. api 모듈 

api 모듈은 프로토콜 주소값과 dto 모델을 구현한다. 복합 마이크로서비스는 프로토콜을 설계할때 앞서 설명한 것처럼 
openapi 와 연계해서 API 프로토콜을 문서화 해서 응집성과 유지보수성을 높인다. ProductCompositeService 참고 

2. util 모듈

유틸리티 모듈은 공통 로직 처리를 담당한다. (ex 예외 처리, 서비스 주소값 가져오기) @RestControllerAdvice 를 사용해서 
@ExceptionHandler 로 예외를 처리하는 로직을 만들었다. util 참고

3. product-composite-service

복합 마이크로 서비스는 발신요청을 처리하는 통합 컴포넌트와, 복합 서비스 자체구현 두 가지 역할로 구현된다.
통합 컴포넌트는 핵심 마이크로서비스의 프로토콜을 통해서 데이터를 받는다.

컴포넌트를 통해 전송받은 데이터는 복합 서비스에서 dto 모델로 매핑하고 api 프로토콜 모듈을 통해서 값을 반환한다
```
### Springdoc-openapi 스웨거 운영
```
앞서 설명했듯이 openapi 는 api 요청을 처리하는 복합 서비스에서 구현한다. 부트 스트랩 클래스에 간단한 openapi 설정을
만들어준다.

ProductCompositeServiceApplication, application.yml 참고 
```
