package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.formular.*;
import de.domschmidt.koku.persistence.model.Activity;
import de.domschmidt.koku.persistence.model.dynamic_documents.*;
import de.domschmidt.koku.persistence.model.enums.Alignment;
import de.domschmidt.koku.service.IActivityService;
import de.domschmidt.koku.service.searchoptions.ActivitySearchOptions;
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

    private final IActivityService activityService;

    public DynamicDocumentToFormularDtoTransformer(
            final IActivityService activityService
    ) {
        this.activityService = activityService;
    }

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
            final List<DocumentRowComposing> rows,
            final Map<String, Object> context,
            final Boolean replaceValueByContext
    ) {
        final List<FormularRowDto> result = new ArrayList<>();

        if (rows != null) {
            for (final DocumentRowComposing currentRow : rows) {
                result.add(transformDocumentRow(currentRow.getRow(), context, replaceValueByContext));
            }
        }

        return result;
    }

    private FormularRowDto transformDocumentRow(
            final DocumentRow currentRow,
            final Map<String, Object> context,
            final Boolean replaceValueByContext
    ) {
        return FormularRowDto.builder()
                .id(currentRow.getId())
                .align(transformRowAlign(currentRow.getAlign()))
                .items(transformDocumentFieldToFormularFieldDto(currentRow.getFields(), context, replaceValueByContext))
                .build();
    }

    private List<FormularRowDto> transformDocumentActivityPriceListItemRowToFormularRowDto(
            final List<ActivityPriceListItemRowComposing> rows,
            final Map<String, Object> context,
            final Boolean replaceValueByContext
    ) {
        final List<FormularRowDto> result = new ArrayList<>();

        if (rows != null) {
            for (final ActivityPriceListItemRowComposing currentRow : rows) {
                result.add(FormularRowDto.builder()
                        .id(currentRow.getId())
                        .align(transformRowAlign(currentRow.getRow().getAlign()))
                        .items(transformDocumentFieldToFormularFieldDto(currentRow.getRow().getFields(), context, replaceValueByContext))
                        .build()
                );
            }
        }

        return result;
    }

    private List<FormularRowDto> transformDocumentActivityPriceListGroupRowToFormularRowDto(
            final List<ActivityPriceListGroupRowComposing> rows,
            final Map<String, Object> context,
            final Boolean replaceValueByContext
    ) {
        final List<FormularRowDto> result = new ArrayList<>();

        if (rows != null) {
            for (final ActivityPriceListGroupRowComposing currentRow : rows) {
                result.add(FormularRowDto.builder()
                        .id(currentRow.getId())
                        .align(transformRowAlign(currentRow.getRow().getAlign()))
                        .items(transformDocumentFieldToFormularFieldDto(currentRow.getRow().getFields(), context, replaceValueByContext))
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
                } else if (fieldDefinitionType instanceof final ActivityPriceListFieldDefinitionType castedFieldDefinitionType) {
                    final List<FormularRowDto> resultRows = new ArrayList<>();
                    if (Boolean.TRUE.equals(replaceValueByContext)) {

                        final List<Activity> priceListActivities = this.activityService.findAll(ActivitySearchOptions.builder()
                                .havingRelevanceForPriceListOnly(true)
                                .search("")
                                .build()
                        );

                        try {
                            if (ActivityPriceListGroupBy.CATEGORY.equals(castedFieldDefinitionType.getGroupBy())) {

                                final Map<Long, List<Activity>> groupByMap = new HashMap<>();

                                for (final Activity currentObj : priceListActivities) {
                                    final Context tempCtx = new Context();
                                    for (final String variableName : thymeleafCtx.getVariableNames()) {
                                        tempCtx.setVariable(variableName, thymeleafCtx.getVariable(variableName));
                                    }
                                    tempCtx.setVariable("activity", currentObj);

                                    long groupBy;
                                    try {
                                        groupBy = Long.parseLong(templateEngine.process(
                                                ActivityPriceListGroupBy.CATEGORY.getGroupBySelector(),
                                                tempCtx
                                        ));
                                    } catch (final NumberFormatException nfe) {
                                        groupBy = 0L;
                                    }

                                    if (groupByMap.containsKey(groupBy)) {
                                        groupByMap.get(groupBy).add(currentObj);
                                    } else {
                                        final List<Activity> objects = new ArrayList<>();
                                        objects.add(currentObj);
                                        groupByMap.put(groupBy, objects);
                                    }
                                }

                                final List<Long> sortedIds = new ArrayList<>();
                                final List<Long> sortedGroups = new ArrayList<>(groupByMap.keySet());
                                if (castedFieldDefinitionType.getSortByIds() != null) {

                                    for (final ActivityPriceListFieldSort sortById : castedFieldDefinitionType.getSortByIds()) {
                                        sortedIds.add(sortById.getSortById());
                                    }

                                    sortedGroups.sort(Comparator.comparingInt(sortedIds::indexOf));
                                }

                                for (final Long currentGroupSortId : sortedGroups) {
                                    List<Activity> currentGroupBy = groupByMap.get(currentGroupSortId);

                                    for (final ActivityPriceListGroupRowComposing groupRow : castedFieldDefinitionType.getGroupRows()) {
                                        // 1. build groupBy header
                                        final Map<String, Object> tempContextForGroup = new HashMap<>(context);
                                        tempContextForGroup.put("activity", currentGroupBy.get(0));
                                        resultRows.add(transformDocumentRow(
                                                groupRow.getRow(),
                                                tempContextForGroup,
                                                replaceValueByContext
                                        ));

                                        currentGroupBy.sort(Comparator.comparingInt(o -> sortedIds.indexOf(o.getId())));

                                        // 2. for each group by header, build group items
                                        for (final Activity currentObj : currentGroupBy) {

                                            for (final ActivityPriceListItemRowComposing itemRow : castedFieldDefinitionType.getItemRows()) {
                                                final Map<String, Object> tempContextForItem = new HashMap<>(context);
                                                tempContextForItem.put("activity", currentObj);
                                                resultRows.add(transformDocumentRow(
                                                        itemRow.getRow(),
                                                        tempContextForItem,
                                                        replaceValueByContext
                                                ));
                                            }
                                        }
                                    }
                                }
                            } else {
                                // no groupby specified -> ignore group by
                                for (final Object currentObj : priceListActivities) {

                                    for (final ActivityPriceListItemRowComposing itemRow : castedFieldDefinitionType.getItemRows()) {
                                        final Map<String, Object> tempContext = new HashMap<>(context);
                                        tempContext.put("activity", currentObj);
                                        resultRows.add(transformDocumentRow(
                                                itemRow.getRow(),
                                                tempContext,
                                                replaceValueByContext
                                        ));
                                    }
                                }
                            }
                        } catch (final Exception e) {
                            log.error("Unable to parse priceListActivities", e);
                        }
                    }

                    final List<Long> resultSortIds = new ArrayList<>();
                    final List<ActivityPriceListFieldSort> sortInfo = castedFieldDefinitionType.getSortByIds();
                    if (sortInfo != null) {
                        for (final ActivityPriceListFieldSort currentActivityPriceListFieldSort : sortInfo) {
                            resultSortIds.add(currentActivityPriceListFieldSort.getSortById());
                        }
                    }

                    result.add(ActivityPriceListFormularItemDto.builder()
                            .id(currentField.getId())
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .align(transformDocumentFieldAlignToFormularItemAlign(currentField))
                            .fieldDefinitionTypeId(fieldDefinitionType.getId())
                            .itemRows(transformDocumentActivityPriceListItemRowToFormularRowDto(
                                    castedFieldDefinitionType.getItemRows(),
                                    context,
                                    replaceValueByContext
                            ))
                            .groupRows(transformDocumentActivityPriceListGroupRowToFormularRowDto(
                                    castedFieldDefinitionType.getGroupRows(),
                                    context,
                                    replaceValueByContext
                            ))
                            .groupBy(transformActivityPriceListGroupByToDto(castedFieldDefinitionType.getGroupBy()))
                            .evaluatedData(!resultRows.isEmpty() ? resultRows : null)
                            .sortByIds(resultSortIds)
                            .build()
                    );
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
                        isCheckboxChecked = Boolean.TRUE.equals(((CheckboxFieldDefinitionType) fieldDefinitionType).getValue());
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
                            .readOnly(Boolean.TRUE.equals(((CheckboxFieldDefinitionType) fieldDefinitionType).getReadOnly()))
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
                            .readOnly(Boolean.TRUE.equals(castedField.getReadOnly()))
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

    private ActivityPriceListGroupByDto transformActivityPriceListGroupByToDto(ActivityPriceListGroupBy groupBy) {
        ActivityPriceListGroupByDto result = null;

        if (groupBy != null) {
            switch (groupBy) {
                case CATEGORY:
                    result = ActivityPriceListGroupByDto.CATEGORY;
                    break;
            }
        }

        return result;
    }

    private ActivityPriceListGroupBy transformActivityPriceListGroupByFromDto(ActivityPriceListGroupByDto groupBy) {
        ActivityPriceListGroupBy result = null;

        if (groupBy != null) {
            switch (groupBy) {
                case CATEGORY:
                    result = ActivityPriceListGroupBy.CATEGORY;
                    break;
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

    private List<DocumentRowComposing> transformFormularRowDtoToDocumentRow(final DynamicDocument document, final List<FormularRowDto> rows) {
        final List<DocumentRowComposing> result = new ArrayList<>();

        if (rows != null) {
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                FormularRowDto currentRow = rows.get(rowIndex);
                final DocumentRow newRow = DocumentRow.builder()
                        .align(transformRowAlign(currentRow.getAlign()))
                        .build();
                newRow.setFields(transformFormularItemToDocumentField(newRow, currentRow.getItems()));
                result.add(DocumentRowComposing.builder()
                        .document(document)
                        .row(newRow)
                        .positionIndex(rowIndex)
                        .build()
                );
            }
        }

        return result;
    }

    private List<ActivityPriceListItemRowComposing> transformActivityPriceListItemRowComposing(
            final ActivityPriceListFieldDefinitionType field,
            final List<FormularRowDto> rows
    ) {
        final List<ActivityPriceListItemRowComposing> result = new ArrayList<>();

        if (rows != null) {
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                FormularRowDto currentRow = rows.get(rowIndex);
                final DocumentRow newRow = DocumentRow.builder()
                        .align(transformRowAlign(currentRow.getAlign()))
                        .build();
                newRow.setFields(transformFormularItemToDocumentField(newRow, currentRow.getItems()));
                result.add(ActivityPriceListItemRowComposing.builder()
                        .fieldDefinition(field)
                        .row(newRow)
                        .positionIndex(rowIndex)
                        .build()
                );
            }
        }

        return result;
    }

    private List<ActivityPriceListGroupRowComposing> transformActivityPriceListGroupRowComposing(
            final ActivityPriceListFieldDefinitionType field,
            final List<FormularRowDto> rows
    ) {
        final List<ActivityPriceListGroupRowComposing> result = new ArrayList<>();

        if (rows != null) {
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                FormularRowDto currentRow = rows.get(rowIndex);
                final DocumentRow newRow = DocumentRow.builder()
                        .align(transformRowAlign(currentRow.getAlign()))
                        .build();
                newRow.setFields(transformFormularItemToDocumentField(newRow, currentRow.getItems()));
                result.add(ActivityPriceListGroupRowComposing.builder()
                        .fieldDefinition(field)
                        .row(newRow)
                        .positionIndex(rowIndex)
                        .build()
                );
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
                if (currentField instanceof final SVGFormularItemDto castedField) {
                    result.add(DocumentField.builder()
                            .row(newRow)
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .positionIndex(fieldIndex)
                            .fieldDefinitionType(SVGFieldDefinitionType.builder()
                                    .maxWidthInPx(castedField.getMaxWidthInPx())
                                    .widthPercentage(castedField.getWidthPercentage())
                                    .svgContentBase64encoded(castedField.getSvgContentBase64encoded())
                                    .build())
                            .alignment(transformFormularItemAlignToDocumentFieldAlign(currentField))
                            .build());
                } else if (currentField instanceof final ActivityPriceListFormularItemDto castedField) {
                    final ActivityPriceListFieldDefinitionType newField = ActivityPriceListFieldDefinitionType.builder()
                            .groupBy(transformActivityPriceListGroupByFromDto(castedField.getGroupBy()))
                            .build();
                    newField.setItemRows(transformActivityPriceListItemRowComposing(
                            newField,
                            castedField.getItemRows())
                    );
                    newField.setGroupRows(transformActivityPriceListGroupRowComposing(
                            newField,
                            castedField.getGroupRows())
                    );
                    final List<ActivityPriceListFieldSort> sortInfo = new ArrayList<>();
                    if (castedField.getSortByIds() != null) {
                        List<Long> sortByIds = castedField.getSortByIds();
                        for (int sortByIdx = 0; sortByIdx < sortByIds.size(); sortByIdx++) {
                            Long sortById = sortByIds.get(sortByIdx);
                            sortInfo.add(ActivityPriceListFieldSort.builder()
                                    .positionIndex(sortByIdx)
                                    .sortById(sortById)
                                    .fieldDefinition(newField)
                                    .build()
                            );
                        }
                    }
                    newField.setSortByIds(sortInfo);

                    result.add(DocumentField.builder()
                            .row(newRow)
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .positionIndex(fieldIndex)
                            .fieldDefinitionType(newField)
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
                                    .build())
                            .alignment(transformFormularItemAlignToDocumentFieldAlign(currentField))
                            .build());
                } else if (currentField instanceof final TextFormularItemDto castedField) {
                    result.add(DocumentField.builder()
                            .row(newRow)
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .positionIndex(fieldIndex)
                            .fieldDefinitionType(TextFieldDefinitionType.builder()
                                    .text(castedField.getText())
                                    .readOnly(castedField.isReadOnly())
                                    .fontSize(castedField.getFontSize())
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
                                    .content(castedField.getValue())
                                    .maxWidthInPx(castedField.getMaxWidthInPx())
                                    .widthPercentage(castedField.getWidthPercentage())
                                    .build()
                            )
                            .alignment(transformFormularItemAlignToDocumentFieldAlign(currentField))
                            .build());
                } else if (currentField instanceof final DateFormularItemDto castedField) {
                    result.add(DocumentField.builder()
                            .row(newRow)
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .positionIndex(fieldIndex)
                            .fieldDefinitionType(DateFieldDefinitionType.builder()
                                    .value(castedField.getValue())
                                    .context(castedField.getContext())
                                    .fontSize(castedField.getFontSize())
                                    .dayDiff(castedField.getDayDiff())
                                    .monthDiff(castedField.getMonthDiff())
                                    .yearDiff(castedField.getYearDiff())
                                    .build()
                            )
                            .alignment(transformFormularItemAlignToDocumentFieldAlign(currentField))
                            .build());
                } else if (currentField instanceof final CheckboxFormularItemDto castedField) {
                    result.add(DocumentField.builder()
                            .row(newRow)
                            .xs(currentField.getXs())
                            .sm(currentField.getSm())
                            .md(currentField.getMd())
                            .lg(currentField.getLg())
                            .xl(currentField.getXl())
                            .positionIndex(fieldIndex)
                            .fieldDefinitionType(CheckboxFieldDefinitionType.builder()
                                    .context(castedField.getContext())
                                    .value(castedField.isValue())
                                    .readOnly(castedField.isReadOnly())
                                    .label(castedField.getLabel())
                                    .fontSize(castedField.getFontSize())
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
