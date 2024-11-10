package com.obs.purchase.service;

import com.obs.purchase.entity.Inventory;
import com.obs.purchase.exceptions.DuplicateObjectExceptions;
import com.obs.purchase.exceptions.NotFoundExceptions;
import com.obs.purchase.repository.InventoryRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setItemId(100L);
        inventory.setStock(50);
    }

    @Test
    void testSave_ShouldReturnSavedInventory() {
        when(inventoryRepository.findByItemId(inventory.getItemId())).thenReturn(null);
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        Inventory result = inventoryService.save(inventory);

        assertNotNull(result);
        assertEquals(inventory.getItemId(), result.getItemId());
        assertEquals(inventory.getStock(), result.getStock());
        verify(inventoryRepository, times(1)).findByItemId(inventory.getItemId());
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    void testSave_ShouldThrowDuplicateObjectExceptions_WhenInventoryExists() {
        when(inventoryRepository.findByItemId(inventory.getItemId())).thenReturn(inventory);

        assertThrows(DuplicateObjectExceptions.class, () -> inventoryService.save(inventory));

        verify(inventoryRepository, times(1)).findByItemId(inventory.getItemId());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testDelete_ShouldSetDeletedAt_WhenInventoryExists() {
        when(inventoryRepository.findById(inventory.getId())).thenReturn(Optional.of(inventory));

        inventoryService.delete(inventory.getId());

        assertNotNull(inventory.getDeletedAt());
        verify(inventoryRepository, times(1)).findById(inventory.getId());
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    void testDelete_ShouldThrowNotFoundExceptions_WhenInventoryNotFound() {
        when(inventoryRepository.findById(inventory.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundExceptions.class, () -> inventoryService.delete(inventory.getId()));

        verify(inventoryRepository, times(1)).findById(inventory.getId());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testUpdate_ShouldUpdateInventory_WhenInventoryExists() {
        Inventory updatedInventory = new Inventory();
        updatedInventory.setId(1L);
        updatedInventory.setItemId(100L);
        updatedInventory.setStock(60);

        when(inventoryRepository.findById(inventory.getId())).thenReturn(Optional.of(inventory));

        inventoryService.update(updatedInventory);

        assertEquals(updatedInventory.getStock(), inventory.getStock());
        verify(inventoryRepository, times(1)).findById(inventory.getId());
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    void testUpdate_ShouldThrowNotFoundExceptions_WhenInventoryNotFound() {
        Inventory updatedInventory = new Inventory();
        updatedInventory.setId(1);
        updatedInventory.setItemId(100L);
        updatedInventory.setStock(60);

        when(inventoryRepository.findById(updatedInventory.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundExceptions.class, () -> inventoryService.update(updatedInventory));

        verify(inventoryRepository, times(1)).findById(updatedInventory.getId());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testFindById_ShouldReturnInventory_WhenInventoryExists() {
        when(inventoryRepository.findById(inventory.getId())).thenReturn(Optional.of(inventory));

        Inventory result = inventoryService.findById(inventory.getId());

        assertNotNull(result);
        assertEquals(inventory.getId(), result.getId());
        assertEquals(inventory.getItemId(), result.getItemId());
        verify(inventoryRepository, times(1)).findById(inventory.getId());
    }

    @Test
    void testFindById_ShouldThrowNotFoundExceptions_WhenInventoryNotFound() {
        when(inventoryRepository.findById(inventory.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundExceptions.class, () -> inventoryService.findById(inventory.getId()));

        verify(inventoryRepository, times(1)).findById(inventory.getId());
    }

    @Test
    void testFindAll_ShouldReturnPageOfInventories() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Inventory> inventoryPage = new PageImpl<>(List.of(inventory));
        when(inventoryRepository.findAll(pageable)).thenReturn(inventoryPage);

        Page<Inventory> result = inventoryService.findAll(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(inventory, result.getContent().get(0));
        verify(inventoryRepository, times(1)).findAll(pageable);
    }
}
