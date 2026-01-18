package de.domschmidt.koku.activity.controller;

import de.domschmidt.chart.dto.response.axes.AxesDto;
import de.domschmidt.chart.dto.response.axis.CategoricalXAxisDto;
import de.domschmidt.chart.dto.response.types.LineChartDto;
import de.domschmidt.chart.dto.response.values.NumericSeriesDto;
import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.formular.dto.content.buttons.EnumButtonType;
import de.domschmidt.formular.dto.content.buttons.FormButtonReloadAction;
import de.domschmidt.formular.factory.DefaultViewContentIdGenerator;
import de.domschmidt.formular.factory.FormViewFactory;
import de.domschmidt.koku.activity.kafka.activity.service.ActivityKafkaService;
import de.domschmidt.koku.activity.persistence.Activity;
import de.domschmidt.koku.activity.persistence.ActivityPriceHistoryEntry;
import de.domschmidt.koku.activity.persistence.ActivityRepository;
import de.domschmidt.koku.activity.persistence.QActivity;
import de.domschmidt.koku.activity.transformer.ActivityToActivityDtoTransformer;
import de.domschmidt.koku.activity.transformer.ActivityToActivitySummaryDtoTransformer;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionCloseButtonDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionSendToDifferentEndpointButtonDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionWithConfirmationMessageDto;
import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.dto.activity.KokuActivityDto;
import de.domschmidt.koku.dto.activity.KokuActivitySummaryDto;
import de.domschmidt.koku.dto.formular.buttons.ButtonDockableSettings;
import de.domschmidt.koku.dto.formular.buttons.EnumButtonStyle;
import de.domschmidt.koku.dto.formular.buttons.KokuFormButton;
import de.domschmidt.koku.dto.formular.containers.grid.GridContainer;
import de.domschmidt.koku.dto.formular.fields.input.EnumInputFormularFieldType;
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
import de.domschmidt.koku.dto.list.fields.input.ListViewInputFieldDto;
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

@RestController
@RequestMapping()
@Slf4j
@RequiredArgsConstructor
public class ActivityController {
    private final EntityManager entityManager;
    private final ActivityRepository activityRepository;
    private final ActivityKafkaService activityKafkaService;
    private final ActivityToActivityDtoTransformer transformer;

