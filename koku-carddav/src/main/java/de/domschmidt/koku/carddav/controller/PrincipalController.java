package de.domschmidt.koku.carddav.controller;

import de.domschmidt.koku.carddav.APIConstants;
import de.domschmidt.koku.carddav.DAVConstants;
import de.domschmidt.koku.carddav.helper.DavUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/principals")
@Slf4j
public class PrincipalController {

    @Data
    @Builder
    private static class PrincipalRequest {

        Element responseTag;
    }

    private static final Map<String, Map<String, Consumer<PrincipalRequest>>> PRINCIPAL_PROPS_RESOLVER = Map.of(
            DAVConstants.CARDDAV_NAMESPACE, Map.of(DAVConstants.CARD_PRPOP_ADDRESSBOOK_HOME_SET, (principalRequest) -> {
                final Element outputNode = principalRequest
                        .getResponseTag()
                        .addElement("d:" + DAVConstants.CARD_PRPOP_ADDRESSBOOK_HOME_SET);
                final Element hrefNode = outputNode.addElement("d:href");
                hrefNode.setText(APIConstants.API_BASEPATH + APIConstants.ADDRESSBOOK_PATH + "/koku/");
            }));

    @RequestMapping(
            consumes = {
                MediaType.APPLICATION_XML_VALUE,
                MediaType.TEXT_XML_VALUE,
            },
            produces = MediaType.APPLICATION_XML_VALUE,
            path = "/{username}")
    @ResponseStatus(HttpStatus.MULTI_STATUS)
    public String principalRequest(final HttpServletRequest request, final @PathVariable String username) {
        final Document result = DocumentHelper.createDocument();
        final Element multiStatusResponse = DavUtils.attachMultiStatusResponse(result);

        try {
            final Document document = new SAXReader().read(request.getInputStream());
            final List<Element> requestedProps = DavUtils.parseRequestedDavProps(document);

            if (requestedProps != null) {
                final List<Element> notFoundProps = new ArrayList<>();
                final List<Consumer<PrincipalRequest>> resolvedPropGenerators = new ArrayList<>();
                for (final Element requestedPropElement : requestedProps) {
                    final Map<String, Consumer<PrincipalRequest>> nameSpaceSpecificPropResolvers =
                            PRINCIPAL_PROPS_RESOLVER.get(requestedPropElement.getNamespaceURI());
                    if (nameSpaceSpecificPropResolvers == null) {
                        notFoundProps.add((Element) requestedPropElement.clone());
                    } else {
                        final Consumer<PrincipalRequest> propResolver =
                                nameSpaceSpecificPropResolvers.get(requestedPropElement.getName());
                        if (propResolver == null) {
                            notFoundProps.add((Element) requestedPropElement.clone());
                        } else {
                            resolvedPropGenerators.add(propResolver);
                        }
                    }
                }
                appendResponse(
                        multiStatusResponse,
                        APIConstants.API_BASEPATH + APIConstants.PRINCIPALS_PATH + "/" + username + "/",
                        resolvedPropGenerators,
                        notFoundProps);
            }
        } catch (final DocumentException ue) {
            log.error("Unable to parse requested document", ue);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (final IOException ioe) {
            log.error("Unable to handle input stream", ioe);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            return DavUtils.toXmlString(result);
        } catch (final IOException e) {
            log.error("Unable to write xml response", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static void appendResponse(
            final Element root,
            final String responseHref,
            final List<Consumer<PrincipalRequest>> resolvedPropGenerators,
            final List<Element> notFoundProps) {
        Element response = root.addElement("d:response");
        Element href = response.addElement("d:href");
        href.setText(responseHref);

        if (!resolvedPropGenerators.isEmpty()) {
            Element posPropStat = response.addElement("d:propstat");
            Element prop = posPropStat.addElement("d:prop");
            posPropStat.addElement("d:status").setText("HTTP/1.1 200 OK");
            for (final Consumer<PrincipalRequest> resolvedPropGenerator : resolvedPropGenerators) {
                resolvedPropGenerator.accept(
                        PrincipalRequest.builder().responseTag(prop).build());
            }
        }
        if (!notFoundProps.isEmpty()) {
            Element posPropStat = response.addElement("d:propstat", DAVConstants.DAV_NAMESPACE);
            Element prop = posPropStat.addElement("d:prop", DAVConstants.DAV_NAMESPACE);
            posPropStat.addElement("d:status", DAVConstants.DAV_NAMESPACE).setText("HTTP/1.1 404 Not Found");
            for (final Element notFoundProp : notFoundProps) {
                prop.add(notFoundProp);
            }
        }
    }
}
