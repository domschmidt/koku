package de.domschmidt.koku.service.searchoptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchOptions {
    String search;
    private LocalDate priceDate;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime priceTime;
}
