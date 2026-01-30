package com.example.doggo.controller;

import com.example.doggo.model.Doggo;
import com.example.doggo.model.DoggoHistory;
import com.example.doggo.service.RemoteDogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dog")
public class DogController {

    private final RemoteDogService dogService;

    @Autowired
    public DogController(RemoteDogService dogService) {
        this.dogService = dogService;
    }

    @GetMapping(value = "/random", produces = MediaType.APPLICATION_JSON_VALUE)
    public Doggo getRandomDog() {
        return dogService.fetchRandomDog();
    }

    @GetMapping("/all")
    public List<Doggo> getAllDogs() {
        return dogService.getAllDogs();
    }

    @GetMapping("/history")
    public List<DoggoHistory> getHistory() {
        return dogService.getDogHistory();
    }

    @GetMapping("/{id}")
    public Doggo getDogById(@PathVariable("id") Long id) {
        return dogService.getDogById(id);
    }

}
