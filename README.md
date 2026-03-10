# KPSC DLPC

**KPSC DLPC**는 KPSC(Kookmin Problem Solved Club) 소속원을 위한 웹 기반 VM 관리 시스템입니다.  
사용자는 도커 컨테이너 기반의 개인 전용 VM을 신청하고 사용할 수 있으며, 관리자는 이를 승인 및 제어할 수 있습니다.  
이 프로젝트는 **Spring Boot**, **MySQL**, **Docker**, **JWT 인증**, **GPU 지원** 기반으로 개발되었습니다.

---

## 📋 선행 요구사항

- **Docker** 및 **Docker Compose** 설치
- **NVIDIA Docker Runtime** (GPU 사용 시)
- **NVIDIA CUDA Toolkit** 설치 (GPU 사용 시)
- 최소 2GB RAM, 5GB 여유 저장공간

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

## ⚙️ 환경 설정 (`.env` 파일)

`.env` 파일에서 다음 환경 변수를 설정합니다:

```ini
# 데이터베이스 설정
DB_NAME=kpsc_db
DB_USER=kpsc_user
DB_PASSWORD=super_secure_pw
DB_ROOT_PASSWORD=root_pw
DB_HOST=mysql
DB_PORT=3306

# 관리자 계정
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin_pw

# JWT 설정
JWT_KEY=super_secret_jwt_key

# 도메인
DOMAIN_NAME=example.com
```

🔒 **보안 권장 사항**  
- `.env` 파일에는 실제 보안 정보를 입력하고 `.gitignore`에 추가하여 버전 관리에서 제외하세요.
- 프로덕션 환경에서는 보다 강력한 비밀번호를 사용하세요.
- `JWT_KEY`는 최소 32자 이상의 안전한 키로 설정하세요.

---

## ▶️ 실행 방법 (Docker Compose)

### 빠른 시작

1. **`.env` 파일 설정**
   ```bash
   # 프로젝트 루트 디렉토리에 .env 파일이 있는지 확인
   cat .env
   ```
   필요시 `.env` 파일을 수정하여 환경 변수를 조정합니다.

2. **Docker 컴포즈로 빌드 및 실행**
   ```bash
   docker-compose up --build
   ```

3. **웹 접속**
   ```
   http://localhost:8000
   ```

4. **컨테이너 중지**
   ```bash
   docker-compose down
   ```

### GPU 지원 (선택사항)

GPU를 사용하려면 다음 설정이 필요합니다:

- **Linux 사용자:**
  ```bash
  # NVIDIA Container Runtime 설치 확인
  docker run --rm --gpus all nvidia/cuda:12.0-runtime-ubuntu22.04 nvidia-smi
  ```

- **Windows 사용자:**
  - WSL2에서 NVIDIA CUDA Toolkit 설치
  - Docker Desktop의 GPU 지원 활성화

`docker-compose.yml`의 app 서비스에 이미 GPU 할당이 설정되어 있습니다:
```yaml
deploy:
  resources:
    reservations:
      devices:
        - driver: nvidia
          count: all
          capabilities: [gpu]
```

### 로컬 개발 환경 (선택)

Docker 없이 로컬에서 실행하려면:

```bash
# MySQL 별도 실행 필요
mysql -u root -p < init.sql

# Spring Boot 애플리케이션 실행
./gradlew bootRun
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
