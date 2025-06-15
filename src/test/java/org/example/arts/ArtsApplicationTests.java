package org.example.arts;

import org.example.arts.dtos.ArtCardDto;
import org.example.arts.repo.ArtRepository;
import org.example.arts.services.ArtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ArtsApplicationTests {

    private final ArtService artService;
    private final ArtRepository artRepo;

    @Autowired
    ArtsApplicationTests(ArtService artService, ArtRepository artRepo) {
        this.artService = artService;
        this.artRepo = artRepo;
    }

    @Test
    void contextLoads() {
        var list = artService.getFeed("latest", 1, 2);
        for (ArtCardDto art : list){
            System.out.println(art);
        }
    }

}
