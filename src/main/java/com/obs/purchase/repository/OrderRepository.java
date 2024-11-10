package com.obs.purchase.repository;

import com.obs.purchase.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends BaseRepository<Order> {
    @Override
    @Query(value = "SELECT o FROM Order o where o.deletedAt IS NULL")
    List<Order> findExistingRecord();

    @Override
    @Query(value = "SELECT o FROM Order o where o.deletedAt IS NULL AND o.id = ?1")
    Order findExistingRecordById(long id);

    Optional<Order> findTopByOrderByIdDesc();

    Order findByOrderNo(String orderNo);
}
