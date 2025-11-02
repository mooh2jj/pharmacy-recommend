package com.dsg.pharmacyrecommend.domain.direction.service;

import io.seruco.encoding.base62.Base62;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Base62 인코딩/디코딩을 담당하는 서비스 클래스
 * 
 * 이 클래스는 Long 타입의 Direction ID를 짧고 안전한 문자열로 변환하여
 * URL에 사용할 수 있는 단축 링크를 생성하는 기능을 제공합니다.
 * 
 * Base62 선택 이유:
 * - Base64 대비 URL 안전성: '=', '+', '/' 등 URL 예약 문자 제외
 * - 읽기 편함: 혼동하기 쉬운 문자 '0', 'O', 'l', 'I' 등을 구분 가능
 * - 짧은 길이: Base10 대비 더 짧은 문자열 생성
 * - 대소문자 구분: 더 많은 조합 가능 (62^n)
 * 
 * 사용 문자:
 * - 숫자: 0-9 (10개)
 * - 소문자: a-z (26개) 
 * - 대문자: A-Z (26개)
 * - 총 62개 문자 사용
 * 
 * 변환 예시:
 * - Long ID: 12345 → Base62: "3D7"
 * - 최종 URL: "localhost:8080/dir/3D7"
 * 
 * 라이브러리: https://github.com/seruco/base62
 * 
 * @author dsg
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Base62Service {

    /**
     * Base62 인코딩/디코딩을 수행하는 인스턴스
     * 
     * Singleton 패턴으로 구현하여 메모리 효율성을 높이고
     * 스레드 안전성을 보장합니다.
     */
    private static final Base62 base62Instance = Base62.createInstance();

    /**
     * Direction ID를 Base62로 인코딩하여 단축 문자열을 생성합니다.
     * 
     * 이 메서드는 데이터베이스의 Long 타입 ID를 URL에 안전한 
     * 짧은 문자열로 변환합니다.
     * 
     * 변환 과정:
     * 1. Long → String 변환
     * 2. String → byte[] 변환 
     * 3. Base62 인코딩 적용
     * 4. 결과를 String으로 반환
     * 
     * 예시:
     * - 입력: 12345L
     * - 출력: "3D7" (실제 결과는 구현에 따라 다름)
     * 
     * @param directionId 인코딩할 Direction 엔티티의 ID
     * @return String Base62로 인코딩된 단축 문자열
     */
    public String encodeDirectionId(Long directionId) {
        return new String(base62Instance.encode(String.valueOf(directionId).getBytes()));
    }

    /**
     * Base62로 인코딩된 문자열을 원본 Direction ID로 디코딩합니다.
     * 
     * 이 메서드는 URL에서 전달받은 단축 문자열을 원본 Long ID로
     * 복원하여 데이터베이스 조회에 사용할 수 있도록 합니다.
     * 
     * 변환 과정:
     * 1. 인코딩된 String → byte[] 변환
     * 2. Base62 디코딩 적용  
     * 3. 결과를 String으로 변환
     * 4. String → Long 변환
     * 
     * 예시:
     * - 입력: "3D7"
     * - 출력: 12345L
     * 
     * @param encodedDirectionId Base62로 인코딩된 Direction ID 문자열
     * @return Long 디코딩된 원본 Direction ID
     * @throws NumberFormatException 잘못된 형식의 인코딩 문자열인 경우
     */
    public Long decodeDirectionId(String encodedDirectionId) {

        String resultDirectionId = new String(base62Instance.decode(encodedDirectionId.getBytes()));
        return Long.valueOf(resultDirectionId);
    }
}
