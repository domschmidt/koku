package de.domschmidt.koku.customer.controller;

import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import de.domschmidt.chart.dto.response.axes.AxesDto;
import de.domschmidt.chart.dto.response.axis.CategoricalXAxisDto;
import de.domschmidt.chart.dto.response.axis.YAxisDto;
import de.domschmidt.chart.dto.response.colors.ColorsEnumDto;
import de.domschmidt.chart.dto.response.types.*;
import de.domschmidt.chart.dto.response.values.NumericSeriesDto;
import de.domschmidt.dashboard.dto.DashboardViewDto;
import de.domschmidt.dashboard.dto.content.panels.DashboardAsyncChartPanelDto;
import de.domschmidt.dashboard.factory.DashboardViewFactory;
import de.domschmidt.dashboard.factory.DefaultDashboardViewContentIdGenerator;
import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.formular.dto.content.buttons.EnumButtonType;
import de.domschmidt.formular.dto.content.buttons.FormButtonReloadAction;
import de.domschmidt.formular.factory.DefaultViewContentIdGenerator;
import de.domschmidt.formular.factory.FormViewFactory;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionCloseButtonDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionSendToDifferentEndpointButtonDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionWithConfirmationMessageDto;
import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.business_logic.dto.*;
import de.domschmidt.koku.customer.exceptions.*;
import de.domschmidt.koku.customer.kafka.activities.service.ActivityKTableProcessor;
import de.domschmidt.koku.customer.kafka.activity_steps.service.ActivityStepKTableProcessor;
import de.domschmidt.koku.customer.kafka.customers.service.CustomerAppointmentKafkaService;
import de.domschmidt.koku.customer.kafka.productmanufacturers.service.ProductManufacturerKTableProcessor;
import de.domschmidt.koku.customer.kafka.products.service.ProductKTableProcessor;
import de.domschmidt.koku.customer.kafka.promotions.service.PromotionKTableProcessor;
import de.domschmidt.koku.customer.kafka.users.service.UserKTableProcessor;
import de.domschmidt.koku.customer.persistence.*;
import de.domschmidt.koku.customer.transformer.CustomerAppointmentToCustomerAppointmentDtoTransformer;
import de.domschmidt.koku.dto.KokuColorEnum;
import de.domschmidt.koku.dto.activity.KokuActivityDto;
import de.domschmidt.koku.dto.activity.KokuActivityStepDto;
import de.domschmidt.koku.dto.chart.filter.types.EnumInputChartFilterType;
import de.domschmidt.koku.dto.chart.filter.types.InputChartFilterDto;
import de.domschmidt.koku.dto.customer.*;
import de.domschmidt.koku.dto.dashboard.containers.grid.DashboardGridContainerDto;
import de.domschmidt.koku.dto.dashboard.panels.text.DashboardAsyncTextPanelDto;
import de.domschmidt.koku.dto.dashboard.panels.text.DashboardTextPanelDto;
import de.domschmidt.koku.dto.dashboard.panels.text.DashboardTextPanelExplanationItemDto;
import de.domschmidt.koku.dto.dashboard.panels.text.DashboardTextPanelProgressDetailsDto;
import de.domschmidt.koku.dto.formular.buttons.ButtonDockableSettings;
import de.domschmidt.koku.dto.formular.buttons.EnumButtonStyle;
import de.domschmidt.koku.dto.formular.buttons.KokuFormButton;
import de.domschmidt.koku.dto.formular.containers.fieldset.FieldsetContainer;
import de.domschmidt.koku.dto.formular.containers.grid.GridContainer;
import de.domschmidt.koku.dto.formular.fields.input.EnumInputFormularFieldType;
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
import de.domschmidt.koku.dto.formular.fields.multi_select.MultiSelectFormularField;
import de.domschmidt.koku.dto.formular.fields.multi_select.MultiSelectFormularFieldPossibleValue;
import de.domschmidt.koku.dto.formular.fields.multi_select_with_pricing_adjustment.MultiSelectWithPricingAdjustmentFormularField;
import de.domschmidt.koku.dto.formular.fields.multi_select_with_pricing_adjustment.MultiSelectWithPricingAdjustmentFormularFieldPossibleValue;
import de.domschmidt.koku.dto.formular.fields.select.SelectFormularField;
import de.domschmidt.koku.dto.formular.fields.select.SelectFormularFieldPossibleValue;
import de.domschmidt.koku.dto.formular.fields.slots.KokuFieldSlotButton;
import de.domschmidt.koku.dto.formular.fields.stat.StatFormularField;
import de.domschmidt.koku.dto.formular.fields.textarea.TextareaFormularField;
import de.domschmidt.koku.dto.formular.listeners.*;
import de.domschmidt.koku.dto.list.items.style.ListViewConditionalItemValueStylingDto;
import de.domschmidt.koku.dto.list.items.style.ListViewItemStylingDto;
import de.domschmidt.koku.dto.product.KokuProductDto;
import de.domschmidt.koku.dto.promotion.KokuPromotionDto;
import de.domschmidt.list.dto.response.ListViewDto;
import de.domschmidt.list.dto.response.ListViewSourcePathReference;
import de.domschmidt.list.dto.response.actions.ListViewOpenRoutedContentActionDto;
import de.domschmidt.list.dto.response.actions.ListViewUserConfirmationDateValueParamDto;
import de.domschmidt.list.dto.response.actions.ListViewUserConfirmationDto;
import de.domschmidt.list.dto.response.actions.ListViewUserConfirmationValueParamDto;
import de.domschmidt.list.dto.response.events.ListViewEventPayloadAddItemGlobalEventListenerDto;
import de.domschmidt.list.dto.response.events.ListViewEventPayloadItemUpdateGlobalEventListenerDto;
import de.domschmidt.list.dto.response.fields.ListViewFieldReference;
import de.domschmidt.list.dto.response.fields.input.ListViewInputFieldDto;
import de.domschmidt.list.dto.response.inline_content.ListViewRoutedContentDto;
import de.domschmidt.list.dto.response.inline_content.formular.*;
import de.domschmidt.list.dto.response.inline_content.header.ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto;
import de.domschmidt.list.dto.response.inline_content.header.ListViewHeaderContentDto;
import de.domschmidt.list.dto.response.items.ListViewRoutedDummyItemDto;
import de.domschmidt.list.dto.response.items.actions.ListViewConditionalItemValueActionDto;
import de.domschmidt.list.dto.response.items.actions.ListViewFormularActionSubmitMethodEnumDto;
import de.domschmidt.list.dto.response.items.actions.call_http.ListViewCallHttpListItemActionDto;
import de.domschmidt.list.dto.response.items.actions.call_http.ListViewCallHttpListItemActionMethodEnumDto;
import de.domschmidt.list.dto.response.items.actions.call_http.ListViewCallHttpListValueActionParamDto;
import de.domschmidt.list.dto.response.items.actions.inline_content.ListViewItemClickOpenRoutedContentActionDto;
import de.domschmidt.list.dto.response.items.actions.inline_content.ListViewItemClickOpenRoutedContentActionItemValueParamDto;
import de.domschmidt.list.dto.response.notifications.*;
import de.domschmidt.list.factory.DefaultListViewContentIdGenerator;
import de.domschmidt.list.factory.ListViewFactory;
import de.domschmidt.listquery.dto.request.ListQuery;
import de.domschmidt.listquery.dto.response.ListPage;
import de.domschmidt.listquery.factory.ListQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static de.domschmidt.koku.customer.persistence.QCustomer.customer;

@RestController
@RequestMapping()
@Slf4j
public class CustomerAppointmentController {
    public static final DateTimeFormatter YEAR_MONTH_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private final EntityManager entityManager;
    private final CustomerAppointmentRepository customerAppointmentRepository;
    private final CustomerAppointmentKafkaService customerAppointmentKafkaService;
    private final CustomerAppointmentToCustomerAppointmentDtoTransformer transformer;
    private final ActivityKTableProcessor activityKTableProcessor;
    private final ActivityStepKTableProcessor activityStepKTableProcessor;
    private final ProductKTableProcessor productKTableProcessor;
    private final ProductManufacturerKTableProcessor productManufacturerKTableProcessor;
    private final PromotionKTableProcessor promotionKTableProcessor;
    private final UserKTableProcessor userKTableProcessor;

    public CustomerAppointmentController(
            final EntityManager entityManager,
            final CustomerAppointmentRepository customerAppointmentRepository,
            final CustomerAppointmentKafkaService customerAppointmentKafkaService,
            final CustomerAppointmentToCustomerAppointmentDtoTransformer transformer,
            final ActivityKTableProcessor activityKTableProcessor,
            final ActivityStepKTableProcessor activityStepKTableProcessor,
            final ProductKTableProcessor productKTableProcessor,
            final ProductManufacturerKTableProcessor productManufacturerKTableProcessor,
            final PromotionKTableProcessor promotionKTableProcessor,
            final UserKTableProcessor userKTableProcessor
    ) {
        this.entityManager = entityManager;
        this.customerAppointmentRepository = customerAppointmentRepository;
        this.customerAppointmentKafkaService = customerAppointmentKafkaService;
        this.transformer = transformer;
        this.activityKTableProcessor = activityKTableProcessor;
        this.activityStepKTableProcessor = activityStepKTableProcessor;
        this.productKTableProcessor = productKTableProcessor;
        this.productManufacturerKTableProcessor = productManufacturerKTableProcessor;
        this.promotionKTableProcessor = promotionKTableProcessor;
        this.userKTableProcessor = userKTableProcessor;
    }

    @GetMapping("/customers/appointments/activitysummary")
    public KokuCustomerActivityPriceSummaryDto getActivitySum(
            KokuActivityPriceSummaryRequestDto request
    ) {
        return transformer.transformToActivityPriceSummary(request);
    }

    @GetMapping("/customers/appointments/productsummary")
    public KokuActivitySoldProductPriceSummaryDto getProductSum(
            KokuActivitySoldProductSummaryRequestDto request
    ) {
        return this.transformer.transformToSoldProductPriceSummary(request);
    }

