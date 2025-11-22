package de.domschmidt.koku.customer.controller;

import com.querydsl.core.types.dsl.CaseBuilder;
import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.formular.dto.content.buttons.EnumButtonType;
import de.domschmidt.formular.dto.content.buttons.FormButtonReloadAction;
import de.domschmidt.formular.factory.DefaultViewContentIdGenerator;
import de.domschmidt.formular.factory.FormViewFactory;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionCloseButtonDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionSendToDifferentEndpointButtonDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionWithConfirmationMessageDto;
import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.customer.kafka.customers.service.CustomerKafkaService;
import de.domschmidt.koku.customer.persistence.Customer;
import de.domschmidt.koku.customer.persistence.CustomerRepository;
import de.domschmidt.koku.customer.persistence.QCustomer;
import de.domschmidt.koku.customer.transformer.CustomerToCustomerDtoTransformer;
import de.domschmidt.koku.customer.transformer.CustomerToCustomerSummaryDtoTransformer;
import de.domschmidt.koku.dto.customer.KokuCustomerDto;
import de.domschmidt.koku.dto.customer.KokuCustomerSummaryDto;
import de.domschmidt.koku.dto.file.KokuFileRefDto;
import de.domschmidt.koku.dto.formular.buttons.ButtonDockableSettings;
import de.domschmidt.koku.dto.formular.buttons.EnumButtonStyle;
import de.domschmidt.koku.dto.formular.buttons.KokuFormButton;
import de.domschmidt.koku.dto.formular.containers.fieldset.FieldsetContainer;
import de.domschmidt.koku.dto.formular.containers.grid.GridContainer;
import de.domschmidt.koku.dto.formular.fields.checkbox.CheckboxFormularField;
import de.domschmidt.koku.dto.formular.fields.input.EnumInputFormularFieldType;
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
import de.domschmidt.koku.dto.formular.fields.textarea.TextareaFormularField;
import de.domschmidt.koku.dto.list.items.style.ListViewConditionalItemValueStylingDto;
import de.domschmidt.koku.dto.list.items.style.ListViewItemStylingDto;
import de.domschmidt.list.dto.response.ListViewDto;
import de.domschmidt.list.dto.response.ListViewSourcePathReference;
import de.domschmidt.list.dto.response.actions.ListViewOpenRoutedContentActionDto;
import de.domschmidt.list.dto.response.actions.ListViewUserConfirmationDto;
import de.domschmidt.list.dto.response.actions.ListViewUserConfirmationValueParamDto;
import de.domschmidt.list.dto.response.events.ListViewEventPayloadAddItemGlobalEventListenerDto;
import de.domschmidt.list.dto.response.events.ListViewEventPayloadItemUpdateGlobalEventListenerDto;
import de.domschmidt.list.dto.response.fields.ListViewFieldReference;
import de.domschmidt.list.dto.response.fields.input.ListViewInputFieldDto;
import de.domschmidt.list.dto.response.inline_content.ListViewRoutedContentDto;
import de.domschmidt.list.dto.response.inline_content.chart.ListViewChartContentDto;
import de.domschmidt.list.dto.response.inline_content.dock.ListViewDockContentDto;
import de.domschmidt.list.dto.response.inline_content.dock.ListViewItemInlineDockContentItemDto;
import de.domschmidt.list.dto.response.inline_content.formular.ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto;
import de.domschmidt.list.dto.response.inline_content.formular.ListViewFormularContentDto;
import de.domschmidt.list.dto.response.inline_content.formular.ListViewInlineFormularContentAfterSavePropagateGlobalEventDto;
import de.domschmidt.list.dto.response.inline_content.formular.ListViewOpenRoutedInlineFormularContentSaveEventDto;
import de.domschmidt.list.dto.response.inline_content.grid.ListViewGridContentDto;
import de.domschmidt.list.dto.response.inline_content.header.ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto;
import de.domschmidt.list.dto.response.inline_content.header.ListViewHeaderContentDto;
import de.domschmidt.list.dto.response.inline_content.list.ListViewListContentDto;
import de.domschmidt.list.dto.response.items.ListViewRoutedDummyItemDto;
import de.domschmidt.list.dto.response.items.actions.ListViewConditionalItemValueActionDto;
import de.domschmidt.list.dto.response.items.actions.ListViewFormularActionSubmitMethodEnumDto;
import de.domschmidt.list.dto.response.items.actions.call_http.ListViewCallHttpListItemActionDto;
import de.domschmidt.list.dto.response.items.actions.call_http.ListViewCallHttpListItemActionMethodEnumDto;
import de.domschmidt.list.dto.response.items.actions.call_http.ListViewCallHttpListValueActionParamDto;
import de.domschmidt.list.dto.response.items.actions.inline_content.ListViewItemClickOpenRoutedContentActionDto;
import de.domschmidt.list.dto.response.items.actions.inline_content.ListViewItemClickOpenRoutedContentActionItemValueParamDto;
import de.domschmidt.list.dto.response.items.preview.ListViewItemPreviewTextDto;
import de.domschmidt.list.dto.response.notifications.ListViewEventPayloadUpdateActionEventDto;
import de.domschmidt.list.dto.response.notifications.ListViewNotificationEvent;
import de.domschmidt.list.dto.response.notifications.ListViewNotificationEventSerenityEnumDto;
import de.domschmidt.list.dto.response.notifications.ListViewNotificationEventValueParamDto;
import de.domschmidt.list.factory.DefaultListViewContentIdGenerator;
import de.domschmidt.list.factory.ListViewFactory;
import de.domschmidt.listquery.dto.request.ListQuery;
import de.domschmidt.listquery.dto.response.ListPage;
import de.domschmidt.listquery.factory.ListQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.querydsl.core.types.dsl.Expressions.stringTemplate;

