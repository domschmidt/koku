package de.domschmidt.koku.scheduled;

import de.domschmidt.koku.exceptions.KokuCardDavException;
import de.domschmidt.koku.service.ICardDavService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardDavSchedule {

    public static final int FIXED_SCHEDULE_RATE = 60 *  // minutes
            60 *  // seconds
            1000; // milliseconds
    private final ICardDavService cardDavService;

    @Autowired
    public CardDavSchedule(
            final ICardDavService cardDavService
    ) {
        this.cardDavService = cardDavService;
    }

    @Scheduled(fixedDelay = FIXED_SCHEDULE_RATE)
    public void runCardDavExport() {
        try {
            log.info("Started CardDav Schedule");
            this.cardDavService.syncAllContacts();
            log.info("Ended CardDav Schedule");
        } catch (final KokuCardDavException kokuCardDavException) {
            log.error("Unable to run CardDav Sync scheduled, due to: ", kokuCardDavException);
        }
    }

}
