package de.domschmidt.koku;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class KokuProductServiceApplicationTest {

    @Test
    void startsWithGermanDefaultLocale() {
        final Locale previousLocale = Locale.getDefault();
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            new KokuProductServiceApplication();
            KokuProductServiceApplication.main(new String[] {"--test"});

            assertEquals(Locale.GERMAN, Locale.getDefault());
            springApplication.verify(
                    () -> SpringApplication.run(KokuProductServiceApplication.class, new String[] {"--test"}));
        } finally {
            Locale.setDefault(previousLocale);
        }
    }
}
