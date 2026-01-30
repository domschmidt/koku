package de.domschmidt.koku.product.controller;

import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.formular.dto.content.buttons.EnumButtonType;
import de.domschmidt.formular.dto.content.buttons.FormButtonReloadAction;
import de.domschmidt.formular.factory.DefaultViewContentIdGenerator;
import de.domschmidt.formular.factory.FormViewFactory;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionCloseButtonDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionSendToDifferentEndpointButtonDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionWithConfirmationMessageDto;
import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.dto.formular.buttons.ButtonDockableSettings;
import de.domschmidt.koku.dto.formular.buttons.EnumButtonStyle;
import de.domschmidt.koku.dto.formular.buttons.KokuFormButton;
import de.domschmidt.koku.dto.formular.containers.grid.GridContainer;
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
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

@RestController
@RequestMapping()
@Slf4j
@RequiredArgsConstructor
public class ProductManufacturersController {
    private final EntityManager entityManager;
    private final ProductManufacturerRepository productManufacturerRepository;
    private final ProductManufacturerKafkaService productManufacturerKafkaService;
    private final ProductManufacturerToProductManufacturerDtoTransformer transformer;

    @GetMapping("/productmanufacturers/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory(
                new DefaultViewContentIdGenerator(),
                GridContainer.builder()
                        .cols(1)
                        .build()
        );

        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuProductManufacturerDto.Fields.name)
                .label("Name")
                .required(true)
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

        return formFactory.create();
    }

    @GetMapping("/productmanufacturers/list")
    public ListViewDto getListView() {
        final ListViewFactory listViewFactory = new ListViewFactory(
                new DefaultListViewContentIdGenerator(),
                KokuProductManufacturerDto.Fields.id
        );

        final ListViewSourcePathReference deletedSourcePathRef = listViewFactory.addSourcePath(
                KokuProductManufacturerDto.Fields.deleted
        );
        final ListViewSourcePathReference idSourcePathRef = listViewFactory.addSourcePath(
                KokuProductManufacturerDto.Fields.id
        );
        final ListViewFieldReference nameFieldRef = listViewFactory.addField(
                KokuProductManufacturerDto.Fields.name,
                ListViewInputFieldDto.builder()
                        .label("Name")
                        .build()
        );

        listViewFactory.addFilter(
                KokuProductManufacturerDto.Fields.deleted,
                ListViewToggleFilterDto.builder()
                        .label("Gelöschte anzeigen?")
                        .enabledPredicate(QueryPredicate.builder()
                                .searchExpression(Boolean.TRUE.toString())
                                .searchOperator(EnumSearchOperator.EQ)
                                .build()
                        )
                        .disabledPredicate(QueryPredicate.builder()
                                .searchExpression(Boolean.FALSE.toString())
                                .searchOperator(EnumSearchOperator.EQ)
                                .build())
                        .defaultState(ListViewToggleFilterDefaultStateEnum.DISABLED)
                        .build()
        );

        listViewFactory.addAction(ListViewOpenRoutedContentActionDto.builder()
                .route("new")
                .icon("PLUS")
                .build()
        );
        listViewFactory.addRoutedItem(ListViewRoutedDummyItemDto.builder()
                .route("new")
                .text("Neuer Produkthersteller")
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadAddItemGlobalEventListenerDto.builder()
                .eventName("productmanufacturer-created")
                .idPath(KokuProductManufacturerDto.Fields.id)
                .valueMapping(Map.of(
                        KokuProductManufacturerDto.Fields.name, nameFieldRef,
                        KokuProductManufacturerDto.Fields.deleted, deletedSourcePathRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route("new")
                        .inlineContent(ListViewHeaderContentDto.builder()
                                .title("Neuer Produkthersteller")
                                .content(ListViewFormularContentDto.builder()
                                        .formularUrl("services/products/productmanufacturers/form")
                                        .submitUrl("services/products/productmanufacturers")
                                        .submitMethod(ListViewFormularActionSubmitMethodEnumDto.POST)
                                        .maxWidthInPx(800)
                                        .onSaveEvents(Arrays.asList(
                                                ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                        .eventName("productmanufacturer-created")
                                                        .build(),
                                                ListViewOpenRoutedInlineFormularContentSaveEventDto.builder()
                                                        .route(":productManufacturerId")
                                                        .params(Arrays.asList(
                                                                ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto.builder()
                                                                        .param(":productManufacturerId")
                                                                        .valuePath(KokuProductManufacturerDto.Fields.id)
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
                .route(":productManufacturerId")
                .params(Arrays.asList(
                        ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                                .param(":productManufacturerId")
                                .valueReference(idSourcePathRef)
                                .build()
                ))
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadItemUpdateGlobalEventListenerDto.builder()
                .eventName("productmanufacturer-updated")
                .idPath(KokuProductManufacturerDto.Fields.id)
                .valueMapping(Map.of(
                        KokuProductManufacturerDto.Fields.name, nameFieldRef,
                        KokuProductManufacturerDto.Fields.deleted, deletedSourcePathRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route(":productManufacturerId")
                        .itemId(":productManufacturerId")
                        .inlineContent(
                                ListViewHeaderContentDto.builder()
                                        .sourceUrl("services/products/productmanufacturers/:productManufacturerId/summary")
                                        .titlePath(KokuProductManufacturerSummaryDto.Fields.summary)
                                        .globalEventListeners(Arrays.asList(ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto.builder()
                                                .eventName("productmanufacturer-updated")
                                                .idPath(KokuProductManufacturerDto.Fields.id)
                                                .titleValuePath(KokuProductManufacturerDto.Fields.name)
                                                .build()
                                        ))
                                        .content(ListViewFormularContentDto.builder()
                                                .formularUrl("services/products/productmanufacturers/form")
                                                .sourceUrl("services/products/productmanufacturers/:productManufacturerId")
                                                .submitMethod(ListViewFormularActionSubmitMethodEnumDto.PUT)
                                                .maxWidthInPx(800)
                                                .onSaveEvents(Arrays.asList(
                                                        ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                                .eventName("productmanufacturer-updated")
                                                                .build()
                                                ))
                                                .build())
                                        .build()
                        )
                        .build()
        );
        listViewFactory.addGlobalItemStyling(ListViewConditionalItemValueStylingDto.builder()
                .compareValuePath(KokuProductManufacturerDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveStyling(ListViewItemStylingDto.builder()
                        .lineThrough(true)
                        .opacity((short) 50)
                        .build()
                )
                .build()
        );
        listViewFactory.addItemAction(ListViewConditionalItemValueActionDto.builder()
                .compareValuePath(KokuProductManufacturerDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("ARROW_LEFT_START_ON_RECTANGLE")
                        .url("services/products/productmanufacturers/:productManufacturerId/restore")
                        .params(Arrays.asList(
                                ListViewCallHttpListValueActionParamDto.builder()
                                        .param(":productManufacturerId")
                                        .valueReference(idSourcePathRef)
                                        .build()
                        ))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.PUT)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Produkthersteller wiederherstellen")
                                .content("Produkthersteller :name wiederherstellen?")
                                .params(Arrays.asList(
                                        ListViewUserConfirmationValueParamDto.builder()
                                                .param(":name")
                                                .valueReference(nameFieldRef)
                                                .build()
                                ))
                                .build()
                        )
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Produkthersteller :name wurde erfolgreich wiederhergestellt")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(nameFieldRef)
                                                        .build()
                                        ))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuProductManufacturerDto.Fields.id)
                                        .valueMapping(Map.of(
                                                KokuProductManufacturerDto.Fields.deleted, deletedSourcePathRef
                                        ))
                                        .build()
                        ))
                        .failEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Produkthersteller :name konnte nicht wiederhergestellt werden")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(nameFieldRef)
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build())
                .negativeAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("TRASH")
                        .url("services/products/productmanufacturers/:productManufacturerId")
                        .params(Arrays.asList(
                                ListViewCallHttpListValueActionParamDto.builder()
                                        .param(":productManufacturerId")
                                        .valueReference(idSourcePathRef)
                                        .build()
                        ))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.DELETE)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Produkthersteller löschen")
                                .content("Produkthersteller :name als gelöscht markieren?")
                                .params(Arrays.asList(
                                        ListViewUserConfirmationValueParamDto.builder()
                                                .param(":name")
                                                .valueReference(nameFieldRef)
                                                .build()
                                ))
                                .build()
                        )
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Produkthersteller :name wurde erfolgreich als gelöscht markiert")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(nameFieldRef)
                                                        .build()
                                        ))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuProductManufacturerDto.Fields.id)
                                        .valueMapping(Map.of(
                                                KokuProductManufacturerDto.Fields.deleted, deletedSourcePathRef
                                        ))
                                        .build()
                        ))
                        .failEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Produkthersteller :name konnte nicht als gelöscht markiert werden")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(nameFieldRef)
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

    @PostMapping("/productmanufacturers/query")
    public ListPage findAll(
            @RequestBody(required = false) final ListQuery predicate
    ) {
        final QProductManufacturer qClazz = QProductManufacturer.productManufacturer;
        final ListQueryFactory<ProductManufacturer> listQueryFactory = new ListQueryFactory<>(
                this.entityManager,
                qClazz,
                qClazz.id,
                predicate
        );

        listQueryFactory.setDefaultOrder(qClazz.name.asc());

        listQueryFactory.addFetchExpr(
                KokuProductManufacturerDto.Fields.id,
                qClazz.id
        );
        listQueryFactory.addFetchExpr(
                KokuProductManufacturerDto.Fields.deleted,
                qClazz.deleted
        );
        listQueryFactory.addFetchExpr(
                KokuProductManufacturerDto.Fields.name,
                qClazz.name
        );

        return listQueryFactory.create();
    }

    @GetMapping(value = "/productmanufacturers/{productManufacturerId}")
    public KokuProductManufacturerDto read(@PathVariable("productManufacturerId") Long productManufacturerId) {
        final ProductManufacturer productManufacturer = this.productManufacturerRepository.findById(productManufacturerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product Manufacturer not found"));
        return this.transformer.transformToDto(productManufacturer);
    }

    @GetMapping(value = "/productmanufacturers/{productManufacturerId}/summary")
    public KokuProductManufacturerSummaryDto readSummary(@PathVariable("productManufacturerId") Long productManufacturerId) {
        final ProductManufacturer productManufacturer = this.productManufacturerRepository.findById(productManufacturerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product Manufacturer not found"));
        return new ProductManufacturerToProductManufacturerSummaryDtoTransformer().transformToDto(productManufacturer);
    }

    @PutMapping(value = "/productmanufacturers/{productManufacturerId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuProductManufacturerDto update(
            @PathVariable("productManufacturerId") Long productManufacturerId,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate,
            @RequestBody KokuProductManufacturerDto updatedDto
    ) {
        final ProductManufacturer productManufacturer = this.entityManager.getReference(ProductManufacturer.class, productManufacturerId);
        if (!Boolean.TRUE.equals(forceUpdate) && !productManufacturer.getVersion().equals(updatedDto.getVersion())) {
            throw new KokuBusinessExceptionWithConfirmationMessage(
                    KokuBusinessExceptionWithConfirmationMessageDto.builder()
                            .headline("Konflikt")
                            .confirmationMessage("Der Produkthersteller wurde zwischenzeitlich bearbeitet.\nWillst Du die Speicherung dennoch vornehmen?")
                            .headerButton(KokuBusinessExceptionCloseButtonDto.builder()
                                    .text("Abbrechen")
                                    .title("Abbruch")
                                    .icon("CLOSE")
                                    .build()
                            )
                            .closeOnClickOutside(true)
                            .button(KokuBusinessExceptionSendToDifferentEndpointButtonDto.builder()
                                    .text("Trotzdem speichern")
                                    .title("Zwischenzeitliche Änderungen überschreiben")
                                    .endpointUrl(String.format("services/products/productmanufacturers/%s?forceUpdate=%s", productManufacturerId, Boolean.TRUE))
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
        this.transformer.transformToEntity(productManufacturer, updatedDto);
        this.entityManager.flush();
        sendProductManufacturerUpdate(productManufacturer);
        return this.transformer.transformToDto(productManufacturer);
    }

    @DeleteMapping(value = "/productmanufacturers/{productManufacturerId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuProductManufacturerDto delete(@PathVariable("productManufacturerId") Long productManufacturerId) {
        final ProductManufacturer productManufacturer = this.entityManager.getReference(ProductManufacturer.class, productManufacturerId);
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
        final ProductManufacturer productManufacturer = this.entityManager.getReference(ProductManufacturer.class, productManufacturerId);
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
        final ProductManufacturer newProductManufacturer = this.transformer.transformToEntity(new ProductManufacturer(), newDto);
        final ProductManufacturer savedProductManufacturer = this.productManufacturerRepository.saveAndFlush(newProductManufacturer);
        sendProductManufacturerUpdate(savedProductManufacturer);
        return this.transformer.transformToDto(savedProductManufacturer);
    }

    public void sendProductManufacturerUpdate(final ProductManufacturer productManufacturer) {
        try {
            this.productManufacturerKafkaService.sendProductManufacturer(productManufacturer);
        } catch (final ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Unable to export to kafka, due to: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to export to kafka");
        }
    }
}
