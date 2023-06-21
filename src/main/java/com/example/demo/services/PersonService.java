package com.example.demo.services;

import org.springframework.stereotype.Service;

import com.example.demo.models.Person;


@Service
public class PersonService extends AbstractService<Person> {
    
    public String test() {

        return "this is the person service";
    }


    public boolean save(Person person) {
        
        super.getDao().save(person);

        return true;
    }
}