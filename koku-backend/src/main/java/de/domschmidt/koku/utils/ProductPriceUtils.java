package de.domschmidt.koku.utils;

import de.domschmidt.koku.persistence.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProductPriceUtils {

    private ProductPriceUtils() {
    }

    public static BigDecimal getSoldProductPriceForCustomerAppointment(final CustomerAppointmentSoldProduct soldProduct,
                                                                       final CustomerAppointment customerAppointment) {
        final List<Promotion> promotions = customerAppointment.getPromotions();
        BigDecimal result;
        if (soldProduct.getSellPrice() != null) {
            result = soldProduct.getSellPrice();

            if (promotions != null && !promotions.isEmpty()) {
                final Map<CustomerAppointmentSoldProduct, BigDecimal> pricePerProductMap = new HashMap<>();

                for (final CustomerAppointmentSoldProduct oneProductOfAllProducts : customerAppointment.getSoldProducts()) {
                    if (oneProductOfAllProducts.getSellPrice() != null) {
                        pricePerProductMap.put(oneProductOfAllProducts, oneProductOfAllProducts.getSellPrice());
                    } else {
                        pricePerProductMap.put(oneProductOfAllProducts, getPriceFromHistory(
                                oneProductOfAllProducts.getProduct(),
                                customerAppointment.getStart()
                        ));
                    }
                }
                // abs
                applyAbsoluteModification(promotions, pricePerProductMap);

                // rel
                applyRelativeModification(promotions, pricePerProductMap);

                result = pricePerProductMap.get(soldProduct);
            }
        } else {
            result = getPriceFromHistory(soldProduct.getProduct(), customerAppointment.getStart());

            if (promotions != null && !promotions.isEmpty()) {
                final Map<CustomerAppointmentSoldProduct, BigDecimal> pricePerProductMap = new HashMap<>();

                for (final CustomerAppointmentSoldProduct oneProductOfAllProducts : customerAppointment.getSoldProducts()) {
                    if (oneProductOfAllProducts.getSellPrice() != null) {
                        pricePerProductMap.put(oneProductOfAllProducts, oneProductOfAllProducts.getSellPrice());
                    } else {
                        pricePerProductMap.put(oneProductOfAllProducts, getPriceFromHistory(
                                oneProductOfAllProducts.getProduct(),
                                customerAppointment.getStart()
                        ));
                    }
                }

                // abs item
                applyAbsoluteItemModification(promotions, pricePerProductMap);

                // rel item
                applyRelativeItemModification(promotions, pricePerProductMap);

                // abs
                applyAbsoluteModification(promotions, pricePerProductMap);

                // rel
                applyRelativeModification(promotions, pricePerProductMap);

                result = pricePerProductMap.get(soldProduct);
            }
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private static void applyRelativeModification(List<Promotion> promotions, Map<CustomerAppointmentSoldProduct, BigDecimal> pricePerProductMap) {
        BigDecimal sumOfAllProducts = BigDecimal.ZERO;
        for (final Map.Entry<CustomerAppointmentSoldProduct, BigDecimal> customerAppointmentSoldProductBigDecimalEntry : pricePerProductMap.entrySet()) {
            sumOfAllProducts = sumOfAllProducts.add(customerAppointmentSoldProductBigDecimalEntry.getValue());
        }

        BigDecimal relativeSavingMultiplierSum = BigDecimal.ONE;
        for (final Promotion promotion : promotions) {
            if (promotion.getPromotionProductSettings().getRelativeSavings() != null) {
                relativeSavingMultiplierSum = relativeSavingMultiplierSum.multiply(
                        BigDecimal.ONE.add(
                                promotion.getPromotionProductSettings().getRelativeSavings().divide(
                                        new BigDecimal(100),
                                        RoundingMode.HALF_UP
                                )
                        )
                );
            }
        }
        if (relativeSavingMultiplierSum.compareTo(BigDecimal.ONE) > 0 && sumOfAllProducts.compareTo(BigDecimal.ZERO) > 0) {
            for (final Map.Entry<CustomerAppointmentSoldProduct, BigDecimal> customerAppointmentSoldProductBigDecimalEntry : pricePerProductMap.entrySet()) {
                final BigDecimal currentValue = customerAppointmentSoldProductBigDecimalEntry.getValue();
                // price - ((price / <sum of all product prices without absolute savings >) * (<sum of all product prices without absolute savings > * <multiplier sum>)
                BigDecimal newValue = currentValue.subtract( // 50 -
                        (currentValue.divide(sumOfAllProducts, RoundingMode.HALF_UP)) // 50/200
                                .multiply( // * 23,25
                                        sumOfAllProducts.multiply(
                                                relativeSavingMultiplierSum.subtract(BigDecimal.ONE)
                                        )
                                )
                );
                if (newValue.compareTo(BigDecimal.ZERO) < 0) {
                    newValue = BigDecimal.ZERO;
                }
                customerAppointmentSoldProductBigDecimalEntry.setValue(newValue);
            }
        }
    }

    private static void applyAbsoluteModification(List<Promotion> promotions, Map<CustomerAppointmentSoldProduct, BigDecimal> pricePerProductMap) {
        // price - ((price / <sum of all product prices>) * <absolute saving sum>)
        BigDecimal sumOfAllProducts = BigDecimal.ZERO;
        for (final Map.Entry<CustomerAppointmentSoldProduct, BigDecimal> customerAppointmentSoldProductBigDecimalEntry : pricePerProductMap.entrySet()) {
            sumOfAllProducts = sumOfAllProducts.add(customerAppointmentSoldProductBigDecimalEntry.getValue());
        }

        BigDecimal sumOfPromotionAbsoluteSavings = BigDecimal.ZERO;
        for (final Promotion promotion : promotions) {
            if (promotion.getPromotionProductSettings().getAbsoluteSavings() != null) {
                sumOfPromotionAbsoluteSavings = sumOfPromotionAbsoluteSavings.add(promotion.getPromotionProductSettings().getAbsoluteSavings());
            }
        }

        for (final Map.Entry<CustomerAppointmentSoldProduct, BigDecimal> customerAppointmentSoldProductBigDecimalEntry : pricePerProductMap.entrySet()) {
            final BigDecimal currentValue = customerAppointmentSoldProductBigDecimalEntry.getValue();
            BigDecimal newValue = currentValue.subtract((currentValue.divide(sumOfAllProducts, RoundingMode.HALF_UP)).multiply(sumOfPromotionAbsoluteSavings));
            if (newValue.compareTo(BigDecimal.ZERO) < 0) {
                newValue = BigDecimal.ZERO;
            }
            customerAppointmentSoldProductBigDecimalEntry.setValue(newValue);
        }

    }

    private static void applyRelativeItemModification(List<Promotion> promotions, Map<CustomerAppointmentSoldProduct, BigDecimal> pricePerProductMap) {
        for (final Promotion currentPromotion : promotions) {
            if (currentPromotion.getPromotionProductSettings().getRelativeItemSavings() != null) {
                for (final Map.Entry<CustomerAppointmentSoldProduct, BigDecimal> customerAppointmentSoldProductBigDecimalEntry : pricePerProductMap.entrySet()) {
                    BigDecimal newValue = customerAppointmentSoldProductBigDecimalEntry.getValue().multiply(BigDecimal.ONE.subtract(currentPromotion.getPromotionProductSettings().getRelativeItemSavings().divide(new BigDecimal(100), RoundingMode.HALF_UP)));
                    if (newValue.compareTo(BigDecimal.ZERO) < 0) {
                        newValue = BigDecimal.ZERO;
                    }
                    customerAppointmentSoldProductBigDecimalEntry.setValue(newValue);
                }
            }
        }
    }

    private static void applyAbsoluteItemModification(List<Promotion> promotions, Map<CustomerAppointmentSoldProduct, BigDecimal> pricePerProductMap) {
        for (final Promotion currentPromotion : promotions) {
            if (currentPromotion.getPromotionProductSettings().getAbsoluteItemSavings() != null) {
                for (final Map.Entry<CustomerAppointmentSoldProduct, BigDecimal> customerAppointmentSoldProductBigDecimalEntry : pricePerProductMap.entrySet()) {
                    BigDecimal newValue = customerAppointmentSoldProductBigDecimalEntry.getValue().subtract(currentPromotion.getPromotionProductSettings().getAbsoluteItemSavings());
                    if (newValue.compareTo(BigDecimal.ZERO) < 0) {
                        newValue = BigDecimal.ZERO;
                    }
                    customerAppointmentSoldProductBigDecimalEntry.setValue(newValue);
                }
            }
        }
    }

    public static BigDecimal getPriceFromHistory(final Product product,
                                                  final LocalDateTime date) {
        BigDecimal result;
        final List<ProductPriceHistoryEntry> currentProductCurrentPriceHistoryEntry = product.getPriceHistory();
        if (currentProductCurrentPriceHistoryEntry != null && !currentProductCurrentPriceHistoryEntry.isEmpty()) {
            ProductPriceHistoryEntry priceToTake = null;
            for (final ProductPriceHistoryEntry currentPriceInHistory : currentProductCurrentPriceHistoryEntry) {
                if (currentPriceInHistory.getRecorded().isBefore(date)) {
                    priceToTake = currentPriceInHistory;
                }
            }
            if (priceToTake != null) {
                result = NPEGuardUtils.get(priceToTake.getPrice());
            } else if (currentProductCurrentPriceHistoryEntry.get(0) != null) {
                result = NPEGuardUtils.get(currentProductCurrentPriceHistoryEntry.get(0).getPrice());
            } else {
                result = BigDecimal.ZERO;
            }
        } else {
            result = BigDecimal.ZERO;
        }
        return result;
    }
}
