package de.domschmidt.koku.service.searchoptions;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchOptions {
    String search;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate priceDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    LocalTime priceTime;
}
