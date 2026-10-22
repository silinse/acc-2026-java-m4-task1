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
            bw.write("=== Shelter Intake Report ===");
            bw.newLine();
            bw.write("Date generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            bw.newLine();
            bw.newLine();
            bw.write("Total imported: " + importResult.allAnimals().size());
            bw.newLine();
            bw.write("Total skipped:  " + importResult.skippedRows());
            bw.newLine();
            bw.write("Invalid rows:   " + importResult.invalidRows().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", ")));
            bw.newLine();
            bw.newLine();
            bw.write("--- Unique species ---");
            bw.newLine();
            bw.write(String.join(", ", reportData.uniqueSpecies()));
            bw.newLine();

            bw.newLine();
            bw.write("--- Per-species breakdown ---");
            bw.newLine();
            for (Map.Entry<String, List<Animal>> entry : new TreeMap<>(reportData.animalsBySpecies()).entrySet()) {
                bw.write(String.format("%-10s  %d total, %d vaccinated",
                        entry.getKey() + ": ", entry.getValue().size(),
                        reportData.vaccinatedCounts().getOrDefault(entry.getKey(), 0L)));
                bw.newLine();
            }

            bw.newLine();
            bw.write("--- Oldest animal per-species ---");
            bw.newLine();
            for (Map.Entry<String, Optional<Animal>> entry : new TreeMap<>(reportData.oldestAnimalPerSpecies()).entrySet()) {
                if (entry.getValue().isPresent()) {
                    Animal animal = entry.getValue().get();
                    bw.write(String.format("%-10s %s",
                            entry.getKey() + ": ", animal.getName() + " (age " + animal.getAge().get() + ")"));
                    bw.newLine();
                }
            }

            bw.newLine();
            bw.write("--- Needs Vet input to determine age ---");
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
