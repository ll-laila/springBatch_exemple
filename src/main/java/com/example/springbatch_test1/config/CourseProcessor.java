package com.example.springbatch_test1.config;

import com.example.springbatch_test1.entity.Course;
import org.springframework.batch.item.ItemProcessor;


public class CourseProcessor implements ItemProcessor<Course, Course> {


    @Override
    public Course process(Course course) throws Exception {
        return course;
    }
}
