package com.leedahun.storecasecatalog.domain.product.entity;

import com.leedahun.storecasecatalog.common.entity.BaseTimeEntity;
import com.leedahun.storecasecatalog.domain.category.entity.Category;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;  // identity 서비스

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    private String name;

    private String description;

    private int price;

    @Builder.Default
    private boolean isDeleted = false;

}
