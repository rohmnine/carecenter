package com.example.carecenter.controller;

import com.example.carecenter.entity.Meal;
import com.example.carecenter.service.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    @Autowired
    private MealService mealService;

    @GetMapping
    public List<Meal> getAllMeals() {
        return mealService.getAllMeals();
    }

    @GetMapping("/{id}")
    public Optional<Meal> getMealById(@PathVariable Long id) {
        return mealService.getMealById(id);
    }

    @PostMapping
    public Meal createMeal(@RequestBody Meal meal) {
        return mealService.createMeal(meal);
    }

    @DeleteMapping("/{id}")
    public void deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);
    }
}