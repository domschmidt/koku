package de.domschmidt.koku.user.controller;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
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
import de.domschmidt.koku.dto.formular.events.FormNotificationEventDateValueParamDto;
import de.domschmidt.koku.dto.formular.events.FormNotificationEventSerenityEnumDto;
import de.domschmidt.koku.dto.formular.events.FormPropagateGlobalEventDto;
import de.domschmidt.koku.dto.formular.fields.input.DateInputFormularField;
import de.domschmidt.koku.dto.formular.fields.input.TimeInputFormularField;
import de.domschmidt.koku.dto.formular.fields.select.SelectFormularField;
import de.domschmidt.koku.dto.formular.fields.select.SelectFormularFieldPossibleValue;
import de.domschmidt.koku.dto.formular.fields.textarea.TextareaFormularField;
import de.domschmidt.koku.dto.formular.listeners.FormViewEventPayloadSourceUpdateGlobalEventListenerDto;
import de.domschmidt.koku.dto.formular.user_confirmation.FormUserConfirmationDto;
import de.domschmidt.koku.dto.list.fields.input.ListViewDateInputFieldDto;
import de.domschmidt.koku.dto.list.fields.input.ListViewInputFieldDto;
import de.domschmidt.koku.dto.list.filters.ListViewToggleFilterDefaultStateEnum;
import de.domschmidt.koku.dto.list.filters.ListViewToggleFilterDto;
import de.domschmidt.koku.dto.list.items.style.ListViewConditionalItemValueStylingDto;
import de.domschmidt.koku.dto.list.items.style.ListViewItemStylingDto;
import de.domschmidt.koku.dto.user.KokuUserAppointmentDto;
import de.domschmidt.koku.dto.user.KokuUserAppointmentSummaryDto;
import de.domschmidt.koku.user.kafka.users.service.UserAppointmentKafkaService;
import de.domschmidt.koku.user.persistence.QUser;
import de.domschmidt.koku.user.persistence.QUserAppointment;
import de.domschmidt.koku.user.persistence.User;
import de.domschmidt.koku.user.persistence.UserAppointment;
import de.domschmidt.koku.user.persistence.UserAppointmentRepository;
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
import de.domschmidt.list.dto.response.inline_content.formular.ListViewRouteBasedFormularContentOverrideDto;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping()
@Slf4j
public class UserAppointmentController {
    private static final String USER_APPOINTMENT_CREATED_EVENT = "user-appointment-created";
    private static final String USER_APPOINTMENT_UPDATED_EVENT = "user-appointment-updated";
    private static final String APPOINTMENT_ID_PARAM = ":appointmentId";
    private static final String DATE_PARAM = ":date";
    private static final String APPOINTMENT_DATE_LABEL = "Termin vom ";
    private static final String USER_APPOINTMENT_SERVICE_URL = "services/users/users/appointments/";

    private final EntityManager entityManager;
    private final UserAppointmentRepository userAppointmentRepository;
    private final UserAppointmentToUserAppointmentDtoTransformer transformer;
    private final UserAppointmentKafkaService userAppointmentKafkaService;

    @Autowired
    public UserAppointmentController(
            final EntityManager entityManager,
            final UserAppointmentRepository userAppointmentRepository,
            final UserAppointmentToUserAppointmentDtoTransformer transformer,
            final UserAppointmentKafkaService userAppointmentKafkaService) {
        this.entityManager = entityManager;
        this.userAppointmentRepository = userAppointmentRepository;
        this.transformer = transformer;
        this.userAppointmentKafkaService = userAppointmentKafkaService;
    }

