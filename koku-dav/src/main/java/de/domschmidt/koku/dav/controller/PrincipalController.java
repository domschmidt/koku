package de.domschmidt.koku.dav.controller;

import de.domschmidt.koku.dav.APIConstants;
import de.domschmidt.koku.dav.model.DavMethod;
import de.domschmidt.koku.dav.service.CardDavService;
import de.domschmidt.koku.dav.xml.DavXmlReader;
import de.domschmidt.koku.dav.xml.DavXmlWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(APIConstants.PRINCIPALS_PATH)
public class PrincipalController extends DavControllerSupport {

    private final CardDavService cardDavService;

    public PrincipalController(
            final DavXmlReader davXmlReader, final DavXmlWriter davXmlWriter, final CardDavService cardDavService) {
        super(davXmlReader, davXmlWriter);
        this.cardDavService = cardDavService;
    }

    @DavRequestMapping(value = {"/{username}", "/{username}/"})
    public ResponseEntity<String> principalRequest(
            final HttpServletRequest request,
            final @PathVariable String username,
            final Authentication authentication) {
        return multistatus(
                request,
                Set.of(DavMethod.PROPFIND),
                davRequest -> cardDavService.handlePrincipal(davRequest, authentication.getName()));
    }
}
