package de.domschmidt.koku.file.controller;

import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.formular.factory.DefaultViewContentIdGenerator;
import de.domschmidt.formular.factory.FormViewFactory;
import de.domschmidt.koku.dto.file.KokuFileDto;
import de.domschmidt.koku.dto.formular.containers.grid.GridContainer;
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
import de.domschmidt.koku.dto.formular.fields.select.SelectFormularField;
import de.domschmidt.koku.dto.formular.fields.select.SelectFormularFieldPossibleValue;
import de.domschmidt.koku.dto.list.fields.input.ListViewInputFieldDto;
import de.domschmidt.koku.dto.list.fields.input.ListViewInputFieldTypeEnumDto;
import de.domschmidt.koku.dto.list.filters.ListViewToggleFilterDefaultStateEnum;
import de.domschmidt.koku.dto.list.filters.ListViewToggleFilterDto;
import de.domschmidt.koku.dto.list.items.style.ListViewConditionalItemValueStylingDto;
import de.domschmidt.koku.dto.list.items.style.ListViewItemStylingDto;
import de.domschmidt.koku.file.kafka.customers.service.CustomerKTableProcessor;
import de.domschmidt.koku.file.persistence.File;
import de.domschmidt.koku.file.persistence.FileRepository;
import de.domschmidt.koku.file.persistence.QFile;
import de.domschmidt.koku.file.transformer.FileToFileDtoTransformer;
import de.domschmidt.list.dto.response.ListViewDto;
import de.domschmidt.list.dto.response.ListViewSourcePathReference;
import de.domschmidt.list.dto.response.actions.*;
import de.domschmidt.list.dto.response.events.ListViewEventPayloadAddItemGlobalEventListenerDto;
import de.domschmidt.list.dto.response.events.ListViewEventPayloadOpenRoutedContentGlobalEventListenerDto;
import de.domschmidt.list.dto.response.events.ListViewEventPayloadOpenRoutedContentGlobalEventListenerParamDto;
import de.domschmidt.list.dto.response.events.ListViewEventPayloadSearchTermGlobalEventListenerDto;
import de.domschmidt.list.dto.response.fields.ListViewFieldReference;
import de.domschmidt.list.dto.response.inline_content.ListViewRoutedContentDto;
import de.domschmidt.list.dto.response.inline_content.formular.ListViewFileViewerContentDto;
import de.domschmidt.list.dto.response.inline_content.header.ListViewHeaderContentDto;
import de.domschmidt.list.dto.response.inline_content.list.AbstractListViewListContentContextDto;
import de.domschmidt.list.dto.response.inline_content.list.EndpointListViewContextMethodEnum;
import de.domschmidt.list.dto.response.inline_content.list.EndpointListViewListContentContextDto;
import de.domschmidt.list.dto.response.inline_content.list.ListViewListContentDto;
import de.domschmidt.list.dto.response.items.ListViewRoutedDummyItemDto;
import de.domschmidt.list.dto.response.items.actions.ListViewConditionalItemValueActionDto;
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
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping()
@Slf4j
@RequiredArgsConstructor
public class FileController {
    private final EntityManager entityManager;
    private final FileRepository fileRepository;
    private final FileToFileDtoTransformer transformer;
    private final CustomerKTableProcessor customerKTableProcessor;

