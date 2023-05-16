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


2. 스프링 데이터 MongoDB 의존성 추가

implementation 'org.springframework.data:spring-data-mongodb:3.4.10'
- 기본 몽고 DB 의존성

testImplementation 'de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.4.10'
- 테스트에서 내장형 몽고 DB 를 사용하기 위한 의존성 


3. JPA, MySQL 의존성 추가하기

implementation 'org.springframework.boot:spring-boot-starter-data-jpa:3.0.6'
implementation 'com.mysql:mysql-connector-j:8.0.31'

testImplementation 'com.h2database:h2'
- 테스트에서 내장형 H2 DB 를 사용하기 위한 의존성 
```
### Product 영속성 계층 구현하기 (몽고 DB)
```
product-service ProductEntity 참고
```
### Recommendation 영속성 계층 구현하기 (몽고 DB)
```
recommendation-service RecommendationEntity 참고 
```
### Review 영속성 계층 구현하기 
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













