package com.altinntech.clicksave;

import com.altinntech.clicksave.core.CSRequestHandler;
import com.altinntech.clicksave.examples.entity.Gender;
import com.altinntech.clicksave.examples.entity.Job;
import com.altinntech.clicksave.examples.entity.Person;
import com.altinntech.clicksave.examples.repository.JpaPersonRepository;
import com.altinntech.clicksave.exceptions.ClicksaveRuntimeException;
import com.altinntech.clicksave.exceptions.ClicksaveSQLException;
import com.altinntech.clicksave.exceptions.ConcurrencyException;
import com.altinntech.clicksave.exceptions.ReflectiveException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ClickSaveConfiguration.class)
public class ExceptionTest {

    @Autowired
    private JpaPersonRepository jpaPersonRepository;

    @MockBean
    private CSRequestHandler requestHandler;

    private Person TEST_PERSON_1;

    @BeforeEach
    void setUp() throws IOException {
        TEST_PERSON_1 = new Person(null, "John", "Doe", 30, "some_address", Gender.MALE, Job.THREE_D_ARTIST, null);
    }

    @Test
    void testSQLException() {
        Mockito.when(requestHandler.handle(any(), any())).thenThrow(new ClicksaveSQLException(new SQLException("test")));

        ClicksaveRuntimeException clicksaveException = Assertions.assertThrows(ClicksaveRuntimeException.class,
                () -> jpaPersonRepository.save(TEST_PERSON_1)
        );

        clicksaveException.printStackTrace();
        assertInstanceOf(ClicksaveSQLException.class, clicksaveException);
        assertEquals("test", clicksaveException.getCause().getMessage());
        assertTrue(clicksaveException.getMessage().contains("SQL Exception"));
    }

    @Test
    void testConcurrencyException() {
        Mockito.when(requestHandler.handle(any(), any())).thenThrow(new ConcurrencyException(new InterruptedException("test")));

        ClicksaveRuntimeException clicksaveException = Assertions.assertThrows(ClicksaveRuntimeException.class,
                () -> jpaPersonRepository.save(TEST_PERSON_1)
        );

        clicksaveException.printStackTrace();
        assertInstanceOf(ConcurrencyException.class, clicksaveException);
        assertEquals("test", clicksaveException.getCause().getMessage());
        assertTrue(clicksaveException.getMessage().contains("Concurrency Exception"));
    }

    @Test
    void testReflectiveException() {
        Mockito.when(requestHandler.handle(any(), any())).thenThrow(new ReflectiveException(new InvocationTargetException(new Throwable(), "test")));

        ClicksaveRuntimeException clicksaveException = Assertions.assertThrows(ClicksaveRuntimeException.class,
                () -> jpaPersonRepository.save(TEST_PERSON_1)
        );

        clicksaveException.printStackTrace();
        assertInstanceOf(ReflectiveException.class, clicksaveException);
        assertEquals("test", clicksaveException.getCause().getMessage());
        assertTrue(clicksaveException.getMessage().contains("Reflective Exception"));
    }
}
