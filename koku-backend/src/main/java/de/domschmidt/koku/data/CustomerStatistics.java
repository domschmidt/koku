package de.domschmidt.koku.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class CustomerStatistics {

    BigDecimal revenue;
    BigDecimal productRevenue;
    BigDecimal activityRevenue;

}