    @GetMapping("/files/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory(
                new DefaultViewContentIdGenerator(),
                GridContainer.builder().cols(1).build());

        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuFileDto.Fields.filename)
                .label("Dateiname")
                .readonly(true)
                .build());
        formFactory.addField(SelectFormularField.builder()
                .valuePath(KokuFileDto.Fields.customerId)
                .label("Kunde")
                .possibleValues(StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                        this.customerKTableProcessor
                                                .getCustomers()
                                                .all(),
                                        Spliterator.DISTINCT),
                                false)
                        .map(customer -> SelectFormularFieldPossibleValue.builder()
                                .id(customer.key + "")
                                .text(Stream.of(customer.value.getFirstname(), customer.value.getLastname())
                                        .filter(s -> s != null && !s.isEmpty())
                                        .collect(Collectors.joining(", ")))
                                .disabled(customer.value.getDeleted())
                                .build())
                        .toList())
                .label("Kunde")
                .readonly(true)
                .build());

        return formFactory.create();
    }

    @GetMapping("/files/list")
    public ListViewDto getListView(
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "contextEndpointUrl", required = false) String contextEndpointUrl,
            @RequestParam(value = "contextEndpointMethod", required = false) String contextEndpointMethod) {
        final ListViewFactory listViewFactory =
                new ListViewFactory(new DefaultListViewContentIdGenerator(), KokuFileDto.Fields.id);

        final ListViewFieldReference filenameFieldRef = listViewFactory.addField(
                KokuFileDto.Fields.filename,
                ListViewInputFieldDto.builder().label("Dateiname").build());
        final ListViewFieldReference recordedFieldRef = listViewFactory.addField(
                KokuFileDto.Fields.recorded,
                ListViewInputFieldDto.builder()
                        .label("Erstelldatum")
                        .type(ListViewInputFieldTypeEnumDto.DATETIME)
                        .build());
        final ListViewSourcePathReference idSourcePathFieldRef = listViewFactory.addSourcePath(KokuFileDto.Fields.id);
        final ListViewSourcePathReference deletedSourceRef = listViewFactory.addSourcePath(KokuFileDto.Fields.deleted);
        listViewFactory.addSourcePath(KokuFileDto.Fields.customerId);

        listViewFactory.addFilter(
                KokuFileDto.Fields.deleted,
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
                .route("capture-barcode")
                .icon("BARCODE")
                .build());
        listViewFactory.addGlobalEventListener(ListViewEventPayloadSearchTermGlobalEventListenerDto.builder()
                .eventName("barcode-captured")
                .build());
        listViewFactory.addRoutedContent(ListViewRoutedContentDto.builder()
                .route("capture-barcode")
                .itemId(":fileId")
                .modalContent(ListViewHeaderContentDto.builder()
                        .title("Barcode Scannen")
                        .content(ListViewBarcodeContentDto.builder()
                                .onCaptureEvents(Arrays.asList(
                                        ListViewBarcodeContentDtoAfterCapturePropagateGlobalEventDto.builder()
                                                .eventName("barcode-captured")
                                                .build()))
                                .build())
                        .build())
                .build());

        listViewFactory.addAction(ListViewOpenRoutedContentActionDto.builder()
                .route("capture")
                .icon("CAPTURE")
                .build());
        listViewFactory.addRoutedItem(ListViewRoutedDummyItemDto.builder()
                .route("capture")
                .text("Neues Dokument")
                .build());
        listViewFactory.addGlobalEventListener(ListViewEventPayloadAddItemGlobalEventListenerDto.builder()
                .eventName("document-captured")
                .idPath(KokuFileDto.Fields.id)
                .valueMapping(Map.of(
                        KokuFileDto.Fields.filename, filenameFieldRef,
                        KokuFileDto.Fields.recorded, recordedFieldRef))
                .build());
        listViewFactory.addGlobalEventListener(ListViewEventPayloadOpenRoutedContentGlobalEventListenerDto.builder()
                .eventName("document-captured")
                .route(":fileId")
                .params(Arrays.asList(ListViewEventPayloadOpenRoutedContentGlobalEventListenerParamDto.builder()
                        .param(":fileId")
                        .valuePath(KokuFileDto.Fields.id)
                        .build()))
                .build());

        Map<String, AbstractListViewListContentContextDto> context = new HashMap<>();
        if (customerId != null && contextEndpointUrl != null) {
            context.put(
                    "customer",
                    EndpointListViewListContentContextDto.builder()
                            .endpointMethod(
                                    contextEndpointMethod != null
                                            ? EndpointListViewContextMethodEnum.valueOf(contextEndpointMethod)
                                            : EndpointListViewContextMethodEnum.GET)
                            .endpointUrl(contextEndpointUrl)
                            .build());
        }

        List<String> listUrlAppendix = new ArrayList<>();
        if (customerId != null) {
            listUrlAppendix.add("customerId=" + customerId);
        }

        listViewFactory.addRoutedContent(ListViewRoutedContentDto.builder()
                .route("capture")
                .itemId(":fileId")
                .modalContent(ListViewHeaderContentDto.builder()
                        .title("Dokument Erfassen")
                        .content(ListViewListContentDto.builder()
                                .listUrl("services/documents/documents/capture/list?submitUrl=services/files/files%3F"
                                        + String.join("%26", listUrlAppendix))
                                .sourceUrl("services/documents/documents/query")
                                .context(context)
                                .maxWidthInPx(9999)
                                .build())
                        .build())
                .build());

        listViewFactory.setItemClickAction(ListViewItemClickOpenRoutedContentActionDto.builder()
                .route(":fileId")
                .params(Arrays.asList(ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                        .param(":fileId")
                        .valueReference(idSourcePathFieldRef)
                        .build()))
                .build());
        listViewFactory.addRoutedContent(ListViewRoutedContentDto.builder()
                .route(":fileId")
                .itemId(":fileId")
                .inlineContent(ListViewHeaderContentDto.builder()
                        .sourceUrl("services/files/files/:fileId")
                        .titlePath(KokuFileDto.Fields.filename)
                        .content(ListViewFileViewerContentDto.builder()
                                .sourceUrl("services/files/files/:fileId")
                                .fileUrl("services/files/files/:fileId/content")
                                .mimeTypeSourcePath(KokuFileDto.Fields.mimeType)
                                .build())
                        .build())
                .build());
        listViewFactory.addGlobalItemStyling(ListViewConditionalItemValueStylingDto.builder()
                .compareValuePath(KokuFileDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveStyling(ListViewItemStylingDto.builder()
                        .lineThrough(true)
                        .opacity((short) 50)
                        .build())
                .build());
        listViewFactory.addItemAction(ListViewConditionalItemValueActionDto.builder()
                .compareValuePath(KokuFileDto.Fields.deleted)
                .expectedValue(Boolean.TRUE)
                .positiveAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("ARROW_LEFT_START_ON_RECTANGLE")
                        .url("services/files/files/:fileId/restore")
                        .params(Arrays.asList(ListViewCallHttpListValueActionParamDto.builder()
                                .param(":fileId")
                                .valueReference(idSourcePathFieldRef)
                                .build()))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.PUT)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Datei wiederherstellen")
                                .content(":name wiederherstellen?")
                                .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                        .param(":name")
                                        .valueReference(filenameFieldRef)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text(":name wurde erfolgreich wiederhergestellt")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                                .param(":name")
                                                .valueReference(filenameFieldRef)
                                                .build()))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuFileDto.Fields.id)
                                        .valueMapping(Map.of(KokuFileDto.Fields.deleted, deletedSourceRef))
                                        .build()))
                        .failEvents(Arrays.asList(ListViewNotificationEvent.builder()
                                .text(":name konnte nicht wiederhergestellt werden")
                                .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                        .param(":name")
                                        .valueReference(filenameFieldRef)
                                        .build()))
                                .build()))
                        .build())
                .negativeAction(ListViewCallHttpListItemActionDto.builder()
                        .icon("TRASH")
                        .url("services/files/files/:fileId")
                        .params(Arrays.asList(ListViewCallHttpListValueActionParamDto.builder()
                                .param(":fileId")
                                .valueReference(idSourcePathFieldRef)
                                .build()))
                        .method(ListViewCallHttpListItemActionMethodEnumDto.DELETE)
                        .userConfirmation(ListViewUserConfirmationDto.builder()
                                .headline("Datei löschen")
                                .content(":name als gelöscht markieren?")
                                .params(Arrays.asList(ListViewUserConfirmationValueParamDto.builder()
                                        .param(":name")
                                        .valueReference(filenameFieldRef)
                                        .build()))
                                .build())
                        .successEvents(Arrays.asList(
                                ListViewNotificationEvent.builder()
                                        .text(":name erfolgreich als gelöscht markiert")
                                        .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                        .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                                .param(":name")
                                                .valueReference(filenameFieldRef)
                                                .build()))
                                        .build(),
                                ListViewEventPayloadUpdateActionEventDto.builder()
                                        .idPath(KokuFileDto.Fields.id)
                                        .valueMapping(Map.of(KokuFileDto.Fields.deleted, deletedSourceRef))
                                        .build()))
                        .failEvents(Arrays.asList(ListViewNotificationEvent.builder()
                                .text(":name konnte nicht als gelöscht markiert werden")
                                .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                .params(Arrays.asList(ListViewNotificationEventValueParamDto.builder()
                                        .param(":name")
                                        .valueReference(filenameFieldRef)
                                        .build()))
                                .build()))
                        .build())
                .build());

        return listViewFactory.create();
    }

    @PostMapping(
            value = {
                "/files/query",
            })
    public ListPage findAll(
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestBody(required = false) final ListQuery predicate) {
        final QFile qClazz = QFile.file;

        final ListQueryFactory<File> listQueryFactory =
                new ListQueryFactory<>(this.entityManager, qClazz, qClazz.id, predicate);

        if (customerId != null) {
            listQueryFactory.addDefaultFilter(qClazz.customerId.eq(customerId));
        }

        listQueryFactory.setDefaultOrder(qClazz.recorded.desc());

        listQueryFactory.addFetchExpr(KokuFileDto.Fields.id, qClazz.id);
        listQueryFactory.addFetchExpr(KokuFileDto.Fields.deleted, qClazz.deleted);
        listQueryFactory.addFetchExpr(KokuFileDto.Fields.filename, qClazz.filename);
        listQueryFactory.addFetchExpr(KokuFileDto.Fields.mimeType, qClazz.mimeType);
        listQueryFactory.addFetchExpr(KokuFileDto.Fields.size, qClazz.size);
        listQueryFactory.addFetchExpr(KokuFileDto.Fields.customerId, qClazz.customerId);
        listQueryFactory.addFetchExpr(KokuFileDto.Fields.updated, qClazz.updated);
        listQueryFactory.addFetchExpr(KokuFileDto.Fields.recorded, qClazz.recorded);

        return listQueryFactory.create();
    }

    @GetMapping(value = "/files/{fileId}")
    public KokuFileDto read(@PathVariable("fileId") UUID fileId) {
        final File file = this.fileRepository.getReferenceById(fileId);
        return this.transformer.transformToDto(file);
    }

    @GetMapping(value = "/files/{fileId}/content")
    public ResponseEntity<Resource> readFile(@PathVariable("fileId") UUID fileId) {
        final File file = this.fileRepository.getReferenceById(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getMimeType()))
                .body(new ByteArrayResource(file.getContent()));
    }

    @DeleteMapping(value = "/files/{fileId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuFileDto delete(@PathVariable("fileId") UUID fileId) {
        final File file = this.entityManager.getReference(File.class, fileId);
        if (file.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is not deletable");
        }
        file.setDeleted(true);
        this.entityManager.flush();
        return this.transformer.transformToDto(file);
    }

    @PutMapping(value = "/files/{fileId}/restore")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuFileDto restore(@PathVariable("fileId") UUID fileId) {
        final File file = this.entityManager.getReference(File.class, fileId);
        if (!file.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CustomerUpload is not restorable");
        }
        file.setDeleted(false);
        this.entityManager.flush();
        return this.transformer.transformToDto(file);
    }

    @PostMapping("/files")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public KokuFileDto create(
            @RequestPart("file") final MultipartFile file,
            @RequestParam(value = "id", required = false) UUID id,
            @RequestParam(value = "customerId", required = false) Long customerId)
            throws IOException {
        final File savedFile = this.fileRepository.saveAndFlush(new File(
                id, file.getOriginalFilename(), customerId, file.getContentType(), file.getBytes(), file.getSize()));
        return this.transformer.transformToDto(savedFile);
    }
}
