package com.vertex.vos.Utilities;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class AmountsToWord {

    private static final String[] tensNames = {
            "", " Ten", " Twenty", " Thirty", " Forty", " Fifty", " Sixty", " Seventy", " Eighty", " Ninety"
    };

    private static final String[] numNames = {
            "", " One", " Two", " Three", " Four", " Five", " Six", " Seven", " Eight", " Nine", " Ten",
            " Eleven", " Twelve", " Thirteen", " Fourteen", " Fifteen", " Sixteen", " Seventeen", " Eighteen", " Nineteen"
    };

    private static String convertLessThanOneThousand(int number) {
        String current;

        if (number % 100 < 20) {
            current = numNames[number % 100];
            number /= 100;
        } else {
            current = numNames[number % 10];
            number /= 10;

            current = tensNames[number % 10] + current;
            number /= 10;
        }
        if (number == 0) return current;
        return numNames[number] + " Hundred" + current;
    }

    public static String convert(BigDecimal number) {
        if (number.equals(BigDecimal.ZERO)) { return "Zero"; }

        String snumber = number.toPlainString();

        // Split the number into whole and fractional parts
        String[] parts = snumber.split("\\.");
        String wholePart = parts[0];
        String fractionalPart = parts.length > 1 ? parts[1] : "";

        // Convert the whole part to words
        long wholeNumber = Long.parseLong(wholePart);
        String words = convert(wholeNumber);

        // If there's a fractional part, convert it to words
        if (!fractionalPart.isEmpty()) {
            words += " And " + convertFractionalPart(fractionalPart);
        }

        return capitalizeFirstLetter(words.trim());
    }

    private static String convertFractionalPart(String fractionalPart) {
        StringBuilder words = new StringBuilder();
        for (char digit : fractionalPart.toCharArray()) {
            words.append(numNames[Character.getNumericValue(digit)]).append(" ");
        }
        words.append("Cents");
        return words.toString().trim();
    }

    private static String convert(long number) {
        if (number == 0) { return "Zero"; }

        String snumber = Long.toString(number);

        String mask = "000000000000";
        DecimalFormat df = new DecimalFormat(mask);
        snumber = df.format(number);

        int billions = Integer.parseInt(snumber.substring(0, 3));
        int millions  = Integer.parseInt(snumber.substring(3, 6));
        int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
        int thousands = Integer.parseInt(snumber.substring(9, 12));

        String tradBillions;
        switch (billions) {
            case 0: tradBillions = ""; break;
            case 1: tradBillions = convertLessThanOneThousand(billions) + " Billion "; break;
            default: tradBillions = convertLessThanOneThousand(billions) + " Billion ";
        }
        String result =  tradBillions;

        String tradMillions;
        switch (millions) {
            case 0: tradMillions = ""; break;
            case 1: tradMillions = convertLessThanOneThousand(millions) + " Million "; break;
            default: tradMillions = convertLessThanOneThousand(millions) + " Million ";
        }
        result =  result + tradMillions;

        String tradHundredThousands;
        switch (hundredThousands) {
            case 0: tradHundredThousands = ""; break;
            case 1: tradHundredThousands = "One Thousand "; break;
            default: tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " Thousand ";
        }
        result =  result + tradHundredThousands;

        String tradThousand;
        tradThousand = convertLessThanOneThousand(thousands);
        result =  result + tradThousand;

        return result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
    }

    private static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder capitalizedStr = new StringBuilder();
        boolean capitalize = true;

        for (char ch : str.toCharArray()) {
            if (Character.isWhitespace(ch)) {
                capitalize = true;
            } else if (capitalize) {
                ch = Character.toUpperCase(ch);
                capitalize = false;
            }
            capitalizedStr.append(ch);
        }

        return capitalizedStr.toString();
    }

    // Method to convert BigDecimal to words
    public static String convertToWords(BigDecimal amount) {
        return convert(amount).trim();
    }
}
