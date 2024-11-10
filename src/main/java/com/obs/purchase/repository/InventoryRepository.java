package com.obs.purchase.repository;

import com.obs.purchase.entity.Inventory;
import com.obs.purchase.entity.Item;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends BaseRepository<Inventory>{
    @Query(value = "SELECT i FROM Inventory i where i.deletedAt IS NULL")
    List<Inventory> findExistingRecord();

    @Override
    @Query(value = "SELECT i FROM Inventory i where i.deletedAt IS NULL AND i.id = ?1")
    Inventory findExistingRecordById(long id);

    Inventory findByItemId(long itemId);
}
