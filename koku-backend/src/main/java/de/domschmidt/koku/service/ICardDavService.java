package de.domschmidt.koku.service;

import de.domschmidt.koku.exceptions.KokuCardDavException;

public interface ICardDavService {

    void syncAllContacts() throws KokuCardDavException;

}
