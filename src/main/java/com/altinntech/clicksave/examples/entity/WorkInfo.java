package com.altinntech.clicksave.examples.entity;

import com.altinntech.clicksave.annotations.Column;
import com.altinntech.clicksave.annotations.Embeddable;
import com.altinntech.clicksave.annotations.Lob;
import com.altinntech.clicksave.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Embeddable
public class WorkInfo {

    public WorkInfo() {
    }

    @Column(FieldType.STRING)
    String workName;
    @Column(FieldType.INT)
    Integer grade;
    @Lob
    CompanyMetadata companyMetadataWorkInfo;
}
