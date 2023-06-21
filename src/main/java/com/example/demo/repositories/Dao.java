package com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.example.demo.models.AbstractEntity;


@NoRepositoryBean
public interface Dao <E extends AbstractEntity> extends JpaRepository<E, Long> {
    
}