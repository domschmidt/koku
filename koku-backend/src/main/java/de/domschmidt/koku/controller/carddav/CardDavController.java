package de.domschmidt.koku.controller.carddav;

import de.domschmidt.koku.configuration.NextCloudConfiguration;
import de.domschmidt.koku.controller.customer.NextcloudPathResolver;
import de.domschmidt.koku.dto.carddav.CardDavInfoDto;
import de.domschmidt.koku.exceptions.KokuCardDavException;
import de.domschmidt.koku.service.ICardDavService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/carddav")
public class CardDavController {

    private final ICardDavService cardDavService;
    private final NextCloudConfiguration nextCloudConfiguration;

    @Autowired
    public CardDavController(
            final ICardDavService cardDavService,
            final NextCloudConfiguration nextCloudConfiguration
    ) {
        this.cardDavService = cardDavService;
        this.nextCloudConfiguration = nextCloudConfiguration;
    }

    @PostMapping("/sync")
    public void sync() {
        try {
            this.cardDavService.syncAllContacts();
        } catch (final KokuCardDavException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/info")
    public CardDavInfoDto getInfo() {
        try {
            final String endpoint = this.nextCloudConfiguration.getNextcloudEndpoint();
            final String user = this.nextCloudConfiguration.getNextcloudUser();
            final String password = this.nextCloudConfiguration.getNextcloudPassword();

            final boolean isProperlyDefined = StringUtils.isNotBlank(endpoint)
                    && StringUtils.isNotBlank(user)
                    && StringUtils.isNotBlank(password);

            if (!isProperlyDefined) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CardDav Konfiguration fehlt");
            }

            final URI endpointUri = new URI(endpoint);
            final String cardDavHost = endpointUri.getScheme() + "://"
                    + endpointUri.getHost()
                    + (endpointUri.getPort() > 0 ? ':' + endpointUri.getPort() : "")
                    + endpointUri.getPath();
            return CardDavInfoDto.builder()
                    .endpointUrl(cardDavHost + new NextcloudPathResolver(endpointUri.getPath()).getPrincipalPath(user))
                    .user(user)
                    .password(password)
                    .build();
        } catch (URISyntaxException use) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
