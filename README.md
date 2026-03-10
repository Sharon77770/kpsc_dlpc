# KPSC DLPC

**KPSC DLPC**는 KPSC(Kookmin Problem Solved Club) 소속원을 위한 웹 기반 VM 관리 시스템입니다.  
사용자는 도커 컨테이너 기반의 개인 전용 VM을 신청하고 사용할 수 있으며, 관리자는 이를 승인 및 제어할 수 있습니다.  
이 프로젝트는 **Spring Boot**, **MySQL**, **Docker**, **JWT 인증** 기반으로 개발되었습니다.

---

## 📌 개발 목적

- **워게임 스터디원들의 모의해킹 실습**을 위한 안전하고 독립적인 VM 환경 제공  
- **인공지능을 공부하는 학우들을 위한 GPU 자원 분배 및 사용 관리**  
- 사용자 인증, 컨테이너 상태 관리, 리소스 할당을 효율적으로 처리할 수 있는 시스템 구현

---

## 🔧 주요 기능

- **사용자 등록 및 승인 요청**  
  사용자가 이름, 학번, 학과, 전화번호를 입력하여 계정 요청을 할 수 있습니다. 관리자는 요청을 승인 또는 거절할 수 있습니다.  
  → `/register`, `/admin/requests`

- **API Key 발급**  
  승인된 사용자에게는 고유한 API Key가 발급되며, 이를 통해 개인 VM에 접속할 수 있습니다.  
  → `/vm`

- **도커 기반 VM 관리**  
  관리자는 각 사용자에 대해 도커 컨테이너를 생성, 실행, 중지, 로그 확인 및 삭제할 수 있습니다.  
  → `/admin/vmlogs`

- **관리자 페이지**  
  계정 요청, 사용자 목록, VM 상태를 직관적으로 관리할 수 있는 웹 인터페이스를 제공합니다.  
  → `/admin`, `/admin/users`

- **JWT 기반 인증**  
  사용자 인증 및 API 보호를 위해 JWT(JSON Web Token)를 사용합니다.

---

## 📁 프로젝트 구조

```
kpsc_wargame/
└─ kpsc_war_game_study_vm/
    └─ kpsc_wargame/
        ├─ src/
        │   ├─ main/
        │   │   ├─ java/com/shaorn77770/kpsc_wargame/   # 백엔드 Java 소스
        │   │   └─ resources/
        │   │        ├─ templates/                      # Thymeleaf 템플릿
        │   │        └─ application.yml                 # 설정 파일
        ├─ build/
        └─ ...
```

---

## ⚙️ 환경 설정 (`application.yml`)

```yaml
server:
  port: 8000
  address: 0.0.0.0

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/database_name
    username: db_id
    password: db_pw

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format-sql: true

  application:
    name: KPSC-DLPC

  devtools:
    restart:
      enabled: true

admin:
  username: admin_id
  password: admin_pw

jwt:
  jwtkey: your_jwt_key

domain:
  domain: your_domain
```

🔒 **보안 권장 사항**  
- 민감한 정보(`db_pw`, `jwtkey`, `admin_pw`)는 환경 변수로 추출하거나 `.env`, `application-prod.yml`로 분리하여 관리하세요.

---

## ▶️ 실행 방법

1. **의존성 설치 및 빌드**
   ```bash
   ./gradlew build
   ```

2. **MySQL DB 준비**
   - `database_name` 생성
   - 사용자 계정 및 권한 부여
   - `application.yml`에 DB 정보 입력

3. **서버 실행**
   ```bash
   ./gradlew bootRun
   ```
   또는 IDE에서 `KpscWargameApplication` 실행

4. **웹 접속**
   ```
   http://localhost:8000
   ```

---

## 🌐 주요 페이지 URL

| 경로 | 설명 |
|------|------|
| `/register` | 사용자 계정 등록 페이지 |
| `/admin` | 관리자 대시보드 |
| `/admin/requests` | 사용자 승인 요청 관리 |
| `/admin/users` | 사용자 목록 및 관리 |
| `/admin/vmlogs` | 사용자 VM 상태 관리 |
| `/vm` | API Key 기반 VM 접속 |

---

## 🧑‍💻 기여

본 프로젝트는 **KPSC 내부 소속원 전용**으로 제작되었습니다.  
기능 제안, 버그 리포트 등은 GitHub Issue를 통해 남겨주세요.

---

## 📜 라이선스

© 2025 [Sharon77770](https://github.com/Sharon77770)  
본 소프트웨어는 KPSC 내 사용을 위해 제작되었으며, 외부 배포 시 라이선스 협의가 필요합니다.
