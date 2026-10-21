package lv.bootcamp.shelter.service;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class CsvImportService {

    public ImportResult importAnimals(Path inputPath) {
        log.info("Starting import from {}", inputPath);

        List<Animal> allAnimals = new ArrayList<>();
        int skippedCount = 0;

        try (BufferedReader br = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            String header = br.readLine(); // skp header row

            String line;
            while ((line = br.readLine()) != null) {
                String name;
                String species;
                Optional<Integer> age;
                Boolean vaccinated;
                LocalDate intakeDate;

                String[] parts = line.split(",", -1);
                if (parts.length != 5) {
                    log.warn("Skipping malformed row: {}", line);
                    skippedCount++;
                    continue;
                }

                if (parts[0].trim().isBlank()) {
                    log.warn("Skipping row with missing name: {}", line);
                    skippedCount++;
                    continue;
                }
                name = parts[0].trim();

                if (parts[1].trim().isBlank()) {
                    log.warn("Skipping row with missing species: {}", line);
                    skippedCount++;
                    continue;
                }
                species = parts[1].trim();

                if (parts[2].trim().isBlank()) {
                    age = Optional.empty();
                } else {
                    try {
                        age = Optional.of(Integer.parseInt(parts[2].trim()));
                    } catch (NumberFormatException e) {
                        log.warn("Skipping row with non-numeric age value: {}", line);
                        skippedCount++;
                        continue;
                    }
                }

                if (!Set.of("true", "false").contains(parts[3].trim().toLowerCase())) {
                    log.warn("Skipping row invalid vaccination data: {}", line);
                    skippedCount++;
                    continue;
                }
                vaccinated = Boolean.parseBoolean(parts[3].trim());

                try {
                    intakeDate = LocalDate.parse(parts[4].trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                } catch (DateTimeParseException e){
                    log.warn("Skipping row with non-parsable date: {}", line);
                    skippedCount++;
                    continue;
                }
                Animal animal = new Animal(name, species, age, vaccinated, intakeDate);

                allAnimals.add(animal);
            }

        } catch (IOException e) {
            log.error("Could not process file {}", inputPath);
        }

        return new ImportResult(allAnimals, skippedCount);
    }
}
