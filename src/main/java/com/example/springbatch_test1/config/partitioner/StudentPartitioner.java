package com.example.springbatch_test1.config.partitioner;

import com.example.springbatch_test1.repo.StudentRepository;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;



public class StudentPartitioner implements Partitioner {

    @Autowired
    private StudentRepository studentRepository;



    @Override
    public Map<String, ExecutionContext> partition(int gridSize)
    {

        int min = studentRepository.findMinId();
        int max = studentRepository.findMaxId();

        int targetSize = (max - min) / gridSize + 1;

        Map<String, ExecutionContext> result = new HashMap<>();

        int number = 0;
        int start = min;
        int end = start + targetSize - 1;

        while (start <= max)
        {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if(end >= max) {
                end = max;
            }

            value.putInt("minValue", start);
            value.putInt("maxValue", end);
            value.putInt("partition_number", number);

            start += targetSize;
            end += targetSize;

            number++;
        }
        return result;
    }



}
