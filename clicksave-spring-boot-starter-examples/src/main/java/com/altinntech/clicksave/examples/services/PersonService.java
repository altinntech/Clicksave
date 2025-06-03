package com.altinntech.clicksave.examples.services;/*
package com.altinntech.clicksave.examples.services;

import com.altinntech.clicksave.examples.entity.Person;
import com.altinntech.clicksave.examples.exceptions.PersonNotFoundException;
import com.altinntech.clicksave.examples.repository.JpaPersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PersonService {

    private final JpaPersonRepository jpaPersonRepository;

    @Autowired
    public PersonService(JpaPersonRepository jpaPersonRepository) {
        this.jpaPersonRepository = jpaPersonRepository;
    }

    public void save(Person person) {
        jpaPersonRepository.save(person); // saving a new entity is fast, but updating an entity takes a long time
    }

    public void delete(Person person) {
        jpaPersonRepository.delete(person);
    }

    public Person getPersonById(Long id) {
        Optional<Person> person = jpaPersonRepository.findById(id);
        if (person.isPresent())
            return person.get();
        else throw new PersonNotFoundException();
    }

    public boolean connectionNotification() {
        return true;
    }
}
*/
