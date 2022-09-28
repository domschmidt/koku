package de.domschmidt.koku.dto.formular;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter

public class DateFormularItemDto extends FormularItemDto {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate value;
    String context;
    Integer fontSize;
    boolean readOnly;

    Integer dayDiff;
    Integer monthDiff;
    Integer yearDiff;

}
