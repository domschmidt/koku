package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.exceptions.KokuVCardException;
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.service.ICustomerService;
import de.domschmidt.koku.service.ICustomerVCardService;
import de.domschmidt.koku.service.searchoptions.CustomerSearchOptions;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.parameter.Type;
import net.fortuna.ical4j.vcard.property.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerVCardService implements ICustomerVCardService {

    private final ICustomerService customerService;
    private static final String PROD_ID = "KoKu";

    @Autowired
    public CustomerVCardService(final ICustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public List<VCard> getAllCustomerVCards() throws KokuVCardException {
        final List<VCard> result = new ArrayList<>();
        try {
            final Date exportDate = new Date();

            final List<Customer> allCustomers = this.customerService.findAll(CustomerSearchOptions.builder().search("").build());
            for (final Customer customer : allCustomers) {
                List<Property> props = new ArrayList<Property>();
                final String firstName = customer.getFirstName();
                final String lastName = customer.getLastName();
                props.add(new N(lastName, firstName, null, null, null));
                final List<String> nameList = new ArrayList<>();
                if (StringUtils.isNotBlank(firstName)) {
                    nameList.add(firstName);
                }
                if (StringUtils.isNotBlank(lastName)) {
                    nameList.add(lastName);
                }
                props.add(new Fn(String.join(" ", nameList)));
                final LocalDate customerBirthday = customer.getBirthday();
                if (customerBirthday != null) {
                    props.add(new BDay(new net.fortuna.ical4j.model.Date(Date.from(customerBirthday.atStartOfDay().atZone(ZoneId.of("Europe/Berlin")).toInstant()))));
                }
                props.add(Version.VERSION_4_0);
                props.add(new ProdId(PROD_ID));

                final String customerPostalCode = customer.getPostalCode();
                final String customerAddress = customer.getAddress();
                final String customerCity = customer.getCity();

                final boolean hasAddress = StringUtils.isNotBlank(customerPostalCode)
                        || StringUtils.isNotBlank(customerAddress)
                        || StringUtils.isNotBlank(customerCity);
                if (hasAddress) {
                    props.add(new Address(
                            null,
                            null,
                            customerAddress,
                            customerCity,
                            null,
                            customerPostalCode,
                            null,
                            Type.HOME
                    ));
                }

                final String customerBusinessTelephoneNo = customer.getBusinessTelephoneNo();
                if (StringUtils.isNotBlank(customerBusinessTelephoneNo)) {
                    props.add(new Telephone(customerBusinessTelephoneNo, Type.WORK));
                }

                final String customerMobileTelephoneNo = customer.getMobileTelephoneNo();
                if (StringUtils.isNotBlank(customerMobileTelephoneNo)) {
                    props.add(new Telephone(customerMobileTelephoneNo, new Type("cell")));
                }

                final String customerPrivateTelephoneNo = customer.getPrivateTelephoneNo();
                if (StringUtils.isNotBlank(customerPrivateTelephoneNo)) {
                    props.add(new Telephone(customerPrivateTelephoneNo, Type.HOME));
                }

                final String customerEmail = customer.getEmail();
                if (StringUtils.isNotBlank(customerEmail)) {
                    props.add(new Email(customerEmail));
                }
                props.add(new Uid(new URI(UUID.nameUUIDFromBytes(customer.getId().toString().getBytes()).toString())));
                props.add(new Revision(new net.fortuna.ical4j.model.Date(exportDate)));

                final net.fortuna.ical4j.vcard.VCard newContact = new net.fortuna.ical4j.vcard.VCard(props);
                result.add(newContact);
            }
        } catch (final URISyntaxException use) {
            throw new KokuVCardException(use);
        }
        return result;
    }

}
