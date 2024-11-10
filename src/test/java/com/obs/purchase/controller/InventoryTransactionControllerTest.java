package com.obs.purchase.controller;
import com.obs.purchase.config.JpaAuditingConfig;
import com.obs.purchase.entity.InventoryTransaction;
import com.obs.purchase.exceptions.NotFoundExceptions;
import com.obs.purchase.repository.ItemRepository;
import com.obs.purchase.service.InventoryTransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.implementation.bind.annotation.Empty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryTransactionController.class)
@ContextConfiguration(classes = {JpaAuditingConfig.class}) // Load mock AuditorAware
class InventoryTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private InventoryTransactionController inventoryTransactionController;

    @MockBean
    private InventoryTransactionService inventoryTransactionService;

    @Autowired
    private ObjectMapper objectMapper;


    @MockBean
    private ItemRepository itemRepository;

    private InventoryTransaction inventoryTransaction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryTransactionController)
                .setControllerAdvice(new GlobalExceptionHandler())  // Set up GlobalExceptionHandler
                .build();
        inventoryTransaction = new InventoryTransaction();
        inventoryTransaction.setId(1L);
        inventoryTransaction.setItemId(100L);
        inventoryTransaction.setQty(5);
        inventoryTransaction.setType("W");
    }

    @Test
    void testCreateInventoryTransaction_ShouldReturnCreated() throws Exception {
        when(inventoryTransactionService.save(any(InventoryTransaction.class))).thenReturn(inventoryTransaction);

        mockMvc.perform(post("/inventory-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryTransaction)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Transaction created successfully"))
                .andExpect(jsonPath("$.data.itemId").value(100));

        verify(inventoryTransactionService, times(1)).save(any(InventoryTransaction.class));
    }

    @Test
    void testCreateInventoryTransaction_ValidTransaction_ShouldReturnCreated() throws Exception {
        when(inventoryTransactionService.save(any(InventoryTransaction.class))).thenReturn(inventoryTransaction);

        mockMvc.perform(post("/inventory-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryTransaction)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Transaction created successfully"))
                .andExpect(jsonPath("$.data.type").value("W"));

        verify(inventoryTransactionService, times(1)).save(any(InventoryTransaction.class));
    }

    @Test
    void testCreateInventoryTransaction_InvalidType_ShouldReturnBadRequest() throws Exception {
        inventoryTransaction.setType("X");  // Invalid type, only 'T' or 'W' allowed

        mockMvc.perform(post("/inventory-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryTransaction)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.type").value("Type must be either 'T' for Top Up or 'W' for Withdrawal"));
    }

    @Test
    void testCreateInventoryTransaction_MissingItemId_ShouldReturnBadRequest() throws Exception {

        inventoryTransaction.setItemId(0);  // Invalid itemId (null or 0)
//        when(inventoryTransactionService.save(inventoryTransaction)).thenThrow(new NotFoundExceptions("not found error", "item is not found"));

        mockMvc.perform(post("/inventory-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryTransaction)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.itemId").exists());
    }

    @Test
    void testCreateItem_NullQuantity_ShouldReturnBadRequest() throws Exception {
        inventoryTransaction.setQty(0);  // Assuming qty cannot be zero or null

        mockMvc.perform(post("/inventory-transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryTransaction)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.qty").exists());
    }

    @Test
    void testGetItemById_NotFound_ShouldReturnNotFound() throws Exception {
        long nonExistentId = 999L;
        when(inventoryTransactionService.findById(nonExistentId)).thenThrow(new NotFoundExceptions("not found error", "Inventory Transaction not found"));

        mockMvc.perform(get("/inventory-transaction/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application error"))
                .andExpect(jsonPath("$.errors['not found error']").value("Inventory Transaction not found"));

        verify(inventoryTransactionService, times(1)).findById(nonExistentId);
    }


    @Test
    void testGetInventoryTransactionById_ShouldReturnInventoryTransaction_WhenExists() throws Exception {
        when(inventoryTransactionService.findById(1L)).thenReturn(inventoryTransaction);

        mockMvc.perform(get("/inventory-transaction/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inventory Transaction retrieved successfully"))
                .andExpect(jsonPath("$.data.itemId").value(100));

        verify(inventoryTransactionService, times(1)).findById(1L);
    }

    @Test
    void testGetInventoryTransactionById_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        when(inventoryTransactionService.findById(1L)).thenThrow(new NotFoundExceptions("not found error", "transaction not found"));

        mockMvc.perform(get("/inventory-transaction/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application error"))
                .andExpect(jsonPath("$.errors['not found error']").value("transaction not found"));

        verify(inventoryTransactionService, times(1)).findById(1L);
    }

    @Test
    void testGetAllInventoryTransactions_ShouldReturnPageOfInventoryTransactions() throws Exception {
        Page<InventoryTransaction> page = new PageImpl<>(Collections.singletonList(inventoryTransaction), PageRequest.of(0, 10), 1);
        when(inventoryTransactionService.findAll(0, 10)).thenReturn(page);

        mockMvc.perform(get("/inventory-transaction")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All transaction retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].itemId").value(100));

        verify(inventoryTransactionService, times(1)).findAll(0, 10);
    }

    @Test
    void testUpdateInventoryTransaction_ShouldReturnOk() throws Exception {
        doNothing().when(inventoryTransactionService).update(any(InventoryTransaction.class));

        mockMvc.perform(put("/inventory-transaction/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction updated successfully"));

        verify(inventoryTransactionService, times(1)).update(any(InventoryTransaction.class));
    }

    @Test
    void testDeleteInventoryTransaction_ShouldReturnOk() throws Exception {
        doNothing().when(inventoryTransactionService).delete(1L);

        mockMvc.perform(delete("/inventory-transaction/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));

        verify(inventoryTransactionService, times(1)).delete(1L);
    }

    @Test
    void testDeleteInventoryTransaction_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        doThrow(new NotFoundExceptions("not found error", "transaction not found")).when(inventoryTransactionService).delete(1L);

        mockMvc.perform(delete("/inventory-transaction/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application error"))
                .andExpect(jsonPath("$.errors['not found error']").value("transaction not found"));

        verify(inventoryTransactionService, times(1)).delete(1L);
    }
}
