package com.obs.purchase.service;

import org.springframework.data.domain.Page;

import java.util.List;

public interface BaseService<T> {
    T save(T t);
    void delete (Long id);

    void update (T t);

    T findById(Long id);

    Page<T> findAll(int pageNo, int pageSize);
}
