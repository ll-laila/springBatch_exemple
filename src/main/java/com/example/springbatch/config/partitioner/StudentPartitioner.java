package com.example.springbatch.config.partitioner;

import com.example.springbatch.repository.StudentRepository;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;



public class StudentPartitioner implements Partitioner {

    @Autowired
    private StudentRepository studentRepository;


    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int min = studentRepository.findMinId();
        int max = studentRepository.findMaxId();

        int targetSize = (max - min + 1) / gridSize;

        Map<String, ExecutionContext> result = new HashMap<>();

        int number = 0;
        int start = min;

        while (start <= max) {
            int end = Math.min(start + targetSize - 1, max);

            ExecutionContext value = new ExecutionContext();
            value.putInt("minValue", start);
            value.putInt("maxValue", end);
            value.putInt("partition_number", number);

            result.put("partition" + number, value);

            start = end + 1;
            number++;
        }
        return result;
    }


}
