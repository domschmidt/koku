package de.domschmidt.koku.product.controller;

import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.formular.dto.content.buttons.EnumButtonType;
import de.domschmidt.formular.factory.FormOutlet;
import de.domschmidt.formular.factory.FormViewFactory;
import de.domschmidt.koku.business_exception.dto.KokuBusinessErrorWithConfirmationMessageDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionCloseButtonDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionSendToDifferentEndpointButtonDto;
import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.dto.formular.buttons.ButtonDockableSettings;
import de.domschmidt.koku.dto.formular.buttons.EnumButtonStyle;
import de.domschmidt.koku.dto.formular.buttons.FormButtonUserConfirmationSourcePathParamDto;
import de.domschmidt.koku.dto.formular.buttons.KokuFormButton;
import de.domschmidt.koku.dto.formular.containers.conditional.ConditionalContainer;
import de.domschmidt.koku.dto.formular.containers.grid.GridContainer;
import de.domschmidt.koku.dto.formular.events.FormNotificationEvent;
import de.domschmidt.koku.dto.formular.events.FormNotificationEventSerenityEnumDto;
import de.domschmidt.koku.dto.formular.events.FormNotificationEventValueParamDto;
import de.domschmidt.koku.dto.formular.events.FormPropagateGlobalEventDto;
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
import de.domschmidt.koku.dto.formular.listeners.FormViewEventPayloadSourceUpdateGlobalEventListenerDto;
import de.domschmidt.koku.dto.formular.user_confirmation.FormUserConfirmationDto;
import de.domschmidt.koku.dto.list.fields.input.ListViewInputFieldDto;
import de.domschmidt.koku.dto.list.filters.ListViewToggleFilterDefaultStateEnum;
import de.domschmidt.koku.dto.list.filters.ListViewToggleFilterDto;
import de.domschmidt.koku.dto.list.items.style.ListViewConditionalItemValueStylingDto;
import de.domschmidt.koku.dto.list.items.style.ListViewItemStylingDto;
import de.domschmidt.koku.dto.product.KokuProductManufacturerDto;
import de.domschmidt.koku.dto.product.KokuProductManufacturerSummaryDto;
import de.domschmidt.koku.product.kafka.product.service.ProductManufacturerKafkaService;
import de.domschmidt.koku.product.persistence.ProductManufacturer;
import de.domschmidt.koku.product.persistence.ProductManufacturerRepository;
import de.domschmidt.koku.product.persistence.QProductManufacturer;
import de.domschmidt.koku.product.transformer.ProductManufacturerToProductManufacturerDtoTransformer;
import de.domschmidt.koku.product.transformer.ProductManufacturerToProductManufacturerSummaryDtoTransformer;
import de.domschmidt.list.dto.response.ListViewDto;
import de.domschmidt.list.dto.response.ListViewSourcePathReference;
import de.domschmidt.list.dto.response.actions.ListViewOpenRoutedContentActionDto;
import de.domschmidt.list.dto.response.actions.ListViewUserConfirmationDto;
import de.domschmidt.list.dto.response.actions.ListViewUserConfirmationValueParamDto;
import de.domschmidt.list.dto.response.events.ListViewEventPayloadAddItemGlobalEventListenerDto;
import de.domschmidt.list.dto.response.events.ListViewEventPayloadItemUpdateGlobalEventListenerDto;
import de.domschmidt.list.dto.response.fields.ListViewFieldReference;
import de.domschmidt.list.dto.response.inline_content.ListViewRoutedContentDto;
import de.domschmidt.list.dto.response.inline_content.formular.ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto;
import de.domschmidt.list.dto.response.inline_content.formular.ListViewFormularContentDto;
import de.domschmidt.list.dto.response.inline_content.formular.ListViewInlineFormularContentAfterSavePropagateGlobalEventDto;
import de.domschmidt.list.dto.response.inline_content.formular.ListViewOpenRoutedInlineFormularContentSaveEventDto;
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

@RestController
@RequestMapping()
@Slf4j
@RequiredArgsConstructor
public class ProductManufacturersController {
    private static final String PRODUCT_MANUFACTURER_CREATED_EVENT = "productmanufacturer-created";
    private static final String PRODUCT_MANUFACTURER_UPDATED_EVENT = "productmanufacturer-updated";
    private static final String PRODUCT_MANUFACTURER_ID_PARAM = ":productManufacturerId";
    private static final String NAME_PARAM = ":name";
    private static final String PRODUCT_MANUFACTURER_LABEL = "Produkthersteller ";
    private static final String PRODUCT_MANUFACTURER_SERVICE_URL = "services/products/productmanufacturers/";
    private final EntityManager entityManager;
    private final ProductManufacturerRepository productManufacturerRepository;
    private final ProductManufacturerKafkaService productManufacturerKafkaService;
    private final ProductManufacturerToProductManufacturerDtoTransformer transformer;

