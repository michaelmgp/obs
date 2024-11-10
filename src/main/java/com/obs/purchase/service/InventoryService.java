package com.obs.purchase.service;
import com.obs.purchase.entity.Inventory;
import com.obs.purchase.exceptions.DuplicateObjectExceptions;
import com.obs.purchase.exceptions.NotFoundExceptions;
import com.obs.purchase.repository.InventoryRepository;
import com.obs.purchase.repository.ItemRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class InventoryService implements BaseService<Inventory>{
    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ItemRepository itemRepository;
    @Override
    public Inventory save(Inventory inventory) {
        if(inventoryRepository.findByItemId(inventory.getItemId())!=null){
            throw new DuplicateObjectExceptions("Duplicate Error", "inventory already registered");
        }
        return inventoryRepository.save(inventory);
    }

    @Override
    public void delete(Long id) {
        Inventory inventory = inventoryRepository.findById(id).orElse(null);
        if(inventory==null){
            throw new NotFoundExceptions("not found error", "inventory not found");
        }
        inventory.setDeletedAt(Instant.now());
        inventoryRepository.save(inventory);
    }

    @Override
    public void update(Inventory inventory) {
        Inventory inventorySaved = inventoryRepository.findById(inventory.getId()).orElse(null);
        if(inventorySaved==null){
            throw new NotFoundExceptions("not found error", "inventory not found");
        }
        BeanUtils.copyProperties(inventory,inventorySaved);
        inventoryRepository.save(inventorySaved);
    }

    @Override
    public Inventory findById(Long id) {
        Inventory inventory = inventoryRepository.findById(id).orElse(null);
        if(inventory==null){
            throw new NotFoundExceptions("not found error", "inventory not found");
        }
        return inventory;
    }

    @Override
    public Page<Inventory> findAll(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return inventoryRepository.findAll(pageable);
    }
}
