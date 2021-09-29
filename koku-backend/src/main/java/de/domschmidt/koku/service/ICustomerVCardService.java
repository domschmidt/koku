package de.domschmidt.koku.service;

import de.domschmidt.koku.exceptions.KokuVCardException;
import net.fortuna.ical4j.vcard.VCard;

import java.util.List;

public interface ICustomerVCardService {
    List<VCard> getAllCustomerVCards() throws KokuVCardException;
}
