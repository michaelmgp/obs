package com.obs.purchase.service;

import com.obs.purchase.entity.Inventory;
import com.obs.purchase.entity.InventoryTransaction;
import com.obs.purchase.entity.Item;
import com.obs.purchase.entity.Order;
import com.obs.purchase.enums.Type;
import com.obs.purchase.exceptions.InvalidRequest;
import com.obs.purchase.exceptions.NotFoundExceptions;
import com.obs.purchase.repository.InventoryRepository;
import com.obs.purchase.repository.InventoryTransactionRepository;
import com.obs.purchase.repository.ItemRepository;
import com.obs.purchase.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class OrderService implements BaseService<Order>{
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;


    @Autowired
    private InventoryTransactionService inventoryTransactionService;

    @Override
    @Transactional
    public Order save(Order order) {
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo(generateOrderNo());
        }
        Item item = itemRepository.findById(order.getItemId()).orElse(null);
        if(item==null){
            throw new NotFoundExceptions("not found error", "item is not found");
        }
        order.setPrice(item.getPrice());
        InventoryTransaction inventoryTransaction = new InventoryTransaction(order.getItemId(),order.getQty(),Type.WITHDRAWAL.getDisplayName());

        inventoryTransactionService.save(inventoryTransaction);
        return orderRepository.save(order);
    }

    @Override
    public void delete(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if(order==null){
            throw new NotFoundExceptions("not found error", "order transaction not found");
        }
        order.setDeletedAt(Instant.now());
        orderRepository.save(order);
    }

    @Override
    public void update(Order order) {
        Order orderSaved = orderRepository.findById(order.getId()).orElse(null);
        if(orderSaved==null){
            throw new NotFoundExceptions("not found error", "order transaction not found");
        }
        BeanUtils.copyProperties(order,orderSaved);
        orderRepository.save(orderSaved);
    }

    @Override
    public Order findById(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if(order==null){
            throw new NotFoundExceptions("not found error", "order not found");
        }
        return order;
    }

    @Override
    public Page<Order> findAll(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return orderRepository.findAll(pageable);    }

    private String generateOrderNo() {
        Optional<Order> lastOrder = orderRepository.findTopByOrderByIdDesc();

        if (lastOrder.isPresent()) {
            String lastOrderNo = lastOrder.get().getOrderNo();
            int lastOrderNumber = Integer.parseInt(lastOrderNo.substring(1)); // Remove the "O" prefix
            return "O" + (lastOrderNumber + 1);
        } else {
            return "O1";
        }
    }
}
