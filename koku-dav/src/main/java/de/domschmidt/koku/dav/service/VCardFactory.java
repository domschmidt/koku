package de.domschmidt.koku.dav.service;

import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.ProductId;
import ezvcard.property.StructuredName;
import ezvcard.property.Uid;
import org.springframework.stereotype.Component;

@Component
public class VCardFactory {

    public String toVCard(final CustomerKafkaDto contact) {
        final VCard vcard = new VCard(VCardVersion.V3_0);
        vcard.setProductId(new ProductId("-//KoKu//KoKu Carddav//DE"));
        final StructuredName structuredName = new StructuredName();
        structuredName.setFamily(contact.getLastname());
        structuredName.setGiven(contact.getFirstname());
        vcard.setStructuredName(structuredName);
        vcard.setFormattedName(contact.getFullname());
        vcard.setUid(new Uid(String.valueOf(contact.getId())));
        if (contact.getBirthday() != null) {
            vcard.setBirthday(contact.getBirthday());
        }
        addTelephone(contact, vcard);
        if (contact.getEmail() != null && !contact.getEmail().isBlank()) {
            vcard.addEmail(contact.getEmail());
        }
        if (contact.getUpdated() != null) {
            vcard.setRevision(contact.getUpdated());
        }
        return Ezvcard.write(vcard).version(VCardVersion.V3_0).prodId(false).go();
    }

    private void addTelephone(final CustomerKafkaDto contact, final VCard vcard) {
        if (contact.getBusinessTelephoneNo() != null
                && !contact.getBusinessTelephoneNo().isBlank()) {
            vcard.addTelephoneNumber(contact.getBusinessTelephoneNo(), TelephoneType.WORK, TelephoneType.VOICE);
        }
        if (contact.getMobileTelephoneNo() != null
                && !contact.getMobileTelephoneNo().isBlank()) {
            vcard.addTelephoneNumber(contact.getMobileTelephoneNo(), TelephoneType.CELL, TelephoneType.VOICE);
        }
        if (contact.getPrivateTelephoneNo() != null
                && !contact.getPrivateTelephoneNo().isBlank()) {
            vcard.addTelephoneNumber(contact.getPrivateTelephoneNo(), TelephoneType.HOME, TelephoneType.VOICE);
        }
    }
}