    @GetMapping("/customers/appointments/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory(
                new DefaultViewContentIdGenerator(),
                GridContainer.builder()
                        .cols(1)
                        .build()
        );
        final QCustomer qCustomer = customer;
        final List<Customer> customersSnapshot = new JPAQuery<>(this.entityManager)
                .select(qCustomer)
                .from(qCustomer)
                .fetch();
        final String customerSelectionFieldRef = formFactory.addField(SelectFormularField.builder()
                .valuePath(KokuCustomerAppointmentDto.Fields.customerId)
                .label("Kunde")
                .id(KokuCustomerAppointmentDto.Fields.customerId)
                .possibleValues(customersSnapshot.stream().map(customer -> {
                    return SelectFormularFieldPossibleValue.builder()
                            .id(customer.getId() + "")
                            .text(Stream.of(customer.getFirstname(), customer.getLastname())
                                    .filter(s -> s != null && !s.isEmpty())
                                    .collect(Collectors.joining(", ")))
                            .disabled(customer.isDeleted())
                            .build();
                }).toList())
                .appendOuter(KokuFieldSlotButton.builder()
                        .icon("PLUS")
                        .buttonType(EnumButtonType.BUTTON)
                        .title("Neuer Kunde anlegen")
                        .build()
                )
                .required(true)
                .build()
        );
        formFactory.addBusinessRule(KokuBusinessRuleDto.builder()
                .id("CreateCustomer")
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(customerSelectionFieldRef)
                        .listener(KokuBusinessRuleFieldReferenceListenerDto.builder()
                                .event(KokuBusinessRuleFieldReferenceListenerEventEnum.CLICK_APPEND_OUTER)
                                .build())
                        .build())
                .execution(
                        KokuBusinessRuleOpenDialogContentDto.builder()
                                .content(KokuBusinessRuleHeaderContentDto.builder()
                                        .title("Neuer Kunde")
                                        .content(
                                                KokuBusinessRuleFormularContentDto.builder()
                                                        .formularUrl("services/customers/customers/form")
                                                        .submitUrl("services/customers/customers")
                                                        .submitMethod(KokuBusinessRuleFormularActionSubmitMethodEnumDto.POST)
                                                        .maxWidthInPx(800)
                                                        .onSaveEvents(Arrays.asList(
                                                                KokuBusinessRuleFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                                        .eventName("customer-created")
                                                                        .build()
                                                        ))
                                                        .build()
                                        )
                                        .build()
                                )
                                .closeEventListener(KokuBusinessRuleOpenContentCloseGlobalEventListenerDto.builder()
                                        .eventName("customer-created")
                                        .build()
                                )
                                .build()
                )
                .build()
        );
        formFactory.addGlobalEventListener(FormViewEventPayloadFieldUpdateGlobalEventListenerDto.builder()
                .eventName("customer-created")
                .fieldValueMapping(Map.of(
                        customerSelectionFieldRef,
                        FormViewFieldReferenceValueMapping.builder()
                                .source(FormViewEventPayloadSourcePathFieldUpdateValueSourceDto.builder()
                                        .sourcePath(KokuCustomerDto.Fields.id)
                                        .build()
                                )
                                .build()
                ))
                .configMapping(Map.of(
                        customerSelectionFieldRef,
                        FormViewFieldConfigMapping.builder()
                                .targetConfigPath(SelectFormularField.Fields.possibleValues)
                                .valueMapping(ConfigMappingAppendListDto.builder()
                                        .valueMapping(List.of(
                                                StringConversionConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuCustomerDto.Fields.id)
                                                        .targetPath(SelectFormularFieldPossibleValue.Fields.id)
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuCustomerDto.Fields.fullNameWithOnFirstNameBasis)
                                                        .targetPath(SelectFormularFieldPossibleValue.Fields.text)
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuCustomerDto.Fields.deleted)
                                                        .targetPath(SelectFormularFieldPossibleValue.Fields.disabled)
                                                        .build()
                                        ))
                                        .build())
                                .build()
                ))
                .build()
        );

        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .md(2)
                .build()
        );
        final String dateFieldRef = formFactory.addField(InputFormularField.builder()
                .valuePath(KokuCustomerAppointmentDto.Fields.date)
                .type(EnumInputFormularFieldType.DATE)
                .label("Datum")
                .required(true)
                .build()
        );
        final String timeFieldRef = formFactory.addField(InputFormularField.builder()
                .valuePath(KokuCustomerAppointmentDto.Fields.time)
                .type(EnumInputFormularFieldType.TIME)
                .label("Zeit")
                .required(true)
                .build()
        );
        formFactory.endContainer();

        formFactory.addContainer(FieldsetContainer.builder()
                .title("Tätigkeiten")
                .build()
        );
        final String activityFieldRef = formFactory.addField(MultiSelectWithPricingAdjustmentFormularField.builder()
                .valuePath(KokuCustomerAppointmentDto.Fields.activities)
                .idPathMapping(KokuCustomerAppointmentActivityDto.Fields.activityId)
                .pricePathMapping(KokuCustomerAppointmentActivityDto.Fields.price)
                .placeholder("Weitere Tätigkeiten...")
                .possibleValues(
                        StreamSupport.stream(
                                        Spliterators.spliteratorUnknownSize(this.activityKTableProcessor.getActivities().all(), Spliterator.DISTINCT),
                                        false
                                )
                                .map(activity -> MultiSelectWithPricingAdjustmentFormularFieldPossibleValue.builder()
                                        .id(activity.key + "")
                                        .text(activity.value.getName())
                                        .disabled(Boolean.TRUE.equals(activity.value.getDeleted()))
                                        .build()
                                )
                                .toList()
                )
                .appendOuter(KokuFieldSlotButton.builder()
                        .icon("PLUS")
                        .buttonType(EnumButtonType.BUTTON)
                        .title("Neue Tätigkeit anlegen")
                        .build()
                )
                .uniqueValues(true)
                .build()
        );
        formFactory.addBusinessRule(KokuBusinessRuleDto.builder()
                .id("CreateActivity")
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(activityFieldRef)
                        .listener(KokuBusinessRuleFieldReferenceListenerDto.builder()
                                .event(KokuBusinessRuleFieldReferenceListenerEventEnum.CLICK_APPEND_OUTER)
                                .build())
                        .build())
                .execution(
                        KokuBusinessRuleOpenDialogContentDto.builder()
                                .content(KokuBusinessRuleHeaderContentDto.builder()
                                        .title("Neue Tätigkeit")
                                        .content(
                                                KokuBusinessRuleFormularContentDto.builder()
                                                        .formularUrl("services/activities/activities/form")
                                                        .submitUrl("services/activities/activities")
                                                        .submitMethod(KokuBusinessRuleFormularActionSubmitMethodEnumDto.POST)
                                                        .maxWidthInPx(800)
                                                        .onSaveEvents(Arrays.asList(
                                                                KokuBusinessRuleFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                                        .eventName("activity-created")
                                                                        .build()
                                                        ))
                                                        .build()
                                        )
                                        .build()
                                )
                                .closeEventListener(KokuBusinessRuleOpenContentCloseGlobalEventListenerDto.builder()
                                        .eventName("activity-created")
                                        .build()
                                )
                                .build()
                )
                .build()
        );
        formFactory.addGlobalEventListener(FormViewEventPayloadFieldUpdateGlobalEventListenerDto.builder()
                .eventName("activity-created")
                .fieldValueMapping(Map.of(
                        activityFieldRef,
                        FormViewFieldReferenceMultiSelectValueMapping.builder()
                                .targetPathMapping(Map.of(
                                        KokuCustomerAppointmentActivityDto.Fields.activityId,
                                        FormViewEventPayloadSourcePathFieldUpdateValueSourceDto.builder()
                                                .sourcePath(KokuActivityDto.Fields.id)
                                                .build()
                                ))
                                .build()
                ))
                .configMapping(Map.of(
                        activityFieldRef,
                        FormViewFieldConfigMapping.builder()
                                .targetConfigPath(SelectFormularField.Fields.possibleValues)
                                .valueMapping(ConfigMappingAppendListDto.builder()
                                        .valueMapping(List.of(
                                                StringConversionConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuActivityDto.Fields.id)
                                                        .targetPath(SelectFormularFieldPossibleValue.Fields.id)
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuActivityDto.Fields.name)
                                                        .targetPath(SelectFormularFieldPossibleValue.Fields.text)
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuActivityDto.Fields.deleted)
                                                        .targetPath(SelectFormularFieldPossibleValue.Fields.disabled)
                                                        .build()
                                        ))
                                        .build())
                                .build()
                ))
                .build()
        );

        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .xl2(2)
                .build()
        );

        final String activityPriceSumStatFieldRef = formFactory.addField(StatFormularField.builder()
                .title("Tätigkeitskosten")
                .description("Erwartete Einnahme")
                .valuePath(KokuCustomerAppointmentDto.Fields.activityPriceSummary)
                .icon("CURRENCY_EURO")
                .build()
        );
        final String activityDurationSumStatFieldRef = formFactory.addField(StatFormularField.builder()
                .title("Tätigkeitsdauer")
                .description("Erwartete Dauer")
                .valuePath(KokuCustomerAppointmentDto.Fields.activityDurationSummary)
                .icon("CLOCK")
                .build()
        );

        formFactory.endContainer();
        formFactory.endContainer();

        final List<MultiSelectFormularFieldPossibleValue> activityStepAndProductPossibleValuesUnion = new ArrayList<>();
        activityStepAndProductPossibleValuesUnion.addAll(
                StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(this.activityStepKTableProcessor.getActivitySteps().all(), Spliterator.DISTINCT),
                        false
                ).map(activityStep -> {
                    return MultiSelectFormularFieldPossibleValue.builder()
                            .id("activity_step_" + activityStep.key)
                            .valueMapping(KokuCustomerAppointmentActivityStepTreatmentDto.builder()
                                    .activityStepId(activityStep.key)
                                    .build()
                            )
                            .text(activityStep.value.getName())
                            .disabled(Boolean.TRUE.equals(activityStep.value.getDeleted()))
                            .color(KokuColorEnum.PRIMARY)
                            .category("Behandlungsschritte")
                            .build();
                }).toList()
        );
        activityStepAndProductPossibleValuesUnion.addAll(StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(this.productKTableProcessor.getProducts().all(), Spliterator.DISTINCT),
                        false
                ).map(product -> {
                    return MultiSelectFormularFieldPossibleValue.builder()
                            .id("product_" + product.key)
                            .valueMapping(KokuCustomerAppointmentProductTreatmentDto.builder()
                                    .productId(product.key)
                                    .build()
                            )
                            .text(String.format("%s / %s", this.productManufacturerKTableProcessor.getProductManufacturers().get(product.value.getManufacturerId()).getName(), product.value.getName()))
                            .disabled(Boolean.TRUE.equals(product.value.getDeleted()))
                            .color(KokuColorEnum.SECONDARY)
                            .category("Produkte")
                            .build();
                }).toList()
        );

        final String treatmentSequenceFieldRef = formFactory.addField(MultiSelectFormularField.builder()
                .valuePath(KokuCustomerAppointmentDto.Fields.treatmentSequence)
                .label("Behandlungsfolge")
                .placeholder("Weitere Behandlungen...")
                .possibleValues(activityStepAndProductPossibleValuesUnion)
                .appendOuter(KokuFieldSlotButton.builder()
                        .icon("PLUS")
                        .buttonType(EnumButtonType.BUTTON)
                        .title("Neue Behandlung anlegen")
                        .build()
                )
                .uniqueValues(false)
                .build()
        );
        formFactory.addBusinessRule(KokuBusinessRuleDto.builder()
                .id("CreateTreatment")
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(treatmentSequenceFieldRef)
                        .listener(KokuBusinessRuleFieldReferenceListenerDto.builder()
                                .event(KokuBusinessRuleFieldReferenceListenerEventEnum.CLICK_APPEND_OUTER)
                                .build())
                        .build())
                .execution(
                        KokuBusinessRuleOpenDialogContentDto.builder()
                                .content(KokuBusinessRuleDockContentDto.builder()
                                        .content(List.of(
                                                KokuBusinessRuleDockContentItemDto.builder()
                                                        .id("products")
                                                        .title("Produkt")
                                                        .content(KokuBusinessRuleHeaderContentDto.builder()
                                                                .title("Neues Produkt")
                                                                .content(
                                                                        KokuBusinessRuleFormularContentDto.builder()
                                                                                .formularUrl("services/products/products/form")
                                                                                .submitUrl("services/products/products")
                                                                                .submitMethod(KokuBusinessRuleFormularActionSubmitMethodEnumDto.POST)
                                                                                .maxWidthInPx(800)
                                                                                .onSaveEvents(Arrays.asList(
                                                                                        KokuBusinessRuleFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                                                                .eventName("treatment-product-created")
                                                                                                .build()
                                                                                ))
                                                                                .build()
                                                                )
                                                                .build()
                                                        ).build(),
                                                KokuBusinessRuleDockContentItemDto.builder()
                                                        .id("activitySteps")
                                                        .title("Behandlungsschritt")
                                                        .content(KokuBusinessRuleHeaderContentDto.builder()
                                                                .title("Neuer Behandlungsschritt")
                                                                .content(KokuBusinessRuleFormularContentDto.builder()
                                                                        .formularUrl("services/activities/activitysteps/form")
                                                                        .submitUrl("services/activities/activitysteps")
                                                                        .submitMethod(KokuBusinessRuleFormularActionSubmitMethodEnumDto.POST)
                                                                        .maxWidthInPx(800)
                                                                        .onSaveEvents(Arrays.asList(
                                                                                KokuBusinessRuleFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                                                        .eventName("treatment-activitystep-created")
                                                                                        .build()
                                                                        ))
                                                                        .build()
                                                                )
                                                                .build()
                                                        )
                                                        .build()
                                        ))
                                        .build()
                                )
                                .closeEventListener(KokuBusinessRuleOpenContentCloseGlobalEventListenerDto.builder()
                                        .eventName("treatment-activitystep-created")
                                        .build()
                                )
                                .closeEventListener(KokuBusinessRuleOpenContentCloseGlobalEventListenerDto.builder()
                                        .eventName("treatment-product-created")
                                        .build()
                                )
                                .build()
                )
                .build()
        );
        formFactory.addGlobalEventListener(FormViewEventPayloadFieldUpdateGlobalEventListenerDto.builder()
                .eventName("treatment-activitystep-created")
                .fieldValueMapping(Map.of(
                        treatmentSequenceFieldRef,
                        FormViewFieldReferenceMultiSelectValueMapping.builder()
                                .targetPathMapping(Map.of(
                                        KokuCustomerAppointmentActivityStepTreatmentDto.Fields.activityStepId,
                                        FormViewEventPayloadSourcePathFieldUpdateValueSourceDto.builder()
                                                .sourcePath(KokuActivityStepDto.Fields.id)
                                                .build(),
                                        "@type",
                                        FormViewEventPayloadStaticValueFieldUpdateValueSourceDto.builder()
                                                .value("activity-step")
                                                .build()
                                ))
                                .build()
                ))
                .configMapping(Map.of(
                        treatmentSequenceFieldRef,
                        FormViewFieldConfigMapping.builder()
                                .targetConfigPath(SelectFormularField.Fields.possibleValues)
                                .valueMapping(ConfigMappingAppendListDto.builder()
                                        .valueMapping(List.of(
                                                StringTransformationConfigMappingAppendListItemDto.builder()
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.id)
                                                        .transformPattern("activity_step_{id}")
                                                        .transformPatternParameters(Map.of(
                                                                "{id}", StringTransformationSourcePathPatternParam.builder()
                                                                        .sourcePath(KokuActivityStepDto.Fields.id)
                                                                        .build()
                                                        ))
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuActivityStepDto.Fields.name)
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.text)
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuActivityStepDto.Fields.deleted)
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.disabled)
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuActivityStepDto.Fields.id)
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.valueMapping + '.' + KokuCustomerAppointmentActivityStepTreatmentDto.Fields.activityStepId)
                                                        .build(),
                                                StaticValueConfigMappingAppendListItemDto.builder()
                                                        .value("activity-step")
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.valueMapping + ".@type")
                                                        .build(),
                                                StaticValueConfigMappingAppendListItemDto.builder()
                                                        .value("Behandlungsschritte")
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.category)
                                                        .build()
                                        ))
                                        .build())
                                .build()
                ))
                .build()
        );
        formFactory.addGlobalEventListener(FormViewEventPayloadFieldUpdateGlobalEventListenerDto.builder()
                .eventName("treatment-product-created")
                .fieldValueMapping(Map.of(
                        treatmentSequenceFieldRef,
                        FormViewFieldReferenceMultiSelectValueMapping.builder()
                                .targetPathMapping(Map.of(
                                        KokuCustomerAppointmentProductTreatmentDto.Fields.productId,
                                        FormViewEventPayloadSourcePathFieldUpdateValueSourceDto.builder()
                                                .sourcePath(KokuProductDto.Fields.id)
                                                .build(),
                                        "@type",
                                        FormViewEventPayloadStaticValueFieldUpdateValueSourceDto.builder()
                                                .value("product")
                                                .build()
                                ))
                                .build()
                ))
                .configMapping(Map.of(
                        treatmentSequenceFieldRef,
                        FormViewFieldConfigMapping.builder()
                                .targetConfigPath(SelectFormularField.Fields.possibleValues)
                                .valueMapping(ConfigMappingAppendListDto.builder()
                                        .valueMapping(List.of(
                                                StringTransformationConfigMappingAppendListItemDto.builder()
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.id)
                                                        .transformPattern("product_{id}")
                                                        .transformPatternParameters(Map.of(
                                                                "{id}", StringTransformationSourcePathPatternParam.builder()
                                                                        .sourcePath(KokuProductDto.Fields.id)
                                                                        .build()
                                                        ))
                                                        .build(),
                                                StringTransformationConfigMappingAppendListItemDto.builder()
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.text)
                                                        .transformPattern("{manufacturerName} / {productName}")
                                                        .transformPatternParameters(Map.of(
                                                                "{manufacturerName}", StringTransformationSourcePathPatternParam.builder()
                                                                        .sourcePath(KokuProductDto.Fields.manufacturerName)
                                                                        .build(),
                                                                "{productName}", StringTransformationSourcePathPatternParam.builder()
                                                                        .sourcePath(KokuProductDto.Fields.name)
                                                                        .build()
                                                        ))
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuProductDto.Fields.deleted)
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.disabled)
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuProductDto.Fields.id)
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.valueMapping + '.' + KokuCustomerAppointmentProductTreatmentDto.Fields.productId)
                                                        .build(),
                                                StaticValueConfigMappingAppendListItemDto.builder()
                                                        .value("product")
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.valueMapping + ".@type")
                                                        .build(),
                                                StaticValueConfigMappingAppendListItemDto.builder()
                                                        .value("Produkte")
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.category)
                                                        .build()
                                        ))
                                        .build())
                                .build()
                ))
                .build()
        );

        formFactory.addContainer(FieldsetContainer.builder()
                .title("Produkte")
                .build()
        );
        String soldProductsFieldRef = formFactory.addField(MultiSelectWithPricingAdjustmentFormularField.builder()
                .valuePath(KokuCustomerAppointmentDto.Fields.soldProducts)
                .placeholder("Weitere Produkte...")
                .possibleValues(StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(this.productKTableProcessor.getProducts().all(), Spliterator.DISTINCT),
                                false
                        )
                        .sorted(Comparator.comparing(longProductKafkaDtoKeyValue -> longProductKafkaDtoKeyValue.value.getManufacturerId()))
                        .map(product -> {
                            return MultiSelectWithPricingAdjustmentFormularFieldPossibleValue.builder()
                                    .id(product.key + "")
                                    .text(String.format("%s / %s", this.productManufacturerKTableProcessor.getProductManufacturers().get(product.value.getManufacturerId()).getName(), product.value.getName()))
                                    .disabled(Boolean.TRUE.equals(product.value.getDeleted()))
                                    .build();
                        }).toList())
                .idPathMapping(KokuCustomerAppointmentSoldProductDto.Fields.productId)
                .pricePathMapping(KokuCustomerAppointmentSoldProductDto.Fields.price)
                .appendOuter(KokuFieldSlotButton.builder()
                        .icon("PLUS")
                        .buttonType(EnumButtonType.BUTTON)
                        .title("Neues Produkt anlegen")
                        .build()
                )
                .uniqueValues(false)
                .build()
        );
        formFactory.addBusinessRule(KokuBusinessRuleDto.builder()
                .id("CreateProduct")
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(soldProductsFieldRef)
                        .listener(KokuBusinessRuleFieldReferenceListenerDto.builder()
                                .event(KokuBusinessRuleFieldReferenceListenerEventEnum.CLICK_APPEND_OUTER)
                                .build())
                        .build())
                .execution(
                        KokuBusinessRuleOpenDialogContentDto.builder()
                                .content(KokuBusinessRuleHeaderContentDto.builder()
                                        .title("Neues Produkt")
                                        .content(
                                                KokuBusinessRuleFormularContentDto.builder()
                                                        .formularUrl("services/products/products/form")
                                                        .submitUrl("services/products/products")
                                                        .submitMethod(KokuBusinessRuleFormularActionSubmitMethodEnumDto.POST)
                                                        .maxWidthInPx(800)
                                                        .onSaveEvents(Arrays.asList(
                                                                KokuBusinessRuleFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                                        .eventName("product-created")
                                                                        .build()
                                                        ))
                                                        .build()
                                        )
                                        .build()
                                )
                                .closeEventListener(KokuBusinessRuleOpenContentCloseGlobalEventListenerDto.builder()
                                        .eventName("product-created")
                                        .build()
                                )
                                .build()
                )
                .build()
        );
        formFactory.addGlobalEventListener(FormViewEventPayloadFieldUpdateGlobalEventListenerDto.builder()
                .eventName("product-created")
                .fieldValueMapping(Map.of(
                        soldProductsFieldRef,
                        FormViewFieldReferenceMultiSelectValueMapping.builder()
                                .targetPathMapping(Map.of(
                                        KokuCustomerAppointmentSoldProductDto.Fields.productId,
                                        FormViewEventPayloadSourcePathFieldUpdateValueSourceDto.builder()
                                                .sourcePath(KokuProductDto.Fields.id)
                                                .build()
                                ))
                                .build()
                ))
                .configMapping(Map.of(
                        soldProductsFieldRef,
                        FormViewFieldConfigMapping.builder()
                                .targetConfigPath(SelectFormularField.Fields.possibleValues)
                                .valueMapping(ConfigMappingAppendListDto.builder()
                                        .valueMapping(List.of(
                                                StringConversionConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuProductDto.Fields.id)
                                                        .targetPath(SelectFormularFieldPossibleValue.Fields.id)
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuProductDto.Fields.name)
                                                        .targetPath(SelectFormularFieldPossibleValue.Fields.text)
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuProductDto.Fields.deleted)
                                                        .targetPath(SelectFormularFieldPossibleValue.Fields.disabled)
                                                        .build()
                                        ))
                                        .build())
                                .build()
                ))
                .build()
        );

        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .xl2(2)
                .build()
        );

        final String productPriceSumStatFieldRef = formFactory.addField(StatFormularField.builder()
                .title("Produktkosten")
                .description("Erwartete Einnahme")
                .valuePath(KokuCustomerAppointmentDto.Fields.activitySoldProductSummary)
                .icon("CURRENCY_EURO")
                .build()
        );

        formFactory.endContainer();
        formFactory.endContainer();

        final String promotionFieldRef = formFactory.addField(MultiSelectFormularField.builder()
                .valuePath(KokuCustomerAppointmentDto.Fields.promotions)
                .label("Aktionen")
                .placeholder("Weitere Aktionen...")
                .possibleValues(StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(this.promotionKTableProcessor.getPromotions().all(), Spliterator.DISTINCT),
                        false
                ).map(promotion -> {
                    return MultiSelectFormularFieldPossibleValue.builder()
                            .id(promotion.key + "")
                            .text(promotion.value.getName())
                            .disabled(Boolean.TRUE.equals(promotion.value.getDeleted()))
                            .build();
                }).toList())
                .idPathMapping(KokuCustomerAppointmentPromotionDto.Fields.promotionId)
                .appendOuter(KokuFieldSlotButton.builder()
                        .icon("PLUS")
                        .buttonType(EnumButtonType.BUTTON)
                        .title("Neue Aktion anlegen")
                        .build()
                )
                .uniqueValues(true)
                .build()
        );
        formFactory.addBusinessRule(KokuBusinessRuleDto.builder()
                .id("CreatePromotion")
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(promotionFieldRef)
                        .listener(KokuBusinessRuleFieldReferenceListenerDto.builder()
                                .event(KokuBusinessRuleFieldReferenceListenerEventEnum.CLICK_APPEND_OUTER)
                                .build())
                        .build())
                .execution(
                        KokuBusinessRuleOpenDialogContentDto.builder()
                                .content(KokuBusinessRuleHeaderContentDto.builder()
                                        .title("Neue Aktion")
                                        .content(
                                                KokuBusinessRuleFormularContentDto.builder()
                                                        .formularUrl("services/promotions/promotions/form")
                                                        .submitUrl("services/promotions/promotions")
                                                        .submitMethod(KokuBusinessRuleFormularActionSubmitMethodEnumDto.POST)
                                                        .maxWidthInPx(800)
                                                        .onSaveEvents(Arrays.asList(
                                                                KokuBusinessRuleFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                                        .eventName("promotion-created")
                                                                        .build()
                                                        ))
                                                        .build()
                                        )
                                        .build()
                                )
                                .closeEventListener(KokuBusinessRuleOpenContentCloseGlobalEventListenerDto.builder()
                                        .eventName("promotion-created")
                                        .build()
                                )
                                .build()
                )
                .build()
        );
        formFactory.addGlobalEventListener(FormViewEventPayloadFieldUpdateGlobalEventListenerDto.builder()
                .eventName("promotion-created")
                .fieldValueMapping(Map.of(
                        promotionFieldRef,
                        FormViewFieldReferenceMultiSelectValueMapping.builder()
                                .targetPathMapping(Map.of(
                                        KokuCustomerAppointmentPromotionDto.Fields.promotionId,
                                        FormViewEventPayloadSourcePathFieldUpdateValueSourceDto.builder()
                                                .sourcePath(KokuPromotionDto.Fields.id)
                                                .build()
                                ))
                                .build()
                ))
                .configMapping(Map.of(
                        promotionFieldRef,
                        FormViewFieldConfigMapping.builder()
                                .targetConfigPath(SelectFormularField.Fields.possibleValues)
                                .valueMapping(ConfigMappingAppendListDto.builder()
                                        .valueMapping(List.of(
                                                StringConversionConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuPromotionDto.Fields.id)
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.id)
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuPromotionDto.Fields.name)
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.text)
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuPromotionDto.Fields.deleted)
                                                        .targetPath(MultiSelectFormularFieldPossibleValue.Fields.disabled)
                                                        .build()
                                        ))
                                        .build())
                                .build()
                ))
                .build()
        );

        formFactory.addField(TextareaFormularField.builder()
                .label("Zusätzliche Informationen")
                .valuePath(KokuCustomerAppointmentDto.Fields.additionalInfo)
                .build()
        );

        formFactory.addField(SelectFormularField.builder()
                .valuePath(KokuCustomerAppointmentDto.Fields.userId)
                .label("Bedienung")
                .possibleValues(this.userKTableProcessor.getUsers().values().stream().map(user -> {
                    return SelectFormularFieldPossibleValue.builder()
                            .id(user.getId())
                            .text(
                                    String.join(
                                            " ",
                                            Objects.toString(user.getFirstname(), ""),
                                            Objects.toString(user.getLastname(), "")
                                    ).trim()
                            )
                            .disabled(Boolean.TRUE.equals(user.getDeleted()))
                            .build();
                }).toList())
                .defaultValue(SecurityContextHolder.getContext().getAuthentication().getName())
                .build()
        );

        formFactory.addButton(KokuFormButton.builder()
                .buttonType(EnumButtonType.SUBMIT)
                .text("Speichern")
                .title("Jetzt speichern")
                .styles(Arrays.asList(EnumButtonStyle.BLOCK))
                .dockable(true)
                .dockableSettings(ButtonDockableSettings.builder()
                        .icon("SAVE")
                        .styles(Arrays.asList(EnumButtonStyle.CIRCLE))
                        .build()
                )
                .postProcessingAction(FormButtonReloadAction.builder().build())
                .build()
        );

        formFactory.addBusinessRule(KokuBusinessRuleDto.builder()
                .id("ActivitySummary")
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(activityFieldRef)
                        .requestParam(KokuActivityPriceSummaryRequestDto.Fields.activities)
                        .listener(KokuBusinessRuleFieldReferenceListenerDto.builder()
                                .event(KokuBusinessRuleFieldReferenceListenerEventEnum.CHANGE)
                                .build())
                        .build())
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(promotionFieldRef)
                        .requestParam(KokuActivityPriceSummaryRequestDto.Fields.promotions)
                        .listener(KokuBusinessRuleFieldReferenceListenerDto.builder()
                                .event(KokuBusinessRuleFieldReferenceListenerEventEnum.CHANGE)
                                .build())
                        .build())
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(dateFieldRef)
                        .requestParam(KokuActivityPriceSummaryRequestDto.Fields.date)
                        .build())
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(timeFieldRef)
                        .requestParam(KokuActivityPriceSummaryRequestDto.Fields.time)
                        .build())
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(activityPriceSumStatFieldRef)
                        .resultUpdateMode(KokuBusinessRuleFieldReferenceUpdateModeEnum.ALWAYS)
                        .resultValuePath(KokuCustomerActivityPriceSummaryDto.Fields.priceSum)
                        .loadingAnimation(true)
                        .build())
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(activityDurationSumStatFieldRef)
                        .resultUpdateMode(KokuBusinessRuleFieldReferenceUpdateModeEnum.ALWAYS)
                        .resultValuePath(KokuCustomerActivityPriceSummaryDto.Fields.durationSum)
                        .loadingAnimation(true)
                        .build())
                .execution(
                        KokuBusinessRuleCallHttpEndpoint.builder()
                                .url("services/customers/customers/appointments/activitysummary")
                                .method(KokuBusinessRuleCallHttpEndpointMethodEnum.GET)
                                .build()
                )
                .build()
        );

        formFactory.addBusinessRule(KokuBusinessRuleDto.builder()
                .id("ProductSummary")
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(soldProductsFieldRef)
                        .requestParam(KokuActivitySoldProductSummaryRequestDto.Fields.soldProducts)
                        .listener(KokuBusinessRuleFieldReferenceListenerDto.builder()
                                .event(KokuBusinessRuleFieldReferenceListenerEventEnum.CHANGE)
                                .build())
                        .build())
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(promotionFieldRef)
                        .requestParam(KokuActivityPriceSummaryRequestDto.Fields.promotions)
                        .listener(KokuBusinessRuleFieldReferenceListenerDto.builder()
                                .event(KokuBusinessRuleFieldReferenceListenerEventEnum.CHANGE)
                                .build())
                        .build())
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(dateFieldRef)
                        .requestParam(KokuActivitySoldProductSummaryRequestDto.Fields.date)
                        .build())
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(timeFieldRef)
                        .requestParam(KokuActivitySoldProductSummaryRequestDto.Fields.time)
                        .build())
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(productPriceSumStatFieldRef)
                        .resultUpdateMode(KokuBusinessRuleFieldReferenceUpdateModeEnum.ALWAYS)
                        .resultValuePath(KokuActivitySoldProductPriceSummaryDto.Fields.priceSum)
                        .loadingAnimation(true)
                        .build())
                .execution(
                        KokuBusinessRuleCallHttpEndpoint.builder()
                                .url("services/customers/customers/appointments/productsummary")
                                .method(KokuBusinessRuleCallHttpEndpointMethodEnum.GET)
                                .build()
                )
                .build()
        );

        return formFactory.create();
    }

    @GetMapping("/customers/appointments/list")
    public ListViewDto getListView() {
        final ListViewFactory listViewFactory = new ListViewFactory(
                new DefaultListViewContentIdGenerator(),
                KokuCustomerAppointmentDto.Fields.id
        );

        final ListViewSourcePathReference customerIdSourcePathRef = listViewFactory.addSourcePath(
                KokuCustomerAppointmentDto.Fields.customerId
        );
        final ListViewSourcePathReference idSourcePathRef = listViewFactory.addSourcePath(
                KokuCustomerAppointmentDto.Fields.id
        );

        final ListViewSourcePathReference customerNamePathRef = listViewFactory.addSourcePath(
                KokuCustomerAppointmentDto.Fields.customerName
        );
        final ListViewFieldReference shortSummaryFieldRef = listViewFactory.addField(
                KokuCustomerAppointmentDto.Fields.shortSummaryText,
                ListViewInputFieldDto.builder()
                        .label("Zusammenfassung")
                        .build()
        );
        final ListViewSourcePathReference deletedSourcePathRef = listViewFactory.addSourcePath(KokuCustomerAppointmentDto.Fields.deleted);

        listViewFactory.addAction(ListViewOpenRoutedContentActionDto.builder()
                .route("new")
                .icon("PLUS")
                .build()
        );
        listViewFactory.addRoutedItem(ListViewRoutedDummyItemDto.builder()
                .route("new")
                .text("Neuer Kundentermin")
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadAddItemGlobalEventListenerDto.builder()
                .eventName("customer-appointment-created")
                .idPath(KokuCustomerAppointmentDto.Fields.id)
                .valueMapping(Map.of(
                        KokuCustomerAppointmentDto.Fields.shortSummaryText, shortSummaryFieldRef,
                        KokuCustomerAppointmentDto.Fields.deleted, deletedSourcePathRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route("new")
                        .inlineContent(ListViewHeaderContentDto.builder()
                                .title("Neuer Kundentermin")
                                .content(ListViewFormularContentDto.builder()
                                        .formularUrl("services/customers/customers/appointments/form")
                                        .submitUrl("services/customers/customers/appointments")
                                        .fieldOverrides(Arrays.asList(
                                                ListViewRouteBasedFormularFieldOverrideDto.builder()
                                                        .routeParam(":customerId")
                                                        .fieldId(KokuCustomerAppointmentDto.Fields.customerId)
                                                        .disable(true)
                                                        .build()
                                        ))
                                        .submitMethod(ListViewFormularActionSubmitMethodEnumDto.POST)
                                        .maxWidthInPx(800)
                                        .onSaveEvents(Arrays.asList(
                                                ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                        .eventName("customer-appointment-created")
                                                        .build(),
                                                ListViewOpenRoutedInlineFormularContentSaveEventDto.builder()
                                                        .route(":appointmentId")
                                                        .params(Arrays.asList(
                                                                ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto.builder()
                                                                        .param(":appointmentId")
                                                                        .valuePath(KokuCustomerAppointmentDto.Fields.id)
                                                                        .build()
                                                        ))
                                                        .build()
                                        ))
                                        .build()
                                )
                                .build()
                        )
                        .build()
        );

        listViewFactory.setItemClickAction(ListViewItemClickOpenRoutedContentActionDto.builder()
                .route(":appointmentId")
                .params(Arrays.asList(
                        ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                                .param(":appointmentId")
                                .valueReference(idSourcePathRef)
                                .build()
                ))
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadItemUpdateGlobalEventListenerDto.builder()
                .eventName("customer-appointment-updated")
                .idPath(KokuCustomerAppointmentDto.Fields.id)
                .valueMapping(Map.of(
                        KokuCustomerAppointmentDto.Fields.shortSummaryText, shortSummaryFieldRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route(":appointmentId")
                        .itemId(":appointmentId")
                        .inlineContent(
                                ListViewHeaderContentDto.builder()
                                        .sourceUrl("services/customers/customers/appointments/:appointmentId/summary")
                                        .titlePath(KokuCustomerAppointmentSummaryDto.Fields.appointmentSummary)
                                        .globalEventListeners(Arrays.asList(ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto.builder()
                                                .eventName("customer-appointment-updated")
                                                .idPath(KokuCustomerAppointmentDto.Fields.id)
                                                .titleValuePath(KokuCustomerAppointmentDto.Fields.longSummaryText)
                                                .build()
                                        ))
                                        .content(ListViewFormularContentDto.builder()
                                                .formularUrl("services/customers/customers/appointments/form")
                                                .sourceUrl("services/customers/customers/appointments/:appointmentId")
                                                .fieldOverrides(Arrays.asList(
                                                        ListViewRouteBasedFormularFieldOverrideDto.builder()
                                                                .routeParam(":customerId")
                                                                .fieldId(KokuCustomerAppointmentDto.Fields.customerId)
                                                                .disable(true)
                                                                .build()
                                                ))
                                                .submitMethod(ListViewFormularActionSubmitMethodEnumDto.PUT)
                                                .maxWidthInPx(800)
                                                .onSaveEvents(Arrays.asList(
                                                        ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                                .eventName("customer-appointment-updated")
                                                                .build()
                                                ))
                                                .build())
                                        .build()
                        )
                        .build()
        );
        listViewFactory.addGlobalItemStyling(ListViewConditionalItemValueStylingDto.builder()
                .compareValuePath(KokuCustomerAppointmentDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveStyling(ListViewItemStylingDto.builder()
                        .lineThrough(true)
                        .opacity((short) 50)
                        .build()
                )
                .build()
        );
        listViewFactory.addItemAction(ListViewConditionalItemValueActionDto.builder()
                .compareValuePath(KokuCustomerAppointmentDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("ARROW_LEFT_START_ON_RECTANGLE")
                        .url("services/customers/customers/appointments/:appointmentId/restore")
                        .params(Arrays.asList(
                                ListViewCallHttpListValueActionParamDto.builder()
                                        .param(":appointmentId")
                                        .valueReference(idSourcePathRef)
                                        .build()
                        ))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.PUT)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Termin wiederherstellen")
                                .content("Termin von :name am :date wiederherstellen?")
                                .params(Arrays.asList(
                                        ListViewUserConfirmationValueParamDto.builder()
                                                .param(":name")
                                                .valueReference(customerNamePathRef)
                                                .build(),
                                        ListViewUserConfirmationDateValueParamDto.builder()
                                                .param(":date")
                                                .valueReference(shortSummaryFieldRef)
                                                .build()
                                ))
                                .build()
                        )
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Termin von :name am :date wurde erfolgreich wiederhergestellt")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(customerNamePathRef)
                                                        .build(),
                                                ListViewNotificationEventDateValueParamDto.builder()
                                                        .param(":date")
                                                        .valueReference(shortSummaryFieldRef)
                                                        .build()
                                        ))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuCustomerAppointmentDto.Fields.id)
                                        .valueMapping(Map.of(
                                                KokuCustomerAppointmentDto.Fields.deleted, deletedSourcePathRef
                                        ))
                                        .build()
                        ))
                        .failEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Termin von :name am :date konnte nicht wiederhergestellt werden")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(customerNamePathRef)
                                                        .build(),
                                                ListViewNotificationEventDateValueParamDto.builder()
                                                        .param(":date")
                                                        .valueReference(shortSummaryFieldRef)
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build())
                .negativeAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("TRASH")
                        .url("services/customers/customers/appointments/:appointmentId")
                        .params(Arrays.asList(
                                ListViewCallHttpListValueActionParamDto.builder()
                                        .param(":appointmentId")
                                        .valueReference(idSourcePathRef)
                                        .build()
                        ))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.DELETE)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Termin löschen")
                                .content("Termin von :name am :date als gelöscht markieren?")
                                .params(Arrays.asList(
                                        ListViewUserConfirmationValueParamDto.builder()
                                                .param(":name")
                                                .valueReference(customerNamePathRef)
                                                .build(),
                                        ListViewUserConfirmationDateValueParamDto.builder()
                                                .param(":date")
                                                .valueReference(shortSummaryFieldRef)
                                                .build()
                                ))
                                .build()
                        )
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Termin von :name am :date wurde erfolgreich als gelöscht markiert")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(customerNamePathRef)
                                                        .build(),
                                                ListViewNotificationEventDateValueParamDto.builder()
                                                        .param(":date")
                                                        .valueReference(shortSummaryFieldRef)
                                                        .build()
                                        ))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuCustomerAppointmentDto.Fields.id)
                                        .valueMapping(Map.of(
                                                KokuCustomerAppointmentDto.Fields.deleted, deletedSourcePathRef
                                        ))
                                        .build()
                        ))
                        .failEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Termin von :name am :date konnte nicht als gelöscht markiert werden")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(customerNamePathRef)
                                                        .build(),
                                                ListViewNotificationEventDateValueParamDto.builder()
                                                        .param(":date")
                                                        .valueReference(shortSummaryFieldRef)
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build()
                )
                .build()
        );

        return listViewFactory.create();
    }

    @PostMapping(value = {
            "/customers/{customerId}/appointments/query",
            "/customers/appointments/query"
    })
    public ListPage findAll(
            @PathVariable(value = "customerId", required = false) Long requestedCustomerId,
            @RequestBody(required = false) final ListQuery predicate
    ) {
        final QCustomerAppointment qClazz = QCustomerAppointment.customerAppointment;
        final ListQueryFactory<CustomerAppointment> listQueryFactory = new ListQueryFactory<>(
                this.entityManager,
                qClazz,
                qClazz.id,
                predicate
        );

        if (requestedCustomerId != null) {
            listQueryFactory.addDefaultFilter(qClazz.customer.id.eq(requestedCustomerId));
        }
        listQueryFactory.setDefaultOrder(qClazz.start.desc());

        listQueryFactory.addFetchExpr(
                KokuCustomerAppointmentDto.Fields.id,
                qClazz.id
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerAppointmentDto.Fields.deleted,
                qClazz.deleted
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerAppointmentDto.Fields.customerId,
                qClazz.customer.id
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerAppointmentDto.Fields.shortSummaryText,
                StringExpressions.lpad(qClazz.start.dayOfMonth().stringValue(), 2, '0').append(".")
                        .append(StringExpressions.lpad(qClazz.start.month().stringValue(), 2, '0')).append(".")
                        .append(StringExpressions.lpad(qClazz.start.year().stringValue(), 4, '0'))
                        .append(" um ")
                        .append(StringExpressions.lpad(Expressions.stringTemplate(
                                "to_char({0}, 'HH24')",
                                qClazz.start
                        ), 2, '0')).append(":")
                        .append(StringExpressions.lpad(qClazz.start.minute().stringValue(), 2, '0'))
                        .append(" Uhr")
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerAppointmentDto.Fields.longSummaryText,
                StringExpressions.lpad(qClazz.start.dayOfMonth().stringValue(), 2, '0').prepend("Kundentermin am ").append(".")
                        .append(StringExpressions.lpad(qClazz.start.month().stringValue(), 2, '0')).append(".")
                        .append(StringExpressions.lpad(qClazz.start.year().stringValue(), 4, '0'))
                        .append(" um ")
                        .append(StringExpressions.lpad(Expressions.stringTemplate(
                                "to_char({0}, 'HH24')",
                                qClazz.start
                        ), 2, '0')).append(":")
                        .append(StringExpressions.lpad(qClazz.start.minute().stringValue(), 2, '0'))
                        .append(" Uhr")
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerAppointmentDto.Fields.description,
                qClazz.description
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerAppointmentDto.Fields.version,
                qClazz.version
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerAppointmentDto.Fields.additionalInfo,
                qClazz.additionalInfo
        );

        final StringExpression firstAndLastnameAndOnFirstnameBasisSignExpr = qClazz.customer.firstname
                .concat(" ")
                .concat(qClazz.customer.lastname)
                .concat(" ")
                .concat(new CaseBuilder().when(qClazz.customer.onFirstnameBasis.eq(Boolean.TRUE)).then("*").otherwise(""))
                .trim();
        listQueryFactory.addFetchExpr(
                KokuCustomerAppointmentDto.Fields.customerName,
                firstAndLastnameAndOnFirstnameBasisSignExpr
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerAppointmentDto.Fields.date,
                Expressions.dateTemplate(
                        LocalDate.class,
                        "cast({0} as date)",
                        qClazz.start
                )
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerAppointmentDto.Fields.time,
                Expressions.timeTemplate(
                        LocalTime.class,
                        "cast({0} as time)",
                        qClazz.start
                )
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerAppointmentDto.Fields.userId,
                qClazz.userId
        );

        return listQueryFactory.create();
    }

    @GetMapping(value = "/customers/appointments/{appointmentId}")
    public KokuCustomerAppointmentDto readAppointment(
            @PathVariable("appointmentId") Long appointmentId) {
        final CustomerAppointment customerAppointment = this.customerAppointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        return this.transformer.transformToDto(customerAppointment);
    }

    @GetMapping(value = "/customers/appointments/{appointmentId}/summary")
    public KokuCustomerAppointmentSummaryDto readAppointmentSummary(
            @PathVariable("appointmentId") Long appointmentId) {
        final CustomerAppointment customerAppointment = this.customerAppointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        return this.transformer.transformToSummaryDto(customerAppointment);
    }

    @PutMapping(value = "/customers/appointments/{appointmentId}")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public KokuCustomerAppointmentDto update(
            @PathVariable("appointmentId") Long appointmentId,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate,
            @Validated @RequestBody KokuCustomerAppointmentDto updatedDto
    ) throws ActivityIdNotFoundException, ProductIdNotFoundException, UserIdNotFoundException, ActivityStepIdNotFoundException, PromotionIdNotFoundException {
        final CustomerAppointment customerAppointment = this.entityManager.getReference(CustomerAppointment.class, appointmentId);
        if (!Boolean.TRUE.equals(forceUpdate) && !customerAppointment.getVersion().equals(updatedDto.getVersion())) {
            throw new KokuBusinessExceptionWithConfirmationMessage(
                    KokuBusinessExceptionWithConfirmationMessageDto.builder()
                            .headline("Konflikt")
                            .confirmationMessage("Der Kundentermin wurde zwischenzeitlich bearbeitet.\nWillst Du die Speicherung dennoch vornehmen?")
                            .headerButton(KokuBusinessExceptionCloseButtonDto.builder()
                                    .text("Abbrechen")
                                    .title("Abbruch")
                                    .icon("Close")
                                    .build()
                            )
                            .closeOnClickOutside(true)
                            .button(KokuBusinessExceptionSendToDifferentEndpointButtonDto.builder()
                                    .text("Trotzdem speichern")
                                    .title("Zwischenzeitliche Änderungen überschreiben")
                                    .endpointUrl(String.format("services/customers/customers/appointments/%s?forceUpdate=%s", appointmentId, Boolean.TRUE))
                                    .build()
                            )
                            .button(KokuBusinessExceptionCloseButtonDto.builder()
                                    .text("Abbrechen")
                                    .title("Abbruch")
                                    .build()
                            )
                            .build()
            );
        }
        this.transformer.transformToEntity(customerAppointment, updatedDto);
        this.entityManager.flush();
        sendCustomerAppointmentUpdate(customerAppointment);
        return this.transformer.transformToDto(customerAppointment);
    }

    @DeleteMapping(value = "/customers/appointments/{appointmentId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuCustomerAppointmentDto delete(@PathVariable("appointmentId") Long appointmentId) {
        final CustomerAppointment customerAppointment = this.entityManager.getReference(CustomerAppointment.class, appointmentId);
        if (customerAppointment.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment is not deletable");
        }
        customerAppointment.setDeleted(true);
        this.entityManager.flush();
        sendCustomerAppointmentUpdate(customerAppointment);
        return this.transformer.transformToDto(customerAppointment);
    }

    @PutMapping(value = "/customers/appointments/{appointmentId}/restore")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuCustomerAppointmentDto restore(@PathVariable("appointmentId") Long appointmentId) {
        final CustomerAppointment customerAppointment = this.entityManager.getReference(CustomerAppointment.class, appointmentId);
        if (!customerAppointment.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment is not restorable");
        }
        customerAppointment.setDeleted(false);
        this.entityManager.flush();
        sendCustomerAppointmentUpdate(customerAppointment);
        return this.transformer.transformToDto(customerAppointment);
    }

    @PostMapping("/customers/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public KokuCustomerAppointmentDto create(@Validated @RequestBody KokuCustomerAppointmentDto newDto) throws ActivityIdNotFoundException, ProductIdNotFoundException, UserIdNotFoundException, ActivityStepIdNotFoundException, PromotionIdNotFoundException {
        final CustomerAppointment newCustomerAppointment = this.transformer.transformToEntity(new CustomerAppointment(), newDto);
        final CustomerAppointment savedCustomerAppointment = this.customerAppointmentRepository.saveAndFlush(newCustomerAppointment);
        sendCustomerAppointmentUpdate(savedCustomerAppointment);
        return this.transformer.transformToDto(savedCustomerAppointment);
    }

    public void sendCustomerAppointmentUpdate(final CustomerAppointment customerAppointment) {
        try {
            this.customerAppointmentKafkaService.sendCustomerAppointment(customerAppointment);
        } catch (final ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Unable to export to kafka, due to: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to export to kafka");
        }
    }


    @GetMapping(value = "/appointments/statistics")
    public BarChartDto getAppointmentStatistics(
            @RequestParam(value = "start", required = false) final YearMonth startFilterRaw,
            @RequestParam(value = "end", required = false) final YearMonth endFilterRaw
    ) {
        final LocalDateTime startFilter = startFilterRaw != null ? startFilterRaw.atDay(1).atStartOfDay() : YearMonth.now().minusMonths(6).atDay(1).atStartOfDay();
        final LocalDateTime endFilter = endFilterRaw != null ? endFilterRaw.atEndOfMonth().atTime(LocalTime.MAX) : YearMonth.now().atEndOfMonth().atTime(LocalTime.MAX);

        final QCustomerAppointment qClazz = QCustomerAppointment.customerAppointment;
        final NumberExpression<BigDecimal> activitiesRevenueSnapshotSum = qClazz.activitiesRevenueSnapshot.sum();
        final NumberExpression<BigDecimal> soldProductsRevenueSnapshotSum = qClazz.soldProductsRevenueSnapshot.sum();
        final Map<YearMonth, Tuple> transform = new JPAQuery<>(this.entityManager, JPQLTemplates.DEFAULT)
                .select(qClazz.start.yearMonth().stringValue(), activitiesRevenueSnapshotSum, soldProductsRevenueSnapshotSum)
                .from(qClazz)
                .where(
                        qClazz.start.goe(startFilter)
                                .and(qClazz.start.year().loe(endFilter.getYear()).and(qClazz.start.month().loe(endFilter.getMonthValue())))
                                .and(qClazz.deleted.ne(Boolean.TRUE))
                )
                .groupBy(qClazz.start.yearMonth())
                .transform(GroupBy.groupBy(qClazz.start.yearMonth().stringValue()).as(
                        Projections.tuple(activitiesRevenueSnapshotSum, soldProductsRevenueSnapshotSum)
                )).entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> YearMonth.parse(entry.getKey(), YEAR_MONTH_DATETIME_FORMATTER),
                        Map.Entry::getValue
                ));

        YearMonth currentMonth = YearMonth.from(startFilter);
        final YearMonth lastMonth = YearMonth.from(endFilter);
        final List<YearMonth> allMonthsBetweenQuery = new ArrayList<>();
        while (currentMonth.isBefore(lastMonth)) {
            allMonthsBetweenQuery.add(currentMonth);
            currentMonth = currentMonth.plusMonths(1);
        }
        allMonthsBetweenQuery.add(lastMonth);

        return BarChartDto.builder()
                .title("Umsätze")
                .filter(InputChartFilterDto.builder()
                        .value(YearMonth.from(startFilter))
                        .type(EnumInputChartFilterType.MONTH)
                        .label("Von")
                        .queryParamName("start")
                        .build()
                )
                .filter(InputChartFilterDto.builder()
                        .value(YearMonth.from(endFilter))
                        .type(EnumInputChartFilterType.MONTH)
                        .label("Bis")
                        .queryParamName("end")
                        .build()
                )
                .axes(AxesDto.builder()
                        .x(CategoricalXAxisDto.builder()
                                .categories(allMonthsBetweenQuery.stream().map(month -> month.format(DateTimeFormatter.ofPattern("MMM yyyy"))).toList())
                                .build()
                        )
                        .build()
                )
                .series(List.of(
                        NumericSeriesDto.builder()
                                .name("Tätigkeitsumsatz")
                                .data(allMonthsBetweenQuery.stream().map(transform::get).map(tuple -> tuple != null ? tuple.get(activitiesRevenueSnapshotSum) : BigDecimal.ZERO).toList())
                                .build(),
                        NumericSeriesDto.builder()
                                .name("Produktumsatz")
                                .data(allMonthsBetweenQuery.stream().map(transform::get).map(tuple -> tuple != null ? tuple.get(soldProductsRevenueSnapshotSum) : BigDecimal.ZERO).toList())
                                .build()
                ))
                .stacked(true)
                .showTotals(true)
                .build();
    }

    @GetMapping(value = "/customers/{id}/statistics/yearlyvisits")
    public BarChartDto getYearlyVisitsByCustomerId(@PathVariable("id") Long customerId) {
        final QCustomerAppointment qClazz = QCustomerAppointment.customerAppointment;

        final NumberExpression<Long> countExpr = qClazz.count();
        final NumberExpression<Integer> yearExpr = qClazz.start.year();
        final Map<Integer, Long> visitsPerYear = new JPAQuery<>(this.entityManager, JPQLTemplates.DEFAULT)
                .select(countExpr, yearExpr)
                .from(qClazz)
                .where(qClazz.customer.id.eq(customerId)
                        .and(qClazz.deleted.ne(Boolean.TRUE))
                )
                .groupBy(yearExpr)
                .transform(GroupBy.groupBy(yearExpr).as(countExpr));

        return BarChartDto.builder()
                .title("Besuche")
                .axes(AxesDto.builder()
                        .x(CategoricalXAxisDto.builder()
                                .categories(visitsPerYear.keySet().stream().map(year -> String.format("%s", year)).toList())
                                .build()
                        )
                        .build()
                )
                .series(List.of(NumericSeriesDto.builder()
                        .name("Besuche")
                        .data(visitsPerYear.values().stream().map(BigDecimal::new).toList())
                        .build()
                ))
                .showTotals(true)
                .build();
    }

    @GetMapping(value = "/products/statistics")
    public BarChartDto getProductRevenue(
            @RequestParam(value = "start", required = false) final YearMonth startFilterRaw,
            @RequestParam(value = "end", required = false) final YearMonth endFilterRaw
    ) {
        final LocalDateTime startFilter = startFilterRaw != null ? startFilterRaw.atDay(1).atStartOfDay() : YearMonth.now().minusMonths(6).atDay(1).atStartOfDay();
        final LocalDateTime endFilter = endFilterRaw != null ? endFilterRaw.atEndOfMonth().atTime(LocalTime.MAX) : YearMonth.now().atEndOfMonth().atTime(LocalTime.MAX);

        final QCustomerAppointment qClazz = QCustomerAppointment.customerAppointment;

        final List<CustomerAppointment> allAppointments = new JPAQuery<>(this.entityManager, JPQLTemplates.DEFAULT)
                .select(qClazz)
                .from(qClazz)
                .where(
                        qClazz.start.goe(startFilter)
                                .and(qClazz.start.year().loe(endFilter.getYear()).and(qClazz.start.month().loe(endFilter.getMonthValue())))
                                .and(qClazz.deleted.ne(Boolean.TRUE))
                )
                .fetch();

        final Map<Long, Long> salesPerProduct = new HashMap<>();
        final Map<Long, BigDecimal> revenuePerProduct = new HashMap<>();

        for (final CustomerAppointment customerAppointment : allAppointments) {
            for (final CustomerAppointmentSoldProduct soldProduct : customerAppointment.getSoldProducts()) {
                salesPerProduct.put(soldProduct.getProductId(), salesPerProduct.getOrDefault(soldProduct.getProductId(), 0L) + 1);
                revenuePerProduct.put(soldProduct.getProductId(),
                        revenuePerProduct.getOrDefault(soldProduct.getProductId(), BigDecimal.ZERO)
                                .add(soldProduct.getFinalPriceSnapshot())
                );
            }
        }

        return BarChartDto.builder()
                .title("Umsätze")
                .filter(InputChartFilterDto.builder()
                        .value(YearMonth.from(startFilter))
                        .type(EnumInputChartFilterType.MONTH)
                        .label("Von")
                        .queryParamName("start")
                        .build()
                )
                .filter(InputChartFilterDto.builder()
                        .value(YearMonth.from(endFilter))
                        .type(EnumInputChartFilterType.MONTH)
                        .label("Bis")
                        .queryParamName("end")
                        .build()
                )
                .axes(AxesDto.builder()
                        .x(CategoricalXAxisDto.builder()
                                .categories(StreamSupport.stream(
                                                Spliterators.spliteratorUnknownSize(this.productKTableProcessor.getProducts().all(), Spliterator.DISTINCT),
                                                false
                                        )
                                        .map(product -> String.format("%s / %s", this.productManufacturerKTableProcessor.getProductManufacturers().get(product.value.getManufacturerId()).getName(), product.value.getName()).trim()).toList())
                                .build()
                        )
                        .y(YAxisDto.builder()
                                .text("Umsatz (€)")
                                .build()
                        )
                        .y(YAxisDto.builder()
                                .opposite(true)
                                .text("Verkäufe")
                                .build()
                        )
                        .build()
                )
                .series(List.of(
                        NumericSeriesDto.builder()
                                .name("Umsatz (€)")
                                .data(StreamSupport.stream(
                                        Spliterators.spliteratorUnknownSize(this.productKTableProcessor.getProducts().all(), Spliterator.DISTINCT),
                                        false
                                ).map(productKafkaDto -> {
                                    return revenuePerProduct.getOrDefault(productKafkaDto.key, BigDecimal.ZERO);
                                }).toList())
                                .build(),
                        NumericSeriesDto.builder()
                                .name("Verkäufe")
                                .data(StreamSupport.stream(
                                                Spliterators.spliteratorUnknownSize(this.productKTableProcessor.getProducts().all(), Spliterator.DISTINCT),
                                                false
                                        )
                                        .map(productKafkaDto -> {
                                            return BigDecimal.valueOf(salesPerProduct.getOrDefault(productKafkaDto.key, 0L));
                                        }).toList())
                                .build()
                ))
                .stacked(true)
                .build();
    }

    @GetMapping(value = "/activities/statistics")
    public BarChartDto getActivityRevenue(
            @RequestParam(value = "start", required = false) final YearMonth startFilterRaw,
            @RequestParam(value = "end", required = false) final YearMonth endFilterRaw
    ) {
        final LocalDateTime startFilter = startFilterRaw != null ? startFilterRaw.atDay(1).atStartOfDay() : YearMonth.now().minusMonths(6).atDay(1).atStartOfDay();
        final LocalDateTime endFilter = endFilterRaw != null ? endFilterRaw.atEndOfMonth().atTime(LocalTime.MAX) : YearMonth.now().atEndOfMonth().atTime(LocalTime.MAX);

        final QCustomerAppointment qClazz = QCustomerAppointment.customerAppointment;

        final List<CustomerAppointment> allAppointments = new JPAQuery<>(this.entityManager, JPQLTemplates.DEFAULT)
                .select(qClazz)
                .from(qClazz)
                .where(
                        qClazz.start.goe(startFilter)
                                .and(qClazz.start.year().loe(endFilter.getYear()).and(qClazz.start.month().loe(endFilter.getMonthValue())))
                                .and(qClazz.deleted.ne(Boolean.TRUE))
                )
                .fetch();

        final Map<Long, Long> applicationsPerActivity = new HashMap<>();
        final Map<Long, BigDecimal> revenuePerActivity = new HashMap<>();

        for (final CustomerAppointment customerAppointment : allAppointments) {
            for (final CustomerAppointmentActivity activity : customerAppointment.getActivities()) {
                applicationsPerActivity.put(activity.getActivityId(), applicationsPerActivity.getOrDefault(activity.getActivityId(), 0L) + 1);
                revenuePerActivity.put(activity.getActivityId(),
                        revenuePerActivity.getOrDefault(activity.getActivityId(), BigDecimal.ZERO)
                                .add(activity.getFinalPriceSnapshot())
                );
            }
        }

        return BarChartDto.builder()
                .title("Umsätze")
                .filter(InputChartFilterDto.builder()
                        .value(YearMonth.from(startFilter))
                        .type(EnumInputChartFilterType.MONTH)
                        .label("Von")
                        .queryParamName("start")
                        .build()
                )
                .filter(InputChartFilterDto.builder()
                        .value(YearMonth.from(endFilter))
                        .type(EnumInputChartFilterType.MONTH)
                        .label("Bis")
                        .queryParamName("end")
                        .build()
                )
                .axes(AxesDto.builder()
                        .x(CategoricalXAxisDto.builder()
                                .categories(
                                        StreamSupport.stream(
                                                        Spliterators.spliteratorUnknownSize(this.activityKTableProcessor.getActivities().all(), Spliterator.DISTINCT),
                                                        false
                                                ).map(activity -> String.format("%s", activity.value.getName()))
                                                .toList()
                                )
                                .build()
                        )
                        .y(YAxisDto.builder()
                                .text("Umsatz (€)")
                                .build()
                        )
                        .y(YAxisDto.builder()
                                .opposite(true)
                                .text("Anwendungen")
                                .build()
                        )
                        .build()
                )
                .series(List.of(
                        NumericSeriesDto.builder()
                                .name("Umsatz (€)")
                                .data(StreamSupport.stream(
                                        Spliterators.spliteratorUnknownSize(this.activityKTableProcessor.getActivities().all(), Spliterator.DISTINCT),
                                        false
                                ).map(activity -> revenuePerActivity.getOrDefault(activity.key, BigDecimal.ZERO)).toList())
                                .build(),
                        NumericSeriesDto.builder()
                                .name("Anwendungen")
                                .data(StreamSupport.stream(
                                        Spliterators.spliteratorUnknownSize(this.activityKTableProcessor.getActivities().all(), Spliterator.DISTINCT),
                                        false
                                ).map(activity -> BigDecimal.valueOf(applicationsPerActivity.getOrDefault(activity.key, 0L))).toList())
                                .build()
                ))
                .stacked(true)
                .build();
    }

    @GetMapping(value = "/customers/statistics")
    public BarChartDto getCustomerStatistics(
            @RequestParam(value = "start", required = false) final YearMonth startFilterRaw,
            @RequestParam(value = "end", required = false) final YearMonth endFilterRaw
    ) {
        final LocalDateTime startFilter = startFilterRaw != null ? startFilterRaw.atDay(1).atStartOfDay() : YearMonth.now().minusMonths(6).atDay(1).atStartOfDay();
        final LocalDateTime endFilter = endFilterRaw != null ? endFilterRaw.atEndOfMonth().atTime(LocalTime.MAX) : YearMonth.now().atEndOfMonth().atTime(LocalTime.MAX);

        final QCustomerAppointment qClazz = QCustomerAppointment.customerAppointment;
        final NumberExpression<BigDecimal> soldProductsRevenueSnapshotSum = qClazz.soldProductsRevenueSnapshot.sum();
        final NumberExpression<BigDecimal> activitiesRevenueSnapshotSum = qClazz.activitiesRevenueSnapshot.sum();
        final Map<Customer, Tuple> statisticsPerCustomer = new JPAQuery<>(this.entityManager, JPQLTemplates.DEFAULT)
                .select(qClazz.customer, soldProductsRevenueSnapshotSum, activitiesRevenueSnapshotSum)
                .from(qClazz)
                .where(
                        qClazz.start.goe(startFilter)
                                .and(qClazz.start.year().loe(endFilter.getYear()).and(qClazz.start.month().loe(endFilter.getMonthValue())))
                                .and(qClazz.deleted.ne(Boolean.TRUE))
                )
                .groupBy(qClazz.customer)
                .transform(GroupBy.groupBy(qClazz.customer).as(Projections.tuple(
                        soldProductsRevenueSnapshotSum,
                        activitiesRevenueSnapshotSum
                )));

        final Map<Customer, Long> visitsPerCustomer = new JPAQuery<>(this.entityManager, JPQLTemplates.DEFAULT)
                .select(qClazz.customer, qClazz.count())
                .from(qClazz)
                .where(
                        qClazz.start.goe(startFilter)
                                .and(qClazz.start.year().loe(endFilter.getYear()).and(qClazz.start.month().loe(endFilter.getMonthValue())))
                                .and(qClazz.deleted.ne(Boolean.TRUE))
                )
                .groupBy(qClazz.customer)
                .transform(GroupBy.groupBy(qClazz.customer).as(qClazz.count()));

        return BarChartDto.builder()
                .title("Umsätze")
                .filter(InputChartFilterDto.builder()
                        .value(YearMonth.from(startFilter))
                        .type(EnumInputChartFilterType.MONTH)
                        .label("Von")
                        .queryParamName("start")
                        .build()
                )
                .filter(InputChartFilterDto.builder()
                        .value(YearMonth.from(endFilter))
                        .type(EnumInputChartFilterType.MONTH)
                        .label("Bis")
                        .queryParamName("end")
                        .build()
                )
                .axes(AxesDto.builder()
                        .x(CategoricalXAxisDto.builder()
                                .categories(statisticsPerCustomer.keySet().stream().map(customer -> String.format("%s %s", customer.getFirstname(), customer.getLastname()).trim()).toList())
                                .build()
                        )
                        .y(YAxisDto.builder()
                                .text("Umsatz (€)")
                                .seriesName(List.of(
                                        "Tätigkeiten (€)",
                                        "Produkte (€)"
                                ))
                                .build()
                        )
                        .y(YAxisDto.builder()
                                .opposite(true)
                                .text("Besuche")
                                .seriesName(List.of(
                                        "Besuche"
                                ))
                                .build()
                        )
                        .build()
                )
                .series(List.of(
                        NumericSeriesDto.builder()
                                .name("Tätigkeiten (€)")
                                .group("revenue")
                                .data(statisticsPerCustomer.keySet().stream().map(customer -> {
                                    final Tuple statisticsCurrentCustomer = statisticsPerCustomer.get(customer);
                                    return statisticsCurrentCustomer != null ? statisticsCurrentCustomer.get(activitiesRevenueSnapshotSum) : BigDecimal.ZERO;
                                }).toList())
                                .build(),
                        NumericSeriesDto.builder()
                                .name("Produkte (€)")
                                .group("revenue")
                                .data(statisticsPerCustomer.keySet().stream().map(customer -> {
                                    final Tuple statisticsCurrentCustomer = statisticsPerCustomer.get(customer);
                                    return statisticsCurrentCustomer != null ? statisticsCurrentCustomer.get(soldProductsRevenueSnapshotSum) : BigDecimal.ZERO;
                                }).toList())
                                .build(),
                        NumericSeriesDto.builder()
                                .name("Besuche")
                                .data(visitsPerCustomer.keySet().stream().map(customer -> {
                                    final Long count = visitsPerCustomer.get(customer);
                                    return count != null ? BigDecimal.valueOf(count) : BigDecimal.ZERO;
                                }).toList())
                                .build()
                ))
                .stacked(true)
                .build();
    }

    @GetMapping(value = "/customers/{id}/statistics/yearlyrevenue")
    public BarChartDto getYearlyRevenueByCustomerId(@PathVariable("id") Long customerId) {
        final QCustomerAppointment qClazz = QCustomerAppointment.customerAppointment;
        final NumberExpression<BigDecimal> activityRevenueSnapshotSum = qClazz.activitiesRevenueSnapshot.sum();
        final NumberExpression<BigDecimal> soldProductsRevenueSnapshotSum = qClazz.soldProductsRevenueSnapshot.sum();
        final Map<Integer, Tuple> revenuesPerYear = new JPAQuery<>(this.entityManager, JPQLTemplates.DEFAULT)
                .select(qClazz.start.year(), activityRevenueSnapshotSum, soldProductsRevenueSnapshotSum)
                .from(qClazz)
                .where(qClazz.customer.id.eq(customerId)
                        .and(qClazz.deleted.ne(Boolean.TRUE)))
                .groupBy(qClazz.start.year())
                .transform(GroupBy.groupBy(qClazz.start.year()).as(Projections.tuple(activityRevenueSnapshotSum, soldProductsRevenueSnapshotSum)));

        return BarChartDto.builder()
                .title("Umsätze")
                .axes(AxesDto.builder()
                        .x(CategoricalXAxisDto.builder()
                                .categories(revenuesPerYear.keySet().stream().map(year -> String.format("%s", year)).toList())
                                .build()
                        )
                        .build()
                )
                .series(List.of(
                        NumericSeriesDto.builder()
                                .name("Tätigkeitsumsatz")
                                .data(revenuesPerYear.values().stream().map(tuple -> tuple.get(activityRevenueSnapshotSum)).toList())
                                .build(),
                        NumericSeriesDto.builder()
                                .name("Produktumsatz")
                                .data(revenuesPerYear.values().stream().map(tuple -> tuple.get(soldProductsRevenueSnapshotSum)).toList())
                                .build()
                ))
                .stacked(true)
                .showTotals(true)
                .build();
    }


    @GetMapping("/customers/dashboard/appointments")
    public DashboardTextPanelDto getAppointmentDashboardContent() {
        final LocalDateTime now = LocalDateTime.now();
        final YearMonth currentMonth = YearMonth.from(now);

        final QCustomerAppointment qClazz = QCustomerAppointment.customerAppointment;
        final Long countAllAppointmentsThisMonth = new JPAQuery<>(this.entityManager)
                .select(qClazz.count())
                .from(qClazz)
                .where(
                        qClazz.start.goe(currentMonth.atDay(1).atTime(LocalTime.MIN))
                                .and(qClazz.start.loe(currentMonth.atEndOfMonth().atTime(LocalTime.MAX)))
                                .and(qClazz.deleted.ne(Boolean.TRUE))
                )
                .fetchOne();
        final Long countPassedAppointmentsThisMonth = new JPAQuery<>(this.entityManager)
                .select(qClazz.count())
                .from(qClazz)
                .where(
                        qClazz.start.goe(currentMonth.atDay(1).atTime(LocalTime.MIN))
                                .and(qClazz.start.loe(now))
                                .and(qClazz.deleted.ne(Boolean.TRUE))
                )
                .fetchOne();
        return DashboardTextPanelDto.builder()
                .color(KokuColorEnum.PURPLE)
                .headline(String.valueOf(countAllAppointmentsThisMonth))
                .subHeadline("Termine " + currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentMonth.getYear() + "  \uD83D\uDCC5")
                .progress((short) (countAllAppointmentsThisMonth != 0L ? Math.round((float) countPassedAppointmentsThisMonth * 100 / countAllAppointmentsThisMonth) : 0))
                .progressDetails(List.of(
                        DashboardTextPanelProgressDetailsDto.builder()
                                .headline(String.valueOf(countPassedAppointmentsThisMonth))
                                .subHeadline("Erledigt ✅")
                                .headlineColor(KokuColorEnum.GREEN)
                                .build(),
                        DashboardTextPanelProgressDetailsDto.builder()
                                .headline(new JPAQuery<>(this.entityManager)
                                        .select(qClazz.count().stringValue())
                                        .from(qClazz)
                                        .where(
                                                qClazz.start.goe(now)
                                                        .and(qClazz.start.loe(currentMonth.atEndOfMonth().atTime(LocalTime.MAX)))
                                                        .and(qClazz.deleted.ne(Boolean.TRUE))
                                        )
                                        .fetchOne()
                                )
                                .subHeadline("Offen ⏳")
                                .headlineColor(KokuColorEnum.YELLOW)
                                .build()
                ))
                .build();
    }

    @GetMapping("/customers/dashboard/revenueschart")
    public LineChartDto getRevenuesChartDashboardContent() {
        final LocalDateTime now = LocalDateTime.now();
        final Year currentYear = Year.from(now);
        final Year lastYear = currentYear.minusYears(1);

        final QCustomerAppointment qClazz = QCustomerAppointment.customerAppointment;
        final NumberExpression<BigDecimal> activitiesRevenueSnapshotSum = qClazz.activitiesRevenueSnapshot.sum();
        final NumberExpression<BigDecimal> soldProductsRevenueSnapshotSum = qClazz.soldProductsRevenueSnapshot.sum();
        final NumberExpression<BigDecimal> sum = activitiesRevenueSnapshotSum.coalesce(BigDecimal.ZERO).add(soldProductsRevenueSnapshotSum.coalesce(BigDecimal.ZERO));
        final Map<YearMonth, BigDecimal> sums = new JPAQuery<>(this.entityManager, JPQLTemplates.DEFAULT)
                .select(qClazz.start.yearMonth().stringValue(), sum)
                .from(qClazz)
                .where(
                        qClazz.start.goe(lastYear.atMonth(1).atDay(1).atTime(LocalTime.MIN))
                                .and(qClazz.start.loe(currentYear.atMonth(12).atEndOfMonth().atTime(LocalTime.MAX)))
                                .and(qClazz.deleted.ne(Boolean.TRUE))
                )
                .groupBy(qClazz.start.yearMonth())
                .transform(GroupBy.groupBy(qClazz.start.yearMonth().stringValue()).as(
                        sum
                )).entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> YearMonth.parse(entry.getKey(), YEAR_MONTH_DATETIME_FORMATTER),
                        Map.Entry::getValue
                ));

        return LineChartDto.builder()
                .title("Umsatzübersicht \uD83D\uDCCA")
                .series(List.of(
                        NumericSeriesDto.builder()
                                .name("Umsatz " + currentYear.getValue())
                                .data(Arrays.stream(Month.values()).map(month -> {
                                    BigDecimal result = sums.get(currentYear.atMonth(month));
                                    return result != null ? result : BigDecimal.ZERO;
                                }).toList())
                                .build(),
                        NumericSeriesDto.builder()
                                .name("Umsatz " + lastYear.getValue())
                                .data(Arrays.stream(Month.values()).map(month -> {
                                    BigDecimal result = sums.get(lastYear.atMonth(month));
                                    return result != null ? result : BigDecimal.ZERO;
                                }).toList())
                                .build()
                ))
                .annotations(AnnotationsDto.builder()
                        .xasis(List.of(
                                AnnotationsAxesDto.builder()
                                        .x(Month.from(now).getDisplayName(TextStyle.SHORT, Locale.GERMAN))
                                        .borderColor(ColorsEnumDto.YELLOW)
                                        .label(AnnotationsAxesLabelDto.builder()
                                                .borderColor(ColorsEnumDto.YELLOW)
                                                .backgroundColor(ColorsEnumDto.YELLOW)
                                                .text("Aktueller Monat")
                                                .build()
                                        )
                                        .build()
                        ))
                        .build()
                )
                .axes(AxesDto.builder()
                        .x(CategoricalXAxisDto.builder()
                                .categories(Arrays.stream(Month.values()).map(month -> month.getDisplayName(TextStyle.SHORT, Locale.GERMAN)).toList())
                                .build()
                        )
                        .build()
                )
                .build();
    }

    @GetMapping("/customers/dashboard/revenues/current")
    public DashboardTextPanelDto getRevenuesCurrentDashboardContent() {
        final LocalDateTime now = LocalDateTime.now();
        final YearMonth currentMonth = YearMonth.from(now);

        final BigDecimal overallRevenueThisMonth = getOverallRevenue(
                currentMonth.atDay(1).atTime(LocalTime.MIN),
                currentMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );
        final BigDecimal achievedRevenueThisMonth = getOverallRevenue(
                currentMonth.atDay(1).atTime(LocalTime.MIN),
                now
        );
        YearMonth lastMonth = currentMonth.minusMonths(1);
        final BigDecimal overallRevenueLastMonth = getOverallRevenue(
                lastMonth.atDay(1).atTime(LocalTime.MIN),
                lastMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );
        YearMonth sameMonthLastYear = currentMonth.minusYears(1);
        final BigDecimal overallRevenueSameMonthLastYear = getOverallRevenue(
                sameMonthLastYear.atDay(1).atTime(LocalTime.MIN),
                sameMonthLastYear.atEndOfMonth().atTime(LocalTime.MAX)
        );

        return DashboardTextPanelDto.builder()
                .color(KokuColorEnum.BLUE)
                .topHeadline("Monatsumsatz")
                .headline(NumberFormat.getCurrencyInstance(Locale.GERMANY).format(overallRevenueThisMonth))
                .subHeadline("Umsatz " + currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentMonth.getYear() + " \uD83D\uDCB0")
                .progress(BigDecimal.ZERO.compareTo(overallRevenueThisMonth) != 0 ? achievedRevenueThisMonth.multiply(BigDecimal.valueOf(100)).divide(overallRevenueThisMonth, 0, RoundingMode.HALF_UP).shortValue() : 0)
                .progressDetails(List.of(
                        DashboardTextPanelProgressDetailsDto.builder()
                                .headline(calculatedFormattedDifference(overallRevenueThisMonth, overallRevenueLastMonth))
                                .subHeadline("vs. " + lastMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.GERMAN) + " " + currentMonth.getYear())
                                .headlineColor(KokuColorEnum.GREEN)
                                .build(),
                        DashboardTextPanelProgressDetailsDto.builder()
                                .headline(calculatedFormattedDifference(overallRevenueThisMonth, overallRevenueSameMonthLastYear))
                                .subHeadline("vs. " + sameMonthLastYear.getMonth().getDisplayName(TextStyle.SHORT, Locale.GERMAN) + " " + currentMonth.getYear())
                                .headlineColor(KokuColorEnum.YELLOW)
                                .build()
                ))
                .build();
    }

    @GetMapping("/customers/dashboard/revenues/preview")
    public DashboardTextPanelDto getRevenuesPreviewDashboardContent() {
        final LocalDateTime now = LocalDateTime.now();
        final YearMonth currentMonth = YearMonth.from(now);

        final BigDecimal overallRevenuePlanned = getOverallRevenue(
                currentMonth.plusMonths(1).atDay(1).atTime(LocalTime.MIN),
                currentMonth.plusMonths(1).plusYears(1).atEndOfMonth().atTime(LocalTime.MAX)
        );
        final BigDecimal overallRevenueNextMonth = getOverallRevenue(
                currentMonth.plusMonths(1).atDay(1).atTime(LocalTime.MIN),
                currentMonth.plusMonths(1).atEndOfMonth().atTime(LocalTime.MAX)
        );
        final BigDecimal overallRevenueNextNextMonth = getOverallRevenue(
                currentMonth.plusMonths(2).atDay(1).atTime(LocalTime.MIN),
                currentMonth.plusMonths(2).atEndOfMonth().atTime(LocalTime.MAX)
        );

        return DashboardTextPanelDto.builder()
                .topHeadline("Umsatzerwartungen")
                .headline(NumberFormat.getCurrencyInstance(Locale.GERMANY).format(overallRevenuePlanned))
                .color(KokuColorEnum.EMERALD)
                .explanations(List.of(
                        DashboardTextPanelExplanationItemDto.builder()
                                .left("Nächster Monat")
                                .right(NumberFormat.getCurrencyInstance(Locale.GERMANY).format(overallRevenueNextMonth))
                                .build(),
                        DashboardTextPanelExplanationItemDto.builder()
                                .left("Übernächster Monat")
                                .right(NumberFormat.getCurrencyInstance(Locale.GERMANY).format(overallRevenueNextNextMonth))
                                .build()
                ))
                .build();
    }

    @GetMapping("/customers/dashboard/topproduct")
    public DashboardTextPanelDto getTopProductDashboardContent() {
        final LocalDateTime now = LocalDateTime.now();
        final YearMonth currentMonth = YearMonth.from(now);

        final Map<Long, Integer> productUsages = getProductUsages(
                currentMonth.atDay(1).atTime(LocalTime.MIN),
                currentMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );

        final Map.Entry<Long, Integer> maxEntry = productUsages.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null); // null, falls Map leer

        String name = "?";
        if (maxEntry != null) {
            name = this.productKTableProcessor.getProducts().get(maxEntry.getKey()).getName();
        }

        return DashboardTextPanelDto.builder()
                .color(KokuColorEnum.PINK)
                .headline(name)
                .subHeadline("Top Produkt im " + currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentMonth.getYear() + " \uD83D\uDC84")
                .build();
    }

    @GetMapping("/customers/dashboard/topactivity")
    public DashboardTextPanelDto getTopActivityDashboardContent() {
        final LocalDateTime now = LocalDateTime.now();
        final YearMonth currentMonth = YearMonth.from(now);

        final Map<Long, Integer> activityUsages = getActivityUsages(
                currentMonth.atDay(1).atTime(LocalTime.MIN),
                currentMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );

        final Map.Entry<Long, Integer> maxEntry = activityUsages.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null); // null, falls Map leer

        String name = "?";
        if (maxEntry != null) {
            name = this.activityKTableProcessor.getActivities().get(maxEntry.getKey()).getName();
        }

        return DashboardTextPanelDto.builder()
                .color(KokuColorEnum.TEAL)
                .headline(name)
                .subHeadline("Top Dienstleistung im " + currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentMonth.getYear() + " ✂\uFE0F")
                .build();
    }

    @GetMapping("/customers/dashboard/newcustomers")
    public DashboardTextPanelDto getNewCustomerDashboardContent() {
        final LocalDateTime now = LocalDateTime.now();
        final YearMonth currentMonth = YearMonth.from(now);

        final QCustomer qClazz = customer;

        return DashboardTextPanelDto.builder()
                .color(KokuColorEnum.CYAN)
                .headline(String.valueOf(new JPAQuery<>(this.entityManager)
                        .select(qClazz.count())
                        .from(qClazz)
                        .where(
                                qClazz.recorded.goe(currentMonth.atDay(1).atTime(LocalTime.MIN))
                                        .and(qClazz.recorded.loe(currentMonth.atEndOfMonth().atTime(LocalTime.MAX)))
                        )
                        .fetchOne())
                )
                .subHeadline("Neukunden im " + currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentMonth.getYear() + " \uD83C\uDF89")
                .build();
    }

    @GetMapping("/customers/dashboard/topcustomers")
    public DashboardTextPanelDto getTopCustomerDashboardContent() {
        final LocalDateTime now = LocalDateTime.now();
        final YearMonth currentMonth = YearMonth.from(now);

        final QCustomerAppointment qClazz = QCustomerAppointment.customerAppointment;

        final NumberExpression<BigDecimal> sum = qClazz.activitiesRevenueSnapshot.sum().coalesce(BigDecimal.ZERO).add(qClazz.soldProductsRevenueSnapshot.sum().coalesce(BigDecimal.ZERO));
        final Map<Customer, BigDecimal> revenuePerCustomer = new JPAQuery<>(this.entityManager, JPQLTemplates.DEFAULT)
                .select(qClazz.customer, sum)
                .from(qClazz)
                .where(
                        qClazz.start.goe(currentMonth.atDay(1).atTime(LocalTime.MIN))
                                .and(qClazz.start.loe(currentMonth.atEndOfMonth().atTime(LocalTime.MAX)))
                                .and(qClazz.deleted.ne(Boolean.TRUE))
                )
                .groupBy(qClazz.customer)
                .transform(GroupBy.groupBy(qClazz.customer).as(
                        sum
                ));

        final Map.Entry<Customer, BigDecimal> maxEntry = revenuePerCustomer.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null); // null, falls Map leer

        String name = "?";
        String revenue = "? €";
        if (maxEntry != null) {
            name = Stream.of(maxEntry.getKey().getFirstname(), maxEntry.getKey().getLastname())
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(" "));
            revenue = NumberFormat.getCurrencyInstance(Locale.GERMANY).format(maxEntry.getValue());
        }

        return DashboardTextPanelDto.builder()
                .color(KokuColorEnum.YELLOW)
                .topHeadline(name)
                .headline(revenue)
                .subHeadline("Top Kunde im " + currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentMonth.getYear() + " \uD83D\uDC8E")
                .build();
    }

    @GetMapping("/customers/dashboard")
    public DashboardViewDto getDashboardView() {
        final DashboardViewFactory dashboardFactory = new DashboardViewFactory(
                new DefaultDashboardViewContentIdGenerator(),
                DashboardGridContainerDto.builder()
                        .cols(1)
                        .build()
        );

        dashboardFactory.addContainer(DashboardGridContainerDto.builder()
                .cols(1)
                .md(2)
                .build()
        );

        dashboardFactory.addPanel(DashboardAsyncTextPanelDto.builder()
                .sourceUrl("services/customers/customers/dashboard/revenues/current")
                .build()
        );
        dashboardFactory.addPanel(DashboardAsyncTextPanelDto.builder()
                .sourceUrl("services/customers/customers/dashboard/revenues/preview")
                .build()
        );
        dashboardFactory.endContainer();

        dashboardFactory.addContainer(DashboardGridContainerDto.builder()
                .cols(1)
                .md(2)
                .xl2(3)
                .build()
        );

        dashboardFactory.addPanel(DashboardAsyncTextPanelDto.builder()
                .sourceUrl("services/customers/customers/dashboard/topproduct")
                .build()
        );
        dashboardFactory.addPanel(DashboardAsyncTextPanelDto.builder()
                .sourceUrl("services/customers/customers/dashboard/topactivity")
                .build()
        );
        dashboardFactory.addPanel(DashboardAsyncTextPanelDto.builder()
                .sourceUrl("services/customers/customers/dashboard/newcustomers")
                .build()
        );
        dashboardFactory.endContainer();

        dashboardFactory.addContainer(DashboardGridContainerDto.builder()
                .cols(1)
                .md(2)
                .build()
        );

        dashboardFactory.addPanel(DashboardAsyncTextPanelDto.builder()
                .sourceUrl("services/customers/customers/dashboard/appointments")
                .build()
        );
        dashboardFactory.addPanel(DashboardAsyncTextPanelDto.builder()
                .sourceUrl("services/customers/customers/dashboard/topcustomers")
                .build()
        );

        dashboardFactory.endContainer();

        dashboardFactory.addPanel(DashboardAsyncChartPanelDto.builder()
                .chartUrl("services/customers/customers/dashboard/revenueschart")
                .build()
        );

        return dashboardFactory.create();
    }

    private BigDecimal getOverallRevenue(
            final LocalDateTime from,
            final LocalDateTime to
    ) {
        final QCustomerAppointment qClazz = QCustomerAppointment.customerAppointment;

        final NumberExpression<BigDecimal> soldProductsRevenueSnapshotSum = qClazz.soldProductsRevenueSnapshot.sum();
        final NumberExpression<BigDecimal> activitiesRevenueSnapshotSum = qClazz.activitiesRevenueSnapshot.sum();
        return new JPAQuery<>(this.entityManager, JPQLTemplates.DEFAULT)
                .select(soldProductsRevenueSnapshotSum.coalesce(BigDecimal.ZERO).add(activitiesRevenueSnapshotSum.coalesce(BigDecimal.ZERO)))
                .from(qClazz)
                .where(
                        qClazz.start.goe(from).and(qClazz.start.loe(to))
                                .and(qClazz.deleted.ne(Boolean.TRUE))
                )
                .fetchOne();
    }

    private Map<Long, Integer> getProductUsages(
            final LocalDateTime from,
            final LocalDateTime to
    ) {
        final QCustomerAppointment qClazz = QCustomerAppointment.customerAppointment;

        final List<CustomerAppointment> appointments = new JPAQuery<>(this.entityManager, JPQLTemplates.DEFAULT)
                .select(qClazz)
                .from(qClazz)
                .where(
                        qClazz.start.goe(from).and(qClazz.start.loe(to))
                                .and(qClazz.deleted.ne(Boolean.TRUE))
                )
                .fetch();

        final Map<Long, Integer> productUsages = new HashMap<>();
        for (final CustomerAppointment customerAppointment : appointments) {

            for (CustomerAppointmentSoldProduct soldProduct : customerAppointment.getSoldProducts()) {
                if (!productUsages.containsKey(soldProduct.getId())) {
                    productUsages.put(soldProduct.getProductId(), 0);
                }
                productUsages.put(soldProduct.getProductId(), productUsages.get(soldProduct.getProductId()) + 1);
            }
        }

        return productUsages;
    }

    private Map<Long, Integer> getActivityUsages(
            final LocalDateTime from,
            final LocalDateTime to
    ) {
        final QCustomerAppointment qClazz = QCustomerAppointment.customerAppointment;

        final List<CustomerAppointment> appointments = new JPAQuery<>(this.entityManager, JPQLTemplates.DEFAULT)
                .select(qClazz)
                .from(qClazz)
                .where(qClazz.start.goe(from).and(qClazz.start.loe(to)
                        .and(qClazz.deleted.ne(Boolean.TRUE))
                ))
                .fetch();

        final Map<Long, Integer> activityUsages = new HashMap<>();
        for (final CustomerAppointment customerAppointment : appointments) {

            for (CustomerAppointmentActivity activity : customerAppointment.getActivities()) {
                if (!activityUsages.containsKey(activity.getId())) {
                    activityUsages.put(activity.getActivityId(), 0);
                }
                activityUsages.put(activity.getActivityId(), activityUsages.get(activity.getActivityId()) + 1);
            }
        }

        return activityUsages;
    }

    static String calculatedFormattedDifference(
            final BigDecimal neu,
            final BigDecimal alt
    ) {
        String formatted;
        if (alt.compareTo(BigDecimal.ZERO) == 0) {
            formatted = "?%";
        } else {
            BigDecimal differenz = neu.subtract(alt);
            BigDecimal prozent = differenz
                    .divide(alt, 10, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            BigDecimal gerundet = prozent.setScale(0, RoundingMode.HALF_UP);

            int cmp = gerundet.compareTo(BigDecimal.ZERO);
            if (cmp > 0) {
                formatted = "+" + gerundet.toPlainString() + "%";
            } else if (cmp < 0) {
                formatted = gerundet.toPlainString() + "%";
            } else {
                formatted = "0%";
            }
        }
        return formatted;
    }

}
