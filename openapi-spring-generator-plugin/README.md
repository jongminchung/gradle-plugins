# Openapi Spring Generator plugin

## Motivation

openapi 문서를 위해 JSR 303, JSR 308 기반의 애너테이션을 추가하고 더불어 간략한 API 문서가 아닌 다른 부서와 협업을 하기 위해서는

API 문서를 자세하게 제공해야했습니다.

그러나, API 문서를 따로 제공하려다보니 코드 베이스와 달라지는 문제가 발생했어요.

그래서 API 문서를 기반으로 코드를 생성하는 방법을 선택했습니다.

## openapi generator option 정리

### artifact, pom metadata

- groupId: pom.xml에 기입되는 groupId
- artifactId: 생성되는 아티팩트 이름 및 Jar 이름에 사용
- artifactVersion: 아티팩트 버전
- artifactDescription: pom description

### java package

- basePackage: 전체 코드의 루트 패키지 (= 'invokerPackage')
- apiPackage: api 인터페이스, 컨트롤러 패키지
- modelPackage: DTO/모델 클래스 패키지
- configPackage: Spring 설정 클래스 패키지 (예: OpenAPI docs 설정, CORS 등)

### Library, runtime, spring version 관련

- library:
    - spring-boot: controller, dto
    - spring-cloud: spring cloud feign
    - spring-http-interface: spring 6 http interface
- dateLibrary:
    - java8: Java 8 JSR310 (LocalDate, OffsetDateTime 등)
    - java8-localdatetime: LocalDateTime 중심 레거시

- useSpringBoot3: spring boot 3.x 용 코드와 의존성을 생성 (useJakartaEe = true가 자동 활성화됨)
- bigDecimalAsString: false (만약 이를 계산해서 제공해야한다면 Client 쪽에서 이를 제어하는게 맞음)
    - ```yaml
        components:
        schemas:
          Price:
            type: object
            properties:
              amount:
                type: number
                format: bigdecimal
      ```

    - ```java
      public class Price {
          private String amount; // bigDecimalAsString = true
          private BigDecimal amount; // bigDecimalAsStirng = false
      }
      ```
        - Java BigDecimal → JSON → JavaScript Number 로 갈 때, JS에서 정밀도가 깨지는 문제를 회피하려는 목적입니다.

