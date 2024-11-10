package com.obs.purchase.controller;
import com.obs.purchase.entity.Inventory;
import com.obs.purchase.entity.dto.GenericResponse;
import com.obs.purchase.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventories")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<GenericResponse<Inventory>> createInventory(@RequestBody @Valid Inventory inventory) {
        Inventory savedInventory = inventoryService.save(inventory);
        return new ResponseEntity<>(new GenericResponse<>("Inventory created successfully", savedInventory), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<Inventory>> getInventoryById(@PathVariable Long id) {
        Inventory inventory = inventoryService.findById(id);
        return new ResponseEntity<>(new GenericResponse<>("Inventory retrieved successfully", inventory), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<GenericResponse<Page<Inventory>>> getAllInventories(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam (defaultValue = "10") int pageSize) {
        Page<Inventory> inventories = inventoryService.findAll(pageNo, pageSize);
        return new ResponseEntity<>(new GenericResponse<>("All inventories retrieved successfully", inventories), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> updateInventory(@PathVariable Long id, @RequestBody @Valid Inventory inventory) {
        inventory.setId(id); // Ensure the correct inventory is updated
        inventoryService.update(inventory);
        return new ResponseEntity<>(new GenericResponse<>("Inventory updated successfully"), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> deleteInventory(@PathVariable Long id) {
        inventoryService.delete(id);
        return new ResponseEntity<>(new GenericResponse<>("Inventory deleted successfully"), HttpStatus.OK);
    }
}
