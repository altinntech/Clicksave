package com.altinntech.clicksave.examples.dto;

import com.altinntech.clicksave.annotations.Column;
import com.altinntech.clicksave.annotations.EnumColumn;
import com.altinntech.clicksave.annotations.Reference;
import com.altinntech.clicksave.enums.EnumType;
import com.altinntech.clicksave.enums.FieldType;
import com.altinntech.clicksave.examples.entity.Job;
import com.altinntech.clicksave.examples.entity.Person;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PersonResponse { // you can use dto projections

    @Column(FieldType.STRING)
    @Reference("name") // this annotation is used to determine the column in source entity
    String some_name;
    @Column(FieldType.STRING)
    String lastName;
    @EnumColumn(EnumType.BY_ID)
    Job job;
    @Column(FieldType.STRING)
    @Reference("description")
    String description;
    String this_field_doesnt_exist;

    public static PersonResponse create(Person person) {
        PersonResponse response = new PersonResponse();
        response.setSome_name(person.getName());
        response.setLastName(person.getLastName());
        response.setJob(person.getJob());
        response.setDescription(person.getEmployeeInfo().getDescription());
        return response;
    }
}
