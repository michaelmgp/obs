package com.obs.purchase.controller;

import com.obs.purchase.entity.Order;
import com.obs.purchase.entity.dto.GenericResponse;
import com.obs.purchase.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<GenericResponse<Order>> createOrder(@RequestBody @Valid Order order) {
        Order savedOrder = orderService.save(order);
        return new ResponseEntity<>(new GenericResponse<>("Order created successfully", savedOrder), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<Order>> getOrderById(@PathVariable Long id) {
        Order order = orderService.findById(id);
        return new ResponseEntity<>(new GenericResponse<>("Order retrieved successfully", order), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<GenericResponse<Page<Order>>> getAllOrders(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Order> orders = orderService.findAll(pageNo, pageSize);
        return new ResponseEntity<>(new GenericResponse<>("All orders retrieved successfully", orders), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> updateOrder(@PathVariable Long id, @RequestBody @Valid Order order) {
        order.setId(id);  // Ensure the correct order is updated
        orderService.update(order);
        return new ResponseEntity<>(new GenericResponse<>("Order updated successfully"), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
        return new ResponseEntity<>(new GenericResponse<>("Order deleted successfully"), HttpStatus.OK);
    }
}
