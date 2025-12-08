package de.domschmidt.koku.customer.transformer;

import de.domschmidt.koku.activity.kafka.dto.ActivityKafkaDto;
import de.domschmidt.koku.activity.kafka.dto.ActivityPriceHistoryKafkaDto;
import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentActivityDomain;
import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentPromotionDomain;
import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentSoldProductDomain;
import de.domschmidt.koku.customer.exceptions.*;
import de.domschmidt.koku.customer.kafka.activities.service.ActivityKTableProcessor;
import de.domschmidt.koku.customer.kafka.activity_steps.service.ActivityStepKTableProcessor;
import de.domschmidt.koku.customer.kafka.products.service.ProductKTableProcessor;
import de.domschmidt.koku.customer.kafka.promotions.service.PromotionKTableProcessor;
import de.domschmidt.koku.customer.kafka.users.service.UserKTableProcessor;
import de.domschmidt.koku.customer.persistence.*;
import de.domschmidt.koku.dto.customer.*;
import de.domschmidt.koku.product.kafka.dto.ProductKafkaDto;
import de.domschmidt.koku.product.kafka.dto.ProductPriceHistoryKafkaDto;
import de.domschmidt.koku.promotion.kafka.dto.PromotionKafkaDto;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CustomerAppointmentToCustomerAppointmentDtoTransformer {

    private static final DateTimeFormatter LONG_SUMMARY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("'Kundentermin am' dd.MM.yyyy 'um' HH:mm 'Uhr'");
    private static final DateTimeFormatter SHORT_SUMMARY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm 'Uhr'");

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
    private final PromotionKTableProcessor promotionKTableProcessor;
    private final UserKTableProcessor userKTableProcessor;

    @Autowired
    public CustomerAppointmentToCustomerAppointmentDtoTransformer(
            final EntityManager entityManager,
            final ActivityKTableProcessor activityKTableProcessor,
            final ActivityStepKTableProcessor activityStepKTableProcessor,
            final ProductKTableProcessor productKTableProcessor,
            final PromotionKTableProcessor promotionKTableProcessor,
            final UserKTableProcessor userKTableProcessor
    ) {
        this.entityManager = entityManager;
        this.activityKTableProcessor = activityKTableProcessor;
        this.activityStepKTableProcessor = activityStepKTableProcessor;
        this.productKTableProcessor = productKTableProcessor;
        this.promotionKTableProcessor = promotionKTableProcessor;
        this.userKTableProcessor = userKTableProcessor;
    }

    public KokuCustomerAppointmentDto transformToDto(final CustomerAppointment model) {
        final List<KokuCustomerAppointmentActivityDto> activities = new ArrayList<>();
        if (model.getActivities() != null) {
            for (final CustomerAppointmentActivity currentActivity : model.getActivities()) {
                activities.add(KokuCustomerAppointmentActivityDto.builder()
                        .price(currentActivity.getSellPrice())
                        .activityId(currentActivity.getActivityId())
                        .build()
                );
            }
        }
        final List<KokuCustomerAppointmentTreatmentDto> treatmentSequence = new ArrayList<>();
        if (model.getTreatmentSequence() != null) {
            for (final CustomerAppointmentTreatmentSequenceItem currentTreatment : model.getTreatmentSequence()) {
                if (currentTreatment.getActivityStepId() != null) {
                    treatmentSequence.add(KokuCustomerAppointmentActivityStepTreatmentDto.builder()
                            .activityStepId(currentTreatment.getActivityStepId())
                            .build()
                    );
                } else if (currentTreatment.getProductId() != null) {
                    treatmentSequence.add(KokuCustomerAppointmentProductTreatmentDto.builder()
                            .productId(currentTreatment.getProductId())
                            .build()
                    );
                }
            }
        }
        final List<KokuCustomerAppointmentSoldProductDto> soldProducts = new ArrayList<>();
        if (model.getSoldProducts() != null) {
            for (final CustomerAppointmentSoldProduct currentSoldProduct : model.getSoldProducts()) {
                soldProducts.add(KokuCustomerAppointmentSoldProductDto.builder()
                        .price(currentSoldProduct.getSellPrice())
                        .productId(currentSoldProduct.getProductId())
                        .build()
                );
            }
        }
        final List<KokuCustomerAppointmentPromotionDto> promotions = new ArrayList<>();
        if (model.getPromotions() != null) {
            for (final CustomerAppointmentPromotion currentPromotion : model.getPromotions()) {
                promotions.add(KokuCustomerAppointmentPromotionDto.builder()
                        .promotionId(currentPromotion.getPromotionId())
                        .build()
                );
            }
        }
        final Duration approximatelyActivitySum = activities.stream().map(
                kokuCustomerAppointmentActivityDto -> {
                    final ActivityKafkaDto activity = this.activityKTableProcessor.getActivities().get(kokuCustomerAppointmentActivityDto.getActivityId());
                    if (activity != null && activity.getApproximatelyDuration() != null) {
                        return activity.getApproximatelyDuration();
                    }
                    return Duration.ZERO;
                }
        ).reduce(Duration.ZERO, Duration::plus);


        final LocalDateTime approximatelyEndDateTime = model.getStart() != null ? model.getStart().plusNanos(approximatelyActivitySum.toNanos()) : null;
        return KokuCustomerAppointmentDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .date(model.getStart() != null ? model.getStart().toLocalDate() : null)
                .time(model.getStart() != null ? model.getStart().toLocalTime() : null)
                .approximatelyEndDate(approximatelyEndDateTime != null ? approximatelyEndDateTime.toLocalDate() : null)
                .approximatelyEndTime(approximatelyEndDateTime != null ? approximatelyEndDateTime.toLocalTime() : null)
                .description(model.getDescription())
                .additionalInfo(model.getAdditionalInfo())
                .userId(model.getUserId())
                .customerId(model.getCustomer().getId())
                .customerName(Stream.of(model.getCustomer().getFirstname(), model.getCustomer().getLastname())
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.joining(" "))
                        + (model.getCustomer().isOnFirstnameBasis() ? " *" : "")
                )
                .shortSummaryText(SHORT_SUMMARY_DATETIME_FORMATTER.format(model.getStart()))
                .longSummaryText(LONG_SUMMARY_DATETIME_FORMATTER.format(model.getStart()))
                .activities(activities)
                .treatmentSequence(treatmentSequence)
                .soldProducts(soldProducts)
                .promotions(promotions)
                .activityPriceSummary(this.calculateCustomerAppointmentActivityPriceSumFormatted(
                        model.getStart(),
                        activities.stream().map(KokuCustomerAppointmentActivityDomain::fromDto).toList(),
                        promotions.stream().map(KokuCustomerAppointmentPromotionDomain::fromDto).toList()
                ))
                .activityDurationSummary(this.calculateCustomerAppointmentActivityDurationHumanReadable(
                        activities.stream().map(KokuCustomerAppointmentActivityDto::getActivityId).collect(Collectors.toList())
                ))
                .activitySoldProductSummary(this.calculateCustomerAppointmentSoldProductPriceSumFormatted(
                        model.getStart(),
                        soldProducts.stream().map(KokuCustomerAppointmentSoldProductDomain::fromDto).toList(),
                        promotions.stream().map(KokuCustomerAppointmentPromotionDomain::fromDto).toList()
                ))
                .build();
    }

    public CustomerAppointment transformToEntity(
            final CustomerAppointment model,
            final KokuCustomerAppointmentDto updatedDto
    ) throws UserIdNotFoundException, ActivityIdNotFoundException, ActivityStepIdNotFoundException, ProductIdNotFoundException, PromotionIdNotFoundException {
        if (updatedDto.getDate() != null && updatedDto.getTime() != null) {
            model.setStart(updatedDto.getDate().atTime(updatedDto.getTime()));
        }
        if (updatedDto.getDescription() != null) {
            model.setDescription(updatedDto.getDescription());
        }
        if (updatedDto.getAdditionalInfo() != null) {
            model.setAdditionalInfo(updatedDto.getAdditionalInfo());
        }
        if (updatedDto.getUserId() != null) {
            if (!this.userKTableProcessor.getUsers().containsKey(updatedDto.getUserId())) {
                throw new UserIdNotFoundException(updatedDto.getUserId());
            }
            model.setUserId(updatedDto.getUserId());
        }
        if (updatedDto.getCustomerId() != null) {
            model.setCustomer(this.entityManager.getReference(Customer.class, updatedDto.getCustomerId()));
        }
        if (updatedDto.getDate() != null && updatedDto.getTime() != null) {
            model.setStart(updatedDto.getDate().atTime(updatedDto.getTime()));
        }
        if (updatedDto.getActivities() != null) {
            final List<CustomerAppointmentActivity> newActivities = model.getActivities();
            newActivities.clear();
            int position = 0;
            for (final KokuCustomerAppointmentActivityDto currentActivity : updatedDto.getActivities()) {
                if (this.activityKTableProcessor.getActivities().get(currentActivity.getActivityId()) == null) {
                    throw new ActivityIdNotFoundException(currentActivity.getActivityId());
                }

                final CustomerAppointmentActivity newActivity = new CustomerAppointmentActivity(
                        model,
                        currentActivity.getActivityId(),
                        currentActivity.getPrice(),
                        position++
                );

                newActivities.add(newActivity);
            }
            model.setActivities(newActivities);
        }
        if (updatedDto.getTreatmentSequence() != null) {
            final List<CustomerAppointmentTreatmentSequenceItem> newTreatments = model.getTreatmentSequence();
            newTreatments.clear();
            int position = 0;
            for (final KokuCustomerAppointmentTreatmentDto currentTreatment : updatedDto.getTreatmentSequence()) {
                switch (currentTreatment) {
                    case KokuCustomerAppointmentActivityStepTreatmentDto activityStep -> {
                        if (this.activityStepKTableProcessor.getActivitySteps().get(activityStep.getActivityStepId()) == null) {
                            throw new ActivityStepIdNotFoundException(activityStep.getActivityStepId());
                        }
                        newTreatments.add(new CustomerAppointmentTreatmentSequenceItem(
                                model,
                                activityStep.getActivityStepId(),
                                null,
                                position++
                        ));
                    }
                    case KokuCustomerAppointmentProductTreatmentDto product -> {
                        if (this.productKTableProcessor.getProducts().get(product.getProductId()) == null) {
                            throw new ProductIdNotFoundException(product.getProductId());
                        }
                        newTreatments.add(new CustomerAppointmentTreatmentSequenceItem(
                                model,
                                null,
                                product.getProductId(),
                                position++
                        ));
                    }
                    default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unexpected type");
                }
            }
            model.setTreatmentSequence(newTreatments);
        }
        if (updatedDto.getSoldProducts() != null) {
            final List<CustomerAppointmentSoldProduct> newSoldProducts = model.getSoldProducts();
            newSoldProducts.clear();
            int position = 0;
            for (final KokuCustomerAppointmentSoldProductDto currentSoldProduct : updatedDto.getSoldProducts()) {
                if (this.productKTableProcessor.getProducts().get(currentSoldProduct.getProductId()) == null) {
                    throw new ProductIdNotFoundException(currentSoldProduct.getProductId());
                }

                final CustomerAppointmentSoldProduct newSoldProduct = new CustomerAppointmentSoldProduct(
                        model,
                        currentSoldProduct.getProductId(),
                        currentSoldProduct.getPrice(),
                        position++
                );

                newSoldProducts.add(newSoldProduct);
            }
            model.setSoldProducts(newSoldProducts);
        }
        if (updatedDto.getPromotions() != null) {
            final List<CustomerAppointmentPromotion> newPromotions = model.getPromotions();
            newPromotions.clear();
            int position = 0;
            for (final KokuCustomerAppointmentPromotionDto currentPromotion : updatedDto.getPromotions()) {
                if (this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId()) == null) {
                    throw new PromotionIdNotFoundException(currentPromotion.getPromotionId());
                }

                newPromotions.add(new CustomerAppointmentPromotion(
                        model,
                        currentPromotion.getPromotionId(),
                        position++
                ));
            }
            model.setPromotions(newPromotions);
        }

        model.setSoldProductsRevenueSnapshot(this.calculateCustomerAppointmentSoldProductPriceSum(
                model.getStart(),
                model.getSoldProducts().stream().map(KokuCustomerAppointmentSoldProductDomain::fromEntity).toList(),
                model.getPromotions().stream().map(KokuCustomerAppointmentPromotionDomain::fromEntity).toList()
        ));
        model.setActivitiesRevenueSnapshot(this.calculateCustomerAppointmentActivityPriceSum(
                model.getStart(),
                model.getActivities().stream().map(KokuCustomerAppointmentActivityDomain::fromEntity).toList(),
                model.getPromotions().stream().map(KokuCustomerAppointmentPromotionDomain::fromEntity).toList()
        ));

        for (CustomerAppointmentSoldProduct soldProduct : model.getSoldProducts()) {
            soldProduct.setFinalPriceSnapshot(this.calculateSoldProductPrice(
                    model.getStart(),
                    KokuCustomerAppointmentSoldProductDomain.fromEntity(soldProduct),
                    model.getPromotions().stream().map(KokuCustomerAppointmentPromotionDomain::fromEntity).toList()
            ));
        }

        for (CustomerAppointmentActivity activity : model.getActivities()) {
            activity.setFinalPriceSnapshot(this.calculateActivityPrice(
                    model.getStart(),
                    KokuCustomerAppointmentActivityDomain.fromEntity(activity),
                    model.getPromotions().stream().map(KokuCustomerAppointmentPromotionDomain::fromEntity).toList()
            ));
        }

        return model;
    }

    public BigDecimal calculateCustomerAppointmentActivityPriceSum(
            final LocalDateTime date,
            final List<KokuCustomerAppointmentActivityDomain> activities,
            final List<KokuCustomerAppointmentPromotionDomain> promotions
    ) {
        BigDecimal sum = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (activities != null) {
            for (final KokuCustomerAppointmentActivityDomain currentActivity : activities) {
                sum = sum.add(calculateActivityPrice(date, currentActivity, promotions));
            }
        }

        for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
            final PromotionKafkaDto currentKafkaPromotion = this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
            if (currentKafkaPromotion.getActivityAbsoluteSavings() != null) {
                sum = sum.subtract(currentKafkaPromotion.getActivityAbsoluteSavings());
            }
        }

        for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
            final PromotionKafkaDto currentKafkaPromotion = this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
            if (currentKafkaPromotion.getActivityRelativeSavings() != null) {
                sum = sum.multiply(BigDecimal.ONE.subtract(currentKafkaPromotion.getActivityRelativeSavings().divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP)));
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
            final List<KokuCustomerAppointmentPromotionDomain> promotions
    ) {
        BigDecimal sellPrice = currentActivity.getPrice();
        if (sellPrice == null) {
            sellPrice = getActivityHistoryDefaultPrice(date, currentActivity.getActivityId());

            for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
                final PromotionKafkaDto currentKafkaPromotion = this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
                if (currentKafkaPromotion.getActivityAbsoluteItemSavings() != null) {
                    sellPrice = sellPrice.subtract(currentKafkaPromotion.getActivityAbsoluteItemSavings());
                }
            }
            for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
                final PromotionKafkaDto currentKafkaPromotion = this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
                if (currentKafkaPromotion.getActivityAbsoluteItemSavings() != null) {
                    sellPrice = sellPrice.multiply(BigDecimal.ONE.subtract(currentKafkaPromotion.getActivityRelativeItemSavings().divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP)));
                }
            }
        }
        if (sellPrice.compareTo(BigDecimal.ZERO) < 0) {
            sellPrice = BigDecimal.ZERO;
        }
        return sellPrice;
    }

    private String calculateCustomerAppointmentActivityPriceSumFormatted(
            final LocalDateTime date,
            final List<KokuCustomerAppointmentActivityDomain> activities,
            final List<KokuCustomerAppointmentPromotionDomain> promotions
    ) {
        return PRICE_FORMATTER.format(calculateCustomerAppointmentActivityPriceSum(date, activities, promotions));
    }

    public BigDecimal calculateCustomerAppointmentSoldProductPriceSum(
            final LocalDateTime date,
            final List<KokuCustomerAppointmentSoldProductDomain> soldProducts,
            final List<KokuCustomerAppointmentPromotionDomain> promotions
    ) {
        BigDecimal sum = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (soldProducts != null) {
            for (final KokuCustomerAppointmentSoldProductDomain currentSoldProduct : soldProducts) {
                sum = sum.add(calculateSoldProductPrice(date, currentSoldProduct, promotions));
            }
        }


        for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
            final PromotionKafkaDto currentKafkaPromotion = this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
            if (currentKafkaPromotion.getProductAbsoluteSavings() != null) {
                sum = sum.subtract(currentKafkaPromotion.getProductAbsoluteSavings());
            }
        }

        for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
            final PromotionKafkaDto currentKafkaPromotion = this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
            if (currentKafkaPromotion.getProductRelativeSavings() != null) {
                sum = sum.multiply(BigDecimal.ONE.subtract(currentKafkaPromotion.getProductRelativeSavings().divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP)));
            }
        }

        return sum;
    }

    public BigDecimal calculateSoldProductPrice(
            final LocalDateTime date,
            final KokuCustomerAppointmentSoldProductDomain currentSoldProduct,
            final List<KokuCustomerAppointmentPromotionDomain> promotions
    ) {
        BigDecimal sellPrice = currentSoldProduct.getPrice();
        if (sellPrice == null) {
            sellPrice = getProductHistoryDefaultPrice(date, this.productKTableProcessor.getProducts().get(currentSoldProduct.getProductId()));

            for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
                final PromotionKafkaDto currentKafkaPromotion = this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
                if (currentKafkaPromotion.getProductAbsoluteItemSavings() != null) {
                    sellPrice = sellPrice.subtract(currentKafkaPromotion.getProductAbsoluteItemSavings());
                }
            }
            for (final KokuCustomerAppointmentPromotionDomain currentPromotion : promotions) {
                final PromotionKafkaDto currentKafkaPromotion = this.promotionKTableProcessor.getPromotions().get(currentPromotion.getPromotionId());
                if (currentKafkaPromotion.getProductAbsoluteItemSavings() != null) {
                    sellPrice = sellPrice.multiply(BigDecimal.ONE.subtract(currentKafkaPromotion.getProductRelativeItemSavings().divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP)));
                }
            }
        }
        if (sellPrice.compareTo(BigDecimal.ZERO) < 0) {
            sellPrice = BigDecimal.ZERO;
        }
        return sellPrice;
    }

    private String calculateCustomerAppointmentSoldProductPriceSumFormatted(
            final LocalDateTime date,
            final List<KokuCustomerAppointmentSoldProductDomain> soldProducts,
            final List<KokuCustomerAppointmentPromotionDomain> promotions
    ) {
        return PRICE_FORMATTER.format(calculateCustomerAppointmentSoldProductPriceSum(date, soldProducts, promotions));
    }

    public String calculateCustomerAppointmentActivityDurationHumanReadable(
            final List<Long> activityIds
    ) {
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

    private Duration calculateCustomerAppointmentActivityDuration(
            final List<Long> activityIds
    ) {
        Duration sum = Duration.ZERO;
        if (activityIds != null) {
            for (final Long currentActivityId : activityIds) {
                final ActivityKafkaDto kafkaActivity = this.activityKTableProcessor.getActivities().get(currentActivityId);
                if (kafkaActivity.getApproximatelyDuration() != null) {
                    sum = sum.plus(kafkaActivity.getApproximatelyDuration());
                }
            }
        }
        return sum;
    }

    private BigDecimal getActivityHistoryDefaultPrice(
            final LocalDateTime date,
            final Long activityId
    ) {
        final ActivityKafkaDto kafkaActivity = this.activityKTableProcessor.getActivities().get(activityId);
        BigDecimal result = null;
        if (kafkaActivity.getPriceHistory() != null) {
            kafkaActivity.getPriceHistory().sort(Comparator.comparing(ActivityPriceHistoryKafkaDto::getRecorded));
            for (final ActivityPriceHistoryKafkaDto activityPriceHistoryKafkaDto : kafkaActivity.getPriceHistory()) {
                if (date.isAfter(activityPriceHistoryKafkaDto.getRecorded())) {
                    result = activityPriceHistoryKafkaDto.getPrice();
                } else {
                    break;
                }
            }
        }
        if (result == null && kafkaActivity.getPriceHistory() != null && !kafkaActivity.getPriceHistory().isEmpty()) {
            result = kafkaActivity.getPriceHistory().getFirst().getPrice();
        }
        if (result == null) {
            result = BigDecimal.ZERO;
        }
        return result;
    }

    private BigDecimal getProductHistoryDefaultPrice(
            final LocalDateTime date,
            final ProductKafkaDto product
    ) {
        BigDecimal result = null;
        if (product.getPriceHistory() != null) {
            product.getPriceHistory().sort(Comparator.comparing(ProductPriceHistoryKafkaDto::getRecorded));
            for (final ProductPriceHistoryKafkaDto productPriceHistoryKafkaDto : product.getPriceHistory()) {
                if (date.isAfter(productPriceHistoryKafkaDto.getRecorded())) {
                    result = productPriceHistoryKafkaDto.getPrice();
                } else {
                    break;
                }
            }
        }
        if (result == null && product.getPriceHistory() != null && !product.getPriceHistory().isEmpty()) {
            result = product.getPriceHistory().getFirst().getPrice();
        }
        if (result == null) {
            result = BigDecimal.ZERO;
        }
        return result;
    }

    public KokuCustomerActivityPriceSummaryDto transformToActivityPriceSummary(
            final KokuActivityPriceSummaryRequestDto request
    ) {
        final LocalDateTime targetTimestamp;
        if (request.getDate() != null && request.getTime() != null) {
            targetTimestamp = request.getDate().atTime(request.getTime());
        } else if (request.getDate() != null) {
            targetTimestamp = request.getDate().atTime(LocalTime.now());
        } else if (request.getTime() != null) {
            targetTimestamp = LocalDate.now().atTime(request.getTime());
        } else {
            targetTimestamp = LocalDateTime.now();
        }

        return KokuCustomerActivityPriceSummaryDto.builder()
                .priceSum(
                        this.calculateCustomerAppointmentActivityPriceSumFormatted(
                                targetTimestamp,
                                request.getActivities().stream().map(KokuCustomerAppointmentActivityDomain::fromDto).toList(),
                                request.getPromotions().stream().map(KokuCustomerAppointmentPromotionDomain::fromDto).toList()
                        )
                )
                .durationSum(
                        this.calculateCustomerAppointmentActivityDurationHumanReadable(
                                request.getActivities().stream().map(KokuCustomerAppointmentActivityDto::getActivityId).toList()
                        )
                )
                .build();
    }

    public KokuActivitySoldProductPriceSummaryDto transformToSoldProductPriceSummary(
            final KokuActivitySoldProductSummaryRequestDto request
    ) {
        final LocalDateTime targetTimestamp;
        if (request.getDate() != null && request.getTime() != null) {
            targetTimestamp = request.getDate().atTime(request.getTime());
        } else if (request.getDate() != null) {
            targetTimestamp = request.getDate().atTime(LocalTime.now());
        } else if (request.getTime() != null) {
            targetTimestamp = LocalDate.now().atTime(request.getTime());
        } else {
            targetTimestamp = LocalDateTime.now();
        }

        return KokuActivitySoldProductPriceSummaryDto.builder()
                .priceSum(
                        this.calculateCustomerAppointmentSoldProductPriceSumFormatted(
                                targetTimestamp,
                                request.getSoldProducts().stream().map(KokuCustomerAppointmentSoldProductDomain::fromDto).toList(),
                                request.getPromotions().stream().map(KokuCustomerAppointmentPromotionDomain::fromDto).toList()
                        )
                )
                .build();
    }
}
