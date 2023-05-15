# 공조 마이크로서비스 집합 생성하기 

기능을 최소화한 공조 마이크로서비스를 만들어보자 ! 

#### + GitBash 에서 환경 변수 설정하는 방법
```
export BOOK_HOME(환경변수명)=~/msa-spring-cloud 
cd $BOOK_HOME 
폴더 이름이 아닌 환경 변수 명으로 해당 디렉토리를 쉽게 호출 할 수 있다.

git clone https://github.com/저장소 주소 $BOOK_HOME
예제 자료나 다른 자료를 내려받을 때도 환경변수를 사용하면 깔끔하다! 

(참고로 GitBash 환경 변수 설정시 Bash 를 껏다 키면 환경 변수 설정이 사라진다.)
```

## 마이크로 서비스 환경 소개
```
예제로 설명할 마이크로서비스는 복합 환경 서비스인 Product Composite 와 핵심 서비스 Product, Review, Recommendation 으로 구성된다 103 p 
```
#### + GradleWrapper 를 사용해서 공조 마이크로 서비스를 생성하고 골격 마이크로 서비스를 관리하기
```
그레이들이란? Groovy를 이용한 빌드 자동화 시스템으로써 라이브러리를 추가하고 버전을 관리할 수 있게 도와주는 역할을 한다.
https://codecrafting.tistory.com/1 참고

그레이들 래퍼란? 새로운 환경에서 프로젝트를 설정할 때 JAVA 나 grdale 를 설치하지 않고 바로 빌드할 수 있게 도와주는 역할을 한다.
그레이들 래퍼를 사용해서 골격 마이크로 서비스를 관리하는 프로젝트인 공조 마이크로 서비스?? 를 생성한다.

공조란? 여러사람이 함께 돕는 것을 뜻하는 단어인듯?? (정확하지 않음)

즉 공조 마이크로서비스 내에서 골격 마이크로서비스(실제 서비스를 제공하는 MSA 애플리케이션) 을 만들고 사용할 수 있다는 의미임.
(이 블럭의 내용은 책의 내용을 토대로 유추한 것이 많기 때문에 정확하지 않음)
```
```
* Gradle 설치 및 GradleWrapper 클래스 생성하는 방법

https://gradle.org/releases/ 에서 그레이들을 다운로드 받을 수 있다.
GradleWrapper 클래스를 생성해서 공조 마이크로 서비스로 사용하려면 폴더(msa-spring-cloud2)를 만들고 gradle init 을 커맨드 한다.
```
```
* 인텔리제이에서 골격 마이크로 서비스 생성하기

spring starter io 에서 마이크로 서비스로 사용할 프로젝트를 생성하고 File -> New -> ModuleFromExistingSource.. 를 클릭해서
만들어둔 프로젝트를 추가하면 공조 마이크로 서비스의 골격을 추가할 수 있다.

(골격 마이크로서비스를 추가하는 부분이 교재와 다른 부분이 있어서 이 내용을 작성함!) 106 p
```
```
* 공조 마이크로 서비스를 사용해서 골격 마이크로 서비스 빌드하기 (멀티 프로젝트 빌드 설정)

공조 마이크로 서비스를 만들면 각각의 마이크로 서비스를 한번에 빌드할 수 있다.
https://docs.gradle.org/8.1.1/userguide/multi_project_builds.html 참고 

settings.gradle 설정

rootProject.name = 'msa-spring-cloud2' // 공조 마이크로 서비스
include 'microservices:product-composite-service' // 골격 마이크로 서비스들 
include 'microservices:product-service'
include 'microservices:recommendation-service'
include 'microservices:review-service'

./gradlew build 공조 마이크로 서비스에서 커멘드


DevOps 관점에서 보면 멀티 프로젝트를 이용한 일괄 빌드는 바람직하지 않다. 마이크로서비스 프로젝트별 별도의 빌드 파이프라인을 설정하는
것이 좋다. (예제 진행을 쉽게 하고자 빌드 커멘드를 하나로 모았음)
```
#### ++ 골격 마이크로 서비스및 API, 헬퍼 클래스를 추가할 때 주의할 점
```
골격 마이크로 서비스를 만들고 모듈로 등록할 때 공조 마이크로 서비스의 폴더에 모듈을 넣고 등록해야한다 그렇지 않으면 제대로 빌드
되지 않는 경우가 발생함. 

*다른 위치에서 골격 마이크로서비스 모듈을 등록하고 공조 마이크로 서비스 폴더의 위치로 옮겨서 사용하는 경우 빌드 시 오류가 발생한다.*
(인텔리제이 IDE 사용시)
```
여기서 설명한 부분들은 책의 내용과 다른 점이 많기 때문에 직접 찾아보고 자료를 정리한 것. 틀린 내용이 있을 수 있다.

