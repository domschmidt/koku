package de.domschmidt.koku.promotion.controller;

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
import de.domschmidt.koku.dto.formular.containers.fieldset.FieldsetContainer;
import de.domschmidt.koku.dto.formular.containers.grid.GridContainer;
import de.domschmidt.koku.dto.formular.fields.input.EnumInputFormularFieldType;
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
import de.domschmidt.koku.dto.list.fields.input.ListViewInputFieldDto;
import de.domschmidt.koku.dto.promotion.KokuPromotionDto;
import de.domschmidt.koku.dto.promotion.KokuPromotionSummaryDto;
import de.domschmidt.koku.promotion.kafka.promotion.service.PromotionKafkaService;
import de.domschmidt.koku.promotion.persistence.Promotion;
import de.domschmidt.koku.promotion.persistence.PromotionRepository;
import de.domschmidt.koku.promotion.persistence.QPromotion;
import de.domschmidt.koku.promotion.transformer.PromotionToPromotionDtoTransformer;
import de.domschmidt.koku.promotion.transformer.PromotionToPromotionSummaryDtoTransformer;
import de.domschmidt.list.dto.response.ListViewDto;
import de.domschmidt.list.dto.response.ListViewSourcePathReference;
import de.domschmidt.list.dto.response.actions.ListViewOpenRoutedContentActionDto;
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
import de.domschmidt.list.dto.response.items.actions.ListViewFormularActionSubmitMethodEnumDto;
import de.domschmidt.list.dto.response.items.actions.inline_content.ListViewItemClickOpenRoutedContentActionDto;
import de.domschmidt.list.dto.response.items.actions.inline_content.ListViewItemClickOpenRoutedContentActionItemValueParamDto;
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

@RestController
@RequestMapping()
@Slf4j
@RequiredArgsConstructor
public class PromotionController {
    private final EntityManager entityManager;
    private final PromotionRepository promotionRepository;
    private final PromotionKafkaService promotionKafkaService;
    private final PromotionToPromotionDtoTransformer transformer;

