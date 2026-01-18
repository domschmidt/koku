package de.domschmidt.koku.user.controller;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
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
import de.domschmidt.koku.dto.formular.fields.input.EnumInputFormularFieldType;
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
import de.domschmidt.koku.dto.formular.fields.select.SelectFormularField;
import de.domschmidt.koku.dto.formular.fields.select.SelectFormularFieldPossibleValue;
import de.domschmidt.koku.dto.formular.fields.textarea.TextareaFormularField;
import de.domschmidt.koku.dto.list.fields.input.ListViewInputFieldDto;
import de.domschmidt.koku.dto.list.fields.input.ListViewInputFieldTypeEnumDto;
import de.domschmidt.koku.dto.list.items.style.ListViewConditionalItemValueStylingDto;
import de.domschmidt.koku.dto.list.items.style.ListViewItemStylingDto;
import de.domschmidt.koku.dto.user.KokuUserAppointmentDto;
import de.domschmidt.koku.dto.user.KokuUserAppointmentSummaryDto;
import de.domschmidt.koku.user.persistence.*;
import de.domschmidt.koku.user.transformer.UserAppointmentToUserAppointmentDtoTransformer;
import de.domschmidt.koku.user.transformer.UserAppointmentToUserAppointmentSummaryDtoTransformer;
import de.domschmidt.list.dto.response.ListViewDto;
import de.domschmidt.list.dto.response.ListViewSourcePathReference;
import de.domschmidt.list.dto.response.actions.ListViewOpenRoutedContentActionDto;
import de.domschmidt.list.dto.response.actions.ListViewPropagateGlobalEventActionEventDto;
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
import de.domschmidt.listquery.dto.request.ListQuery;
import de.domschmidt.listquery.dto.response.ListPage;
import de.domschmidt.listquery.factory.ListQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping()
@Slf4j
public class UserAppointmentController {

    private final EntityManager entityManager;
    private final UserAppointmentRepository userAppointmentRepository;
    private final UserAppointmentToUserAppointmentDtoTransformer transformer;

    @Autowired
    public UserAppointmentController(
            final EntityManager entityManager,
            final UserAppointmentRepository userAppointmentRepository,
            final UserAppointmentToUserAppointmentDtoTransformer transformer
    ) {
        this.entityManager = entityManager;
        this.userAppointmentRepository = userAppointmentRepository;
        this.transformer = transformer;
    }

    @GetMapping("/users/appointments/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory(
                new DefaultViewContentIdGenerator(),
                GridContainer.builder()
                        .cols(1)
                        .build()
        );
        final QUser qUser = QUser.user;
        final List<User> usersSnapshot = new JPAQuery<>(this.entityManager)
                .select(qUser)
                .from(qUser)
                .fetch();
        formFactory.addField(SelectFormularField.builder()
                .valuePath(KokuUserAppointmentDto.Fields.userId)
                .label("Nutzer")
                .id(KokuUserAppointmentDto.Fields.userId)
                .possibleValues(usersSnapshot.stream().map(user -> {
                    return SelectFormularFieldPossibleValue.builder()
                            .id(user.getId())
                            .text((user.getFirstname() + " " + user.getLastname()).trim())
                            .disabled(user.isDeleted())
                            .build();
                }).toList())
                .defaultValue(SecurityContextHolder.getContext().getAuthentication().getName())
                .readonly(true)
                .build()
        );

        formFactory.addField(TextareaFormularField.builder()
                .label("Beschreibung")
                .valuePath(KokuUserAppointmentDto.Fields.description)
                .build()
        );

