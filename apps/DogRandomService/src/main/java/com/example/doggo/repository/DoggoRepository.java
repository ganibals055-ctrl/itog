package com.example.doggo.repository;

import com.example.doggo.model.Doggo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoggoRepository extends JpaRepository<Doggo, Long> {
}