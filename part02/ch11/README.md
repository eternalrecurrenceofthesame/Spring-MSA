# API 접근 보안

에지 서버를 통해 공개되는 복합 마이크로서비스, 유레카 API 및 웹 페이지(유레카 서버)에 대한 접근을 보호하기 

## OAuth 2.0 및 OpenID Connect 소개
```
* 용어 정리

인증: 사용자 이름과 암호 같은 사용자가 제공하는 자격 증명을 확인해 사용자를 식별하는 것
권한 부여: 식별된 사용자에게 API 에 대한 접근 권한을 부여 (ex OAuth 2.0 스코프)
OAuth 2.0: 권한 부여를 위한 공개 표준

OpenID Connect: 클라이언트 애플리케이션이 권한 부여 서버에서 받은 자격 증명을 기반으로 사용자의 신원을 확인할 수
있도록 OAuth 2.0 에 추가된 기능
```
### OAuth 2.0 소개
```
OAuth 2.0 는 서드파티 클라이언트 애플리케이션(웹 클라이언트) 가 사용자를 대신해 보안 리소스에 접근할 수 있게 해준다. 
```
```
* 개념 정리

자원 소유자 resource owner: 최종 사용자. (유저)
클라이언트 client: 최종 사용자의 권한을 위임 받아 보안 API 를 호출하는 서브파티 애플리케이션(웹 앱, 네이티브  모바일 앱)
자원 서버 resource server: 보호 대상 자원에 대한 API 를 제공하는 서버 (게이트 웨이 -> 유레카, 복합 마이크로서비스)

권한 부여 서버 authorization server: 자원 소유자를 인증하고 자원 소유자의 승인을 바다서 *클라이언트* 에게 토큰을 발급한다.
사용자 정보 관리 및 사용자 인증은 보통 ID 제공자에게 위임된다.
```
```
* 권한 부여 서버를 사용해서 권한을 부여 받는 전체적인 흐름

사용자 -> 클라이언트(웹 앱) -> API 접근 요청 클라이언트는 리소스에 접근하기 위해 권한 부여 서버에서 권한을 부여받아야 한다.
사용자의 자격 증명을 클라이언트 와 공유하지 않기 위해 권한 부여 서버에서는 클라이언트에 토큰을 발급한다. 

클라이언트는 이 토큰으로 승인된 API 에 접근할 수 있다. 토큰 시간에는 제한이 있고 OAuth 2.0 스코프를 사용해서 접근 권한을 제한한다. 
권한 부여 서버는 클라이언트 애플리케이션에 재발급 토큰을 발급할 수 있다. 

https://github.com/eternalrecurrenceofthesame/Spring-security-in-Action/tree/main/ch12
토큰을 부여받는 각 그랜트 유형은 위 링크를 참고한다.

https://datatracker.ietf.org/doc/html/rfc6749
https://www.oauth.com/oauth2-servers/map-oauth-2-0-specs/ 
OAuth 2 전체 사양 정보
```
### OpenID Connect 소개
```
OIDC 는 클라이언트 애플리케이션이 사용자의 신원을 확인하게 하려고 OAuth 2.0 에 추가된 기능이다. 

OIDC 를 사용하면 승인 흐름이 완료된 후 클라이언트 애플리케이션이 권한 부여 서버에서 받아오는 토큰인 ID 토큰(JWT 인코딩) 이 
추가되고 사용자 ID 이메일 주소와 같은 다수의 클레임을 포함한다. 
(ID token 과 같은 방식으로 접근 토큰을 인코딩하고 서명할 수 있다 필수는 아님.)


OIDC 는 디스커버리 엔드포인트를 정의해서 주요 엔드포인트에 대한 정보를 제공한다. 주요 엔드포인트로

JWKS 엔드포인트 (JWK 란 서명된 JWT 토큰을 확인할 때 필요한 공개키를 의미한다.)
권한 부여 엔드포인트
사용자 정보 엔드포인트 가 있다 387 p

more info ODIC - openid.net/developers/specs
```
## 시스템 환경 보안 구현하기
```
HTTPS 를 사용해서 공개된 API 에 대한 외부 요청과 응답을 암호화 하여 도청을 방지한다.
OAuth 2.0 + OIDC 를 사용해서 API 에 접근하는 클라이언트에 인증 및 권한 부여를 수행한다.
HTTP 기본 인증을 사용해 검색 서비스(유레카) 에 대한 접근을 보호한다.

외부 통신은 https 로 보호하고 시스템 환경에서는 http 를 사용해서 통신한다.
```
### 권한 부여 서버 추가하기

