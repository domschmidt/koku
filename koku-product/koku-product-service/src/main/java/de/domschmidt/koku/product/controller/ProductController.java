package de.domschmidt.koku.product.controller;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import de.domschmidt.chart.dto.response.axes.AxesDto;
import de.domschmidt.chart.dto.response.axis.CategoricalXAxisDto;
import de.domschmidt.chart.dto.response.types.LineChartDto;
import de.domschmidt.chart.dto.response.values.NumericSeriesDto;
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
import de.domschmidt.koku.dto.formular.buttons.ButtonDockableSettings;
import de.domschmidt.koku.dto.formular.buttons.EnumButtonStyle;
import de.domschmidt.koku.dto.formular.buttons.KokuFormButton;
import de.domschmidt.koku.dto.formular.containers.grid.GridContainer;
import de.domschmidt.koku.dto.formular.fields.input.EnumInputFormularFieldType;
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
import de.domschmidt.koku.dto.formular.fields.select.SelectFormularField;
import de.domschmidt.koku.dto.formular.fields.select.SelectFormularFieldPossibleValue;
import de.domschmidt.koku.dto.formular.fields.slots.KokuFieldSlotButton;
import de.domschmidt.koku.dto.formular.listeners.*;
import de.domschmidt.koku.dto.list.fields.input.ListViewInputFieldDto;
import de.domschmidt.koku.dto.list.fields.input.ListViewInputFieldTypeEnumDto;
import de.domschmidt.koku.dto.list.items.style.ListViewConditionalItemValueStylingDto;
import de.domschmidt.koku.dto.list.items.style.ListViewItemStylingDto;
import de.domschmidt.koku.dto.product.KokuProductDto;
import de.domschmidt.koku.dto.product.KokuProductManufacturerDto;
import de.domschmidt.koku.dto.product.KokuProductSummaryDto;
import de.domschmidt.koku.product.exceptions.ManufacturerIdNotFoundException;
import de.domschmidt.koku.product.kafka.product.service.ProductKafkaService;
import de.domschmidt.koku.product.persistence.*;
import de.domschmidt.koku.product.transformer.ProductToProductDtoTransformer;
import de.domschmidt.koku.product.transformer.ProductToProductSummaryDtoTransformer;
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

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.querydsl.core.types.dsl.Expressions.stringTemplate;

@RestController
@RequestMapping()
@Slf4j
@RequiredArgsConstructor
public class ProductController {
    private final EntityManager entityManager;
    private final ProductRepository productRepository;
    private final ProductKafkaService productKafkaService;
    private final ProductToProductDtoTransformer transformer;

    @GetMapping("/products/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory(
                new DefaultViewContentIdGenerator(),
                GridContainer.builder()
                        .cols(1)
                        .build()
        );

