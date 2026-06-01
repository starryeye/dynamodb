# Credentials and IAM

이 주제는 Spring Boot DynamoDB 애플리케이션의 credential과 IAM을 배우는 단계다.

실습 위치:

```text
theory/16-credentials-and-iam/
```

## 이번 주제에서 배우는 것

- local profile의 dummy credential
- prod profile의 default credential provider
- endpoint override 사용 범위
- table/index ARN에 대한 least privilege IAM

## 이번 주제에서 아직 다루지 않는 것

- capacity
- backup
- CloudWatch alarm

## 설정 원칙

local:

```text
endpointOverride = http://localhost:8000
dummy credential 사용
```

prod:

```text
endpointOverride 사용 금지
IAM role 또는 default credential provider 사용
```

## 운영 포인트

access key를 application.yml, README, source code에 하드코딩하지 않는다. Production 권한은 table과 index ARN 기준으로 최소화한다.

## 확인 질문

- local에서 dummy credential이 필요한 이유는 무엇인가?
- prod에서 endpoint override를 사용하면 왜 위험한가?
- read-only API와 write API에 같은 IAM permission을 줘도 되는가?