권한 부여 서버는 아파치 라이선스 2.0 리소스를 사용해서 구현한다. 

아파치 라이선스 2.0 에 대해서는 이 글을 참고. https://namu.wiki/w/%EC%95%84%ED%8C%8C%EC%B9%98%20%EB%9D%BC%EC%9D%B4%EC%84%A0%EC%8A%A4

권한 부여 서버를 만드는 상세 정보는 이 글을 참고한다. 

https://github.com/eternalrecurrenceofthesame/Spring-security-in-Action/tree/main/ch13

```
OAuth 2.0 및 OIDC 기반의 보안 API 로 로컬 테스트 및 완전히 자동화된 테스트를 실행하고자 OAuth 2.0 기반의 권한 부여 서버를 
직접 구현해본다.

sprin-cloud authorization-server  참고

config: AuthServerConfig(Auth 2.0 설정 정보) , DefaultSecurityConfig(시큐리티 기본 설정)
jose: keygenerateutils 클래스를 사용해서 (jwk 서명 키)를 생성한다. 
```
## HTTPS 를 사용한 외부 통신 보호
```
* HTTPS 를 사용해서 통신을 암호화 하기 위한 준비 392p

HTTPS 인증서 생성: 개발 목적의 자체 서명 인증서 생성
에지서버 구성: 인증서를 사용해 HTTPS 기반 외부 트래픽만 에지 서버에 접근을 허용하도록 구성 

생성된 자체 서명 인증서 파일은 gateway resources keystore 참고한다
프로젝트 빌드시 .jar 파일에 포함시킬 수 있고 런타임시 keystore/dege.p12 클래스패스로 접근할 수 있다.

+ keytool 을 사용해서 자체 서명 인증서 생성하는 방법
https://velog.io/@jummi10/keytool로-local에서-SSL-인증서-생성-및-적용 참고 
```
```
* 인증서를 사용하는 에지서버 설정

개발 환경에서는 HTTPS 클래스패스로 인증서를 제공해도 되지만, 상용 환경 등의 다른 환경에 적용해서는 안 된다.
런타임에 클래스패스 인증서를 외부 인증서로 교체해서 사용해야 한다. 

gateway yml, docker-compose 참고
```
### 런타임에 자체 서명 인증서 교체하기
```
앞서 설명했듯 개발 환경에서는 HTTPS 인증서를 .jar 파일 안에 두는 것이 유용하지만 테스트나 상용 환경과 같은 런타임
환경에서는 공인된 인증기관 CA 에서 서명한 인증서를 사용해야 한다.

또한 도커 환경에서 .jar 파일이 포함된 도커 이미지를 사용할 때는 .jar 파일을 다시 빌드할 필요 없이 인증서를 지정해서
사용할 수도 있어야 한다. (빌드해서 이미지를 만들지 않고 인증서를 지정해서 사용한다는 의미)
```
```
1. 새 인증서 생성 

cd msa-spring-reactivce
mkdir keystore // 오타 조심

keytool -genkeypair -alias localhost -keyalg RSA -keysize 2048 -storetype PKC12 - keystore
keystore/edge-test.p12 -validity 3650
```
```
2. 도커 컴포즈 파일에 새 인증서의 위치 및 암호를 지정하는 변수 추가

gateway:
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - SERVER_SSL_KEY_STORE=file:/keystore/dege-test.p12
    - SERVER_SSL_KEY_SOTRE_PASSWORD=testtest
  volumes:
    - $PWD/keystore:/keystore
  build: spring-cloud/gateway
  mem_limit: 512m
  ports:
    - "8443:8443"

새 인증서가 있는 폴더와 매핑된 볼륨을 추가
(이 부분은 도커에 대해 잘 모르기 때문에 자세한 설명은 생략 추가 예정)     
```
```
3. 에지 서버(게이트 웨이) 가 동작 중이라면 다음 커맨드로 다시 시작

docker-compose up -d --scale gateway=0 // 인스턴스를 0 개로 초기화
docker-compose up -d --scale gateway=1 // 인스턴스 1 개 추가 345 p 

docker-compose restart gateway 커맨드로 게이트웨이를 재시작 할 경우
docker-compose.yml 에 적용한 위 변경 사항이 반영되지 않는다 (주의)
```
런타임 서명 인증서 교체 부분은 실습은 생략한다. 

