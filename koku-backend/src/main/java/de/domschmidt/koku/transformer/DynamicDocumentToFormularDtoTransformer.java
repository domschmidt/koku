package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.formular.*;
import de.domschmidt.koku.persistence.model.dynamic_documents.*;
import de.domschmidt.koku.persistence.model.enums.Alignment;
import de.domschmidt.koku.transformer.common.ITransformer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
@Slf4j
public class DynamicDocumentToFormularDtoTransformer implements ITransformer<DynamicDocument, FormularDto> {

    public List<FormularDto> transformToDtoList(final List<DynamicDocument> documentList) {
        final List<FormularDto> result = new ArrayList<>();
        for (final DynamicDocument dynamicDocument : documentList) {
            result.add(transformToDto(dynamicDocument, false, new HashMap<>(), false));
        }
        return result;
    }

    private FormularDto transformToDto(
            final DynamicDocument document,
            final boolean detailed,
            final Map<String, Object> context,
            final boolean replaceValueByContext
    ) {
        final Map<String, Object> guardedContext;
        if (context == null) {
            guardedContext = new HashMap<>();
        } else {
            guardedContext = context;
        }

        guardedContext.put("document", document);
        guardedContext.put("localDateTime", LocalDateTime.now());
        guardedContext.put("localDate", LocalDate.now());
        guardedContext.put("localTime", LocalTime.now());
        guardedContext.put("randomUuid", UUID.randomUUID());

        return FormularDto.builder()
                .id(document.getId())
                .description(document.getDescription())
                .tags(detailed ? transformContextToTags(guardedContext) : null)
                .context(transformContext(document.getContext()))
                .rows(detailed ? transformDocumentRowToFormularRowDto(document.getRows(), guardedContext, replaceValueByContext) : null)
                .build();
    }

    private Map<String, String> transformContextToTags(final Map<String, Object> context) {
        final Map<String, String> tags = new HashMap<>();

        for (final Map.Entry<String, Object> currentContextItem : context.entrySet()) {
            if (currentContextItem.getValue() != null) {
                tags.put(currentContextItem.getKey(), currentContextItem.getValue().toString());
            }
        }

        return tags;
    }

    private List<FormularRowDto> transformDocumentRowToFormularRowDto(
            final List<DocumentRow> rows,
            final Map<String, Object> context,
            final Boolean replaceValueByContext
    ) {
        final List<FormularRowDto> result = new ArrayList<>();

        if (rows != null) {
            for (final DocumentRow currentRow : rows) {
                result.add(FormularRowDto.builder()
                        .id(currentRow.getId())
                        .align(transformRowAlign(currentRow.getAlign()))
                        .items(transformDocumentFieldToFormularFieldDto(currentRow.getFields(), context, replaceValueByContext))
                        .build()
                );
            }
        }

        return result;
    }

    private FormularRowAlignDto transformRowAlign(final DocumentRowAlign align) {
        FormularRowAlignDto result = FormularRowAlignDto.TOP;
        if (align != null) {
            switch (align) {
                case CENTER:
                    result = FormularRowAlignDto.CENTER;
                    break;
                case BOTTOM:
                    result = FormularRowAlignDto.BOTTOM;
                    break;
            }
        }
        return result;
    }

    public DocumentContextDto transformContext(final DocumentContext context) {
        DocumentContextDto result = null;
        if (context != null) {
            switch (context) {
                case CUSTOMER:
                    result = DocumentContextDto.builder()
                            .value(DocumentContextEnumDto.CUSTOMER)
                            .description(DocumentContextEnumDto.CUSTOMER.getDescription())
                            .build();
                    break;
                case NONE:
                    result = DocumentContextDto.builder()
                            .value(DocumentContextEnumDto.NONE)
                            .description(DocumentContextEnumDto.NONE.getDescription())
                            .build();
                    break;
            }
        }
        return result;
    }

    public DocumentContext transformContext(final DocumentContextEnumDto context) {
        DocumentContext result = null;
        if (context != null) {
            switch (context) {
                case CUSTOMER:
                    result = DocumentContext.CUSTOMER;
                    break;
                case NONE:
                    result = DocumentContext.NONE;
                    break;
            }
        }
        return result;
    }

