package com.altinntech.clicksave;

import com.altinntech.clicksave.examples.dto.DateResponse;
import com.altinntech.clicksave.examples.dto.ExampleResponse;
import com.altinntech.clicksave.examples.dto.PersonResponse;
import com.altinntech.clicksave.examples.entity.Gender;
import com.altinntech.clicksave.examples.entity.Job;
import com.altinntech.clicksave.examples.entity.Person;
import com.altinntech.clicksave.examples.repository.JpaPersonRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ClickSaveConfiguration.class)
public class ClicksaveTests {

    @Autowired
    private JpaPersonRepository jpaPersonRepository;

    private Person TEST_PERSON_1;
    private Person TEST_PERSON_2;
    private Person TEST_PERSON_3;
    private Person TEST_PERSON_4;
    private Person TEST_PERSON_5;

    @BeforeEach
    void setUp() throws IOException {
        jpaPersonRepository.deleteAll();
        TEST_PERSON_1 = new Person(null, "John", "Doe", 30, "some_address", Gender.MALE, Job.THREE_D_ARTIST, null);
        TEST_PERSON_2 = new Person(null, "Kent", "Martinez", 28, "some_address_2", Gender.MALE, Job.PROGRAMMER, null);
        TEST_PERSON_3 = new Person(null, "Zachary", "Daniels", 41, "some_address_3", Gender.MALE, Job.HR, null);
        TEST_PERSON_4 = new Person(null, "Brenda", "Cox", 28, "some_address_4", Gender.FEMALE, Job.QA, null);
        TEST_PERSON_5 = new Person(null, "Nancy", "Cruz", 34, "some_address_5", Gender.FEMALE, Job.PROGRAMMER, null);
    }

    @AfterEach
    void after() {
        //jpaPersonRepository.deleteAll();
    }

    @Test
    public void contextLoads() {
    }

    @Test
    void save() throws InterruptedException, ExecutionException {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        Future<Person> personFuture = jpaPersonRepository.saveAsync(TEST_PERSON_3);
        assertNotNull(TEST_PERSON_1.getId());
        assertNotNull(TEST_PERSON_2.getId());
        assertNotNull(personFuture.get().getId());
    }

    @Test
    void update() throws InterruptedException {
        jpaPersonRepository.save(TEST_PERSON_1);
        Optional<Person> fetched = jpaPersonRepository.findById(TEST_PERSON_1.getId());
        assertTrue(fetched.isPresent());
        Person person = fetched.get();
        person.setAge(40);
        jpaPersonRepository.save(person);
        Thread.sleep(500);
        Optional<Person> fetched2 = jpaPersonRepository.findById(person.getId());
        assertTrue(fetched2.isPresent());
        assertEquals(person, fetched2.get());
    }

    @Test
    void find() {
        jpaPersonRepository.save(TEST_PERSON_1);
        Optional<Person> fetched = jpaPersonRepository.findById(TEST_PERSON_1.getId());
        assertTrue(fetched.isPresent());
        assertEquals(TEST_PERSON_1, fetched.get());
    }

