package de.domschmidt.koku.customer.controller;

import static com.querydsl.core.types.dsl.Expressions.stringTemplate;

import com.querydsl.core.types.dsl.CaseBuilder;
import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.formular.dto.content.buttons.EnumButtonType;
import de.domschmidt.formular.factory.FormOutlet;
import de.domschmidt.formular.factory.FormViewFactory;
import de.domschmidt.koku.business_exception.dto.KokuBusinessErrorWithConfirmationMessageDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionCloseButtonDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionSendToDifferentEndpointButtonDto;
import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.customer.kafka.customers.service.CustomerKafkaService;
import de.domschmidt.koku.customer.persistence.Customer;
import de.domschmidt.koku.customer.persistence.CustomerRepository;
import de.domschmidt.koku.customer.persistence.QCustomer;
import de.domschmidt.koku.customer.transformer.CustomerToCustomerDtoTransformer;
import de.domschmidt.koku.customer.transformer.CustomerToCustomerSummaryDtoTransformer;
import de.domschmidt.koku.dto.customer.KokuCustomerDto;
import de.domschmidt.koku.dto.customer.KokuCustomerSummaryDto;
import de.domschmidt.koku.dto.formular.buttons.ButtonDockableSettings;
import de.domschmidt.koku.dto.formular.buttons.EnumButtonStyle;
import de.domschmidt.koku.dto.formular.buttons.FormButtonUserConfirmationSourcePathParamDto;
import de.domschmidt.koku.dto.formular.buttons.KokuFormButton;
import de.domschmidt.koku.dto.formular.containers.conditional.ConditionalContainer;
import de.domschmidt.koku.dto.formular.containers.fieldset.FieldsetContainer;
import de.domschmidt.koku.dto.formular.containers.grid.GridContainer;
import de.domschmidt.koku.dto.formular.events.FormNotificationEvent;
import de.domschmidt.koku.dto.formular.events.FormNotificationEventSerenityEnumDto;
import de.domschmidt.koku.dto.formular.events.FormNotificationEventValueParamDto;
import de.domschmidt.koku.dto.formular.events.FormPropagateGlobalEventDto;
import de.domschmidt.koku.dto.formular.fields.checkbox.CheckboxFormularField;
import de.domschmidt.koku.dto.formular.fields.input.DateInputFormularField;
import de.domschmidt.koku.dto.formular.fields.input.EnumInputFormularFieldType;
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
import de.domschmidt.koku.dto.formular.fields.textarea.TextareaFormularField;
import de.domschmidt.koku.dto.formular.listeners.FormViewEventPayloadSourceUpdateGlobalEventListenerDto;
import de.domschmidt.koku.dto.formular.user_confirmation.FormUserConfirmationDto;
import de.domschmidt.koku.dto.list.fields.input.ListViewInputFieldDto;
import de.domschmidt.koku.dto.list.filters.ListViewToggleFilterDefaultStateEnum;
import de.domschmidt.koku.dto.list.filters.ListViewToggleFilterDto;
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
import de.domschmidt.listquery.dto.request.EnumSearchOperator;
import de.domschmidt.listquery.dto.request.ListQuery;
import de.domschmidt.listquery.dto.request.QueryPredicate;
import de.domschmidt.listquery.dto.response.ListPage;
import de.domschmidt.listquery.factory.ListQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@RestController
@RequestMapping()
@Slf4j
public class CustomerController {
    private static final String CUSTOMER_UPDATED_EVENT = "customer-updated";
    private static final String CUSTOMER_CREATED_EVENT = "customer-created";
    private static final String CUSTOMER_ID_PARAM = ":customerId";
    private static final String NAME_PARAM = ":name";
    private static final String CUSTOMER_LABEL = "Kunde ";
    private static final String CUSTOMER_SERVICE_URL = "services/customers/customers/";

    private final EntityManager entityManager;
    private final CustomerRepository customerRepository;
    private final CustomerToCustomerDtoTransformer transformer;
    private final CustomerKafkaService customerKafkaService;

