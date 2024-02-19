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

/**
 * The interface Jpa person repository.
 */
@ClickHouseRepository // repository interface must be marked with this annotation
public interface JpaPersonRepository extends ClickHouseJpa<Person> {
    /**
     * Find by name optional.
     *
     * @param name the name
     * @return the optional
     */
    Optional<Person> findByName(String name); // findBy always returns the Optional<T>

    /**
     * Find all by job list.
     *
     * @param job the job
     * @return the list
     */
    List<Person> findAllByJob(Job job); // supports pulling by enumeration

    /**
     * Find all by gender list.
     *
     * @param gender the gender
     * @return the list
     */
    List<Person> findAllByGender(Gender gender); // another one enumeration

    /**
     * Find all by age list.
     *
     * @param age the age
     * @return the list
     */
    List<Person> findAllByAge(int age);

    /**
     * Find by name and last name optional.
     *
     * @param name     the name
     * @param lastName the last name
     * @return the optional
     */
    Optional<Person> findByNameAndLastName(String name, String lastName); // supports "And" & "Or" operators

    /**
     * Find by name and last name or address optional.
     *
     * @param name     the name
     * @param lastName the last name
     * @param address  the address
     * @return the optional
     */
    Optional<Person> findByNameAndLastNameOrAddress(String name, String lastName, String address);

    /**
     * Find by age and name optional.
     *
     * @param age  the age
     * @param name the name
     * @return the optional
     */
    Optional<Person> findByAgeAndName(int age, String name);

    /**
     * Find by last name optional.
     *
     * @param lastName the last name
     * @return the optional
     */
    Optional<PersonResponse> findByLastName(String lastName); // supports data projections

    /**
     * Find by id custom optional.
     *
     * @param id the id
     * @return the optional
     */
    Optional<PersonResponse> findByIdCustom(UUID id); // overrides default findById method
}
