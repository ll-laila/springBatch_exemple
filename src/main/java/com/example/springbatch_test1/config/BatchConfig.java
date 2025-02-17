package com.example.springbatch_test1.config;

import com.example.springbatch_test1.entity.*;
import com.example.springbatch_test1.repo.AcademicEmailRepository;
import com.example.springbatch_test1.repo.CourseRepository;
import com.example.springbatch_test1.repo.StudentRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
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

import java.io.*;
import java.util.Iterator;
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



    /******************************* Step 1 : from csv to db *************************************/

    /**
     * Ce étape automatise l'importation des données des étudiants à partir d'un fichier CSV :
     * 1. Lecture du fichier `students.csv`.
     * 2. Traitement et transformation des données.
     * 3. Stockage des données dans la table "student" en base de données.
     */


    @Bean
    public FlatFileItemReader<Student> csvReader() {
        FlatFileItemReader<Student> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/inputs/students.csv"));
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
        return new StepBuilder("CsvToDb", jobRepository)
                .<Student, Student>chunk(20, platformTransactionManager)
                .reader(csvReader())
                .processor(processor())
                .writer(writer())
                .build();
    }











    /******************************* Step 2 : from json to db *************************************/
    /**
     * Ce étape automatise l'importation des données des étudiants à partir d'un fichier Json :
     * 1. Lecture du fichier `students.json`.
     * 2. Traitement et transformation des données.
     * 3. Stockage des données dans la table `student` en base de données.
     */


    @Bean
    public JsonItemReader<Student> jsonReader() {
        JsonItemReader<Student> itemReader = new JsonItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/inputs/students.json"));
        itemReader.setJsonObjectReader(new JacksonJsonObjectReader<>(Student.class));
        return itemReader;
    }

    @Bean
    public Step step2() {
        return new StepBuilder("JsonToDb", jobRepository)
                .<Student, Student>chunk(20, platformTransactionManager)
                .reader(jsonReader())
                .processor(processor())
                .writer(writer())
                .build();
    }






    /******************************** Step 3 : excel to db **********************************/
    /**
     * Cet étape automatise l'importation des données des étudiants à partir d'un fichier Excel :
     * 1. Lecture du fichier `students.xlsx`.
     * 2. Traitement et transformation des données.
     * 3. Stockage des données dans la table `student` en base de données.
     */


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
    public RepositoryItemWriter<Student> excelWriter() {
        RepositoryItemWriter<Student> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step step3() {
        return new StepBuilder("ExcelToDb", jobRepository)
                .<Student, Student>chunk(10, platformTransactionManager)
                .reader(excelReader())
                .processor(processor())
                .writer(excelWriter())
                .build();
    }








    /******************************** Step 4 : db to xml **********************************/
    /**
     * Ce étape automatise l'importation des données de tout les étudiants à partir de la BD :
     * 1. Récuperation à partir de la table `student`.
     * 2. Traitement et transformation des données.
     * 3. Stockage des données dans `allStudents.xml`.
     */


    @Bean
    public StaxEventItemWriter<Student> xmlWriter3() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Student.class);

        StaxEventItemWriter<Student> writer = new StaxEventItemWriterBuilder<Student>()
                .name("AllStudentsWriter")
                .resource(new FileSystemResource("src/main/resources/outputs/allStudents.xml"))
                .marshaller(marshaller)
                .rootTagName("Students")
                .saveState(false)
                .build();

        writer.open(new ExecutionContext());

        return writer;
    }


    @Bean
    public Step step4() {
        return new StepBuilder("DbToXml", jobRepository)
                .<Student, Student>chunk(60, platformTransactionManager)
                .reader(studentDBReader())
                .processor(processor())
                .writer(xmlWriter3())
                .build();
    }





    /******************************** Step 5 : csv to xml **********************************/
    /**
     * Ce étape automatise l'importation des données des étudiants à partir d'un fichier CSV :
     * 1. Lecture du fichier `students.csv`.
     * 2. Traitement et transformation des données.
     * 3. Stockage des données dans `students.xml`.
     */


    @Bean
    public StaxEventItemWriter<Student> xmlWriter2() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Student.class);

        StaxEventItemWriter<Student> writer = new StaxEventItemWriterBuilder<Student>()
                .name("StudentsWriter")
                .resource(new FileSystemResource("src/main/resources/outputs/students.xml"))
                .marshaller(marshaller)
                .rootTagName("Students")
                .saveState(false)
                .build();

        writer.open(new ExecutionContext());

        return writer;
    }


    @Bean
    public Step step5() {
        return new StepBuilder("CsvToXml", jobRepository)
                .<Student, Student>chunk(20, platformTransactionManager)
                .reader(csvReader())
                .processor(processor())
                .writer(xmlWriter2())
                .build();
    }








    /******************************** Step 6 : from xml to db ************************************/
    /**
     * Ce étape automatise l'importation des données des cours à partir d'un fichier XML :
     * 1. Lecture du fichier `courses.xml`.
     * 2. Traitement et transformation des données.
     * 3. Stockage des données dans la table `courses` en BD.
     */


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
    public CourseProcessor xmlProcessor() {
        return new CourseProcessor();
    }


    @Bean
    public RepositoryItemWriter<Course> dbWriter() {
        RepositoryItemWriter<Course> writer = new RepositoryItemWriter<>();
        writer.setRepository(courseRepository);
        writer.setMethodName("save");
        return writer;
    }


    @Bean
    public Step step6() {
        return new StepBuilder("xmlToDB", jobRepository)
                .<Course, Course>chunk(3, platformTransactionManager)
                .reader(xmlReader())
                .processor(xmlProcessor())
                .writer(dbWriter())
                .build();
    }






    /******************************* Step 7 : from db to db *************************************/
    /**
     * Ce étape automatise l'importation des données des étudiants à partir de la BD :
     * 1. Récuperation à partir de la table `student`.
     * 2. Traitement et transformation des données (génération des email académique à partir de leurs noms).
     * 3. Stockage des données dans la table `academic-email` en BD.
     */


    @Bean
    public JpaPagingItemReader<Student> studentDBReader() {
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
    public RepositoryItemWriter<AcademicEmail> academicEmaiDBlWriter() {
        RepositoryItemWriter<AcademicEmail> writer = new RepositoryItemWriter<>();
        writer.setRepository(academicEmailRepository);
        writer.setMethodName("save");
        return writer;
    }


    @Bean
    public Step step7() {
        return new StepBuilder("emailGeneration", jobRepository)
                .<Student, AcademicEmail>chunk(6, platformTransactionManager)
                .reader(studentDBReader())
                .processor(studentToAcademicEmailProcessor())
                .writer(academicEmaiDBlWriter())
                .build();
    }






    /******************************* Step 8 : from db to json **************************************/
    /**
     * Ce étape automatise l'importation des données des étudiants à partir de la BD :
     * 1. Récuperation à partir de la table `student`.
     * 2. Traitement et transformation des données.
     * 3. Stockage des données dans `students_filiere.json`.
     */


    // Processor pour affecter les étudiants à une filière selon leur note generale
    @Bean
    public StudentFiliereProcessor studentFiliereDBProcessor() {
        return new StudentFiliereProcessor();
    }


    @Bean
    public JsonFileItemWriter<StudentFiliere> studentFiliereDBWriter() {
        return new JsonFileItemWriterBuilder<StudentFiliere>()
                .name("studentFiliereJsonWriter")
                .resource(new FileSystemResource("src/main/resources/outputs/students_filiere.json"))
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .build();
    }



    // Step pour effectuer le traitement d'affectation des étudiants aux filières
    @Bean
    public Step step8() {
        return new StepBuilder("dbToJson", jobRepository)
                .<Student, StudentFiliere>chunk(6, platformTransactionManager)
                .reader(studentDBReader())
                .processor(studentFiliereDBProcessor())
                .writer(studentFiliereDBWriter())
                .build();
    }





    /******************************** Step 9 : json to txt **********************************/
    /**
     * Ce étape automatise l'importation des données des étudiants et leurs filieres à partir du fichier JSON :
     * 1. Lecture du fichier `students_filiere.json`.
     * 2.
     * 3. Stockage des données dans `students_filiere.txt`.
     */

    @Bean
    public JsonItemReader<StudentFiliere> jsonFiliereReader() {
        JsonItemReader<StudentFiliere> itemReader = new JsonItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/outputs/students_filiere.json"));
        itemReader.setJsonObjectReader(new JacksonJsonObjectReader<>(StudentFiliere.class));
        return itemReader;
    }

    @Bean
    public ItemWriter<StudentFiliere> txtStudentFiliereWriter() {
        return items -> {
            File file = new File("src/main/resources/inputs/students_filiere.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (StudentFiliere studentFiliere : items) {
                    writer.write("ID: " + studentFiliere.getId() + ", ");
                    writer.write("Nom: " + studentFiliere.getFirstName() + " " + studentFiliere.getLastName() + ", ");
                    writer.write("Email: " + studentFiliere.getEmail() + ", ");
                    writer.write("Filière: " + studentFiliere.getFiliere() + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }



    @Bean
    public Step step9() {
        return new StepBuilder("jsonToTxt", jobRepository)
                .<StudentFiliere, StudentFiliere>chunk(40, platformTransactionManager)
                .reader(jsonFiliereReader())
                .writer(txtStudentFiliereWriter())
                .build();
    }





    /******************************** Step 10 : from txt to xml **********************************/
    /**
     * Ce étape automatise l'importation des données des étudiants et leurs filieres à partir de fichier TXT :
     * 1. Lecture du fichier `students_filiere.txt`.
     * 2.
     * 3. Stockage des données dans `students_filiere.xml`.
     */


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
    public StaxEventItemWriter<StudentFiliere> xmlWriter() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(StudentFiliere.class);

        StaxEventItemWriter<StudentFiliere> writer = new StaxEventItemWriterBuilder<StudentFiliere>()
                .name("StudentsFiliereWriter")
                .resource(new FileSystemResource("src/main/resources/outputs/students_filiere.xml"))
                .marshaller(marshaller)
                .rootTagName("StudentsFiliere")
                .saveState(false)
                .build();

        writer.open(new ExecutionContext());

        return writer;
    }



    @Bean
    public Step step10() {
        return new StepBuilder("txtToXml", jobRepository)
                .<StudentFiliere, StudentFiliere>chunk(40, platformTransactionManager)
                .reader(txtReader())
                .writer(xmlWriter())
                .build();
    }






    /******************************** Step 11 : from db to pdf **********************************/
    /**
     * Ce étape automatise l'importation des données des étudiants et leurs cours à partir de la BD :
     * 1. Récuperation à partir de la table `student`.
     * 2. Traitement et transformation des données.
     * 3. Stockage des données dans `course_students.pdf`.
     */



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
            File file = new File("src/main/resources/outputs/course_students.pdf");
            file.getParentFile().mkdirs();
            document.save(file);
            document.close();

            System.out.println("PDF généré avec succès.");
        };
    }



    @Bean
    public Step step11() {
        return new StepBuilder("dbToPdf", jobRepository)
                .<Student, CourseStudent>chunk(20, platformTransactionManager)
                .reader(studentDBReader())
                .processor(courseStudentProcessor())
                .writer(pdfCourseStudentWriter())
                .build();
    }





    /******************************* Job ****************************************/

    @Bean
    public Job runJob() {
        return new JobBuilder("job", jobRepository)
                .start(step1()) //  csv -> db   (infos students)
                .next(step2()) //   json -> db  (infos students)
                .next(step3()) //  excel to db  (infos students)
                .next(step4()) // xml -> db     (infos students)
                .next(step5()) // csv to xml    (infos students)
                .next(step6()) // db to xml     (infos courses)
                .next(step7()) //  db -> db     (email generation)
                .next(step8())  //  db -> json  (students affectations)
                .next(step9()) // json -> txt   (students affectations)
                .next(step10())  // txt -> xml  (students affectations)
                .next(step11()) //  db -> pdf   (students courses)
                .build();
    }




















}
