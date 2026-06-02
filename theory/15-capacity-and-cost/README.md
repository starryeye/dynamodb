# Capacity and Cost

이 주제는 DynamoDB capacity와 비용 감각만 배우는 단계다.

프로젝트 위치:

```text
theory/15-capacity-and-cost/
```

## 이번 주제에서 배우는 것

- on-demand mode
- provisioned mode
- RCU와 WCU
- item size가 비용에 주는 영향
- GSI와 transaction 비용 감각

## 이번 주제에서 아직 다루지 않는 것

- IAM
- backup
- CloudWatch alarm

## Capacity Mode

DynamoDB table은 크게 두 capacity mode를 가진다.

- on-demand: 요청량에 따라 과금되고 capacity planning 부담이 작다.
- provisioned: 미리 RCU/WCU를 정하고 auto scaling을 함께 고려한다.

입문 학습과 초기 서비스에는 on-demand가 이해하기 쉽다. 예측 가능한 대규모 traffic에서는 provisioned mode를 검토할 수 있다.

## 비용 감각

- item이 커질수록 read/write 비용이 늘어난다.
- strongly consistent read는 eventually consistent read보다 비용이 크다.
- GSI query는 GSI의 read capacity를 사용한다.
- GSI에 반영되는 write도 비용에 영향을 준다.
- transaction은 일반 read/write보다 비용이 커질 수 있다.

## 운영 포인트

학습 프로젝트는 단순함을 위해 on-demand를 기본으로 둔다. production에서는 traffic pattern, peak, budget, alarm 기준을 함께 정한다.

## 확인 질문

- on-demand와 provisioned는 어떤 상황에서 각각 유리한가?
- GSI를 추가하면 write cost가 왜 달라지는가?
- transaction을 남용하면 어떤 비용 문제가 생기는가?
