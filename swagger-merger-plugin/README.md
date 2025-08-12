https://github.com/jongmin-chung/spring-multi-module-boilerplate/tree/main/docs/api-docs

해당 경로를 분석해서 README.md 파일에 작성해주세요.

해당 과정을 수행하는 gradle plugin을 만드려고 해.

즉 yaml 파일들을 하나의 OAS 문서로 합치는 작업을 수행하는 Gradle Plugin을 만들고 싶습니다.

---

# OAS Generation Plugin

이 Gradle Plugin은 여러 YAML 파일을 하나의 OpenAPI Specification(OAS) 문서로 병합하는 작업을 자동화합니다.

## 주요 기능
- 지정된 경로에서 YAML 파일을 검색.
- YAML 파일들을 병합하여 단일 OAS 문서 생성.
- Gradle Task로 실행 가능.

## 개발 계획
- YAML 파일 병합 로직 구현.
- Gradle Task 정의.
- 테스트 코드 작성 및 실행.
