# 마이크로 서비스 소개

### 독립 소프트웨어 컴포넌트의 장점 및 문제점 
```
독립 소프트웨어 컴포넌트를 사용하면 API 로 각 컴포넌트가 통신한다. 이때 컴포넌트에 기능이 추가되거나 변경되어도 이를 사용하는
다른 컴포넌트에서는 API 를 알고 있기 때문에 변경되는 컴포넌트 A 를 호출하는 컴포넌트 B 는 이에 따른 하위 호환성을 유지한다. 

또한 API 를 사용하면 플랫폼의 각 컴포넌트를 다른 컴포넌트와 상관없이 여러 서버로 스케일 아웃 할 수 있다.
(로드 밸런서를 설정하는 방식으로 스케일링을 수행한다? 44 p)
```
```
분산 컴포넌트 시스템을 사용해서 스케일 아웃 하는 것이 좋아 보일 수도 있지만 문제점도 있다. 로드 밸런서를 구성하는 것은 쉽지 않은 
작업이며 다른 컴포넌트에서 API 통신을 하면서 오류가 발생하면 연쇄 장애로 이어질 수 있다. 

또한 컴포넌트가 여러 개로 나뉘어져있기 때문에 관리해야할 일이 늘어나고 역설적으로 유지 보수가 어려워지는 문제도 생긴다. 45 p
```
## 마이크로 서비스 정의
```
마이크로 서비스는 일체형 애플리케이션을 각각의 독립된 컴포넌트로 나누어서 운영하는 방식이다.

마이크로 서비스의 주요 목표

1. 빠르게 개발해 지속적으로 배포할 수 있어야 한다.
2. 수동 혹은 자동으로 쉽게 스케일링할 수 있어야 한다.
```
```
* 독립 컴포넌트로 동작하기 위한 기준

마이크로 서비스는 각 컴포넌트별 데이터베이스를 공유하지 않는다. (다른 마이크로 서비스에 저장된 데이터와 외래 키를 맺지 않는다.)
명확한 인터페이스를 통해서만 통신해야 한다.

도커 컨테이너와 같은 독립된 런타임 프로세스에서 마이크로 서비스 인스턴스를 실행한다.
마이크로 서비스 인스턴스는 stateless 로써 모든 마이크로 서비스 인스턴스가 마이크로 서비스로 들어오는 요청을 처리할 수 있다 ?? 49 p 
```
### 마이크로 서비스의 문제점 
```
동기식 통신을 하는 다수의 소형 컴포넌트들 중 하나에서 문제가 생기면 다른 컴포넌트도 영향을 받는다.
다수의 소형 컴포넌트를 최신 상태로 유지하는 것은 어렵다.

컴포넌트가 처리에 관여하는 요청을 추적하기 어렵고, 컴포넌트 수준의 하드웨어 자원 사용량 분석이 어렵다 ?
다수의 소형 컴포넌트를 **수동으로** 구성하고 관리하는 것은 비용이 많으 들고 오류가 발생하기 쉽다!
```
## 마이크로 서비스 디자인 패턴

