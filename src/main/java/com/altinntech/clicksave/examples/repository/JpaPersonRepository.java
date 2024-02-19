package com.altinntech.clicksave.examples.repository;

import com.altinntech.clicksave.annotations.ClickHouseRepository;
import com.altinntech.clicksave.examples.dto.PersonResponse;
import com.altinntech.clicksave.examples.entity.Gender;
import com.altinntech.clicksave.examples.entity.Job;
import com.altinntech.clicksave.examples.entity.Person;
import com.altinntech.clicksave.interfaces.ClickHouseJpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ClickHouseRepository // repository interface must be marked with this annotation
public interface JpaPersonRepository extends ClickHouseJpa<Person> {
    Optional<Person> findByName(String name); // findBy always returns the Optional<T>
    List<Person> findAllByJob(Job job); // supports pulling by enumeration
    List<Person> findAllByGender(Gender gender); // another one enumeration
    List<Person> findAllByAge(int age);
    Optional<Person> findByNameAndLastName(String name, String lastName); // supports "And" & "Or" operators
    Optional<Person> findByNameAndLastNameOrAddress(String name, String lastName, String address);
    Optional<Person> findByAgeAndName(int age, String name);
    Optional<PersonResponse> findByLastName(String lastName);
    Optional<PersonResponse> findByIdCustom(UUID id);
}
