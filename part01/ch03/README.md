# 공조 마이크로서비스 집합 생성하기 

복합 마이크로서비스인 Product Composite 와 핵심 마이크로서비스인 Product, Review, Recommendation 으로 구성되는 공조 마이크로서비스를 구현한다. 

### GradleWrapper 를 사용해서 공조 마이크로 서비스를 생성하고 골격 마이크로 서비스를 관리하기
```
- 그레이들이란? Groovy를 이용한 빌드 자동화 시스템으로써 라이브러리를 추가하고 버전을 관리할 수 있게 도와주는 역할을 한다.
https://codecrafting.tistory.com/1 참고

- 그레이들 래퍼란? 새로운 환경에서 프로젝트를 설정할 때 JAVA 나 grdale 를 설치하지 않고 바로 빌드할 수 있게 도와주는 역할을 한다.
그레이들 래퍼를 사용해서 골격 마이크로서비스를 관리하는 프로젝트인 공조 마이크로 서비스를 생성한다.

공조 마이크로서비스 내에서 골격마이크로서비스(실제 서비스를 제공하는 MSA 애플리케이션) 을 만들고 사용할 수 있다.
(이 블럭의 내용은 책의 내용을 토대로 유추한 것이 많기 때문에 정확하지 않음)
```
```
1. Gradle 설치 및 GradleWrapper 클래스 생성

https://gradle.org/releases/ 에서 그레이들을 설치할 수 있다. 
GradleWrapper 클래스를 생성해서 공조 마이크로 서비스로 사용하려면 폴더(msa-spring-cloud)를 만들고 gradle init 을 커맨드 한다.
```
```
2. 인텔리제이에서 골격 마이크로서비스 생성하기

spring starter io 에서 마이크로 서비스로 사용할 프로젝트를 생성하고 File -> New -> ModuleFromExistingSource.. 를 클릭해서
만들어둔 프로젝트를 추가하면 공조 마이크로 서비스의 골격을 추가할 수 있다.
```
```
3. 공조 마이크로 서비스를 사용해서 골격 마이크로 서비스 빌드하기 (멀티 프로젝트 빌드 설정)

공조 마이크로서비스를 만들면 각각의 마이크로 서비스를 한번에 빌드할 수 있다.
https://docs.gradle.org/8.1.1/userguide/multi_project_builds.html 참고 

settings.gradle 설정

rootProject.name = 'msa-spring-cloud' // 공조 마이크로 서비스
include 'microservices:product-composite-service' // 골격 마이크로 서비스들 
include 'microservices:product-service'
include 'microservices:recommendation-service'
include 'microservices:review-service'

./gradlew build 공조 마이크로 서비스에서 커멘드

DevOps 관점에서 보면 멀티 프로젝트를 이용한 일괄 빌드는 바람직하지 않다. 마이크로서비스 프로젝트별 별도의 빌드 파이프라인을
설정하는 것이 좋다. (예제 진행을 쉽게 하고자 빌드 커멘드를 하나로 모았음)
```
#### ++ 골격 마이크로 서비스및 API, 헬퍼 클래스를 추가할 때 주의할 점
```
골격 마이크로 서비스를 만들고 모듈로 등록할 때 공조 마이크로 서비스의 폴더에 모듈을 넣고 등록해야한다
그렇지 않으면 제대로 빌드 되지 않는 경우가 발생함. 

*다른 위치에서 골격 마이크로서비스 모듈을 등록하고 공조 마이크로 서비스 폴더의 위치로 옮겨서 사용하는 경우 빌드 시 오류가 발생한다.*
(인텔리제이 IDE 사용시)
```
여기서 설명한 부분들은 책의 내용과 다른 점이 많기 때문에 직접 찾아보고 자료를 정리한 것. 틀린 내용이 있을 수 있다.

## RESTful API 추가 

각 마이크로서비스에서 사용할 api 모델과 util 공통 모델을 추가한다. 

