package de.domschmidt.koku.carddav.controller;

import de.domschmidt.koku.carddav.http.DavHttpHeaders;
import de.domschmidt.koku.carddav.model.DavMethod;
import de.domschmidt.koku.carddav.model.DavMultiStatus;
import de.domschmidt.koku.carddav.model.DavRequest;
import de.domschmidt.koku.carddav.xml.DavXmlReader;
import de.domschmidt.koku.carddav.xml.DavXmlWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

abstract class DavControllerSupport {

    private static final Logger LOG = LoggerFactory.getLogger(DavControllerSupport.class);

    private final DavXmlReader davXmlReader;
    private final DavXmlWriter davXmlWriter;

    protected DavControllerSupport(final DavXmlReader davXmlReader, final DavXmlWriter davXmlWriter) {
        this.davXmlReader = davXmlReader;
        this.davXmlWriter = davXmlWriter;
    }

    protected ResponseEntity<String> multistatus(
            final HttpServletRequest request,
            final Set<DavMethod> allowedMethods,
            final Function<DavRequest, DavMultiStatus> handler) {
        try {
            final DavRequest davRequest = davXmlReader.read(request);
            if (!allowedMethods.contains(davRequest.method())) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
            }
            return ResponseEntity.status(HttpStatus.MULTI_STATUS)
                    .contentType(MediaType.APPLICATION_XML)
                    .cacheControl(CacheControl.noStore())
                    .header(DavHttpHeaders.DAV, DavHttpHeaders.DAV_COMPLIANCE)
                    .header(DavHttpHeaders.MS_AUTHOR_VIA, DavHttpHeaders.MS_AUTHOR_VIA_VALUE)
                    .header(DavHttpHeaders.ALLOW, DavHttpHeaders.ALLOW_VALUE)
                    .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                    .body(davXmlWriter.write(handler.apply(davRequest)));
        } catch (final ResponseStatusException e) {
            throw e;
        } catch (final IOException e) {
            LOG.error("Unable to serialize DAV response", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to serialize DAV response", e);
        }
    }
}
