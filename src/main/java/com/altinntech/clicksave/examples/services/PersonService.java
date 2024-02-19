package com.altinntech.clicksave.examples.services;

import com.altinntech.clicksave.examples.entity.Person;
import com.altinntech.clicksave.examples.exceptions.PersonNotFoundException;
import com.altinntech.clicksave.examples.repository.JpaPersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * The type Person service.
 */
@Service
public class PersonService {

    private final JpaPersonRepository jpaPersonRepository;

    /**
     * Instantiates a new Person service.
     *
     * @param jpaPersonRepository the jpa person repository
     */
    @Autowired
    public PersonService(JpaPersonRepository jpaPersonRepository) {
        this.jpaPersonRepository = jpaPersonRepository;
    }

    /**
     * Save.
     *
     * @param person the person
     */
    public void save(Person person) {
        jpaPersonRepository.save(person); // saving a new entity is fast, but updating an entity takes a long time
    }

    /**
     * Delete.
     *
     * @param person the person
     */
    public void delete(Person person) {
        jpaPersonRepository.delete(person);
    }

    /**
     * Gets person by id.
     *
     * @param id the id
     * @return the person by id
     */
    public Person getPersonById(UUID id) {
        Optional<Person> person = jpaPersonRepository.findById(id);
        if (person.isPresent())
            return person.get();
        else throw new PersonNotFoundException();
    }

    /**
     * Health check boolean.
     *
     * @return the boolean
     */
    public boolean healthCheck() {
        return true;
    }
}