### 1. API 정의를 배치할 별도의 그레이들 프로젝트 만들기
```
마이크로서비스에서 사용할 API 스펙을 정의하고 공조 마이크로서비스에서 관리한다. 각각의 마이크로서비스는
공조 서비스가 제공하는 API 모델을 주입받아서 사용한다.

마이크로 서비스 API 를 정의한 모듈을 공조 마이크로 서비스 그룹에서 관리하는 것은 좋은 선택지가 될 수 있다. 
```
```
DevOps 관점에서 보면 모든 프로젝트는 각자의 빌드 파이프라인을 가지고 각자 API 와 util 프로젝트에 대한 버전 제어 의존성을
갖게 하는 것이 바람직하다. 115 p
```
### 2. 전체 마이크로 서비스가 공유하는 헬퍼 클래스를 배치할 util 프로젝트 만들기 
```
골격 마이크로 서비스가 사용할 공통 유틸리티 모듈을 만들어서 공조 마이크로 서비스에 포함시켰다. * util 참고
하지만 앞서 설명했듯이 각각의 마이크로 서비스에서 유틸 모듈을 제어하는 것이 바람직하다. 
```
## 핵심 마이크로 서비스의 API 구현하기 
```
* API, util 모듈 조립하기

공조 마이크로 서비스의 settings.gradle 에 공통 모듈을 추가한다
incldue ':api'
incldue ':util'

각각의 핵심 마이크로 서비스 build.gradle 에 공통 모듈을 추가한다 

dependencies{
  implementation project(':api')
  implementation project(':util') } 

각각의 핵심 마이크로 서비스 settings.gradle 에 include 를 추가한다.

include ':api'
include ':util'

인텔리제이 IDE 사용시 프로젝트의 의존성을 인식하지 못하는 경우가 있는데 이런 경우 

IntelliJ - File - Invalidate caches / Restart 메뉴를 실행후 재구동하면 인식한다. (이거 찾느라 엄청 고생함..)
(https://www.lesstif.com/spring/gradle-intellij-113345573.html 참고)
```
```
* 빌드하고 실행해보기

공조 마이크로 서비스에서 ./gradlew build 커맨드로 전체 마이크로 서비스를 빌드한다.
(빌드하면 build/libs 에 빌드 파일이 생긴다.)

java -jar microservices/product-service/build/libs/스냅샷 이름.jar & 커맨딩으로 프로덕트를 실행한다. 
(jar 파일 위치에서 커맨딩 해도 된다.)

curl http://localhost:7001/product/123 
프로덕트의 엔드포인트 서비스를 호출해서 서비스 결과를 확인한다. 
```
## API 설계하기

API 모듈은 공조 마이크로서비스에서 관리하는 각각의 마이크로서 서비스가 사용할 API 스펙을 정의하는 모듈이다.

이 모듈에는 API 모델 뿐만 아니라 마이크로서비스를 호출하는 엔드포인트 서비스 인터페이스를 제공한다. 

## 마이크로 서비스 구현하기

앞서 정의한 API 모듈을 사용해서 각각의 마이크로서비스를 구현한다.

### 복합 마이크로서비스 구현 
```
복합 마이크로서비스는 세 가지 핵심 마이크로서비스를 호출하는 역할을 한다. 핵심 마이크로서비스 발신 요청을
처리하는 통합 컴포넌트와 복합 서비스 자체 API 구현으로 나뉜다. 122p
```
```
* 통합 컴포넌트 구현하기 ProductCompositeIntegration 참고

restTempalte 을 사용해서 핵심 서비스로의 요청을 처리한다. 

getRecommendations() 메서드와 getReviews() 메서드는 제너릭 리스트 객체를 반환해야 하는데 제네릭은 런타임에
유형 정보를 갖지 못하기 때문에 메서드의 반환 값으로 제네릭 타입을 지정할 수 없지만

ParameterizedTypeReference(헬퍼 클래스) 를 사용하면 런타임시에도 제네릭 타입을 응답 값으로 사용할 수 있다. 128 p

restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {}).getBody();
exchange 메서드를 사용하면 요청 후 응답값을 받는다.
```
```
* 복합 API 서비스 구현하기 ProductCompositeServiceImpl

핵심 서비스에 적용한 것처럼 복합 마이크로 서비스를 호출하기 위한 RestController 를 구현한다
ProductCompositeServiceImpl 참고 

비즈니스 로직 계층 ProductCompositeServiceImpl 을 마이크로 서비스 구현에 추가하면 비즈니스 로직과
프로토콜별 코드 ProductCompositeService 가 분리돼 테스트와 재사용이 쉬워진다 130 p 
```
## 복합 마이크로 서비스를 통해서 핵심 마이크로 서비스를 호출해보기
```
앞서 만든 복합 마이크로 서비스(product-composite-service 참고) 는 상품에 대한 정보를 요약해서 보여주는 통합 컴포넌트와 
복합 서비스를 제공한다.
(product-composite-serivce 의 통합 컴포넌트 통해서 요청을 처리하고, 복합 서비스로 3 개의 컴포넌트 정보를 모아서 summary 
값을 제공하는 서비스 API 로직을 구현했다.)

쉽게 말해서 핵심 마이크로 서비스(상품, 상품 추천, 리뷰) 에서 필요한 값을 모아서 요약된 서비스를 제공한다. 
```
```
* 핵심 마이크로서비스 API 구현의 예외 처리

복합 마이크로 서비스의 통합 컴포넌트가 호출하는 핵심마이크로 서비스 API 에서 예외가 발생하는 경우 
상품 조회는 예외를 던지지만, 추천,리뷰 조회의 경우 간단하게 경고 로그만 남긴다.

ProductCompositeIntegration 컴포넌트 클래스 참고
```
## API 수동 테스트

