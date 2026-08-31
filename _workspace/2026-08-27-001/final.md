**두 테이블을 지금 넣는 건, 이 리포 규모에서는 과합니다.** SQL 두 개는 문제가 아닙니다. 그 모델이 끌고 오는 변환·엔티티·시드·조회 로직이 문제입니다.

Slice A에서 이미 같은 판단을 했습니다. 기간·슬롯·한도는 DB가 아니라 `reservation.*` 설정이고 `zone-id` / `isAlignedToSlot`은 “연습 단계에선 duration만”으로 뺐습니다. 운영시간을 방×요일 `TIME` 테이블로 올리는 건 그 결정을 되돌리는 쪽에 가깝습니다.

---

## 실제로 복잡한 지점

테이블 자체는 작습니다. 비용은 여기입니다.

```text
date(로컬) + TIME + 요일 + ZoneId → Instant
Instant 구간 − 예약 − blackout → 슬롯
create 때도 같은 변환을 다시
```

`day_of_week`가 1~7인지, 일요일이 7인지, `close`를 `[)`로 볼지, 자정 넘김을 막을지 — 버그가 여기 모입니다. 예약 gist exclusion보다 학습 난이도가 높고 제품 가치는 더 낮습니다.

방마다·요일마다 시간이 다르다는 가정도 아직 없습니다. 방 몇 개, 관리자 API 없음, 시드는 “월–금 09:00–22:00”을 N방 × 5행으로 복붙하게 됩니다. 그건 정규화가 아니라 중복입니다.

blackout도 지금은 `study_rooms.status = MAINTENANCE`(방 전체 닫힘)로 충분히 가립니다. “오늘은 열고 13–15만 점검”은 관리자가 구간을 넣을 때 의미가 있습니다.

---

## 대안 비교

| 설계 | 무엇을 사나 | 지금 필요한가 |
|------|-------------|---------------|
| **A. 설정만** `open-time` / `close-time` + zone | availability 슬롯, create 시 영업시간 검사 | **예. Slice A와 같은 축** |
| **B. blackout 테이블만** | 특정 구간 폐쇄 | 관리자/예외가 생기기 전엔 아니오 |
| **C. PRD 그대로 2테이블** | 방·요일별 시간 + 예외 | 요구가 없을 때 아니오 |

availability에 테이블이 필수는 아닙니다.

```text
?date=2026-09-01
  → 설정 09:00–22:00을 Asia/Seoul Instant로
  → 그날 CONFIRMED와 겹치면 available: false
```

이게 Slice B의 핵심이고 Flyway 없이도 됩니다. `zone-id`를 다시 넣는 이유도 이때가 맞습니다. “서울 기준 그날 9시”를 계산해야 하니까요. Policy의 10:07 정렬용이 아닙니다.

---

## 도메인으로 보면

막을 이유는 세 종류뿐입니다.

| 종류 | 이미 있는 것 | 테이블이 필요한가 |
|------|----------------|-------------------|
| 규칙 (30분, 4시간, 9–22시) | `ReservationProperties` | 아니오. 설정을 늘리면 됨 |
| 경쟁 (누가 먼저 잡음) | gist exclusion | 이미 있음 |
| 예외 (그날만 공사) | 없음 | **나중에** 한 테이블 |

운영시간과 blackout을 처음부터 쪼개는 건 반복 패턴 vs 예외라는 교과서 모델입니다. 틀린 모델은 아닙니다. 지금 쓸 데이터가 없을 때 올리면 엔티티 2개, 리포지토리 2개, seed, 변환 코드만 생깁니다.

8단계에서 관리자가 방마다 시간을 바꾸고 요일마다 다르고 점검 구간을 넣을 때 C로 올리면 그때는 이득이 있습니다. 지금 올리면 Flyway를 한 번 더 연습하는 대가입니다.

---

## 권장

**지금:** 운영시간 = `application.properties`. availability GET만 붙인다. 테이블 없음.

**다음에 정말 필요할 때:** `room_blackouts` 하나. `start_at`/`end_at`/`reason`. 예약과 같은 `TIMESTAMPTZ`라서 변환이 거의 없습니다.

**방·요일별 시간이 실제 요구가 되면:** 그때 `room_operating_hours`.

PRD의 두 테이블은 작은 운영 서비스를 가정한 최종 스케치입니다. 학습 리포의 다음 한 스텝 기준으로는 이릅니다.

availability를 설정 기반으로 갈지, 그래도 Flyway 연습이 목표면 테이블을 최소 하나(`blackouts`)만 갈지 정하면 됩니다.