마이크로 서비스에서 사용하는 디자인 패턴의 개념을 알아본다! 
```
디자인 패턴이란? 특정 상황에서 발생하는 문제에 대해 재사용 가능한 해결책을 정리한 것이다.
```
### 서비스 검색 패턴 
```
클라이언트가 마이크로서비스 인스턴스를 찾을 수 있게 도와주는 서비스 검색 컴포넌트를 추가하면 클라이언트에서 안정적으로
컴포넌트에 접근할 수 있다.

검색 컴포넌트는 마이크로 서비스와 마이크로 서비스 인스턴스를 자동으로 등록 및 해지할 수 있어야하며 요청을 처리할 수있는지
컴포넌트의 상태를 감지하고 클라이언트 요청을 라우팅해준다. ( 유레카 서버의 개념인듯 )

클라이언트 측 라우팅 방식과, 서버 측 라우팅 방식 두 가지로 구현할 수있다 55 p 참고
```
### 에지 서버
```
에지서버란 클라이언트 요청을 검증하는 컴포넌트로써 공개된 마이크로 서비스 컴포넌트에 접근하는 악의적인 클라이언트의 요청으로
부터 마이크로 서비스를 보호하는 역할을 한다.

(스프링 시큐리티의 역할을 수행한다고 생각하면 된다. 67 p)
(동적 로드 밸런싱 기능을 제공하고자 검색 서비스와 통합되어 구현될 수 있다.) 56 p
```
### 리액티브 마이크로 서비스
```
전통적인 스레드 블로킹 방식의 문제점(과도한 스레드 점령) 을 해결하기 위해 리액티브 마이크로 서비스를 사용할 수 있다.

하나의 컴포넌트에서 스레드를 오래 점령하고 있으면 앞서 설명했든 연쇄 오류 문제가 발생할 수 있기때문에 스레드 요청이 과도하게? 많은 
애플리케이션이라면 논 블로킹 방식의 리액티브 마이크로 서비스를 고려할 수 있다 

(연쇄 오류 관련 문제의 처리는 서킷 브레이커 챕터를 참고한다.)
```
### 구성 중앙화
```
마이크로 서비스 컴포넌트 별 환경 변수와 구성 정보를 중앙 집중식 방식으로 처리하는 컴포넌트(구성 서버) 를 만들면
응집성과 유지 보수성을 높일 수 있다. (쉽게 말해서 구성 정보를 한곳에서 저장하고 환경별 설정을 지원한다는 의미임)
```
### 로그 분석 중앙화
```
컴포넌트 별로 발생하는 이벤트를 로그로 기록해서 가지고 있으면 응집성이 떨어진다 또한 전체 시스템 환경에서 발생하는
사건을 로그로 기록할 수 없다. 

로그를 중앙화된 컴포넌트로 만들고 MSA 컴포넌트별 로그 이벤트를 수집하고 로그 이벤트를 해석해 구조적이고 검색 가능한
형식의 중앙 데이터베이스에 저장해서 관리한다. 또한 로그 이벤트 조회 및 분석을 위한 API 와 그래픽 도구를 제공할 수 있다.
```
### 분산 추적
```
마이크로 서비스 사이에서 흐르는 요청 및 메시지를 추적하기위해 분산 추적을 사용한다. 61 p

마이크로 서비스에서 외부로 요청이나 메시지를 보낼 때는 요청과 메시지를 식별할 수있는 상관 ID 를 꼭 넣어야 한다. 
또한 로그 이벤트에 상관 ID 를 넣어서 중앙화된 로깅 서비스에서 로그 이벤트를 상관 ID 로 추출할 수 있게 해야한다. 
```
### 서킷 브레이커
```
동기 방식으로 통신하는 마이크로 서비스는 요청후 응답을 하는 컴포넌트에서 오류가 발생하면 연쇄 장애로 이어질 수 있고 
동기화된 환경에서는 이런 장애가 빈번하게 발생할 수있다.

서킷 브레이커를 사용하면 대상 서비스에 문제가 있다는 것을 감지하면 새 요청을 보내지 않도록 차단한다.

마이크로 서비스는 의존하는 서비스가 중단되더라도 응답할 수 있도록 탄력성있게 설계되어야 한다. 이런 역할을 
서킷 브레이커가 도와준다.

서비스의 문제를 감지하면 서킷을 열고 요청을 차단하여 연쇄 장애를 방지하고, 반열림 서킷이라고 하는 장애 복구용
프로브 probe 로 서비스가 정상적으로 동작하는지 확인하고자 주기적인 요청을 보내고 정상 동작을 감지하면 서킷을 닫는다.

이러한 것들은 시스템 환경을 탄력적으로 만들고 자가 치유를 가능하게 하는 중요한 기능들이다. 

서킷 브레이커 발생시 실패 응답을 하기 전 컴포넌트를 대체하는 로직을 적용할 수도 있다. 63 p
```
### 제어 루프 
```
서킷 브레이커가 서비스의 오류를 감지하는 역할을 한다면 제어 루프는 마이크로 서비스 자체의 상태를 지속적으로
감지하는 역할을 한다.

이 컴포넌트는 운영자가 지정한 상태와 실제 상태를 지속적으로 관찰해서 두 상태가 다른 경우 지정한 상태가 되도록
조치를 취한다. (쿠버네티스 같은 컨테이너 오케스트레이터로 이 패턴을 구현할 수 있다.)
```
### 모니터링 및 경고 중앙화 
```
마이크로 서비스 별 하드웨어 자원 사용량을 분석하기 위해 프로메테우스를 사용해서 모니터링 할 수 있다.
(마이크로 서비스별로 하나하나 관리하는 것은 힘들기 때문에 역설적으로 중앙화된 관리를 사용해야 하는 경우가 많다.)
```

다른 주요 고려 사항 68 p 참고 