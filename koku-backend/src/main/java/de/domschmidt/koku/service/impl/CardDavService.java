package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.configuration.NextCloudConfiguration;
import de.domschmidt.koku.controller.customer.NextcloudPathResolver;
import de.domschmidt.koku.exceptions.KokuCardDavException;
import de.domschmidt.koku.exceptions.KokuVCardException;
import de.domschmidt.koku.service.ICardDavService;
import de.domschmidt.koku.service.ICustomerVCardService;
import net.fortuna.ical4j.connector.FailedOperationException;
import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.CardDavCollection;
import net.fortuna.ical4j.connector.dav.CardDavStore;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.VCard;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

@Service
public class CardDavService implements ICardDavService {

    private ICustomerVCardService customerVCardService;
    private NextCloudConfiguration nextCloudConfiguration;
    private static final String PROD_ID = "KoKu";

    @Autowired
    public CardDavService(
            final ICustomerVCardService customerVCardService,
            final NextCloudConfiguration nextCloudConfiguration
    ) {
        this.customerVCardService = customerVCardService;
        this.nextCloudConfiguration = nextCloudConfiguration;
    }

    @Override
    public void syncAllContacts() throws KokuCardDavException {
        try {
            final String endpoint = this.nextCloudConfiguration.getNextcloudEndpoint();
            final String user = this.nextCloudConfiguration.getNextcloudUser();
            final String password = this.nextCloudConfiguration.getNextcloudPassword();

            final boolean isProperlyDefined = StringUtils.isNotBlank(endpoint)
                    && StringUtils.isNotBlank(user)
                    && StringUtils.isNotBlank(password);

            if (!isProperlyDefined) {
                throw new KokuCardDavException("invalid configuration");
            }

            final URI endpointUri = new URI(endpoint);
            final String cardDavHost = endpointUri.getScheme() + "://"
                    + endpointUri.getHost()
                    + (endpointUri.getPort() > 0 ? ':' + endpointUri.getPort() : "")
                    + endpointUri.getPath();

            final NextcloudPathResolver pathResolver = new NextcloudPathResolver(endpointUri.getPath());
            final CardDavStore store = new CardDavStore(
                    PROD_ID,
                    new URL(cardDavHost),
                    pathResolver
            );
            store.connect(
                    user,
                    password.toCharArray()
            );

            final CardDavCollection cardDavCollection = store.getCollection(pathResolver.getPrincipalPath(user));
            final net.fortuna.ical4j.vcard.VCard[] components = cardDavCollection.getComponents();
            for (net.fortuna.ical4j.vcard.VCard component : components) {
                // delete existing ones
                final Property currentIdProp = component.getProperty(Property.Id.UID);
                cardDavCollection.removeCard(currentIdProp.getValue());
            }

            final List<VCard> allCustomerVCards = this.customerVCardService.getAllCustomerVCards();
            for (final VCard currentCustomerVCard : allCustomerVCards) {
                cardDavCollection.addCard(currentCustomerVCard);
            }
        } catch (final ObjectStoreException | ConstraintViolationException | ObjectNotFoundException | FailedOperationException | KokuVCardException | MalformedURLException | URISyntaxException e) {
            throw new KokuCardDavException(e);
        }
    }

}
