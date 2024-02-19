package com.altinntech.clicksave.examples.entity;

import com.altinntech.clicksave.annotations.Batching;
import com.altinntech.clicksave.annotations.ClickHouseEntity;
import com.altinntech.clicksave.annotations.Column;
import com.altinntech.clicksave.annotations.EnumColumn;
import com.altinntech.clicksave.core.CSUtils;
import com.altinntech.clicksave.enums.EnumType;
import com.altinntech.clicksave.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * The type Person.
 */
@Data
@AllArgsConstructor
@ClickHouseEntity // you should use this annotation for persistence entity
@Batching(batchSize = 10) // add batch for saving
public class Person {

    /**
     * Instantiates a new Person.
     */
// entity class must have a no arguments constructor
    public Person() {
    }

    /**
     * The Id.
     */
    @Column(value = FieldType.UUID, id = true)
    UUID id; // the id field must be UUID type
    /**
     * The Name.
     */
    @Column(FieldType.STRING)
    String name;
    /**
     * The Last name.
     */
    @Column(FieldType.STRING)
    String lastName;
    /**
     * The Age.
     */
    @Column(FieldType.INT)
    Integer age; // not supported primitive types
    /**
     * The Address.
     */
    @Column(FieldType.STRING)
    String address;
    /**
     * The Gender.
     */
    @EnumColumn(EnumType.STRING) // for enum use @EnumColumn annotation
    Gender gender;
    /**
     * The Job.
     */
    @EnumColumn(EnumType.BY_ID) // you can persist the enum by id value (enum must implements the EnumId interface!)
    Job job;
    /**
     * The No save field.
     */
    String noSaveField; // this field will not be saved

    /**
     * Build mock person person.
     *
     * @return the person
     */
    public static Person buildMockPerson() {
        Person person = new Person();
        person.name = CSUtils.generateRandomString(5);
        person.lastName = CSUtils.generateRandomString(10);
        person.age = CSUtils.generateRandomNumber(1, 99);
        person.address = CSUtils.generateRandomString(15);
        person.gender = CSUtils.getRandomEnum(Gender.class);
        person.job = CSUtils.getRandomEnum(Job.class);
        return person;
    }
}
