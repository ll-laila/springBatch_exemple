package com.example.springbatch.config.writer;

import com.example.springbatch.model.*;
import com.example.springbatch.repository.AcademicEmailRepository;
import com.example.springbatch.repository.CourseRepository;
import com.example.springbatch.repository.StudentRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import javax.sql.DataSource;
import java.io.*;
import java.time.LocalDateTime;
import java.util.List;




@Configuration
public class WritersConfig {


    @Autowired
    private StudentRepository repository;

    @Autowired
    private AcademicEmailRepository academicEmailRepository;

    @Autowired
    private CourseRepository courseRepository;


    @Autowired
    private DataSource dataSource;



    @Bean
    public RepositoryItemWriter<Student> writer1() {
        RepositoryItemWriter<Student> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }




    @Bean
    public JdbcBatchItemWriter<Student> writer2() {
        JdbcBatchItemWriter<Student> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);

        writer.setSql("INSERT INTO student (id, first_name, last_name, age, email, phone_number, note_generale) " +
                "VALUES (:id, :firstName, :lastName, :age, :email, :phoneNumber, :noteGenerale) " +
                "ON DUPLICATE KEY UPDATE " +
                "first_name = VALUES(first_name), " +
                "last_name = VALUES(last_name), " +
                "age = VALUES(age), " +
                "email = VALUES(email), " +
                "phone_number = VALUES(phone_number), " +
                "note_generale = VALUES(note_generale)");

        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());

        return writer;
    }



    @Bean
    public RepositoryItemWriter<Course> dbWriter() {
        RepositoryItemWriter<Course> writer = new RepositoryItemWriter<>();
        writer.setRepository(courseRepository);
        writer.setMethodName("save");
        return writer;
    }




    @Bean
    public RepositoryItemWriter<Student> excelWriter() {
        RepositoryItemWriter<Student> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }




    @Bean
    public StaxEventItemWriter<StudentFiliere> xmlWriter1() {
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
    public JsonFileItemWriter<StudentFiliere> studentFiliereDBWriter() {
        return new JsonFileItemWriterBuilder<StudentFiliere>()
                .name("studentFiliereJsonWriter")
                .resource(new FileSystemResource("src/main/resources/outputs/students_filiere.json"))
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .build();
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
    public ItemWriter<CourseStudent> htmlCourseStudentWriter() {
        return items -> {
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<html><head><meta charset=\"UTF-8\">")
                    .append("<title>Liste des Étudiants et leurs Cours</title>")
                    .append("<style>")
                    .append("body { font-family: Arial, sans-serif; margin: 20px; }")
                    .append("h2 { color: #333; }")
                    .append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
                    .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }")
                    .append("th { background-color: #f4f4f4; }")
                    .append("</style></head><body>")
                    .append("<h2>Liste des Étudiants et leurs Cours</h2>")
                    .append("<table><tr><th>Nom</th><th>Filière</th><th>Cours</th></tr>");
            // Ajouter chaque étudiant avec ses cours
            for (CourseStudent cs : items) {
                htmlContent.append("<tr>")
                        .append("<td>").append(cs.getStudentName()).append("</td>")
                        .append("<td>").append(cs.getFiliere()).append("</td>")
                        .append("<td>");

                List<Course> courses = cs.getCourses();
                if (courses != null && !courses.isEmpty()) {
                    htmlContent.append(courses.stream()
                            .map(Course::getName)
                            .reduce((c1, c2) -> c1 + ", " + c2)
                            .orElse(""));
                } else {
                    htmlContent.append("Aucun cours associé.");
                }

                htmlContent.append("</td></tr>");
            }

            htmlContent.append("</table></body></html>");
            File file = new File("src/main/resources/outputs/course_students.html");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(htmlContent.toString());
            }

            System.out.println("Fichier HTML généré avec succès.");
        };
    }




    @Bean
    public RepositoryItemWriter<AcademicEmail> academicEmaiDBlWriter() {
        RepositoryItemWriter<AcademicEmail> writer = new RepositoryItemWriter<>();
        writer.setRepository(academicEmailRepository);
        writer.setMethodName("save");
        return writer;
    }



    @Bean
    public ItemWriter<Integer> studentCountWriter() {
        return items -> {
            int total = items.getItems().stream().mapToInt(Integer::intValue).sum();
            File file = new File("src/main/resources/outputs/students_count.txt");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write("[" + LocalDateTime.now() + "] Nombre total d'étudiants dans la BD: " + total + " \n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }





}
