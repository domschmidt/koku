package de.domschmidt.koku.user.transformer;

import de.domschmidt.koku.dto.user.KokuUserSummaryDto;
import de.domschmidt.koku.user.persistence.User;
import io.micrometer.common.util.StringUtils;

import java.util.stream.Stream;

public class UserToKokuUserSummaryDtoTransformer {

    public KokuUserSummaryDto transformToDto(final User model) {
        return KokuUserSummaryDto.builder()
                .id(model.getId())
                .summary(String.join(" ", Stream.of(model.getFirstname(), model.getLastname()).filter(StringUtils::isNotBlank).toList()))
                .build();
    }

}
