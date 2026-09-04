package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Модуль расчёта цены заказа. Намеренно почти не покрыт тестами и
 * содержит magic numbers / нечитаемые ветвления скидок - цель для
 * характеризационных тестов (ЛР2) и mutation-testing гейта PIT (ЛР5).
 */
public class PricingCalculator {

    public record LineItem(BigDecimal price, int quantity) {
    }

    private static final BigDecimal EXPENSIVE_PERCENTAGE_MULTIPLIER = BigDecimal.valueOf(0.98);
    private static final BigDecimal BULK_PERCENTAGE_MULTIPLIER = BigDecimal.valueOf(0.95);
    private static final BigDecimal VIP_PERCENTAGE_MULTIPLIER = BigDecimal.valueOf(0.9);
    private static final BigDecimal WHOLESALE_PERCENTAGE_MULTIPLIER = BigDecimal.valueOf(0.85);
    private static final BigDecimal COUPON_PERCENTAGE_MULTIPLIER = BigDecimal.valueOf(0.8);

    private static final BigDecimal CURRENCY_0 = BigDecimal.ZERO;
    private static final BigDecimal CURRENCY_10 = BigDecimal.valueOf(10);
    private static final BigDecimal CURRENCY_1000 = BigDecimal.valueOf(1000);

    private static final int ITEM_QUANTITY_10 = 10;

    private static final String VIP_CUSTOMER_STATUS = "vip";
    private static final String WHOLESALE_CUSTOMER_STATUS = "wholesale";
    private static final String SAVE_10_FLAT_COUPON_CODE = "SAVE10";
    private static final String SAVE_20_PERCENT_COUPON_CODE = "SAVE20PERCENT";

    public BigDecimal calculateOrderTotal(List<LineItem> items, String customerType, String couponCode) {
        BigDecimal total = CURRENCY_0;

        for (LineItem item : items) {
            BigDecimal linePrice = item.price().multiply(BigDecimal.valueOf(item.quantity()));
            if (item.quantity() > ITEM_QUANTITY_10) {
                linePrice = linePrice.multiply(BULK_PERCENTAGE_MULTIPLIER);
            }
            total = total.add(linePrice);
        }

        if (VIP_CUSTOMER_STATUS.equals(customerType)) {
            total = total.multiply(VIP_PERCENTAGE_MULTIPLIER);
        } else if (WHOLESALE_CUSTOMER_STATUS.equals(customerType)) {
            total = total.multiply(WHOLESALE_PERCENTAGE_MULTIPLIER);
        }

        if (SAVE_10_FLAT_COUPON_CODE.equals(couponCode)) {
            total = total.subtract(CURRENCY_10);
        } else if (SAVE_20_PERCENT_COUPON_CODE.equals(couponCode)) {
            total = total.multiply(COUPON_PERCENTAGE_MULTIPLIER);
        }

        if (total.compareTo(CURRENCY_0) < 0) {
            total = CURRENCY_0;
        }

        if (total.compareTo(CURRENCY_1000) > 0) {
            total = total.multiply(EXPENSIVE_PERCENTAGE_MULTIPLIER);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
