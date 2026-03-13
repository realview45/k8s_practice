package com.beyond.order.product.dtos;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductStockUpdateDto {
    @NotBlank
    private Long productId;
    @NotBlank
    private int productCount;
}