        final QProductManufacturer qProductManufacturer = QProductManufacturer.productManufacturer;
        final List<ProductManufacturer> productManufacturersSnapshot = new JPAQuery<>(this.entityManager)
                .select(qProductManufacturer)
                .from(qProductManufacturer)
                .fetch();
        final String productManufacturerFieldRef = formFactory.addField(SelectFormularField.builder()
                .valuePath(KokuProductDto.Fields.manufacturerId)
                .label("Hersteller")
                .required(true)
                .possibleValues(productManufacturersSnapshot.stream().map(productManufacturer -> {
                    return SelectFormularFieldPossibleValue.builder()
                            .id(productManufacturer.getId() + "")
                            .text(productManufacturer.getName())
                            .disabled(productManufacturer.isDeleted())
                            .build();
                }).toList())
                .appendOuter(KokuFieldSlotButton.builder()
                        .icon("PLUS")
                        .buttonType(EnumButtonType.BUTTON)
                        .title("Neuer Hersteller anlegen")
                        .build()
                )
                .build()
        );
        formFactory.addBusinessRule(KokuBusinessRuleDto.builder()
                .id("CreateProductManufacturer")
                .reference(KokuBusinessRuleFieldReferenceDto.builder()
                        .reference(productManufacturerFieldRef)
                        .listener(KokuBusinessRuleFieldReferenceListenerDto.builder()
                                .event(KokuBusinessRuleFieldReferenceListenerEventEnum.CLICK_APPEND_OUTER)
                                .build())
                        .build())
                .execution(
                        KokuBusinessRuleOpenDialogContentDto.builder()
                                .content(KokuBusinessRuleHeaderContentDto.builder()
                                        .title("Neuer Hersteller")
                                        .content(
                                                KokuBusinessRuleFormularContentDto.builder()
                                                        .formularUrl("services/products/productmanufacturers/form")
                                                        .submitUrl("services/products/productmanufacturers")
                                                        .submitMethod(KokuBusinessRuleFormularActionSubmitMethodEnumDto.POST)
                                                        .maxWidthInPx(800)
                                                        .onSaveEvents(Arrays.asList(
                                                                KokuBusinessRuleFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                                        .eventName("productmanufacturer-created")
                                                                        .build()
                                                        ))
                                                        .build()
                                        )
                                        .build()
                                )
                                .closeEventListener(KokuBusinessRuleOpenContentCloseGlobalEventListenerDto.builder()
                                        .eventName("productmanufacturer-created")
                                        .build()
                                )
                                .build()
                )
                .build()
        );
        formFactory.addGlobalEventListener(FormViewEventPayloadFieldUpdateGlobalEventListenerDto.builder()
                .eventName("productmanufacturer-created")
                .fieldValueMapping(Map.of(
                        productManufacturerFieldRef,
                        FormViewFieldReferenceValueMapping.builder()
                                .source(FormViewEventPayloadSourcePathFieldUpdateValueSourceDto.builder()
                                        .sourcePath(KokuProductManufacturerDto.Fields.id)
                                        .build()
                                )
                                .build()
                ))
                .configMapping(Map.of(
                        productManufacturerFieldRef,
                        FormViewFieldConfigMapping.builder()
                                .targetConfigPath(SelectFormularField.Fields.possibleValues)
                                .valueMapping(ConfigMappingAppendListDto.builder()
                                        .valueMapping(List.of(
                                                StringConversionConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuProductManufacturerDto.Fields.id)
                                                        .targetPath(SelectFormularFieldPossibleValue.Fields.id)
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuProductManufacturerDto.Fields.name)
                                                        .targetPath(SelectFormularFieldPossibleValue.Fields.text)
                                                        .build(),
                                                SourcePathConfigMappingAppendListItemDto.builder()
                                                        .sourcePath(KokuProductManufacturerDto.Fields.deleted)
                                                        .targetPath(SelectFormularFieldPossibleValue.Fields.disabled)
                                                        .build()
                                        ))
                                        .build())
                                .build()
                ))
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuProductDto.Fields.name)
                .label("Name")
                .required(true)
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuProductDto.Fields.price)
                .type(EnumInputFormularFieldType.NUMBER)
                .label("Preis")
                .regexp("^\\d{0,19}([\\.]\\d{0,2})?$")
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


    @GetMapping("/products/list")
    public ListViewDto getListView() {
        final ListViewFactory listViewFactory = new ListViewFactory(
                new DefaultListViewContentIdGenerator(),
                KokuProductDto.Fields.id
        );

        final ListViewSourcePathReference deletedSourcePathRef = listViewFactory.addSourcePath(
                KokuProductDto.Fields.deleted
        );
        final ListViewSourcePathReference idSourcePathRef = listViewFactory.addSourcePath(
                KokuProductDto.Fields.id
        );
        final ListViewFieldReference nameFieldRef = listViewFactory.addField(
                KokuProductDto.Fields.name,
                ListViewInputFieldDto.builder()
                        .label("Name")
                        .build()
        );
        final ListViewFieldReference manufacturerNameFieldRef = listViewFactory.addField(
                KokuProductDto.Fields.manufacturerName,
                ListViewInputFieldDto.builder()
                        .label("Hersteller")
                        .build()
        );
        final ListViewFieldReference priceFieldRef = listViewFactory.addField(
                KokuProductDto.Fields.price,
                ListViewInputFieldDto.builder()
                        .label("Preis")
                        .type(ListViewInputFieldTypeEnumDto.NUMBER)
                        .build()
        );

        listViewFactory.addAction(ListViewOpenRoutedContentActionDto.builder()
                .route("new")
                .icon("PLUS")
                .build()
        );
        listViewFactory.addRoutedItem(ListViewRoutedDummyItemDto.builder()
                .route("new")
                .text("Neues Produkt")
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadAddItemGlobalEventListenerDto.builder()
                .eventName("product-created")
                .idPath(KokuProductDto.Fields.id)
                .valueMapping(Map.of(
                        KokuProductDto.Fields.name, nameFieldRef,
                        KokuProductDto.Fields.manufacturerName, manufacturerNameFieldRef,
                        KokuProductDto.Fields.formattedPrice, priceFieldRef,
                        KokuProductDto.Fields.deleted, deletedSourcePathRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route("new")
                        .inlineContent(ListViewHeaderContentDto.builder()
                                .title("Neues Produkt")
                                .content(ListViewFormularContentDto.builder()
                                        .formularUrl("services/products/products/form")
                                        .submitUrl("services/products/products")
                                        .submitMethod(ListViewFormularActionSubmitMethodEnumDto.POST)
                                        .maxWidthInPx(800)
                                        .onSaveEvents(Arrays.asList(
                                                ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                        .eventName("product-created")
                                                        .build(),
                                                ListViewOpenRoutedInlineFormularContentSaveEventDto.builder()
                                                        .route(":productId/information")
                                                        .params(Arrays.asList(
                                                                ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto.builder()
                                                                        .param(":productId")
                                                                        .valuePath(KokuProductDto.Fields.id)
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
                .route(":productId/information")
                .params(Arrays.asList(
                        ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                                .param(":productId")
                                .valueReference(idSourcePathRef)
                                .build()
                ))
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadItemUpdateGlobalEventListenerDto.builder()
                .eventName("product-updated")
                .idPath(KokuProductDto.Fields.id)
                .valueMapping(Map.of(
                        KokuProductDto.Fields.deleted, deletedSourcePathRef,
                        KokuProductDto.Fields.name, nameFieldRef,
                        KokuProductDto.Fields.formattedPrice, priceFieldRef,
                        KokuProductDto.Fields.manufacturerName, manufacturerNameFieldRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route(":productId")
                        .itemId(":productId")
                        .inlineContent(
                                ListViewHeaderContentDto.builder()
                                        .sourceUrl("services/products/products/:productId/summary")
                                        .titlePath(KokuProductSummaryDto.Fields.summary)
                                        .globalEventListeners(Arrays.asList(ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto.builder()
                                                .eventName("product-updated")
                                                .idPath(KokuProductDto.Fields.id)
                                                .titleValuePath(KokuProductDto.Fields.name)
                                                .build()
                                        ))
                                        .content(
                                                ListViewDockContentDto.builder()
                                                        .content(Arrays.asList(
                                                                        ListViewItemInlineDockContentItemDto.builder()
                                                                                .id("information")
                                                                                .route("information")
                                                                                .icon("INFORMATION_CIRCLE")
                                                                                .title("Bearbeiten")
                                                                                .content(

                                                                                        ListViewFormularContentDto.builder()
                                                                                                .formularUrl("services/products/products/form")
                                                                                                .sourceUrl("services/products/products/:productId")
                                                                                                .submitMethod(ListViewFormularActionSubmitMethodEnumDto.PUT)
                                                                                                .maxWidthInPx(800)
                                                                                                .onSaveEvents(Arrays.asList(
                                                                                                        ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                                                                                .eventName("product-updated")
                                                                                                                .build()
                                                                                                ))
                                                                                                .build()
                                                                                )

                                                                                .build(),
                                                                        ListViewItemInlineDockContentItemDto.builder()
                                                                                .id("pricehistory")
                                                                                .title("Preishistorie")
                                                                                .route("pricehistory")
                                                                                .icon("CHART_BAR")
                                                                                .content(ListViewGridContentDto.builder()
                                                                                        .cols(1)
                                                                                        .content(Arrays.asList(
                                                                                                ListViewChartContentDto.builder()
                                                                                                        .chartUrl("services/products/products/:productId/statistics/pricehistory")
                                                                                                        .build()
                                                                                        ))
                                                                                        .build()
                                                                                )
                                                                                .build()

                                                                )
                                                        ).build()
                                        )
                                        .build()
                        )
                        .build()
        );
        listViewFactory.addGlobalItemStyling(ListViewConditionalItemValueStylingDto.builder()
                .compareValuePath(KokuProductDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveStyling(ListViewItemStylingDto.builder()
                        .lineThrough(true)
                        .opacity((short) 50)
                        .build()
                )
                .build()
        );
        listViewFactory.addItemAction(ListViewConditionalItemValueActionDto.builder()
                .compareValuePath(KokuProductDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("ARROW_LEFT_START_ON_RECTANGLE")
                        .url("services/products/products/:productId/restore")
                        .params(Arrays.asList(
                                ListViewCallHttpListValueActionParamDto.builder()
                                        .param(":productId")
                                        .valueReference(idSourcePathRef)
                                        .build()
                        ))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.PUT)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Produkt wiederherstellen")
                                .content("Produkt :name wiederherstellen?")
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
                                        .text("Produkt :name wurde erfolgreich wiederhergestellt")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(nameFieldRef)
                                                        .build()
                                        ))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuProductDto.Fields.id)
                                        .valueMapping(Map.of(
                                                KokuProductDto.Fields.deleted, deletedSourcePathRef
                                        ))
                                        .build()
                        ))
                        .failEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Produkt :name konnte nicht wiederhergestellt werden")
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
                        .url("services/products/products/:productId")
                        .params(Arrays.asList(
                                ListViewCallHttpListValueActionParamDto.builder()
                                        .param(":productId")
                                        .valueReference(idSourcePathRef)
                                        .build()
                        ))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.DELETE)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Produkt löschen")
                                .content("Produkt :name als gelöscht markieren?")
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
                                        .text("Produkt :name wurde erfolgreich als gelöscht markiert")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(nameFieldRef)
                                                        .build()
                                        ))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuProductDto.Fields.id)
                                        .valueMapping(Map.of(
                                                KokuProductDto.Fields.deleted, deletedSourcePathRef
                                        ))
                                        .build()
                        ))
                        .failEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Produkt :name konnte nicht als gelöscht markiert werden")
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

    @PostMapping("/products/query")
    public ListPage findAll(
            @RequestBody(required = false) final ListQuery predicate
    ) {
        final QProduct qClazz = QProduct.product;
        final ListQueryFactory<Product> listQueryFactory = new ListQueryFactory<>(
                this.entityManager,
                qClazz,
                qClazz.id,
                predicate
        );

        listQueryFactory.setDefaultOrder(qClazz.name.asc());

        listQueryFactory.addFetchExpr(
                KokuProductDto.Fields.id,
                qClazz.id
        );
        listQueryFactory.addFetchExpr(
                KokuProductDto.Fields.deleted,
                qClazz.deleted
        );
        listQueryFactory.addFetchExpr(
                KokuProductDto.Fields.name,
                qClazz.name
        );
        final QProductPriceHistoryEntry qProductPriceHistoryEntry = QProductPriceHistoryEntry.productPriceHistoryEntry;
        final QProductPriceHistoryEntry qProductPriceHistoryEntryInner = new QProductPriceHistoryEntry("priceInner");
        listQueryFactory.addFetchExpr(
                KokuProductDto.Fields.price,
                stringTemplate("to_char({0}, 'FM999G999G999G990D00 \"€\"')",
                        JPAExpressions.select(
                                        qProductPriceHistoryEntry.price)
                                .from(qProductPriceHistoryEntry)
                                .where(qProductPriceHistoryEntry.product.eq(qClazz)
                                        .and(qProductPriceHistoryEntry.recorded.eq(
                                                        JPAExpressions.select(qProductPriceHistoryEntryInner.recorded.max())
                                                                .from(qProductPriceHistoryEntryInner)
                                                                .where(qProductPriceHistoryEntryInner.product.eq(qClazz))
                                                )
                                        )
                                )
                )
        );
        listQueryFactory.addFetchExpr(
                KokuProductDto.Fields.manufacturerId,
                qClazz.manufacturer.id
        );
        listQueryFactory.addFetchExpr(
                KokuProductDto.Fields.manufacturerName,
                qClazz.manufacturer.name
        );

        return listQueryFactory.create();
    }

    @GetMapping(value = "/products/{productId}/statistics/pricehistory")
    public LineChartDto readProductPriceHistory(@PathVariable("productId") Long productId) {
        final Product product = this.productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "dd.MM.yyyy HH:mm 'Uhr'",
                Locale.GERMAN
        );

        return LineChartDto.builder()
                .title("Preishistorie")
                .series(List.of(
                        NumericSeriesDto.builder()
                                .name("Preis")
                                .data(product.getPriceHistory().stream().map(ProductPriceHistoryEntry::getPrice).toList())
                                .build()
                ))
                .axes(AxesDto.builder()
                        .x(CategoricalXAxisDto.builder()
                                .categories(product.getPriceHistory().stream().map(productPriceHistoryEntry -> {
                                    return formatter.format(productPriceHistoryEntry.getRecorded());
                                }).toList())
                                .build()
                        )
                        .build()
                )
                .build();
    }

    @GetMapping(value = "/products/{productId}")
    public KokuProductDto read(@PathVariable("productId") Long productId) {
        final Product product = this.productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return this.transformer.transformToDto(product);
    }

    @GetMapping(value = "/products/{productId}/summary")
    public KokuProductSummaryDto readSummary(@PathVariable("productId") Long productId) {
        final Product product = this.productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return new ProductToProductSummaryDtoTransformer().transformToDto(product);
    }

    @PutMapping(value = "/products/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuProductDto update(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate,
            @RequestBody KokuProductDto updatedDto
    ) throws ManufacturerIdNotFoundException {
        final Product product = this.entityManager.getReference(Product.class, productId);
        if (!Boolean.TRUE.equals(forceUpdate) && !product.getVersion().equals(updatedDto.getVersion())) {
            throw new KokuBusinessExceptionWithConfirmationMessage(
                    KokuBusinessExceptionWithConfirmationMessageDto.builder()
                            .headline("Konflikt")
                            .confirmationMessage("Das Produkt wurde zwischenzeitlich bearbeitet.\nWillst Du die Speicherung dennoch vornehmen?")
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
                                    .endpointUrl(String.format("services/products/products/%s?forceUpdate=%s", productId, Boolean.TRUE))
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
        this.transformer.transformToEntity(product, updatedDto);
        this.entityManager.flush();
        sendProductUpdate(product);
        return this.transformer.transformToDto(product);
    }

    @DeleteMapping(value = "/products/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuProductDto delete(@PathVariable("productId") Long productId) {
        final Product product = this.entityManager.getReference(Product.class, productId);
        if (product.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment is not deletable");
        }
        product.setDeleted(true);
        this.entityManager.flush();
        sendProductUpdate(product);
        return this.transformer.transformToDto(product);
    }

    @PutMapping(value = "/products/{productId}/restore")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuProductDto restore(@PathVariable("productId") Long productId) {
        final Product product = this.entityManager.getReference(Product.class, productId);
        if (!product.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment is not restorable");
        }
        product.setDeleted(false);
        this.entityManager.flush();
        sendProductUpdate(product);
        return this.transformer.transformToDto(product);
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public KokuProductDto create(@RequestBody KokuProductDto newDto) throws ManufacturerIdNotFoundException {
        final Product newProduct = this.transformer.transformToEntity(new Product(), newDto);
        final Product savedProduct = this.productRepository.saveAndFlush(newProduct);
        sendProductUpdate(savedProduct);
        return this.transformer.transformToDto(savedProduct);
    }

    public void sendProductUpdate(final Product product) {
        try {
            this.productKafkaService.sendProduct(product);
        } catch (final ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Unable to export to kafka, due to: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to export to kafka");
        }
    }
}