<!-- HUMANIZE-SUMMARY v1.6.1
run_id: 2026-08-27-001
route: light (risk_band low, score 2) · intensity: 보수
genre: report
metrics:
  char_in: 2211
  char_out: 2199
  change_rate: ~2.8% (자가 산출 — 확정 판정은 verify_change_rate.py)
  self_check: 6/6
  grade: B
categories:  # before → after
  C-11 연결어미 뒤 쉼표: 6 → 0
  C-8 "A가 아니라 B" 대구: 3 → 2
  J-2 따옴표 강조: 6 → 4
  J-1 본문 중간 볼드 강조: 1 → 0
  D-1~D-3 결산·의의 과장 lexicon: 0 → 0
  A-7·A-8·H-1·I-1: 0 → 0
self_check:
  - 고유명사·수치·인용·내용 앵커 100% 보존: ✅ (Slice A/B, reservation.*, zone-id, isAlignedToSlot, TIME, TIMESTAMPTZ, day_of_week, gist exclusion, ReservationProperties, study_rooms.status, MAINTENANCE, application.properties, room_blackouts, room_operating_hours, Flyway, Instant, ZoneId, Asia/Seoul, CONFIRMED, PRD, open-time, close-time, start_at, end_at, reason, availability, Policy 전량 원형 유지)
  - 변경률 30% 이하: ✅
  - 장르 이탈 없음: ✅ (리포트 유지, 표·코드블록·헤딩 구조 그대로)
  - register 보존: ✅ (합쇼체 '~습니다' 유지, '권장' 절의 '붙인다' 평어 라벨도 원문대로)
  - S1 잔존 0건: ✅
  - 인공 표현 추가 없음: ✅ (신규 어휘·비유·상투구 삽입 0, 원문 대시 1개 보존)
highlights:
  - id: C-11 + C-8
    before: "SQL 두 개가 문제가 아니라, 그 모델이 끌고 오는 변환·엔티티·시드·조회 로직이 문제입니다."
    after: "SQL 두 개는 문제가 아닙니다. 그 모델이 끌고 오는 변환·엔티티·시드·조회 로직이 문제입니다."
  - id: C-11
    before: "예약 gist exclusion보다 학습 난이도가 높고, 제품 가치는 더 낮습니다."
    after: "예약 gist exclusion보다 학습 난이도가 높고 제품 가치는 더 낮습니다."
  - id: C-11
    before: "관리자가 방마다 시간을 바꾸고, 요일마다 다르고, 점검 구간을 넣을 때"
    after: "관리자가 방마다 시간을 바꾸고 요일마다 다르고 점검 구간을 넣을 때"
  - id: J-1
    before: "**지금 쓸 데이터가 없을 때 올리면** 엔티티 2개, 리포지토리 2개, seed, 변환 코드만 생깁니다."
    after: "지금 쓸 데이터가 없을 때 올리면 엔티티 2개, 리포지토리 2개, seed, 변환 코드만 생깁니다."
  - id: J-2
    before: "“반복 패턴 vs 예외”라는 교과서 모델입니다 / “작은 운영 서비스”를 가정한 최종 스케치"
    after: "반복 패턴 vs 예외라는 교과서 모델입니다 / 작은 운영 서비스를 가정한 최종 스케치"
residual_findings:
  - id: C-8
    severity: S2
    reason: "'DB가 아니라 설정' · '정규화가 아니라 중복' 2건 잔존. 대조가 주장의 뼈대라 제거 시 의미 손실 — 보수 모드에서 의도적 보존."
  - id: J-2
    severity: S2
    reason: "따옴표 4건 잔존. 시드 값·발화 인용(“월–금 09:00–22:00”, “오늘은 열고 13–15만 점검”, “연습 단계에선 duration만”, “서울 기준 그날 9시”)이라 Do-NOT 대상."
  - id: lexical_diversity z=+1.87
    severity: info
    reason: "quick-rules에 대응 처방 없음. 근거 기반 원칙에 따라 미조치."
notes:
  - "표 3개·펜스 코드블록 2개·헤딩 5개 원형 유지. 표를 산문으로 풀지 않음."
  - "'권장' 절의 라벨형 볼드(**지금:** 등)와 표 내부 볼드는 구조 표지라 보존."
grade_reason: "B — S1 잔존 0, 자체검증 6/6, S2 잔존 2계열. 변경률 2.8%로 A 대역(10~25%) 미달이나, 입력이 risk_band low·light 경로여서 대역 충족을 위한 추가 손질은 과윤문에 해당하므로 의도적으로 억제."
-->
