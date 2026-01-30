package com.example.doggo.repository;

import com.example.doggo.model.DoggoHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoggoHistoryRepository extends JpaRepository<DoggoHistory, Long> {
    List<DoggoHistory> findAllByOrderByIdDesc();
}
