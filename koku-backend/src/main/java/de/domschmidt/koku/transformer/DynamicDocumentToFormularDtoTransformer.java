package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.formular.*;
import de.domschmidt.koku.persistence.model.dynamic_documents.*;
import de.domschmidt.koku.persistence.model.enums.Alignment;
import de.domschmidt.koku.transformer.common.ITransformer;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DynamicDocumentToFormularDtoTransformer implements ITransformer<DynamicDocument, FormularDto> {

    public List<FormularDto> transformToDtoList(final List<DynamicDocument> documentList) {
        final List<FormularDto> result = new ArrayList<>();
        for (final DynamicDocument dynamicDocument : documentList) {
            result.add(transformToDto(dynamicDocument, false, new HashMap<>()));
        }
        return result;
    }

    private FormularDto transformToDto(final DynamicDocument document,
                                       final boolean detailed,
                                       final Map<String, Object> replacementToken) {
        return FormularDto.builder()
                .id(document.getId())
                .description(document.getDescription())
                .rows(detailed ? transformDocumentRowToFormularRowDto(document, document.getRows(), replacementToken) : null)
                .build();
    }

    private List<FormularRowDto> transformDocumentRowToFormularRowDto(final DynamicDocument document,
                                                                      final List<DocumentRow> rows,
                                                                      final Map<String, Object> replacementToken) {
        final List<FormularRowDto> result = new ArrayList<>();

        if (rows != null) {
            for (final DocumentRow currentRow : rows) {
                result.add(FormularRowDto.builder()
                        .id(currentRow.getId())
                        .items(transformDocumentFieldToFormularFieldDto(document, currentRow.getFields(), replacementToken))
                        .build());
            }
        }

        return result;
    }

    private List<FormularItemDto> transformDocumentFieldToFormularFieldDto(final DynamicDocument document,
                                                                           final List<DocumentField> fields,
                                                                           final Map<String, Object> replacementToken) {
        final List<FormularItemDto> result = new ArrayList<>();

        if (fields != null) {
            for (final DocumentField currentField : fields) {
                final FieldDefinitionType fieldDefinitionType = currentField.getFieldDefinitionType();
                if (fieldDefinitionType instanceof SVGFieldDefinitionType) {
                    result.add(SVGFormularItemDto.builder()
                            .id(currentField.getId())
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .align(transformDocumentFieldAlignToFormularItemAlign(currentField))
                            .svgContentBase64encoded(transformSvgFieldValue((SVGFieldDefinitionType) fieldDefinitionType))
                            .widthPercentage(((SVGFieldDefinitionType) fieldDefinitionType).getWidthPercentage())
                            .maxWidthInPx(((SVGFieldDefinitionType) fieldDefinitionType).getMaxWidthInPx())
                            .fieldDefinitionTypeId(fieldDefinitionType.getId())
                            .build());
                } else if (fieldDefinitionType instanceof SignatureFieldDefinitionType) {
                    result.add(SignatureFormularItemDto.builder()
                            .id(currentField.getId())
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .align(transformDocumentFieldAlignToFormularItemAlign(currentField))
                            .fieldDefinitionTypeId(fieldDefinitionType.getId())
                            .build());
                } else if (fieldDefinitionType instanceof TextFieldDefinitionType) {
                    final TemplateEngine templateEngine = new SpringTemplateEngine();
                    final StringTemplateResolver templateResolver = new StringTemplateResolver();
                    templateResolver.setTemplateMode(TemplateMode.HTML);
                    templateEngine.addDialect(new Java8TimeDialect());
                    templateEngine.setTemplateResolver(templateResolver);
                    final Context ctx = new Context();
                    for (Map.Entry<String, Object> currentContextEntry : replacementToken.entrySet()) {
                        ctx.setVariable(currentContextEntry.getKey(), currentContextEntry.getValue());
                    }
                    ctx.setVariable("document", document);
                    ctx.setVariable("header", "<div></div>");
                    ctx.setVariable("footer", "<div></div>");
                    ctx.setVariable("localDateTime", LocalDateTime.now());
                    ctx.setVariable("localDate", LocalDate.now());
                    ctx.setVariable("localTime", LocalTime.now());
                    ctx.setVariable("timestamp", Instant.now());

                    final String replacedText;
                    if (replacementToken.size() > 0) {
                        replacedText = templateEngine.process(((TextFieldDefinitionType) fieldDefinitionType).getText(), ctx);
                    } else {
                        replacedText = ((TextFieldDefinitionType) fieldDefinitionType).getText();
                    }
                    result.add(TextFormularItemDto.builder()
                            .id(currentField.getId())
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .align(transformDocumentFieldAlignToFormularItemAlign(currentField))
                            .text(replacedText)
                            .fontSize(transformFieldDefintionTypeFontSizeToFontSizeDto((TextFieldDefinitionType)fieldDefinitionType))
                            .fieldDefinitionTypeId(fieldDefinitionType.getId())
                            .build());
                }
            }
        }

        return result;
    }

    private FontSizeDto transformFieldDefintionTypeFontSizeToFontSizeDto(final TextFieldDefinitionType fieldDefinitionType) {
        FontSizeDto result;
        if (fieldDefinitionType.getFontSize() != null) {
            switch (fieldDefinitionType.getFontSize()) {
                case LARGE:
                    result = FontSizeDto.LARGE;
                    break;
                case SMALL:
                    result = FontSizeDto.SMALL;
                    break;
                default:
                    result = null;
                    break;
            }
        } else {
            result = null;
        }
        return result;
    }

    private FontSize transformFontSizeDtoToFieldDefintionTypeFontSize(final FontSizeDto fontSizeDto) {
        FontSize result;
        if (fontSizeDto != null) {
            switch (fontSizeDto) {
                case LARGE:
                    result = FontSize.LARGE;
                    break;
                case SMALL:
                    result = FontSize.SMALL;
                    break;
                default:
                    result = null;
                    break;
            }
        } else {
            result = null;
        }
        return result;
    }

    private String transformSvgFieldValue(final SVGFieldDefinitionType svgFieldDefinitionType) {
        return svgFieldDefinitionType.getSvgContentBase64encoded();
    }

    private FormularItemAlign transformDocumentFieldAlignToFormularItemAlign(final DocumentField currentField) {
        FormularItemAlign result = FormularItemAlign.LEFT;

        if (currentField.getAlignment() != null) {
            switch (currentField.getAlignment()) {
                case CENTER:
                    result = FormularItemAlign.CENTER;
                    break;
                case LEFT:
                    result = FormularItemAlign.LEFT;
                    break;
                case RIGHT:
                    result = FormularItemAlign.RIGHT;
                    break;
            }
        }

        return result;
    }

    private Alignment transformFormularItemAlignToDocumentFieldAlign(final FormularItemDto currentField) {
        Alignment result = Alignment.LEFT;

        if (currentField.getAlign() != null) {
            switch (currentField.getAlign()) {
                case CENTER:
                    result = Alignment.CENTER;
                    break;
                case LEFT:
                    result = Alignment.LEFT;
                    break;
                case RIGHT:
                    result = Alignment.RIGHT;
                    break;
            }
        }

        return result;
    }

    public FormularDto transformToDto(final DynamicDocument model) {
        return this.transformToDto(model, true, new HashMap<>());
    }

    public DynamicDocument transformToEntity(final FormularDto dtoModel) {
        final DynamicDocument result = DynamicDocument.builder()
                .id(dtoModel.getId())
                .description(dtoModel.getDescription())
                .build();

        result.setRows(transformFormularRowDtoToDocumentRow(result, dtoModel.getRows()));

        return result;
    }

    private List<DocumentRow> transformFormularRowDtoToDocumentRow(final DynamicDocument document, final List<FormularRowDto> rows) {
        final List<DocumentRow> result = new ArrayList<>();

        if (rows != null) {
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                FormularRowDto currentRow = rows.get(rowIndex);
                final DocumentRow newRow = DocumentRow.builder()
                        .document(document)
                        .id(currentRow.getId())
                        .positionIndex(rowIndex)
                        .build();
                newRow.setFields(transformFormularItemToDocumentField(newRow, currentRow.getItems()));
                result.add(newRow);
            }
        }

        return result;
    }

    private List<DocumentField> transformFormularItemToDocumentField(DocumentRow newRow, final List<FormularItemDto> items) {
        final List<DocumentField> result = new ArrayList<>();

        if (items != null) {
            for (int fieldIndex = 0; fieldIndex < items.size(); fieldIndex++) {
                FormularItemDto currentField = items.get(fieldIndex);
                if (currentField instanceof SVGFormularItemDto) {
                    result.add(DocumentField.builder()
                            .row(newRow)
                            .id(currentField.getId())
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .positionIndex(fieldIndex)
                            .fieldDefinitionType(SVGFieldDefinitionType.builder()
                                    .maxWidthInPx(((SVGFormularItemDto) currentField).getMaxWidthInPx())
                                    .widthPercentage(((SVGFormularItemDto) currentField).getWidthPercentage())
                                    .svgContentBase64encoded(((SVGFormularItemDto) currentField).getSvgContentBase64encoded())
                                    .id(currentField.getFieldDefinitionTypeId())
                                    .build())
                            .alignment(transformFormularItemAlignToDocumentFieldAlign(currentField))
                            .build());
                } else if (currentField instanceof SignatureFormularItemDto) {
                    result.add(DocumentField.builder()
                            .row(newRow)
                            .id(currentField.getId())
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .positionIndex(fieldIndex)
                            .fieldDefinitionType(SignatureFieldDefinitionType.builder()
                                    .id(currentField.getFieldDefinitionTypeId())
                                    .build())
                            .alignment(transformFormularItemAlignToDocumentFieldAlign(currentField))
                            .build());
                } else if (currentField instanceof TextFormularItemDto) {
                    result.add(DocumentField.builder()
                            .row(newRow)
                            .id(currentField.getId())
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .positionIndex(fieldIndex)
                            .fieldDefinitionType(TextFieldDefinitionType.builder()
                                    .id(currentField.getFieldDefinitionTypeId())
                                    .text(((TextFormularItemDto) currentField).getText())
                                    .fontSize(transformFontSizeDtoToFieldDefintionTypeFontSize(((TextFormularItemDto) currentField).getFontSize()))
                                    .build())
                            .alignment(transformFormularItemAlignToDocumentFieldAlign(currentField))
                            .build());
                }
            }
        }

        return result;
    }

    public FormularDto transformToDto(final DynamicDocument model, final Map<String, Object> replacementToken) {
        return this.transformToDto(model, true, replacementToken);
    }

}
