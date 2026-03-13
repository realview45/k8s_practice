package com.beyond.order.product.service;
import com.beyond.order.product.dtos.ProductStockUpdateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StockKafkaListener {
    private final ProductService productService;
    private final ObjectMapper objectMapper;
    public StockKafkaListener(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
    }
    @KafkaListener(topics = "stock-update-topic", containerFactory = "kafkaListener")
    public void stockConsumer(String message) throws JsonProcessingException {
        System.out.println("========kafka listener start========");
        ProductStockUpdateDto dto = objectMapper.readValue(message, ProductStockUpdateDto.class);
        //에러났을때 http요청
        productService.updateStock(dto);
    }
}