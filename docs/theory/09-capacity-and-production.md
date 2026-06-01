# Capacity와 Production

DynamoDB를 production에서 쓰려면 key design뿐 아니라 capacity, 비용, retry, IAM, backup도 함께 봐야 한다.

## Capacity Mode

DynamoDB table은 크게 두 capacity mode를 가진다.

- on-demand: 요청량에 따라 과금되고 capacity planning 부담이 작다.
- provisioned: 미리 RCU/WCU를 정하고 auto scaling을 함께 고려한다.

입문 학습과 초기 서비스에는 on-demand가 이해하기 쉽다. 예측 가능한 대규모 traffic에서는 provisioned mode를 검토할 수 있다.

## RCU와 WCU

Read capacity와 write capacity는 item size와 consistency에 영향을 받는다.

학습할 때는 다음 감각이 중요하다.

- item이 커질수록 read/write 비용이 늘어난다.
- strongly consistent read는 eventually consistent read보다 비용이 크다.
- GSI query는 GSI의 read capacity를 사용한다.
- GSI에 반영되는 write도 비용에 영향을 준다.
- transaction은 일반 read/write보다 비용이 커질 수 있다.

## Hot Partition

Partition key 값 하나에 traffic이 몰리면 hot partition이 될 수 있다.

이 프로젝트에서는 `ownerId`가 partition key다. 특정 owner가 매우 많은 task를 만들거나 아주 높은 traffic을 만들면 병목이 생길 수 있다.

학습 프로젝트에서는 단순함을 위해 이 설계를 사용한다. production에서는 traffic 분포를 보고 write sharding, key 재설계, index 재설계를 검토한다.

## Retry와 Throttling

DynamoDB request는 throttling이나 transient error로 실패할 수 있다.

AWS SDK의 retry 설정을 이해하고, application에서는 idempotency가 필요한 write path를 구분한다.

특히 transaction retry는 같은 client request token을 사용할지, 새 요청으로 볼지 명확히 해야 한다.

## IAM

Production에서는 access key를 코드나 설정 파일에 하드코딩하지 않는다.

권장:

- IAM role 또는 default credential provider 사용
- table과 index ARN에 대한 least privilege permission
- local profile에서만 dummy credential 사용

## 운영 보호 장치

Production table에는 다음을 고려한다.

- PITR
- backup
- deletion protection
- CloudWatch alarm
- latency, throttling, consumed capacity, error rate monitoring
- application startup에서 table 생성 금지
- IaC로 table과 index 생성

## 확인 질문

- GSI를 추가하면 read path와 write cost가 어떻게 달라지는가?
- 이 프로젝트의 `ownerId` partition key는 어떤 traffic에서 위험해질 수 있는가?
- local profile과 prod profile의 credential 설정은 어떻게 달라야 하는가?

