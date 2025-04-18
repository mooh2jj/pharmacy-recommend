package com.dsg.pharmacyrecommend.domain.pharmacy.entity;

import com.dsg.pharmacyrecommend.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pharmacy")
public class Pharmacy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pharmacyName;
    private String pharmacyAddress;
    private double latitude;
    private double longitude;

    public void changePharmacyAddress(String address) {
        this.pharmacyAddress = address;
    }
}
