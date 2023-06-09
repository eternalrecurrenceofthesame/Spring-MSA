# 영속성 추가

## 핵심 마이크로서비스에 영속성 계층 추가하기
```
Product - MongoDB
Recommendation - MongoDB
Review -MySQL

Tip
MapStruct 를 사용하면 스프링 데이터 엔티티 객체와 API 모델 클래스를 쉽게 상호 변환할 수 있다. 
```
```
* 의존성 추가하기

1. MapStruct 을 사용하기 위한 의존성 추가

When using a modern version of Gradle (>= 4.6), you add something along the following lines to your build.gradle:

 implementation 'org.mapstruct:mapstruct:1.5.5.Final'
 annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'

2. 스프링 데이터 MongoDB 의존성 추가 및 내장형 몽고 DB 설정

implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'de.flapdoodle.embed:de.flapdoodle.embed.mongo'

spring:
  mongodb:
    embedded:
      version: 3.4.12 // 자신의 컴퓨터 환경에 맞는 버전을 선택하면 된다.

버전 목록 https://spring.io/projects/spring-data-mongodb#learn

3. JPA, MySQL 의존성 추가하기

implementation 'org.springframework.boot:spring-boot-starter-data-jpa:3.0.6'
implementation 'com.mysql:mysql-connector-j:8.0.31'

testImplementation 'com.h2database:h2'
- 테스트에서 내장형 H2 DB 를 사용하기 위한 의존성 
```
#### + 의존성을 추가할 때 주의할 점
```
교재로 실습을 진행하다보면 오래된 내용일 경우 맞지 않는 경우가 굉장히 많다. 의존성을 추가하는 것 또한 오래된 내용일 경우 
교재의 방법대로 진행이 되지 않을 수가 있다. 

이런 경우 Spring starter io 에서 모듈을 조립할 때 선택하는 의존성을 가져다 쓰면 된다. 이때 선택되는 의존성은 스프링 부트 버전에
맞는 의존성을 추가해주기 때문에 자신의 스프링 부트에 맞는 올바른 의존성을 가져다 쓸 수 있다. 

 * 몽고 디비의 경우 교재의 의존성을 추가하는 방법이 되지 않아서 이 내용을 추가함  * 
```
## 핵심마이크로서비스 영속성 계층 구현하기 
```
product, recommendation, review 의 엔티티 모델을 구현한다. persistence entity 참고 
```
## 스프링 데이터 리포지토리 정의하기 
```
앞서 만든 엔티티를 저장할 스프링 데이터 리포지토리를 구현한다! persistence Repository 참고 
```
## 영속성에 중점을 둔 자동 테스트 작성하기
```
웹 애플리케이션 런타임에 필요한 웹 서버 등의 다른 자원이 시작되길 기다리지 않고 내장형 데이터베이스를 
사용해서 영속성 계층만 테스트하려면 아래 두 가지 애노테이션을 사용하면 된다.

@DataMongoTest
@DataJpaTest   
```
```
* Product 마이크로 서비스 영속성 테스트 하기 

 product test PersistenceTests 참고
```

## 서비스 계층에서 영속성 계층 사용하기
```
서비스 계층 (프로토콜 구현 로직 계층 - ProductServiceImpl) 에서 영속성 계층으로 데이터를 저장하고 데이터베이스의 데이터를 검색하는
단계별 구현 과정 (product-service 를 기준으로 설명한다.)
```
### 1. 데이터베이스 연결 URL 기록하기
```
자체 데이터베이스와 연결된 마이크로서비스를 확장하는 경우 각 마이크로서비스가 실제로 사용하는 데이터베이스가 무엇인지 파악하기 힘든
문제가 있다. 

따라서 마이크로서비스가 시작된 직후 접속한 데이터베이스의 URL 을 기록하는 로그를 추가하면 어떤 데이터베이스를 사용하고 있는지 
알 수 있다.

ProductServiceApplication 참고 
```
### 2. 새 API 추가하기
```
데이터를 저장하고 삭제하는 API 를 api 공통 모듈에 추가한다. api - core 참고 
```
### 3. 영속성 계층 사용하기 
```
핵심 마이크로서비스의 서비스 구현체에서 영속성 계층을 사용하는 코드를 작성한다. serviceImpl, Mapper 클래스 참고 
```
#### 핵심 마이크로서비스 api(ProductServiceImpl) 테스트하기
```
* 생성 및 삭제 API 오퍼레이션에 대한 테스트

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
                properties = {"spring.data.mongodb.port: 0", "spring.data.mongodb.auto-index-creation: true"})

스프링부트 통합 테스트시 포트와 yml 설정 정보를 주입하는 방법. mongodb.auto-index-creation 옵션을 설정해야 @Indexted(unique = true)
를 사용할 수 있다. https://stackoverflow.com/questions/62816322/spring-boot-mongo-db-index-unique-true-not-working 참고 

product - ProductServiceApplicationTests, MapperTests 참고 // 리뷰와 추천 테스트도 비슷하기 때문에 일단 상품 테스트만 만든다. 
```
### 4. 복합 서비스 API 확장하기
```
이번 단원에서 만든 복합 서비스 api 와 컴포넌트의 구현은 핵심 마이크로서비스에서 오류가 발생하면 데이터의 정합성이 깨질 수 있는 문제가 있다.
모듈별 트랜잭션 전파가 되지 않기 때문에 데이터가 일부만 삭제되거나 저장될 수 있는 문제는 ch 7 에서 해결한다. 

ProductCompositeServiceImpl(api 구현), ProductCompositeIntegration(통합 컴포넌트) 참고 
```
#### 복합 서비스 테스트 업데이트 
```
ProductCompositeServiceApplicationTests 참고 
```
#### 데이터베이스 이미지 파일 도커 컴포즈에 추가하기
```
  mongodb:
    image: mongo:6.0.4
    mem_limit: 512m
    ports:
      - "27017:27017"
    command: mongod
    healthcheck:
      test: "mongostat -n 1"
      interval: 5s
      timeout: 2s
      retries: 60

  mysql:
    image: mysql:8.0.33
    mem_limit: 512m
    ports:
      - "3306:3306" # 포트 중복 조심
    environment:
      - MYSQL_ROOT_PASSWORD=rootpwd
      - MYSQL_DATABASE=review-db
      - MYSQL_USER=user
      - MYSQL_PASSWORD=pwd
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "--silent"]
      interval: 5s
      timeout: 2s
      retries: 100 # mysql 헬스 테스트에 실패하는 경우 60 -> 100 으로 재시도 값을 늘려서 실행시킨다.
```

데이터베이스 도커 이미지파일을 생성하고 도커를 실행할 때 헬스체크에 실패하는 경우가 있는데 이 경우 재시도 횟수를 늘리는 것이 해결 방법이 될 수 있다.

### 5. 새 API 및 영속성 계층의 수동 테스트 진행
```
./gradlew build 244 p 참고 도커 실습 후 업데이트 예정. 
```
