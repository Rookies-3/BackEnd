# Docker 실행 가이드

## 사전 요구사항

1. Docker Desktop 설치 및 실행
2. Docker Hub 계정 (선택사항 - 이미지 푸시 시)
3. `.env` 파일 설정

## 로컬에서 Docker 실행

### 1. 환경 변수 설정

`.env` 파일을 프로젝트 루트에 생성하고 다음 내용을 입력:

```env
# Google OAuth Configuration
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Database Configuration
DB_PASSWORD=your-password
```

### 2. JAR 파일 빌드

```bash
cd rookies-oneteam
./gradlew clean build
```

또는 Windows:
```bash
cd rookies-oneteam
.\gradlew.bat clean build
```

### 3. Docker 이미지 빌드

```bash
docker build -t rookies-3/rookies-oneteam:latest .
```

### 4. Docker Compose로 실행

```bash
docker-compose up -d
```

### 5. 로그 확인

```bash
docker-compose logs -f app
```

### 6. 중지

```bash
docker-compose down
```

## Docker Hub에 이미지 푸시

### 1. Docker Hub 로그인

```bash
docker login
```

### 2. 이미지 태그 지정

```bash
docker tag rookies-3/rookies-oneteam:latest your-username/rookies-oneteam:latest
```

### 3. 이미지 푸시

```bash
docker push your-username/rookies-oneteam:latest
```

## GitHub Actions를 통한 자동 배포

GitHub Actions가 자동으로:
1. 코드를 빌드
2. JAR 파일 생성
3. Docker 이미지 빌드
4. Docker Hub에 푸시

### Secrets 설정

GitHub 저장소의 Settings > Secrets and variables > Actions에서 다음 secrets를 추가:

- `DOCKER_USERNAME`: Docker Hub 사용자명
- `DOCKER_PASSWORD`: Docker Hub 비밀번호

## 외부 서버에서 실행

### 1. Docker Hub에서 이미지 pull

```bash
docker pull your-username/rookies-oneteam:latest
```

### 2. docker-compose.yml 파일 생성

```yaml
version: '3.8'

services:
  app:
    image: your-username/rookies-oneteam:latest
    container_name: rookies-oneteam-app
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=db
      - DB_PORT=3306
      - DB_NAME=rookies_db
      - DB_USERNAME=root
      - DB_PASSWORD=${DB_PASSWORD}
      - GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
      - GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
    depends_on:
      - db
    restart: unless-stopped

  db:
    image: mariadb:10.11
    container_name: rookies-oneteam-db
    environment:
      - MYSQL_ROOT_PASSWORD=${DB_PASSWORD}
      - MYSQL_DATABASE=rookies_db
    volumes:
      - mariadb-data:/var/lib/mysql
    restart: unless-stopped

volumes:
  mariadb-data:
```

### 3. 실행

```bash
docker-compose up -d
```

## 포트

- 애플리케이션: 8080
- MariaDB: 3306

## 접속 URL

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

