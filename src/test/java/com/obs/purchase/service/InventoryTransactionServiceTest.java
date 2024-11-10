package com.obs.purchase.service;

import com.obs.purchase.entity.Inventory;
import com.obs.purchase.entity.InventoryTransaction;
import com.obs.purchase.entity.Item;
import com.obs.purchase.enums.Type;
import com.obs.purchase.exceptions.InvalidRequest;
import com.obs.purchase.exceptions.NotFoundExceptions;
import com.obs.purchase.repository.InventoryRepository;
import com.obs.purchase.repository.InventoryTransactionRepository;
import com.obs.purchase.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryTransactionServiceTest {

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private InventoryTransactionService inventoryTransactionService;

    private InventoryTransaction inventoryTransaction;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        inventoryTransaction = new InventoryTransaction();
        inventoryTransaction.setId(1L);
        inventoryTransaction.setItemId(1L);
        inventoryTransaction.setQty(10);
        inventoryTransaction.setType(Type.TOP_UP.getDisplayName());

        inventory = new Inventory();
        inventory.setItemId(1L);
        inventory.setStock(20);
    }

    @Test
    void testSave_ShouldAddToStock_WhenTopUp() {
        when(itemRepository.findById(inventoryTransaction.getItemId())).thenReturn(Optional.of(new Item()));
        when(inventoryRepository.findByItemId(inventoryTransaction.getItemId())).thenReturn(inventory);
        when(inventoryTransactionRepository.save(inventoryTransaction)).thenReturn(inventoryTransaction);

        InventoryTransaction savedTransaction = inventoryTransactionService.save(inventoryTransaction);

        assertEquals(inventoryTransaction, savedTransaction);
        verify(inventoryRepository, times(1)).save(inventory);
        verify(inventoryTransactionRepository, times(2)).save(inventoryTransaction);
        assertEquals(30, inventory.getStock());
    }

    @Test
    void testSave_ShouldThrowInvalidRequest_WhenWithdrawalExceedsStock() {
        inventoryTransaction.setType(Type.WITHDRAWAL.getDisplayName());
        inventoryTransaction.setQty(50);  // Exceeds available stock

        when(itemRepository.findById(inventoryTransaction.getItemId())).thenReturn(Optional.of(new Item()));
        when(inventoryRepository.findByItemId(inventoryTransaction.getItemId())).thenReturn(inventory);

        InvalidRequest exception = assertThrows(InvalidRequest.class, () -> inventoryTransactionService.save(inventoryTransaction));
        assertEquals("limited stock", exception.getMessage());
        assertEquals("insufficient amount of stock", exception.getSpecificCause());
    }

    @Test
    void testSave_ShouldInitializeStock_WhenNewItemAndTopUp() {
        when(itemRepository.findById(inventoryTransaction.getItemId())).thenReturn(Optional.of(new Item()));
        when(inventoryRepository.findByItemId(inventoryTransaction.getItemId())).thenReturn(null);  // No inventory record exists for the item
        when(inventoryTransactionRepository.save(inventoryTransaction)).thenReturn(inventoryTransaction);

        InventoryTransaction savedTransaction = inventoryTransactionService.save(inventoryTransaction);

        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        assertEquals(inventoryTransaction, savedTransaction);
    }

    @Test
    void testSave_ShouldThrowNotFound_WhenItemDoesNotExist() {
        when(itemRepository.findById(inventoryTransaction.getItemId())).thenReturn(Optional.empty());

        NotFoundExceptions exception = assertThrows(NotFoundExceptions.class, () -> inventoryTransactionService.save(inventoryTransaction));
        assertEquals("not found error", exception.getMessage());
        assertEquals("item is not found", exception.getSpecificCause());
    }

    @Test
    void testDelete_ShouldMarkAsDeleted_WhenInventoryTransactionExists() {
        when(inventoryTransactionRepository.findById(1L)).thenReturn(Optional.of(inventoryTransaction));

        inventoryTransactionService.delete(1L);

        assertNotNull(inventoryTransaction.getDeletedAt());
        verify(inventoryTransactionRepository, times(1)).save(inventoryTransaction);
    }

    @Test
    void testDelete_ShouldThrowNotFound_WhenInventoryTransactionDoesNotExist() {
        when(inventoryTransactionRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundExceptions exception = assertThrows(NotFoundExceptions.class, () -> inventoryTransactionService.delete(1L));
        assertEquals("not found error", exception.getMessage());
        assertEquals("inventory transaction not found", exception.getSpecificCause());
    }

    @Test
    void testUpdate_ShouldUpdateTransaction_WhenExists() {
        InventoryTransaction updatedTransaction = new InventoryTransaction();
        updatedTransaction.setId(1L);
        updatedTransaction.setItemId(1L);
        updatedTransaction.setQty(5);
        updatedTransaction.setType(Type.WITHDRAWAL.getDisplayName());

        when(inventoryTransactionRepository.findById(1L)).thenReturn(Optional.of(inventoryTransaction));

        inventoryTransactionService.update(updatedTransaction);

        verify(inventoryTransactionRepository, times(1)).save(inventoryTransaction);
        assertEquals(5, inventoryTransaction.getQty());
        assertEquals(Type.WITHDRAWAL.getDisplayName(), inventoryTransaction.getType());
    }

    @Test
    void testFindById_ShouldReturnTransaction_WhenExists() {
        when(inventoryTransactionRepository.findById(1L)).thenReturn(Optional.of(inventoryTransaction));

        InventoryTransaction foundTransaction = inventoryTransactionService.findById(1L);

        assertEquals(inventoryTransaction, foundTransaction);
    }

    @Test
    void testFindById_ShouldThrowNotFound_WhenNotExists() {
        when(inventoryTransactionRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundExceptions exception = assertThrows(NotFoundExceptions.class, () -> inventoryTransactionService.findById(1L));
        assertEquals("not found error", exception.getMessage());
        assertEquals("inventory not found", exception.getSpecificCause());
    }

    @Test
    void testFindAll_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<InventoryTransaction> page = new PageImpl<>(List.of(inventoryTransaction));
        when(inventoryTransactionRepository.findAll(pageable)).thenReturn(page);

        Page<InventoryTransaction> result = inventoryTransactionService.findAll(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(inventoryTransaction, result.getContent().get(0));
        verify(inventoryTransactionRepository, times(1)).findAll(pageable);
    }
}
