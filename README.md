# KPSC DLPC

KPSC DLPC (Deep Learning & Pwnable Container)는 KPSC(Kookmin Problem Solved Club) 소속원을 위한 웹 기반 VM 관리 시스템입니다.

사용자는 Docker 컨테이너 기반의 개인 전용 VM을 신청하여 사용할 수 있으며, 관리자는 웹 인터페이스를 통해 이를 승인, 생성, 실행, 관리할 수 있습니다.

## 🛠️ 주요 기술 스택

- **Spring Boot** - 백엔드 프레임워크
- **MySQL** - 데이터베이스
- **Docker** - 컨테이너 기반 VM 관리
- **JSON Web Token** - 인증 및 보안
- **Thymeleaf** - 템플릿 엔진

## 📌 개발 목적

### 1. 워게임 실습 환경 제공
워게임 스터디원들이 모의해킹 및 보안 실습을 수행할 수 있는 독립적인 VM 환경 제공

### 2. GPU 자원 공유
딥러닝을 공부하는 학우들이 GPU 기반 컨테이너 환경을 사용할 수 있도록 지원

### 3. 중앙 관리 시스템 구축
사용자 인증, 컨테이너 상태 관리, 리소스 분배를 웹 인터페이스로 통합 관리

## 🔧 주요 기능

### 사용자 계정 요청
사용자는 다음 정보를 입력하여 계정 요청을 할 수 있습니다.

- 이름
- 학번
- 학과
- 전화번호

관리자는 요청을 승인하거나 거절할 수 있습니다.

**관련 페이지:** `/register`, `/admin/requests`

### API Key 기반 VM 접속
승인된 사용자에게는 고유한 API Key가 발급되며 해당 Key를 통해 개인 VM에 접속할 수 있습니다.

**관련 페이지:** `/vm`

### Docker 기반 VM 관리
관리자는 다음 작업을 수행할 수 있습니다.

- 사용자 VM 생성
- 컨테이너 실행
- 컨테이너 중지
- 컨테이너 삭제
- 컨테이너 로그 확인

**관련 페이지:** `/admin/vmlogs`

### 관리자 페이지
관리자는 웹 인터페이스에서 다음 항목을 관리할 수 있습니다.

- 사용자 승인 요청
- 사용자 목록
- VM 상태 확인

**관련 페이지:** `/admin`, `/admin/users`

### JWT 기반 인증
사용자 인증 및 API 보호를 위해 JWT(JSON Web Token) 기반 인증 시스템을 사용합니다.

## 📁 프로젝트 구조

```
kpsc_wargame/
├─ Dockerfile
├─ docker-compose.yml
├─ .env
│
├─ src/
│  ├─ main/
│  │  ├─ java/com/shaorn77770/kpsc_wargame/   # Spring Boot Backend
│  │  └─ resources/
│  │       ├─ templates/                      # Thymeleaf Templates
│  │       └─ application.yml                 # Spring Configuration
│
├─ build.gradle
└─ README.md
```
## ⚙️ 환경 설정

본 프로젝트는 환경 변수 기반 설정을 사용합니다. `application.yml`은 Docker 환경 변수를 참조합니다.

```yaml
server:
  port: 8000
  address: 0.0.0.0

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

admin:
  username: ${ADMIN_USERNAME}
  password: ${ADMIN_PASSWORD}

jwt:
  jwtkey: ${JWT_KEY}

domain:
  domain: ${DOMAIN_NAME}
```

### 🔑 환경 변수 (.env)

프로젝트 루트에 `.env` 파일을 생성합니다.

```env
DB_NAME=kpsc_db
DB_USER=kpsc_user
DB_PASSWORD=secure_password
DB_ROOT_PASSWORD=root_password

ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin_password

JWT_KEY=super_secret_jwt_key

DOMAIN_NAME=example.com
```
## 🐳 Docker 기반 실행 방법 (권장)

이 프로젝트는 docker-compose 기반 배포를 지원합니다.

### 1. 프로젝트 클론

```bash
git clone https://github.com/your-repo/kpsc_dlpc.git
cd kpsc_dlpc
```

### 2. 환경 변수 설정

`.env` 파일 생성 후 환경 변수 입력

```bash
nano .env
```

### 3. 서비스 실행

```bash
docker compose up -d --build
```

실행 후 다음 컨테이너가 생성됩니다.

- `kpsc-service`
- `kpsc-mysql`

### 4. 웹 접속

```
http://localhost:8000
```
## 🖥️ 컨테이너 구조

서비스 실행 후 Docker 구조는 다음과 같습니다.

```
Docker Engine
 ├─ kpsc-service (Spring Boot)
 ├─ kpsc-mysql
 ├─ jupyter_<student_number>
 ├─ jupyter_<student_number>
 └─ ...
```

Spring 서버는 Docker API를 통해 사용자별 Jupyter VM 컨테이너를 생성합니다.

## 🌐 주요 페이지 URL

| 경로 | 설명 |
|------|------|
| `/register` | 사용자 계정 등록 |
| `/admin` | 관리자 대시보드 |
| `/admin/requests` | 사용자 승인 요청 |
| `/admin/users` | 사용자 목록 |
| `/admin/vmlogs` | 사용자 VM 관리 |
| `/vm` | API Key 기반 VM 접속 |
## 🔒 보안 권장 사항

다음 항목은 반드시 보호되어야 합니다.

- DB 비밀번호
- 관리자 계정
- JWT Secret Key

### 권장 방법

- `.env` 파일 사용
- `application-prod.yml` 분리
- Git 저장소에 `.env` 제외 (`.gitignore`)

## 🧑‍💻 기여

본 프로젝트는 KPSC 내부 스터디 환경을 위해 제작되었습니다.

기능 제안 또는 버그 리포트는 [GitHub Issue](https://github.com/Sharon77770/kpsc_dlpc/issues)를 통해 제출해주세요.

## 📜 라이선스

© 2025 Sharon77770

본 소프트웨어는 KPSC 내부 사용을 위해 제작되었습니다.
외부 배포 및 상업적 사용 시 별도 협의가 필요합니다.