package com.obs.purchase.controller;
import com.obs.purchase.entity.InventoryTransaction;
import com.obs.purchase.entity.dto.GenericResponse;
import com.obs.purchase.service.InventoryTransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory-transaction")
public class InventoryTransactionController {
    @Autowired
    private InventoryTransactionService inventoryTransactionService;

    @PostMapping
    public ResponseEntity<GenericResponse<InventoryTransaction>> createItem(@RequestBody @Valid InventoryTransaction inventoryTransaction) {
        InventoryTransaction savedInventoryTransaction = inventoryTransactionService.save(inventoryTransaction);
        return new ResponseEntity<>(new GenericResponse<>("Transaction created successfully", savedInventoryTransaction), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<InventoryTransaction>> getItemById(@PathVariable Long id) {
        InventoryTransaction inventoryTransaction = inventoryTransactionService.findById(id);
        return new ResponseEntity<>(new GenericResponse<>("Inventory Transaction retrieved successfully", inventoryTransaction), HttpStatus.OK);
    }


    @GetMapping
    public ResponseEntity<GenericResponse<Page<InventoryTransaction>>> getAllItems(@RequestParam(defaultValue = "0") int pageNo,
                                                                   @RequestParam(defaultValue = "10") int pageSize) {
        Page<InventoryTransaction> inventoryTransactions = inventoryTransactionService.findAll(pageNo,pageSize);
        return new ResponseEntity<>(new GenericResponse<>("All transaction retrieved successfully", inventoryTransactions), HttpStatus.OK);
    }


    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> updateItem(@PathVariable Long id, @RequestBody InventoryTransaction inventoryTransaction) {
        inventoryTransaction.setId(id);
        inventoryTransactionService.update(inventoryTransaction);
        return new ResponseEntity<>(new GenericResponse<>("Transaction updated successfully"), HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> deleteItem(@PathVariable Long id) {
        inventoryTransactionService.delete(id);
        return new ResponseEntity<>(new GenericResponse<>("Transaction deleted successfully"), HttpStatus.OK);
    }
}
