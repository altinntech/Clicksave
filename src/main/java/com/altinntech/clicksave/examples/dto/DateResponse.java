package com.altinntech.clicksave.examples.dto;

import com.altinntech.clicksave.annotations.Column;
import com.altinntech.clicksave.annotations.EnumColumn;
import com.altinntech.clicksave.annotations.Reference;
import com.altinntech.clicksave.enums.EnumType;
import com.altinntech.clicksave.enums.FieldType;
import com.altinntech.clicksave.examples.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateResponse {

    @Reference("date")
    @Column(FieldType.DATE)
    LocalDate localDate;

    @Reference("sqlTimestamp")
    @Column(FieldType.DATE_TIME)
    LocalDateTime localDateSqlTimestamp;

    @Reference("timestamp")
    @Column(FieldType.DATE_TIME)
    LocalDateTime localDateTime;
}