## RESTful API 추가
### API 정의를 배치할 별도의 그레이들 프로젝트 만들기
```
자바 인터페이스를 사용해 RESTful API 를 설계하고, 모델 클래스를 만들어서 API 요청 및 응답에 사용할 데이터를 정의한다.
(MSA API 를 정의하는 모듈을 공조 MSA 가 관리하며, 각각의 MSA 가 모듈을 주입받아 사용하겠다는 의미) 115 P

마이크로 서비스 API 를 정의한 모듈을  공조 마이크로 서비스 그룹에서 관리하는 것은 좋은 선택지가 될 수 있다. 
(마이크로 서비스와 API 문서를 같이 관리한다는 의미임) * app 참고 
```
```
DevOps 관점에서 보면 모든 프로젝트는 각자의 빌드 파이프라인을 가지고 각자 API 와 util 프로젝트에 대한 버전 제어 의존성을
갖게 하는 것이 바람직하다. 115 p
```
### 전체 마이크로 서비스가 공유하는 헬퍼 클래스를 배치할 util 프로젝트 만들기 
```
골격 마이크로 서비스가 사용할 공통 유틸리티 모듈을 만들어서 공조 마이크로 서비스에 포함시켰다. * util 참고
하지만 앞서 설명했듯이 각각의 마이크로 서비스에서 유틸 모듈을 제어하는 것이 바람직하다. 
```
```
* GlobalControllerExceptionHandler 참고

이 클래스는 프로토콜별 예외 처리를 API 구현(REST 컨트롤러의 비즈니스 로직) 에서 분리하기 위한 유틸리티 클래스 130 p

@RestControllerAdvice(@ControllerAdvice + @ResponseBody) 의 컨트롤러 어드바이스는 @ExceptionHandler, @InitBinder
또는 @ModelAttribute 가 선언된 메서드를 @Controller 빈에서 공유할 수 있게 해준다.
```
## 핵심 마이크로 서비스의 API 구현하기 

앞서 만든 API 와 util 모듈을 핵심 마이크로 서비스에서 구현하기!
```
* API, util 모듈 조립하기

공조 마이크로 서비스의 settings.gradle 에 공통 모듈을 추가한다
incldue ':api'
incldue ':util'

각각의 핵심 마이크로 서비스 build.gradle 에 공통 모듈을 추가한다 

dependencies{
  implementation project(':api')
  implementation project(':util;) } 

각각의 핵심 마이크로 서비스 settings.gradle 에 include 를 추가한다.
include ':api'
include ':util'
  
공통 모듈 추가 후 부트스트랩 클래스에서 @ComponentScan("se.magnus") 스캔 대상을 지정한다.

인텔리제이 IDE 사용시 프로젝트의 의존성을 인식하지 못하는 경우가 있는데 이런 경우 

IntelliJ - File - Invalidate caches / Restart 메뉴를 실행후 재구동하면 인식한다. (이거 찾느라 엄청 고생함..)
(https://www.lesstif.com/spring/gradle-intellij-113345573.html 참고)
```
```
* microservices 참고

공통 api , util 을 사용해서 마이크로서비스(product, recommendation, review) 를 구현한다. 
```
```
* product 를 빌드하고 실행해보기

공조 마이크로 서비스에서 ./gradlew build 커맨드로 전체 마이크로 서비스를 빌드한다.
(빌드하면 build/libs 에 빌드 파일이 생긴다.)

java -jar microservices/product-service/build/libs/스냅샷 이름.jar & 커맨딩으로 프로덕트를 실행한다. 
(jar 파일 위치에서 커맨딩 해도 된다.)

curl http://localhost:7001/product/123 
프로덕트의 엔드포인트 서비스를 호출해서 서비스 결과를 확인한다. 
```
## 복합 마이크로 서비스 추가하기
```
* product-composite-serivce 참고

앞서 만든 세 가지 핵심 서비스를 호출하는 복합 서비스를 추가해서 마이크로 서비스를 하나로 묶어보자! 

복합 서비스 구현은 핵심 서비스로의 발신 요청을 처리하는 통합 컴포넌트 ProductCompositeIntegration 와
복합 서비스 자체 구현 두 부분으로 나뉜다. ProductCompositeServiceImpl (api 구현)  122 p

복합서비스의 구현도 앞서 구현한 핵심 마이크로서비스와 마찬가지로 api, util 의 공통 정보를 기반으로 만들어진다.
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

지금까지 만든 공조 마이크로 서비스를 수동으로 조회하는 테스트를 해보겠음! 

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
공조 마이크로서비스를 설계하고 각각의 마이크로 서비스의 빌드와 공통 모듈을 공조 마이크로서비스에서 관리할 경우 각 
마이크로서비스의 부트스트랩 클래스를 사용할 수 없고 빌드된 파일은 커맨딩을 사용해서 실행해야 한다. 

(부트스트랩으로 애플리케이션을 실행하는 방법을 찾지 못함.)
```

```
* 예외 요청


```