    private List<FormularItemDto> transformDocumentFieldToFormularFieldDto(
            final List<DocumentField> fields,
            final Map<String, Object> context,
            final Boolean replaceValueByContext
    ) {
        final List<FormularItemDto> result = new ArrayList<>();

        final Context thymeleafCtx = new Context();
        for (Map.Entry<String, Object> currentContextEntry : context.entrySet()) {
            thymeleafCtx.setVariable(currentContextEntry.getKey(), currentContextEntry.getValue());
        }

        if (fields != null) {
            final TemplateEngine templateEngine = new SpringTemplateEngine();
            final StringTemplateResolver templateResolver = new StringTemplateResolver();
            templateResolver.setTemplateMode(TemplateMode.HTML);
            templateEngine.addDialect(new Java8TimeDialect());
            templateEngine.setTemplateResolver(templateResolver);
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
                } else if (fieldDefinitionType instanceof CheckboxFieldDefinitionType) {
                    final boolean isCheckboxChecked;
                    final String fieldContext = ((CheckboxFieldDefinitionType) fieldDefinitionType).getContext();
                    if (replaceValueByContext && context.size() > 0 && fieldContext != null) {
                        String replacedText = null;
                        try {
                            replacedText = templateEngine.process(
                                    fieldContext,
                                    thymeleafCtx
                            );
                        } finally {
                            isCheckboxChecked = StringUtils.defaultString(replacedText).trim().equalsIgnoreCase(Boolean.TRUE.toString());
                        }
                    } else {
                        isCheckboxChecked = ((CheckboxFieldDefinitionType) fieldDefinitionType).isValue();
                    }
                    result.add(CheckboxFormularItemDto.builder()
                            .id(currentField.getId())
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .align(transformDocumentFieldAlignToFormularItemAlign(currentField))
                            .value(isCheckboxChecked)
                            .context(fieldContext)
                            .fontSize(((CheckboxFieldDefinitionType) fieldDefinitionType).getFontSize())
                            .fieldDefinitionTypeId(fieldDefinitionType.getId())
                            .readOnly(((CheckboxFieldDefinitionType) fieldDefinitionType).isReadOnly())
                            .label(((CheckboxFieldDefinitionType) fieldDefinitionType).getLabel())
                            .build());
                } else if (fieldDefinitionType instanceof final DateFieldDefinitionType castedField) {
                    final String fieldContext = castedField.getContext();
                    LocalDate value = castedField.getValue();
                    if (replaceValueByContext && fieldContext != null) {
                        String replacedValue = null;
                        try {
                            replacedValue = templateEngine.process(fieldContext, thymeleafCtx);
                        } finally {
                            try {
                                value = LocalDate.parse(StringUtils.defaultString(replacedValue));
                                if (castedField.getDayDiff() != null) {
                                    value = value.plusDays(castedField.getDayDiff());
                                }
                                if (castedField.getMonthDiff() != null) {
                                    value = value.plusMonths(castedField.getMonthDiff());
                                }
                                if (castedField.getYearDiff() != null) {
                                    value = value.plusYears(castedField.getYearDiff());
                                }
                            } catch (final DateTimeParseException dtpe) {
                                log.error("Unable to parse value as Date");
                            }
                        }
                    }
                    result.add(DateFormularItemDto.builder()
                            .id(currentField.getId())
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .align(transformDocumentFieldAlignToFormularItemAlign(currentField))
                            .value(value)
                            .context(castedField.getContext())
                            .fontSize(castedField.getFontSize())
                            .fieldDefinitionTypeId(fieldDefinitionType.getId())
                            .readOnly(castedField.isReadOnly())
                            .dayDiff(castedField.getDayDiff())
                            .monthDiff(castedField.getMonthDiff())
                            .yearDiff(castedField.getYearDiff())
                            .build()
                    );
                } else if (fieldDefinitionType instanceof final TextFieldDefinitionType castedFieldType) {
                    String textFieldValue;
                    if (replaceValueByContext && context.size() > 0) {
                        try {
                            textFieldValue = templateEngine.process(
                                    StringUtils.defaultString(castedFieldType.getText()),
                                    thymeleafCtx
                            );
                        } catch (final TemplateInputException tie) {
                            textFieldValue = castedFieldType.getText();
                        }
                    } else {
                        textFieldValue = castedFieldType.getText();
                    }
                    result.add(TextFormularItemDto.builder()
                            .id(currentField.getId())
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .align(transformDocumentFieldAlignToFormularItemAlign(currentField))
                            .text(textFieldValue)
                            .fontSize(castedFieldType.getFontSize())
                            .fieldDefinitionTypeId(fieldDefinitionType.getId())
                            .readOnly(castedFieldType.isReadOnly()).build());
                } else if (fieldDefinitionType instanceof final QRCodeFieldDefinitionType castedFieldType) {
                    String replacedContent;
                    if (replaceValueByContext && context.size() > 0) {
                        try {
                            replacedContent = templateEngine.process(
                                    StringUtils.defaultString(castedFieldType.getContent()),
                                    thymeleafCtx
                            );
                        } catch (final TemplateInputException tie) {
                            replacedContent = castedFieldType.getContent();
                        }
                    } else {
                        replacedContent = castedFieldType.getContent();
                    }
                    result.add(QrCodeFormularItemDto.builder()
                            .id(currentField.getId())
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .align(transformDocumentFieldAlignToFormularItemAlign(currentField))
                            .value(replacedContent)
                            .fieldDefinitionTypeId(fieldDefinitionType.getId())
                            .maxWidthInPx(castedFieldType.getMaxWidthInPx())
                            .widthPercentage(castedFieldType.getWidthPercentage())
                            .build()
                    );
                }
            }
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

