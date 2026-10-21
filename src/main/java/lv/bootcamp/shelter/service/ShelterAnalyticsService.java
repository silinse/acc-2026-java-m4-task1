package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.util.*;
import java.util.stream.Collectors;

public class ShelterAnalyticsService {

    public ShelterReportData buildReportData(ImportResult importResult) {
        List<Animal> allAnimals = importResult.allAnimals();

        Set<String> uniqueSpecies = allAnimals.stream()
                .map(Animal::getSpecies)
                .collect(Collectors.toCollection(TreeSet::new));

        Map<String, List<Animal>> animalsBySpecies = allAnimals.stream()
                .collect(Collectors.groupingBy(Animal::getSpecies));

        List<String> animalsNeedingVetInput = allAnimals.stream()
                .filter(a -> !a.isVaccinated())
                .map(a -> a.getName() + "(" + a.getSpecies() + ")")
                .toList();

        Map<String, Long> vaccinatedCounts = allAnimals.stream()
                .filter(Animal::isVaccinated)
                .collect(Collectors.groupingBy(Animal::getSpecies, Collectors.counting()));

        Map<String, Long> unvaccinatedCounts = allAnimals.stream()
                .filter(a -> !a.isVaccinated())
                .collect(Collectors.groupingBy(Animal::getSpecies, Collectors.counting()));

        Map<String, Optional<Animal>> oldestAnimalPerSpecies = allAnimals.stream()
                .filter(a -> a.getAge().isPresent())
                .collect(Collectors.groupingBy(Animal::getSpecies, Collectors.maxBy(Comparator.comparing(a -> a.getAge().get()))));

        return new ShelterReportData(
                uniqueSpecies,
                animalsBySpecies,
                animalsNeedingVetInput,
                vaccinatedCounts,
                unvaccinatedCounts,
                oldestAnimalPerSpecies
        );
    }
}
