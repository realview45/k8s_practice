package com.beyond.order.product.dtos;

import com.beyond.order.member.domain.Member;
import com.beyond.order.product.domain.Product;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateDto {
    @NotBlank
    private String name;
    @NotBlank
    private Long price;
    private String category;
    @NotBlank
    private int stockQuantity;
//    이미지 수정은 일반적으로 별도의 api로 처리
    private MultipartFile productImage;//s3와 통신시 성능이 떨어지 이미지업로드는 높은확률로 api가 따로있다.

    public Product toEntity(Member member) {
        return Product.builder()
                .name(name).price(price).category(category).stockQuantity(stockQuantity).member(member)
                .imagePath("path").build();
    }
}