    @GetMapping("/promotions/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory(
                new DefaultViewContentIdGenerator(),
                GridContainer.builder()
                        .cols(1)
                        .build()
        );

        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuPromotionDto.Fields.name)
                .type(EnumInputFormularFieldType.TEXT)
                .label("Name")
                .required(true)
                .build()
        );

        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .md(2)
                .build()
        );
        formFactory.addContainer(FieldsetContainer.builder()
                .title("Tätigkeiten")
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuPromotionDto.Fields.activityAbsoluteItemSavings)
                .type(EnumInputFormularFieldType.NUMBER)
                .label("Absolute Ersparnis je Tätigkeit")
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuPromotionDto.Fields.activityAbsoluteSavings)
                .type(EnumInputFormularFieldType.NUMBER)
                .label("Absolute Ersparnis")
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuPromotionDto.Fields.activityRelativeItemSavings)
                .type(EnumInputFormularFieldType.NUMBER)
                .label("Relative Ersparnis je Tätigkeit")
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuPromotionDto.Fields.activityRelativeSavings)
                .type(EnumInputFormularFieldType.NUMBER)
                .label("Relative Ersparnis")
                .build()
        );
        formFactory.endContainer();

        formFactory.addContainer(FieldsetContainer.builder()
                .title("Produkte")
                .build()
        );

        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuPromotionDto.Fields.productAbsoluteItemSavings)
                .type(EnumInputFormularFieldType.NUMBER)
                .label("Absolute Ersparnis je Produkt")
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuPromotionDto.Fields.productAbsoluteSavings)
                .type(EnumInputFormularFieldType.NUMBER)
                .label("Absolute Ersparnis")
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuPromotionDto.Fields.productRelativeItemSavings)
                .type(EnumInputFormularFieldType.NUMBER)
                .label("Relative Ersparnis je Produkt")
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuPromotionDto.Fields.productRelativeSavings)
                .type(EnumInputFormularFieldType.NUMBER)
                .label("Relative Ersparnis")
                .build()
        );

        formFactory.endContainer();
        formFactory.endContainer();

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

    @GetMapping("/promotions/list")
    public ListViewDto getListView() {
        final ListViewFactory listViewFactory = new ListViewFactory(
                new DefaultListViewContentIdGenerator(),
                KokuPromotionDto.Fields.id
        );

        final ListViewSourcePathReference idSourcePathRef = listViewFactory.addSourcePath(
                KokuPromotionDto.Fields.id
        );
        final ListViewSourcePathReference deletedSourcePathRef = listViewFactory.addSourcePath(
                KokuPromotionDto.Fields.deleted
        );
        final ListViewFieldReference nameFieldRef = listViewFactory.addField(
                KokuPromotionDto.Fields.name,
                ListViewInputFieldDto.builder()
                        .label("Name")
                        .build()
        );
        final ListViewFieldReference shortSummaryFieldRef = listViewFactory.addField(
                KokuPromotionDto.Fields.shortSummary,
                ListViewInputFieldDto.builder()
                        .label("Name")
                        .build()
        );

        listViewFactory.addAction(ListViewOpenRoutedContentActionDto.builder()
                .route("new")
                .icon("PLUS")
                .build()
        );
        listViewFactory.addRoutedItem(ListViewRoutedDummyItemDto.builder()
                .route("new")
                .text("Neue Aktion")
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadAddItemGlobalEventListenerDto.builder()
                .eventName("promotion-created")
                .idPath(KokuPromotionDto.Fields.id)
                .valueMapping(Map.of(
                        KokuPromotionDto.Fields.name, nameFieldRef,
                        KokuPromotionDto.Fields.shortSummary, shortSummaryFieldRef,
                        KokuPromotionDto.Fields.deleted, deletedSourcePathRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route("new")
                        .inlineContent(ListViewHeaderContentDto.builder()
                                .title("Neue Aktion")
                                .content(ListViewFormularContentDto.builder()
                                        .formularUrl("services/promotions/promotions/form")
                                        .submitUrl("services/promotions/promotions")
                                        .submitMethod(ListViewFormularActionSubmitMethodEnumDto.POST)
                                        .maxWidthInPx(800)
                                        .onSaveEvents(Arrays.asList(
                                                ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                        .eventName("promotion-created")
                                                        .build(),
                                                ListViewOpenRoutedInlineFormularContentSaveEventDto.builder()
                                                        .route(":promotionId")
                                                        .params(Arrays.asList(
                                                                ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto.builder()
                                                                        .param(":promotionId")
                                                                        .valuePath(KokuPromotionDto.Fields.id)
                                                                        .build()
                                                        ))
                                                        .build()
                                        ))
                                        .build())
                                .build()
                        )
                        .build()
        );

        listViewFactory.setItemClickAction(ListViewItemClickOpenRoutedContentActionDto.builder()
                .route(":promotionId")
                .params(Arrays.asList(
                        ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                                .param(":promotionId")
                                .valueReference(idSourcePathRef)
                                .build()
                ))
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadItemUpdateGlobalEventListenerDto.builder()
                .eventName("promotion-updated")
                .idPath(KokuPromotionDto.Fields.id)
                .valueMapping(Map.of(
                        KokuPromotionDto.Fields.name, nameFieldRef,
                        KokuPromotionDto.Fields.shortSummary, shortSummaryFieldRef,
                        KokuPromotionDto.Fields.deleted, deletedSourcePathRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route(":promotionId")
                        .itemId(":promotionId")
                        .inlineContent(ListViewHeaderContentDto.builder()
                                .sourceUrl("services/promotions/promotions/:promotionId/summary")
                                .titlePath(KokuPromotionSummaryDto.Fields.summary)
                                .globalEventListeners(Arrays.asList(ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto.builder()
                                        .eventName("promotion-updated")
                                        .idPath(KokuPromotionDto.Fields.id)
                                        .titleValuePath(KokuPromotionDto.Fields.longSummary)
                                        .build()
                                ))
                                .content(ListViewFormularContentDto.builder()
                                        .formularUrl("services/promotions/promotions/form")
                                        .sourceUrl("services/promotions/promotions/:promotionId")
                                        .submitMethod(ListViewFormularActionSubmitMethodEnumDto.PUT)
                                        .maxWidthInPx(800)
                                        .onSaveEvents(Arrays.asList(
                                                ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                        .eventName("promotion-updated")
                                                        .build()
                                        ))
                                        .build()
                                ).build()
                        )
                        .build()
        );

        return listViewFactory.create();
    }

    @PostMapping("/promotions/query")
    public ListPage findAll(
            @RequestBody(required = false) final ListQuery predicate
    ) {
        final QPromotion qClazz = QPromotion.promotion;
        final ListQueryFactory<Promotion> ListQueryFactory = new ListQueryFactory<>(
                this.entityManager,
                qClazz,
                qClazz.id,
                predicate
        );

        ListQueryFactory.addFetchExpr(
                KokuPromotionDto.Fields.id,
                qClazz.id
        );
        ListQueryFactory.addFetchExpr(
                KokuPromotionDto.Fields.name,
                qClazz.name
        );

        return ListQueryFactory.create();
    }

    @GetMapping(value = "/promotions/{promotionId}")
    public KokuPromotionDto read(@PathVariable("promotionId") Long promotionId) {
        final Promotion promotion = this.promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found"));
        return this.transformer.transformToDto(promotion);
    }


    @GetMapping(value = "/promotions/{promotionId}/summary")
    public KokuPromotionSummaryDto readSummary(@PathVariable("promotionId") Long promotionId) {
        final Promotion promotion = this.promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found"));
        return new PromotionToPromotionSummaryDtoTransformer().transformToDto(promotion);
    }

    @PutMapping(value = "/promotions/{promotionId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuPromotionDto update(
            @PathVariable("promotionId") Long promotionId,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate,
            @RequestBody KokuPromotionDto updatedDto
    ) {
        final Promotion promotion = this.entityManager.getReference(Promotion.class, promotionId);
        if (!Boolean.TRUE.equals(forceUpdate) && !promotion.getVersion().equals(updatedDto.getVersion())) {
            throw new KokuBusinessExceptionWithConfirmationMessage(
                    KokuBusinessExceptionWithConfirmationMessageDto.builder()
                            .headline("Konflikt")
                            .confirmationMessage("Die Aktion wurde zwischenzeitlich bearbeitet.\nWillst Du die Speicherung dennoch vornehmen?")
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
                                    .endpointUrl(String.format("services/promotions/promotions/%s?forceUpdate=%s", promotionId, Boolean.TRUE))
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
        this.transformer.transformToEntity(promotion, updatedDto);
        this.entityManager.flush();
        sendPromotionUpdate(promotion);
        return this.transformer.transformToDto(promotion);
    }

    @DeleteMapping(value = "/promotions/{promotionId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuPromotionDto delete(@PathVariable("promotionId") Long promotionId) {
        final Promotion promotion = this.entityManager.getReference(Promotion.class, promotionId);
        if (promotion.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promotion is not deletable");
        }
        promotion.setDeleted(true);
        this.entityManager.flush();
        sendPromotionUpdate(promotion);
        return this.transformer.transformToDto(promotion);
    }

    @PutMapping(value = "/promotions/{promotionId}/restore")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuPromotionDto restore(@PathVariable("promotionId") Long promotionId) {
        final Promotion promotion = this.entityManager.getReference(Promotion.class, promotionId);
        if (!promotion.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promotion is not restorable");
        }
        promotion.setDeleted(false);
        this.entityManager.flush();
        sendPromotionUpdate(promotion);
        return this.transformer.transformToDto(promotion);
    }

    @PostMapping("/promotions")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public KokuPromotionDto create(@RequestBody KokuPromotionDto newDto) {
        final Promotion newPromotion = this.transformer.transformToEntity(new Promotion(), newDto);
        final Promotion savedPromotion = this.promotionRepository.saveAndFlush(newPromotion);
        sendPromotionUpdate(savedPromotion);
        return this.transformer.transformToDto(savedPromotion);
    }

    public void sendPromotionUpdate(final Promotion promotion) {
        try {
            this.promotionKafkaService.sendPromotion(promotion);
        } catch (final ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Unable to export to kafka, due to: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to export to kafka");
        }
    }
}
