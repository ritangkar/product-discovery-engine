package com.ritangkar.productdiscovery.service;

import com.ritangkar.productdiscovery.model.StructuredQuery;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stage 2: Intent Extraction. Every field is extracted independently by a
 * dictionary or regex match against the literal query text — nothing here
 * infers a value the query doesn't contain evidence for. See
 * docs/design-decisions.md for why this is a deterministic default rather
 * than an LLM call, and where an LLM-backed alternative would plug in.
 */
@Service
public class IntentExtractionService {

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new LinkedHashMap<>();
    private static final List<String> COLORS = List.of(
            "black", "white", "red", "blue", "navy", "grey", "gray", "green", "orange", "olive", "brown"
    );
    private static final List<Map.Entry<String, List<String>>> USE_CASE_PHRASES = new ArrayList<>();

    static {
        CATEGORY_KEYWORDS.put("Footwear", List.of("footwear", "shoe", "shoes", "sneaker", "sneakers", "boot", "boots", "running shoe"));
        CATEGORY_KEYWORDS.put("Apparel", List.of("apparel", "jacket", "shirt", "tee", "t-shirt", "tights", "joggers", "windbreaker", "base layer"));
        CATEGORY_KEYWORDS.put("Accessories", List.of("accessories", "backpack", "cap", "gloves", "bottle", "pack", "visor"));
        CATEGORY_KEYWORDS.put("Outdoor Gear", List.of("outdoor gear", "tent", "sleeping bag", "trekking pole", "poles", "camp stove", "stove"));
        CATEGORY_KEYWORDS.put("Electronics", List.of("electronics", "earbuds", "watch", "monitor", "tracker"));

        USE_CASE_PHRASES.add(Map.entry("long-distance running", List.of("long distance", "long-distance", "marathon")));
        USE_CASE_PHRASES.add(Map.entry("trail running", List.of("trail running", "trail run")));
        USE_CASE_PHRASES.add(Map.entry("sprint training", List.of("sprint")));
        USE_CASE_PHRASES.add(Map.entry("daily training", List.of("daily training", "everyday training")));
        USE_CASE_PHRASES.add(Map.entry("hiking", List.of("hiking", "hike", "trek")));
        USE_CASE_PHRASES.add(Map.entry("camping", List.of("camping", "camp")));
        USE_CASE_PHRASES.add(Map.entry("training", List.of("training", "gym", "workout")));
        USE_CASE_PHRASES.add(Map.entry("everyday", List.of("everyday", "daily wear", "casual")));
    }

    private static final Pattern BETWEEN_PATTERN = Pattern.compile(
            "between\\s*(?:\u20B9|rs\\.?|inr)?\\s*(\\d+)(k)?\\s*(?:and|-|to)\\s*(?:\u20B9|rs\\.?|inr)?\\s*(\\d+)(k)?");
    private static final Pattern UNDER_PATTERN = Pattern.compile(
            "(?:under|below|less than|up to)\\s*(?:\u20B9|rs\\.?|inr)?\\s*(\\d+)(k)?");
    private static final Pattern OVER_PATTERN = Pattern.compile(
            "(?:over|above|more than)\\s*(?:\u20B9|rs\\.?|inr)?\\s*(\\d+)(k)?");

    public StructuredQuery extract(String rawQuery, List<String> knownBrands) {
        String normalized = rawQuery.toLowerCase(Locale.ROOT).replace(",", "");
        List<String> extractedFrom = new ArrayList<>();

        String category = extractCategory(normalized, extractedFrom);
        String color = extractColor(normalized, extractedFrom);
        String brand = extractBrand(rawQuery, knownBrands, extractedFrom);
        int[] priceRange = extractPriceRange(normalized, extractedFrom);
        String useCase = extractUseCase(normalized, extractedFrom);

        return new StructuredQuery(rawQuery, category, color, brand,
                priceRange[0] < 0 ? null : priceRange[0], priceRange[1] < 0 ? null : priceRange[1],
                useCase, extractedFrom);
    }

    private String extractCategory(String q, List<String> extractedFrom) {
        String best = null;
        int bestLen = 0;
        for (var entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String kw : entry.getValue()) {
                if (q.contains(kw) && kw.length() > bestLen) {
                    best = entry.getKey();
                    bestLen = kw.length();
                }
            }
        }
        if (best != null) extractedFrom.add("category \u2190 matched keyword in query");
        return best;
    }

    private String extractColor(String q, List<String> extractedFrom) {
        for (String c : COLORS) {
            if (Pattern.compile("\\b" + c + "\\b").matcher(q).find()) {
                String canonical = c.equals("gray") ? "Grey" : capitalize(c);
                extractedFrom.add("color \u2190 \"" + c + "\"");
                return canonical;
            }
        }
        return null;
    }

    private String extractBrand(String rawQuery, List<String> knownBrands, List<String> extractedFrom) {
        if (knownBrands == null) return null;
        String lower = rawQuery.toLowerCase(Locale.ROOT);
        for (String brand : knownBrands) {
            if (lower.contains(brand.toLowerCase(Locale.ROOT))) {
                extractedFrom.add("brand \u2190 \"" + brand + "\"");
                return brand;
            }
        }
        return null;
    }

    /** Returns {minPrice, maxPrice}, -1 for whichever wasn't found. */
    private int[] extractPriceRange(String q, List<String> extractedFrom) {
        Matcher between = BETWEEN_PATTERN.matcher(q);
        if (between.find()) {
            int min = parseAmount(between.group(1), between.group(2));
            int max = parseAmount(between.group(3), between.group(4));
            extractedFrom.add("price range \u2190 \"" + between.group() + "\"");
            return new int[]{min, max};
        }

        int min = -1, max = -1;
        Matcher under = UNDER_PATTERN.matcher(q);
        if (under.find()) {
            max = parseAmount(under.group(1), under.group(2));
            extractedFrom.add("maxPrice \u2190 \"" + under.group() + "\"");
        }
        Matcher over = OVER_PATTERN.matcher(q);
        if (over.find()) {
            min = parseAmount(over.group(1), over.group(2));
            extractedFrom.add("minPrice \u2190 \"" + over.group() + "\"");
        }
        return new int[]{min, max};
    }

    private int parseAmount(String digits, String kSuffix) {
        int value = Integer.parseInt(digits);
        return kSuffix != null ? value * 1000 : value;
    }

    private String extractUseCase(String q, List<String> extractedFrom) {
        String normalizedHyphens = q.replace("-", " ");
        String best = null;
        int bestLen = 0;
        for (var entry : USE_CASE_PHRASES) {
            for (String phrase : entry.getValue()) {
                String phraseNorm = phrase.replace("-", " ");
                if (normalizedHyphens.contains(phraseNorm) && phraseNorm.length() > bestLen) {
                    best = entry.getKey();
                    bestLen = phraseNorm.length();
                }
            }
        }
        if (best != null) extractedFrom.add("useCase \u2190 matched phrase in query");
        return best;
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}
