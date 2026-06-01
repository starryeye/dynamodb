# Backup and Monitoring

이 주제는 DynamoDB production 보호 장치와 관측 지표를 배우는 단계다.

실습 위치:

```text
theory/17-backup-monitoring/
```

## 이번 주제에서 배우는 것

- PITR
- backup
- deletion protection
- CloudWatch alarm
- latency, throttling, consumed capacity, error rate

## 이번 주제에서 아직 다루지 않는 것

- key design 변경
- transaction 구현
- WebFlux

## Spring Boot 관점

practice 애플리케이션은 actuator health와 metrics를 제공할 수 있다. DynamoDB 자체의 운영 지표는 CloudWatch에서 확인한다.

## 운영 포인트

production table은 실수로 삭제되거나 장애 상황에서 복구할 수 있어야 한다. 학습용 local table과 production table의 lifecycle은 완전히 다르게 관리한다.

## 확인 질문

- PITR과 backup은 어떤 차이가 있는가?
- throttling alarm은 왜 필요한가?
- application health가 OK여도 DynamoDB 지표를 따로 봐야 하는 이유는 무엇인가?
