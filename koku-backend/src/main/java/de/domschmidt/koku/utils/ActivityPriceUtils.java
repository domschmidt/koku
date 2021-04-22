package de.domschmidt.koku.utils;

import de.domschmidt.koku.persistence.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ActivityPriceUtils {

    private ActivityPriceUtils() {
    }

    public static BigDecimal getActivityPriceForCustomerAppointment(final CustomerAppointmentActivity activity,
                                                                    final CustomerAppointment customerAppointment) {
        final List<Promotion> promotions = customerAppointment.getPromotions();
        BigDecimal result;
        if (activity.getSellPrice() != null) {
            result = activity.getSellPrice();

            if (promotions != null && !promotions.isEmpty()) {
                final Map<CustomerAppointmentActivity, BigDecimal> pricePerActivityMap = new HashMap<>();

                for (final CustomerAppointmentActivity oneActivityOfAllActivities : customerAppointment.getActivities()) {
                    if (oneActivityOfAllActivities.getSellPrice() != null) {
                        pricePerActivityMap.put(oneActivityOfAllActivities, oneActivityOfAllActivities.getSellPrice());
                    } else {
                        pricePerActivityMap.put(oneActivityOfAllActivities, getPriceFromHistory(
                                oneActivityOfAllActivities.getActivity(),
                                customerAppointment.getStart()
                        ));
                    }
                }
                // abs
                applyAbsoluteModification(promotions, pricePerActivityMap);

                // rel
                applyRelativeModification(promotions, pricePerActivityMap);

                result = pricePerActivityMap.get(activity);
            }
        } else {
            result = getPriceFromHistory(
                    activity.getActivity(),
                    customerAppointment.getStart()
            );

            if (promotions != null && !promotions.isEmpty()) {
                final Map<CustomerAppointmentActivity, BigDecimal> pricePerActivityMap = new HashMap<>();

                for (final CustomerAppointmentActivity oneActivityOfAllActivitys : customerAppointment.getActivities()) {
                    if (oneActivityOfAllActivitys.getSellPrice() != null) {
                        pricePerActivityMap.put(oneActivityOfAllActivitys, oneActivityOfAllActivitys.getSellPrice());
                    } else {
                        pricePerActivityMap.put(oneActivityOfAllActivitys, getPriceFromHistory(
                                oneActivityOfAllActivitys.getActivity(),
                                customerAppointment.getStart()
                        ));
                    }
                }

                // abs item
                applyAbsoluteItemModification(promotions, pricePerActivityMap);

                // rel item
                applyRelativeItemModification(promotions, pricePerActivityMap);

                // abs
                applyAbsoluteModification(promotions, pricePerActivityMap);

                // rel
                applyRelativeModification(promotions, pricePerActivityMap);

                result = pricePerActivityMap.get(activity);
            }
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private static void applyRelativeModification(List<Promotion> promotions, Map<CustomerAppointmentActivity, BigDecimal> pricePerActivityMap) {
        BigDecimal sumOfAllActivitys = BigDecimal.ZERO;
        for (final Map.Entry<CustomerAppointmentActivity, BigDecimal> CustomerAppointmentActivityBigDecimalEntry : pricePerActivityMap.entrySet()) {
            sumOfAllActivitys = sumOfAllActivitys.add(CustomerAppointmentActivityBigDecimalEntry.getValue());
        }

        BigDecimal relativeSavingMultiplierSum = BigDecimal.ONE;
        for (final Promotion promotion : promotions) {
            if (promotion.getPromotionActivitySettings().getRelativeSavings() != null) {
                relativeSavingMultiplierSum = relativeSavingMultiplierSum.multiply(
                        BigDecimal.ONE.add(
                                promotion.getPromotionActivitySettings().getRelativeSavings().divide(
                                        new BigDecimal(100),
                                        RoundingMode.HALF_UP
                                )
                        )
                );
            }
        }
        if (relativeSavingMultiplierSum.compareTo(BigDecimal.ONE) > 0 && sumOfAllActivitys.compareTo(BigDecimal.ZERO) > 0) {
            for (final Map.Entry<CustomerAppointmentActivity, BigDecimal> CustomerAppointmentActivityBigDecimalEntry : pricePerActivityMap.entrySet()) {
                final BigDecimal currentValue = CustomerAppointmentActivityBigDecimalEntry.getValue();
                // price - ((price / <sum of all Activity prices without absolute savings >) * (<sum of all Activity prices without absolute savings > * <multiplier sum>)
                BigDecimal newValue = currentValue.subtract( // 50 -
                        (currentValue.divide(sumOfAllActivitys, RoundingMode.HALF_UP)) // 50/200
                                .multiply( // * 23,25
                                        sumOfAllActivitys.multiply(
                                                relativeSavingMultiplierSum.subtract(BigDecimal.ONE)
                                        )
                                )
                );
                if (newValue.compareTo(BigDecimal.ZERO) < 0) {
                    newValue = BigDecimal.ZERO;
                }
                CustomerAppointmentActivityBigDecimalEntry.setValue(newValue);
            }
        }
    }

    private static void applyAbsoluteModification(
            final List<Promotion> promotions,
            final Map<CustomerAppointmentActivity, BigDecimal> pricePerActivityMap
    ) {
        // price - ((price / <sum of all Activity prices>) * <absolute saving sum>)
        BigDecimal sumOfAllActivitys = BigDecimal.ZERO;
        for (final Map.Entry<CustomerAppointmentActivity, BigDecimal> CustomerAppointmentActivityBigDecimalEntry : pricePerActivityMap.entrySet()) {
            sumOfAllActivitys = sumOfAllActivitys.add(CustomerAppointmentActivityBigDecimalEntry.getValue());
        }

        BigDecimal sumOfPromotionAbsoluteSavings = BigDecimal.ZERO;
        for (final Promotion promotion : promotions) {
            if (promotion.getPromotionActivitySettings().getAbsoluteSavings() != null) {
                sumOfPromotionAbsoluteSavings = sumOfPromotionAbsoluteSavings.add(promotion.getPromotionActivitySettings().getAbsoluteSavings());
            }
        }

        for (final Map.Entry<CustomerAppointmentActivity, BigDecimal> CustomerAppointmentActivityBigDecimalEntry : pricePerActivityMap.entrySet()) {
            final BigDecimal currentValue = CustomerAppointmentActivityBigDecimalEntry.getValue();
            BigDecimal newValue = currentValue.subtract((currentValue.divide(sumOfAllActivitys, RoundingMode.HALF_UP)).multiply(sumOfPromotionAbsoluteSavings));
            if (newValue.compareTo(BigDecimal.ZERO) < 0) {
                newValue = BigDecimal.ZERO;
            }
            CustomerAppointmentActivityBigDecimalEntry.setValue(newValue);
        }

    }

    private static void applyRelativeItemModification(List<Promotion> promotions, Map<CustomerAppointmentActivity, BigDecimal> pricePerActivityMap) {
        for (final Promotion currentPromotion : promotions) {
            if (currentPromotion.getPromotionActivitySettings().getRelativeItemSavings() != null) {
                for (final Map.Entry<CustomerAppointmentActivity, BigDecimal> CustomerAppointmentActivityBigDecimalEntry : pricePerActivityMap.entrySet()) {
                    BigDecimal newValue = CustomerAppointmentActivityBigDecimalEntry.getValue().multiply(BigDecimal.ONE.subtract(currentPromotion.getPromotionActivitySettings().getRelativeItemSavings().divide(new BigDecimal(100), RoundingMode.HALF_UP)));
                    if (newValue.compareTo(BigDecimal.ZERO) < 0) {
                        newValue = BigDecimal.ZERO;
                    }
                    CustomerAppointmentActivityBigDecimalEntry.setValue(newValue);
                }
            }
        }
    }

    private static void applyAbsoluteItemModification(List<Promotion> promotions, Map<CustomerAppointmentActivity, BigDecimal> pricePerActivityMap) {
        for (final Promotion currentPromotion : promotions) {
            if (currentPromotion.getPromotionActivitySettings().getAbsoluteItemSavings() != null) {
                for (final Map.Entry<CustomerAppointmentActivity, BigDecimal> CustomerAppointmentActivityBigDecimalEntry : pricePerActivityMap.entrySet()) {
                    BigDecimal newValue = CustomerAppointmentActivityBigDecimalEntry.getValue().subtract(currentPromotion.getPromotionActivitySettings().getAbsoluteItemSavings());
                    if (newValue.compareTo(BigDecimal.ZERO) < 0) {
                        newValue = BigDecimal.ZERO;
                    }
                    CustomerAppointmentActivityBigDecimalEntry.setValue(newValue);
                }
            }
        }
    }

    public static BigDecimal getPriceFromHistory(final Activity activity,
                                                 final LocalDateTime date) {
        BigDecimal result;
        final List<ActivityPriceHistoryEntry> currentActivityCurrentPriceHistoryEntry = activity.getPriceHistory();
        if (currentActivityCurrentPriceHistoryEntry != null && !currentActivityCurrentPriceHistoryEntry.isEmpty()) {
            ActivityPriceHistoryEntry priceToTake = null;
            for (final ActivityPriceHistoryEntry currentPriceInHistory : currentActivityCurrentPriceHistoryEntry) {
                if (currentPriceInHistory.getRecorded().isBefore(date)) {
                    priceToTake = currentPriceInHistory;
                }
            }
            if (priceToTake != null) {
                result = NPEGuardUtils.get(priceToTake.getPrice());
            } else if (currentActivityCurrentPriceHistoryEntry.get(0) != null) {
                result = NPEGuardUtils.get(currentActivityCurrentPriceHistoryEntry.get(0).getPrice());
            } else {
                result = BigDecimal.ZERO;
            }
        } else {
            result = BigDecimal.ZERO;
        }
        return result;
    }
}
