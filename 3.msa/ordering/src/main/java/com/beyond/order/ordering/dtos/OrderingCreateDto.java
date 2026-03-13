package com.beyond.order.ordering.dtos;


import com.beyond.order.ordering.domain.OrderStatus;
import com.beyond.order.ordering.domain.Ordering;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderingCreateDto {
    @NotBlank
    private Long productId;
    @NotBlank
    private int productCount;
    public static Ordering toEntity(String email) {
        return Ordering.builder()
                .orderStatus(OrderStatus.ordered)
                .memberEmail(email)
                .build();
    }
}
