package com.example.springbatch_test1.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.springbatch_test1.entity.Student;
import org.springframework.jdbc.core.RowMapper;

public class StudentRowMapper implements RowMapper<Student> {


    @Override
    public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Student(rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("age"),
                rs.getString("phone_number"),
                rs.getDouble("note_generale") );
    }
}