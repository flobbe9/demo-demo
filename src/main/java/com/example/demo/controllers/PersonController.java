package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.models.Person;
import com.example.demo.services.PersonService;


@RestController
@RequestMapping("/api/demo")
public class PersonController {

    @Autowired
    private PersonService personService;


    @GetMapping("/test")
    public String test() {

        return "Test. \n This page should have a USER restriction.";
    }

    
    @GetMapping("/viewUser")
    public String viewUser() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return "Currently logged in: " + username;
    }


    @PostMapping("/save")
    public ResponseEntity<Boolean> save(@RequestBody Person person) {

        return ResponseEntity.ok().body(personService.save(person));
    }
}