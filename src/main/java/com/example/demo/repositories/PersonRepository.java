package com.example.demo.repositories;

import org.springframework.stereotype.Repository;

import com.example.demo.models.Person;


@Repository
public interface PersonRepository extends Dao<Person> {
    
}