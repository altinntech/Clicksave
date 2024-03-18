package com.altinntech.clicksave.examples.entity;

import com.altinntech.clicksave.annotations.*;
import com.altinntech.clicksave.core.CSUtils;
import com.altinntech.clicksave.enums.EnumType;
import com.altinntech.clicksave.enums.FieldType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@ClickHouseEntity(forTest = true) // you should use this annotation for persistence entity
@PartitionBy("toYYYYMM(timestamp)")
@OrderBy("(id, gender)")
//@Batching(batchSize = 10000) // add batch for saving
public class Person {

    // entity class must have a no arguments constructor
    public Person() {
    }

    @Column(value = FieldType.UUID, id = true, primaryKey = true) // it is recommended to make the id field a UUID type
    UUID id;
    @Column(FieldType.STRING)
    String name;
    @Column(FieldType.STRING)
    String lastName;
    @Column(FieldType.INT)
    Integer age; // not supported primitive types
    @Column(FieldType.STRING)
    String address;
    @EnumColumn(EnumType.STRING) // for enum use @EnumColumn annotation
    Gender gender;
    @EnumColumn(EnumType.BY_ID) // you can persist the enum by id value (enum must implements the EnumId interface!)
    Job job;
    @Embedded
    EmployeeInfo employeeInfo;
    @Lob
    List<CompanyMetadata> companyMetadata;
    @Lob
    CompanyMetadata companyMetadataSingle;
    @Lob
    int[][][] matrix;
    @Column(FieldType.DATE_TIME)
    LocalDateTime timestamp;
    @Column(FieldType.BOOL)
    Boolean enabled;
    String noSaveField; // this field will not be saved

    public Person(UUID id, String name, String lastName, Integer age, String address, Gender gender, Job job, String noSaveField) throws IOException {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.age = age;
        this.address = address;
        this.gender = gender;
        this.job = job;
        this.noSaveField = noSaveField;
        this.employeeInfo = buildMockEmployeeInfo();
        this.companyMetadata = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CompanyMetadata metadata = buildMockCompanyMetadata();
            this.companyMetadata.add(metadata);
        }
        this.companyMetadataSingle = buildMockCompanyMetadata();
        this.matrix = buildMockMatrix();
        this.timestamp = LocalDateTime.now();
        this.enabled = true;
    }

    public static Person buildMockPerson() throws IOException {
        Person person = new Person();
        person.name = CSUtils.generateRandomString(5);
        person.lastName = CSUtils.generateRandomString(10);
        person.age = CSUtils.generateRandomNumber(1, 99);
        person.address = CSUtils.generateRandomString(15);
        person.gender = CSUtils.getRandomEnum(Gender.class);
        person.job = CSUtils.getRandomEnum(Job.class);
        person.employeeInfo = buildMockEmployeeInfo();
        person.companyMetadata = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CompanyMetadata metadata = buildMockCompanyMetadata();
            person.companyMetadata.add(metadata);
        }
        person.companyMetadataSingle = buildMockCompanyMetadata();
        person.matrix = buildMockMatrix();
        person.timestamp = LocalDateTime.now();
        person.enabled = true;
        return person;
    }

    public static EmployeeInfo buildMockEmployeeInfo() {
        EmployeeInfo employeeInfo = new EmployeeInfo();
        employeeInfo.description = CSUtils.generateRandomString(15);
        employeeInfo.experience = (double) CSUtils.generateRandomNumber(1, 99);
        employeeInfo.setWorkInfo(buildMockWorkInfo());
        return employeeInfo;
    }

    public static WorkInfo buildMockWorkInfo() {
        WorkInfo workInfo = new WorkInfo();
        workInfo.setWorkName(CSUtils.generateRandomString(5));
        workInfo.setGrade(CSUtils.generateRandomNumber(1, 99));
        return workInfo;
    }

    public static CompanyMetadata buildMockCompanyMetadata() {
        CompanyMetadata companyMetadata = new CompanyMetadata();
        companyMetadata.setCompanyName(CSUtils.generateRandomString(7));
        companyMetadata.setCompanyIndex((long) CSUtils.generateRandomNumber(10000, 99999));
        return companyMetadata;
    }

    public static int[][][] buildMockMatrix() {
        int[][][] matrix = new int[10][10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    matrix[i][j][k] = CSUtils.generateRandomNumber(10, 99);
                }
            }
        }
        return matrix;
    }
}