        formFactory.addContainer(GridContainer.builder()
                .cols(1)
                .md(2)
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuUserAppointmentDto.Fields.startDate)
                .type(EnumInputFormularFieldType.DATE)
                .label("Datum von")
                .required(true)
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuUserAppointmentDto.Fields.startTime)
                .type(EnumInputFormularFieldType.TIME)
                .label("Zeit von")
                .required(true)
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuUserAppointmentDto.Fields.endDate)
                .type(EnumInputFormularFieldType.DATE)
                .label("Datum bis")
                .required(true)
                .build()
        );
        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuUserAppointmentDto.Fields.endTime)
                .type(EnumInputFormularFieldType.TIME)
                .label("Zeit bis")
                .required(true)
                .build()
        );
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


    @GetMapping("/users/appointments/list")
    public ListViewDto getListView() {
        final ListViewFactory listViewFactory = new ListViewFactory(
                new DefaultListViewContentIdGenerator(),
                KokuUserAppointmentDto.Fields.id
        );

        final ListViewFieldReference startDateFieldRef = listViewFactory.addField(
                KokuUserAppointmentDto.Fields.startDate,
                ListViewInputFieldDto.builder()
                        .label("Datum")
                        .type(ListViewInputFieldTypeEnumDto.DATE)
                        .build()
        );
        final ListViewFieldReference startTimeFieldRef = listViewFactory.addField(
                KokuUserAppointmentDto.Fields.startTime,
                ListViewInputFieldDto.builder()
                        .label("Zeit")
                        .build()
        );
        final ListViewFieldReference descriptionFieldRef = listViewFactory.addField(
                KokuUserAppointmentDto.Fields.description,
                ListViewInputFieldDto.builder()
                        .label("Beschreibung")
                        .build()
        );
        final ListViewSourcePathReference idSourcePathFieldRef = listViewFactory.addSourcePath(KokuUserAppointmentDto.Fields.id);
        final ListViewSourcePathReference deletedSourceRef = listViewFactory.addSourcePath(KokuUserAppointmentDto.Fields.deleted);

        listViewFactory.addAction(ListViewOpenRoutedContentActionDto.builder()
                .route("appointments/new")
                .icon("PLUS")
                .build()
        );
        listViewFactory.addRoutedItem(ListViewRoutedDummyItemDto.builder()
                .route("appointments/new")
                .text("Neuer Privater Termin")
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadAddItemGlobalEventListenerDto.builder()
                .eventName("user-appointment-created")
                .idPath(KokuUserAppointmentDto.Fields.id)
                .valueMapping(Map.of(
                        KokuUserAppointmentDto.Fields.startDate, startDateFieldRef,
                        KokuUserAppointmentDto.Fields.startTime, startTimeFieldRef,
                        KokuUserAppointmentDto.Fields.description, descriptionFieldRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route("appointments/new")
                        .inlineContent(ListViewHeaderContentDto.builder()
                                .title("Neuer Privater Termin")
                                .content(ListViewFormularContentDto.builder()
                                        .formularUrl("services/users/users/appointments/form")
                                        .submitUrl("services/users/users/appointments")
                                        .submitMethod(ListViewFormularActionSubmitMethodEnumDto.POST)
                                        .maxWidthInPx(800)
                                        .onSaveEvents(Arrays.asList(
                                                ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                        .eventName("user-appointment-created")
                                                        .build(),
                                                ListViewOpenRoutedInlineFormularContentSaveEventDto.builder()
                                                        .route("appointments/:appointmentId")
                                                        .params(Arrays.asList(
                                                                ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto.builder()
                                                                        .param(":appointmentId")
                                                                        .valuePath(KokuUserAppointmentDto.Fields.id)
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
                .route("appointments/:appointmentId")
                .params(Arrays.asList(ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                        .param(":appointmentId")
                        .valueReference(idSourcePathFieldRef)
                        .build()
                ))
                .build()
        );
        listViewFactory.addGlobalEventListener(ListViewEventPayloadItemUpdateGlobalEventListenerDto.builder()
                .eventName("user-appointment-updated")
                .idPath(KokuUserAppointmentDto.Fields.id)
                .valueMapping(Map.of(
                        KokuUserAppointmentDto.Fields.startDate, startDateFieldRef,
                        KokuUserAppointmentDto.Fields.startTime, startTimeFieldRef,
                        KokuUserAppointmentDto.Fields.description, descriptionFieldRef
                ))
                .build()
        );
        listViewFactory.addRoutedContent(
                ListViewRoutedContentDto.builder()
                        .route("appointments/:appointmentId")
                        .itemId(":appointmentId")
                        .inlineContent(ListViewHeaderContentDto.builder()
                                .sourceUrl("services/users/users/appointments/:appointmentId/summary")
                                .titlePath(KokuUserAppointmentSummaryDto.Fields.summary)
                                .globalEventListeners(Arrays.asList(ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto.builder()
                                        .eventName("user-appointment-updated")
                                        .idPath(KokuUserAppointmentDto.Fields.id)
                                        .titleValuePath(KokuUserAppointmentDto.Fields.summary)
                                        .build()
                                ))
                                .content(ListViewFormularContentDto.builder()
                                        .formularUrl("services/users/users/appointments/form")
                                        .sourceUrl("services/users/users/appointments/:appointmentId")
                                        .submitMethod(ListViewFormularActionSubmitMethodEnumDto.PUT)
                                        .maxWidthInPx(800)
                                        .onSaveEvents(Arrays.asList(
                                                ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                        .eventName("user-appointment-updated")
                                                        .build()
                                        ))
                                        .build()
                                )
                                .build()
                        )
                        .build()
        );
        listViewFactory.addGlobalItemStyling(ListViewConditionalItemValueStylingDto.builder()
                .compareValuePath(KokuUserAppointmentDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveStyling(ListViewItemStylingDto.builder()
                        .lineThrough(true)
                        .opacity((short) 50)
                        .build()
                )
                .build()
        );
        listViewFactory.addItemAction(ListViewConditionalItemValueActionDto.builder()
                .compareValuePath(KokuUserAppointmentDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("ARROW_LEFT_START_ON_RECTANGLE")
                        .url("services/users/users/appointments/:appointmentId/restore")
                        .params(Arrays.asList(
                                ListViewCallHttpListValueActionParamDto.builder()
                                        .param(":appointmentId")
                                        .valueReference(idSourcePathFieldRef)
                                        .build()
                        ))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.PUT)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Termin wiederherstellen")
                                .content("Termin vom :date wiederherstellen?")
                                .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                        .param(":date")
                                        .valueReference(startDateFieldRef)
                                        .build()
                                ))
                                .build()
                        )
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Termin vom :date wurde erfolgreich wiederhergestellt")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":date")
                                                        .valueReference(startDateFieldRef)
                                                        .build()
                                        ))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuUserAppointmentDto.Fields.id)
                                        .valueMapping(Map.of(
                                                KokuUserAppointmentDto.Fields.deleted, deletedSourceRef
                                        ))
                                        .build(),
                                ListViewPropagateGlobalEventActionEventDto.builder()
                                        .eventName("user-appointment-updated")
                                        .build()
                        ))
                        .failEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text("Termin vom :date konnte nicht wiederhergestellt werden")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                        .params(Arrays.asList(
                                                ListViewNotificationEventValueParamDto.builder()
                                                        .param(":date")
                                                        .valueReference(startDateFieldRef)
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build())
                .negativeAction(
                        ListViewCallHttpListItemActionDto.builder()
                                .icon("TRASH")
                                .url("services/users/users/appointments/:appointmentId")
                                .params(Arrays.asList(
                                        ListViewCallHttpListValueActionParamDto.builder()
                                                .param(":appointmentId")
                                                .valueReference(idSourcePathFieldRef)
                                                .build()
                                ))
                                .method(ListViewCallHttpListItemActionMethodEnumDto.DELETE)
                                .userConfirmation(ListViewUserConfirmationDto.builder()
                                        .headline("Termin löschen")
                                        .content("Termin vom :date als gelöscht markieren?")
                                        .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                                .param(":date")
                                                .valueReference(startDateFieldRef)
                                                .build()
                                        ))
                                        .build()
                                )

                                .successEvents(Arrays.asList(
                                        ListViewNotificationEvent.builder()
                                                .text("Termin vom :date erfolgreich als gelöscht markiert")
                                                .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                                .params(Arrays.asList(
                                                        ListViewNotificationEventValueParamDto.builder()
                                                                .param(":date")
                                                                .valueReference(startDateFieldRef)
                                                                .build()
                                                ))
                                                .build(),
                                        ListViewEventPayloadUpdateActionEventDto.builder()
                                                .idPath(KokuUserAppointmentDto.Fields.id)
                                                .valueMapping(Map.of(
                                                        KokuUserAppointmentDto.Fields.deleted, deletedSourceRef
                                                ))
                                                .build(),
                                        ListViewPropagateGlobalEventActionEventDto.builder()
                                                .eventName("user-appointment-updated")
                                                .build()
                                ))
                                .failEvents(Arrays.asList(
                                        ListViewNotificationEvent.builder()
                                                .text("Termin vom :date konnte nicht als gelöscht markiert werden")
                                                .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                                .params(Arrays.asList(
                                                        ListViewNotificationEventValueParamDto.builder()
                                                                .param(":date")
                                                                .valueReference(startDateFieldRef)
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
            "/users/{userId}/appointments/query",
            "/users/appointments/query"
    })
    public ListPage findAll(
            @PathVariable(value = "userId", required = false) String requestedUserId,
            @RequestBody(required = false) final ListQuery predicate
    ) {

        final QUserAppointment qClazz = QUserAppointment.userAppointment;
        final ListQueryFactory<UserAppointment> listQueryFactory = new ListQueryFactory<>(
                this.entityManager,
                qClazz,
                qClazz.id,
                predicate
        );

        if (requestedUserId != null) {
            listQueryFactory.addDefaultFilter(qClazz.user.id.eq(requestedUserId));
        }
        listQueryFactory.setDefaultOrder(qClazz.startTimestamp.desc());

        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.id,
                qClazz.id
        );
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.deleted,
                qClazz.deleted
        );
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.version,
                qClazz.version
        );
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.description,
                qClazz.description
        );
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.userName,
                qClazz.user.firstname
                        .concat(" ")
                        .concat(qClazz.user.lastname)
                        .trim()
        );
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.startDate,
                Expressions.dateTemplate(
                        LocalDate.class,
                        "DATE({0})",
                        qClazz.startTimestamp
                )
        );
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.startTime,
                Expressions.timeTemplate(
                        LocalTime.class,
                        "cast({0} as time)",
                        qClazz.startTimestamp
                )
        );
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.endDate,
                Expressions.dateTemplate(
                        LocalDate.class,
                        "DATE({0})",
                        qClazz.endTimestamp
                )
        );
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.endTime,
                Expressions.timeTemplate(
                        LocalTime.class,
                        "cast({0} as time)",
                        qClazz.endTimestamp
                )
        );
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.userId,
                qClazz.user.id
        );

        return listQueryFactory.create();
    }

    @GetMapping(value = "/users/appointments/{appointmentId}")
    public KokuUserAppointmentDto readAppointment(@PathVariable("appointmentId") Long appointmentId) {
        final UserAppointment userAppointment = this.userAppointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        return this.transformer.transformToDto(userAppointment);
    }

    @GetMapping(value = "/users/appointments/{appointmentId}/summary")
    public KokuUserAppointmentSummaryDto readAppointmentSummary(@PathVariable("appointmentId") Long appointmentId) {
        final UserAppointment userAppointment = this.userAppointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        return new UserAppointmentToUserAppointmentSummaryDtoTransformer().transformToSummaryDto(userAppointment);
    }

    @PutMapping(value = "/users/appointments/{appointmentId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuUserAppointmentDto update(
            @PathVariable("appointmentId") Long appointmentId,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate,
            @RequestBody KokuUserAppointmentDto updatedDto
    ) {
        final UserAppointment userAppointment = this.entityManager.getReference(UserAppointment.class, appointmentId);
        if (!Boolean.TRUE.equals(forceUpdate) && !userAppointment.getVersion().equals(updatedDto.getVersion())) {
            throw new KokuBusinessExceptionWithConfirmationMessage(
                    KokuBusinessExceptionWithConfirmationMessageDto.builder()
                            .headline("Konflikt")
                            .confirmationMessage("Der Termin wurde zwischenzeitlich bearbeitet.\nWillst Du die Speicherung dennoch vornehmen?")
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
                                    .endpointUrl(String.format("services/users/users/appointments/%s?forceUpdate=%s", appointmentId, Boolean.TRUE))
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
        this.transformer.transformToEntity(userAppointment, updatedDto);
        this.entityManager.flush();
        return this.transformer.transformToDto(userAppointment);
    }

    @DeleteMapping(value = "/users/appointments/{appointmentId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuUserAppointmentDto delete(@PathVariable("appointmentId") Long appointmentId) {
        final UserAppointment userAppointment = this.entityManager.getReference(UserAppointment.class, appointmentId);
        if (userAppointment.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment is not deletable");
        }
        userAppointment.setDeleted(true);
        this.entityManager.flush();
        return this.transformer.transformToDto(userAppointment);
    }

    @PutMapping(value = "/users/appointments/{appointmentId}/restore")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuUserAppointmentDto restore(@PathVariable("appointmentId") Long appointmentId) {
        final UserAppointment userAppointment = this.entityManager.getReference(UserAppointment.class, appointmentId);
        if (!userAppointment.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment is not restorable");
        }
        userAppointment.setDeleted(false);
        this.entityManager.flush();
        return this.transformer.transformToDto(userAppointment);
    }

    @PostMapping("/users/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public KokuUserAppointmentDto create(@Validated @RequestBody KokuUserAppointmentDto newDto) {
        final UserAppointment newKokuUserAppointment = this.transformer.transformToEntity(new UserAppointment(), newDto);
        final UserAppointment savedKokuUserAppointment = this.userAppointmentRepository.saveAndFlush(newKokuUserAppointment);
        return this.transformer.transformToDto(savedKokuUserAppointment);
    }

}