    @GetMapping("/customers/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory();
        final String rootId =
                formFactory.addContent(GridContainer.builder().cols(1).build());

        addMainSection(formFactory, rootId);
        addLivingSection(formFactory, rootId);
        addPhoneSection(formFactory, rootId);
        addHealthSection(formFactory, rootId);
        addAllergySection(formFactory, rootId);
        addIllnessSection(formFactory, rootId);

        formFactory
                .place(formFactory.addContent(KokuFormButton.builder()
                        .buttonType(EnumButtonType.SUBMIT)
                        .text("Speichern")
                        .title("Jetzt speichern")
                        .styles(Arrays.asList(EnumButtonStyle.BLOCK))
                        .dockable(true)
                        .dockableSettings(ButtonDockableSettings.builder()
                                .icon("SAVE")
                                .styles(Arrays.asList(EnumButtonStyle.CIRCLE))
                                .build())
                        .build()))
                .in(rootId)
                .outlet(FormOutlet.CONTENT);

        final String deleteContainerId = formFactory.addContent(ConditionalContainer.builder()
                .compareValuePath(KokuCustomerDto.Fields.deleted)
                .expectedValue(Boolean.FALSE)
                .build());
        formFactory.place(deleteContainerId).in(rootId).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(KokuFormButton.builder()
                        .buttonType(EnumButtonType.SUBMIT)
                        .text("Löschen")
                        .title("Jetzt löschen")
                        .styles(Arrays.asList(EnumButtonStyle.BLOCK, EnumButtonStyle.ERROR, EnumButtonStyle.OUTLINE))
                        .dockableSettings(ButtonDockableSettings.builder()
                                .icon("TRASH")
                                .styles(Arrays.asList(EnumButtonStyle.CIRCLE, EnumButtonStyle.ERROR))
                                .build())
                        .submitPayload(KokuCustomerDto.builder().deleted(true).build())
                        .userConfirmation(FormUserConfirmationDto.builder()
                                .headline(CUSTOMER_LABEL + "löschen")
                                .content(CUSTOMER_LABEL + NAME_PARAM + " als gelöscht markieren?")
                                .params(Arrays.asList(FormButtonUserConfirmationSourcePathParamDto.builder()
                                        .param(NAME_PARAM)
                                        .sourcePath(KokuCustomerDto.Fields.fullName)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                FormNotificationEvent.builder()
                                        .text(CUSTOMER_LABEL + NAME_PARAM + " erfolgreich als gelöscht markiert")
                                        .serenity(FormNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(FormNotificationEventValueParamDto.builder()
                                                .param(NAME_PARAM)
                                                .sourcePath(KokuCustomerDto.Fields.fullName)
                                                .build()))
                                        .build(),
                                FormPropagateGlobalEventDto.builder()
                                        .eventName(CUSTOMER_UPDATED_EVENT)
                                        .build()))
                        .failEvents(Arrays.asList(FormNotificationEvent.builder()
                                .text(CUSTOMER_LABEL + NAME_PARAM + " konnte nicht als gelöscht markiert werden")
                                .serenity(FormNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(FormNotificationEventValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .sourcePath(KokuCustomerDto.Fields.fullName)
                                        .build()))
                                .build()))
                        .build()))
                .in(deleteContainerId)
                .outlet(FormOutlet.CONTENT);

        final String restoreContainerId = formFactory.addContent(ConditionalContainer.builder()
                .compareValuePath(KokuCustomerDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .build());
        formFactory.place(restoreContainerId).in(rootId).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(KokuFormButton.builder()
                        .buttonType(EnumButtonType.SUBMIT)
                        .text("Wiederherstellen")
                        .title("Jetzt wiederherstellen")
                        .styles(Arrays.asList(EnumButtonStyle.BLOCK, EnumButtonStyle.SUCCESS, EnumButtonStyle.OUTLINE))
                        .dockableSettings(ButtonDockableSettings.builder()
                                .icon("ARROW_LEFT_START_ON_RECTANGLE")
                                .styles(Arrays.asList(EnumButtonStyle.CIRCLE, EnumButtonStyle.SUCCESS))
                                .build())
                        .submitPayload(KokuCustomerDto.builder().deleted(false).build())
                        .userConfirmation(FormUserConfirmationDto.builder()
                                .headline(CUSTOMER_LABEL + "wiederherstellen")
                                .content(CUSTOMER_LABEL + NAME_PARAM + " wiederherstellen?")
                                .params(Arrays.asList(FormButtonUserConfirmationSourcePathParamDto.builder()
                                        .param(NAME_PARAM)
                                        .sourcePath(KokuCustomerDto.Fields.fullName)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                FormNotificationEvent.builder()
                                        .text(CUSTOMER_LABEL + NAME_PARAM + " wurde erfolgreich wiederhergestellt")
                                        .serenity(FormNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(FormNotificationEventValueParamDto.builder()
                                                .param(NAME_PARAM)
                                                .sourcePath(KokuCustomerDto.Fields.fullName)
                                                .build()))
                                        .build(),
                                FormPropagateGlobalEventDto.builder()
                                        .eventName(CUSTOMER_UPDATED_EVENT)
                                        .build()))
                        .failEvents(Arrays.asList(FormNotificationEvent.builder()
                                .text(CUSTOMER_LABEL + NAME_PARAM + " konnte nicht wiederhergestellt werden")
                                .serenity(FormNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(FormNotificationEventValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .sourcePath(KokuCustomerDto.Fields.fullName)
                                        .build()))
                                .build()))
                        .build()))
                .in(restoreContainerId)
                .outlet(FormOutlet.CONTENT);

        formFactory.addGlobalEventListener(FormViewEventPayloadSourceUpdateGlobalEventListenerDto.builder()
                .eventName(CUSTOMER_UPDATED_EVENT)
                .idPath(KokuCustomerDto.Fields.id)
                .build());

        return formFactory.create(rootId);
    }

    @GetMapping("/customers/list")
    public ListViewDto getListView() {
        final ListViewFactory listViewFactory =
                new ListViewFactory(new DefaultListViewContentIdGenerator(), KokuCustomerDto.Fields.id);

        final ListViewFieldReference fullNameWithOnFirstNameBasisFieldRef = listViewFactory.addField(
                KokuCustomerDto.Fields.fullNameWithOnFirstNameBasis,
                ListViewInputFieldDto.builder().label("Vor- und Nachname").build());
        final ListViewFieldReference addressFieldRef = listViewFactory.addField(
                KokuCustomerDto.Fields.address,
                ListViewInputFieldDto.builder().label("Adresse").build());
        final ListViewFieldReference addressLine2FieldRef = listViewFactory.addField(
                KokuCustomerDto.Fields.addressLine2,
                ListViewInputFieldDto.builder().label("Adresszeile 2").build());
        final ListViewSourcePathReference idSourcePathFieldRef =
                listViewFactory.addSourcePath(KokuCustomerDto.Fields.id);
        final ListViewSourcePathReference deletedSourceRef =
                listViewFactory.addSourcePath(KokuCustomerDto.Fields.deleted);
        listViewFactory.addSourcePath(KokuCustomerDto.Fields.initials);

        listViewFactory.addFilter(
                KokuCustomerDto.Fields.deleted,
                ListViewToggleFilterDto.builder()
                        .label("Gelöschte anzeigen?")
                        .enabledPredicate(QueryPredicate.builder()
                                .searchExpression(Boolean.TRUE.toString())
                                .searchOperator(EnumSearchOperator.EQ)
                                .build())
                        .disabledPredicate(QueryPredicate.builder()
                                .searchExpression(Boolean.FALSE.toString())
                                .searchOperator(EnumSearchOperator.EQ)
                                .build())
                        .defaultState(ListViewToggleFilterDefaultStateEnum.DISABLED)
                        .build());

        listViewFactory.addAction(ListViewOpenRoutedContentActionDto.builder()
                .route("new")
                .icon("PLUS")
                .build());
        listViewFactory.addRoutedItem(ListViewRoutedDummyItemDto.builder()
                .route("new")
                .text("Neuer Kunde")
                .build());
        listViewFactory.addGlobalEventListener(ListViewEventPayloadAddItemGlobalEventListenerDto.builder()
                .eventName(CUSTOMER_CREATED_EVENT)
                .idPath(KokuCustomerDto.Fields.id)
                .valueMapping(Map.of(
                        KokuCustomerDto.Fields.fullNameWithOnFirstNameBasis, fullNameWithOnFirstNameBasisFieldRef,
                        KokuCustomerDto.Fields.address, addressFieldRef,
                        KokuCustomerDto.Fields.addressLine2, addressLine2FieldRef,
                        KokuCustomerDto.Fields.deleted, deletedSourceRef))
                .build());
        listViewFactory.addRoutedContent(ListViewRoutedContentDto.builder()
                .route("new")
                .inlineContent(ListViewHeaderContentDto.builder()
                        .title("Neuer Kunde")
                        .content(ListViewFormularContentDto.builder()
                                .formularUrl(CUSTOMER_SERVICE_URL + "form")
                                .submitUrl("services/customers/customers")
                                .submitMethod(ListViewFormularActionSubmitMethodEnumDto.POST)
                                .maxWidthInPx(800)
                                .onSaveEvents(Arrays.asList(
                                        ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                .eventName(CUSTOMER_CREATED_EVENT)
                                                .build(),
                                        ListViewOpenRoutedInlineFormularContentSaveEventDto.builder()
                                                .route(CUSTOMER_ID_PARAM + "/information")
                                                .params(Arrays.asList(
                                                        ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto
                                                                .builder()
                                                                .param(CUSTOMER_ID_PARAM)
                                                                .valuePath(KokuCustomerDto.Fields.id)
                                                                .build()))
                                                .build()))
                                .build())
                        .build())
                .build());

        listViewFactory.setItemClickAction(ListViewItemClickOpenRoutedContentActionDto.builder()
                .route(CUSTOMER_ID_PARAM + "/information")
                .params(Arrays.asList(ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                        .param(CUSTOMER_ID_PARAM)
                        .valueReference(idSourcePathFieldRef)
                        .build()))
                .build());
        listViewFactory.addGlobalEventListener(ListViewEventPayloadItemUpdateGlobalEventListenerDto.builder()
                .eventName(CUSTOMER_UPDATED_EVENT)
                .idPath(KokuCustomerDto.Fields.id)
                .valueMapping(Map.of(
                        KokuCustomerDto.Fields.fullNameWithOnFirstNameBasis, fullNameWithOnFirstNameBasisFieldRef,
                        KokuCustomerDto.Fields.address, addressFieldRef,
                        KokuCustomerDto.Fields.addressLine2, addressLine2FieldRef,
                        KokuCustomerDto.Fields.deleted, deletedSourceRef))
                .build());
        listViewFactory.addRoutedContent(ListViewRoutedContentDto.builder()
                .route(CUSTOMER_ID_PARAM)
                .itemId(CUSTOMER_ID_PARAM)
                .inlineContent(ListViewHeaderContentDto.builder()
                        .sourceUrl(CUSTOMER_SERVICE_URL + CUSTOMER_ID_PARAM + "/summary")
                        .titlePath(KokuCustomerSummaryDto.Fields.fullName)
                        .globalEventListeners(
                                Arrays.asList(ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto.builder()
                                        .eventName(CUSTOMER_UPDATED_EVENT)
                                        .idPath(KokuCustomerDto.Fields.id)
                                        .titleValuePath(KokuCustomerDto.Fields.fullNameWithOnFirstNameBasis)
                                        .build()))
                        .content(ListViewDockContentDto.builder()
                                .content(Arrays.asList(
                                        ListViewItemInlineDockContentItemDto.builder()
                                                .id("information")
                                                .route("information")
                                                .icon("INFORMATION_CIRCLE")
                                                .title("Bearbeiten")
                                                .content(ListViewFormularContentDto.builder()
                                                        .formularUrl(CUSTOMER_SERVICE_URL + "form")
                                                        .sourceUrl(CUSTOMER_SERVICE_URL + CUSTOMER_ID_PARAM)
                                                        .submitMethod(ListViewFormularActionSubmitMethodEnumDto.PUT)
                                                        .maxWidthInPx(800)
                                                        .onSaveEvents(Arrays.asList(
                                                                ListViewInlineFormularContentAfterSavePropagateGlobalEventDto
                                                                        .builder()
                                                                        .eventName(CUSTOMER_UPDATED_EVENT)
                                                                        .build()))
                                                        .build())
                                                .build(),
                                        ListViewItemInlineDockContentItemDto.builder()
                                                .id("appointments")
                                                .route("appointments")
                                                .icon("CALENDAR")
                                                .title("Termine")
                                                .content(ListViewListContentDto.builder()
                                                        .listUrl(CUSTOMER_SERVICE_URL + "appointments/list")
                                                        .sourceUrl(CUSTOMER_SERVICE_URL + CUSTOMER_ID_PARAM
                                                                + "/appointments/query")
                                                        .build())
                                                .build(),
                                        ListViewItemInlineDockContentItemDto.builder()
                                                .id("documents")
                                                .title("Dokumente")
                                                .route("documents")
                                                .icon("DOCUMENT")
                                                .content(ListViewListContentDto.builder()
                                                        .listUrl("services/files/files/list?customerId="
                                                                + CUSTOMER_ID_PARAM
                                                                + "&contextEndpointUrl=" + CUSTOMER_SERVICE_URL
                                                                + CUSTOMER_ID_PARAM)
                                                        .sourceUrl("services/files/files/query?customerId="
                                                                + CUSTOMER_ID_PARAM)
                                                        .build())
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
                                                                        .chartUrl(CUSTOMER_SERVICE_URL
                                                                                + CUSTOMER_ID_PARAM
                                                                                + "/statistics/yearlyvisits")
                                                                        .build(),
                                                                ListViewChartContentDto.builder()
                                                                        .chartUrl(CUSTOMER_SERVICE_URL
                                                                                + CUSTOMER_ID_PARAM
                                                                                + "/statistics/yearlyrevenue")
                                                                        .build()))
                                                        .build())
                                                .build()))
                                .build())
                        .build())
                .build());

        listViewFactory.addGlobalItemStyling(ListViewConditionalItemValueStylingDto.builder()
                .compareValuePath(KokuCustomerDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveStyling(ListViewItemStylingDto.builder()
                        .lineThrough(true)
                        .opacity((short) 50)
                        .build())
                .build());
        listViewFactory.addItemAction(ListViewConditionalItemValueActionDto.builder()
                .compareValuePath(KokuCustomerDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("ARROW_LEFT_START_ON_RECTANGLE")
                        .url(CUSTOMER_SERVICE_URL + CUSTOMER_ID_PARAM + "/restore")
                        .params(Arrays.asList(ListViewCallHttpListValueActionParamDto.builder()
                                .param(CUSTOMER_ID_PARAM)
                                .valueReference(idSourcePathFieldRef)
                                .build()))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.PUT)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline(CUSTOMER_LABEL + "wiederherstellen")
                                .content(NAME_PARAM + " wiederherstellen?")
                                .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .valueReference(fullNameWithOnFirstNameBasisFieldRef)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text(NAME_PARAM + " wurde erfolgreich wiederhergestellt")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                                .param(NAME_PARAM)
                                                .valueReference(fullNameWithOnFirstNameBasisFieldRef)
                                                .build()))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuCustomerDto.Fields.id)
                                        .valueMapping(Map.of(KokuCustomerDto.Fields.deleted, deletedSourceRef))
                                        .build()))
                        .failEvents(Arrays.asList(ListViewNotificationEvent.builder()
                                .text(NAME_PARAM + " konnte nicht wiederhergestellt werden")
                                .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .valueReference(fullNameWithOnFirstNameBasisFieldRef)
                                        .build()))
                                .build()))
                        .build())
                .negativeAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("TRASH")
                        .url(CUSTOMER_SERVICE_URL + CUSTOMER_ID_PARAM)
                        .params(Arrays.asList(ListViewCallHttpListValueActionParamDto.builder()
                                .param(CUSTOMER_ID_PARAM)
                                .valueReference(idSourcePathFieldRef)
                                .build()))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.DELETE)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline(CUSTOMER_LABEL + "löschen")
                                .content(NAME_PARAM + " als gelöscht markieren?")
                                .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .valueReference(fullNameWithOnFirstNameBasisFieldRef)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text(NAME_PARAM + " erfolgreich als gelöscht markiert")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                                .param(NAME_PARAM)
                                                .valueReference(fullNameWithOnFirstNameBasisFieldRef)
                                                .build()))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuCustomerDto.Fields.id)
                                        .valueMapping(Map.of(KokuCustomerDto.Fields.deleted, deletedSourceRef))
                                        .build()))
                        .failEvents(Arrays.asList(ListViewNotificationEvent.builder()
                                .text(NAME_PARAM + " konnte nicht als gelöscht markiert werden")
                                .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .valueReference(fullNameWithOnFirstNameBasisFieldRef)
                                        .build()))
                                .build()))
                        .build())
                .build());

        listViewFactory.setItemPreview(ListViewItemPreviewTextDto.builder()
                .valuePath(KokuCustomerDto.Fields.initials)
                .build());

        return listViewFactory.create();
    }

    @PostMapping("/customers/query")
    public ListPage findAll(@RequestBody(required = false) final ListQuery predicate) {
        final QCustomer qClazz = QCustomer.customer;

        final ListQueryFactory<Customer> listQueryFactory =
                new ListQueryFactory<>(this.entityManager, qClazz, qClazz.id, predicate);

        listQueryFactory.setDefaultOrder(qClazz.firstname.asc());

        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.id, qClazz.id);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.deleted, qClazz.deleted);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.firstName, qClazz.firstname);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.lastName, qClazz.lastname);
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.fullName,
                qClazz.firstname.concat(" ").concat(qClazz.lastname).trim());
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.fullNameWithOnFirstNameBasis,
                qClazz.firstname
                        .concat(" ")
                        .concat(qClazz.lastname)
                        .concat(" ")
                        .concat(new CaseBuilder()
                                .when(qClazz.onFirstnameBasis.eq(Boolean.TRUE))
                                .then("*")
                                .otherwise(""))
                        .trim());
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.initials,
                qClazz.firstname.substring(0, 1).concat(qClazz.lastname.substring(0, 1)));
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.email, qClazz.email);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.address, qClazz.address);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.postalCode, qClazz.postalCode);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.city, qClazz.city);
        listQueryFactory.addFetchExpr(
                KokuCustomerDto.Fields.addressLine2,
                stringTemplate(
                        "cast(concat_ws(' ', NULLIF(TRIM({0}), ''), NULLIF(TRIM({1}), '')) as text)",
                        qClazz.postalCode, qClazz.city));
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.privateTelephoneNo, qClazz.privateTelephoneNo);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.businessTelephoneNo, qClazz.businessTelephoneNo);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.mobileTelephoneNo, qClazz.mobileTelephoneNo);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.medicalTolerance, qClazz.medicalTolerance);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.additionalInfo, qClazz.additionalInfo);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.birthday, qClazz.birthday);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.onFirstnameBasis, qClazz.onFirstnameBasis);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.hayFever, qClazz.hayFever);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.plasterAllergy, qClazz.plasterAllergy);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.cyanoacrylateAllergy, qClazz.cyanoacrylateAllergy);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.asthma, qClazz.asthma);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.dryEyes, qClazz.dryEyes);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.circulationProblems, qClazz.circulationProblems);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.epilepsy, qClazz.epilepsy);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.diabetes, qClazz.diabetes);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.claustrophobia, qClazz.claustrophobia);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.neurodermatitis, qClazz.neurodermatitis);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.contacts, qClazz.contacts);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.glasses, qClazz.glasses);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.eyeDisease, qClazz.eyeDisease);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.allergy, qClazz.allergy);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.covid19vaccinated, qClazz.covid19vaccinated);
        listQueryFactory.addFetchExpr(KokuCustomerDto.Fields.covid19boostered, qClazz.covid19boostered);

        return listQueryFactory.create();
    }

    @GetMapping(value = "/customers/{id}")
    public KokuCustomerDto read(@PathVariable("id") Long id) {
        final Customer customer = this.customerRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return transformer.transformToDto(customer);
    }

    @GetMapping(value = "/customers/{id}/summary")
    public KokuCustomerSummaryDto readSummary(@PathVariable("id") Long id) {
        final Customer customer = this.customerRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return new CustomerToCustomerSummaryDtoTransformer().transformToDto(customer);
    }

    @PutMapping(value = "/customers/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuCustomerDto update(
            @PathVariable("id") Long id,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate,
            @RequestBody KokuCustomerDto updatedDto) {
        final Customer customer = this.entityManager.getReference(Customer.class, id);
        if (!Boolean.TRUE.equals(forceUpdate) && !customer.getVersion().equals(updatedDto.getVersion())) {
            throw new KokuBusinessExceptionWithConfirmationMessage(KokuBusinessErrorWithConfirmationMessageDto.builder()
                    .headline("Konflikt")
                    .confirmationMessage("Der Kunde wurde zwischenzeitlich bearbeitet.\n"
                            + "Willst Du die Speicherung dennoch vornehmen?")
                    .headerButton(KokuBusinessExceptionCloseButtonDto.builder()
                            .text("Abbrechen")
                            .title("Abbruch")
                            .icon("CLOSE")
                            .build())
                    .closeOnClickOutside(true)
                    .button(KokuBusinessExceptionSendToDifferentEndpointButtonDto.builder()
                            .text("Trotzdem speichern")
                            .title("Zwischenzeitliche Änderungen überschreiben")
                            .endpointUrl(String.format(CUSTOMER_SERVICE_URL + "%s?forceUpdate=%s", id, Boolean.TRUE))
                            .build())
                    .button(KokuBusinessExceptionCloseButtonDto.builder()
                            .text("Abbrechen")
                            .title("Abbruch")
                            .build())
                    .build());
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

    private static void addIllnessSection(FormViewFactory formFactory, final String rootId) {
        final String container1Id = formFactory.addContent(
                FieldsetContainer.builder().title("Erkrankungen").build());
        formFactory.place(container1Id).in(rootId).outlet(FormOutlet.CONTENT);
        final String container2Id =
                formFactory.addContent(GridContainer.builder().cols(1).xl(3).build());
        formFactory.place(container2Id).in(container1Id).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.contacts)
                        .label("Kontaktlinsen")
                        .build()))
                .in(container2Id)
                .outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.glasses)
                        .label("Brillenträger")
                        .build()))
                .in(container2Id)
                .outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.dryEyes)
                        .label("Trockene Augen")
                        .build()))
                .in(container2Id)
                .outlet(FormOutlet.CONTENT);

        final String container3Id =
                formFactory.addContent(GridContainer.builder().cols(1).build());
        formFactory.place(container3Id).in(container1Id).outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(TextareaFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.eyeDisease)
                        .label("Andere Augenerkrankungen")
                        .build()))
                .in(container3Id)
                .outlet(FormOutlet.CONTENT);

        final String container4Id =
                formFactory.addContent(GridContainer.builder().cols(1).xl(3).build());
        formFactory.place(container4Id).in(container1Id).outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.asthma)
                        .label("Asthma")
                        .build()))
                .in(container4Id)
                .outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.circulationProblems)
                        .label("Kreislaufprobleme")
                        .build()))
                .in(container4Id)
                .outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.epilepsy)
                        .label("Epilepsie")
                        .build()))
                .in(container4Id)
                .outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.diabetes)
                        .label("Diabetes")
                        .build()))
                .in(container4Id)
                .outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.claustrophobia)
                        .label("Klaustrophobie")
                        .build()))
                .in(container4Id)
                .outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.neurodermatitis)
                        .label("Neurodermitis")
                        .build()))
                .in(container4Id)
                .outlet(FormOutlet.CONTENT);
    }

    private static void addAllergySection(FormViewFactory formFactory, final String rootId) {
        final String container5Id = formFactory.addContent(
                FieldsetContainer.builder().title("Allergien").build());
        formFactory.place(container5Id).in(rootId).outlet(FormOutlet.CONTENT);
        final String container6Id =
                formFactory.addContent(GridContainer.builder().cols(1).xl(3).build());
        formFactory.place(container6Id).in(container5Id).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.hayFever)
                        .label("Heuschnupfen")
                        .build()))
                .in(container6Id)
                .outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.plasterAllergy)
                        .label("Allergie gegen Pflaster")
                        .build()))
                .in(container6Id)
                .outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.cyanoacrylateAllergy)
                        .label("Allergie gegen Cyanacrylat")
                        .build()))
                .in(container6Id)
                .outlet(FormOutlet.CONTENT);

        final String container7Id =
                formFactory.addContent(GridContainer.builder().cols(1).build());
        formFactory.place(container7Id).in(container5Id).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(TextareaFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.allergy)
                        .label("Andere Allergien")
                        .build()))
                .in(container7Id)
                .outlet(FormOutlet.CONTENT);
    }

    private static void addHealthSection(FormViewFactory formFactory, final String rootId) {
        final String container8Id = formFactory.addContent(
                FieldsetContainer.builder().title("Gesundheit").build());
        formFactory.place(container8Id).in(rootId).outlet(FormOutlet.CONTENT);
        final String container9Id =
                formFactory.addContent(GridContainer.builder().cols(1).xl(3).build());
        formFactory.place(container9Id).in(container8Id).outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.covid19vaccinated)
                        .label("Covid 19 geimpft")
                        .build()))
                .in(container9Id)
                .outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.covid19boostered)
                        .label("Covid 19 geboostert")
                        .build()))
                .in(container9Id)
                .outlet(FormOutlet.CONTENT);
        final String container10Id =
                formFactory.addContent(GridContainer.builder().cols(1).build());
        formFactory.place(container10Id).in(container8Id).outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(TextareaFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.medicalTolerance)
                        .label("Medizinische Informationen")
                        .build()))
                .in(container10Id)
                .outlet(FormOutlet.CONTENT);
    }

    private static void addPhoneSection(FormViewFactory formFactory, final String rootId) {
        final String container11Id = formFactory.addContent(
                FieldsetContainer.builder().title("Erreichbarkeit").build());
        formFactory.place(container11Id).in(rootId).outlet(FormOutlet.CONTENT);
        final String container12Id =
                formFactory.addContent(GridContainer.builder().cols(1).xl(3).build());
        formFactory.place(container12Id).in(container11Id).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(InputFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.privateTelephoneNo)
                        .label("Private Telefonnummer")
                        .type(EnumInputFormularFieldType.TEL)
                        .build()))
                .in(container12Id)
                .outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(InputFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.mobileTelephoneNo)
                        .label("Mobile Telefonnummer")
                        .type(EnumInputFormularFieldType.TEL)
                        .build()))
                .in(container12Id)
                .outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(InputFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.businessTelephoneNo)
                        .label("Geschäftliche Telefonnummer")
                        .type(EnumInputFormularFieldType.TEL)
                        .build()))
                .in(container12Id)
                .outlet(FormOutlet.CONTENT);
    }

    private static void addLivingSection(FormViewFactory formFactory, final String rootId) {
        final String container13Id = formFactory.addContent(
                FieldsetContainer.builder().title("Wohnsituation").build());
        formFactory.place(container13Id).in(rootId).outlet(FormOutlet.CONTENT);
        final String container14Id =
                formFactory.addContent(GridContainer.builder().cols(1).build());
        formFactory.place(container14Id).in(container13Id).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(InputFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.address)
                        .label("Adresse")
                        .build()))
                .in(container14Id)
                .outlet(FormOutlet.CONTENT);

        final String container15Id =
                formFactory.addContent(GridContainer.builder().cols(2).build());
        formFactory.place(container15Id).in(container13Id).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(InputFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.postalCode)
                        .label("Postleitzahl")
                        .build()))
                .in(container15Id)
                .outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(InputFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.city)
                        .label("Wohnort")
                        .build()))
                .in(container15Id)
                .outlet(FormOutlet.CONTENT);
    }

    private static void addMainSection(FormViewFactory formFactory, final String rootId) {
        final String container16Id =
                formFactory.addContent(GridContainer.builder().cols(1).build());
        formFactory.place(container16Id).in(rootId).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(CheckboxFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.onFirstnameBasis)
                        .label("Duzen")
                        .build()))
                .in(container16Id)
                .outlet(FormOutlet.CONTENT);
        final String container17Id =
                formFactory.addContent(GridContainer.builder().cols(1).xl(2).build());
        formFactory.place(container17Id).in(rootId).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(InputFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.firstName)
                        .label("Vorname")
                        .required(true)
                        .build()))
                .in(container17Id)
                .outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(InputFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.lastName)
                        .label("Nachname")
                        .required(true)
                        .build()))
                .in(container17Id)
                .outlet(FormOutlet.CONTENT);
        final String container18Id =
                formFactory.addContent(GridContainer.builder().cols(1).build());
        formFactory.place(container18Id).in(rootId).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(InputFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.email)
                        .label("Email")
                        .type(EnumInputFormularFieldType.EMAIL)
                        .build()))
                .in(container18Id)
                .outlet(FormOutlet.CONTENT);
        final String container19Id =
                formFactory.addContent(GridContainer.builder().cols(1).build());
        formFactory.place(container19Id).in(rootId).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(DateInputFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.birthday)
                        .label("Geburtstag")
                        .build()))
                .in(container19Id)
                .outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(TextareaFormularField.builder()
                        .valuePath(KokuCustomerDto.Fields.additionalInfo)
                        .label("Zusätzliche Informationen")
                        .build()))
                .in(container19Id)
                .outlet(FormOutlet.CONTENT);
    }
}
