package com.beyond.order.ordering.repository;

import com.beyond.order.ordering.domain.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface OrderingRepository extends JpaRepository<Ordering, Long> {
    List<Ordering> findAllByMemberEmail(String email);
}
