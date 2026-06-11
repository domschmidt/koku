package de.domschmidt.koku.dav.controller;

import de.domschmidt.koku.dav.APIConstants;
import de.domschmidt.koku.dav.http.DavMediaTypes;
import de.domschmidt.koku.dav.model.DavMethod;
import de.domschmidt.koku.dav.service.CardDavService;
import de.domschmidt.koku.dav.xml.DavXmlReader;
import de.domschmidt.koku.dav.xml.DavXmlWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(APIConstants.ADDRESSBOOK_PATH)
public class AddressBookController extends DavControllerSupport {

    private final CardDavService cardDavService;

    public AddressBookController(
            final DavXmlReader davXmlReader, final DavXmlWriter davXmlWriter, final CardDavService cardDavService) {
        super(davXmlReader, davXmlWriter);
        this.cardDavService = cardDavService;
    }

    @RequestMapping(
            value = {
                "/{userName}",
                "/{userName}/",
            })
    public ResponseEntity<String> propfindOrReportRequest(
            final HttpServletRequest request,
            final @PathVariable String userName,
            final Authentication authentication) {
        return multistatus(
                request,
                Set.of(DavMethod.PROPFIND, DavMethod.REPORT),
                davRequest -> cardDavService.handleAddressBook(davRequest, authentication.getName()));
    }

    @GetMapping(
            value = {
                "/{userName}/{contactId}.vcf",
            },
            produces = DavMediaTypes.VCARD)
    public ResponseEntity<String> getContact(
            final @PathVariable String userName,
            final @PathVariable long contactId,
            final Authentication authentication) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(DavMediaTypes.VCARD_UTF8))
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.ETAG, cardDavService.getContactEtag(contactId))
                .body(cardDavService.getVCard(contactId));
    }
}
