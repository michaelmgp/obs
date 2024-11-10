package com.obs.purchase.service;


import com.obs.purchase.entity.InventoryTransaction;
import com.obs.purchase.entity.Item;
import com.obs.purchase.entity.Order;

import com.obs.purchase.exceptions.NotFoundExceptions;


import com.obs.purchase.repository.ItemRepository;
import com.obs.purchase.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryTransactionService inventoryTransactionService;

    private Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        order = new Order();
        order.setId(1L);
        order.setItemId(100L);
        order.setQty(5);
        order.setPrice(500);
    }

    @Test
    void testSave_ShouldGenerateOrderNoAndSaveOrder() {
        when(itemRepository.findById(order.getItemId())).thenReturn(Optional.of(new Item())); // Mock item existence
        when(orderRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty()); // Mock no existing orders
        when(orderRepository.save(order)).thenReturn(order);

        // Act
        Order savedOrder = orderService.save(order);

        // Assert
        assertNotNull(savedOrder);
        assertEquals("O1", savedOrder.getOrderNo()); // Order number should start from O1
        verify(orderRepository, times(1)).save(order);
        verify(inventoryTransactionService, times(1)).save(any(InventoryTransaction.class));
    }

    @Test
    void testSave_ShouldIncrementOrderNoWhenOrdersExist() {
        // Arrange
        Order lastOrder = new Order();
        lastOrder.setOrderNo("O5");
        when(itemRepository.findById(order.getItemId())).thenReturn(Optional.of(new Item()));
        when(orderRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(lastOrder)); // Mock last order as O5
        when(orderRepository.save(order)).thenReturn(order);

        // Act
        Order savedOrder = orderService.save(order);

        // Assert
        assertNotNull(savedOrder);
        assertEquals("O6", savedOrder.getOrderNo()); // Expect next order number to be O6
        verify(orderRepository, times(1)).save(order);
        verify(inventoryTransactionService, times(1)).save(any(InventoryTransaction.class));
    }

    @Test
    void testSave_ShouldThrowNotFoundException_WhenItemDoesNotExist() {
        // Arrange
        when(itemRepository.findById(order.getItemId())).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundExceptions exception = assertThrows(NotFoundExceptions.class, () -> orderService.save(order));
        assertEquals("item is not found", exception.getSpecificCause());
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryTransactionService, never()).save(any(InventoryTransaction.class));
    }

    @Test
    void testDelete_ShouldMarkOrderAsDeleted() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        // Act
        orderService.delete(1L);

        // Assert
        assertNotNull(order.getDeletedAt());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testDelete_ShouldThrowNotFoundException_WhenOrderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundExceptions exception = assertThrows(NotFoundExceptions.class, () -> orderService.delete(1L));
        assertEquals("order transaction not found", exception.getSpecificCause());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testUpdate_ShouldUpdateOrder() {
        Order updatedOrder = new Order();
        updatedOrder.setId(1l);
        updatedOrder.setOrderNo("O1");
        updatedOrder.setQty(10);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.update(updatedOrder);

        assertEquals(updatedOrder.getQty(), order.getQty());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testUpdate_ShouldThrowNotFoundException_WhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundExceptions exception = assertThrows(NotFoundExceptions.class, () -> orderService.update(order));
        assertEquals("order transaction not found", exception.getSpecificCause());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testFindById_ShouldReturnOrder_WhenOrderExists() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        Order foundOrder = orderService.findById(1L);

        // Assert
        assertNotNull(foundOrder);
        assertEquals(order, foundOrder);
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_ShouldThrowNotFoundException_WhenOrderDoesNotExist() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundExceptions exception = assertThrows(NotFoundExceptions.class, () -> orderService.findById(1L));
        assertEquals("order not found", exception.getSpecificCause());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testFindAll_ShouldReturnPagedOrders() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Order> result = orderService.findAll(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findAll(pageable);
    }
}
