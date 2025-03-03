package com.vertex.vos.Utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MatchColorManager {
    private final Map<Integer, String> matchColors = new HashMap<>();
    private final Random random = new Random();

    public String getColorForMatch(int matchNo) {
        return matchColors.computeIfAbsent(matchNo, key -> {
            if (key == 0) {
                return null;
            }
            int r = random.nextInt(156) + 100;  // Avoid too dark colors
            int g = random.nextInt(156) + 100;
            int b = random.nextInt(156) + 100;
            return String.format("rgb(%d, %d, %d)", r, g, b);
        });
    }
}