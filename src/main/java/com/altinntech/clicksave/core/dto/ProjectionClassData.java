package com.altinntech.clicksave.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProjectionClassData {

    private List<ProjectionFieldData> fields;
}
