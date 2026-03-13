package com.beyond.order.ordering.service;

import com.beyond.order.common.service.SseAlarmService;
import com.beyond.order.member.domain.Member;
import com.beyond.order.member.repository.MemberRepository;
import com.beyond.order.ordering.domain.Ordering;
import com.beyond.order.ordering.dtos.OrderingCreateDto;
import com.beyond.order.ordering.dtos.OrderingListDto;
import com.beyond.order.ordering.repository.OrderingDetailsRepository;
import com.beyond.order.ordering.repository.OrderingRepository;
import com.beyond.order.ordering.domain.OrderingDetails;
import com.beyond.order.product.domain.Product;
import com.beyond.order.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderingDetailsRepository orderingDetailsRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final SseAlarmService sseAlarmService;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public OrderingService(OrderingRepository orderingRepository, OrderingDetailsRepository orderingDetailsRepository, ProductRepository productRepository, MemberRepository memberRepository, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate) {
        this.orderingRepository = orderingRepository;
        this.orderingDetailsRepository = orderingDetailsRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
    }

//    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long create(List<OrderingCreateDto> dtoList) {
        //나중에 이메일을 토큰에서 꺼낼수없음 게이트웨이에서 처리
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("엔티티가 없습니다."));
        Ordering ordering = OrderingCreateDto.toEntity(member);
        orderingRepository.save(ordering);
        for (OrderingCreateDto dto : dtoList) {
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("엔티티가없습니다."));
            if(product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            product.updateStockQuantity(dto.getProductCount());
            OrderingDetails od =
                    OrderingDetails.builder()
                            .product(product)
                            .quantity(dto.getProductCount())
                            .ordering(ordering).build();
            orderingDetailsRepository.save(od);
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
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("엔티티가 없습니다."));
        return orderingRepository.findAllByMember(member).stream().map(o-> OrderingListDto.fromEntity(o)).collect(Collectors.toList());

//        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
//        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("엔티티가 없습니다."));
//        List<Ordering> orderingList = member.getOrderingList();
//        List<OrderingListDto> dtoList = new ArrayList<>();
//        for(Ordering o : orderingList){
//            List<OrderingDetails> detailsList = o.getOrderList();
//            List<OrderingDetailsListDto> detailsListDtoList = detailsList.stream().map(d-> OrderingDetailsListDto.fromEntity(d)).collect(Collectors.toList());
//            OrderingListDto orderingListDto = OrderingListDto.builder()
//                    .id(o.getId())
//                    .memberEmail(o.getMember().getEmail())
//                    .orderStatus(o.getOrderStatus())
//                    .orderingDetailsListDtoList(detailsListDtoList).build();
//            dtoList.add(orderingListDto);
//        }
//        return dtoList;
    }
}
