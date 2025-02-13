package com.example.springbatch_test1.config;

import com.example.springbatch_test1.entity.*;
import com.example.springbatch_test1.repo.AcademicEmailRepository;
import com.example.springbatch_test1.repo.CourseRepository;
import com.example.springbatch_test1.repo.CourseStudentRepository;
import com.example.springbatch_test1.repo.StudentRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;
import java.io.File;
import java.util.List;


@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private StudentRepository repository;

    @Autowired
    private AcademicEmailRepository academicEmailRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseStudentRepository courseStudentRepository;





    /******************************* Step 1 : from csv to db *************************************/

    @Bean
    public FlatFileItemReader<Student> reader() {
        FlatFileItemReader<Student> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/students.csv"));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }


    @Bean
    public StudentProcessor processor() {
        return new StudentProcessor();
    }


    @Bean
    public RepositoryItemWriter<Student> writer() {
        RepositoryItemWriter<Student> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
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
    public Step step1() {
        return new StepBuilder("ImportStudents", jobRepository)
                .<Student, Student>chunk(20, platformTransactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }





    /******************************* Step 2 : from db to db *************************************/

    @Bean
    public JpaPagingItemReader<Student> studentReader() {
        JpaPagingItemReader<Student> reader = new JpaPagingItemReader<>();
        reader.setQueryString("SELECT s FROM Student s");
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setPageSize(6);
        return reader;
    }


    @Bean
    public StudentToAcademicEmailProcessor studentToAcademicEmailProcessor() {
        return new StudentToAcademicEmailProcessor();
    }

    @Bean
    public RepositoryItemWriter<AcademicEmail> academicEmailWriter() {
        RepositoryItemWriter<AcademicEmail> writer = new RepositoryItemWriter<>();
        writer.setRepository(academicEmailRepository);
        writer.setMethodName("save");
        return writer;
    }


    @Bean
    public Step step2() {
        return new StepBuilder("emailGeneration", jobRepository)
                .<Student, AcademicEmail>chunk(6, platformTransactionManager)
                .reader(studentReader())
                .processor(studentToAcademicEmailProcessor())
                .writer(academicEmailWriter())
                .build();
    }






    /******************************* Step 3 : from db to json **************************************/


    // Processor pour affecter les étudiants à une filière selon leur note
    @Bean
    public StudentFiliereProcessor studentFiliereProcessor() {
        return new StudentFiliereProcessor();
    }



    @Bean
    public JsonFileItemWriter<StudentFiliere> studentFiliereWriter() {
        return new JsonFileItemWriterBuilder<StudentFiliere>()
                .name("studentFiliereJsonWriter")
                .resource(new FileSystemResource("src/main/resources/students_filiere.json"))
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .build();
    }



    // Step pour effectuer le traitement d'affectation des étudiants aux filières
    @Bean
    public Step step3() {
        return new StepBuilder("filiereAffectation", jobRepository)
                .<Student, StudentFiliere>chunk(6, platformTransactionManager)
                .reader(studentReader())
                .processor(studentFiliereProcessor())
                .writer(studentFiliereWriter())
                .build();
    }





    /******************************** Step 4 : from xml to db/xml ************************************/



    @Bean
    public StaxEventItemReader<Course> xmlReader() {
        Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
        unmarshaller.setClassesToBeBound(Course.class);
        return new StaxEventItemReaderBuilder<Course>()
                    .name("courseReader")
                    .resource(new ClassPathResource("courses.xml"))
                    .addFragmentRootElements("course")
                    .unmarshaller(unmarshaller)
                    .build();
    }


    @Bean
    public CourseProcessor xmlProcessor() {
        return new CourseProcessor();
    }



    @Bean
    public StaxEventItemWriter<Course> xmlWriter() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Course.class);
        return new StaxEventItemWriterBuilder<Course>()
                .name("courseWriter")
                .resource(new FileSystemResource("output.xml"))
                .marshaller(marshaller)
                .rootTagName("courses")
                .build();
   }


    @Bean
    public RepositoryItemWriter<Course> dbWriter() {
        RepositoryItemWriter<Course> writer = new RepositoryItemWriter<>();
        writer.setRepository(courseRepository);
        writer.setMethodName("save");
        return writer;
    }


    @Bean
    public Step step4() {
        return new StepBuilder("courses", jobRepository)
                .<Course, Course>chunk(3, platformTransactionManager)
                .reader(xmlReader())
                .processor(xmlProcessor())
               // .writer(xmlWriter())
                .writer(dbWriter())
                .build();
    }




    /******************************** Step 5 : from db to pdf **********************************/

    @Bean
    public CourseStudentProcessor courseStudentProcessor() {
        return new CourseStudentProcessor();
    }


    @Bean
    public ItemWriter<CourseStudent> pdfCourseStudentWriter() {
        return items -> {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            // Flux pour écrire du contenu dans le PDF
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
            contentStream.beginText();
            contentStream.setLeading(14.5f);
            contentStream.newLineAtOffset(50, 700); // Positionnement du texte au début de la page

            contentStream.showText("Liste des Étudiants et leurs Cours");
            contentStream.newLine();
            contentStream.newLine();

            // Ajouter chaque étudiant avec ses cours
            for (CourseStudent cs : items) {
                contentStream.showText("- Nom : " + cs.getStudentName());
                contentStream.newLine();
                contentStream.showText("- Filière : " + cs.getFiliere());
                contentStream.newLine();

                // Ajouter les cours de l'étudiant
                List<Course> courses = cs.getCourses();  // Liste des cours associés
                if (courses != null && !courses.isEmpty()) {
                    contentStream.showText("- Cours : ");
                    for (Course course : courses) {
                        contentStream.showText(course.getName() + " / ");
                    }
                    contentStream.newLine();
                } else {
                    contentStream.showText("Aucun cours associé.");
                    contentStream.newLine();
                }
                contentStream.newLine();

            }

            contentStream.endText();
            contentStream.close();

            // Sauvegarder le fichier PDF
            File file = new File("src/main/resources/course_students.pdf");
            file.getParentFile().mkdirs();
            document.save(file);
            document.close();

            System.out.println("PDF généré avec succès.");
        };
    }



    @Bean
    public Step step5() {
        return new StepBuilder("coursesStudent", jobRepository)
                .<Student, CourseStudent>chunk(20, platformTransactionManager)
                .reader(studentReader())
                .processor(courseStudentProcessor())
                .writer(pdfCourseStudentWriter())
                .build();
    }


    /******************************* Job ****************************************/

    @Bean
    public Job runJob() {
        return new JobBuilder("job", jobRepository)
                .start(step1()) // from csv to db
                .next(step2()) // from db to db
                .next(step3())  // from db to json
                .next(step4()) // from xml to db or xml
                .next(step5()) // from db to pdf
                .build();
    }






}
