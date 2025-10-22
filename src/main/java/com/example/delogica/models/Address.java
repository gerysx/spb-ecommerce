// src/main/java/com/example/shop/domain/Address.java
package com.example.delogica.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses", indexes = {
        @Index(name = "idx_address_customer", columnList = "customer_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
public class Address {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Customer customer;

    @Column(nullable = false, length = 160)
    private String line1;

    @Column(length = 160)
    private String line2;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 80)
    private String country;

    @JsonProperty("isDefault") 
    @Column(name = "is_default", nullable = false)
    private Boolean defaultAddress;
}
