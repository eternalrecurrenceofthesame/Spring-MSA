# 마이크로 서비스를 위한 설계 원칙 

## The twelve-factor app

### 소개
```
The twelve-factor app 은 다음의 특징을 가진 SaaS (클라우드에 배포 가능한 애플리케이션) 을 구축하기 위한 방법론이다. 

- 새로운 개발자가 프로젝트에 참여하기 위한 시간과 비용을 최소화 하기 위해 자동 구성 애노테이션을 사용하는 애플리케이션

- 운영중인 시스템에 대한 명확한 contract(인터페이스) 를 가지며, 프로그램 실행 환경에서 최대한의 이식성(maximum portability) 을 제공한다.
(인터페이스를 별도로 설계하고 구현 애플리케이션에서 인터페이스를 이식 받아서 사용하거나, 계약과 구현을 분리해 유지 보수의 응집성을 높이는 것을 의미한다.)

- 서버와 시스템 운영 체제가 없는 현대 클라우드 플랫폼 애플리케이션 개발에 적합하다.  
- 개발과 운영 환경의 분리를 최소화 하여, 지속 가능한 개발의 유연성을 최대화 한다. (개발하고 바로바로 운여에 적용한다는 의미)

- 개발 관행(practice) 과 구조 도구의 중요 변화 없이 스케일업 할 수 있다.

The twelve-factor app 방법론은 어떤 프로그래밍 언어, 데이터베이스 를 사용하든 적용할 수 있다.
```
## 1. CodeBase

하나의 코드 베이스를 만들어서 개정 버전을 추적한다. One codebase tracked in revision control, many deploys

```
버전 관리 시스템(Git, Mercurial, or Subversion) 으로 코드베이스(인터페이스) 를 저장해서 관리한다. 수정이 발생하더라도
사용자 간의 버전을 맞춰야 하기 때문에 같은 버전의 애플리케이션을 관리할 수 있다.  

애플리케이션당 하나의 코드베이스로 시작하지만 버전 관리가 되지 않는 서로 다른 코드 베이스를 사용한다면 하나의 애플리케이션이
아닌 각각의 다른 애플리케이션으로 변질되기 때문에 코드베이스를 만들고 버전을 서로 공유해야 한다.

코드베이스는 스테이징 상태에서 배포되기 전까지 모두 같은 버전을 사용하기 떄문에 동일한 애플리케이션의 다른 배포를 식별할 수 있다.
```
## 2. Dependencies

의존 관계를 명확하게 선언하고 분리시켜야 한다. Explicitly declare and isolate dependencies

```
루비, 파이썬, C 언어는 의존 관계 분리 툴 (dependency isolation tool) 을 제공한다. (안 써봐서 정확하게 모름)

의존 관계를 명확하게 선언하면 애플리케이션을 관리할 새로운 개발자가 쉽게 접근할 수 있게 된다.
One benefit of explicit dependency declaration is that it simplifies setup for developers new to the app.

마이크로 서비스로 애플리케이션을 구현할 때 (그 외 여러가지 모듈이 합쳐진 애플리케이션을 구현할 때) 모듈 별 공통적으로 사용되는
의존관계(라이브러리 패키지) 가 충돌되지 않도록 관리해야 한다.

그레이들이나 메이븐으로 의존 관계를 관리할 때 모듈별 공통 의존성의 버전 충돌이 일어나지 않도록 해야한다.
(중복되는 것들은 애초에 같은 버전을 사용 한다.) 

Twelve-factor apps 은 또한 curl, imagemagick 같이 보편적으로 사용되는 시스템 툴과 충돌하지 않도록 관리해야한다. 이러한 도구 들은
미래 애플리케이션 환경에서 작동을 보장할 수 없으며 애플리케이션과 강하게 결합될 수 있다. that tool should be vendored into the app.
```
## 3. Config

구성 설정은 코드에서 떼어내서 환경변수에 저장해야 한다. Store config in the environment
```
구성 설정은 데이터 베이스 연결 정보, 외부 서비스 (아마존 or 트위터) 의 비밀번호 정보, 배포의 표준 호스트 네임을 가지고 있다.
애플리케이션에서 이런 설정들을 코드에 저장하는 것은 Twelve-factor apps 규칙을 위반하는 행위이다.

구성 설정은 배포에 따라서 크게 변화하지만 코드는 변화하지 않기 때문에 구성 설정을 코드에서 분리해서 관리한다.

구성 설정은 Twelve-factor apps 에 따라서 환경 변수로 관리되어야 한다. 환경 변수로 구성 설정을 관리하면 코드의 변경 없이 쉽게 설정을
바꿀 수 있다.

환경 변수를 사용하면 구성 파일을 사용하는 것보다 설정이 노출될 일이 없으며 언어 및 os 에 구애 받지 않는 표준으로 사용할 수 있다.
they are a language- and OS-agnostic standard.

구성을 그룹으로 관리하는 방법도 있지만 이 방법은 애플리케이션이 확장될 때마다 특정 구성 그룹과 강하게 결합되어 관리에 취약하다.

결론적으로 환경 변수를 구성 설정으로 사용하면 독립적으로 관리되어 세분화된 컨트롤을 할 수 있으면서 배포된 애플리케이션의 생존 주기와
상관없이 간편하게 확장할 수 있다. 
```
```
참고로 루비나 스프링을 사용하는 경우 구성이 배포에따라서 달라지지 않기 때문에 코드 내부에서 구성 설정을 만드는 것이 좋다.

Note that this definition of “config” does not include internal application config, such as config/routes.rb in Rails,
or how code modules are connected in Spring. This type of config does not vary between deploys, and so is best done in the code.

마이크로 서비스로 애플리케이션을 구현해서 모듈이 분산되어 있다면 스프링의 경우 구성 중앙화 서버를 만들어서 일괄적으로 구성 정보를 관리할 수 있다.
분산된 여러 개의 모듈을 한 곳에서 관리할 수 있기 떄문에 응집성이 높아진다. (루비는 안 써봐서 모름)

구성 중앙화에 대해서는 msa-ch12 를 참고한다. 
```