    @GetMapping("/productmanufacturers/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory();
        final String rootId =
                formFactory.addContent(GridContainer.builder().cols(1).build());

        formFactory
                .place(formFactory.addContent(InputFormularField.builder()
                        .valuePath(KokuProductManufacturerDto.Fields.name)
                        .label("Name")
                        .required(true)
                        .build()))
                .in(rootId)
                .outlet(FormOutlet.CONTENT);

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
                .compareValuePath(KokuProductManufacturerDto.Fields.deleted)
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
                        .submitPayload(KokuProductManufacturerDto.builder()
                                .deleted(true)
                                .build())
                        .userConfirmation(FormUserConfirmationDto.builder()
                                .headline(PRODUCT_MANUFACTURER_LABEL + "löschen")
                                .content(PRODUCT_MANUFACTURER_LABEL + NAME_PARAM + " als gelöscht markieren?")
                                .params(Arrays.asList(FormButtonUserConfirmationSourcePathParamDto.builder()
                                        .param(NAME_PARAM)
                                        .sourcePath(KokuProductManufacturerDto.Fields.name)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                FormNotificationEvent.builder()
                                        .text(PRODUCT_MANUFACTURER_LABEL + NAME_PARAM
                                                + " erfolgreich als gelöscht markiert")
                                        .serenity(FormNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(FormNotificationEventValueParamDto.builder()
                                                .param(NAME_PARAM)
                                                .sourcePath(KokuProductManufacturerDto.Fields.name)
                                                .build()))
                                        .build(),
                                FormPropagateGlobalEventDto.builder()
                                        .eventName(PRODUCT_MANUFACTURER_UPDATED_EVENT)
                                        .build()))
                        .failEvents(Arrays.asList(FormNotificationEvent.builder()
                                .text(PRODUCT_MANUFACTURER_LABEL + NAME_PARAM
                                        + " konnte nicht als gelöscht markiert werden")
                                .serenity(FormNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(FormNotificationEventValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .sourcePath(KokuProductManufacturerDto.Fields.name)
                                        .build()))
                                .build()))
                        .build()))
                .in(deleteContainerId)
                .outlet(FormOutlet.CONTENT);

        final String restoreContainerId = formFactory.addContent(ConditionalContainer.builder()
                .compareValuePath(KokuProductManufacturerDto.Fields.deleted)
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
                        .submitPayload(KokuProductManufacturerDto.builder()
                                .deleted(false)
                                .build())
                        .userConfirmation(FormUserConfirmationDto.builder()
                                .headline(PRODUCT_MANUFACTURER_LABEL + "wiederherstellen")
                                .content(PRODUCT_MANUFACTURER_LABEL + NAME_PARAM + " wiederherstellen?")
                                .params(Arrays.asList(FormButtonUserConfirmationSourcePathParamDto.builder()
                                        .param(NAME_PARAM)
                                        .sourcePath(KokuProductManufacturerDto.Fields.name)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                FormNotificationEvent.builder()
                                        .text(PRODUCT_MANUFACTURER_LABEL + NAME_PARAM
                                                + " wurde erfolgreich wiederhergestellt")
                                        .serenity(FormNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(FormNotificationEventValueParamDto.builder()
                                                .param(NAME_PARAM)
                                                .sourcePath(KokuProductManufacturerDto.Fields.name)
                                                .build()))
                                        .build(),
                                FormPropagateGlobalEventDto.builder()
                                        .eventName(PRODUCT_MANUFACTURER_UPDATED_EVENT)
                                        .build()))
                        .failEvents(Arrays.asList(FormNotificationEvent.builder()
                                .text(PRODUCT_MANUFACTURER_LABEL + NAME_PARAM
                                        + " konnte nicht wiederhergestellt werden")
                                .serenity(FormNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(FormNotificationEventValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .sourcePath(KokuProductManufacturerDto.Fields.name)
                                        .build()))
                                .build()))
                        .build()))
                .in(restoreContainerId)
                .outlet(FormOutlet.CONTENT);

        formFactory.addGlobalEventListener(FormViewEventPayloadSourceUpdateGlobalEventListenerDto.builder()
                .eventName(PRODUCT_MANUFACTURER_UPDATED_EVENT)
                .idPath(KokuProductManufacturerDto.Fields.id)
                .build());

