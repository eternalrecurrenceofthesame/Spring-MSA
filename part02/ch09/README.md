# 넷플릭스 유레카와 리본을 사용한 서비스 검색

마이크로서비스는 넷플릭스 유레카 클라이언트 모듈을 사용해서 넷플릭스 유레카와 통신한다. 

## 서비스 검색 소개 

### DNS 기반 서비스 검색의 문제점
```
DNS 란? 도메인 이름을 사용했을 때 도메인을 실제 네트워크상의 IP 로 바꾸는 과정을 의미한다. 
```
```
DNS 클라이언트는 보통 리졸브된 IP 주소를 캐시하고 DNS 이름에 대응되는 IP 주소가 여러 개인 경우 동작하는 첫 번째 IP 주소를 계속
사용한다.

즉 컨테이너에 올라간 마이크로서비스의 인스턴스를 두 개로 확장해도 두 개의 인스턴스가 같은 주소값을 가지게 되고 하나의 인스턴스만
사용하게 된다. 동적으로 변하는 마이크로서비스 인스턴스를 처리한는 데 적합하지 않다! 333 p 참고 
```
### 넷플릭스 유레카를 사용한 서비스 검색
```
클라이언트는 사용 가능한 마이크로서비스 인스턴스의 정보를얻고자 검색 서비스 넷플릭스 유레카와 통신한다.

1. review 서비스 등의 마이크로서비스 인스턴스는 시작할 때마다 자신을 유레카 서버에 등록한다.

2. 각 마이크로서비스 인스턴스는 자신이 정상이며 요청을 받을 준비가 됐음을 알리고자 정기적으로 유레카 서버에
하트비트 메시지를 보낸다.

3. 복합 마이크로서비스 같은 클라이언트는 클라이언트 라이브러리를 사용해, 사용 가능한 서비스의 정보를 정기적으로
유레카 서비스에 요청한다.

4. 클라이언트에서 다른 마이크로서비스로 요청을 보내야 하는 경우에는 검색 서버에 요청하지 않고도 클라이언트 라이브러리에
보존된 사용 가능한 인스턴스 목록에서 대상을 선택할 수 있으며 보통 라운드 로빈 방식으로 인스턴스를 선택한다 ??
```
## 넷플릭스 유레카 서버 설정하기
```
*  도커 컴포즈 설정하기 docker-compose

eureka:
  build: spring-cloud/eureka-server
  mem_limit: 512m
  ports:
    - "8761:8761"  # 마이크로서비스에서 사용하는 8080 기본 포트를 변경한다. 
```
## 넷플릭스 유레카 서버에 마이크로서비스 연결하기
```
1. 테스트 코드 수정하기

단일 마이크로서비스 테스트시에는 유레카 서버를 실행할 필요가 없기 때문에 @SpringBootTest 애노테이션이 있는 테스트에 유레카 false 를 추가한다.
@SpringBootTest(webEnviornment=RANDOM_PORT, properties = {"eureka.client.enabled=false"})
```
```
2. @LoadBalanced 를 사용해서 로드밸랜싱 필터를 WebClient 에 주입하기

도커 컴포즈를 구성하면서 복합 마이크로서비스의 포트를 매핑해서 복합 마이크로서비스만 외부에서 접근할 수 있도록 만들었다. 
복합 마이크로서비스에 @LoadBalcned 를 사용해서 WebClient 빈을 생성할 때 필터에 로드밸런싱을 추가할 수 있다.

이 동작은 컴포넌트 클래스에서 생성자가 실행되어야 수행되므로 통합 컴포넌트 클래스의 생성자를 구성할 때 build 메서드를 사용해서
초기화해주면 된다.

ProductCompositeServiceApplication, ProductCompositeIntegration 참고
```
```
+ 복합 클라이언트 측에 로드밸런싱을 해야하는 이유

각각의 핵심 마이크로서비스에 접근하기 전 로드 밸런싱을 적용해서 핵심 마이크로서비스의 수에 비례하여 자연스럽게 로드 밸런서의
크기를 조정할 수 있다. spring 5 445 p
```
```
3. 유레카 서버 모듈 자바 설정

부트 스트랩 클래스에 @EnableEurekaServer 를 설정한다. eureka-server 참고
```
```
* 그레이들 의존관계 설정하기

implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-server'(서버)
implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'(모듈)

유레카서버에는 서버 의존성을 추가하고 MSA 모듈에는 클라이언트 의존성을 추가한다.
```
```
4. yml 설정 수정하기

복합 마이크로서비스 yml 에서 각 핵심 마이크로서비스의 호스트와 포트의 값을 통합 컴포넌트 클래스에서 사용했다면
지금부터는 이 값 대신 핵심 마이크로서비스의 API 를 가리키는 기본 URL 선언으로 대체한다.

app:  // DNS 기반 방식 
  product-service:
    host: localhost
    port: 7001 
앞서 설명했듯이 yml 방식을 사용해서 DNS 기반의 검색서비스를 사용하면 문제가 발생할 수 있다! 

private static final String PRODUCT_SERVICE_URL = "http://product";
필드 값으로 실제 DNS 이름이 아닌 핵심 마이크로서비스를 유레카 서버에 등록하는 가상 호스트 이름을 사용한다. 

ProductCompositeIntegration 참고 
```
```
5. 핵심 마이크로서비스 yml 설정 수정

spring.application.name 설정으로 가상 호스트 이름이 넷플릭스 유레카로 등록되기 때문에 이 값을 수정해야 한다.
핵심 msa yml 참고 
```
## 개발 프로세스에서 사용할 유레카 구성 설정
```
유레카는 다양한 구성 옵션을 제공한다. 상용 환경에서 사용하는 대부분의 구성 옵션에 대한 적절한 기본 값을 제공하지만 기본 구성 값이
많은 만큼 개발 프로세스에서 사용할 때 시작 시간이 길어질 수 있다. 

개발 과정에서는 이런 대기 시간을 최소화해서 사용하는 것이 유용하다! 

참고로 상용 환경에서는 넷플릭스 유레카 서버의 고가용성을 보장하기위해 하나 이상의 유레카 서버를 사용해야 한다.
```
### 유레카 구성 매개변수 알아보기
```
https://github.com/eternalrecurrenceofthesame/Spring5/tree/main/part4/ch13 참고
자세한 정보는 spring-cloud eureka docs 를 참고한다
```
### 유레카 서버 구성
```
* 개발 환경에서 사용하는 유레카 서버 구성

response-cache-update-interval-ms: 5000 
# 유레카 서버의 시작시간을 최소화 하기 위한 매개변수로 해당 시간마다 캐시된 클라이언트 정보를 업데이트한다.

eureka-server yml 참고 342 p 
```
### 유레카 서버에 연결할 클라이언트 구성하기 
```
각 마이크로서비스의 yml 을 참고한다. 
```
## 유레카 검색 서비스 사용하기
```
* 전체 애플리케이션 빌드 및 도커 컴포즈 빌드
./gradlew build && docker-compose build && docker-compose up -d // d 옵션을 사용하면 터미널이 잠기지 않는다.
```
### 검색 서비스 테스트해보기
```
* 인스턴스 확장

docker-compose up -d --scale review=3 // 리뷰 인스턴스 확장 커맨드 

curl -H "accept:application/json" localhost:8761/eureka/apps -s |jq -r .applications.
application[].instance[].instanceId // curl 커맨드로 확장된 인스턴스 확인하기

curl localhost:8080/product-composite/2 -s | jq -r .serviceAddresses.rev 
// 클라이언트 로드밸런서로 요청 후 결과에 있는 review 서비스의 주소를 확인

review 서비스의 주소는 인스턴스를 확장했기 때문에 매번 다를 수 있다. 로드 밸런서가 review 서비스 인스턴스를 
호출할 때 라운드 로빈 방식을 사용하기 때문이다. 

docker-compose logs -f review // review 로그 확인 인스턴스가 돌아가면서 요청에 응답한다. 
```
```
* 인스턴스 축소

docker-compose up -d --scale review=2 

인스턴스가 축소되면 클라이언트(복합 msa) 측에서 호출이 실패할 수 있다. 축소된 정보가 전파되는 데 걸리는 시간 때문.
전파되는 공백 동안 로드 밸런서가 사라진 인스턴스를 선택할 수 있음 
```
```
* 유레카 서버의 장애 상황 테스트 (유레카 서버 중지)

유레카 서버가 중된되기 전에 클라이언트가 사용 가능한 마이크로서비스 인스턴스에 대한 정보를 이미 읽고 로컬에 
캐시되어 있으면 문제될 게 없다 하지만 새 인스턴스는 사용할 수 없으며 실행 중인 인스턴스가 종료되더라도 알 수 없다.

docker-compose up -d --scale review=2 --scale eureka=0 // 유레카 서버 중지 2 개의 review 인스턴스 실행
curl localhost:8080/product-composite/2 -s | jq -r .serviceAddresses.rev // review 서비스 주소 2 개 확인
```
```
* review 인스턴스가 중지되는 경우

유레카 서버가 중단되면 클라이언트는 리뷰 인스턴스가 중단됐는지 알지 못하고 라운드 로빈 방식을 이용해서 중지된 
인스턴스에 요청을 보낼 수 있다. (시간 초과 및 재시도 등의 복원 메커니즘을 사용해서 이를 방지할 수 있다. 뒤 단원에서 설명)
```
```
* 유레카 서버가 중단된 상태에서 product 인스턴스 추가

docker-compose up -d --scale review=1 --scale eureka=0 --scale product=2

curl localhost:8080/product-composite/2 -s | jq -r .serviceAddresses.pro // 조회시 하나만 나옴 

product 인스턴스 확장 전 유레카 서버가 중단 됐으므로 클라이언트는 새로운 인스턴스를 알지 못한다 그렇기 때문에
하나의 인스턴스로만 요청이 몰리게 된다. 
```
