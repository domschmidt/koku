package de.domschmidt.koku.activity.controller;

import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.formular.dto.content.buttons.EnumButtonType;
import de.domschmidt.formular.dto.content.buttons.FormButtonReloadAction;
import de.domschmidt.formular.factory.DefaultViewContentIdGenerator;
import de.domschmidt.formular.factory.FormViewFactory;
import de.domschmidt.koku.activity.kafka.activity.service.ActivityStepKafkaService;
import de.domschmidt.koku.activity.persistence.ActivityStep;
import de.domschmidt.koku.activity.persistence.ActivityStepRepository;
import de.domschmidt.koku.activity.persistence.QActivityStep;
import de.domschmidt.koku.activity.transformer.ActivityStepToActivityStepDtoTransformer;
import de.domschmidt.koku.activity.transformer.ActivityStepToActivityStepSummaryDtoTransformer;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionCloseButtonDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionSendToDifferentEndpointButtonDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionWithConfirmationMessageDto;
import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.dto.activity.KokuActivityStepDto;
import de.domschmidt.koku.dto.activity.KokuActivityStepSummaryDto;
import de.domschmidt.koku.dto.formular.buttons.ButtonDockableSettings;
import de.domschmidt.koku.dto.formular.buttons.EnumButtonStyle;
import de.domschmidt.koku.dto.formular.buttons.KokuFormButton;
import de.domschmidt.koku.dto.formular.containers.grid.GridContainer;
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
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
import de.domschmidt.listquery.dto.request.ListQuery;
import de.domschmidt.listquery.dto.response.ListPage;
import de.domschmidt.listquery.factory.ListQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ActivityStepController {
    private final EntityManager entityManager;
    private final ActivityStepRepository activityStepRepository;
    private final ActivityStepKafkaService activityStepKafkaService;
    private final ActivityStepToActivityStepDtoTransformer transformer;

    @Autowired
    public ActivityStepController(
            final EntityManager entityManager,
            final ActivityStepRepository activityStepRepository,
            final ActivityStepToActivityStepDtoTransformer transformer,
            final ActivityStepKafkaService activityStepKafkaService
    ) {
        this.entityManager = entityManager;
        this.activityStepRepository = activityStepRepository;
        this.transformer = transformer;
        this.activityStepKafkaService = activityStepKafkaService;
    }

    @GetMapping("/activitysteps/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory(
                new DefaultViewContentIdGenerator(),
                GridContainer.builder()
                        .cols(1)
                        .build()
        );

        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuActivityStepDto.Fields.name)
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


    @GetMapping("/activitysteps/list")
    public ListViewDto getListView() {
        final ListViewFactory listViewFactory = new ListViewFactory(
                new DefaultListViewContentIdGenerator(),
                KokuActivityStepDto.Fields.id
        );

        final ListViewSourcePathReference idSourcePathRef = listViewFactory.addSourcePath(
                KokuActivityStepDto.Fields.id
        );
        final ListViewSourcePathReference deletedSourcePathRef = listViewFactory.addSourcePath(
                KokuActivityStepDto.Fields.deleted
        );
        final ListViewFieldReference nameFieldRef = listViewFactory.addField(
                KokuActivityStepDto.Fields.name,
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
                .text("Neuer Behandlungsschritt")
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadAddItemGlobalEventListenerDto.builder()
                .eventName("activitystep-created")
                .idPath(KokuActivityStepDto.Fields.id)
                .valueMapping(Map.of(
                        KokuActivityStepDto.Fields.name, nameFieldRef,
                        KokuActivityStepDto.Fields.deleted, deletedSourcePathRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route("new")
                        .inlineContent(ListViewHeaderContentDto.builder()
                                .title("Neuer Behandlungsschritt")
                                .content(ListViewFormularContentDto.builder()
                                        .formularUrl("services/activities/activitysteps/form")
                                        .submitUrl("services/activities/activitysteps")
                                        .submitMethod(ListViewFormularActionSubmitMethodEnumDto.POST)
                                        .maxWidthInPx(800)
                                        .onSaveEvents(Arrays.asList(
                                                ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                        .eventName("activitystep-created")
                                                        .build(),
                                                ListViewOpenRoutedInlineFormularContentSaveEventDto.builder()
                                                        .route(":activityStepId")
                                                        .params(Arrays.asList(
                                                                ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto.builder()
                                                                        .param(":activityStepId")
                                                                        .valuePath(KokuActivityStepDto.Fields.id)
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
                .route(":activityStepId")
                .params(Arrays.asList(
                        ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                                .param(":activityStepId")
                                .valueReference(idSourcePathRef)
                                .build()
                ))
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadItemUpdateGlobalEventListenerDto.builder()
                .eventName("activitystep-updated")
                .idPath(KokuActivityStepDto.Fields.id)
                .valueMapping(Map.of(
                        KokuActivityStepDto.Fields.name, nameFieldRef,
                        KokuActivityStepDto.Fields.deleted, deletedSourcePathRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route(":activityStepId")
                        .itemId(":activityStepId")
                        .inlineContent(
                                ListViewHeaderContentDto.builder()
                                        .sourceUrl("services/activities/activitysteps/:activityStepId/summary")
                                        .titlePath(KokuActivityStepSummaryDto.Fields.summary)
                                        .globalEventListeners(Arrays.asList(ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto.builder()
                                                .eventName("activitystep-updated")
                                                .idPath(KokuActivityStepDto.Fields.id)
                                                .titleValuePath(KokuActivityStepDto.Fields.name)
                                                .build()
                                        ))
                                        .content(ListViewFormularContentDto.builder()
                                                .formularUrl("services/activities/activitysteps/form")
                                                .sourceUrl("services/activities/activitysteps/:activityStepId")
                                                .submitMethod(ListViewFormularActionSubmitMethodEnumDto.PUT)
                                                .maxWidthInPx(800)
                                                .onSaveEvents(Arrays.asList(
                                                        ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                                .eventName("activitystep-updated")
                                                                .build()
                                                ))
                                                .build())
                                        .build()
                        )
                        .build()
        );
        listViewFactory.addGlobalItemStyling(ListViewConditionalItemValueStylingDto.builder()
                .compareValuePath(KokuActivityStepDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveStyling(ListViewItemStylingDto.builder()
                        .lineThrough(true)
                        .opacity((short) 50)
                        .build()
                )
                .build()
        );
        listViewFactory.addItemAction(ListViewConditionalItemValueActionDto.builder()
                .compareValuePath(KokuActivityStepDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("ARROW_LEFT_START_ON_RECTANGLE")
                        .url("services/activities/activitysteps/:activityStepId/restore")
                        .params(Arrays.asList(
                                ListViewCallHttpListValueActionParamDto.builder()
                                        .param(":activityStepId")
                                        .valueReference(idSourcePathRef)
                                        .build()
                        ))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.PUT)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Behandlungsschritt wiederherstellen")
                                .content("Behandlungsschritt :name wiederherstellen?")
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
                                        .text("Behandlungsschritt :name wurde erfolgreich wiederhergestellt")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(nameFieldRef)
                                                        .build()
                                        ))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuActivityStepDto.Fields.id)
                                        .valueMapping(Map.of(
                                                KokuActivityStepDto.Fields.deleted, deletedSourcePathRef
                                        ))
                                        .build()
                        ))
                        .failEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Behandlungsschritt :name konnte nicht wiederhergestellt werden")
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
                        .url("services/activities/activitysteps/:activityStepId")
                        .params(Arrays.asList(
                                ListViewCallHttpListValueActionParamDto.builder()
                                        .param(":activityStepId")
                                        .valueReference(idSourcePathRef)
                                        .build()
                        ))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.DELETE)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Behandlungsschritt löschen")
                                .content("Behandlungsschritt :name als gelöscht markieren?")
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
                                        .text("Behandlungsschritt :name wurde erfolgreich als gelöscht markiert")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(nameFieldRef)
                                                        .build()
                                        ))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuActivityStepDto.Fields.id)
                                        .valueMapping(Map.of(
                                                KokuActivityStepDto.Fields.deleted, deletedSourcePathRef
                                        ))
                                        .build()
                        ))
                        .failEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Behandlungsschritt :name konnte nicht als gelöscht markiert werden")
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


    @PostMapping("/activitysteps/query")
    public ListPage findAll(
            @RequestBody(required = false) final ListQuery predicate
    ) {
        final QActivityStep qClazz = QActivityStep.activityStep;
        final ListQueryFactory<ActivityStep> listQueryFactory = new ListQueryFactory<>(
                this.entityManager,
                qClazz,
                qClazz.id,
                predicate
        );

        listQueryFactory.setDefaultOrder(qClazz.name.asc());

        listQueryFactory.addFetchExpr(
                KokuActivityStepDto.Fields.id,
                qClazz.id
        );
        listQueryFactory.addFetchExpr(
                KokuActivityStepDto.Fields.deleted,
                qClazz.deleted
        );
        listQueryFactory.addFetchExpr(
                KokuActivityStepDto.Fields.name,
                qClazz.name
        );

        return listQueryFactory.create();
    }

    @GetMapping(value = "/activitysteps/{activityStepId}")
    public KokuActivityStepDto read(@PathVariable("activityStepId") Long activityStepId) {
        final ActivityStep activityStep = this.activityStepRepository.findById(activityStepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ActivityStep not found"));
        return this.transformer.transformToDto(activityStep);
    }

    @GetMapping(value = "/activitysteps/{activityStepId}/summary")
    public KokuActivityStepSummaryDto readSummary(@PathVariable("activityStepId") Long activityStepId) {
        final ActivityStep activityStep = this.activityStepRepository.findById(activityStepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ActivityStep not found"));
        return new ActivityStepToActivityStepSummaryDtoTransformer().transformToDto(activityStep);
    }

    @PutMapping(value = "/activitysteps/{activityStepId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuActivityStepDto update(
            @PathVariable("activityStepId") Long activityStepId,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate,
            @RequestBody KokuActivityStepDto updatedDto
    ) {
        final ActivityStep activityStep = this.entityManager.getReference(ActivityStep.class, activityStepId);
        if (!Boolean.TRUE.equals(forceUpdate) && !activityStep.getVersion().equals(updatedDto.getVersion())) {
            throw new KokuBusinessExceptionWithConfirmationMessage(
                    KokuBusinessExceptionWithConfirmationMessageDto.builder()
                            .headline("Konflikt")
                            .confirmationMessage("der Behandlungsschritt wurde zwischenzeitlich bearbeitet.\nWillst Du die Speicherung dennoch vornehmen?")
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
                                    .endpointUrl(String.format("services/activities/activitysteps/%s?forceUpdate=%s", activityStepId, Boolean.TRUE))
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
        this.transformer.transformToEntity(activityStep, updatedDto);
        this.entityManager.flush();
        sendActivityStepUpdate(activityStep);
        return this.transformer.transformToDto(activityStep);
    }

    @DeleteMapping(value = "/activitysteps/{activityStepId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuActivityStepDto delete(@PathVariable("activityStepId") Long activityStepId) {
        final ActivityStep activityStep = this.entityManager.getReference(ActivityStep.class, activityStepId);
        if (activityStep.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ActivityStep is not deletable");
        }
        activityStep.setDeleted(true);
        this.entityManager.flush();
        sendActivityStepUpdate(activityStep);
        return this.transformer.transformToDto(activityStep);
    }

    @PutMapping(value = "/activitysteps/{activityStepId}/restore")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuActivityStepDto restore(@PathVariable("activityStepId") Long activityStepId) {
        final ActivityStep activityStep = this.entityManager.getReference(ActivityStep.class, activityStepId);
        if (!activityStep.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ActivityStep is not restorable");
        }
        activityStep.setDeleted(false);
        this.entityManager.flush();
        sendActivityStepUpdate(activityStep);
        return this.transformer.transformToDto(activityStep);
    }

    @PostMapping("/activitysteps")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public KokuActivityStepDto create(@RequestBody KokuActivityStepDto newDto) {
        final ActivityStep newActivityStep = this.transformer.transformToEntity(new ActivityStep(), newDto);
        final ActivityStep savedActivityStep = this.activityStepRepository.saveAndFlush(newActivityStep);
        sendActivityStepUpdate(savedActivityStep);
        return this.transformer.transformToDto(savedActivityStep);
    }

    public void sendActivityStepUpdate(final ActivityStep activityStep) {
        try {
            this.activityStepKafkaService.sendActivityStep(activityStep);
        } catch (final ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Unable to export to kafka, due to: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to export to kafka");
        }
    }
}