    public DynamicDocument transformToEntity(final FormularDto dtoModel) {
        final DynamicDocument result = DynamicDocument.builder()
                .id(dtoModel.getId())
                .description(dtoModel.getDescription())
                .context(transformContext(dtoModel.getContext().getValue()))
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
                        .align(transformRowAlign(currentRow.getAlign()))
                        .build();
                newRow.setFields(transformFormularItemToDocumentField(newRow, currentRow.getItems()));
                result.add(newRow);
            }
        }

        return result;
    }

    private DocumentRowAlign transformRowAlign(
            final FormularRowAlignDto align
    ) {
        DocumentRowAlign result = DocumentRowAlign.TOP;
        if (align != null) {
            switch (align) {
                case CENTER:
                    result = DocumentRowAlign.CENTER;
                    break;
                case BOTTOM:
                    result = DocumentRowAlign.BOTTOM;
                    break;
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
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .positionIndex(fieldIndex)
                            .fieldDefinitionType(TextFieldDefinitionType.builder()
                                    .id(currentField.getFieldDefinitionTypeId())
                                    .text(((TextFormularItemDto) currentField).getText())
                                    .readOnly(((TextFormularItemDto) currentField).isReadOnly())
                                    .fontSize(((TextFormularItemDto) currentField).getFontSize())
                                    .build())
                            .alignment(transformFormularItemAlignToDocumentFieldAlign(currentField))
                            .build());
                } else if (currentField instanceof final QrCodeFormularItemDto castedField) {
                    result.add(DocumentField.builder()
                            .row(newRow)
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .positionIndex(fieldIndex)
                            .fieldDefinitionType(QRCodeFieldDefinitionType.builder()
                                    .id(currentField.getFieldDefinitionTypeId())
                                    .content(castedField.getValue())
                                    .maxWidthInPx(castedField.getMaxWidthInPx())
                                    .widthPercentage(castedField.getWidthPercentage())
                                    .build()
                            )
                            .alignment(transformFormularItemAlignToDocumentFieldAlign(currentField))
                            .build());
                } else if (currentField instanceof DateFormularItemDto) {
                    result.add(DocumentField.builder()
                            .row(newRow)
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .positionIndex(fieldIndex)
                            .fieldDefinitionType(DateFieldDefinitionType.builder()
                                    .id(currentField.getFieldDefinitionTypeId())
                                    .value(((DateFormularItemDto) currentField).getValue())
                                    .context(((DateFormularItemDto) currentField).getContext())
                                    .fontSize(((DateFormularItemDto) currentField).getFontSize())
                                    .dayDiff(((DateFormularItemDto) currentField).getDayDiff())
                                    .monthDiff(((DateFormularItemDto) currentField).getMonthDiff())
                                    .yearDiff(((DateFormularItemDto) currentField).getYearDiff())
                                    .build()
                            )
                            .alignment(transformFormularItemAlignToDocumentFieldAlign(currentField))
                            .build());
                } else if (currentField instanceof CheckboxFormularItemDto) {
                    result.add(DocumentField.builder()
                            .row(newRow)
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .positionIndex(fieldIndex)
                            .fieldDefinitionType(CheckboxFieldDefinitionType.builder()
                                    .id(currentField.getFieldDefinitionTypeId())
                                    .context(((CheckboxFormularItemDto) currentField).getContext())
                                    .value(((CheckboxFormularItemDto) currentField).isValue())
                                    .readOnly(((CheckboxFormularItemDto) currentField).isReadOnly())
                                    .label(((CheckboxFormularItemDto) currentField).getLabel())
                                    .fontSize(((CheckboxFormularItemDto) currentField).getFontSize())
                                    .build()
                            )
                            .alignment(transformFormularItemAlignToDocumentFieldAlign(currentField))
                            .build()
                    );
                }
            }
        }

        return result;
    }

    public FormularDto transformToDto(final DynamicDocument model) {
        return this.transformToDto(model, true, new HashMap<>(), false);
    }

    public FormularDto transformToDto(final DynamicDocument model, final Map<String, Object> context) {
        return this.transformToDto(model, true, context, true);
    }

}
