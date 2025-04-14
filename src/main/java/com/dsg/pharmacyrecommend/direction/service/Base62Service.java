package com.dsg.pharmacyrecommend.direction.service;

import io.seruco.encoding.base62.Base62;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Base62Service {

    // https://github.com/seruco/base62
    /**
     * base64는 "=" 등 URL 예약어이기 때문에 부적절
     * base62 -> Base62는 Base64와 유사하지만, 특수 문자를 제외하고 알파벳 대문자(A-Z), 소문자(a-z), 숫자(0-9)만 사용합니다.
     * 이는 URL이나 파일 이름처럼 특수 문자를 사용할 수 없는 환경에서 유용
     * ex) localhost:8080/dir/raad21
     */
    private static final Base62 base62Instance = Base62.createInstance();

    public String encodeDirectionId(Long directionId) {
        return new String(base62Instance.encode(String.valueOf(directionId).getBytes()));
    }

    public Long decodeDirectionId(String encodedDirectionId) {

        String resultDirectionId = new String(base62Instance.decode(encodedDirectionId.getBytes()));
        return Long.valueOf(resultDirectionId);
    }
}
