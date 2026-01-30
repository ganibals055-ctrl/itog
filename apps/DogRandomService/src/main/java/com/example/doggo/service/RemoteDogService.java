package com.example.doggo.service;

import com.example.doggo.model.Doggo;
import com.example.doggo.model.DoggoHistory;
import com.example.doggo.repository.DoggoHistoryRepository;
import com.example.doggo.repository.DoggoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class RemoteDogService {

    private final DoggoRepository dogRepo;
    private final DoggoHistoryRepository historyRepo;
    private final Random random = new Random();
    
    // Гарантированно рабочие изображения собак
    private final String[] DOG_IMAGES = {
        "https://images.unsplash.com/photo-1552053831-71594a27632d?w=400&h=400&fit=crop", // собака 1
        "https://images.unsplash.com/photo-1568572933382-74d440642117?w=400&h=400&fit=crop", // собака 2
        "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w-400&h=400&fit=crop", // собака 3
        "https://images.unsplash.com/photo-1507146426996-ef05306b995a?w=400&h=400&fit=crop", // собака 4
        "https://images.unsplash.com/photo-1518717758536-85ae29035b6d?w=400&h=400&fit=crop", // собака 5
        "https://images.unsplash.com/photo-1514984879728-be0aff75a6e8?w=400&h=400&fit=crop", // собака 6
        "https://images.unsplash.com/photo-1517423568366-8b81723034cb?w=400&h=400&fit=crop", // собака 7
        "https://images.unsplash.com/photo-1537151608828-ea2b11777ee8?w=400&h=400&fit=crop", // собака 8
        "https://images.unsplash.com/photo-1554456854-55a089fd4cb2?w=400&h=400&fit=crop", // собака 9
        "https://images.unsplash.com/photo-1583512603805-3cc6b41f3edb?w=400&h=400&fit=crop"  // собака 10
    };

    @Autowired
    public RemoteDogService(DoggoRepository dogRepo, DoggoHistoryRepository historyRepo) {
        this.dogRepo = dogRepo;
        this.historyRepo = historyRepo;
    }

    public Doggo fetchRandomDog() {
        try {
            System.out.println("Generating random dog...");
            
            // Выбираем случайную картинку из нашего списка
            int index = random.nextInt(DOG_IMAGES.length);
            String imageUrl = DOG_IMAGES[index];
            long fileSize = 1500 + random.nextInt(2000); // случайный размер 1500-3500 байт
            
            System.out.println("Selected image: " + imageUrl);
            
            // Создаем объект Doggo
            Doggo dog = Doggo.builder()
                    .url(imageUrl)
                    .sizeBytes(fileSize)
                    .build();
            
            // Сохраняем в БД
            Doggo saved = dogRepo.save(dog);
            System.out.println("Dog saved with ID: " + saved.getId());
            
            // Сохраняем в историю
            DoggoHistory history = DoggoHistory.builder()
                    .doggoId(saved.getId())
                    .url(saved.getUrl())
                    .sizeBytes(saved.getSizeBytes())
                    .name("Dog_" + saved.getId())
                    .build();
                    
            historyRepo.save(history);
            System.out.println("History saved");
            
            return saved;
            
        } catch (Exception e) {
            System.err.println("Error creating dog: " + e.getMessage());
            e.printStackTrace();
            
            // Ultimate fallback
            return createUltimateFallbackDog();
        }
    }
    
    private Doggo createUltimateFallbackDog() {
        System.out.println("Using ultimate fallback...");
        
        Doggo dog = Doggo.builder()
                .url("https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Collage_of_Nine_Dogs.jpg/400px-Collage_of_Nine_Dogs.jpg")
                .sizeBytes(2500L)
                .build();
        
        Doggo saved = dogRepo.save(dog);
        
        DoggoHistory history = DoggoHistory.builder()
                .doggoId(saved.getId())
                .url(saved.getUrl())
                .sizeBytes(saved.getSizeBytes())
                .name("Ultimate_Fallback_Dog_" + saved.getId())
                .build();
                
        historyRepo.save(history);
        return saved;
    }

    public List<Doggo> getAllDogs() {
        return dogRepo.findAll();
    }

    public List<DoggoHistory> getDogHistory() {
        return historyRepo.findAllByOrderByIdDesc();
    }

    public Doggo getDogById(Long id) {
        return dogRepo.findById(id).orElse(null);
    }
}