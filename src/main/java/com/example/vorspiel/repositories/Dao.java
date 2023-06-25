package com.example.vorspiel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.example.vorspiel.entites.AbstractEntity;


@NoRepositoryBean
public interface Dao <E extends AbstractEntity> extends JpaRepository<E, Long> {
    
}