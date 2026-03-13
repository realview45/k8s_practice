package com.beyond.order.ordering.controller;


import com.beyond.order.ordering.dtos.OrderingCreateDto;
import com.beyond.order.ordering.dtos.OrderingListDto;
import com.beyond.order.ordering.service.OrderingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderingController {
    private final OrderingService orderingService;
    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody List<OrderingCreateDto> dtoList, @RequestHeader("X-User-Email")String email){
        Long id = orderingService.createFeign(dtoList, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }
    @GetMapping("/list")
    public ResponseEntity<?> findAll(){
        List<OrderingListDto> dtoList = orderingService.findAll();
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoList);
    }
    @GetMapping("/myorders")
    public ResponseEntity<?> myorders(@RequestHeader("X-User-Email")String email){
        List<OrderingListDto> dtoList = orderingService.myorders(email);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoList);
    }
}