@RestController
@RequestMapping()
@Slf4j
@RequiredArgsConstructor
public class CustomerController {
    private final EntityManager entityManager;
    private final CustomerRepository customerRepository;
    private final CustomerToCustomerDtoTransformer transformer;
    private final CustomerKafkaService customerKafkaService;

    @GetMapping("/customers/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory(
                new DefaultViewContentIdGenerator(),
                GridContainer.builder()
                        .cols(1)
                        .build()
        );

        addMainSection(formFactory);
        addLivingSection(formFactory);
        addPhoneSection(formFactory);
        addHealthSection(formFactory);
        addAllergySection(formFactory);
        addIllnessSection(formFactory);

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

        return formFactory.create();
    }

    @GetMapping("/customers/list")
    public ListViewDto getListView() {
        final ListViewFactory listViewFactory = new ListViewFactory(
                new DefaultListViewContentIdGenerator(),
                KokuCustomerDto.Fields.id
        );

        final ListViewFieldReference fullNameWithOnFirstNameBasisFieldRef = listViewFactory.addField(
                KokuCustomerDto.Fields.fullNameWithOnFirstNameBasis,
                ListViewInputFieldDto.builder()
                        .label("Vor- und Nachname")
                        .build()
        );
        final ListViewFieldReference addressFieldRef = listViewFactory.addField(
                KokuCustomerDto.Fields.address,
                ListViewInputFieldDto.builder()
                        .label("Adresse")
                        .build()
        );
        final ListViewFieldReference addressLine2FieldRef = listViewFactory.addField(
                KokuCustomerDto.Fields.addressLine2,
                ListViewInputFieldDto.builder()
                        .label("Adresszeile 2")
                        .build()
        );
        final ListViewSourcePathReference idSourcePathFieldRef = listViewFactory.addSourcePath(KokuCustomerDto.Fields.id);
        final ListViewSourcePathReference deletedSourceRef = listViewFactory.addSourcePath(KokuCustomerDto.Fields.deleted);
        final ListViewSourcePathReference initialsSourceRef = listViewFactory.addSourcePath(KokuCustomerDto.Fields.initials);

        listViewFactory.addAction(ListViewOpenRoutedContentActionDto.builder()
                .route("new")
                .icon("PLUS")
                .build()
        );
        listViewFactory.addRoutedItem(ListViewRoutedDummyItemDto.builder()
                .route("new")
                .text("Neuer Kunde")
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadAddItemGlobalEventListenerDto.builder()
                .eventName("customer-created")
                .idPath(KokuCustomerDto.Fields.id)
                .valueMapping(Map.of(
                        KokuCustomerDto.Fields.fullNameWithOnFirstNameBasis, fullNameWithOnFirstNameBasisFieldRef,
                        KokuCustomerDto.Fields.address, addressFieldRef,
                        KokuCustomerDto.Fields.addressLine2, addressLine2FieldRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route("new")
                        .inlineContent(ListViewHeaderContentDto.builder()
                                .title("Neuer Kunde")
                                .content(ListViewFormularContentDto.builder()
                                        .formularUrl("services/customers/customers/form")
                                        .submitUrl("services/customers/customers")
                                        .submitMethod(ListViewFormularActionSubmitMethodEnumDto.POST)
                                        .maxWidthInPx(800)
                                        .onSaveEvents(Arrays.asList(
                                                ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                        .eventName("customer-created")
                                                        .build(),
                                                ListViewOpenRoutedInlineFormularContentSaveEventDto.builder()
                                                        .route(":customerId/information")
                                                        .params(Arrays.asList(
                                                                ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto.builder()
                                                                        .param(":customerId")
                                                                        .valuePath(KokuCustomerDto.Fields.id)
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
                .route(":customerId/information")
                .params(Arrays.asList(ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                        .param(":customerId")
                        .valueReference(idSourcePathFieldRef)
                        .build()
                ))
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadItemUpdateGlobalEventListenerDto.builder()
                .eventName("customer-updated")
                .idPath(KokuCustomerDto.Fields.id)
                .valueMapping(Map.of(
                        KokuCustomerDto.Fields.fullNameWithOnFirstNameBasis, fullNameWithOnFirstNameBasisFieldRef,
                        KokuCustomerDto.Fields.address, addressFieldRef,
                        KokuCustomerDto.Fields.addressLine2, addressLine2FieldRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route(":customerId")
                        .itemId(":customerId")
                        .inlineContent(ListViewHeaderContentDto.builder()
                                .sourceUrl("services/customers/customers/:customerId/summary")
                                .titlePath(KokuCustomerSummaryDto.Fields.fullName)
                                .globalEventListeners(Arrays.asList(ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto.builder()
                                        .eventName("customer-updated")
                                        .idPath(KokuCustomerDto.Fields.id)
                                        .titleValuePath(KokuCustomerDto.Fields.fullNameWithOnFirstNameBasis)
                                        .build()
                                ))
                                .content(ListViewDockContentDto.builder()
                                        .content(Arrays.asList(
                                                ListViewItemInlineDockContentItemDto.builder()
                                                        .id("information")
                                                        .route("information")
                                                        .icon("INFORMATION_CIRCLE")
                                                        .title("Bearbeiten")
                                                        .content(ListViewFormularContentDto.builder()
                                                                .formularUrl("services/customers/customers/form")
                                                                .sourceUrl("services/customers/customers/:customerId")
                                                                .submitMethod(ListViewFormularActionSubmitMethodEnumDto.PUT)
                                                                .maxWidthInPx(800)
                                                                .onSaveEvents(Arrays.asList(
                                                                        ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                                                .eventName("customer-updated")
                                                                                .build()
                                                                ))
                                                                .build()
                                                        )
                                                        .build(),
                                                ListViewItemInlineDockContentItemDto.builder()
                                                        .id("appointments")
                                                        .route("appointments")
                                                        .icon("CALENDAR")
                                                        .title("Termine")
                                                        .content(ListViewListContentDto.builder()
                                                                .listUrl("services/customers/customers/appointments/list")
                                                                .sourceUrl("services/customers/customers/:customerId/appointments/query")
                                                                .build()
                                                        )
                                                        .build(),
                                                ListViewItemInlineDockContentItemDto.builder()
                                                        .id("documents")
                                                        .title("Dokumente")
                                                        .route("documents")
                                                        .icon("DOCUMENT")
                                                        .content(ListViewListContentDto.builder()
                                                                .listUrl("services/files/files/list?ref=" + KokuFileRefDto.CUSTOMER + "&refId=:customerId&contextEndpointUrl=services/customers/customers/:customerId")
                                                                .sourceUrl("services/files/files/query?ref=" + KokuFileRefDto.CUSTOMER + "&refId=:customerId")
                                                                .build()
                                                        )
                                                        .build(),
                                                ListViewItemInlineDockContentItemDto.builder()
                                                        .id("statistics")
                                                        .title("Statistik")
                                                        .route("statistics")
                                                        .icon("CHART_BAR")
                                                        .content(ListViewGridContentDto.builder()
                                                                .cols(1)
                                                                .xl5(2)
                                                                .content(Arrays.asList(
                                                                        ListViewChartContentDto.builder()
                                                                                .chartUrl("services/customers/customers/:customerId/statistics/yearlyvisits")
                                                                                .build(),
                                                                        ListViewChartContentDto.builder()
                                                                                .chartUrl("services/customers/customers/:customerId/statistics/yearlyrevenue")
                                                                                .build()
                                                                ))
                                                                .build()
                                                        )
                                                        .build()
                                        ))
                                        .build()
                                )
                                .build()
                        )
                        .build()
        );

        listViewFactory.addGlobalItemStyling(ListViewConditionalItemValueStylingDto.builder()
                        .compareValuePath(KokuCustomerDto.Fields.deleted)
                        .expectedValue(Boolean.TRUE)
                        .positiveStyling(ListViewItemStylingDto.builder()
                                .lineThrough(true)
                                .opacity((short) 50)
                                .build()
                        )
                        .build()
        );
        listViewFactory.addItemAction(ListViewConditionalItemValueActionDto.builder()
                .compareValuePath(KokuCustomerDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("ARROW_LEFT_START_ON_RECTANGLE")
                        .url("services/customers/customers/:customerId/restore")
                        .params(Arrays.asList(
                                ListViewCallHttpListValueActionParamDto.builder()
                                        .param(":customerId")
                                        .valueReference(idSourcePathFieldRef)
                                        .build()
                        ))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.PUT)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Kunde wiederherstellen")
                                .content(":name wiederherstellen?")
                                .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                        .param(":name")
                                        .valueReference(fullNameWithOnFirstNameBasisFieldRef)
                                        .build()
                                ))
                                .build()
                        )
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text(":name wurde erfolgreich wiederhergestellt")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(fullNameWithOnFirstNameBasisFieldRef)
                                                        .build()
                                        ))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuCustomerDto.Fields.id)
                                        .valueMapping(Map.of(
                                                KokuCustomerDto.Fields.deleted, deletedSourceRef
                                        ))
                                        .build()
                        ))
                        .failEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text(":name konnte nicht wiederhergestellt werden")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(fullNameWithOnFirstNameBasisFieldRef)
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build())
                .negativeAction(
                        ListViewCallHttpListItemActionDto.builder()
                                .icon("TRASH")
                                .url("services/customers/customers/:customerId")
                                .params(Arrays.asList(
                                        ListViewCallHttpListValueActionParamDto.builder()
                                                .param(":customerId")
                                                .valueReference(idSourcePathFieldRef)
                                                .build()
                                ))
                                .method(ListViewCallHttpListItemActionMethodEnumDto.DELETE)
                                .userConfirmation(ListViewUserConfirmationDto.builder()
                                        .headline("Kunde löschen")
                                        .content(":name als gelöscht markieren?")
                                        .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                                .param(":name")
                                                .valueReference(fullNameWithOnFirstNameBasisFieldRef)
                                                .build()
                                        ))
                                        .build()
                                )

                                .successEvents(Arrays.asList(
                                        ListViewNotificationEvent.builder()
                                                .text(":name erfolgreich als gelöscht markiert")
                                                .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                                .params(Arrays.asList(
                                                        ListViewNotificationEventValueParamDto.builder()
                                                                .param(":name")
                                                                .valueReference(fullNameWithOnFirstNameBasisFieldRef)
                                                                .build()
                                                ))
                                                .build(),
                                        ListViewEventPayloadUpdateActionEventDto.builder()
                                                .idPath(KokuCustomerDto.Fields.id)
                                                .valueMapping(Map.of(
                                                        KokuCustomerDto.Fields.deleted, deletedSourceRef
                                                ))
                                                .build()
                                ))
                                .failEvents(Arrays.asList(
                                        ListViewNotificationEvent.builder()
                                                .text(":name konnte nicht als gelöscht markiert werden")
                                                .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                                .params(Arrays.asList(
                                                        ListViewNotificationEventValueParamDto.builder()
                                                                .param(":name")
                                                                .valueReference(fullNameWithOnFirstNameBasisFieldRef)
                                                                .build()
                                                ))
                                                .build()
                                ))
                                .build()
                )
                .build()
        );

        listViewFactory.setItemPreview(ListViewItemPreviewTextDto.builder()
                .valuePath(KokuCustomerDto.Fields.initials)
                .build()
        );

        return listViewFactory.create();
    }

    @PostMapping("/customers/query")
    public ListPage findAll(@RequestBody(required = false) final ListQuery predicate) {
        final QCustomer qClazz = QCustomer.customer;

        final ListQueryFactory<Customer> listQueryFactory = new ListQueryFactory<>(
                this.entityManager,
                qClazz,
                qClazz.id,
                predicate
        );

        listQueryFactory.setDefaultOrder(qClazz.firstname.asc());

        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.id,
                qClazz.id
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.deleted,
                qClazz.deleted
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.firstName,
                qClazz.firstname
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.lastName,
                qClazz.lastname
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.fullName,
                qClazz.firstname
                        .concat(" ")
                        .concat(qClazz.lastname)
                        .trim()
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.fullNameWithOnFirstNameBasis,
                qClazz.firstname
                        .concat(" ")
                        .concat(qClazz.lastname)
                        .concat(" ")
                        .concat(new CaseBuilder().when(qClazz.onFirstnameBasis.eq(Boolean.TRUE)).then("*").otherwise(""))
                        .trim()
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.initials,
                qClazz.firstname.substring(0, 1)
                        .concat(qClazz.lastname.substring(0, 1))
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.email,
                qClazz.email
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.address,
                qClazz.address
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.postalCode,
                qClazz.postalCode
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.city,
                qClazz.city
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.addressLine2,
                stringTemplate("cast(concat_ws(' ', NULLIF(TRIM({0}), ''), NULLIF(TRIM({1}), '')) as text)",
                        qClazz.postalCode,
                        qClazz.city
                )
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.privateTelephoneNo,
                qClazz.privateTelephoneNo
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.businessTelephoneNo,
                qClazz.businessTelephoneNo
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.mobileTelephoneNo,
                qClazz.mobileTelephoneNo
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.medicalTolerance,
                qClazz.medicalTolerance
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.additionalInfo,
                qClazz.additionalInfo
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.birthday,
                qClazz.birthday
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.onFirstnameBasis,
                qClazz.onFirstnameBasis
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.hayFever,
                qClazz.hayFever
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.plasterAllergy,
                qClazz.plasterAllergy
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.cyanoacrylateAllergy,
                qClazz.cyanoacrylateAllergy
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.asthma,
                qClazz.asthma
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.dryEyes,
                qClazz.dryEyes
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.circulationProblems,
                qClazz.circulationProblems
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.epilepsy,
                qClazz.epilepsy
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.diabetes,
                qClazz.diabetes
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.claustrophobia,
                qClazz.claustrophobia
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.neurodermatitis,
                qClazz.neurodermatitis
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.contacts,
                qClazz.contacts
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.glasses,
                qClazz.glasses
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.eyeDisease,
                qClazz.eyeDisease
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.allergy,
                qClazz.allergy
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.covid19vaccinated,
                qClazz.covid19vaccinated
        );
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.covid19boostered,
                qClazz.covid19boostered
        );

        return listQueryFactory.create();
    }

    @GetMapping(value = "/customers/{id}")
    public KokuCustomerDto read(@PathVariable("id") Long id) {
        final Customer customer = this.customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return new CustomerToCustomerDtoTransformer().transformToDto(customer);
    }

    @GetMapping(value = "/customers/{id}/summary")
    public KokuCustomerSummaryDto readSummary(@PathVariable("id") Long id) {
        final Customer customer = this.customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return new CustomerToCustomerSummaryDtoTransformer().transformToDto(customer);
    }

    @PutMapping(value = "/customers/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuCustomerDto update(
            @PathVariable("id") Long id,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate,
            @RequestBody KokuCustomerDto updatedDto
    ) {
        final Customer customer = this.entityManager.getReference(Customer.class, id);
        if (!Boolean.TRUE.equals(forceUpdate) && !customer.getVersion().equals(updatedDto.getVersion())) {
            throw new KokuBusinessExceptionWithConfirmationMessage(
                    KokuBusinessExceptionWithConfirmationMessageDto.builder()
                            .headline("Konflikt")
                            .confirmationMessage("Der Kunde wurde zwischenzeitlich bearbeitet.\nWillst Du die Speicherung dennoch vornehmen?")
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
                                    .endpointUrl(String.format("services/customers/customers/%s?forceUpdate=%s", id, Boolean.TRUE))
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
        this.transformer.transformToEntity(customer, updatedDto);
        this.entityManager.flush();
        sendCustomerUpdate(customer);
        return this.transformer.transformToDto(customer);
    }

    @DeleteMapping(value = "/customers/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuCustomerDto delete(@PathVariable("id") Long id) {
        final Customer customer = this.entityManager.getReference(Customer.class, id);
        if (customer.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is not deletable");
        }
        customer.setDeleted(true);
        this.entityManager.flush();
        sendCustomerUpdate(customer);
        return this.transformer.transformToDto(customer);
    }

    @PutMapping(value = "/customers/{id}/restore")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuCustomerDto restore(@PathVariable("id") Long id) {
        final Customer customer = this.entityManager.getReference(Customer.class, id);
        if (!customer.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is not restorable");
        }
        customer.setDeleted(false);
        this.entityManager.flush();
        sendCustomerUpdate(customer);
        return this.transformer.transformToDto(customer);
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public KokuCustomerDto create(@RequestBody KokuCustomerDto newDto) {
        final Customer newCustomer = this.transformer.transformToEntity(new Customer(), newDto);
        final Customer savedCustomer = this.customerRepository.saveAndFlush(newCustomer);
        sendCustomerUpdate(savedCustomer);
        return this.transformer.transformToDto(savedCustomer);
    }

    public void sendCustomerUpdate(final Customer updatedCustomer) {
        try {
            this.customerKafkaService.sendCustomer(updatedCustomer);
        } catch (final ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Unable to export to kafka, due to: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to export to kafka");
        }
    }

    private static void addIllnessSection(FormViewFactory formFactory) {
        formFactory.addContainer(FieldsetContainer.builder()
                .title("Erkrankungen")
                .build()
        );
        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .xl(3)
                .build()
        );
        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.contacts)
                .label("Kontaktlinsen")
                .build()
        );
        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.glasses)
                .label("Brillenträger")
                .build()
        );

        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.dryEyes)
                .label("Trockene Augen")
                .build()
        );
        formFactory.endContainer();

        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .build()
        );

        formFactory.addField(TextareaFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.eyeDisease)
                .label("Andere Augenerkrankungen")
                .build()
        );
        formFactory.endContainer();

        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .xl(3)
                .build()
        );

        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.asthma)
                .label("Asthma")
                .build()
        );
        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.circulationProblems)
                .label("Kreislaufprobleme")
                .build()
        );

        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.epilepsy)
                .label("Epilepsie")
                .build()
        );

        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.diabetes)
                .label("Diabetes")
                .build()
        );

        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.claustrophobia)
                .label("Klaustrophobie")
                .build()
        );

        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.neurodermatitis)
                .label("Neurodermitis")
                .build()
        );
        formFactory.endContainer();
        formFactory.endContainer();
    }

    private static void addAllergySection(FormViewFactory formFactory) {
        formFactory.addContainer(FieldsetContainer.builder()
                .title("Allergien")
                .build()
        );
        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .xl(3)
                .build()
        );
        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.hayFever)
                .label("Heuschnupfen")
                .build()
        );
        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.plasterAllergy)
                .label("Allergie gegen Pflaster")
                .build()
        );
        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.cyanoacrylateAllergy)
                .label("Allergie gegen Cyanacrylat")
                .build()
        );
        formFactory.endContainer();

        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .build()
        );
        formFactory.addField(TextareaFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.allergy)
                .label("Andere Allergien")
                .build()
        );
        formFactory.endContainer();
        formFactory.endContainer();
    }

    private static void addHealthSection(FormViewFactory formFactory) {
        formFactory.addContainer(FieldsetContainer.builder()
                .title("Gesundheit")
                .build()
        );
        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .xl(3)
                .build()
        );

        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.covid19vaccinated)
                .label("Covid 19 geimpft")
                .build()
        );

        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.covid19boostered)
                .label("Covid 19 geboostert")
                .build()
        );
        formFactory.endContainer();
        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .build()
        );

        formFactory.addField(TextareaFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.medicalTolerance)
                .label("Medizinische Informationen")
                .build()
        );

        formFactory.endContainer();
        formFactory.endContainer();
    }

    private static void addPhoneSection(FormViewFactory formFactory) {
        formFactory.addContainer(FieldsetContainer.builder()
                .title("Erreichbarkeit")
                .build()
        );
        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .xl(3)
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.privateTelephoneNo)
                .label("Private Telefonnummer")
                .build()
        );

        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.mobileTelephoneNo)
                .label("Mobile Telefonnummer")
                .build()
        );

        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.businessTelephoneNo)
                .label("Geschäftliche Telefonnummer")
                .build()
        );

        formFactory.endContainer();
        formFactory.endContainer();
    }

    private static void addLivingSection(FormViewFactory formFactory) {
        formFactory.addContainer(FieldsetContainer.builder()
                .title("Wohnsituation")
                .build()
        );
        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.address)
                .label("Adresse")
                .build()
        );

        formFactory.endContainer();
        formFactory.addContainer(GridContainer.builder()
                .cols(2)
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.postalCode)
                .label("Postleitzahl")
                .build()
        );

        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.city)
                .label("Wohnort")
                .build()
        );

        formFactory.endContainer();
        formFactory.endContainer();
    }

    private static void addMainSection(FormViewFactory formFactory) {
        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .build()
        );
        formFactory.addField(CheckboxFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.onFirstnameBasis)
                .label("Duzen")
                .build()
        );
        formFactory.endContainer();
        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .xl(2)
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.firstName)
                .label("Vorname")
                .required(true)
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.lastName)
                .label("Nachname")
                .required(true)
                .build()
        );
        formFactory.endContainer();
        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.email)
                .label("Email")
                .type(EnumInputFormularFieldType.EMAIL)
                .build()
        );
        formFactory.endContainer();
        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.birthday)
                .label("Geburtstag")
                .type(EnumInputFormularFieldType.DATE)
                .build()
        );
        formFactory.addField(TextareaFormularField.builder()
                .valuePath(KokuCustomerDto.Fields.additionalInfo)
                .label("Zusätzliche Informationen")
                .build()
        );
        formFactory.endContainer();
    }


}