## 검색 서비스 접근 보안 
```
스프링 시큐리티 설정으로 유레카 검색 서버 API 및 웹 페이지에 대한 접근을 제한할 수 있다.

유레카 서버에 시큐리티 인증 정보를 추가하고 마이크로서비스가 유레카 서버에 등록될 때 인증된 서비스라는 것을 검증하기 위해
등록시 인증 정보를 포함한 요청을 보내도록 설계한다.

마이크로서비스 모듈이 유레카에 등록되는 주소 값을 설정하면서 아이디와 패스워드를 넘겨주고 인증할 수 있도록 마이크로서비스 
구성 정보를 아래와 같이 수정했다.
"http://${app.eureka-username}:${app.eureka-password}@${app.eureka-server}:8761/eureka/"

유레카 서버에 등록되려면 마이크로서비스 모듈에서 유레카 시큐리티 서버의 아이디와 비밀번호를 알아야 한다. 


유레카 서버에 등록된 인스턴스 목록을 게이트웨이를 통해서 조회하는 예시 
curl -H "accept:application/json" https://u:p@localhost:8443/eureka/api/apps -ks | jq
-r .applications.application[].instance[].instanceId  

eureka-server yml, securityconfig 및 msa 모듈 yml 참고 
```
## OAuth 2.0 과 OpenID Connect 를 사용한 API 접근 인증 및 권한 부여 

권한 부여 서버를 설계했고 HTTPS 를 사용해서 게이트웨이의 API 요청 및 응답을 암호화 했다. 그리고 유레카 서버에 스프링 시큐리티

HTTP 기본 인증을 사용해서 사용자 이름과 암호가 있어야 접근할 수 있도록 검색 서버 API 및 웹 페이지에 대한 접근을 제한했다. 

권한 부여서버를 바탕으로 게이트웨이 에지 서버와 product-composite 서비스를 OAuth 2.0 리소스 서버로 리팩토링 한다. 

```
에지 서버는 권한 부여 서버에서 발급한 서명(JWK) 을 사용해 접근 토큰의 유효성을 검사한다. product-composite 서비스에 접근하려면
OAuth 2.0 스코프가 추가된 접근 토큰이 필요하다.

product:read 스코프 (읽기)
product:write 스코프 (생성 삭제) 
```
### 게이트웨이와 product-composite 리팩토링
```
* 게이트웨이 시큐리티 구현

gateway securityconfig, yml 참고 
```
```
* product-composite 시큐리티 구현 

- api 를 호출할 때마다 관련된 JWT 접근 토큰을 로그로 기록하는 메서드를 구현한다.
- 인증 서버에서 jwk 서명키를 받기 위한 액세스 토큰 발행자를 yml 으로 설정한다. 

product-composite ProductCompositeServiceImpl, SecurityConfig, yml 참고 
```
```
스프링 시큐리티를 구현한 모듈에서 스프링 기반 통합 테스트를 실행할 때는 csrf 와 엔드포인트 접근시 인증 요구를 
비활성화 해야한다. 

TestSecurityConfig 를 구현해서 이런식으로 추가하면 구현한 자바 설정을 사용할 수 있다. 

@SpringBootTest(
  webEnvironment = RANDOM_PORT,
  classes = {TestSecurityConfig.class},
  properties = {
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=",
    "spring.main.allow-bean-definition-overriding=true",
    "eureka.client.enabled=false"})
```

HTTPS 요청으로 애플리케이션에 접근한다. 복합 마이크로서비스에 접근하려면 인증 서버로부터 액세스 토큰을 받아서 접근할 수 있도록 

인증서버와 리소스 서버(복합마이크로서비스) 를 구현했다. 그리고 유레카 서버 보안을 위해 유레카에 등록하려는 서비스는 유레카 서버의 

인증 정보를 가지고 있어야 등록될 수 있게 각 마이크로서비스의 yml 구성 설정을 변경했다. 