지금까지 만든 공조 마이크로 서비스를 수동으로 조회하는 테스트를 수동으로 해보겠음! 

```
* 정상 요청

./gradlew build 전체 빌드 커멘드

java -jar /microservices/product-composite-service/build/libs/파일명.jar &
커맨딩으로 각각의 빌드된 핵심 마이크로 서비스와 복합 마이크로 서비스를 실행시킨다.

curl http://localhost:7000/product-composite/1 -s |jq . 
정상 요청을 호출한다. (커맨드라인 JSON 처리기 jq 를 사용하면 깔끔하게 호출할 수 있다. 포스트맨을 사용해도 됨)

stedolan.github.io/jq/download 
https://blog.naver.com/justdoplzz/222642933341 jq 설치 방법 참고 
```
```
* 골격 마이크로서비스 부트스트랩 클래스 실행에 관해서

공조 마이크로서비스를 설계하고 각각의 마이크로 서비스의 빌드와 공통 모듈을 공조 마이크로서비스에서 관리할 경우 각 마이크로서비스의
부트스트랩 클래스를 실행할 때 오류가 발생하는 경우가 있다.

이런 경우 인텔리제이 IDE 를 사용한다면 상단 탭의 : 버튼을 클릭해서 edit 을 클릭한 후 모듈을 모듈 설정을 no module 로 만든 다음
부트스트랩 클래스를 부팅 시키면 모듈 설정 화면이 다시 나오는데 이때 부트스트랩 main 메서드를 다시 설정해서 부팅하면 된다.

또는 빌드 옵션을 그레이들이 아닌 인텔리제이로 설정해서 해결할 수도 있다. 

(인텔리제이 자체의 문제인지, 공조 마이크로서비스 설계상의 문제인지 정확히 어떤 부분이 문제인지는 모름..)
```
```
* 예외 요청 (포스트맨을 사용해도 된다.)

curl http://localhost:7000/product-composite/13 -i 
존재하지 않는 아이디 404 반환 검증

curl http://localhost:7000/product-composite/113 -s |jq.
추천 목록이 없는지 조회

curl http://localhost:7000/product-composite/213 -s |jq.
리뷰 목록이 없는지 조회

curl http://localhost:7000/product-composite/-1 -i
범위를 벗어난 아이디 조회

curl http://localhost:7000/product-composite/badrequest -i
badrequest 조회
```
## 자동화된 마이크로 서비스 테스트

웹플럭스와 함께 나온 새로운 테스트 클라이언트 WebTestClient 는 요청을 보내고 결과를 검증하는 다양한 API 를 제공한다.

### 복합 마이크로 서비스 테스트 코드 작성
```
* ProductCompositeServiceApplicationTests 참고 

테스트 전 @BeforeEach 로직을 실행해서 시나리오 데이터를 생성한다.
(복합 마이크로서비스 테스트이기 때문에 다른 핵심 마이크로 서비스를 대신하는 데이터가 되며, 공통 API 모듈을 사용해서 만들 수 있다.)

웹 플럭스가 지원하는 WebTestClient 를 사용하면 요청을 보내고 결과를 검증하는 다양한 API 를 사용할 수 있다.

테스트로 반환받은 제이슨 객체에서 값을 꺼내는 방법 .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
```
```
./gradlew build 를 커맨드하면 자동으로 테스트가 실행된다. 
./gradlew test 를 커맨드하면 빌드하지 않고 테스트만 실행한다.
```
## 반자동화된 마이크로서비스 환경 테스트
```
자동화된 테스트는 마이크로서비스를 실행 하지 않아도 되지만 테스트 결과를 확인할 수 없다는 단점이 있다.
마이크로서비스를 실행하고 bash 파일을 만들면 스크립트로 테스트를 관리하고 결과를 받을 수있다. 143 p 
```
```
* test-em-all.bash 참고

java -jar microservices/product-composite-service/build/libs/*.jar & 마이크로 서비스 부팅
./test-em-all.bash 테스트 스크립트 실행 
```

