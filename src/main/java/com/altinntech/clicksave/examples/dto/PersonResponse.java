package com.altinntech.clicksave.examples.dto;

import com.altinntech.clicksave.annotations.Reference;
import com.altinntech.clicksave.examples.entity.Job;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Person response.
 */
@Data
@NoArgsConstructor
public class PersonResponse { // you can use dto projections

    /**
     * The Some name.
     */
    @Reference("name") // this annotation is used to determine the column in source entity
    String some_name;
    /**
     * The Last name.
     */
    String lastName;
    /**
     * The Job.
     */
    Job job;
    /**
     * The This field doesnt exist.
     */
    String this_field_doesnt_exist;
}
