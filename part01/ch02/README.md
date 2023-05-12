# 스프링 부트 소개 
```
스프링 부트는 스프링 프레임워크와 서드파티 제품으로 구성된 핵심 모듈의 설정 방식을 개선해 상용 스프링 애플리케이션을
빠르게 개발할 수 있도록 지원하는 프레임워크다. 

적용 구성을 최소화하고 필요한 경우에만 구성을 작성해 기존 규칙을 대신한다. 

구성이 필요할 때는 xml 보다 자바 코드, 애노테이션, yml 파일을 사용하는 것이 좋다. 
https://github.com/eternalrecurrenceofthesame/Spring5/blob/main/part1/ch05/README.md 참고
```
```
* JAR 

스프링 부트는 JAR 파일 기반의 런타임 모델을 지원한다. JAR 를 사용하면 아파치 톰캣과 같은 Java EE 웹 서버를 별도로 설치하지 않고도
사용할 수 있다. 76 p
```
```
* 부트 스트랩 클래스 예시

@SpringBootApplication
@ComponentScan({"se.magnus.myapp", "se.magnus.utils"}) // 컴포넌트 스캔 범위를 세밀하게 조정할 수 있다.
```
## 스프링 웹플럭스 
```
https://github.com/eternalrecurrenceofthesame/Spring5/tree/main/part3/ch10
및 교재 82 p 참고 
```
## 스프링 폭스 
```
RESTful 서비스를 만들 때 API 를 문서화 하기 위해 스웨거를 사용하는 경우가 많다. 스프링 폭스는 스프링 프레임워크와는
별개의 오픈소스 프로젝트로써 런타임에 스웨거 기반의 API 문서를 생성한다. 
```
