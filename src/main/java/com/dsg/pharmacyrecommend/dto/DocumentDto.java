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
    private double longitude;

    @JsonProperty("y")
    private double latitude;

    @JsonProperty("distance")
    private double distance;
}
