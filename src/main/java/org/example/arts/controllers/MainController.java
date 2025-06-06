package org.example.arts.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;

@RequestMapping("/main")
public interface MainController {

    @GetMapping("/")
    Objects pageMain();
}
