package com.example.carecenter.service;

import com.example.carecenter.entity.Meal;
import com.example.carecenter.repository.MealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MealService {

    @Autowired
    private MealRepository mealRepository;

    public List<Meal> getAllMeals() {
        return mealRepository.findAll();
    }

    public Optional<Meal> getMealById(Long id) {
        return mealRepository.findById(id);
    }

    public Meal createMeal(Meal meal) {
        return mealRepository.save(meal);
    }

    public void deleteMeal(Long id) {
        mealRepository.deleteById(id);
    }
}