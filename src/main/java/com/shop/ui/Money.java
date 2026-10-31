package com.shop.ui;

import java.text.DecimalFormat;

public final class Money {

    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.00");

    private Money() {
    }

    public static String format(double amount) {
        return "Rs. " + FORMAT.format(amount);
    }
}
