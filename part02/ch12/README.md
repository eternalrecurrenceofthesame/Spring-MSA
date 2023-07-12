# 구성 중앙화

마이크로서비스에서 사용하는 구성 설정을 중앙화 해서 응집성을 높일 수 있다.

## 스프링 클라우드 컨피그 서버로 구성 중앙화 하기

컨피그 서버는 다른 마이크로서비스와 마찬가지로 에지 서버의 뒤에 위치하게 된다. 432 그림 참고 

```
* 구성 중앙화 유형

- 깃 저장소
- 로컬 파일 시스템 (이번 단원에서 사용)
- 하시코프 볼트
- JDBC 데이터베이스
```
```
* 구성 서버 의존성

implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.cloud:spring-cloud-config-server'
```
### 구성 중앙화 메커니즘 선택하기
```
클라이언트가 먼저 구성을 결정하고 유레카 서버에 등록할 수 있다 이 방식은 유레카의 구성 정보를 구성 서버에 저장할 수 있다.
다른 방식으로 클라이언트를 유레카에 먼저 등록하고 구성 정보를 가져오게 하는 방법이 있다.

다른 방식에 대한 내용은 spring cloud docs 을 참고 (이번 단원에서는 첫번째 방법을 사용한다.)

구성을 먼저 가져오면 구성 서버가 단일 장애점(SPOF, 구성을 먼저 가져올 때 오류가 발생하면 다음 작업으로 넘어가지 못하는 문제)
이 될 수 있다. 이 문제는 쿠버네티스를 사용해서 해결한다. 433p (쿠버네티스는 part 3 에서 설명)
```
### 구성 서버 보안 및 API
```
구성 서버는 게이트웨이 뒤에 위치하며 외부로 노출되지 않는다. 기본적으로 게이트웨이 접근 요청은 https 를
사용해서 도청을 방지하며 마이크로 서비스 내에서는 시큐리티 http 기본 인증으로 클라이언트를 식별할 수 있다.

구성서버 또한 스프링 시큐리티 기본 인증을 사용하며 환경 변수에서 값을 설정한다 env, docker-compose 참고
구성 저장은 로컬 파일 시스템을 사용한다 (/config-repo)

구성 저장소는 접근 권한이 있는 사람이 암호 등의 민감한 정보를 훔쳐가는 상황을 피하고자 구성 서버에서 정보를
암호화해서 디스크 저장한다.

구성 서버는 대칭키와 비대칭키를 지원하는데 여기서는 대칭키를 사용한다. (비대칭 키는 좀 더 복잡하다.)

비대칭 키에 대한 내용은 아래를 참고
cloud.spring.io/spring-cloud-static/spring-cloud-config... key_management 
```
```
* 클라이언트가 검색할 수 있는 구성 서버 REST API

/actuator: 액추에이터는 필요한 정보만 노출해야한다.
/encrypt 및 /decrypt: 중요한 정보를 암호화하고 해독하기 위한 엔드포인트 상용 환경에서는 잠금한다.
/{microservice}/{profile}: 지정한 마이크로서비스의 스프링 프로필 구성 반환
```
## 구성 서버 구현하기

### config-server
```
스프링 클라우드 패키지에 구성 서버 모듈을 추가한다. 이 모듈은 간단한 시큐리티 규칙 및 구성 저장소의
위치 값을 가지고 있다.
```
### docker-compse 에 구성 서버 모듈 이미지 추가
```
* .env 

도커 컴포즈에서 사용할 환경 변수를 추가한다.

! 사용자의 이름, 암호, 암호화 키와 같은 .env 파일에 저장된 민감 정보는 개발 및 테스트 이외의 용도로
사용할 때는 안전하게 보호해야 한다. !  
```
```
환경 변수에 있는 시큐리티 인증 값을 사용해서 구성 서버에 접근할 수 있도록 이미지 파일을 설계한다.
구성서버 환경 변수 값을 추가한다.
```
### 구성 설정 중앙화 하기
```
지금까지 구현한 구성 정보를 로컬 저장소인 config-repo 파일로 옮긴다. 공통적으로 사용되는 정보는
application.yml 파일로 만들고 각 서비스별 구성 정보를 구현한다. 

구성 정보를 추출한 마이크로서비스에는 구성 서버를 통해서 구성 저장소의 정보를 가져올 수 있도록
아래 의존성을 추가하고 구성서버 연결 설정 yml 파일을 구현한다. 

// 클라우드 컨피그 의존성
implementation 'org.springframework.cloud:spring-cloud-starter-config'
implementation 'org.springframework.retry:spring-retry'

// 클라우드 오류 발생 해결 추가
implementation('org.springframework.cloud:spring-cloud-starter-bootstrap')

config-rep, 각 마이크로 서비스의 application.yml 참고
```
### 스프링 부트 기반의 자동 테스트 수정하기
```
@DataMongoTest, @DataJpaTest, @SpringBootTest 애노테이션이 있는 테스트에서 구성 서버를 사용하지 않도록
spring.cloud.config.enabled=false 속성을 추가해서 비활성화 한다.

ProductCompositeServiceApplicationTests, PersistenceTests 참고
```
## 클라우드 컨피그 서버 사용해보기

