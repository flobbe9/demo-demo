package com.example.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.models.AbstractEntity;
import com.example.demo.repositories.Dao;


@Service
public abstract class AbstractService <E extends AbstractEntity> {
    
    @Autowired
    private Dao<E> dao;


    public Dao<E> getDao() {

        return this.dao;
    }
}