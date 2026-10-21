package lv.bootcamp.shelter.service;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;
import lv.bootcamp.shelter.service.data.ShelterReportData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ReportExportService {

    public void writeReport(Path outputPath, ShelterReportData reportData, ImportResult importResult) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath.toString(), StandardCharsets.UTF_8))) {
            bw.write("=== Report ==");
            bw.newLine();
            bw.write("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            bw.newLine();
            bw.write("Imported animals: " + importResult.allAnimals().size());
            bw.newLine();
            bw.write("Skipped rows: " + importResult.skippedRows());
            bw.newLine();
            bw.write("Unique species: " + String.join(", ", reportData.uniqueSpecies()));
            bw.newLine();

            bw.newLine();
            bw.write("Per-species breakdown: ");
            bw.newLine();
            for (Map.Entry<String, List<Animal>> entry : new TreeMap<>(reportData.animalsBySpecies()).entrySet()) {
                String names = entry.getValue().stream()
                        .map(Animal::getName)
                        .collect(Collectors.joining(", "));
                bw.write(entry.getKey() + ": " + names);
                bw.newLine();
                bw.write("  Total: " + entry.getValue().size());
                bw.newLine();
                bw.write("  Vaccinated: " + reportData.vaccinatedCounts().get(entry.getKey()));
                bw.newLine();
            }

            bw.newLine();
            bw.write("Oldest animal per-species");
            bw.newLine();
            for (Map.Entry<String, Optional<Animal>> entry : new TreeMap<>(reportData.oldestAnimalPerSpecies()).entrySet()) {
                if (entry.getValue().isPresent()) {
                    Animal animal = entry.getValue().get();
                    bw.write("  " + entry.getKey() + ": " + animal.getName() + " (" + animal.getAge().get() + " years)");
                    bw.newLine();
                }
            }

            bw.newLine();
            bw.write("Animals needing vet input:");
            bw.newLine();
            bw.write(reportData.animalsNeedingVetInput().stream()
                    .sorted(Comparator.comparing(
                            s -> s.substring(s.indexOf("(") + 1, s.indexOf(")"))
                    ))
                    .collect(Collectors.joining(", ")));
            bw.newLine();

        } catch (IOException e) {
            log.error("Could not write file {}", outputPath, e);
        }
    }
}
