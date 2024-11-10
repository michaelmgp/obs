package com.obs.purchase.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.purchase.config.JpaAuditingConfig;
import com.obs.purchase.entity.InventoryTransaction;
import com.obs.purchase.entity.Order;
import com.obs.purchase.entity.dto.GenericResponse;
import com.obs.purchase.exceptions.InvalidRequest;
import com.obs.purchase.exceptions.NotFoundExceptions;
import com.obs.purchase.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(OrderController.class)
@ContextConfiguration(classes = {JpaAuditingConfig.class}) // Load mock AuditorAware
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())  // Set up GlobalExceptionHandler
                .build();

        order = new Order();
        order.setId(1L);
        order.setOrderNo("O1");
        order.setItemId(100L);
        order.setQty(5);
        order.setPrice(50);
    }

    // Positive test cases

    @Test
    void testCreateOrder_ShouldReturnCreated() throws Exception {
        when(orderService.save(any(Order.class))).thenReturn(order);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.data.orderNo").value("O1"))
                .andExpect(jsonPath("$.data.itemId").value(100));

        verify(orderService, times(1)).save(any(Order.class));
    }

    @Test
    void testGetOrderById_ShouldReturnOrder_WhenOrderExists() throws Exception {
        when(orderService.findById(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order retrieved successfully"))
                .andExpect(jsonPath("$.data.orderNo").value("O1"))
                .andExpect(jsonPath("$.data.itemId").value(100));

        verify(orderService, times(1)).findById(1L);
    }

    @Test
    void testGetAllOrders_ShouldReturnAllOrders() throws Exception {
        Page<Order> page = new PageImpl<>(Collections.singletonList(order), PageRequest.of(0,10),1);

        when(orderService.findAll(0, 10)).thenReturn(page);

        mockMvc.perform(get("/orders")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All orders retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].orderNo").value("O1"))
                .andExpect(jsonPath("$.data.content[0].itemId").value(100));

        verify(orderService, times(1)).findAll(0, 10);
    }

    @Test
    void testUpdateOrder_ShouldReturnOk() throws Exception {
        doNothing().when(orderService).update(any(Order.class));

        mockMvc.perform(put("/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order updated successfully"));

        verify(orderService, times(1)).update(any(Order.class));
    }

    @Test
    void testDeleteOrder_ShouldReturnOk() throws Exception {
        doNothing().when(orderService).delete(1L);

        mockMvc.perform(delete("/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order deleted successfully"));

        verify(orderService, times(1)).delete(1L);
    }

    // Negative test cases

    @Test
    void testCreateOrder_ShouldReturnBadRequest_WhenItemNotFound() throws Exception {
        when(orderService.save(any(Order.class))).thenThrow(new NotFoundExceptions("not found error", "item is not found"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application error"))
                .andExpect(jsonPath("$.errors['not found error']").value("item is not found"));

        verify(orderService, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateOrder_ShouldReturnBadRequest_WhenStockIsInsufficient() throws Exception {
        when(orderService.save(any(Order.class))).thenThrow(new InvalidRequest("out of stock", "stock is out cannot perform withdrawal"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application error"))
                .andExpect(jsonPath("$.errors['out of stock']").value("stock is out cannot perform withdrawal"));

        verify(orderService, times(1)).save(any(Order.class));
    }

    @Test
    void testGetOrderById_ShouldReturnNotFound_WhenOrderDoesNotExist() throws Exception {
        when(orderService.findById(1L)).thenThrow(new NotFoundExceptions("not found error", "order not found"));

        mockMvc.perform(get("/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application error"))
                .andExpect(jsonPath("$.errors['not found error']").value("order not found"));

        verify(orderService, times(1)).findById(1L);
    }

    @Test
    void testUpdateOrder_ShouldReturnNotFound_WhenOrderDoesNotExist() throws Exception {
        doThrow(new NotFoundExceptions("not found error", "order transaction not found")).when(orderService).update(any(Order.class));

        mockMvc.perform(put("/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application error"))
                .andExpect(jsonPath("$.errors['not found error']").value("order transaction not found"));

        verify(orderService, times(1)).update(any(Order.class));
    }

    @Test
    void testDeleteOrder_ShouldReturnNotFound_WhenOrderDoesNotExist() throws Exception {
        doThrow(new NotFoundExceptions("not found error", "order transaction not found")).when(orderService).delete(1L);

        mockMvc.perform(delete("/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application error"))
                .andExpect(jsonPath("$.errors['not found error']").value("order transaction not found"));

        verify(orderService, times(1)).delete(1L);
    }
}