        return formFactory.create(rootId);
    }

    @GetMapping("/productmanufacturers/list")
    public ListViewDto getListView() {
        final ListViewFactory listViewFactory =
                new ListViewFactory(new DefaultListViewContentIdGenerator(), KokuProductManufacturerDto.Fields.id);

        final ListViewSourcePathReference deletedSourcePathRef =
                listViewFactory.addSourcePath(KokuProductManufacturerDto.Fields.deleted);
        final ListViewSourcePathReference idSourcePathRef =
                listViewFactory.addSourcePath(KokuProductManufacturerDto.Fields.id);
        final ListViewFieldReference nameFieldRef = listViewFactory.addField(
                KokuProductManufacturerDto.Fields.name,
                ListViewInputFieldDto.builder().label("Name").build());

        listViewFactory.addFilter(
                KokuProductManufacturerDto.Fields.deleted,
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
                .text("Neuer Produkthersteller")
                .build());
        listViewFactory.addGlobalEventListener(ListViewEventPayloadAddItemGlobalEventListenerDto.builder()
                .eventName(PRODUCT_MANUFACTURER_CREATED_EVENT)
                .idPath(KokuProductManufacturerDto.Fields.id)
                .valueMapping(Map.of(
                        KokuProductManufacturerDto.Fields.name, nameFieldRef,
                        KokuProductManufacturerDto.Fields.deleted, deletedSourcePathRef))
                .build());
        listViewFactory.addRoutedContent(ListViewRoutedContentDto.builder()
                .route("new")
                .inlineContent(ListViewHeaderContentDto.builder()
                        .title("Neuer Produkthersteller")
                        .content(ListViewFormularContentDto.builder()
                                .formularUrl(PRODUCT_MANUFACTURER_SERVICE_URL + "form")
                                .submitUrl("services/products/productmanufacturers")
                                .submitMethod(ListViewFormularActionSubmitMethodEnumDto.POST)
                                .maxWidthInPx(800)
                                .onSaveEvents(Arrays.asList(
                                        ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                .eventName(PRODUCT_MANUFACTURER_CREATED_EVENT)
                                                .build(),
                                        ListViewOpenRoutedInlineFormularContentSaveEventDto.builder()
                                                .route(PRODUCT_MANUFACTURER_ID_PARAM)
                                                .params(Arrays.asList(
                                                        ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto
                                                                .builder()
                                                                .param(PRODUCT_MANUFACTURER_ID_PARAM)
                                                                .valuePath(KokuProductManufacturerDto.Fields.id)
                                                                .build()))
                                                .build()))
                                .build())
                        .build())
                .build());

        listViewFactory.setItemClickAction(ListViewItemClickOpenRoutedContentActionDto.builder()
                .route(PRODUCT_MANUFACTURER_ID_PARAM)
                .params(Arrays.asList(ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                        .param(PRODUCT_MANUFACTURER_ID_PARAM)
                        .valueReference(idSourcePathRef)
                        .build()))
                .build());
        listViewFactory.addGlobalEventListener(ListViewEventPayloadItemUpdateGlobalEventListenerDto.builder()
                .eventName(PRODUCT_MANUFACTURER_UPDATED_EVENT)
                .idPath(KokuProductManufacturerDto.Fields.id)
                .valueMapping(Map.of(
                        KokuProductManufacturerDto.Fields.name, nameFieldRef,
                        KokuProductManufacturerDto.Fields.deleted, deletedSourcePathRef))
                .build());
        listViewFactory.addRoutedContent(ListViewRoutedContentDto.builder()
                .route(PRODUCT_MANUFACTURER_ID_PARAM)
                .itemId(PRODUCT_MANUFACTURER_ID_PARAM)
                .inlineContent(ListViewHeaderContentDto.builder()
                        .sourceUrl(PRODUCT_MANUFACTURER_SERVICE_URL + PRODUCT_MANUFACTURER_ID_PARAM + "/summary")
                        .titlePath(KokuProductManufacturerSummaryDto.Fields.summary)
                        .globalEventListeners(
                                Arrays.asList(ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto.builder()
                                        .eventName(PRODUCT_MANUFACTURER_UPDATED_EVENT)
                                        .idPath(KokuProductManufacturerDto.Fields.id)
                                        .titleValuePath(KokuProductManufacturerDto.Fields.name)
                                        .build()))
                        .content(ListViewFormularContentDto.builder()
                                .formularUrl(PRODUCT_MANUFACTURER_SERVICE_URL + "form")
                                .sourceUrl(PRODUCT_MANUFACTURER_SERVICE_URL + PRODUCT_MANUFACTURER_ID_PARAM)
                                .submitMethod(ListViewFormularActionSubmitMethodEnumDto.PUT)
                                .maxWidthInPx(800)
                                .onSaveEvents(Arrays.asList(
                                        ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                .eventName(PRODUCT_MANUFACTURER_UPDATED_EVENT)
                                                .build()))
                                .build())
                        .build())
                .build());
        listViewFactory.addGlobalItemStyling(ListViewConditionalItemValueStylingDto.builder()
                .compareValuePath(KokuProductManufacturerDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveStyling(ListViewItemStylingDto.builder()
                        .lineThrough(true)
                        .opacity((short) 50)
                        .build())
                .build());
        listViewFactory.addItemAction(ListViewConditionalItemValueActionDto.builder()
                .compareValuePath(KokuProductManufacturerDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("ARROW_LEFT_START_ON_RECTANGLE")
                        .url(PRODUCT_MANUFACTURER_SERVICE_URL + PRODUCT_MANUFACTURER_ID_PARAM + "/restore")
                        .params(Arrays.asList(ListViewCallHttpListValueActionParamDto.builder()
                                .param(PRODUCT_MANUFACTURER_ID_PARAM)
                                .valueReference(idSourcePathRef)
                                .build()))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.PUT)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline(PRODUCT_MANUFACTURER_LABEL + "wiederherstellen")
                                .content(PRODUCT_MANUFACTURER_LABEL + NAME_PARAM + " wiederherstellen?")
                                .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .valueReference(nameFieldRef)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text(PRODUCT_MANUFACTURER_LABEL + NAME_PARAM
                                                + " wurde erfolgreich wiederhergestellt")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                                .param(NAME_PARAM)
                                                .valueReference(nameFieldRef)
                                                .build()))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuProductManufacturerDto.Fields.id)
                                        .valueMapping(
                                                Map.of(KokuProductManufacturerDto.Fields.deleted, deletedSourcePathRef))
                                        .build()))
                        .failEvents(Arrays.asList(ListViewNotificationEvent.builder()
                                .text(PRODUCT_MANUFACTURER_LABEL + NAME_PARAM
                                        + " konnte nicht wiederhergestellt werden")
                                .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .valueReference(nameFieldRef)
                                        .build()))
                                .build()))
                        .build())
                .negativeAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("TRASH")
                        .url(PRODUCT_MANUFACTURER_SERVICE_URL + PRODUCT_MANUFACTURER_ID_PARAM)
                        .params(Arrays.asList(ListViewCallHttpListValueActionParamDto.builder()
                                .param(PRODUCT_MANUFACTURER_ID_PARAM)
                                .valueReference(idSourcePathRef)
                                .build()))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.DELETE)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline(PRODUCT_MANUFACTURER_LABEL + "löschen")
                                .content(PRODUCT_MANUFACTURER_LABEL + NAME_PARAM + " als gelöscht markieren?")
                                .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .valueReference(nameFieldRef)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text(PRODUCT_MANUFACTURER_LABEL + NAME_PARAM
                                                + " wurde erfolgreich als gelöscht markiert")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                                .param(NAME_PARAM)
                                                .valueReference(nameFieldRef)
                                                .build()))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuProductManufacturerDto.Fields.id)
                                        .valueMapping(
                                                Map.of(KokuProductManufacturerDto.Fields.deleted, deletedSourcePathRef))
                                        .build()))
                        .failEvents(Arrays.asList(ListViewNotificationEvent.builder()
                                .text(PRODUCT_MANUFACTURER_LABEL + NAME_PARAM
                                        + " konnte nicht als gelöscht markiert werden")
                                .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .valueReference(nameFieldRef)
                                        .build()))
                                .build()))
                        .build())
                .build());

        return listViewFactory.create();
    }

    @PostMapping("/productmanufacturers/query")
    public ListPage findAll(@RequestBody(required = false) final ListQuery predicate) {
        final QProductManufacturer qClazz = QProductManufacturer.productManufacturer;
        final ListQueryFactory<ProductManufacturer> listQueryFactory =
                new ListQueryFactory<>(this.entityManager, qClazz, qClazz.id, predicate);

        listQueryFactory.setDefaultOrder(qClazz.name.asc());

        listQueryFactory.addFetchExpr(KokuProductManufacturerDto.Fields.id, qClazz.id);
        listQueryFactory.addFetchExpr(KokuProductManufacturerDto.Fields.deleted, qClazz.deleted);
        listQueryFactory.addFetchExpr(KokuProductManufacturerDto.Fields.name, qClazz.name);

        return listQueryFactory.create();
    }

    @GetMapping(value = "/productmanufacturers/{productManufacturerId}")
    public KokuProductManufacturerDto read(@PathVariable("productManufacturerId") Long productManufacturerId) {
        final ProductManufacturer productManufacturer = this.productManufacturerRepository
                .findById(productManufacturerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product Manufacturer not found"));
        return this.transformer.transformToDto(productManufacturer);
    }

    @GetMapping(value = "/productmanufacturers/{productManufacturerId}/summary")
    public KokuProductManufacturerSummaryDto readSummary(
            @PathVariable("productManufacturerId") Long productManufacturerId) {
        final ProductManufacturer productManufacturer = this.productManufacturerRepository
                .findById(productManufacturerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product Manufacturer not found"));
        return new ProductManufacturerToProductManufacturerSummaryDtoTransformer().transformToDto(productManufacturer);
    }

    @PutMapping(value = "/productmanufacturers/{productManufacturerId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuProductManufacturerDto update(
            @PathVariable("productManufacturerId") Long productManufacturerId,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate,
            @RequestBody KokuProductManufacturerDto updatedDto) {
        final ProductManufacturer productManufacturer =
                this.entityManager.getReference(ProductManufacturer.class, productManufacturerId);
        if (!Boolean.TRUE.equals(forceUpdate)
                && !productManufacturer.getVersion().equals(updatedDto.getVersion())) {
            throw new KokuBusinessExceptionWithConfirmationMessage(KokuBusinessErrorWithConfirmationMessageDto.builder()
                    .headline("Konflikt")
                    .confirmationMessage("Der Produkthersteller wurde zwischenzeitlich bearbeitet.\n"
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
                            .endpointUrl(String.format(
                                    PRODUCT_MANUFACTURER_SERVICE_URL + "%s?forceUpdate=%s",
                                    productManufacturerId,
                                    Boolean.TRUE))
                            .build())
                    .button(KokuBusinessExceptionCloseButtonDto.builder()
                            .text("Abbrechen")
                            .title("Abbruch")
                            .build())
                    .build());
        }
        this.transformer.transformToEntity(productManufacturer, updatedDto);
        this.entityManager.flush();
        sendProductManufacturerUpdate(productManufacturer);
        return this.transformer.transformToDto(productManufacturer);
    }

    @DeleteMapping(value = "/productmanufacturers/{productManufacturerId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuProductManufacturerDto delete(@PathVariable("productManufacturerId") Long productManufacturerId) {
        final ProductManufacturer productManufacturer =
                this.entityManager.getReference(ProductManufacturer.class, productManufacturerId);
        if (productManufacturer.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product Manufacturer is not deletable");
        }
        productManufacturer.setDeleted(true);
        this.entityManager.flush();
        sendProductManufacturerUpdate(productManufacturer);
        return this.transformer.transformToDto(productManufacturer);
    }

    @PutMapping(value = "/productmanufacturers/{productManufacturerId}/restore")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuProductManufacturerDto restore(@PathVariable("productManufacturerId") Long productManufacturerId) {
        final ProductManufacturer productManufacturer =
                this.entityManager.getReference(ProductManufacturer.class, productManufacturerId);
        if (!productManufacturer.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product Manufacturer is not restorable");
        }
        productManufacturer.setDeleted(false);
        this.entityManager.flush();
        sendProductManufacturerUpdate(productManufacturer);
        return this.transformer.transformToDto(productManufacturer);
    }

    @PostMapping("/productmanufacturers")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public KokuProductManufacturerDto create(@RequestBody KokuProductManufacturerDto newDto) {
        final ProductManufacturer newProductManufacturer =
                this.transformer.transformToEntity(new ProductManufacturer(), newDto);
        final ProductManufacturer savedProductManufacturer =
                this.productManufacturerRepository.saveAndFlush(newProductManufacturer);
        sendProductManufacturerUpdate(savedProductManufacturer);
        return this.transformer.transformToDto(savedProductManufacturer);
    }

    public void sendProductManufacturerUpdate(final ProductManufacturer productManufacturer) {
        try {
            this.productManufacturerKafkaService.sendProductManufacturer(productManufacturer);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Unable to export to kafka, due to: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to export to kafka");
        } catch (final ExecutionException | TimeoutException e) {
            log.error("Unable to export to kafka, due to: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to export to kafka");
        }
    }
}
