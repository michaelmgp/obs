package com.obs.purchase.service;
import com.obs.purchase.entity.Item;
import com.obs.purchase.exceptions.DuplicateObjectExceptions;
import com.obs.purchase.exceptions.NotFoundExceptions;
import com.obs.purchase.repository.ItemRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.Instant;
@Service
public class ItemService implements BaseService<Item>{
    @Autowired
    private ItemRepository itemRepository;


    @Override
    public Item save(Item item) {
        if(itemRepository.findByName(item.getName())!=null){
            throw new DuplicateObjectExceptions("Duplicate Error", "item already registered");
        }
        return itemRepository.save(item);
    }

    @Override
    public void delete(Long id) {
        Item item = itemRepository.findById(id).orElse(null);
        if(item==null){
            throw new NotFoundExceptions("not found error", "item not found");
        }
        item.setDeletedAt(Instant.now());
        itemRepository.save(item);
    }

    @Override
    public void update(Item item) {
        Item itemSaved = itemRepository.findById(item.getId()).orElse(null);
        if(itemSaved==null){
            throw new NotFoundExceptions("not found error", "item not found");
        }
        BeanUtils.copyProperties(item,itemSaved);
        itemRepository.save(itemSaved);
    }

    @Override
    public Item findById(Long id) {
        Item user = itemRepository.findById(id).orElse(null);
        if(user==null){
            throw new NotFoundExceptions("not found error", "item not found");
        }
        return user;
    }

    @Override
    public Page<Item> findAll(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return itemRepository.findAll(pageable);
    }
}
