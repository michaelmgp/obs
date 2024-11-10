package com.obs.purchase.repository;

import com.obs.purchase.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface InventoryTransactionRepository extends BaseRepository<InventoryTransaction> {
    @Override
    @Query(value = "SELECT i FROM InventoryTransaction i where i.deletedAt IS NULL AND i.id = ?1")
    List<InventoryTransaction> findExistingRecord();

    @Override
    @Query(value = "SELECT i FROM InventoryTransaction i where i.deletedAt IS NULL")
    InventoryTransaction findExistingRecordById(long id);
}
