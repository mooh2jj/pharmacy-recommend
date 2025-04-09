package com.dsg.pharmacyrecommend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DocumentDto {

//    @JsonProperty("place_name")
//    private String placeName;

    @JsonProperty("address_name")
    private String addressName;

    @JsonProperty("x")
    private double longitude; // 경도

    @JsonProperty("y")
    private double latitude;  // 위도

    @JsonProperty("distance")
    private double distance;  // 거리
}
