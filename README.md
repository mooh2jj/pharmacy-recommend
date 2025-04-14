# 약국 추천 서비스 개발문서

주소를 입력시, 10km 이내 가장 가까운 약국 최대 3개를 추천해주는 서비스입니다. 😊
![image](https://github.com/user-attachments/assets/2e0e5e67-11d9-4c20-96da-15150b3ae635)

<br>

## 목차
1. [프로젝트 개요](#프로젝트-개요)
2. [백엔드](#백엔드)
   - [구조](#백엔드-구조)
   - [API 명세](#api-명세)
   - [주요 기능](#백엔드-주요-기능)
   - [설정 가이드](#백엔드-설정-가이드)
3. [프론트엔드](#프론트엔드)
   - [구조](#프론트엔드-구조)
   - [컴포넌트](#컴포넌트)
   - [API 연동](#api-연동)
   - [설정 가이드](#프론트엔드-설정-가이드)
4. [기술 스택](#기술-스택)
5. [배포 가이드](#배포-가이드)
6. [문제 해결](#문제-해결)

<br>

## 프로젝트 개요

약국 추천 서비스는 사용자가 입력한 주소 기반으로 가까운 약국을 추천해주는 웹 애플리케이션입니다. 백엔드는 Spring Boot를 사용하여 개발되었으며, 프론트엔드는 Next.js를 사용하여 개발되었습니다.

<br>

## 백엔드

### 백엔드 구조

백엔드는 다음과 같은 주요 패키지로 구성되어 있습니다:

```
src/main/java/com/dsg/pharmacyrecommend/
├── config/                # 설정 클래스
│   ├── RedisConfig.java   # Redis 설정
│   ├── WebConfig.java     # CORS 설정
│   └── ...
├── direction/             # 약국 방향 관련 기능
│   ├── controller/        # API 엔드포인트
│   ├── dto/               # 데이터 전송 객체
│   ├── service/           # 비즈니스 로직
│   └── entity/            # 데이터 모델
├── pharmacy/              # 약국 관련 기능
│   ├── service/           # 약국 추천 서비스
│   ├── entity/            # 약국 엔티티
│   └── ...
└── ...
```

### API 명세

#### 약국 추천 API

**엔드포인트**: `/api/direction/search`
**메소드**: POST
**설명**: 주소를 기반으로 가까운 약국을 추천합니다.

**요청 예시**:
```json
{
  "address": "경기 성남시 분당구 백현동 555"
}
```

**응답 예시**:
```json
[
  {
    "pharmacyName": "바우약국",
    "pharmacyAddress": "경기 성남시 분당구 백현동 552",
    "directionUrl": "http://localhost:8085/dir/t",
    "roadViewUrl": "https://map.kakao.com/link/roadview/37.3882788387018,127.114570230861",
    "distance": "0.55 km"
  },
  {
    "pharmacyName": "옵티미희망약국",
    "pharmacyAddress": "경기 성남시 분당구 백현동 546",
    "directionUrl": "http://localhost:8085/dir/u",
    "roadViewUrl": "https://map.kakao.com/link/roadview/37.38755457303961,127.11625616026306",
    "distance": "0.55 km"
  }
]
```

### 백엔드 주요 기능

1. **약국 정보 관리**
   - 약국 정보를 데이터베이스에 저장하고 관리합니다.

2. **거리 계산**
  - 입력 받은 주소를 위도, 경도로 변환 하여 기존 약국 데이터와 비교 및 가까운 약국을 찾는다.   
  - 지구는 평면이 아니기 때문에, 구면에서 두 점 사이의 최단 거리 구하는 공식이 필요    
  - 두 위 경도 좌표 사이의 거리를 [haversine formula](https://en.wikipedia.org/wiki/Haversine_formula)로 계산  
  - 지구가 완전한 구형이 아니 므로 아주 조금의 오차가 있다.   

3. **길찾기 및 로드뷰 URL 생성**
   - 카카오맵 등의 지도 서비스와 연동하여 길찾기 및 로드뷰 URL을 생성합니다.

4. **캐싱**
   - Redis를 사용하여 자주 요청되는 데이터를 캐싱합니다.

### 약국 추천 프로세스
<img width="615" alt="스크린샷 2022-07-07 오후 1 58 39" src="https://user-images.githubusercontent.com/26623547/177694773-b53d1251-652f-41e6-8f19-c32b931d4b5b.png">

### 백엔드 설정 가이드

1. **환경 설정**
   - Java 17 이상이 설치되어 있어야 합니다.
   - Gradle을 사용하여 의존성을 관리합니다.

2. **데이터베이스 설정**
   - `application.properties` 또는 `application.yml`에 데이터베이스 연결 정보를 설정합니다.

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/pharmacy
       username: root
       password: password
       driver-class-name: com.mysql.cj.jdbc.Driver
   ```

3. **Redis 설정**
   - Redis 서버 연결 정보를 설정합니다.

   ```yaml
   spring:
     redis:
       host: localhost
       port: 6379
   ```

4. **CORS 설정**
   - 프론트엔드와의 통신을 위한 CORS 설정을 `WebConfig.java`에 정의합니다.

   ```java
   @Configuration
   public class WebConfig implements WebMvcConfigurer {
       @Override
       public void addCorsMappings(CorsRegistry registry) {
           registry.addMapping("/**")
                   .allowedOrigins("http://localhost:3000")
                   .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                   .allowedHeaders("*")
                   .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                   .maxAge(3600)
                   .allowCredentials(true);
       }
   }
   ```

5. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```
6. **애플리케이션 테스트 실행**
   ```bash
   ./gradlew clean test
   ```
<br>

## 프론트엔드

### 프론트엔드 구조

프론트엔드는 Next.js를 기반으로 하며 다음과 같은 구조로 구성되어 있습니다:

```
frontend/
├── app/                   # Next.js 페이지
│   ├── page.tsx           # 메인 페이지
│   ├── layout.tsx         # 레이아웃
│   └── globals.css        # 전역 스타일
├── components/            # 컴포넌트
│   ├── address-search.tsx # 주소 검색 컴포넌트
│   ├── pharmacy-card.tsx  # 약국 카드 컴포넌트
│   ├── header.tsx         # 헤더 컴포넌트
│   ├── ui/                # UI 컴포넌트
│   │   ├── button.tsx     # 버튼 컴포넌트
│   │   ├── card.tsx       # 카드 컴포넌트
│   │   └── ...
│   └── ...
├── lib/                   # 유틸리티 및 API
│   ├── api.ts             # API 요청 함수
│   └── utils.ts           # 유틸리티 함수
├── public/                # 정적 파일
├── next.config.ts         # Next.js 설정
└── ...
```

### 컴포넌트

1. **주소 검색 컴포넌트 (AddressSearch)**
   - 다음(카카오) 주소 검색 API를 활용하여 주소 검색 기능을 제공합니다.
   - 입력한 주소를 기반으로 약국 검색을 요청합니다.

2. **약국 카드 컴포넌트 (PharmacyCard)**
   - 약국 정보를 카드 형태로 표시합니다.
   - 약국 이름, 주소, 거리 정보를 제공합니다.
   - 길찾기와 로드뷰 버튼을 제공합니다.

3. **헤더 컴포넌트 (Header)**
   - 사이트 로고와 네비게이션을 포함합니다.
   - 테마 토글 버튼을 제공합니다.

4. **UI 컴포넌트**
   - shadcn/ui 라이브러리를 기반으로 한 재사용 가능한 UI 컴포넌트들을 포함합니다.
   - 버튼, 카드, 배지, 스켈레톤 등의 컴포넌트가 있습니다.

### API 연동

프론트엔드에서 백엔드 API를 호출하는 방법은 다음과 같습니다:

```typescript
// lib/api.ts
// lib/api.ts
import axios, { AxiosError } from "axios";

export interface PharmacyDirection {
  pharmacyName: string;
  pharmacyAddress: string;
  directionUrl: string;
  roadViewUrl: string;
  distance: string;
}

// Next.js API 리디렉션을 사용하므로 상대 경로로 변경
const API_BASE_URL = "";

// axios 인스턴스 생성 및 기본 설정
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: false, // 상대 경로 사용으로 withCredentials 불필요
});

// 약국 길찾기 URL을 가져오는 함수
export async function getPharmacyDirection(
  encodedId: string
): Promise<PharmacyDirection | undefined> {
  try {
    const response = await api.get(`/api/direction/${encodedId}`);
    return response.data;
  } catch (error: unknown) {
    console.error("길찾기 URL을 가져오는 중 오류 발생:", error);
    return undefined;
  }
}

// 약국 검색 함수
export async function searchPharmaciesByAddress(
  address: string
): Promise<PharmacyDirection[]> {
  try {
    console.log("약국 검색 요청:", address);
    const response = await api.post("/api/direction/search", {
      address,
    });
    console.log("약국 검색 응답:", response);
    return response.data;
  } catch (error: unknown) {
    console.error("약국 조회 오류:", error);
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError;
      if (axiosError.response) {
        // 서버 응답이 있는 경우 (2xx 외의 상태 코드)
        console.error("응답 상태:", axiosError.response.status);
        console.error("응답 데이터:", axiosError.response.data);
      } else if (axiosError.request) {
        // 응답을 받지 못한 경우
        console.error("요청은 전송됐으나 응답 없음:", axiosError.request);
      } else {
        // 요청 설정 중 오류 발생
        console.error("요청 설정 오류:", axiosError.message);
      }
    }
    return [];
  }
}

```

### 프론트엔드 설정 가이드

1. **환경 설정**
   - Node.js 14 이상이 설치되어 있어야 합니다.
   - npm 또는 yarn을 사용하여 의존성을 관리합니다.

2. **의존성 설치**
   ```bash
   cd frontend
   npm install
   # 또는
   yarn install
   ```

3. **개발 서버 실행**
   ```bash
   npm run dev
   # 또는
   yarn dev
   ```

4. **CORS 이슈 해결을 위한 Next.js 설정**
   - `next.config.ts` 파일에 API 리디렉션 설정을 추가합니다.

   ```typescript
   import type { NextConfig } from "next";

   const nextConfig: NextConfig = {
     async rewrites() {
       return [
         {
           source: '/api/:path*',
           destination: 'http://localhost:8085/api/:path*',
         },
       ];
     },
   };

   export default nextConfig;
   ```

5. **배포용 빌드**
   ```bash
   npm run build
   # 또는
   yarn build
   ```

<br>

## 기술 스택

### 백엔드
- Java 17+
- Spring Boot
- Spring Data JPA
- Redis (캐싱)
- Gradle
- Lombok
- mariaDB
- docker

### 프론트엔드
- TypeScript
- Next.js
- Tailwind CSS
- shadcn/ui
- Axios

<br>

## 배포 가이드

### 백엔드 배포

1. **JAR 파일 생성**
   ```bash
   ./gradlew clean build
   ```

2. **JAR 파일 실행**
   ```bash
   java -jar build/libs/pharmacy-recommend-0.0.1-SNAPSHOT.jar
   ```

### 프론트엔드 배포

1. **정적 빌드 생성**
   ```bash
   cd frontend
   npm run build
   # 또는
   yarn build
   ```

2. **Next.js 애플리케이션 실행**
   ```bash
   npm start
   # 또는
   yarn start
   ```
<br>

## 문제 해결

### CORS 이슈

**문제**: 프론트엔드에서 백엔드 API 호출 시 CORS 오류 발생

**해결방법**:
1. 백엔드에 CORS 설정 추가:
   ```java
   @Configuration
   public class WebConfig implements WebMvcConfigurer {
       @Override
       public void addCorsMappings(CorsRegistry registry) {
           registry.addMapping("/**")
                   .allowedOrigins("http://localhost:3000")
                   .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                   .allowedHeaders("*")
                   .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                   .maxAge(3600)
                   .allowCredentials(true);
       }
   }
   ```

2. 또는 Next.js에서 API 리디렉션 사용:
   ```typescript
   // next.config.ts
   async rewrites() {
     return [
       {
         source: '/api/:path*',
         destination: 'http://localhost:8085/api/:path*',
       },
     ];
   }
   ```

### 405 Method Not Allowed 오류

**문제**: API 호출 시 405 Method Not Allowed 오류 발생

**해결방법**:
1. 프론트엔드에서 적절한 HTTP 메소드 사용 확인 (GET 대신 POST 등)
2. 백엔드 컨트롤러에 `@RequestBody` 애노테이션 추가 확인
   ```java
   @PostMapping("/search")
   public ResponseEntity<List<OutputDto>> searchPharmacy(@RequestBody InputDto inputDto) {
       // ...
   }
   ```

### 모듈 불러오기 오류

**문제**: UI 컴포넌트 모듈을 찾을 수 없음

**해결방법**:
1. 필요한 UI 컴포넌트 생성 (예: Badge)
2. `@/` 경로가 올바르게 설정되어 있는지 확인
3. 타입 선언 파일이 올바르게 생성되어 있는지 확인

이 문서는 약국 추천 서비스를 개발하고 배포하는 방법에 대한 기본 가이드입니다. 프로젝트의 특성에 따라 추가적인 설정이나 기능이 필요할 수 있습니다.
