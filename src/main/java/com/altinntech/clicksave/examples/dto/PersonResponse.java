package com.altinntech.clicksave.examples.dto;

import com.altinntech.clicksave.annotations.Reference;
import com.altinntech.clicksave.examples.entity.CompanyMetadata;
import com.altinntech.clicksave.examples.entity.Job;
import com.altinntech.clicksave.examples.entity.Person;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PersonResponse { // you can use dto projections

    @Reference("name") // this annotation is used to determine the column in source entity
    String some_name;
    String lastName;
    Job job;
    @Reference("description")
    String description;
    CompanyMetadata companyMetadata;
    String this_field_doesnt_exist;

    public static PersonResponse create(Person person) {
        PersonResponse response = new PersonResponse();
        response.setSome_name(person.getName());
        response.setLastName(person.getLastName());
        response.setJob(person.getJob());
        response.setDescription(person.getEmployeeInfo().getDescription());
        response.setCompanyMetadata(person.getCompanyMetadata());
        return response;
    }
}