    @GetMapping("/users/appointments/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory();
        final String rootId =
                formFactory.addContent(GridContainer.builder().cols(1).build());
        final QUser qUser = QUser.user;
        final List<User> usersSnapshot =
                new JPAQuery<>(this.entityManager).select(qUser).from(qUser).fetch();
        formFactory
                .place(formFactory.addContent(SelectFormularField.builder()
                        .valuePath(KokuUserAppointmentDto.Fields.userId)
                        .label("Nutzer")
                        .alias(KokuUserAppointmentDto.Fields.userId)
                        .possibleValues(usersSnapshot.stream()
                                .map(user -> SelectFormularFieldPossibleValue.builder()
                                        .id(user.getId())
                                        .text(Stream.of(user.getFirstname(), user.getLastname())
                                                .filter(s -> s != null && !s.isEmpty())
                                                .collect(Collectors.joining(" ")))
                                        .disabled(user.isDeleted())
                                        .build())
                                .toList())
                        .defaultValue(SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName())
                        .readonly(true)
                        .build()))
                .in(rootId)
                .outlet(FormOutlet.CONTENT);

        formFactory
                .place(formFactory.addContent(TextareaFormularField.builder()
                        .label("Beschreibung")
                        .valuePath(KokuUserAppointmentDto.Fields.description)
                        .build()))
                .in(rootId)
                .outlet(FormOutlet.CONTENT);

        final String container1Id =
                formFactory.addContent(GridContainer.builder().cols(1).md(2).build());
        formFactory.place(container1Id).in(rootId).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(DateInputFormularField.builder()
                        .valuePath(KokuUserAppointmentDto.Fields.startDate)
                        .label("Datum von")
                        .required(true)
                        .build()))
                .in(container1Id)
                .outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(TimeInputFormularField.builder()
                        .valuePath(KokuUserAppointmentDto.Fields.startTime)
                        .label("Zeit von")
                        .required(true)
                        .build()))
                .in(container1Id)
                .outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(DateInputFormularField.builder()
                        .valuePath(KokuUserAppointmentDto.Fields.endDate)
                        .label("Datum bis")
                        .required(true)
                        .build()))
                .in(container1Id)
                .outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(TimeInputFormularField.builder()
                        .valuePath(KokuUserAppointmentDto.Fields.endTime)
                        .label("Zeit bis")
                        .required(true)
                        .build()))
                .in(container1Id)
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
                        .successEvents(Arrays.asList(
                                FormNotificationEvent.builder()
                                        .text("Erfolgreich gespeichert")
                                        .serenity(FormNotificationEventSerenityEnumDto.SUCCESS)
                                        .build(),
                                FormPropagateGlobalEventDto.builder()
                                        .eventName(USER_APPOINTMENT_UPDATED_EVENT)
                                        .build()))
                        .failEvents(Arrays.asList(FormNotificationEvent.builder()
                                .text("Fehler beim Speichern")
                                .serenity(FormNotificationEventSerenityEnumDto.ERROR)
                                .build()))
                        .build()))
                .in(rootId)
                .outlet(FormOutlet.CONTENT);

        final String container2Id = formFactory.addContent(ConditionalContainer.builder()
                .compareValuePath(KokuUserAppointmentDto.Fields.deleted)
                .expectedValue(Boolean.FALSE)
                .build());
        formFactory.place(container2Id).in(rootId).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(KokuFormButton.builder()
                        .id("DeletePrivateAppointmentButton")
                        .buttonType(EnumButtonType.SUBMIT)
                        .text("Löschen")
                        .title("Jetzt löschen")
                        .styles(Arrays.asList(EnumButtonStyle.BLOCK, EnumButtonStyle.ERROR, EnumButtonStyle.OUTLINE))
                        .dockableSettings(ButtonDockableSettings.builder()
                                .icon("TRASH")
                                .styles(Arrays.asList(EnumButtonStyle.CIRCLE, EnumButtonStyle.ERROR))
                                .build())
                        .submitPayload(
                                KokuUserAppointmentDto.builder().deleted(true).build())
                        .userConfirmation(FormUserConfirmationDto.builder()
                                .headline("Termin löschen")
                                .content(APPOINTMENT_DATE_LABEL + DATE_PARAM + " als gelöscht markieren?")
                                .params(Arrays.asList(FormButtonUserConfirmationSourcePathParamDto.builder()
                                        .param(DATE_PARAM)
                                        .sourcePath(KokuUserAppointmentDto.Fields.startDate)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                FormNotificationEvent.builder()
                                        .text(APPOINTMENT_DATE_LABEL + DATE_PARAM
                                                + " erfolgreich als gelöscht markiert")
                                        .serenity(FormNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(FormNotificationEventDateValueParamDto.builder()
                                                .param(DATE_PARAM)
                                                .sourcePath(KokuUserAppointmentDto.Fields.startDate)
                                                .build()))
                                        .build(),
                                FormPropagateGlobalEventDto.builder()
                                        .eventName(USER_APPOINTMENT_UPDATED_EVENT)
                                        .build()))
                        .failEvents(Arrays.asList(FormNotificationEvent.builder()
                                .text(APPOINTMENT_DATE_LABEL + DATE_PARAM
                                        + " konnte nicht als gelöscht markiert werden")
                                .serenity(FormNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(FormNotificationEventDateValueParamDto.builder()
                                        .param(DATE_PARAM)
                                        .sourcePath(KokuUserAppointmentDto.Fields.startDate)
                                        .build()))
                                .build()))
                        .build()))
                .in(container2Id)
                .outlet(FormOutlet.CONTENT);

        final String container3Id = formFactory.addContent(ConditionalContainer.builder()
                .compareValuePath(KokuUserAppointmentDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .build());
        formFactory.place(container3Id).in(rootId).outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(KokuFormButton.builder()
                        .id("RestorePrivateAppointmentButton")
                        .buttonType(EnumButtonType.SUBMIT)
                        .text("Wiederherstellen")
                        .title("Jetzt wiederherstellen")
                        .styles(Arrays.asList(EnumButtonStyle.BLOCK, EnumButtonStyle.SUCCESS, EnumButtonStyle.OUTLINE))
                        .dockableSettings(ButtonDockableSettings.builder()
                                .icon("ARROW_LEFT_START_ON_RECTANGLE")
                                .styles(Arrays.asList(EnumButtonStyle.CIRCLE, EnumButtonStyle.SUCCESS))
                                .build())
                        .submitPayload(
                                KokuUserAppointmentDto.builder().deleted(false).build())
                        .userConfirmation(FormUserConfirmationDto.builder()
                                .headline("Termin wiederherstellen")
                                .content(APPOINTMENT_DATE_LABEL + DATE_PARAM + " wiederherstellen?")
                                .params(Arrays.asList(FormButtonUserConfirmationSourcePathParamDto.builder()
                                        .param(DATE_PARAM)
                                        .sourcePath(KokuUserAppointmentDto.Fields.startDate)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                FormNotificationEvent.builder()
                                        .text(APPOINTMENT_DATE_LABEL + DATE_PARAM
                                                + " wurde erfolgreich wiederhergestellt")
                                        .serenity(FormNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(FormNotificationEventDateValueParamDto.builder()
                                                .param(DATE_PARAM)
                                                .sourcePath(KokuUserAppointmentDto.Fields.startDate)
                                                .build()))
                                        .build(),
                                FormPropagateGlobalEventDto.builder()
                                        .eventName(USER_APPOINTMENT_UPDATED_EVENT)
                                        .build()))
                        .failEvents(Arrays.asList(FormNotificationEvent.builder()
                                .text(APPOINTMENT_DATE_LABEL + DATE_PARAM + " konnte nicht wiederhergestellt werden")
                                .serenity(FormNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(FormNotificationEventDateValueParamDto.builder()
                                        .param(DATE_PARAM)
                                        .sourcePath(KokuUserAppointmentDto.Fields.startDate)
                                        .build()))
                                .build()))
                        .build()))
                .in(container3Id)
                .outlet(FormOutlet.CONTENT);

        formFactory.addGlobalEventListener(FormViewEventPayloadSourceUpdateGlobalEventListenerDto.builder()
                .eventName(USER_APPOINTMENT_UPDATED_EVENT)
                .idPath(KokuUserAppointmentDto.Fields.id)
                .build());

        return formFactory.create(rootId);
    }

    @GetMapping("/users/appointments/list")
    public ListViewDto getListView() {
        final ListViewFactory listViewFactory =
                new ListViewFactory(new DefaultListViewContentIdGenerator(), KokuUserAppointmentDto.Fields.id);

        final ListViewFieldReference startDateFieldRef = listViewFactory.addField(
                KokuUserAppointmentDto.Fields.startDate,
                ListViewDateInputFieldDto.builder().label("Datum").build());
        final ListViewFieldReference startTimeFieldRef = listViewFactory.addField(
                KokuUserAppointmentDto.Fields.startTime,
                ListViewInputFieldDto.builder().label("Zeit").build());
        final ListViewFieldReference descriptionFieldRef = listViewFactory.addField(
                KokuUserAppointmentDto.Fields.description,
                ListViewInputFieldDto.builder().label("Beschreibung").build());
        final ListViewSourcePathReference idSourcePathFieldRef =
                listViewFactory.addSourcePath(KokuUserAppointmentDto.Fields.id);
        final ListViewSourcePathReference deletedSourceRef =
                listViewFactory.addSourcePath(KokuUserAppointmentDto.Fields.deleted);

        listViewFactory.addFilter(
                KokuUserAppointmentDto.Fields.deleted,
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
                .text("Neuer Privater Termin")
                .build());
        listViewFactory.addGlobalEventListener(ListViewEventPayloadAddItemGlobalEventListenerDto.builder()
                .eventName(USER_APPOINTMENT_CREATED_EVENT)
                .idPath(KokuUserAppointmentDto.Fields.id)
                .valueMapping(Map.of(
                        KokuUserAppointmentDto.Fields.startDate, startDateFieldRef,
                        KokuUserAppointmentDto.Fields.startTime, startTimeFieldRef,
                        KokuUserAppointmentDto.Fields.description, descriptionFieldRef))
                .build());
        listViewFactory.addRoutedContent(ListViewRoutedContentDto.builder()
                .route("new")
                .inlineContent(ListViewHeaderContentDto.builder()
                        .title("Neuer Privater Termin")
                        .content(ListViewFormularContentDto.builder()
                                .formularUrl(USER_APPOINTMENT_SERVICE_URL + "form")
                                .submitUrl("services/users/users/appointments")
                                .contentOverrides(Arrays.asList(ListViewRouteBasedFormularContentOverrideDto.builder()
                                        .routeParam(":userId")
                                        .alias(KokuUserAppointmentDto.Fields.userId)
                                        .disabled(true)
                                        .build()))
                                .submitMethod(ListViewFormularActionSubmitMethodEnumDto.POST)
                                .maxWidthInPx(800)
                                .onSaveEvents(Arrays.asList(
                                        ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                .eventName(USER_APPOINTMENT_CREATED_EVENT)
                                                .build(),
                                        ListViewOpenRoutedInlineFormularContentSaveEventDto.builder()
                                                .route(APPOINTMENT_ID_PARAM)
                                                .params(Arrays.asList(
                                                        ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto
                                                                .builder()
                                                                .param(APPOINTMENT_ID_PARAM)
                                                                .valuePath(KokuUserAppointmentDto.Fields.id)
                                                                .build()))
                                                .build()))
                                .build())
                        .build())
                .build());

        listViewFactory.setItemClickAction(ListViewItemClickOpenRoutedContentActionDto.builder()
                .route(APPOINTMENT_ID_PARAM)
                .params(Arrays.asList(ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                        .param(APPOINTMENT_ID_PARAM)
                        .valueReference(idSourcePathFieldRef)
                        .build()))
                .build());
        listViewFactory.addGlobalEventListener(ListViewEventPayloadItemUpdateGlobalEventListenerDto.builder()
                .eventName(USER_APPOINTMENT_UPDATED_EVENT)
                .idPath(KokuUserAppointmentDto.Fields.id)
                .valueMapping(Map.of(
                        KokuUserAppointmentDto.Fields.startDate, startDateFieldRef,
                        KokuUserAppointmentDto.Fields.startTime, startTimeFieldRef,
                        KokuUserAppointmentDto.Fields.description, descriptionFieldRef,
                        KokuUserAppointmentDto.Fields.deleted, deletedSourceRef))
                .build());
        listViewFactory.addRoutedContent(ListViewRoutedContentDto.builder()
                .route(APPOINTMENT_ID_PARAM)
                .itemId(APPOINTMENT_ID_PARAM)
                .inlineContent(ListViewHeaderContentDto.builder()
                        .sourceUrl(USER_APPOINTMENT_SERVICE_URL + APPOINTMENT_ID_PARAM + "/summary")
                        .titlePath(KokuUserAppointmentSummaryDto.Fields.summary)
                        .globalEventListeners(
                                Arrays.asList(ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto.builder()
                                        .eventName(USER_APPOINTMENT_UPDATED_EVENT)
                                        .idPath(KokuUserAppointmentDto.Fields.id)
                                        .titleValuePath(KokuUserAppointmentDto.Fields.summary)
                                        .build()))
                        .content(ListViewFormularContentDto.builder()
                                .formularUrl(USER_APPOINTMENT_SERVICE_URL + "form")
                                .sourceUrl(USER_APPOINTMENT_SERVICE_URL + APPOINTMENT_ID_PARAM)
                                .contentOverrides(Arrays.asList(ListViewRouteBasedFormularContentOverrideDto.builder()
                                        .routeParam(":userId")
                                        .alias(KokuUserAppointmentDto.Fields.userId)
                                        .disabled(true)
                                        .build()))
                                .submitMethod(ListViewFormularActionSubmitMethodEnumDto.PUT)
                                .maxWidthInPx(800)
                                .onSaveEvents(Arrays.asList(
                                        ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                .eventName(USER_APPOINTMENT_UPDATED_EVENT)
                                                .build()))
                                .build())
                        .build())
                .build());
        listViewFactory.addGlobalItemStyling(ListViewConditionalItemValueStylingDto.builder()
                .compareValuePath(KokuUserAppointmentDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveStyling(ListViewItemStylingDto.builder()
                        .lineThrough(true)
                        .opacity((short) 50)
                        .build())
                .build());
        listViewFactory.addItemAction(ListViewConditionalItemValueActionDto.builder()
                .compareValuePath(KokuUserAppointmentDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("ARROW_LEFT_START_ON_RECTANGLE")
                        .url(USER_APPOINTMENT_SERVICE_URL + APPOINTMENT_ID_PARAM + "/restore")
                        .params(Arrays.asList(ListViewCallHttpListValueActionParamDto.builder()
                                .param(APPOINTMENT_ID_PARAM)
                                .valueReference(idSourcePathFieldRef)
                                .build()))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.PUT)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Termin wiederherstellen")
                                .content(APPOINTMENT_DATE_LABEL + DATE_PARAM + " wiederherstellen?")
                                .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                        .param(DATE_PARAM)
                                        .valueReference(startDateFieldRef)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text(APPOINTMENT_DATE_LABEL + DATE_PARAM
                                                + " wurde erfolgreich wiederhergestellt")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                                .param(DATE_PARAM)
                                                .valueReference(startDateFieldRef)
                                                .build()))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuUserAppointmentDto.Fields.id)
                                        .valueMapping(Map.of(KokuUserAppointmentDto.Fields.deleted, deletedSourceRef))
                                        .build(),
                                ListViewPropagateGlobalEventActionEventDto.builder()
                                        .eventName(USER_APPOINTMENT_UPDATED_EVENT)
                                        .build()))
                        .failEvents(Arrays.asList(ListViewNotificationEvent.builder()
                                .text(APPOINTMENT_DATE_LABEL + DATE_PARAM + " konnte nicht wiederhergestellt werden")
                                .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                        .param(DATE_PARAM)
                                        .valueReference(startDateFieldRef)
                                        .build()))
                                .build()))
                        .build())
                .negativeAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("TRASH")
                        .url(USER_APPOINTMENT_SERVICE_URL + APPOINTMENT_ID_PARAM)
                        .params(Arrays.asList(ListViewCallHttpListValueActionParamDto.builder()
                                .param(APPOINTMENT_ID_PARAM)
                                .valueReference(idSourcePathFieldRef)
                                .build()))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.DELETE)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Termin löschen")
                                .content(APPOINTMENT_DATE_LABEL + DATE_PARAM + " als gelöscht markieren?")
                                .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                        .param(DATE_PARAM)
                                        .valueReference(startDateFieldRef)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text(APPOINTMENT_DATE_LABEL + DATE_PARAM
                                                + " erfolgreich als gelöscht markiert")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                                .param(DATE_PARAM)
                                                .valueReference(startDateFieldRef)
                                                .build()))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuUserAppointmentDto.Fields.id)
                                        .valueMapping(Map.of(KokuUserAppointmentDto.Fields.deleted, deletedSourceRef))
                                        .build(),
                                ListViewPropagateGlobalEventActionEventDto.builder()
                                        .eventName(USER_APPOINTMENT_UPDATED_EVENT)
                                        .build()))
                        .failEvents(Arrays.asList(ListViewNotificationEvent.builder()
                                .text(APPOINTMENT_DATE_LABEL + DATE_PARAM
                                        + " konnte nicht als gelöscht markiert werden")
                                .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                        .param(DATE_PARAM)
                                        .valueReference(startDateFieldRef)
                                        .build()))
                                .build()))
                        .build())
                .build());

        return listViewFactory.create();
    }

    @PostMapping(value = {"/users/{userId}/appointments/query", "/users/appointments/query"})
    public ListPage findAll(
            @PathVariable(value = "userId", required = false) String requestedUserId,
            @RequestBody(required = false) final ListQuery predicate) {

        final QUserAppointment qClazz = QUserAppointment.userAppointment;
        final ListQueryFactory<UserAppointment> listQueryFactory =
                new ListQueryFactory<>(this.entityManager, qClazz, qClazz.id, predicate);

        if (requestedUserId != null) {
            listQueryFactory.addDefaultFilter(qClazz.user.id.eq(requestedUserId));
        }
        listQueryFactory.setDefaultOrder(qClazz.startTimestamp.desc());

        listQueryFactory.addFetchExpr(KokuUserAppointmentDto.Fields.id, qClazz.id);
        listQueryFactory.addFetchExpr(KokuUserAppointmentDto.Fields.deleted, qClazz.deleted);
        listQueryFactory.addFetchExpr(KokuUserAppointmentDto.Fields.version, qClazz.version);
        listQueryFactory.addFetchExpr(KokuUserAppointmentDto.Fields.description, qClazz.description);
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.userName,
                qClazz.user.firstname.concat(" ").concat(qClazz.user.lastname).trim());
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.startDate,
                Expressions.dateTemplate(LocalDate.class, "DATE({0})", qClazz.startTimestamp));
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.startTime,
                Expressions.timeTemplate(LocalTime.class, "cast({0} as time)", qClazz.startTimestamp));
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.endDate,
                Expressions.dateTemplate(LocalDate.class, "DATE({0})", qClazz.endTimestamp));
        listQueryFactory.addFetchExpr(
                KokuUserAppointmentDto.Fields.endTime,
                Expressions.timeTemplate(LocalTime.class, "cast({0} as time)", qClazz.endTimestamp));
        listQueryFactory.addFetchExpr(KokuUserAppointmentDto.Fields.userId, qClazz.user.id);

        return listQueryFactory.create();
    }

    @GetMapping(value = "/users/appointments/{appointmentId}")
    public KokuUserAppointmentDto readAppointment(@PathVariable("appointmentId") Long appointmentId) {
        final UserAppointment userAppointment = this.userAppointmentRepository
                .findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        return this.transformer.transformToDto(userAppointment);
    }

    @GetMapping(value = "/users/appointments/{appointmentId}/summary")
    public KokuUserAppointmentSummaryDto readAppointmentSummary(@PathVariable("appointmentId") Long appointmentId) {
        final UserAppointment userAppointment = this.userAppointmentRepository
                .findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        return new UserAppointmentToUserAppointmentSummaryDtoTransformer().transformToSummaryDto(userAppointment);
    }

    @PutMapping(value = "/users/appointments/{appointmentId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuUserAppointmentDto update(
            @PathVariable("appointmentId") Long appointmentId,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate,
            @RequestBody KokuUserAppointmentDto updatedDto) {
        final UserAppointment userAppointment = this.entityManager.getReference(UserAppointment.class, appointmentId);
        if (!Boolean.TRUE.equals(forceUpdate) && !userAppointment.getVersion().equals(updatedDto.getVersion())) {
            throw new KokuBusinessExceptionWithConfirmationMessage(KokuBusinessErrorWithConfirmationMessageDto.builder()
                    .headline("Konflikt")
                    .confirmationMessage("Der Termin wurde zwischenzeitlich bearbeitet.\n"
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
                                    USER_APPOINTMENT_SERVICE_URL + "%s?forceUpdate=%s", appointmentId, Boolean.TRUE))
                            .build())
                    .button(KokuBusinessExceptionCloseButtonDto.builder()
                            .text("Abbrechen")
                            .title("Abbruch")
                            .build())
                    .build());
        }
        this.transformer.transformToEntity(userAppointment, updatedDto);
        this.entityManager.flush();
        sendUserAppointmentUpdate(userAppointment);
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
        sendUserAppointmentUpdate(userAppointment);
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
        sendUserAppointmentUpdate(userAppointment);
        return this.transformer.transformToDto(userAppointment);
    }

    @PostMapping("/users/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public KokuUserAppointmentDto create(@Validated @RequestBody KokuUserAppointmentDto newDto) {
        final UserAppointment newKokuUserAppointment =
                this.transformer.transformToEntity(new UserAppointment(), newDto);
        final UserAppointment savedKokuUserAppointment =
                this.userAppointmentRepository.saveAndFlush(newKokuUserAppointment);
        sendUserAppointmentUpdate(savedKokuUserAppointment);
        return this.transformer.transformToDto(savedKokuUserAppointment);
    }

    public void sendUserAppointmentUpdate(final UserAppointment userAppointment) {
        try {
            userAppointmentKafkaService.sendUserAppointment(userAppointment);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Error sending user appointment update", e);
        }
    }
}
