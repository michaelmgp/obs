package com.obs.purchase.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.purchase.config.JpaAuditingConfig;
import com.obs.purchase.controller.ItemController;
import com.obs.purchase.entity.Item;
import com.obs.purchase.exceptions.DuplicateObjectExceptions;
import com.obs.purchase.exceptions.NotFoundExceptions;
import com.obs.purchase.service.ItemService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@ContextConfiguration(classes = {JpaAuditingConfig.class}) // Load mock AuditorAware
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private ItemController itemController;

    @MockBean
    private ItemService itemService;

    private Item item;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(itemController)
                .setControllerAdvice(new GlobalExceptionHandler())  // Set up GlobalExceptionHandler
                .build();
        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setPrice(10);
    }

    @Test
    void testCreateItem_ShouldReturnCreated() throws Exception {
        when(itemService.save(any(Item.class))).thenReturn(item);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Item created successfully"))
                .andExpect(jsonPath("$.data.name").value("Test Item"));

        verify(itemService, times(1)).save(any(Item.class));
    }

    @Test
    void testCreateItem_ShouldReturnBadRequest_WhenNameIsBlank() throws Exception {
        item.setName(""); // Invalid name, violates @NotBlank

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.errors.name").value("this field cannot be empty"));

        verify(itemService, never()).save(any(Item.class));
    }

    @Test
    void testCreateItem_ShouldReturnBadRequest_WhenPriceIsNegative() throws Exception {
        item.setPrice(-1); // Invalid price, violates @PositiveOrZero

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.errors.price").value("must be greater than or equal to 0"));

        verify(itemService, never()).save(any(Item.class));
    }
    @Test
    void testCreateItem_ShouldReturnBadRequest_WhenNameIsInvalid() throws Exception {
        item.setName("InvalidName123"); // Invalid name, violates @Pattern

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.errors.name").value("Invalid Input"));

        verify(itemService, never()).save(any(Item.class));
    }

    @Test
    void testSaveUser_ShouldReturnBadRequest_WhenUserAlreadyRegistered() throws Exception {
        DuplicateObjectExceptions duplicateException = new DuplicateObjectExceptions("item already registered", "Duplicate Error");

        doThrow(duplicateException).when(itemService).save(any(Item.class));

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application error"))
                .andExpect(jsonPath("$.errors['item already registered']").value("Duplicate Error"));

        verify(itemService, times(1)).save(any(Item.class));
    }

    @Test
    void testGetItemById_ShouldReturnItem_WhenItemExists() throws Exception {
        when(itemService.findById(1L)).thenReturn(item);

        mockMvc.perform(get("/items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item retrieved successfully"))
                .andExpect(jsonPath("$.data.name").value("Test Item"));

        verify(itemService, times(1)).findById(1L);
    }

    @Test
    void testGetItemById_ShouldReturnNotFound_WhenItemDoesNotExist() throws Exception {
        when(itemService.findById(1L)).thenThrow(new NotFoundExceptions("not found error", "item not found"));

        mockMvc.perform(get("/items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application error"))
                .andExpect(jsonPath("$.errors['not found error']").value("item not found"));

        verify(itemService, times(1)).findById(1L);
    }

    @Test
    void testGetAllItems_ShouldReturnAllItems() throws Exception {
        // Arrange
        int pageNo = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        List<Item> items = Arrays.asList(new Item(), new Item());
        Page<Item> itemPage = new PageImpl<>(items, pageable, items.size());

        when(itemService.findAll(pageNo, pageSize)).thenReturn(itemPage);

        // Act & Assert
        mockMvc.perform(get("/items")
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All items retrieved successfully"))
                .andExpect(jsonPath("$.data.content.length()").value(items.size()));

        verify(itemService, times(1)).findAll(pageNo, pageSize);

    }

    @Test
    void testUpdateItem_ShouldReturnOk() throws Exception {
        doNothing().when(itemService).update(any(Item.class));

        mockMvc.perform(put("/items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item updated successfully"));

        verify(itemService, times(1)).update(any(Item.class));
    }

    @Test
    void testDeleteItem_ShouldReturnOk() throws Exception {
        doNothing().when(itemService).delete(1L);

        mockMvc.perform(delete("/items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item deleted successfully"));

        verify(itemService, times(1)).delete(1L);
    }

    @Test
    void testDeleteItem_ShouldReturnNotFound_WhenItemDoesNotExist() throws Exception {
        doThrow(new NotFoundExceptions("item not found", "not found error")).when(itemService).delete(1L);

        mockMvc.perform(delete("/items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application error"))
                .andExpect(jsonPath("$.errors['item not found']").value("not found error"));

        verify(itemService, times(1)).delete(1L);
    }
}
