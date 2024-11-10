package com.obs.purchase.repository;

import com.obs.purchase.entity.Item;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends BaseRepository<Item>{
    @Override
    @Query(value = "SELECT i FROM Item i where i.deletedAt IS NULL")
    List<Item> findExistingRecord();

    @Override
    @Query(value = "SELECT i FROM Item i where i.deletedAt IS NULL AND i.id = ?1")
    Item findExistingRecordById(long id);


    Item findByName(String name);
}
