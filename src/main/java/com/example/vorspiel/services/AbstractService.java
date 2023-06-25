package com.example.vorspiel.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.vorspiel.entites.AbstractEntity;
import com.example.vorspiel.repositories.Dao;


@Service
public abstract class AbstractService <E extends AbstractEntity> {
    
    @Autowired
    private Dao<E> dao;


    public Dao<E> getDao() {

        return this.dao;
    }
}