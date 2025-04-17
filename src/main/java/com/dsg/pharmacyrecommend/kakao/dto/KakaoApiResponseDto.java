package com.dsg.pharmacyrecommend.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KakaoApiResponseDto {

    @JsonProperty("documents")
    private List<DocumentDto> documentList;

    @JsonProperty("meta")
    private MetaDto metaDto;

}
