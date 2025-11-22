package de.domschmidt.koku.user.controller;

import com.querydsl.jpa.JPQLTemplates;
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
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
import de.domschmidt.koku.dto.formular.fields.picture_upload.PictureUploadFormularField;
import de.domschmidt.koku.dto.formular.fields.select.SelectFormularField;
import de.domschmidt.koku.dto.formular.fields.select.SelectFormularFieldPossibleValue;
import de.domschmidt.koku.dto.list.items.style.ListViewConditionalItemValueStylingDto;
import de.domschmidt.koku.dto.list.items.style.ListViewItemStylingDto;
import de.domschmidt.koku.dto.user.KokuUserDto;
import de.domschmidt.koku.dto.user.KokuUserSummaryDto;
import de.domschmidt.koku.user.kafka.users.service.UserKafkaService;
import de.domschmidt.koku.user.persistence.QUser;
import de.domschmidt.koku.user.persistence.QUserRegion;
import de.domschmidt.koku.user.persistence.User;
import de.domschmidt.koku.user.persistence.UserRepository;
import de.domschmidt.koku.user.transformer.UserToKokuUserDtoTransformer;
import de.domschmidt.koku.user.transformer.UserToKokuUserSummaryDtoTransformer;
import de.domschmidt.list.dto.response.ListViewDto;
import de.domschmidt.list.dto.response.ListViewSourcePathReference;
import de.domschmidt.list.dto.response.actions.ListViewUserConfirmationDto;
import de.domschmidt.list.dto.response.actions.ListViewUserConfirmationValueParamDto;
import de.domschmidt.list.dto.response.events.ListViewEventPayloadItemUpdateGlobalEventListenerDto;
import de.domschmidt.list.dto.response.fields.ListViewFieldReference;
import de.domschmidt.list.dto.response.fields.input.ListViewInputFieldDto;
import de.domschmidt.list.dto.response.inline_content.ListViewRoutedContentDto;
import de.domschmidt.list.dto.response.inline_content.formular.ListViewFormularContentDto;
import de.domschmidt.list.dto.response.inline_content.formular.ListViewInlineFormularContentAfterSavePropagateGlobalEventDto;
import de.domschmidt.list.dto.response.inline_content.header.ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto;
import de.domschmidt.list.dto.response.inline_content.header.ListViewHeaderContentDto;
import de.domschmidt.list.dto.response.items.actions.ListViewConditionalItemValueActionDto;
import de.domschmidt.list.dto.response.items.actions.ListViewFormularActionSubmitMethodEnumDto;
import de.domschmidt.list.dto.response.items.actions.call_http.ListViewCallHttpListItemActionDto;
import de.domschmidt.list.dto.response.items.actions.call_http.ListViewCallHttpListItemActionMethodEnumDto;
import de.domschmidt.list.dto.response.items.actions.call_http.ListViewCallHttpListValueActionParamDto;
import de.domschmidt.list.dto.response.items.actions.inline_content.ListViewItemClickOpenRoutedContentActionDto;
import de.domschmidt.list.dto.response.items.actions.inline_content.ListViewItemClickOpenRoutedContentActionItemValueParamDto;
import de.domschmidt.list.dto.response.items.actions.inline_content.ListViewItemClickPropagateGlobalEventActionDto;
import de.domschmidt.list.dto.response.items.preview.ListViewItemPreviewAvatarDto;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final UserToKokuUserDtoTransformer transformer;
    private final UserKafkaService userKafkaService;

    @Autowired
    public UserController(
            final EntityManager entityManager,
            final UserRepository userRepository,
            final UserToKokuUserDtoTransformer transformer,
            final UserKafkaService userKafkaService
    ) {
        this.entityManager = entityManager;
        this.userRepository = userRepository;
        this.transformer = transformer;
        this.userKafkaService = userKafkaService;
    }

    @GetMapping("/form")
    public FormViewDto getFormularView() {
        final FormViewFactory formFactory = new FormViewFactory(
                new DefaultViewContentIdGenerator(),
                GridContainer.builder()
                        .cols(1)
                        .build()
        );

        formFactory.addField(PictureUploadFormularField.builder()
                .valuePath(KokuUserDto.Fields.avatarBase64)
                .label("Profilbild")
                .build()
        );

        formFactory.addField(InputFormularField.builder()
                .valuePath(KokuUserDto.Fields.fullname)
                .label("Vollständiger Name")
                .disabled(true)
                .build()
        );

        formFactory.addField(SelectFormularField.builder()
                .valuePath(KokuUserDto.Fields.regionId)
                .label("Region")
                .possibleValues(new JPAQuery<>(this.entityManager, JPQLTemplates.DEFAULT)
                        .select(QUserRegion.userRegion)
                        .from(QUserRegion.userRegion)
                        .fetch().stream().map(userRegion -> {
                            return SelectFormularFieldPossibleValue.builder()
                                    .id(userRegion.getId() + "")
                                    .text(userRegion.getStateIso() != null ? userRegion.getStateName() : userRegion.getCountryName())
                                    .category(userRegion.getStateIso() != null ? userRegion.getCountryName() : null)
                                    .build();
                        }).toList())
                .build()
        );

        formFactory.addButton(KokuFormButton.builder()
                .buttonType(EnumButtonType.SUBMIT)
                .text("Speichern")
                .title("Jetzt speichern")
                .styles(Arrays.asList(
                        EnumButtonStyle.BLOCK
                ))
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

    @GetMapping("/list")
    public ListViewDto getListView(
            @RequestParam(required = false) boolean selectMode
    ) {
        final ListViewFactory listViewFactory = new ListViewFactory(
                new DefaultListViewContentIdGenerator(),
                KokuUserDto.Fields.id
        );

        final ListViewSourcePathReference idSourcePathRef = listViewFactory.addSourcePath(
                KokuUserDto.Fields.id
        );
        final ListViewSourcePathReference avatarSourcePathRef = listViewFactory.addSourcePath(
                KokuUserDto.Fields.avatarBase64
        );
        final ListViewFieldReference fullnameFieldRef = listViewFactory.addField(
                KokuUserDto.Fields.fullname,
                ListViewInputFieldDto.builder().build()
        );
        final ListViewSourcePathReference deletedSourcePathRef = listViewFactory.addSourcePath(
                KokuUserDto.Fields.deleted
        );
        if (!selectMode) {
            listViewFactory.setItemClickAction(ListViewItemClickOpenRoutedContentActionDto.builder()
                    .route(":userId")
                    .params(Arrays.asList(
                            ListViewItemClickOpenRoutedContentActionItemValueParamDto.builder()
                                    .param(":userId")
                                    .valueReference(idSourcePathRef)
                                    .build()
                    ))
                    .build()
            );
            listViewFactory.addGlobalEventListener(ListViewEventPayloadItemUpdateGlobalEventListenerDto.builder()
                    .eventName("user-updated")
                    .idPath(KokuUserDto.Fields.id)
                    .valueMapping(Map.of(
                            KokuUserDto.Fields.avatarBase64, avatarSourcePathRef,
                            KokuUserDto.Fields.deleted, deletedSourcePathRef
                    ))
                    .build()
            );
            listViewFactory.addRoutedContent(
                    ListViewRoutedContentDto.builder()
                            .route(":userId")
                            .itemId(":userId")
                            .inlineContent(ListViewHeaderContentDto.builder()
                                    .sourceUrl("services/users/users/:userId/summary")
                                    .titlePath(KokuUserSummaryDto.Fields.summary)
                                    .globalEventListeners(Arrays.asList(ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto.builder()
                                            .eventName("user-updated")
                                            .idPath(KokuUserDto.Fields.id)
                                            .titleValuePath(KokuUserDto.Fields.fullname)
                                            .build()
                                    ))
                                    .content(ListViewFormularContentDto.builder()
                                            .formularUrl("services/users/users/form")
                                            .sourceUrl("services/users/users/:userId")
                                            .submitMethod(ListViewFormularActionSubmitMethodEnumDto.PUT)
                                            .maxWidthInPx(600)
                                            .onSaveEvents(Arrays.asList(
                                                    ListViewInlineFormularContentAfterSavePropagateGlobalEventDto.builder()
                                                            .eventName("user-updated")
                                                            .build()
                                            ))
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
            );
            listViewFactory.addGlobalItemStyling(ListViewConditionalItemValueStylingDto.builder()
                    .compareValuePath(KokuUserDto.Fields.deleted)
                    .expectedValue(Boolean.TRUE)
                    .positiveStyling(ListViewItemStylingDto.builder()
                            .lineThrough(true)
                            .opacity((short) 50)
                            .build()
                    )
                    .build()
            );
            listViewFactory.addItemAction(ListViewConditionalItemValueActionDto.builder()
                    .compareValuePath(KokuUserDto.Fields.deleted)
                    .expectedValue(Boolean.TRUE)
                    .positiveAction(ListViewCallHttpListItemActionDto.builder()
                            .icon("ARROW_LEFT_START_ON_RECTANGLE")
                            .url("services/users/users/:userId/restore")
                            .params(Arrays.asList(
                                    ListViewCallHttpListValueActionParamDto.builder()
                                            .param(":userId")
                                            .valueReference(idSourcePathRef)
                                            .build()
                            ))
                            .method(ListViewCallHttpListItemActionMethodEnumDto.PUT)
                            .userConfirmation(ListViewUserConfirmationDto.builder()
                                    .headline("Nutzer wiederherstellen")
                                    .content("Nutzer :name wiederherstellen?")
                                    .params(Arrays.asList(
                                            ListViewUserConfirmationValueParamDto.builder()
                                                    .param(":name")
                                                    .valueReference(fullnameFieldRef)
                                                    .build()
                                    ))
                                    .build()
                            )
                            .successEvents(Arrays.asList(
                                    ListViewNotificationEvent.builder()
                                            .text("Nutzer :name wurde erfolgreich wiederhergestellt")
                                            .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                            .params(Arrays.asList(
                                                    ListViewNotificationEventValueParamDto.builder()
                                                            .param(":name")
                                                            .valueReference(fullnameFieldRef)
                                                            .build()
                                            ))
                                            .build(),
                                    ListViewEventPayloadUpdateActionEventDto.builder()
                                            .idPath(KokuUserDto.Fields.id)
                                            .valueMapping(Map.of(
                                                    KokuUserDto.Fields.deleted, deletedSourcePathRef
                                            ))
                                            .build()
                            ))
                            .failEvents(Arrays.asList(
                                    ListViewNotificationEvent.builder()
                                            .text("Nutzer :name konnte nicht wiederhergestellt werden")
                                            .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                            .params(Arrays.asList(
                                                    ListViewNotificationEventValueParamDto.builder()
                                                            .param(":name")
                                                            .valueReference(fullnameFieldRef)
                                                            .build()
                                            ))
                                            .build()
                            ))
                            .build())
                    .negativeAction(ListViewCallHttpListItemActionDto.builder()
                            .icon("TRASH")
                            .url("services/users/users/:userId")
                            .params(Arrays.asList(
                                    ListViewCallHttpListValueActionParamDto.builder()
                                            .param(":userId")
                                            .valueReference(idSourcePathRef)
                                            .build()
                            ))
                            .method(ListViewCallHttpListItemActionMethodEnumDto.DELETE)
                            .userConfirmation(ListViewUserConfirmationDto.builder()
                                    .headline("Nutzer löschen")
                                    .content("Nutzer :name als gelöscht markieren?")
                                    .params(Arrays.asList(
                                            ListViewUserConfirmationValueParamDto.builder()
                                                    .param(":name")
                                                    .valueReference(fullnameFieldRef)
                                                    .build()
                                    ))
                                    .build()
                            )
                            .successEvents(Arrays.asList(
                                    ListViewNotificationEvent.builder()
                                            .text("Nutzer :name wurde erfolgreich als gelöscht markiert")
                                            .serenity(ListViewNotificationEventSerenityEnumDto.SUCCESS)
                                            .params(Arrays.asList(
                                                    ListViewNotificationEventValueParamDto.builder()
                                                            .param(":name")
                                                            .valueReference(fullnameFieldRef)
                                                            .build()
                                            ))
                                            .build(),
                                    ListViewEventPayloadUpdateActionEventDto.builder()
                                            .idPath(KokuUserDto.Fields.id)
                                            .valueMapping(Map.of(
                                                    KokuUserDto.Fields.deleted, deletedSourcePathRef
                                            ))
                                            .build()
                            ))
                            .failEvents(Arrays.asList(
                                    ListViewNotificationEvent.builder()
                                            .text("Nutzer :name konnte nicht als gelöscht markiert werden")
                                            .serenity(ListViewNotificationEventSerenityEnumDto.ERROR)
                                            .params(Arrays.asList(
                                                    ListViewNotificationEventValueParamDto.builder()
                                                            .param(":name")
                                                            .valueReference(fullnameFieldRef)
                                                            .build()
                                            ))
                                            .build()
                            ))
                            .build()
                    )
                    .build()
            );
        } else {
            listViewFactory.setItemClickAction(ListViewItemClickPropagateGlobalEventActionDto.builder()
                    .eventName("user-selected")
                    .build()
            );
        }

        listViewFactory.setItemPreview(ListViewItemPreviewAvatarDto.builder()
                .valuePath(KokuUserDto.Fields.avatarBase64)
                .build()
        );

        return listViewFactory.create();
    }

    @PostMapping("/query")
    public ListPage findAll(@RequestBody(required = false) final ListQuery predicate) {
        final QUser qClazz = QUser.user;
        final ListQueryFactory<User> listQueryFactory = new ListQueryFactory<>(
                this.entityManager,
                qClazz,
                qClazz.id,
                predicate
        );

        listQueryFactory.addFetchExpr(
                KokuUserDto.Fields.id,
                qClazz.id
        );
        listQueryFactory.addFetchExpr(
                KokuUserDto.Fields.avatarBase64,
                qClazz.avatarBase64
        );
        listQueryFactory.addFetchExpr(
                KokuUserDto.Fields.firstname,
                qClazz.firstname
        );
        listQueryFactory.addFetchExpr(
                KokuUserDto.Fields.lastname,
                qClazz.lastname
        );
        listQueryFactory.addFetchExpr(
                KokuUserDto.Fields.deleted,
                qClazz.deleted
        );
        listQueryFactory.addFetchExpr(
                KokuUserDto.Fields.fullname,
                qClazz.firstname.concat(" ").concat(qClazz.lastname).trim()
        );
        listQueryFactory.addFetchExpr(
                KokuUserDto.Fields.regionId,
                qClazz.region.id
        );

        return listQueryFactory.create();
    }

    @GetMapping("/@self")
    public KokuUserDto getMyDetails(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return read(jwt.getSubject());
    }

    @PutMapping("/@self")
    @Transactional
    public void updateMyDetails(
            @RequestBody KokuUserDto updatedDto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        updateOrCreate(
                jwt.getSubject(),
                true,
                updatedDto
        );
    }

    @PostMapping("/@self/sync")
    @Transactional
    public void syncMyDetails(
            @AuthenticationPrincipal Jwt jwt
    ) {
        final User user = this.userRepository.findById(jwt.getSubject()).orElseGet(() -> this.userRepository.save(new User(jwt.getSubject())));
        boolean dirty = false;
        final String givenName = jwt.getClaimAsString("given_name");
        if (givenName != null && !user.getFirstname().equals(givenName)) {
            user.setFirstname(givenName);
            dirty = true;
        }
        final String familyName = jwt.getClaimAsString("family_name");
        if (familyName != null && !user.getLastname().equals(familyName)) {
            user.setLastname(familyName);
            dirty = true;
        }
        final String name = jwt.getClaimAsString("name");
        if (name != null && !user.getFullname().equals(name)) {
            user.setFullname(name);
            dirty = true;
        }
        if (dirty) {
            sendUserUpdate(user);
        }
    }

    @GetMapping(value = "/{id}")
    public KokuUserDto read(@PathVariable("id") String id) {
        final User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return this.transformer.transformToDto(user);
    }

    @GetMapping(value = "/{id}/summary")
    public KokuUserSummaryDto readSummary(@PathVariable("id") String id) {
        final User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserToKokuUserSummaryDtoTransformer().transformToDto(user);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuUserDto updateOrCreate(
            @PathVariable("id") String id,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate,
            @RequestBody KokuUserDto updatedDto
    ) {
        final User user = this.userRepository.findById(id).orElseGet(() -> this.userRepository.save(new User(id)));
        if (!Boolean.TRUE.equals(forceUpdate) && !user.getVersion().equals(updatedDto.getVersion())) {
            throw new KokuBusinessExceptionWithConfirmationMessage(
                    KokuBusinessExceptionWithConfirmationMessageDto.builder()
                            .headline("Konflikt")
                            .confirmationMessage("Der Nutzer wurde zwischenzeitlich bearbeitet.\nWillst Du die Speicherung dennoch vornehmen?")
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
                                    .endpointUrl(String.format("services/customers/customers/appointments/%s?forceUpdate=%s", id, Boolean.TRUE))
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
        this.transformer.transformToEntity(user, updatedDto);
        this.entityManager.flush();
        sendUserUpdate(user);
        return this.transformer.transformToDto(user);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuUserDto delete(@PathVariable("id") Long id) {
        final User user = this.entityManager.getReference(User.class, id);
        if (user.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not deletable");
        }
        user.setDeleted(true);
        this.entityManager.flush();
        sendUserUpdate(user);
        return this.transformer.transformToDto(user);
    }

    @PutMapping(value = "/{id}/restore")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public KokuUserDto restore(@PathVariable("id") Long id) {
        final User user = this.entityManager.getReference(User.class, id);
        if (!user.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not restorable");
        }
        user.setDeleted(false);
        this.entityManager.flush();
        sendUserUpdate(user);
        return this.transformer.transformToDto(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public KokuUserDto create(@RequestBody KokuUserDto newDto) {
        final User newUser = this.transformer.transformToEntity(new User(), newDto);

        final User savedUser = this.userRepository.saveAndFlush(newUser);
        sendUserUpdate(newUser);
        return this.transformer.transformToDto(savedUser);
    }

    public void sendUserUpdate(final User updatedUser) {
        try {
            this.userKafkaService.sendUser(updatedUser);
        } catch (final ExecutionException | InterruptedException | TimeoutException e) {
            log.error("Unable to export to kafka, due to: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to export to kafka");
        }
    }

}
