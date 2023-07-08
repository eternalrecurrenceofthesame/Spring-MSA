# 도커 컨테이너를 사용해서 마이크로서비스 배포하기 

## 도커 소개
```
도커는 리눅스 컨테이너에서 실행되는 컨테이너로써 가상머신을 대체할 수 있다. 도커 컨테이너를 사용하면 가상머신으로 같은 사양의 서버를
사용하는 것보다 훨씬 많이 실행시킬 수 있다. 
```
### 도커로 단일 마이크로서비스 실행
```
마이크로서비스를 도커 컨테이너로 실행하려면 도커 이미지로 패키징해야한다. Dockerfile 을 만든 후 도커에 맞춰 마이크로서비스를
구성하면 된다.

마이크로서비스는 컨테이너에서 실행되는 자체 IP 주소, 호스트, 포트를 가지고 다른 마이크로서비스와 격리된다. 
```
```
1. yml 도커 프로필 설정하기

spring.profiles: docker 프로필은 도커 컨테이너에서 마이크로서비스를 실행할 때만 적용할 수 있다. (프로필 값은 기본 값보다 우선한다.)
참고로 --- 로 구분하면 yml 파일 하나에 여러 개의 스프링 프로필을 사용할 수 있다. 

도커 컨테이너의 마이크로서비스는 다른 마이크로서비스와 격리되기 때문에 포트 충돌이 발생하지 않는다. 즉 모든 마이크로 서비스가
기본 포트 8080 을 사용할 수 있다. product yml 참고
```
```
2. 도커 이미지 빌드 파일 만들기

Dockerfile 참고 // 모르는 내용이 많기 때문에 예제의 내용을 그대로 복사 붙이기 했다. 도커를 배우고 리팩토링 예정
```
```
3. 도커 이미지 빌드하고 실행해보기

./gradlew :microservices:product-service:build // jar 파일 만들기
unzip -| microservices/product-service/build/libs/product-service-1.0.0-SNAPSHOT.jar // 실제 내용물 확인

cd microservices/product-service // 빌드할 마이크로서비스 모듈로 이동

docker build -tag product-service:1.0.0 . // product-service 라는 이름으로 앞서 만든 도커 이미지 파일을 빌드한다 !마지막 점 앞에 띄어쓰기 조심!
docker images ls // 이미지 파일을 확인한다 

docker run product-service:1.0.0 // 도커 실행 
```

전체 yml 구성 설정은 핵심 마이크로서비스를 참고한다. 

### 도커 컴포즈를 사용해서 마이크로서비스 환경 관리하기
```
전체 마이크로서비스 시스템 환경을 관리하는 도커 컴포즈를 만들어본다. 
```
```
1. MongoDB, MySQL 을 도커 컴포즈 구성으로 만들기

각 핵심마이크로서비스는 데이터소스로 데이터베이스와 연결한다. 공조 마이크로서비스의 도커 컴포즈는 각 데이터베이스의 이미지 파일을 가져와서 
컨테이너에 올리고 관리할 수 있는 환경 설정을 제공한다. 

도커 컴포즈가 제어하는 시스템 환경에 MongoDB , MySQL 을 추가하고 도커 컨테이너로 실행될 때도 데이터베이스를 찾을 수 있도록 추가 구성한다.

docker-compse, 핵심 마이크로서비스 yml 참고 
```
```
2-1. 파티션 없이 RabbitMQ 사용하기

파티션을 사용하지 않고 RabbitMQ 와 마이크로서비스를 테스트한다. 

cd msa-spring-reactive
./gradlew build && docker-compose build && docker-compose up -d // 핵심 마이크로서비스를 빌드하고 도커 컴포즈 정보를 빌드한다 
                                                                   d 옵션을 사용하면 터미널이 잠기지 않고 커맨딩을 계속 할 수 있다.

curl -s localhost:8080/actuator/health | jq -r .status // 마이크로서비스 환경이 정상 작동하는지 확인한다. up 으로 응답하면 테스트 실행 준비 완료
```
```
body='{"productId":1, "name":"product name C", "weight":300, 
"recommendations":
[{"recommendationId":1, "author":"author 1","rate":1,"content":"content 1"},
{"recommendationId":2, "author":"author 2","rate":2,"content":"content 2"},
{"recommendationId":3, "author":"author 3","rate":3,"content":"content 3"}],
"reviews":
{"reviewId":1,"author 1","subject":"subject 1","content":"content 1"},
{"reviewId":2,"author 2","subject":"subject 2","content":"content 2"},
{"reviewId":3,"author 3","subject":"subject 3","content":"content 3"}]}'

curl -X POST product-composite:8080/product-composite -H "Content-Type: application/json" --data "$body"
커멘드를 실행해서 이벤트를 토픽을 게시한다. 

curl localhost:8080/product-composite/1 | jq  // 커맨드로 조회한다.
curl -X DELETE localhost:8080/product-composite/1  // 커맨드 삭제 후 다시 조회하면 404 에러 메시지가 반환된다.  

docker compose down 으로 도커 종료 
```
