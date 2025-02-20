package com.example.springbatch.config.reader;

import com.example.springbatch.mapper.StudentRowMapper;
import com.example.springbatch.model.Course;
import com.example.springbatch.model.Student;
import com.example.springbatch.model.StudentFiliere;
import jakarta.persistence.EntityManagerFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


@Configuration
public class ReadersConfig {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private DataSource dataSource;


    @Bean
    public FlatFileItemReader<Student> csvReader() {
        FlatFileItemReader<Student> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/inputs/students.csv"));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    private LineMapper<Student> lineMapper() {
        DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id","firstName","lastName","age","email","phoneNumber","noteGenerale");

        BeanWrapperFieldSetMapper<Student> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Student.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }





    @Bean
    public JsonItemReader<Student> jsonReader() {
        JsonItemReader<Student> itemReader = new JsonItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/inputs/students.json"));
        itemReader.setJsonObjectReader(new JacksonJsonObjectReader<>(Student.class));
        return itemReader;
    }




    @Bean
    public ItemReader<Student> excelReader() {
        AbstractItemCountingItemStreamItemReader<Student> reader = new AbstractItemCountingItemStreamItemReader<Student>() {
            private Iterator<Row> rowIterator;

            @Override
            protected void doOpen() throws Exception {
                FileInputStream file = new FileInputStream(new File("src/main/resources/inputs/students.xlsx"));
                Workbook workbook = WorkbookFactory.create(file);
                Sheet sheet = workbook.getSheetAt(0);
                rowIterator = sheet.iterator();
                rowIterator.next(); // Skip header
            }

            @Override
            protected Student doRead() {
                if (rowIterator != null && rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Student student = new Student();
                    student.setId((int) row.getCell(0).getNumericCellValue());
                    student.setFirstName(row.getCell(1).getStringCellValue());
                    student.setLastName(row.getCell(2).getStringCellValue());
                    student.setAge(String.valueOf((int) row.getCell(3).getNumericCellValue()));
                    student.setEmail(row.getCell(4).getStringCellValue());
                    student.setPhoneNumber(row.getCell(5).getStringCellValue());
                    student.setNoteGenerale(row.getCell(6).getNumericCellValue());
                    return student;
                }
                return null;
            }

            @Override
            protected void doClose() {}
        };
        reader.setName("excelReader");
        return reader;
    }



    @Bean
    public StaxEventItemReader<Course> xmlReader() {
        Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
        unmarshaller.setClassesToBeBound(Course.class);
        return new StaxEventItemReaderBuilder<Course>()
                .name("courseReader")
                .resource(new ClassPathResource("inputs/courses.xml"))
                .addFragmentRootElements("course")
                .unmarshaller(unmarshaller)
                .build();
    }


    @Bean
    public JpaPagingItemReader<Student> studentDBReader() {
        JpaPagingItemReader<Student> reader = new JpaPagingItemReader<>();
        reader.setQueryString("SELECT s FROM Student s");
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setPageSize(6);
        return reader;
    }


    @Bean
    public JsonItemReader<StudentFiliere> jsonFiliereReader() {
        JsonItemReader<StudentFiliere> itemReader = new JsonItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/outputs/students_filiere.json"));
        itemReader.setJsonObjectReader(new JacksonJsonObjectReader<>(StudentFiliere.class));
        return itemReader;
    }


    @Bean
    public FlatFileItemReader<StudentFiliere> txtReader() {
        FlatFileItemReader<StudentFiliere> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource("src/main/resources/inputs/students_filiere.txt"));
        reader.setLineMapper(new LineMapper<StudentFiliere>() {
            @Override
            public StudentFiliere mapLine(String line, int lineNumber) throws Exception {
                System.out.println("Lecture de la ligne : " + line);
                String[] fields = line.split(",\\s*");
                String studentId = fields[0].split(":")[1].trim();
                String studentName = fields[1].split(":")[1].trim();
                String email = fields[2].split(":")[1].trim();
                String filiere = fields[3].split(":")[1].trim();

                StudentFiliere student = new StudentFiliere(studentId, studentName, email, filiere);
                System.out.println("Objet créé : " + student);
                return student;
            }
        });
        return reader;
    }



    @Bean
    @StepScope
    public FlatFileItemReader<Student> partitionedCsvItemReader(
            @Value("#{stepExecutionContext['minValue']}") Long minValue,
            @Value("#{stepExecutionContext['maxValue']}") Long maxValue,
            @Value("#{stepExecutionContext['partition_number']}") Long partitionNumber) {

        System.out.println("Partition " + partitionNumber + " ,reading from " + minValue + " to " + maxValue);

        FlatFileItemReader<Student> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource("src/main/resources/inputs/students2.csv"));
        reader.setLinesToSkip(1);

        DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("id", "firstName", "lastName", "age", "email", "phoneNumber", "noteGenerale");

        BeanWrapperFieldSetMapper<Student> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Student.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        reader.setLineMapper(lineMapper);

        return reader;
    }


    @Bean
    @StepScope
    public JdbcPagingItemReader<Student> pagingItemReader(
            @Value("#{stepExecutionContext['minValue']}") Long minValue,
            @Value("#{stepExecutionContext['maxValue']}") Long maxValue,
            @Value("#{stepExecutionContext['partition_number']}") Long partitionNumber) {

        System.out.println("Partition " + partitionNumber + " reading from " + minValue + " to " + maxValue);

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, first_name, last_name, age, email, phone_number, note_generale");
        queryProvider.setFromClause("FROM student");
        queryProvider.setWhereClause("WHERE id BETWEEN :minValue AND :maxValue");
        queryProvider.setSortKeys(sortKeys);

        JdbcPagingItemReader<Student> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(this.dataSource);
        reader.setFetchSize(1000);
        reader.setRowMapper(new StudentRowMapper());
        reader.setQueryProvider(queryProvider);

        // Ajout des paramètres à la requête
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("minValue", minValue);
        parameterValues.put("maxValue", maxValue);
        reader.setParameterValues(parameterValues);

        return reader;
    }






}
