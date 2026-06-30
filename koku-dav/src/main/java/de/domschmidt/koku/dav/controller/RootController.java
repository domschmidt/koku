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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(APIConstants.ROOT_PATH)
public class RootController extends DavControllerSupport {

    private final CardDavService cardDavService;

    public RootController(
            final DavXmlReader davXmlReader, final DavXmlWriter davXmlWriter, final CardDavService cardDavService) {
        super(davXmlReader, davXmlWriter);
        this.cardDavService = cardDavService;
    }

    @RequestMapping
    public ResponseEntity<String> rootRequest(final HttpServletRequest request, final Authentication authentication) {
        return multistatus(
                request,
                Set.of(DavMethod.PROPFIND),
                davRequest -> cardDavService.handleRoot(davRequest, authentication.getName()));
    }
}
