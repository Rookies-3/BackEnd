# 외부 환경에서 실행하기 위한 가이드

## 필요한 파일들

### 1. JAR 파일
- `build/libs/RookiesOneteam-0.0.1-SNAPSHOT.jar` - Spring Boot 실행 가능한 JAR 파일

### 2. 설정 파일 (선택사항 - 외부 설정을 위해)
다음 구조로 배포 디렉토리를 만드세요:

```
deployment/
├── RookiesOneteam-0.0.1-SNAPSHOT.jar
├── application.properties
└── .env
```

### 3. 환경 변수 파일 (.env)
프로젝트 루트의 .env 파일을 복사:
```
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
DB_HOST=localhost (또는 데이터베이스 호스트)
DB_PORT=3306
DB_NAME=rookies_db
DB_USERNAME=root
DB_PASSWORD=your-password
```

## 실행 방법

### 방법 1: 내장된 설정 사용 (개발환경)
```bash
java -jar RookiesOneteam-0.0.1-SNAPSHOT.jar
```

### 방법 2: 외부 설정 파일로 실행 (운영환경)
```bash
java -jar RookiesOneteam-0.0.1-SNAPSHOT.jar --spring.config.location=file:./application.properties
```

### 방법 3: 환경 변수로 실행
```bash
# Linux/Mac
export GOOGLE_CLIENT_ID=your-id
export GOOGLE_CLIENT_SECRET=your-secret
java -jar RookiesOneteam-0.0.1-SNAPSHOT.jar

# Windows
set GOOGLE_CLIENT_ID=your-id
set GOOGLE_CLIENT_SECRET=your-secret
java -jar RookiesOneteam-0.0.1-SNAPSHOT.jar
```

### 방법 4: 환경별 프로파일 사용
```bash
java -jar RookiesOneteam-0.0.1-SNAPSHOT.jar --spring.profiles.active=docker
```

## 필요한 시스템 요구사항

1. **Java 17** - JRE 또는 JDK 설치 필요
2. **MariaDB** - 데이터베이스 서버가 실행 중이어야 함
3. **포트 8080** - 기본적으로 8080 포트를 사용 (설정으로 변경 가능)

## 배포 체크리스트

- [ ] JAR 파일 복사
- [ ] application.properties 수정 (데이터베이스 연결 정보)
- [ ] .env 파일 설정 (OAuth 정보)
- [ ] Java 17 설치 확인
- [ ] MariaDB 데이터베이스 생성 및 연결 확인
- [ ] 방화벽 설정 확인 (8080 포트)
- [ ] 실행 테스트

## JAR 파일만으로도 실행 가능

JAR 파일 하나만으로도 실행 가능합니다. JAR 내부에 이미 application.properties가 포함되어 있습니다.
하지만 외부 설정을 오버라이드하려면 위의 파일들이 필요합니다.

