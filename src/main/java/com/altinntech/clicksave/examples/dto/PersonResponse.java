package com.altinntech.clicksave.examples.dto;

import com.altinntech.clicksave.annotations.Reference;
import com.altinntech.clicksave.examples.entity.Job;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PersonResponse {

    @Reference("name")
    String some_name;
    String lastName;
    Job job;
    String this_field_doesnt_exist;
}
