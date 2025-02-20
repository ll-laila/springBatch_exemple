package com.example.springbatch.config.processor;

import com.example.springbatch.model.Course;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;


@Component
public class CourseProcessor implements ItemProcessor<Course, Course> {


    @Override
    public Course process(Course course) throws Exception {
        return course;
    }
}