#### + 게이트웨이 의존성 추가
```
스프링 클라우드를 사용하는 게이트웨이 모듈을 spring starter 로 생성하면 필요한 의존성이 없어서 오류가
발생하는 경우가 있다

No spring.config.import property has been defined 오류 발생시 아래 의존성을 추가한다.
implementation('org.springframework.cloud:spring-cloud-starter-bootstrap')

https://stackoverflow.com/questions/67507452/no-spring-config-import-property-has-been-defined 참고
```
## 로컬 권한 부여서버를 사용한 테스트 (직접 구현한 권한부여 서버를 의미함) 
```
API 접근 보안을 위한 모든 작업이 끝났다. 간단하게 구현 결과물을 설명하고 넘어간다.

HTTPS 를 사용해서 공개된 API(게이트웨이) 에 대한 외부 요청과 응답을 암호화하여 도청을 방지한다.
(게이트웨이 안쪽 즉 마이크로서비스 내에서는 http 통신을 사용)

유레카서버에서 시큐리티 의존성을 가지고 인증 논리를 구현했다.

마이크로서비스를 유레카 서버에 등록할 때 인증 값을 사용하게 해서 인증되지 않은 서비스가 등록되는 것을
차단했다.

권한부여 서버를 추가해서 사용자 인증 논리를 구현한다. 권한 부여 서버와 외부의 통신(인증, 인가 과정) 은
에지서버(게이트 웨이) 를 통해서 라우팅 되게끔 설계했다.

리소스를 직접 호출하는 게이트웨이와 (게이트웨이는 복합 서비스와 유레카 서버를 호출할 수 있다.)

복합 서비스는 스프링 시큐리티를 사용해서 OAuth2 인증 방식의 구현 모듈 중 하나인 리소스 서버로 만들고
리소스를 호출하기 전 권한이 있는지 권한 부여 서버에서 검증하도록 설계했다.

OAuth 2  프레임워크를 스프링 시큐리티로 구현하는 것에 대해서는 아래 링크를 참고한다.
https://github.com/eternalrecurrenceofthesame/Spring-security-in-action/tree/main/part4/OAuth2-spring-security

./gradlew build && docker-compse build 전체 애플리케이션을 새로 빌드하고 이미지 파일을 생성한다.
```

테스트는 포스트맨을 사용한다.

```
* CLIENT_CREDENTIALS(클라이언트 자격 증명 유형)

https://localhost:8443/oauth2/token : 액세스 토큰 요청 url
reader : 클라이언트 아이디
secret-reader : 클라이언트 시크릿
product:read : 스코프

클라이언트 아이디와 시크릿 스코프 및 클라이언트 자격증명유형을 포함한 postman 요청으로
액세스 토큰을 얻을 수 있다.

https://localhost:8443/product-composite/1
액세스 토큰으로 리소스 엔드포인트를 호출 한다.

참고로 권한부여 서버를 구현할 때 읽기와 저장 클라이언트를 각각 따로 만들었다. 
```
```
* AUTHORIZATION_CODE (승인 코드 그랜트 유형) 

https://my.redirect.uri 
https://localhost:8443/oauth2/authorize : 권한 부여 서버 엔드포인트
https://localhost:8443/oauth2/token
reader
secret-reader
product:read

username, password

https://localhost:8443/product-composite/1

승인 코드 그랜트 유형에서는 리다이렉트 주소와 권한부여 서버의 엔드포인트 그리고 인가 정보가 추가로 필요하다.
마찬가지로 포스트맨을 사용하면 리소스를 쉽게 호출 할 수 있다.


승인 코드 그랜트 유형에 대한 추가 설명이 필요하면 아래 내용을 참고한다.
https://github.com/eternalrecurrenceofthesame/Spring-security-in-action/tree/main/part4/ch12
https://github.com/eternalrecurrenceofthesame/Spring-security-in-action/tree/main/part4/OAuth2-spring-security/OAuth2-authorization
```

클라이언트 자격 증명 유형 및 승인 코드 그랜트 유형으로 시큐리티가 적용된 API 호출 테스트를 마무리한다.

참고로 스프링 시큐리티에서 패스워드 그랜트 타입은 depreacted 되었고 암시적 그랜트 유형은 지원하지 않는다.

OAuth 0 추가 예정.

