package de.domschmidt.koku.dto.formular;

import lombok.Getter;

@Getter
public enum FontSizeDto {

    SMALL(8),
    MEDIUM(12),
    LARGE(20);

    // use with openpdf
    final Integer pdfPCellFontSize;

    FontSizeDto(final Integer pdfPCellFontSize) {
        this.pdfPCellFontSize = pdfPCellFontSize;
    }
}
