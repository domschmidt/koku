package de.domschmidt.koku.carddav.controller;

import de.domschmidt.koku.carddav.model.DavMethod;
import de.domschmidt.koku.carddav.service.CardDavService;
import de.domschmidt.koku.carddav.xml.DavXmlReader;
import de.domschmidt.koku.carddav.xml.DavXmlWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class RootController extends DavControllerSupport {

    private final CardDavService cardDavService;

    public RootController(
            final DavXmlReader davXmlReader, final DavXmlWriter davXmlWriter, final CardDavService cardDavService) {
        super(davXmlReader, davXmlWriter);
        this.cardDavService = cardDavService;
    }

    @RequestMapping
    public ResponseEntity<String> rootRequest(final HttpServletRequest request) {
        return multistatus(request, Set.of(DavMethod.PROPFIND), cardDavService::handleRoot);
    }
}