    @GetMapping("/activities/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory(
                new DefaultViewContentIdGenerator(),
                GridContainer.builder()
                        .cols(1)
                        .build()
        );

        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuActivityDto.Fields.name)
                .label("Name")
                .required(true)
                .build()
        );

        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuActivityDto.Fields.approximatelyDuration)
                .type(EnumInputFormularFieldType.TIME)
                .label("Ungefähre Behandlungsdauer")
                .required(true)
                .build()
        );

        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuActivityDto.Fields.price)
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

    @GetMapping("/activities/list")
    public ListViewDto getListView(
            @RequestBody(required = false) final ListQuery predicate
    ) {
        final ListViewFactory listViewFactory = new ListViewFactory(
                new DefaultListViewContentIdGenerator(),
                KokuActivityDto.Fields.id
        );

        final ListViewSourcePathReference deletedSourcePathRef = listViewFactory.addSourcePath(
                KokuActivityDto.Fields.deleted
        );
        final ListViewSourcePathReference idSourcePathRef = listViewFactory.addSourcePath(
                KokuActivityDto.Fields.id
        );
        final ListViewFieldReference nameFieldRef = listViewFactory.addField(
                KokuActivityDto.Fields.name,
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
                .text("Neue Tätigkeit")
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadAddItemGlobalEventListenerDto.builder()
                .eventName("activity-created")
                .idPath(KokuActivityDto.Fields.id)
                .valueMapping(Map.of(
                        KokuActivityDto.Fields.name, nameFieldRef,
                        KokuActivityDto.Fields.deleted, deletedSourcePathRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route("new")
                        .inlineContent(ListViewHeaderContentDto.builder()
                                .title("Neue Tätigkeit")
                                .content(ListViewFormularContentDto.builder()
                                        .formularUrl("services/activities/activities/form")
                                        .submitUrl("services/activities/activities")
                                        .submitMethod(ListViewFormularActionSubmitMethodEnumDto.POST)
                                        .maxWidthInPx(800)
                                        .onSaveEvents(Arrays.asList(
                                                ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                        .eventName("activity-created")
                                                        .build(),
                                                ListViewOpenRoutedInlineFormularContentSaveEventDto.builder()
                                                        .route(":activityId/information")
                                                        .params(Arrays.asList(
                                                                ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto.builder()
                                                                        .param(":activityId")
                                                                        .valuePath(KokuActivityDto.Fields.id)
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
                .route(":activityId/information")
                .params(Arrays.asList(
                        ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                                .param(":activityId")
                                .valueReference(idSourcePathRef)
                                .build()
                ))
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadItemUpdateGlobalEventListenerDto.builder()
                .eventName("activity-updated")
                .idPath(KokuActivityDto.Fields.id)
                .valueMapping(Map.of(
                        KokuActivityDto.Fields.name, nameFieldRef,
                        KokuActivityDto.Fields.deleted, deletedSourcePathRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route(":activityId")
                        .itemId(":activityId")
                        .inlineContent(ListViewHeaderContentDto.builder()
                                .sourceUrl("services/activities/activities/:activityId/summary")
                                .titlePath(KokuActivitySummaryDto.Fields.summary)
                                .globalEventListeners(Arrays.asList(ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto.builder()
                                        .eventName("activity-updated")
                                        .idPath(KokuActivityDto.Fields.id)
                                        .titleValuePath(KokuActivityDto.Fields.name)
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
                                                                .content(ListViewFormularContentDto.builder()
                                                                        .formularUrl("services/activities/activities/form")
                                                                        .sourceUrl("services/activities/activities/:activityId")
                                                                        .submitMethod(ListViewFormularActionSubmitMethodEnumDto.PUT)
                                                                        .maxWidthInPx(800)
                                                                        .onSaveEvents(Arrays.asList(
                                                                                ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                                                        .eventName("activity-updated")
                                                                                        .build()
                                                                        ))
                                                                        .build()
                                                                ).build(),

                                                        ListViewItemInlineDockContentItemDto.builder()
                                                                .id("pricehistory")
                                                                .title("Preishistorie")
                                                                .route("pricehistory")
                                                                .icon("CHART_BAR")
                                                                .content(ListViewGridContentDto.builder()
                                                                        .cols(1)
                                                                        .content(Arrays.asList(
                                                                                ListViewChartContentDto.builder()
                                                                                        .chartUrl("services/activities/activities/:activityId/statistics/pricehistory")
                                                                                        .build()
                                                                        ))
                                                                        .build()
                                                                )
                                                                .build()
                                                )).build()

                                )
                                .build()
                        )
                        .build()
        );


        listViewFactory.addGlobalItemStyling(ListViewConditionalItemValueStylingDto.builder()
                .compareValuePath(KokuActivityDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveStyling(ListViewItemStylingDto.builder()
                        .lineThrough(true)
                        .opacity((short) 50)
                        .build()
                )
                .build()
        );
        listViewFactory.addItemAction(ListViewConditionalItemValueActionDto.builder()
                .compareValuePath(KokuActivityDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("ARROW_LEFT_START_ON_RECTANGLE")
                        .url("services/activities/activities/:activityId/restore")
                        .params(Arrays.asList(
                                ListViewCallHttpListValueActionParamDto.builder()
                                        .param(":activityId")
                                        .valueReference(idSourcePathRef)
                                        .build()
                        ))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.PUT)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Tätigkeit wiederherstellen")
                                .content("Tätigkeit :name wiederherstellen?")
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
                                        .text("Tätigkeit :name wurde erfolgreich wiederhergestellt")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(nameFieldRef)
                                                        .build()
                                        ))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuActivityDto.Fields.id)
                                        .valueMapping(Map.of(
                                                KokuActivityDto.Fields.deleted, deletedSourcePathRef
                                        ))
                                        .build()
                        ))
                        .failEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Tätigkeit :name konnte nicht wiederhergestellt werden")
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
                        .url("services/activities/activities/:activityId")
                        .params(Arrays.asList(
                                ListViewCallHttpListValueActionParamDto.builder()
                                        .param(":activityId")
                                        .valueReference(idSourcePathRef)
                                        .build()
                        ))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.DELETE)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Tätigkeit löschen")
                                .content("Tätigkeit :name als gelöscht markieren?")
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
                                        .text("Tätigkeit :name wurde erfolgreich als gelöscht markiert")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":name")
                                                        .valueReference(nameFieldRef)
                                                        .build()
                                        ))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuActivityDto.Fields.id)
                                        .valueMapping(Map.of(
                                                KokuActivityDto.Fields.deleted, deletedSourcePathRef
                                        ))
                                        .build()
                        ))
                        .failEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Tätigkeit :name konnte nicht als gelöscht markiert werden")
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

    @PostMapping("/activities/query")
    public ListPage findAll(
            @RequestBody(required = false) final ListQuery predicate
    ) {
        final QActivity qClazz = QActivity.activity;
        final ListQueryFactory<Activity> listQueryFactory = new ListQueryFactory<>(
                this.entityManager,
                qClazz,
                qClazz.id,
                predicate
        );

        listQueryFactory.setDefaultOrder(qClazz.name.asc());


        listQueryFactory.addFetchExpr(
                KokuActivityDto.Fields.id,
                qClazz.id
        );

        listQueryFactory.addFetchExpr(
                KokuActivityDto.Fields.deleted,
                qClazz.deleted
        );
        listQueryFactory.addFetchExpr(
                KokuActivityDto.Fields.name,
                qClazz.name
        );
        listQueryFactory.addFetchExpr(
                KokuActivityDto.Fields.approximatelyDuration,
                qClazz.approximatelyDuration
        );

        return listQueryFactory.create();
    }

    @GetMapping(value = "/activities/{activityId}/statistics/pricehistory")
    public LineChartDto readPriceHistory(@PathVariable("activityId") Long activityId) {
        final Activity activity = this.activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "dd.MM.yyyy HH:mm 'Uhr'",
                Locale.GERMAN
        );

        return LineChartDto.builder()
                .title("Preishistorie")
                .series(List.of(
                        NumericSeriesDto.builder()
                                .name("Preis")
                                .data(activity.getPriceHistory().stream().map(ActivityPriceHistoryEntry::getPrice).toList())
                                .build()
                ))
                .axes(AxesDto.builder()
                        .x(CategoricalXAxisDto.builder()
                                .categories(activity.getPriceHistory().stream().map(activityPriceHistoryEntry -> {
                                    return formatter.format(activityPriceHistoryEntry.getRecorded());
                                }).toList())
                                .build()
                        )
                        .build()
                )
                .build();
    }

    @GetMapping(value = "/activities/{activityId}")
    public KokuActivityDto read(@PathVariable("activityId") Long activityId) {
        final Activity activity = this.activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        return this.transformer.transformToDto(activity);
    }

    @GetMapping(value = "/activities/{activityId}/summary")
    public KokuActivitySummaryDto readSummary(@PathVariable("activityId") Long activityId) {
        final Activity activity = this.activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        return new ActivityToActivitySummaryDtoTransformer().transformToDto(activity);
    }

    @PutMapping(value = "/activities/{activityId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuActivityDto update(
            @PathVariable("activityId") Long activityId,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate,
            @RequestBody KokuActivityDto updatedDto
    ) {
        final Activity activity = this.entityManager.getReference(Activity.class, activityId);
        if (!Boolean.TRUE.equals(forceUpdate) && !activity.getVersion().equals(updatedDto.getVersion())) {
            throw new KokuBusinessExceptionWithConfirmationMessage(
                    KokuBusinessExceptionWithConfirmationMessageDto.builder()
                            .headline("Konflikt")
                            .confirmationMessage("die Tätigkeit wurde zwischenzeitlich bearbeitet.\nWillst Du die Speicherung dennoch vornehmen?")
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
                                    .endpointUrl(String.format("services/activities/activities/%s?forceUpdate=%s", activityId, Boolean.TRUE))
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
        this.transformer.transformToEntity(activity, updatedDto);
        this.entityManager.flush();
        sendActivityUpdate(activity);
        return this.transformer.transformToDto(activity);
    }

    @DeleteMapping(value = "/activities/{activityId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuActivityDto delete(@PathVariable("activityId") Long activityId) {
        final Activity activity = this.entityManager.getReference(Activity.class, activityId);
        if (activity.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activity is not deletable");
        }
        activity.setDeleted(true);
        this.entityManager.flush();
        sendActivityUpdate(activity);
        return this.transformer.transformToDto(activity);
    }

    @PutMapping(value = "/activities/{activityId}/restore")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuActivityDto restore(@PathVariable("activityId") Long activityId) {
        final Activity activity = this.entityManager.getReference(Activity.class, activityId);
        if (!activity.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activity is not restorable");
        }
        activity.setDeleted(false);
        this.entityManager.flush();
        sendActivityUpdate(activity);
        return this.transformer.transformToDto(activity);
    }

    @PostMapping("/activities")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public KokuActivityDto create(@RequestBody KokuActivityDto newDto) {
        final Activity newActivity = this.transformer.transformToEntity(new Activity(), newDto);
        final Activity savedActivity = this.activityRepository.saveAndFlush(newActivity);
        sendActivityUpdate(savedActivity);
        return this.transformer.transformToDto(savedActivity);
    }

    public void sendActivityUpdate(final Activity activity) {
        try {
            this.activityKafkaService.sendActivity(activity);
        } catch (final ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Unable to export to kafka, due to: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to export to kafka");
        }
    }
}
