package com.oauth.pizzaservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class PizzaController {

    @PreAuthorize("hasAuthority('SCOPE_api.read')")
    @GetMapping("/api/pizzas")
    public List<Map<String, Object>> getPizzas() {

        return List.of(
                Map.of("id", 1, "name", "Pepperoni"),
                Map.of("id", 2, "name", "Margherita"),
                Map.of("id", 3, "name", "Veggie")
        );
    }
}