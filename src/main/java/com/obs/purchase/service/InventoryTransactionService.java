package com.obs.purchase.service;
import com.obs.purchase.entity.Inventory;
import com.obs.purchase.entity.InventoryTransaction;
import com.obs.purchase.enums.Type;
import com.obs.purchase.exceptions.InvalidRequest;
import com.obs.purchase.exceptions.NotFoundExceptions;
import com.obs.purchase.repository.InventoryRepository;
import com.obs.purchase.repository.InventoryTransactionRepository;
import com.obs.purchase.repository.ItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.Instant;
@Service
public class InventoryTransactionService implements BaseService<InventoryTransaction>{
    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ItemRepository itemRepository;
    @Override
    @Transactional
    public InventoryTransaction save(InventoryTransaction inventoryTransaction) {
        if(itemRepository.findById(inventoryTransaction.getItemId()).isEmpty()){
            throw new NotFoundExceptions("not found error", "item is not found");
        }
        Inventory inventory = inventoryRepository.findByItemId(inventoryTransaction.getItemId());

        if(inventory!=null) {
            inventory.setStock(updatedInventoryStock(inventory, inventoryTransaction));
        }else if(inventoryTransaction.getType().equals(Type.WITHDRAWAL.getDisplayName())){
            throw new InvalidRequest("out of stock", "stock is out cannot perform withdrawal");
        }else{
            inventory = new Inventory();
            inventory.setStock(inventoryTransaction.getQty());
            inventory.setItemId(inventoryTransaction.getItemId());
        }

        inventoryRepository.save(inventory);
        inventoryTransactionRepository.save(inventoryTransaction);
        return inventoryTransactionRepository.save(inventoryTransaction);
    }

    @Override
    public void delete(Long id) {
        InventoryTransaction inventoryTransaction = inventoryTransactionRepository.findById(id).orElse(null);
        if(inventoryTransaction==null){
            throw new NotFoundExceptions("not found error", "inventory transaction not found");
        }
        inventoryTransaction.setDeletedAt(Instant.now());
        inventoryTransactionRepository.save(inventoryTransaction);
    }

    @Override
    public void update(InventoryTransaction inventoryTransaction) {
        InventoryTransaction inventoryTransactionSaved = inventoryTransactionRepository.findById(inventoryTransaction.getId()).orElse(null);
        if(inventoryTransactionSaved==null){
            throw new NotFoundExceptions("not found error", "inventory transaction not found");
        }
        BeanUtils.copyProperties(inventoryTransaction,inventoryTransactionSaved);
        inventoryTransactionRepository.save(inventoryTransactionSaved);
    }

    @Override
    public InventoryTransaction findById(Long id) {
        InventoryTransaction inventoryTransaction = inventoryTransactionRepository.findById(id).orElse(null);
        if(inventoryTransaction==null){
            throw new NotFoundExceptions("not found error", "inventory not found");
        }
        return inventoryTransaction;
    }

    @Override
    public Page<InventoryTransaction> findAll(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return inventoryTransactionRepository.findAll(pageable);
    }

    public Integer updatedInventoryStock(Inventory inventory, InventoryTransaction inventoryTransaction){
        int stockQty = inventory.getStock();
        int updatedStockQty = stockQty;
        if(inventoryTransaction.getType().equals(Type.TOP_UP.getDisplayName())){
            updatedStockQty +=inventoryTransaction.getQty();
        }else{
            updatedStockQty -= inventoryTransaction.getQty();
        }
        if(updatedStockQty<0){
            throw new InvalidRequest("limited stock", "insufficient amount of stock");
        }
        return updatedStockQty;
    }
}
