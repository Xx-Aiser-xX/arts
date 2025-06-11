package org.example.arts;

import org.example.arts.controllers.impl.ArtController;
import org.example.arts.dtos.ArtCardDto;
import org.example.arts.entities.Art;
import org.example.arts.repo.ArtRepository;
import org.example.arts.services.ArtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootTest
class ArtsApplicationTests {

    private final ArtService artService;
    private final ArtController artController;
    private final ArtRepository artRepo;

    @Autowired
    ArtsApplicationTests(ArtService artService, ArtController artController, ArtRepository artRepo) {
        this.artService = artService;
        this.artController = artController;
        this.artRepo = artRepo;
    }

    @Test
    void contextLoads() {
        var list = artService.getFeed("latest", 1, 2);
        for (ArtCardDto art : list){
            System.out.println(art);
        }

//        var list = artRepo.getAll(false);
//        for (Art art : list){
//            System.out.println(art);
//        }
    }

}
