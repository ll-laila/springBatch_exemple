package com.example.springbatch.config.partitioner;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;




public class StudentCsvPartitioner implements Partitioner {



    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitionMap = new HashMap<>();

        int totalLines = countLinesInCsv("src/main/resources/inputs/students2.csv");
        int partitionSize = totalLines / gridSize;

        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            context.put("minValue", i * partitionSize);
            context.put("maxValue", (i + 1) * partitionSize);
            context.put("partition_number", i + 1);
            partitionMap.put("partition" + i, context);
        }

        return partitionMap;
    }



    private int countLinesInCsv(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return (int) reader.lines().count() - 1;  // Exclure l'entête
        } catch (IOException e) {
            throw new RuntimeException("Error counting lines in CSV", e);
        }
    }


}