    @Test
    void delete() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.delete(TEST_PERSON_1);
        assertNull(TEST_PERSON_1.getId());
    }

    @Test
    void findByName() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        Optional<Person> fetched = jpaPersonRepository.findByName(TEST_PERSON_1.getName());
        assertTrue(fetched.isPresent());
        assertEquals(TEST_PERSON_1, fetched.get());
    }

    @Test
    void findAllByJob() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        List<Person> fetched = jpaPersonRepository.findAllByJob(Job.PROGRAMMER);
        assertEquals(2, fetched.size());
    }

    @Test
    void findByEnum() {
        jpaPersonRepository.save(TEST_PERSON_1);
        List<Person> fetchedById = jpaPersonRepository.findAllByJob(Job.THREE_D_ARTIST);
        List<Person> fetchedByString = jpaPersonRepository.findAllByGender(Gender.MALE);
        assertEquals(1, fetchedById.size());
        assertEquals(1, fetchedByString.size());
        Person personOne = fetchedById.get(0);
        Person personTwo = fetchedById.get(0);
        assertEquals(personOne, TEST_PERSON_1);
        assertEquals(personTwo, TEST_PERSON_1);
    }

    @Test
    void findAllByGender() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        List<Person> fetched = jpaPersonRepository.findAllByGender(Gender.MALE);
        assertEquals(3, fetched.size());
    }

    @Test
    void findAllByAge() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        List<Person> fetched = jpaPersonRepository.findAllByAge(28);
        assertEquals(2, fetched.size());
    }

    @Test
    void findByNameAndLastName() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        Optional<Person> fetched = jpaPersonRepository.findByNameAndLastName(TEST_PERSON_1.getName(), TEST_PERSON_1.getLastName());
        assertTrue(fetched.isPresent());
        assertEquals(TEST_PERSON_1, fetched.get());
    }

    @Test
    void findByNameAndLastNameOrAddress() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        Optional<Person> fetched = jpaPersonRepository.findByNameAndLastNameOrAddress(TEST_PERSON_1.getName(), TEST_PERSON_1.getLastName(), "address");
        assertTrue(fetched.isPresent());
        assertEquals(TEST_PERSON_1, fetched.get());

        Optional<Person> fetched_another = jpaPersonRepository.findByNameAndLastNameOrAddress("Name", "LastName", "some_address_2");
        assertTrue(fetched_another.isPresent());
        assertEquals(TEST_PERSON_2, fetched_another.get());
    }

    @Test
    void findByAgeAndName() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        Optional<Person> fetched = jpaPersonRepository.findByAgeAndName(TEST_PERSON_1.getAge(), TEST_PERSON_1.getName());
        assertTrue(fetched.isPresent());
        assertEquals(TEST_PERSON_1, fetched.get());
    }

    @Test
    void findAll() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        List<Person> fetched = jpaPersonRepository.findAll();
        assertEquals(5, fetched.size());
    }

    @Test
    void annotationBasedQuery() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        Optional<Person> fetched = jpaPersonRepository.annotationBasedQuery(TEST_PERSON_3.getName(), TEST_PERSON_3.getAge());
        assertTrue(fetched.isPresent());
        assertEquals(TEST_PERSON_3, fetched.get());
    }

    @Test
    void annotationBasedQuery_Projection() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        PersonResponse personResponse = PersonResponse.create(TEST_PERSON_3);
        Optional<PersonResponse> fetched = jpaPersonRepository.annotationBasedQueryProjection(TEST_PERSON_3.getName(), TEST_PERSON_3.getAge());
        assertTrue(fetched.isPresent());
        assertEquals(personResponse, fetched.get());
    }

    @Test
    void annotationBasedQuery_NotAllFields() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        Optional<Person> fetched = jpaPersonRepository.annotationBasedQueryNotAllFields(TEST_PERSON_3.getName(), TEST_PERSON_3.getAge());
        assertTrue(fetched.isPresent());
        assertEquals(TEST_PERSON_3.getName(), fetched.get().getName());
    }

    @Test
    void annotationBasedQuery_Projection_NotAllFields() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        PersonResponse personResponse = new PersonResponse();
        personResponse.setSome_name(TEST_PERSON_3.getName());
        Optional<PersonResponse> fetched = jpaPersonRepository.annotationBasedQueryProjectionNotAllFields(TEST_PERSON_3.getName(), TEST_PERSON_3.getAge());
        assertTrue(fetched.isPresent());
        assertEquals(personResponse, fetched.get());
    }

    @Test
    void annotationBasedQuery_Projection_FieldsOverloads() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        PersonResponse personResponse = PersonResponse.create(TEST_PERSON_3);
        Optional<PersonResponse> fetched = jpaPersonRepository.annotationBasedQueryProjectionFieldsOverload(TEST_PERSON_3.getName(), TEST_PERSON_3.getAge());
        assertTrue(fetched.isPresent());
        assertEquals(personResponse, fetched.get());
    }

    @Test
    void annotationBasedQueryFindAll_Projection_FieldsOverload() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        List<PersonResponse> fetched = jpaPersonRepository.annotationBasedQueryFindAll_Projection_FieldsOverload();
        assertEquals(5, fetched.size());
    }

    @Test
    void aggregateQuery_SumAgeByGender() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        ExampleResponse males = new ExampleResponse(99L, Gender.MALE, null);
        ExampleResponse females = new ExampleResponse(62L, Gender.FEMALE, null);
        List<ExampleResponse> fetched = jpaPersonRepository.aggregationSumAgeByGender();
        assertEquals(2, fetched.size());
        assertEquals(males, fetched.get(0));
        assertEquals(females, fetched.get(1));
    }

    @Test
    void findAllByName() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        List<Person> fetched = jpaPersonRepository.findAllByName(TEST_PERSON_1.getName());
        assertEquals(TEST_PERSON_1, fetched.get(0));
    }

    @Test
    void findAllByNameAndAge() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        List<Person> fetched = jpaPersonRepository.findAllByNameAndAge(TEST_PERSON_1.getName(), TEST_PERSON_1.getAge());
        assertEquals(TEST_PERSON_1, fetched.get(0));
    }

    @Test
    void findAllByName_settable() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);

        String query = "SELECT * FROM person WHERE name = ?";
        List<Person> fetched = jpaPersonRepository.findAllByName_Settable(Person.class, query, TEST_PERSON_1.getName());
        assertEquals(TEST_PERSON_1, fetched.get(0));

        query = "SELECT * FROM person WHERE name = ?";
        fetched = jpaPersonRepository.findAllByName_Settable(Person.class, query, TEST_PERSON_2.getName());
        assertEquals(TEST_PERSON_2, fetched.get(0));
    }

    @Test
    void customReplaceableQuery() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);

        String query = "SELECT * FROM person WHERE name = ?";
        List<Person> fetched = jpaPersonRepository.findAllCustomQuery(Person.class, query, TEST_PERSON_1.getName());
        assertEquals(TEST_PERSON_1, fetched.get(0));

        query = "SELECT * FROM person WHERE name = ?";
        fetched = jpaPersonRepository.findAllCustomQuery(Person.class, query, TEST_PERSON_2.getName());
        assertEquals(TEST_PERSON_2, fetched.get(0));

        query = "SELECT * FROM person WHERE name = ? AND age = ?";
        fetched = jpaPersonRepository.findAllCustomQuery(Person.class, query, TEST_PERSON_3.getName(), TEST_PERSON_3.getAge());
        assertEquals(TEST_PERSON_3, fetched.get(0));
    }

    @Test
    void customReplaceableQuery_projection() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);

        PersonResponse expected = PersonResponse.create(TEST_PERSON_1);
        String query = "SELECT * FROM person WHERE name = ?";
        List<PersonResponse> fetched = jpaPersonRepository.findAllCustomQuery(PersonResponse.class, query, TEST_PERSON_1.getName());
        assertEquals(expected, fetched.get(0));
    }

    @Test
    void customReplaceableQuery_projection_findAllByAge() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);

        String query = "SELECT * FROM person WHERE age > ?";
        List<PersonResponse> fetched = jpaPersonRepository.findAllCustomQuery(PersonResponse.class, query, 30);
        assertEquals(2, fetched.size());
    }

    @Test
    void customReplaceableQuery_projection_findAllByAgeAndGender() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);

        String query = "SELECT * FROM person WHERE age > ? AND gender = ?";
        List<PersonResponse> fetched = jpaPersonRepository.findAllCustomQuery(PersonResponse.class, query,
                20, Gender.MALE.toString());
        assertEquals(3, fetched.size());
    }

    @Test
    void customReplaceableQuery_aggregateQuery_SumAgeAvgExpByGender() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);

        ExampleResponse males = new ExampleResponse(99L, Gender.MALE, ((TEST_PERSON_1.getEmployeeInfo().getExperience()
                + TEST_PERSON_2.getEmployeeInfo().getExperience()
                + TEST_PERSON_3.getEmployeeInfo().getExperience()) / 3));
        ExampleResponse females = new ExampleResponse(62L, Gender.FEMALE, ((TEST_PERSON_4.getEmployeeInfo().getExperience()
                + TEST_PERSON_5.getEmployeeInfo().getExperience()) / 2));

        String query = "SELECT gender, sum(age) AS age, avg(experience) AS experience FROM person GROUP BY gender";
        List<ExampleResponse> fetched = jpaPersonRepository.findAllCustomQuery(ExampleResponse.class, query);
        assertEquals(2, fetched.size());
        assertEquals(males, fetched.get(0));
        assertEquals(females, fetched.get(1));
    }

    @Test
    void customReplaceableQuery_aggregateQuery_SumAgeAvgExpByGenderAndJob() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);

        ExampleResponse males = new ExampleResponse(28L, Gender.MALE, TEST_PERSON_2.getEmployeeInfo().getExperience());
        ExampleResponse females = new ExampleResponse(34L, Gender.FEMALE, TEST_PERSON_5.getEmployeeInfo().getExperience());

        String query = "SELECT gender, sum(age) AS age, avg(experience) AS experience FROM person WHERE job = ? GROUP BY gender";
        List<ExampleResponse> fetched = jpaPersonRepository.findAllCustomQuery(ExampleResponse.class, query, Job.PROGRAMMER.getId());
        assertEquals(2, fetched.size());
        assertEquals(males, fetched.get(0));
        assertEquals(females, fetched.get(1));
    }

    @Test
    void customReplaceableQuery_aggregateQuery_count() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);

        String query = "SELECT count(DISTINCT id) AS count FROM person WHERE job = ?";
        List<ExampleResponse> fetched = jpaPersonRepository.findAllCustomQuery(ExampleResponse.class, query, Job.PROGRAMMER.getId());
        assertEquals(1, fetched.size());
        assertEquals(2L, fetched.get(0).getCount());
    }

    @Test
    void customReplaceableQuery_dateTest() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);

        String query = "SELECT timestamp, toDateTime(timestamp) AS sqlTimestamp, toDate(timestamp) AS date FROM person WHERE id = ?";
        List<DateResponse> fetched = jpaPersonRepository.findAllCustomQuery(DateResponse.class, query, TEST_PERSON_1.getId());
        assertEquals(1, fetched.size());
        assertEquals(TEST_PERSON_1.getTimestamp(), fetched.get(0).getLocalDateTime());
        assertEquals(TEST_PERSON_1.getTimestamp().withNano(0), fetched.get(0).getLocalDateSqlTimestamp());
        assertEquals(TEST_PERSON_1.getTimestamp().toLocalDate(), fetched.get(0).getLocalDate());
    }

    @Test
    void customReplaceableQuery_paramsOverload() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);

        String query = "SELECT * FROM person WHERE position(name, ?) > 0";
        List<Person> fetched = jpaPersonRepository.findAllCustomQuery(Person.class, query, null, "Jo", TEST_PERSON_1.getLastName(), TEST_PERSON_1.getGender(), null, null, null);
        assertEquals(TEST_PERSON_1, fetched.get(0));
    }

    @Test
    void aggregateQuery_SumAgeAvgExpByGender() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        jpaPersonRepository.save(TEST_PERSON_4);
        jpaPersonRepository.save(TEST_PERSON_5);
        ExampleResponse males = new ExampleResponse(99L, Gender.MALE, ((TEST_PERSON_1.getEmployeeInfo().getExperience()
                        + TEST_PERSON_2.getEmployeeInfo().getExperience()
                        + TEST_PERSON_3.getEmployeeInfo().getExperience()) / 3));
        ExampleResponse females = new ExampleResponse(62L, Gender.FEMALE, ((TEST_PERSON_4.getEmployeeInfo().getExperience()
                + TEST_PERSON_5.getEmployeeInfo().getExperience()) / 2));
        List<ExampleResponse> fetched = jpaPersonRepository.aggregationSumAgeAvgExpByGender();
        assertEquals(2, fetched.size());
        assertEquals(males, fetched.get(0));
        assertEquals(females, fetched.get(1));
    }

    @Test
    void count() {
        jpaPersonRepository.save(TEST_PERSON_1);
        jpaPersonRepository.save(TEST_PERSON_2);
        jpaPersonRepository.save(TEST_PERSON_3);
        long expected = 3L;
        long actual = jpaPersonRepository.count();
        assertEquals(expected, actual);
    }

    @Test
    void prePersist() {
        jpaPersonRepository.save(TEST_PERSON_1);
        assertTrue(TEST_PERSON_1.isTestFieldForPrePersist());
    }

    @Test
    void preUpdate() {
        jpaPersonRepository.save(TEST_PERSON_1);
        TEST_PERSON_1.setAddress("New address");
        jpaPersonRepository.save(TEST_PERSON_1);
        assertTrue(TEST_PERSON_1.isTestFieldForPreUpdate());
    }

    @Test
    void postLoad() {
        jpaPersonRepository.save(TEST_PERSON_1);
        Optional<Person> fetched = jpaPersonRepository.findById(TEST_PERSON_1.getId());
        assertTrue(fetched.isPresent());
        assertTrue(fetched.get().isTestFieldForPostLoad());
    }

    @Test
    void multipleSaving() throws IOException, InterruptedException {
        int iterations = 1000;
        List<Person> persons = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            Person person = Person.buildMockPerson();
            persons.add(person);
        }
        long startTime = System.nanoTime();
        for (Person person : persons) {
            jpaPersonRepository.save(person);
        }
        long endTime = System.nanoTime();
        double executionTime =  (endTime - startTime) / 1_000_000.0;

        List<Person> fetched = jpaPersonRepository.findAll();
        assertEquals(iterations, fetched.size());
        System.out.println("Time to saving: " + executionTime);
        System.out.println("Entities per second: " + Math.floor((iterations / (executionTime / 1000.0))) + " e/s");
    }

    @Disabled
    @Test
    void saveFindStressTest() throws IOException {
        int iterations = 10_000;
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            Person person = Person.buildMockPerson();
            jpaPersonRepository.save(person);
            jpaPersonRepository.findById(person.getId());
        }
        long endTime = System.nanoTime();
        double executionTime =  (endTime - startTime) / 1_000_000.0;
        System.out.println("Time to saving: " + executionTime);
        System.out.println("Entities per second: " + Math.floor((iterations / (executionTime / 1000.0))) + " e/s");
    }

    @Disabled
    @Test
    void saveStressTest() throws IOException {
        int iterations = 100_000;
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            Person person = Person.buildMockPerson();
            jpaPersonRepository.saveAsync(person);
        }
        long endTime = System.nanoTime();
        double executionTime =  (endTime - startTime) / 1_000_000.0;
        System.out.println("Time to saving: " + executionTime);
        System.out.println("Entities per second: " + Math.floor((iterations / (executionTime / 1000.0))) + " e/s");
    }

    @Disabled
    @Test
    void saveOneMStressTest() throws IOException {
        int iterations = 1_000_000;
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            Person person = Person.buildMockPerson();
            jpaPersonRepository.saveAsync(person);
        }
        long endTime = System.nanoTime();
        double executionTime =  (endTime - startTime) / 1_000_000.0;
        System.out.println("Time to saving: " + executionTime);
        System.out.println("Entities per second: " + Math.floor((iterations / (executionTime / 1000.0))) + " e/s");
    }

    @Test
    void projectionMapping() {
        jpaPersonRepository.save(TEST_PERSON_1);
        PersonResponse personResponseExpected = PersonResponse.create(TEST_PERSON_1);

        Optional<PersonResponse> personResponse = jpaPersonRepository.findByLastName(TEST_PERSON_1.getLastName());
        assertTrue(personResponse.isPresent());
        assertEquals(personResponseExpected, personResponse.get());
    }

    @Test
    void performanceTest() {
        double maxTimeForSaveOperation = 0.2; // ms
        double maxTimeForUpdateOperation = 10.0; // ms
        double maxTimeForDeleteOperation = 14.0; // ms
        double maxTimeForFindOperation = 2.7; // ms
        double maxTimeForCustomFindOperation = 2.75; // ms

        double epsilon = 3.0; // ms

        double avgSaveTime = 0.0;
        double avgUpdateTime = 0.0;
        double avgDeleteTime = 0.0;
        double avgFindTime = 0.0;
        double avgCustomFindTime = 0.0;

        int iterations = 10;

        long startTime;
        long endTime;
        double executionTime;

        for (int i = 0; i < iterations; i++) {
            // -----save----- //
            startTime = System.nanoTime();
            jpaPersonRepository.save(TEST_PERSON_1);
            endTime = System.nanoTime();
            executionTime = (endTime - startTime) / 1_000_000.0;
            avgSaveTime += executionTime;

            // -----update----- //
            TEST_PERSON_1.setAge(28);
            startTime = System.nanoTime();
            jpaPersonRepository.save(TEST_PERSON_1);
            endTime = System.nanoTime();
            executionTime = (endTime - startTime) / 1_000_000.0;
            avgUpdateTime += executionTime;

            // -----find----- //
            startTime = System.nanoTime();
            jpaPersonRepository.findById(TEST_PERSON_1.getId());
            endTime = System.nanoTime();
            executionTime = (endTime - startTime) / 1_000_000.0;
            avgFindTime += executionTime;

            // -----custom_find----- //
            startTime = System.nanoTime();
            jpaPersonRepository.findByName(TEST_PERSON_1.getName());
            endTime = System.nanoTime();
            executionTime = (endTime - startTime) / 1_000_000.0;
            avgCustomFindTime += executionTime;

            // -----delete----- //
            startTime = System.nanoTime();
            jpaPersonRepository.delete(TEST_PERSON_1);
            endTime = System.nanoTime();
            executionTime = (endTime - startTime) / 1_000_000.0;
            avgDeleteTime += executionTime;

            jpaPersonRepository.deleteAll();
        }

        avgSaveTime /= iterations;
        avgUpdateTime /= iterations;
        avgFindTime /= iterations;
        avgCustomFindTime /= iterations;
        avgDeleteTime /= iterations;

        System.out.println("\n-=RESULTS=-\n");
        System.out.println("Save avg operation time: " + avgSaveTime + " ms" + " " + calculatePerformanceStatus(avgSaveTime, epsilon, maxTimeForSaveOperation));
        System.out.println("Update avg operation time: " + avgUpdateTime + " ms" + " " + calculatePerformanceStatus(avgUpdateTime, epsilon, maxTimeForUpdateOperation));
        System.out.println("Find avg operation time: " + avgFindTime + " ms" + " " + calculatePerformanceStatus(avgFindTime, epsilon, maxTimeForFindOperation));
        System.out.println("Find custom avg operation time: " + avgCustomFindTime + " ms" + " " + calculatePerformanceStatus(avgCustomFindTime, epsilon, maxTimeForCustomFindOperation));
        System.out.println("Delete avg operation time: " + avgDeleteTime + " ms" + " " + calculatePerformanceStatus(avgDeleteTime, epsilon, maxTimeForDeleteOperation));
    }

    String calculatePerformanceStatus(double valueToTest, double epsilon, double maxTime) {
        double exmValue;
        String status = "";

        exmValue = valueToTest;
        if (exmValue < maxTime + (epsilon * 0.75))
            status = "[PERFECT]";
        else if (exmValue < maxTime + (epsilon * 1.5))
            status = "[GOOD]";
        else if (exmValue < maxTime + (epsilon * 2.35))
            status = "[ACCEPTABLE]";
        else if (exmValue < maxTime + (epsilon * 3.0))
            status = "[BAD]";

        return status;
    }
}
