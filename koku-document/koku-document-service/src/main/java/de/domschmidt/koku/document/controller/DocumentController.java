package de.domschmidt.koku.document.controller;

import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.formular.dto.content.buttons.EnumButtonType;
import de.domschmidt.formular.factory.FormOutlet;
import de.domschmidt.formular.factory.FormViewFactory;
import de.domschmidt.koku.business_exception.dto.KokuBusinessErrorWithConfirmationMessageDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionCloseButtonDto;
import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionSendToDifferentEndpointButtonDto;
import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.document.persistence.Document;
import de.domschmidt.koku.document.persistence.DocumentRepository;
import de.domschmidt.koku.document.persistence.QDocument;
import de.domschmidt.koku.document.transformer.DocumentToDocumentDtoTransformer;
import de.domschmidt.koku.dto.document.KokuDocumentDto;
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
import de.domschmidt.koku.dto.formular.fields.documents.DocumentDesignerFormularField;
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
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
import de.domschmidt.list.dto.response.inline_content.document.ListViewDocumentFormContentAfterSavePropagateGlobalEventDto;
import de.domschmidt.list.dto.response.inline_content.document.ListViewDocumentFormContentDto;
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
import de.domschmidt.list.dto.response.items.actions.inline_content.ListViewItemActionOpenRoutedContentActionDto;
import de.domschmidt.list.dto.response.items.actions.inline_content.ListViewItemActionOpenRoutedContentActionItemValueParamDto;
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
public class DocumentController {
    private static final String NAME_PARAM = ":name";
    private static final String DOCUMENT_ID_PARAM = ":documentId";
    private static final String DOCUMENT_CREATED_EVENT = "document-created";
    private static final String DOCUMENT_UPDATED_EVENT = "document-updated";
    private static final String NEW_DOCUMENT_LABEL = "Neues Dokument";
    private static final String DOCUMENT_TEMPLATE_LABEL = "Dokumentvorlage ";
    private static final String DOCUMENT_FORM_URL = "services/documents/documents/form";
    private static final String DUPLICATE_DOCUMENT_ROUTE = "duplicate/" + DOCUMENT_ID_PARAM;
    private static final String DOCUMENT_ID_URL = "services/documents/documents/" + DOCUMENT_ID_PARAM;

    private final DocumentRepository documentRepository;
    private final DocumentToDocumentDtoTransformer transformer;
    private final EntityManager entityManager;

