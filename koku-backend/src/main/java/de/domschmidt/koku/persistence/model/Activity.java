package de.domschmidt.koku.persistence.model;

import de.domschmidt.koku.persistence.model.common.DomainModel;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.util.List;

@Entity
@Getter
@Setter

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "activity", schema = "koku")
public class Activity extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    String description;
    boolean deleted;
    Duration approximatelyDuration;

    @OneToMany(mappedBy = "activity", fetch = FetchType.EAGER)
    @OrderBy("recorded asc")
    List<ActivityPriceHistoryEntry> priceHistory;

    @OneToMany(mappedBy = "activity", fetch = FetchType.LAZY)
    List<CustomerAppointmentActivity> usageInCustomerAppointments;

    @ManyToOne
    ActivityCategory category;
    boolean relevantForPriceList;

}
