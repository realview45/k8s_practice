package com.beyond.order.product.domain;

import com.beyond.order.common.domain.BaseTimeEntity;
import com.beyond.order.member.domain.Member;
import com.beyond.order.product.dtos.ProductDetailDto;
import com.beyond.order.product.dtos.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable=false)
    private String name;
    @Column(nullable=false)
    private Long price;
    private String category;
    @Column(nullable=false)
    private int stockQuantity;
    private String imagePath;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Member member;

    public void updateProfileImageUrl(String imgUrl) {
        imagePath = imgUrl;
    }
    public void updateStockQuantity(int orderQuantity) {
        this.stockQuantity = this.stockQuantity-orderQuantity;
    }

    public void updateProduct(ProductUpdateDto dto) {
        this.name=dto.getName();
        this.category=dto.getCategory();
        this.price=dto.getPrice();
        this.stockQuantity = dto.getStockQuantity();
    }
}
