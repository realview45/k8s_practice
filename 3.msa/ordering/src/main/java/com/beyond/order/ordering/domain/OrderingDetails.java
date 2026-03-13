package com.beyond.order.ordering.domain;


import com.beyond.order.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
public class OrderingDetails extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int quantity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private Ordering ordering;

    private String productName;
    private Long productId;
//    msa환경에서는 빈번한 http요청에 의한 성능저하를 막기 위한 반정규화 설계도 가능
//    private String productName;
}
