# 공조 마이크로서비스 집합 생성하기 

기능을 최소화한 공조 마이크로서비스를 만들어보자 ! 

#### + GitBash 에서 환경 변수 설정하기
```
export BOOK_HOME(환경변수명)=~/msa-spring-cloud 
cd $BOOK_HOME 
폴더 이름이 아닌 환경 변수 명으로 해당 디렉토리를 쉽게 호출 할 수 있다.

git clone https://github.com/저장소 주소 $BOOK_HOME
예제 자료나 다른 자료를 내려받을 때도 환경변수를 사용하면 깔끔하다! 
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
여기서 설명한 부분들은 책의 내용과 다른 점이 많기 때문에 직접 찾아보고 자료를 정리한 것. 틀린 내용이 있을 수 있다.

## RESTful API 추가
### API 정의를 배치할 별도의 그레이들 프로젝트 만들기 (API 문서화)
```
자바 인터페이스를 사용해 RESTful API 를 설명하고, 모델 클래스를 만들어서 API 요청 및 응답에 사용할 데이터를 정의한다.
(MSA API 를 정의하는 모듈을 만들고 공조 마이크로서비스에 포함시켜서 관리하겠다는 의미) 115 P

마이크로 서비스 API 를 정의한 모듈을  공조 마이크로 서비스 그룹에서 관리하는 것은 좋은 선택지가 될 수 있다. 
(마이크로 서비스와 API 문서를 같이 관리한다는 의미임) app 참고 
```
```
DevOps 관점에서 보면 모든 프로젝트는 각자의 빌드 파이프라인을 가지고 각자 API 문서와 util 프로젝트에 대한 버전 제어
의존성을 갖게 하는 것이 바람직하다. 115 p
```
### 전체 마이크로 서비스가 공유하는 헬퍼 클래스를 배치할 util 프로젝트 만들기 
```
골격 마이크로 서비스가 사용할 공통 유틸리티 모듈을 만들어서 공조 마이크로 서비스에 포함시켰다. util 참고
하지만 앞서 설명했듯이 각각의 마이크로 서비스에서 유틸 모듈을 제어하는 것이 바람직하다. 
```
```
* GlobalControllerExceptionHandler

이 클래스는 HTTP 요청으로 발생하는 오류를 핸들링하고 응답을 하는 유틸리티 역할을 한다.

@RestControllerAdvice(@ControllerAdvice + @ResponseBody) 의 컨트롤러 어드바이스는 @ExceptionHandler, @InitBinder
또는 @ModelAttribute 가 선언된 메서드를 @Controller 빈에서 공유할 수 있게 해준다.

ex 3 GlobalControllerExceptioHandler 참고 
```
## 핵심 마이크로 서비스의 API 구현하기 

앞서 만든 API 설명서와 util 모듈을 핵심 마이크로 서비스에 구현하기!
```
* API, util 모듈 조립하기 

각각의 핵심 마이크로 서비스 build.gradle 에 공통 모듈을 추가한다 

dependencies{
  implementation project(':api')
  implementation project(':util;) } 
  
공통 모듈 추가 후 부트스트랩 클래스에서 @ComponentScan("se.magnus") 스캔 대상을 지정한다.
```
```


```



