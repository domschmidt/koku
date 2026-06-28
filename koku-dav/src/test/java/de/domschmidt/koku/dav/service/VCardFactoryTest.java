package de.domschmidt.koku.dav.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.Test;

class VCardFactoryTest {

    private final VCardFactory vCardFactory = new VCardFactory();

    @Test
    void writesStandardVcardWithTypedTelephoneNumbers() {
        final CustomerKafkaDto contact = CustomerKafkaDto.builder()
                .id(42L)
                .firstname("Max;One")
                .lastname("Muster,mann")
                .fullname("Max\nMustermann")
                .email("max@example.test")
                .businessTelephoneNo("06244 123")
                .mobileTelephoneNo("0170 123")
                .privateTelephoneNo("06244 456")
                .birthday(LocalDate.of(1990, Month.MAY, 4))
                .updated(LocalDateTime.of(2026, Month.JUNE, 11, 10, 0))
                .build();

        final String vcard = vCardFactory.toVCard(contact);

        assertThat(vcard)
                .contains(
                        "VERSION:3.0\r\n",
                        "PRODID:-//KoKu//KoKu Carddav//DE\r\n",
                        "TEL;TYPE=work,voice:06244 123\r\n",
                        "TEL;TYPE=cell,voice:0170 123\r\n",
                        "TEL;TYPE=home,voice:06244 456\r\n",
                        "BDAY:1990-05-04\r\n",
                        "UID:42\r\n",
                        "REV:")
                .endsWith("END:VCARD\r\n");
    }
}
