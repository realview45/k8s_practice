package com.beyond.order.ordering.service;


import com.beyond.order.common.service.SseAlarmService;
import com.beyond.order.ordering.domain.Ordering;
import com.beyond.order.ordering.domain.OrderingDetails;
import com.beyond.order.ordering.dtos.OrderingCreateDto;
import com.beyond.order.ordering.dtos.OrderingListDto;
import com.beyond.order.ordering.dtos.ProductDto;
import com.beyond.order.ordering.feignclients.ProductFeignClient;
import com.beyond.order.ordering.repository.OrderingDetailsRepository;
import com.beyond.order.ordering.repository.OrderingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderingDetailsRepository orderingDetailsRepository;
    private final SseAlarmService sseAlarmService;
    private final RestTemplate restTemplate;
    private final ProductFeignClient productFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public OrderingService(OrderingRepository orderingRepository, OrderingDetailsRepository orderingDetailsRepository, SseAlarmService sseAlarmService, RestTemplate restTemplate, ProductFeignClient productFeignClient, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderingRepository = orderingRepository;
        this.orderingDetailsRepository = orderingDetailsRepository;
        this.sseAlarmService = sseAlarmService;
        this.restTemplate = restTemplate;
        this.productFeignClient = productFeignClient;
        this.kafkaTemplate = kafkaTemplate;
    }

//    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long create(List<OrderingCreateDto> dtoList, String email) {
        //나중에 이메일을 토큰에서 꺼낼수없음 게이트웨이에서 처리
//        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("엔티티가 없습니다."));
        Ordering ordering = OrderingCreateDto.toEntity(email);
        orderingRepository.save(ordering);
        for (OrderingCreateDto dto : dtoList) {
            //1. 재고조회(동기요청-http요청)
//           "http://localhost:8080/product-service/product/detail/":apigateway을 통한 호출
//            "http://product-service/product/detail/":eureka에게 질의 후 produc-service 직접 호출
            String endpoint1 = "http://product-service/product/detail/" + dto.getProductId();
            //Body꺼내서 파싱까지 알아서해줌  내부통신이기 때문에 토큰은 필요없음
            //org.springframework
            HttpHeaders headers = new HttpHeaders();
//            HttpEntity : header + body
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<ProductDto> responseEntity =
             restTemplate.exchange(endpoint1, HttpMethod.GET, httpEntity, ProductDto.class);
            ProductDto product = responseEntity.getBody();
            System.out.println(product);

            if(product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            //2. 주문발생 3step
            OrderingDetails od =
                    OrderingDetails.builder()
                            .productName(product.getName())
                            .productId(dto.getProductId())
                            .quantity(dto.getProductCount())
                            .ordering(ordering).build();
            orderingDetailsRepository.save(od);
            //3. 재고감소 요청(동기-http요청/비동기-이벤트기반메시지기반 잘안씀(반대편에서 에러가나도 할수있는게없음) 모두 가능)카프카를 활용
//            product.updateStockQuantity(dto.getProductCount());
            String endpoint2 = "http://product-service/product/updatestock";
            //Body꺼내서 파싱까지 알아서해줌  내부통신이기 때문에 토큰은 필요없음
            //org.springframework
            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity : header + body 직렬화가 자동
            HttpEntity<OrderingCreateDto> httpEntity2 = new HttpEntity<>(dto, headers2);
            restTemplate.exchange(endpoint2, HttpMethod.PUT, httpEntity2, Void.class);
            //에러가나도 기다리다 롤백처리된다.

        }
        return ordering.getId();
    }
    public Long createFeign(List<OrderingCreateDto> dtoList, String email) {
        //나중에 이메일을 토큰에서 꺼낼수없음 게이트웨이에서 처리
        Ordering ordering = OrderingCreateDto.toEntity(email);
        orderingRepository.save(ordering);
        for (OrderingCreateDto dto : dtoList) {
            ProductDto product = productFeignClient.getProductById(dto.getProductId());

            if(product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            //2. 주문발생 3step
            OrderingDetails od =
                    OrderingDetails.builder()
                            .productName(product.getName())
                            .productId(dto.getProductId())
                            .quantity(dto.getProductCount())
                            .ordering(ordering).build();
            orderingDetailsRepository.save(od);
            //feign을 사용한 동기적 재고감소 요청 동기적 요청 부분을 비동기적 요청으로 바꿔보자
            //productFeignClient.updateStockQuantity(dto);
            //kafka를 활용한 비동기적 재고감소 요청 성능이 매우 빨라야하거나, 요청에 대한 응답이 느릴 때(보상트랜잭션설계가 매우 까다로움)
            kafkaTemplate.send("stock-update-topic", dto);
        }
        return ordering.getId();
    }
    public List<OrderingListDto> findAll() {
        List<Ordering> orderingList = orderingRepository.findAll();
        List<OrderingListDto> dtoList = new ArrayList<>();
        for(Ordering o : orderingList){
            OrderingListDto orderingListDto = OrderingListDto.fromEntity(o);
            dtoList.add(orderingListDto);
        }
        return dtoList;
        //return orderingRepository.findAll().stream().map(o-> OrderingListDto.fromEntity(o)).collect(Collectors.toList());
    }
    public List<OrderingListDto> myorders(String email) {
//        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("엔티티가 없습니다."));
        return orderingRepository.findAllByMemberEmail(email).stream().map(o-> OrderingListDto.fromEntity(o)).collect(Collectors.toList());
    }
}
