package de.domschmidt.koku.controller.documents;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.formular.FormularDto;
import de.domschmidt.koku.dto.formular.FormularReplacementTokenDto;
import de.domschmidt.koku.persistence.model.dynamic_documents.DynamicDocument;
import de.domschmidt.koku.service.impl.DocumentService;
import de.domschmidt.koku.service.searchoptions.DocumentSearchOptions;
import de.domschmidt.koku.transformer.DynamicDocumentToFormularDtoTransformer;
import de.domschmidt.koku.utils.DocumentCheckboxReplacementToken;
import de.domschmidt.koku.utils.DocumentDateReplacementToken;
import de.domschmidt.koku.utils.DocumentQrCodeReplacementToken;
import de.domschmidt.koku.utils.DocumentTextReplacementToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/documents")
public class DocumentController extends AbstractController<DynamicDocument, FormularDto, DocumentSearchOptions> {

    @Autowired
    public DocumentController(final DocumentService documentService,
                              final DynamicDocumentToFormularDtoTransformer transformer) {
        super(documentService, transformer);
    }

    @GetMapping
    public List<FormularDto> findAll(final DocumentSearchOptions documentSearchOptions) {
        return super.findAll(documentSearchOptions);
    }
    @GetMapping(value = "/{id}")
    public FormularDto findByIdTransformed(@PathVariable("id") Long id) {
        return super.findByIdTransformed(id);
    }

    @GetMapping(value = "/replacementtoken/text")
    public List<FormularReplacementTokenDto> getAllTextReplacementTokenPresets() {
        final List<FormularReplacementTokenDto> result = new ArrayList<>();
        for (final DocumentTextReplacementToken currentReplacementToken : DocumentTextReplacementToken.values()) {
            result.add(FormularReplacementTokenDto.builder()
                    .replacementToken(currentReplacementToken.getReplacementString())
                    .tokenName(currentReplacementToken.getTokenName())
                    .build());
        }
        return result;
    }
    @GetMapping(value = "/replacementtoken/date")
    public List<FormularReplacementTokenDto> getAllDateReplacementTokenPresets() {
        final List<FormularReplacementTokenDto> result = new ArrayList<>();
        for (final DocumentDateReplacementToken currentReplacementToken : DocumentDateReplacementToken.values()) {
            result.add(FormularReplacementTokenDto.builder()
                    .replacementToken(currentReplacementToken.getReplacementString())
                    .tokenName(currentReplacementToken.getTokenName())
                    .build());
        }
        return result;
    }

    @GetMapping(value = "/replacementtoken/checkbox")
    public List<FormularReplacementTokenDto> getAllCheckboxReplacementTokenPresets() {
        final List<FormularReplacementTokenDto> result = new ArrayList<>();
        for (final DocumentCheckboxReplacementToken currentReplacementToken : DocumentCheckboxReplacementToken.values()) {
            result.add(FormularReplacementTokenDto.builder()
                    .replacementToken(currentReplacementToken.getReplacementString())
                    .tokenName(currentReplacementToken.getTokenName())
                    .build());
        }
        return result;
    }

    @GetMapping(value = "/replacementtoken/qrcode")
    public List<FormularReplacementTokenDto> getAllQrCodeReplacementTokenPresets() {
        final List<FormularReplacementTokenDto> result = new ArrayList<>();
        for (final DocumentQrCodeReplacementToken currentReplacementToken : DocumentQrCodeReplacementToken.values()) {
            result.add(FormularReplacementTokenDto.builder()
                    .replacementToken(currentReplacementToken.getReplacementString())
                    .tokenName(currentReplacementToken.getTokenName())
                    .build());
        }
        return result;
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void update(@PathVariable("id") Long id, @RequestBody FormularDto updatedDto) {
        final DynamicDocument model = this.transformer.transformToEntity(updatedDto);
        this.service.update(model);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void delete(@PathVariable("id") Long id) {
        final DynamicDocument model = this.service.findById(id);
        model.setDeleted(true);
        this.service.update(model);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public FormularDto create(@RequestBody FormularDto newDto) {
        final DynamicDocument model = this.transformer.transformToEntity(newDto);
        final DynamicDocument savedModel = this.service.create(model);
        return this.transformer.transformToDto(savedModel);
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public FormularDto createDuplicate(@PathVariable("id") Long id) {
        final DynamicDocument documentToBeCopied = this.service.findById(id);
        final DynamicDocument documentCopy = new DynamicDocument(documentToBeCopied);
        final DynamicDocument savedModel = this.service.create(documentCopy);
        return this.transformer.transformToDto(savedModel);
    }


}