    @GetMapping("/documents/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory();
        final String rootId =
                formFactory.addContent(GridContainer.builder().cols(1).build());

        formFactory
                .place(formFactory.addContent(InputFormularField.builder()
                        .label("Name")
                        .valuePath(KokuDocumentDto.Fields.name)
                        .required(true)
                        .build()))
                .in(rootId)
                .outlet(FormOutlet.CONTENT);
        formFactory
                .place(formFactory.addContent(DocumentDesignerFormularField.builder()
                        .valuePath(KokuDocumentDto.Fields.template)
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
                .compareValuePath(KokuDocumentDto.Fields.deleted)
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
                        .submitPayload(KokuDocumentDto.builder().deleted(true).build())
                        .userConfirmation(FormUserConfirmationDto.builder()
                                .headline(DOCUMENT_TEMPLATE_LABEL + "löschen")
                                .content(DOCUMENT_TEMPLATE_LABEL + NAME_PARAM + " als gelöscht markieren?")
                                .params(Arrays.asList(FormButtonUserConfirmationSourcePathParamDto.builder()
                                        .param(NAME_PARAM)
                                        .sourcePath(KokuDocumentDto.Fields.name)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                FormNotificationEvent.builder()
                                        .text(DOCUMENT_TEMPLATE_LABEL + NAME_PARAM
                                                + " erfolgreich als gelöscht markiert")
                                        .serenity(FormNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(FormNotificationEventValueParamDto.builder()
                                                .param(NAME_PARAM)
                                                .sourcePath(KokuDocumentDto.Fields.name)
                                                .build()))
                                        .build(),
                                FormPropagateGlobalEventDto.builder()
                                        .eventName(DOCUMENT_UPDATED_EVENT)
                                        .build()))
                        .failEvents(Arrays.asList(FormNotificationEvent.builder()
                                .text(DOCUMENT_TEMPLATE_LABEL + NAME_PARAM
                                        + " konnte nicht als gelöscht markiert werden")
                                .serenity(FormNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(FormNotificationEventValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .sourcePath(KokuDocumentDto.Fields.name)
                                        .build()))
                                .build()))
                        .build()))
                .in(deleteContainerId)
                .outlet(FormOutlet.CONTENT);

        final String restoreContainerId = formFactory.addContent(ConditionalContainer.builder()
                .compareValuePath(KokuDocumentDto.Fields.deleted)
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
                        .submitPayload(KokuDocumentDto.builder().deleted(false).build())
                        .userConfirmation(FormUserConfirmationDto.builder()
                                .headline(DOCUMENT_TEMPLATE_LABEL + "wiederherstellen")
                                .content(DOCUMENT_TEMPLATE_LABEL + NAME_PARAM + " wiederherstellen?")
                                .params(Arrays.asList(FormButtonUserConfirmationSourcePathParamDto.builder()
                                        .param(NAME_PARAM)
                                        .sourcePath(KokuDocumentDto.Fields.name)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                FormNotificationEvent.builder()
                                        .text(DOCUMENT_TEMPLATE_LABEL + NAME_PARAM
                                                + " wurde erfolgreich wiederhergestellt")
                                        .serenity(FormNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(FormNotificationEventValueParamDto.builder()
                                                .param(NAME_PARAM)
                                                .sourcePath(KokuDocumentDto.Fields.name)
                                                .build()))
                                        .build(),
                                FormPropagateGlobalEventDto.builder()
                                        .eventName(DOCUMENT_UPDATED_EVENT)
                                        .build()))
                        .failEvents(Arrays.asList(FormNotificationEvent.builder()
                                .text(DOCUMENT_TEMPLATE_LABEL + NAME_PARAM + " konnte nicht wiederhergestellt werden")
                                .serenity(FormNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(FormNotificationEventValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .sourcePath(KokuDocumentDto.Fields.name)
                                        .build()))
                                .build()))
                        .build()))
                .in(restoreContainerId)
                .outlet(FormOutlet.CONTENT);

        formFactory.addGlobalEventListener(FormViewEventPayloadSourceUpdateGlobalEventListenerDto.builder()
                .eventName(DOCUMENT_UPDATED_EVENT)
                .idPath(KokuDocumentDto.Fields.id)
                .build());

        return formFactory.create(rootId);
    }

    @GetMapping("/documents/list")
    public ListViewDto getListView() {
        final ListViewFactory listViewFactory =
                new ListViewFactory(new DefaultListViewContentIdGenerator(), KokuDocumentDto.Fields.id);

        final ListViewFieldReference nameFieldRef = listViewFactory.addField(
                KokuDocumentDto.Fields.name,
                ListViewInputFieldDto.builder().label("Name").build());
        final ListViewSourcePathReference idSourcePathFieldRef =
                listViewFactory.addSourcePath(KokuDocumentDto.Fields.id);
        final ListViewSourcePathReference deletedSourceRef =
                listViewFactory.addSourcePath(KokuDocumentDto.Fields.deleted);

        listViewFactory.addFilter(
                KokuDocumentDto.Fields.deleted,
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
                .text(NEW_DOCUMENT_LABEL)
                .build());
        listViewFactory.addRoutedContent(ListViewRoutedContentDto.builder()
                .route("new")
                .inlineContent(ListViewHeaderContentDto.builder()
                        .title(NEW_DOCUMENT_LABEL)
                        .content(ListViewFormularContentDto.builder()
                                .formularUrl(DOCUMENT_FORM_URL)
                                .submitUrl("services/documents/documents")
                                .submitMethod(ListViewFormularActionSubmitMethodEnumDto.POST)
                                .maxWidthInPx(9999)
                                .onSaveEvents(Arrays.asList(
                                        ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                .eventName(DOCUMENT_CREATED_EVENT)
                                                .build(),
                                        ListViewOpenRoutedInlineFormularContentSaveEventDto.builder()
                                                .route(DOCUMENT_ID_PARAM)
                                                .params(Arrays.asList(
                                                        ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto
                                                                .builder()
                                                                .param(DOCUMENT_ID_PARAM)
                                                                .valuePath(KokuDocumentDto.Fields.id)
                                                                .build()))
                                                .build()))
                                .build())
                        .build())
                .build());

        listViewFactory.addItemAction(ListViewItemActionOpenRoutedContentActionDto.builder()
                .route(DUPLICATE_DOCUMENT_ROUTE)
                .params(Arrays.asList(ListViewItemActionOpenRoutedContentActionItemValueParamDto.builder()
                        .param(DOCUMENT_ID_PARAM)
                        .valueReference(idSourcePathFieldRef)
                        .build()))
                .icon("DUPLICATE")
                .build());
        listViewFactory.addRoutedItem(ListViewRoutedDummyItemDto.builder()
                .route(DUPLICATE_DOCUMENT_ROUTE)
                .text(NEW_DOCUMENT_LABEL)
                .build());
        listViewFactory.addRoutedContent(ListViewRoutedContentDto.builder()
                .route(DUPLICATE_DOCUMENT_ROUTE)
                .inlineContent(ListViewHeaderContentDto.builder()
                        .title("Dupliziere Dokument")
                        .content(ListViewFormularContentDto.builder()
                                .formularUrl(DOCUMENT_FORM_URL)
                                .sourceUrl(DOCUMENT_ID_URL)
                                .submitUrl("services/documents/documents")
                                .submitMethod(ListViewFormularActionSubmitMethodEnumDto.POST)
                                .maxWidthInPx(9999)
                                .onSaveEvents(Arrays.asList(
                                        ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                .eventName(DOCUMENT_CREATED_EVENT)
                                                .build(),
                                        ListViewOpenRoutedInlineFormularContentSaveEventDto.builder()
                                                .route(DOCUMENT_ID_PARAM)
                                                .params(Arrays.asList(
                                                        ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto
                                                                .builder()
                                                                .param(DOCUMENT_ID_PARAM)
                                                                .valuePath(KokuDocumentDto.Fields.id)
                                                                .build()))
                                                .build()))
                                .build())
                        .build())
                .build());

        listViewFactory.setItemClickAction(ListViewItemClickOpenRoutedContentActionDto.builder()
                .route(DOCUMENT_ID_PARAM)
                .params(Arrays.asList(ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                        .param(DOCUMENT_ID_PARAM)
                        .valueReference(idSourcePathFieldRef)
                        .build()))
                .build());

        listViewFactory.addRoutedContent(ListViewRoutedContentDto.builder()
                .route(DOCUMENT_ID_PARAM)
                .itemId(DOCUMENT_ID_PARAM)
                .inlineContent(ListViewHeaderContentDto.builder()
                        .sourceUrl(DOCUMENT_ID_URL)
                        .titlePath(KokuDocumentDto.Fields.name)
                        .globalEventListeners(
                                Arrays.asList(ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto.builder()
                                        .eventName(DOCUMENT_UPDATED_EVENT)
                                        .idPath(KokuDocumentDto.Fields.id)
                                        .titleValuePath(KokuDocumentDto.Fields.name)
                                        .build()))
                        .content(ListViewFormularContentDto.builder()
                                .formularUrl(DOCUMENT_FORM_URL)
                                .sourceUrl(DOCUMENT_ID_URL)
                                .submitMethod(ListViewFormularActionSubmitMethodEnumDto.PUT)
                                .maxWidthInPx(9999)
                                .onSaveEvents(Arrays.asList(
                                        ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                .eventName(DOCUMENT_UPDATED_EVENT)
                                                .build()))
                                .build())
                        .build())
                .build());
        listViewFactory.addGlobalItemStyling(ListViewConditionalItemValueStylingDto.builder()
                .compareValuePath(KokuDocumentDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveStyling(ListViewItemStylingDto.builder()
                        .lineThrough(true)
                        .opacity((short) 50)
                        .build())
                .build());
        listViewFactory.addItemAction(ListViewConditionalItemValueActionDto.builder()
                .compareValuePath(KokuDocumentDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("ARROW_LEFT_START_ON_RECTANGLE")
                        .url(DOCUMENT_ID_URL + "/restore")
                        .params(Arrays.asList(ListViewCallHttpListValueActionParamDto.builder()
                                .param(DOCUMENT_ID_PARAM)
                                .valueReference(idSourcePathFieldRef)
                                .build()))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.PUT)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Kunde wiederherstellen")
                                .content(NAME_PARAM + " wiederherstellen?")
                                .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .valueReference(nameFieldRef)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text(NAME_PARAM + " wurde erfolgreich wiederhergestellt")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                                .param(NAME_PARAM)
                                                .valueReference(nameFieldRef)
                                                .build()))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuDocumentDto.Fields.id)
                                        .valueMapping(Map.of(KokuDocumentDto.Fields.deleted, deletedSourceRef))
                                        .build()))
                        .failEvents(Arrays.asList(ListViewNotificationEvent.builder()
                                .text(NAME_PARAM + " konnte nicht wiederhergestellt werden")
                                .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .valueReference(nameFieldRef)
                                        .build()))
                                .build()))
                        .build())
                .negativeAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("TRASH")
                        .url(DOCUMENT_ID_URL)
                        .params(Arrays.asList(ListViewCallHttpListValueActionParamDto.builder()
                                .param(DOCUMENT_ID_PARAM)
                                .valueReference(idSourcePathFieldRef)
                                .build()))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.DELETE)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Dokument löschen")
                                .content(NAME_PARAM + " als gelöscht markieren?")
                                .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .valueReference(nameFieldRef)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text(NAME_PARAM + " erfolgreich als gelöscht markiert")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                                .param(NAME_PARAM)
                                                .valueReference(nameFieldRef)
                                                .build()))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuDocumentDto.Fields.id)
                                        .valueMapping(Map.of(KokuDocumentDto.Fields.deleted, deletedSourceRef))
                                        .build()))
                        .failEvents(Arrays.asList(ListViewNotificationEvent.builder()
                                .text(NAME_PARAM + " konnte nicht als gelöscht markiert werden")
                                .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                        .param(NAME_PARAM)
                                        .valueReference(nameFieldRef)
                                        .build()))
                                .build()))
                        .build())
                .build());

        listViewFactory.addGlobalEventListener(ListViewEventPayloadAddItemGlobalEventListenerDto.builder()
                .eventName(DOCUMENT_CREATED_EVENT)
                .idPath(KokuDocumentDto.Fields.id)
                .valueMapping(Map.of(
                        KokuDocumentDto.Fields.name, nameFieldRef,
                        KokuDocumentDto.Fields.deleted, deletedSourceRef))
                .build());
        listViewFactory.addGlobalEventListener(ListViewEventPayloadItemUpdateGlobalEventListenerDto.builder()
                .eventName(DOCUMENT_UPDATED_EVENT)
                .idPath(KokuDocumentDto.Fields.id)
                .valueMapping(Map.of(
                        KokuDocumentDto.Fields.name, nameFieldRef,
                        KokuDocumentDto.Fields.deleted, deletedSourceRef))
                .build());

        return listViewFactory.create();
    }

    @GetMapping("/documents/capture/list")
    public ListViewDto getCaptureListView(
            @RequestParam(value = "context", required = false) String context,
            @RequestParam(value = "submitUrl") String submitUrl) {
        final ListViewFactory listViewFactory =
                new ListViewFactory(new DefaultListViewContentIdGenerator(), KokuDocumentDto.Fields.id);

        listViewFactory.addField(
                KokuDocumentDto.Fields.name,
                ListViewInputFieldDto.builder().label("Name").build());
        final ListViewSourcePathReference idSourcePathFieldRef =
                listViewFactory.addSourcePath(KokuDocumentDto.Fields.id);

        listViewFactory.addFilter(
                KokuDocumentDto.Fields.deleted,
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

        listViewFactory.setItemClickAction(ListViewItemClickOpenRoutedContentActionDto.builder()
                .route(DOCUMENT_ID_PARAM)
                .params(Arrays.asList(ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                        .param(DOCUMENT_ID_PARAM)
                        .valueReference(idSourcePathFieldRef)
                        .build()))
                .build());
        listViewFactory.addRoutedContent(ListViewRoutedContentDto.builder()
                .route(DOCUMENT_ID_PARAM)
                .itemId(DOCUMENT_ID_PARAM)
                .inlineContent(ListViewHeaderContentDto.builder()
                        .sourceUrl(DOCUMENT_ID_URL)
                        .titlePath(KokuDocumentDto.Fields.name)
                        .content(ListViewDocumentFormContentDto.builder()
                                .documentUrl(DOCUMENT_ID_URL)
                                .submitUrl(submitUrl)
                                .onSubmitEvents(Arrays.asList(
                                        ListViewDocumentFormContentAfterSavePropagateGlobalEventDto.builder()
                                                .eventName("document-captured")
                                                .build()))
                                .build())
                        .build())
                .build());

        return listViewFactory.create();
    }

    @PostMapping("/documents/query")
    public ListPage findAll(@RequestBody(required = false) final ListQuery predicate) {
        final QDocument qClazz = QDocument.document;

        final ListQueryFactory<Document> listQueryFactory =
                new ListQueryFactory<>(this.entityManager, qClazz, qClazz.id, predicate);

        listQueryFactory.setDefaultOrder(qClazz.recorded.asc());

        listQueryFactory.addFetchExpr(KokuDocumentDto.Fields.id, qClazz.id);
        listQueryFactory.addFetchExpr(KokuDocumentDto.Fields.name, qClazz.name);
        listQueryFactory.addFetchExpr(KokuDocumentDto.Fields.deleted, qClazz.deleted);

        return listQueryFactory.create();
    }

    @GetMapping(value = "/documents/{id}")
    public KokuDocumentDto read(@PathVariable("id") Long id) {
        final Document document = this.documentRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        return this.transformer.transformToDto(document);
    }

    @PutMapping(value = "/documents/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuDocumentDto update(
            @PathVariable("id") Long id,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate,
            @RequestBody KokuDocumentDto updatedDto) {
        final Document document = this.entityManager.getReference(Document.class, id);
        if (!Boolean.TRUE.equals(forceUpdate) && !document.getVersion().equals(updatedDto.getVersion())) {
            throw new KokuBusinessExceptionWithConfirmationMessage(KokuBusinessErrorWithConfirmationMessageDto.builder()
                    .headline("Konflikt")
                    .confirmationMessage("Das Dokument wurde zwischenzeitlich bearbeitet.\n"
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
                            .endpointUrl(
                                    String.format("services/documents/documents/%s?forceUpdate=%s", id, Boolean.TRUE))
                            .build())
                    .button(KokuBusinessExceptionCloseButtonDto.builder()
                            .text("Abbrechen")
                            .title("Abbruch")
                            .build())
                    .build());
        }
        this.transformer.transformToEntity(document, updatedDto);
        this.entityManager.flush();
        return this.transformer.transformToDto(document);
    }

    @DeleteMapping(value = "/documents/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuDocumentDto delete(@PathVariable("id") Long id) {
        final Document document = this.entityManager.getReference(Document.class, id);
        if (document.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document is not deletable");
        }
        document.setDeleted(true);
        this.entityManager.flush();
        return this.transformer.transformToDto(document);
    }

    @PutMapping(value = "/documents/{id}/restore")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuDocumentDto restore(@PathVariable("id") Long id) {
        final Document document = this.entityManager.getReference(Document.class, id);
        if (!document.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document is not restorable");
        }
        document.setDeleted(false);
        this.entityManager.flush();
        return this.transformer.transformToDto(document);
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public KokuDocumentDto create(@RequestBody KokuDocumentDto newDto) {
        final Document newDocument = this.transformer.transformToEntity(new Document(), newDto);
        final Document savedDocument = this.documentRepository.saveAndFlush(newDocument);
        return this.transformer.transformToDto(savedDocument);
    }
}
