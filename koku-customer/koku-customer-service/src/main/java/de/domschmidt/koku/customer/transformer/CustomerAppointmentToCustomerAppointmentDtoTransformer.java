package de.domschmidt.koku.customer.transformer;

import de.domschmidt.koku.activity.kafka.dto.ActivityKafkaDto;
import de.domschmidt.koku.activity.kafka.dto.ActivityPriceHistoryKafkaDto;
import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentActivityDomain;
import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentPromotionDomain;
import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentSoldProductDomain;
import de.domschmidt.koku.customer.exceptions.*;
import de.domschmidt.koku.customer.kafka.activities.service.ActivityKTableProcessor;
import de.domschmidt.koku.customer.kafka.activity_steps.service.ActivityStepKTableProcessor;
import de.domschmidt.koku.customer.kafka.productmanufacturers.service.ProductManufacturerKTableProcessor;
import de.domschmidt.koku.customer.kafka.products.service.ProductKTableProcessor;
import de.domschmidt.koku.customer.kafka.promotions.service.PromotionKTableProcessor;
import de.domschmidt.koku.customer.kafka.users.service.UserKTableProcessor;
import de.domschmidt.koku.customer.persistence.*;
import de.domschmidt.koku.dto.customer.*;
import de.domschmidt.koku.product.kafka.dto.ProductKafkaDto;
import de.domschmidt.koku.product.kafka.dto.ProductManufacturerKafkaDto;
import de.domschmidt.koku.product.kafka.dto.ProductPriceHistoryKafkaDto;
import de.domschmidt.koku.promotion.kafka.dto.PromotionKafkaDto;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class CustomerAppointmentToCustomerAppointmentDtoTransformer {

    private static final DateTimeFormatter LONG_SUMMARY_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("'Kundentermin am' dd.MM.yyyy 'um' HH:mm 'Uhr'");
    private static final DateTimeFormatter SHORT_SUMMARY_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm 'Uhr'");
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private static final NumberFormat PRICE_FORMATTER;

    static {
        final NumberFormat newPriceFormatter = NumberFormat.getInstance(Locale.GERMAN);

        newPriceFormatter.setMaximumFractionDigits(2);
        newPriceFormatter.setMinimumFractionDigits(2);
        newPriceFormatter.setRoundingMode(RoundingMode.HALF_UP);
        newPriceFormatter.setGroupingUsed(true);

        PRICE_FORMATTER = newPriceFormatter;
    }

    public KokuCustomerAppointmentSummaryDto transformToSummaryDto(final CustomerAppointment model) {
        return KokuCustomerAppointmentSummaryDto.builder()
                .id(model.getId())
                .appointmentSummary(LONG_SUMMARY_DATETIME_FORMATTER.format(model.getStart()))
                .build();
    }

    private final EntityManager entityManager;
    private final ActivityKTableProcessor activityKTableProcessor;
    private final ActivityStepKTableProcessor activityStepKTableProcessor;
    private final ProductKTableProcessor productKTableProcessor;
    private final ProductManufacturerKTableProcessor productManufacturerKTableProcessor;
    private final PromotionKTableProcessor promotionKTableProcessor;
    private final UserKTableProcessor userKTableProcessor;

    public KokuCustomerAppointmentDto transformToDto(final CustomerAppointment model) {
        final List<KokuCustomerAppointmentActivityDto> activities = transformActivities(model.getActivities());
        final List<KokuCustomerAppointmentTreatmentDto> treatmentSequence =
                transformTreatmentSequence(model.getTreatmentSequence());
        final List<KokuCustomerAppointmentSoldProductDto> soldProducts = transformSoldProducts(model.getSoldProducts());
        final List<KokuCustomerAppointmentPromotionDto> promotions = transformPromotions(model.getPromotions());
        BigDecimal activityPriceSum = calculateCustomerAppointmentActivityPriceSum(
                model.getStart(),
                activities.stream()
                        .map(KokuCustomerAppointmentActivityDomain::fromDto)
                        .toList(),
                promotions.stream()
                        .map(KokuCustomerAppointmentPromotionDomain::fromDto)
                        .toList());
        BigDecimal soldProductPriceSum = calculateCustomerAppointmentSoldProductPriceSum(
                model.getStart(),
                soldProducts.stream()
                        .map(KokuCustomerAppointmentSoldProductDomain::fromDto)
                        .toList(),
                promotions.stream()
                        .map(KokuCustomerAppointmentPromotionDomain::fromDto)
                        .toList());
        return KokuCustomerAppointmentDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .date(model.getStart() != null ? model.getStart().toLocalDate() : null)
                .time(model.getStart() != null ? model.getStart().toLocalTime() : null)
                .approximatelyEndDate(
                        model.getCalculatedEndSnapshot() != null
                                ? model.getCalculatedEndSnapshot().toLocalDate()
                                : null)
                .approximatelyEndTime(
                        model.getCalculatedEndSnapshot() != null
                                ? model.getCalculatedEndSnapshot().toLocalTime()
                                : null)
                .description(model.getDescription())
                .additionalInfo(model.getAdditionalInfo())
                .userId(model.getUserId())
                .customerId(model.getCustomer().getId())
                .customerName(CustomerNameFormatter.displayNameWithFirstnameBasisMarker(model.getCustomer()))
                .shortSummaryText(SHORT_SUMMARY_DATETIME_FORMATTER.format(model.getStart()))
                .longSummaryText(LONG_SUMMARY_DATETIME_FORMATTER.format(model.getStart()))
                .activities(activities)
                .treatmentSequence(treatmentSequence)
                .soldProducts(soldProducts)
                .promotions(promotions)
                .overallPriceSummary(PRICE_FORMATTER.format(activityPriceSum.add(soldProductPriceSum)))
                .activityPriceSummary(PRICE_FORMATTER.format(activityPriceSum))
                .activityDurationSummary(
                        this.calculateCustomerAppointmentActivityDurationHumanReadable(activities.stream()
                                .map(KokuCustomerAppointmentActivityDto::getActivityId)
                                .toList()))
                .activitySoldProductSummary(PRICE_FORMATTER.format(soldProductPriceSum))
                .activitySummarySnapshot(this.calculateCustomerAppointmentActivitySummary(activities.stream()
                        .map(KokuCustomerAppointmentActivityDomain::fromDto)
                        .toList()))
                .soldProductSummarySnapshot(this.calculateCustomerAppointmentSoldProductSummary(soldProducts.stream()
                        .map(KokuCustomerAppointmentSoldProductDomain::fromDto)
                        .toList()))
                .build();
    }

    private static List<KokuCustomerAppointmentActivityDto> transformActivities(
            final List<CustomerAppointmentActivity> activities) {
        final List<KokuCustomerAppointmentActivityDto> result = new ArrayList<>();
        if (activities == null) {
            return result;
        }

        for (final CustomerAppointmentActivity currentActivity : activities) {
            result.add(KokuCustomerAppointmentActivityDto.builder()
                    .price(currentActivity.getSellPrice())
                    .activityId(currentActivity.getActivityId())
                    .build());
        }
        return result;
    }

    private static List<KokuCustomerAppointmentTreatmentDto> transformTreatmentSequence(
            final List<CustomerAppointmentTreatmentSequenceItem> treatmentSequence) {
        final List<KokuCustomerAppointmentTreatmentDto> result = new ArrayList<>();
        if (treatmentSequence == null) {
            return result;
        }

        for (final CustomerAppointmentTreatmentSequenceItem currentTreatment : treatmentSequence) {
            if (currentTreatment.getActivityStepId() != null) {
                result.add(KokuCustomerAppointmentActivityStepTreatmentDto.builder()
                        .activityStepId(currentTreatment.getActivityStepId())
                        .build());
            } else if (currentTreatment.getProductId() != null) {
                result.add(KokuCustomerAppointmentProductTreatmentDto.builder()
                        .productId(currentTreatment.getProductId())
                        .build());
            }
        }
        return result;
    }

    private static List<KokuCustomerAppointmentSoldProductDto> transformSoldProducts(
            final List<CustomerAppointmentSoldProduct> soldProducts) {
        final List<KokuCustomerAppointmentSoldProductDto> result = new ArrayList<>();
        if (soldProducts == null) {
            return result;
        }

        for (final CustomerAppointmentSoldProduct currentSoldProduct : soldProducts) {
            result.add(KokuCustomerAppointmentSoldProductDto.builder()
                    .price(currentSoldProduct.getSellPrice())
                    .productId(currentSoldProduct.getProductId())
                    .build());
        }
        return result;
    }

    private static List<KokuCustomerAppointmentPromotionDto> transformPromotions(
            final List<CustomerAppointmentPromotion> promotions) {
        final List<KokuCustomerAppointmentPromotionDto> result = new ArrayList<>();
        if (promotions == null) {
            return result;
        }

        for (final CustomerAppointmentPromotion currentPromotion : promotions) {
            result.add(KokuCustomerAppointmentPromotionDto.builder()
                    .promotionId(currentPromotion.getPromotionId())
                    .build());
        }
        return result;
    }

    public CustomerAppointment transformToEntity(
            final CustomerAppointment model, final KokuCustomerAppointmentDto updatedDto)
            throws UserIdNotFoundException, ActivityIdNotFoundException, ActivityStepIdNotFoundException,
                    ProductIdNotFoundException, PromotionIdNotFoundException {
        updateBaseFields(model, updatedDto);
        updateAssignedUser(model, updatedDto);
        updateCustomer(model, updatedDto);
        replaceActivities(model, updatedDto);
        replaceTreatmentSequence(model, updatedDto);
        replaceSoldProducts(model, updatedDto);
        replacePromotions(model, updatedDto);
        refreshSnapshots(model);
        updateFinalPriceSnapshots(model);
        updateCalculatedEndSnapshot(model);
        updateDeletedState(model, updatedDto);

        return model;
    }

    private void updateBaseFields(final CustomerAppointment model, final KokuCustomerAppointmentDto updatedDto) {
        if (updatedDto.getDate() != null && updatedDto.getTime() != null) {
            model.setStart(updatedDto.getDate().atTime(updatedDto.getTime()));
        }
        if (updatedDto.getDescription() != null) {
            model.setDescription(updatedDto.getDescription());
        }
        if (updatedDto.getAdditionalInfo() != null) {
            model.setAdditionalInfo(updatedDto.getAdditionalInfo());
        }
    }

    private void updateAssignedUser(final CustomerAppointment model, final KokuCustomerAppointmentDto updatedDto)
            throws UserIdNotFoundException {
        if (updatedDto.getUserId() == null) {
            return;
        }
        if (!this.userKTableProcessor.getUsers().containsKey(updatedDto.getUserId())) {
            throw new UserIdNotFoundException(updatedDto.getUserId());
        }
        model.setUserId(updatedDto.getUserId());
    }

    private void updateCustomer(final CustomerAppointment model, final KokuCustomerAppointmentDto updatedDto) {
        if (updatedDto.getCustomerId() != null) {
            model.setCustomer(this.entityManager.getReference(Customer.class, updatedDto.getCustomerId()));
        }
    }

    private void replaceActivities(final CustomerAppointment model, final KokuCustomerAppointmentDto updatedDto)
            throws ActivityIdNotFoundException {
        if (updatedDto.getActivities() == null) {
            return;
        }

        final List<CustomerAppointmentActivity> newActivities = model.getActivities();
        newActivities.clear();
        int position = 0;
        for (final KokuCustomerAppointmentActivityDto currentActivity : updatedDto.getActivities()) {
            if (this.activityKTableProcessor.getActivities().get(currentActivity.getActivityId()) == null) {
                throw new ActivityIdNotFoundException(currentActivity.getActivityId());
            }

            newActivities.add(new CustomerAppointmentActivity(
                    model, currentActivity.getActivityId(), currentActivity.getPrice(), position++));
        }
        model.setActivities(newActivities);
    }

    private void replaceTreatmentSequence(final CustomerAppointment model, final KokuCustomerAppointmentDto updatedDto)
            throws ActivityStepIdNotFoundException, ProductIdNotFoundException {
        if (updatedDto.getTreatmentSequence() == null) {
            return;
        }

        final List<CustomerAppointmentTreatmentSequenceItem> newTreatments = model.getTreatmentSequence();
        newTreatments.clear();
        int position = 0;
        for (final KokuCustomerAppointmentTreatmentDto currentTreatment : updatedDto.getTreatmentSequence()) {
            position = addTreatmentSequenceItem(model, newTreatments, currentTreatment, position);
        }
        model.setTreatmentSequence(newTreatments);
    }

    private int addTreatmentSequenceItem(
            final CustomerAppointment model,
            final List<CustomerAppointmentTreatmentSequenceItem> newTreatments,
            final KokuCustomerAppointmentTreatmentDto currentTreatment,
            final int position)
            throws ActivityStepIdNotFoundException, ProductIdNotFoundException {
        return switch (currentTreatment) {
            case KokuCustomerAppointmentActivityStepTreatmentDto activityStep ->
                addActivityStepTreatment(model, newTreatments, activityStep, position);
            case KokuCustomerAppointmentProductTreatmentDto product ->
                addProductTreatment(model, newTreatments, product, position);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unexpected type");
        };
    }

    private int addActivityStepTreatment(
            final CustomerAppointment model,
            final List<CustomerAppointmentTreatmentSequenceItem> newTreatments,
            final KokuCustomerAppointmentActivityStepTreatmentDto activityStep,
            final int position)
            throws ActivityStepIdNotFoundException {
        if (this.activityStepKTableProcessor.getActivitySteps().get(activityStep.getActivityStepId()) == null) {
            throw new ActivityStepIdNotFoundException(activityStep.getActivityStepId());
        }
        newTreatments.add(
                new CustomerAppointmentTreatmentSequenceItem(model, activityStep.getActivityStepId(), null, position));
        return position + 1;
    }

    private int addProductTreatment(
            final CustomerAppointment model,
            final List<CustomerAppointmentTreatmentSequenceItem> newTreatments,
            final KokuCustomerAppointmentProductTreatmentDto product,
            final int position)
            throws ProductIdNotFoundException {
        if (this.productKTableProcessor.getProducts().get(product.getProductId()) == null) {
            throw new ProductIdNotFoundException(product.getProductId());
        }
        newTreatments.add(new CustomerAppointmentTreatmentSequenceItem(model, null, product.getProductId(), position));
        return position + 1;
    }

    private void replaceSoldProducts(final CustomerAppointment model, final KokuCustomerAppointmentDto updatedDto)
            throws ProductIdNotFoundException {
        if (updatedDto.getSoldProducts() == null) {
            return;
        }

        final List<CustomerAppointmentSoldProduct> newSoldProducts = model.getSoldProducts();
        newSoldProducts.clear();
        int position = 0;
        for (final KokuCustomerAppointmentSoldProductDto currentSoldProduct : updatedDto.getSoldProducts()) {
            if (this.productKTableProcessor.getProducts().get(currentSoldProduct.getProductId()) == null) {
                throw new ProductIdNotFoundException(currentSoldProduct.getProductId());
            }

            newSoldProducts.add(new CustomerAppointmentSoldProduct(
                    model, currentSoldProduct.getProductId(), currentSoldProduct.getPrice(), position++));
        }
        model.setSoldProducts(newSoldProducts);
    }

    private void replacePromotions(final CustomerAppointment model, final KokuCustomerAppointmentDto updatedDto)
            throws PromotionIdNotFoundException {
        if (updatedDto.getPromotions() == null) {
            return;
        }

        final List<CustomerAppointmentPromotion> newPromotions = model.getPromotions();
        newPromotions.clear();
        int position = 0;
        for (final KokuCustomerAppointmentPromotionDto currentPromotion : updatedDto.getPromotions()) {
            if (this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId()) == null) {
                throw new PromotionIdNotFoundException(currentPromotion.getPromotionId());
            }

            newPromotions.add(new CustomerAppointmentPromotion(model, currentPromotion.getPromotionId(), position++));
        }
        model.setPromotions(newPromotions);
    }

    private void refreshSnapshots(final CustomerAppointment model) {
        final List<KokuCustomerAppointmentSoldProductDomain> soldProductDomains = soldProductDomains(model);
        final List<KokuCustomerAppointmentActivityDomain> activityDomains = activityDomains(model);
        final List<KokuCustomerAppointmentPromotionDomain> promotionDomains = promotionDomains(model);

        model.setSoldProductsRevenueSnapshot(this.calculateCustomerAppointmentSoldProductPriceSum(
                model.getStart(), soldProductDomains, promotionDomains));
        model.setSoldProductsSummarySnapshot(this.calculateCustomerAppointmentSoldProductSummary(soldProductDomains));
        model.setActivitiesRevenueSnapshot(
                this.calculateCustomerAppointmentActivityPriceSum(model.getStart(), activityDomains, promotionDomains));
        model.setActivitiesSummarySnapshot(this.calculateCustomerAppointmentActivitySummary(activityDomains));
    }

    private void updateFinalPriceSnapshots(final CustomerAppointment model) {
        final List<KokuCustomerAppointmentPromotionDomain> promotionDomains = promotionDomains(model);
        for (CustomerAppointmentSoldProduct soldProduct : model.getSoldProducts()) {
            soldProduct.setFinalPriceSnapshot(this.calculateSoldProductPrice(
                    model.getStart(),
                    KokuCustomerAppointmentSoldProductDomain.fromEntity(soldProduct),
                    promotionDomains));
        }

        for (CustomerAppointmentActivity activity : model.getActivities()) {
            activity.setFinalPriceSnapshot(this.calculateActivityPrice(
                    model.getStart(), KokuCustomerAppointmentActivityDomain.fromEntity(activity), promotionDomains));
        }
    }

    private void updateCalculatedEndSnapshot(final CustomerAppointment model) {
        model.setCalculatedEndSnapshot(this.calculateCustomerAppointmentEnd(model.getStart(), activityDomains(model)));
    }

    private static void updateDeletedState(
            final CustomerAppointment model, final KokuCustomerAppointmentDto updatedDto) {
        if (updatedDto.getDeleted() != null) {
            model.setDeleted(updatedDto.getDeleted());
        }
    }

    private static List<KokuCustomerAppointmentSoldProductDomain> soldProductDomains(final CustomerAppointment model) {
        return model.getSoldProducts().stream()
                .map(KokuCustomerAppointmentSoldProductDomain::fromEntity)
                .toList();
    }

    private static List<KokuCustomerAppointmentActivityDomain> activityDomains(final CustomerAppointment model) {
        return model.getActivities().stream()
                .map(KokuCustomerAppointmentActivityDomain::fromEntity)
                .toList();
    }

    private static List<KokuCustomerAppointmentPromotionDomain> promotionDomains(final CustomerAppointment model) {
        return model.getPromotions().stream()
                .map(KokuCustomerAppointmentPromotionDomain::fromEntity)
                .toList();
    }

    public BigDecimal calculateCustomerAppointmentActivityPriceSum(
            final LocalDateTime date,
            final List<KokuCustomerAppointmentActivityDomain> activities,
            final List<KokuCustomerAppointmentPromotionDomain> promotions) {
        BigDecimal sum = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (activities != null) {
            for (final KokuCustomerAppointmentActivityDomain currentActivity : activities) {
                sum = sum.add(calculateActivityPrice(date, currentActivity, promotions));
            }
        }

        for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
            final PromotionKafkaDto currentKafkaPromotion =
                    this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
            if (currentKafkaPromotion.getActivityAbsoluteSavings() != null) {
                sum = sum.subtract(currentKafkaPromotion.getActivityAbsoluteSavings());
            }
        }

        for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
            final PromotionKafkaDto currentKafkaPromotion =
                    this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
            if (currentKafkaPromotion.getActivityRelativeSavings() != null) {
                sum = sum.multiply(BigDecimal.ONE.subtract(currentKafkaPromotion
                        .getActivityRelativeSavings()
                        .divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP)));
            }
        }

        if (sum.compareTo(BigDecimal.ZERO) < 0) {
            sum = BigDecimal.ZERO;
        }

        return sum;
    }

    public BigDecimal calculateActivityPrice(
            final LocalDateTime date,
            final KokuCustomerAppointmentActivityDomain currentActivity,
            final List<KokuCustomerAppointmentPromotionDomain> promotions) {
        BigDecimal sellPrice = currentActivity.getPrice();
        if (sellPrice == null) {
            sellPrice = getActivityHistoryDefaultPrice(date, currentActivity.getActivityId());

            for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
                final PromotionKafkaDto currentKafkaPromotion =
                        this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
                if (currentKafkaPromotion.getActivityAbsoluteItemSavings() != null) {
                    sellPrice = sellPrice.subtract(currentKafkaPromotion.getActivityAbsoluteItemSavings());
                }
            }
            for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
                final PromotionKafkaDto currentKafkaPromotion =
                        this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
                if (currentKafkaPromotion.getActivityRelativeItemSavings() != null) {
                    sellPrice = sellPrice.multiply(BigDecimal.ONE.subtract(currentKafkaPromotion
                            .getActivityRelativeItemSavings()
                            .divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP)));
                }
            }
        }
        if (sellPrice.compareTo(BigDecimal.ZERO) < 0) {
            sellPrice = BigDecimal.ZERO;
        }
        return sellPrice;
    }

    public BigDecimal calculateCustomerAppointmentSoldProductPriceSum(
            final LocalDateTime date,
            final List<KokuCustomerAppointmentSoldProductDomain> soldProducts,
            final List<KokuCustomerAppointmentPromotionDomain> promotions) {
        BigDecimal sum = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (soldProducts != null) {
            for (final KokuCustomerAppointmentSoldProductDomain currentSoldProduct : soldProducts) {
                sum = sum.add(calculateSoldProductPrice(date, currentSoldProduct, promotions));
            }
        }

        for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
            final PromotionKafkaDto currentKafkaPromotion =
                    this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
            if (currentKafkaPromotion.getProductAbsoluteSavings() != null) {
                sum = sum.subtract(currentKafkaPromotion.getProductAbsoluteSavings());
            }
        }

        for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
            final PromotionKafkaDto currentKafkaPromotion =
                    this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
            if (currentKafkaPromotion.getProductRelativeSavings() != null) {
                sum = sum.multiply(BigDecimal.ONE.subtract(currentKafkaPromotion
                        .getProductRelativeSavings()
                        .divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP)));
            }
        }

        return sum;
    }

    public BigDecimal calculateSoldProductPrice(
            final LocalDateTime date,
            final KokuCustomerAppointmentSoldProductDomain currentSoldProduct,
            final List<KokuCustomerAppointmentPromotionDomain> promotions) {
        BigDecimal sellPrice = currentSoldProduct.getPrice();
        if (sellPrice == null) {
            sellPrice = getProductHistoryDefaultPrice(
                    date, this.productKTableProcessor.getProducts().get(currentSoldProduct.getProductId()));

            for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
                final PromotionKafkaDto currentKafkaPromotion =
                        this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
                if (currentKafkaPromotion.getProductAbsoluteItemSavings() != null) {
                    sellPrice = sellPrice.subtract(currentKafkaPromotion.getProductAbsoluteItemSavings());
                }
            }
            for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
                final PromotionKafkaDto currentKafkaPromotion =
                        this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
                if (currentKafkaPromotion.getProductRelativeItemSavings() != null) {
                    sellPrice = sellPrice.multiply(BigDecimal.ONE.subtract(currentKafkaPromotion
                            .getProductRelativeItemSavings()
                            .divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP)));
                }
            }
        }
        if (sellPrice.compareTo(BigDecimal.ZERO) < 0) {
            sellPrice = BigDecimal.ZERO;
        }
        return sellPrice;
    }

    public String calculateCustomerAppointmentActivityDurationHumanReadable(final List<Long> activityIds) {
        final Duration sum = calculateCustomerAppointmentActivityDuration(activityIds);
        long minutes = sum.toMinutesPart();
        long hours = sum.toHoursPart();
        long days = sum.toDaysPart();

        final StringBuilder formattedDuration = new StringBuilder(100);
        if (days > 0) {
            formattedDuration.append(days).append(" Tage ");
        }
        if (hours > 0) {
            formattedDuration.append(hours).append(" Std. ");
        }
        if (minutes > 0) {
            formattedDuration.append(minutes).append(" Min. ");
        }
        return formattedDuration.toString().trim();
    }

    private Duration calculateCustomerAppointmentActivityDuration(final List<Long> activityIds) {
        Duration sum = Duration.ZERO;
        if (activityIds != null) {
            for (final Long currentActivityId : activityIds) {
                final ActivityKafkaDto kafkaActivity =
                        this.activityKTableProcessor.getActivities().get(currentActivityId);
                if (kafkaActivity.getApproximatelyDuration() != null) {
                    sum = sum.plus(kafkaActivity.getApproximatelyDuration());
                }
            }
        }
        return sum;
    }

    private BigDecimal getActivityHistoryDefaultPrice(final LocalDateTime date, final Long activityId) {
        final ActivityKafkaDto kafkaActivity =
                this.activityKTableProcessor.getActivities().get(activityId);
        BigDecimal result = null;
        if (kafkaActivity.getPriceHistory() != null) {
            final List<ActivityPriceHistoryKafkaDto> sortedPriceHistory = kafkaActivity.getPriceHistory().stream()
                    .sorted(Comparator.comparing(ActivityPriceHistoryKafkaDto::getRecorded))
                    .toList();
            for (final ActivityPriceHistoryKafkaDto activityPriceHistoryKafkaDto : sortedPriceHistory) {
                if (date.isAfter(activityPriceHistoryKafkaDto.getRecorded())) {
                    result = activityPriceHistoryKafkaDto.getPrice();
                } else {
                    break;
                }
            }
        }
        if (result == null
                && kafkaActivity.getPriceHistory() != null
                && !kafkaActivity.getPriceHistory().isEmpty()) {
            result = kafkaActivity.getPriceHistory().stream()
                    .min(Comparator.comparing(ActivityPriceHistoryKafkaDto::getRecorded))
                    .orElseThrow()
                    .getPrice();
        }
        if (result == null) {
            result = BigDecimal.ZERO;
        }
        return result;
    }

    private BigDecimal getProductHistoryDefaultPrice(final LocalDateTime date, final ProductKafkaDto product) {
        BigDecimal result = null;
        if (product.getPriceHistory() != null) {
            final List<ProductPriceHistoryKafkaDto> sortedPriceHistory = product.getPriceHistory().stream()
                    .sorted(Comparator.comparing(ProductPriceHistoryKafkaDto::getRecorded))
                    .toList();
            for (final ProductPriceHistoryKafkaDto productPriceHistoryKafkaDto : sortedPriceHistory) {
                if (date.isAfter(productPriceHistoryKafkaDto.getRecorded())) {
                    result = productPriceHistoryKafkaDto.getPrice();
                } else {
                    break;
                }
            }
        }
        if (result == null
                && product.getPriceHistory() != null
                && !product.getPriceHistory().isEmpty()) {
            result = product.getPriceHistory().stream()
                    .min(Comparator.comparing(ProductPriceHistoryKafkaDto::getRecorded))
                    .orElseThrow()
                    .getPrice();
        }
        if (result == null) {
            result = BigDecimal.ZERO;
        }
        return result;
    }

    public KokuCustomerActivityPriceSummaryDto transformToActivityPriceSummary(
            final KokuActivityPriceSummaryRequestDto request) {
        final LocalDateTime targetTimestamp;
        if (request.getDate() != null && request.getTime() != null) {
            targetTimestamp = request.getDate().atTime(request.getTime());
        } else if (request.getDate() != null) {
            targetTimestamp = request.getDate().atTime(LocalTime.now(DEFAULT_ZONE));
        } else if (request.getTime() != null) {
            targetTimestamp = LocalDate.now(DEFAULT_ZONE).atTime(request.getTime());
        } else {
            targetTimestamp = LocalDateTime.now(DEFAULT_ZONE);
        }

        return KokuCustomerActivityPriceSummaryDto.builder()
                .priceSum(PRICE_FORMATTER.format(calculateCustomerAppointmentActivityPriceSum(
                        targetTimestamp,
                        request.getActivities().stream()
                                .map(KokuCustomerAppointmentActivityDomain::fromDto)
                                .toList(),
                        request.getPromotions().stream()
                                .map(KokuCustomerAppointmentPromotionDomain::fromDto)
                                .toList())))
                .durationSum(
                        this.calculateCustomerAppointmentActivityDurationHumanReadable(request.getActivities().stream()
                                .map(KokuCustomerAppointmentActivityDto::getActivityId)
                                .toList()))
                .build();
    }

    public KokuCustomerAppointmentOverallPriceSummaryDto transformToOverallPriceSummary(
            final KokuCustomerAppointmentOverallPriceSummaryRequestDto request) {
        final LocalDateTime targetTimestamp;
        if (request.getDate() != null && request.getTime() != null) {
            targetTimestamp = request.getDate().atTime(request.getTime());
        } else if (request.getDate() != null) {
            targetTimestamp = request.getDate().atTime(LocalTime.now(DEFAULT_ZONE));
        } else if (request.getTime() != null) {
            targetTimestamp = LocalDate.now(DEFAULT_ZONE).atTime(request.getTime());
        } else {
            targetTimestamp = LocalDateTime.now(DEFAULT_ZONE);
        }

        final BigDecimal activityPriceSum = calculateCustomerAppointmentActivityPriceSum(
                targetTimestamp,
                request.getActivities().stream()
                        .map(KokuCustomerAppointmentActivityDomain::fromDto)
                        .toList(),
                request.getPromotions().stream()
                        .map(KokuCustomerAppointmentPromotionDomain::fromDto)
                        .toList());

        final BigDecimal soldProductsPriceSum = calculateCustomerAppointmentSoldProductPriceSum(
                targetTimestamp,
                request.getSoldProducts().stream()
                        .map(KokuCustomerAppointmentSoldProductDomain::fromDto)
                        .toList(),
                request.getPromotions().stream()
                        .map(KokuCustomerAppointmentPromotionDomain::fromDto)
                        .toList());

        return KokuCustomerAppointmentOverallPriceSummaryDto.builder()
                .priceSum(PRICE_FORMATTER.format(activityPriceSum.add(soldProductsPriceSum)))
                .build();
    }

    public KokuActivitySoldProductPriceSummaryDto transformToSoldProductPriceSummary(
            final KokuActivitySoldProductSummaryRequestDto request) {
        final LocalDateTime targetTimestamp;
        if (request.getDate() != null && request.getTime() != null) {
            targetTimestamp = request.getDate().atTime(request.getTime());
        } else if (request.getDate() != null) {
            targetTimestamp = request.getDate().atTime(LocalTime.now(DEFAULT_ZONE));
        } else if (request.getTime() != null) {
            targetTimestamp = LocalDate.now(DEFAULT_ZONE).atTime(request.getTime());
        } else {
            targetTimestamp = LocalDateTime.now(DEFAULT_ZONE);
        }

        return KokuActivitySoldProductPriceSummaryDto.builder()
                .priceSum(PRICE_FORMATTER.format(calculateCustomerAppointmentSoldProductPriceSum(
                        targetTimestamp,
                        request.getSoldProducts().stream()
                                .map(KokuCustomerAppointmentSoldProductDomain::fromDto)
                                .toList(),
                        request.getPromotions().stream()
                                .map(KokuCustomerAppointmentPromotionDomain::fromDto)
                                .toList())))
                .build();
    }

    public LocalDateTime calculateCustomerAppointmentEnd(
            final LocalDateTime start, final List<KokuCustomerAppointmentActivityDomain> activities) {
        Duration duration = Duration.ZERO;
        for (final KokuCustomerAppointmentActivityDomain currentActivity : activities) {
            final ActivityKafkaDto kafkaActivity =
                    this.activityKTableProcessor.getActivities().get(currentActivity.getActivityId());
            duration = duration.plus(kafkaActivity.getApproximatelyDuration());
        }
        return start.plus(duration);
    }

    public String calculateCustomerAppointmentActivitySummary(List<KokuCustomerAppointmentActivityDomain> list) {
        List<ActivityKafkaDto> kafkaActivities = new ArrayList<>();
        for (final KokuCustomerAppointmentActivityDomain currentActivity : list) {
            kafkaActivities.add(this.activityKTableProcessor.getActivities().get(currentActivity.getActivityId()));
        }
        return kafkaActivities.stream().map(ActivityKafkaDto::getName).collect(Collectors.joining(", "));
    }

    public String calculateCustomerAppointmentSoldProductSummary(List<KokuCustomerAppointmentSoldProductDomain> list) {
        List<ProductKafkaDto> kafkaSoldProducts = new ArrayList<>();
        ReadOnlyKeyValueStore<Long, ProductKafkaDto> productsSnapshot = this.productKTableProcessor.getProducts();
        ReadOnlyKeyValueStore<Long, ProductManufacturerKafkaDto> manufacturerSnapshot =
                this.productManufacturerKTableProcessor.getProductManufacturers();
        for (final KokuCustomerAppointmentSoldProductDomain currentSoldProduct : list) {
            kafkaSoldProducts.add(productsSnapshot.get(currentSoldProduct.getProductId()));
        }
        return kafkaSoldProducts.stream()
                .map(productKafkaDto -> Stream.of(
                                manufacturerSnapshot
                                        .get(productKafkaDto.getManufacturerId())
                                        .getName(),
                                productKafkaDto.getName())
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.joining(" / ")))
                .collect(Collectors.joining(", "));
    }
}
