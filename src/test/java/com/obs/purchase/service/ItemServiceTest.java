package com.obs.purchase.service;


import com.obs.purchase.entity.Item;
import com.obs.purchase.exceptions.DuplicateObjectExceptions;
import com.obs.purchase.exceptions.NotFoundExceptions;
import com.obs.purchase.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item item;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setPrice(10);
    }

    @Test
    void testSave_ShouldSaveItem_WhenItemIsNotDuplicate() {
        when(itemRepository.findByName(item.getName())).thenReturn(null);
        when(itemRepository.save(item)).thenReturn(item);

        Item savedItem = itemService.save(item);

        assertNotNull(savedItem);
        assertEquals("Test Item", savedItem.getName());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void testSave_ShouldThrowDuplicateObjectExceptions_WhenItemIsDuplicate() {
        when(itemRepository.findByName(item.getName())).thenReturn(item);

        assertThrows(DuplicateObjectExceptions.class, () -> itemService.save(item));
        verify(itemRepository, never()).save(item);
    }

    @Test
    void testDelete_ShouldSetDeletedAt_WhenItemExists() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        itemService.delete(item.getId());

        assertNotNull(item.getDeletedAt());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void testDelete_ShouldThrowNotFoundExceptions_WhenItemDoesNotExist() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundExceptions.class, () -> itemService.delete(item.getId()));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void testUpdate_ShouldUpdateItem_WhenItemExists() {
        Item updatedItem = new Item();
        updatedItem.setId(item.getId());
        updatedItem.setName("Updated Name");
        updatedItem.setPrice(20);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        itemService.update(updatedItem);

        verify(itemRepository, times(1)).save(item);
        assertEquals("Updated Name", item.getName());

    }

    @Test
    void testUpdate_ShouldThrowNotFoundExceptions_WhenItemDoesNotExist() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundExceptions.class, () -> itemService.update(item));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void testFindById_ShouldReturnItem_WhenItemExists() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        Item foundItem = itemService.findById(item.getId());

        assertNotNull(foundItem);
        assertEquals(item.getId(), foundItem.getId());
        assertEquals("Test Item", foundItem.getName());
        verify(itemRepository, times(1)).findById(item.getId());
    }

    @Test
    void testFindById_ShouldThrowNotFoundExceptions_WhenItemDoesNotExist() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundExceptions.class, () -> itemService.findById(item.getId()));
        verify(itemRepository, times(1)).findById(item.getId());
    }

    @Test
    void testFindAll_ShouldReturnListOfItems() {
        // Arrange
        int pageNo = 0;
        int pageSize = 2;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        List<Item> items = Arrays.asList(new Item(), new Item());
        Page<Item> itemPage = new PageImpl<>(items, pageable, items.size());

        when(itemRepository.findAll(pageable)).thenReturn(itemPage);

        // Act
        Page<Item> result = itemService.findAll(pageNo, pageSize);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(items, result.getContent());
        verify(itemRepository, times(1)).findAll(pageable);
    }
}
