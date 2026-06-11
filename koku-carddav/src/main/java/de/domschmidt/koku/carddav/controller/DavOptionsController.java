package de.domschmidt.koku.carddav.controller;

import de.domschmidt.koku.carddav.http.DavHttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DavOptionsController {

    @RequestMapping(
            value = {"/principals/**", "/addressbook/**", "/calendars/**", "/"},
            method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> options() {
        return ResponseEntity.noContent()
                .header(DavHttpHeaders.DAV, DavHttpHeaders.DAV_COMPLIANCE)
                .header(DavHttpHeaders.MS_AUTHOR_VIA, DavHttpHeaders.MS_AUTHOR_VIA_VALUE)
                .header(DavHttpHeaders.ALLOW, DavHttpHeaders.ALLOW_VALUE)
                .build();
    }
}
