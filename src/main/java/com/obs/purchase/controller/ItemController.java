package com.obs.purchase.controller;
import com.obs.purchase.entity.Item;
import com.obs.purchase.entity.dto.GenericResponse;
import com.obs.purchase.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/items")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @PostMapping
    public ResponseEntity<GenericResponse<Item>> createItem(@RequestBody @Valid Item item) {
        Item savedItem = itemService.save(item);
        return new ResponseEntity<>(new GenericResponse<>("Item created successfully", savedItem), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<Item>> getItemById(@PathVariable Long id) {
        Item item = itemService.findById(id);
        return new ResponseEntity<>(new GenericResponse<>("Item retrieved successfully", item), HttpStatus.OK);
    }


    @GetMapping
    public ResponseEntity<GenericResponse<Page<Item>>> getAllItems(@RequestParam(defaultValue = "0") int pageNo,
                                                                   @RequestParam(defaultValue = "10") int pageSize) {
        Page<Item> items = itemService.findAll(pageNo,pageSize);
        return new ResponseEntity<>(new GenericResponse<>("All items retrieved successfully", items), HttpStatus.OK);
    }


    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> updateItem(@PathVariable Long id, @RequestBody Item item) {
        item.setId(id);  // Set the ID to ensure the correct item is updated
        itemService.update(item);
        return new ResponseEntity<>(new GenericResponse<>("Item updated successfully"), HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> deleteItem(@PathVariable Long id) {
        itemService.delete(id);
        return new ResponseEntity<>(new GenericResponse<>("Item deleted successfully"), HttpStatus.OK);
    }
}
