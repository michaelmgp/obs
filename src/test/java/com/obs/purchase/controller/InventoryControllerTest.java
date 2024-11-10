package com.obs.purchase.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.purchase.config.JpaAuditingConfig;
import com.obs.purchase.entity.Inventory;
import com.obs.purchase.entity.dto.GenericResponse;
import com.obs.purchase.exceptions.NotFoundExceptions;
import com.obs.purchase.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
@ContextConfiguration(classes = {JpaAuditingConfig.class}) // Load mock AuditorAware
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController)
                .setControllerAdvice(new GlobalExceptionHandler())  // Set up GlobalExceptionHandler
                .build();
        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setItemId(100L);
        inventory.setStock(50);
    }

    @Test
    void testCreateInventory_ShouldReturnCreated() throws Exception {
        when(inventoryService.save(any(Inventory.class))).thenReturn(inventory);

        mockMvc.perform(post("/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Inventory created successfully"))
                .andExpect(jsonPath("$.data.itemId").value(100))
                .andExpect(jsonPath("$.data.stock").value(50));

        verify(inventoryService, times(1)).save(any(Inventory.class));
    }

    @Test
    void testGetInventoryById_ShouldReturnInventory_WhenExists() throws Exception {
        when(inventoryService.findById(1L)).thenReturn(inventory);

        mockMvc.perform(get("/inventories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inventory retrieved successfully"))
                .andExpect(jsonPath("$.data.itemId").value(100))
                .andExpect(jsonPath("$.data.stock").value(50));

        verify(inventoryService, times(1)).findById(1L);
    }

    @Test
    void testGetInventoryById_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        when(inventoryService.findById(anyLong())).thenThrow(new NotFoundExceptions("not found error", "inventory not found"));

        mockMvc.perform(get("/inventories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application error"))
                .andExpect(jsonPath("$.errors['not found error']").value("inventory not found"));

        verify(inventoryService, times(1)).findById(1L);
    }


    @Test
    void testGetAllInventories_ShouldReturnAllInventories() throws Exception {
        Page<Inventory> page = new PageImpl<>(List.of(inventory), PageRequest.of(0, 10), 1);
        when(inventoryService.findAll(0, 10)).thenReturn(page);

        mockMvc.perform(get("/inventories")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All inventories retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].itemId").value(100))
                .andExpect(jsonPath("$.data.content[0].stock").value(50));

        verify(inventoryService, times(1)).findAll(0, 10);
    }

    @Test
    void testUpdateInventory_ShouldReturnOk() throws Exception {
        doNothing().when(inventoryService).update(any(Inventory.class));

        mockMvc.perform(put("/inventories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inventory updated successfully"));

        verify(inventoryService, times(1)).update(any(Inventory.class));
    }

    @Test
    void testDeleteInventory_ShouldReturnOk() throws Exception {
        doNothing().when(inventoryService).delete(1L);

        mockMvc.perform(delete("/inventories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inventory deleted successfully"));

        verify(inventoryService, times(1)).delete(1L);
    }

    @Test
    void testDeleteInventory_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        doThrow(new NotFoundExceptions("not found error", "inventory not found")).when(inventoryService).delete(1L);

        mockMvc.perform(delete("/inventories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application error"))
                .andExpect(jsonPath("$.errors['not found error']").value("inventory not found"));

        verify(inventoryService, times(1)).delete(1L);
    }
}
