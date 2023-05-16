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

implementation 'org.mapstruct:mapstruct:1.5.5.Final' 
- 기본 mapstruct

implementation 'org.mapstruct:mapstruct-processor:1.5.5.Final' 
testAnnotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
- 컴파일 타임에 애노테이션을 처리해 빈 매핑을 구현하기위한 의존성

compileOnly 'org.mapstruct:mapstruct-processor:1.5.5.Final'
- 인텔리제이 같은 IDE 에서 컴파일 타임을 생성할 때 필요한 의존성 


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
### Product 영속성 계층 구현하기 (몽고 DB)
```
product-service ProductEntity 참고
```
### Recommendation 영속성 계층 구현하기 (몽고 DB)
```
recommendation-service RecommendationEntity 참고 
```
### Review 영속성 계층 구현하기 (MySQL)
```
review-service ReviewEntity 참고 
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


```













