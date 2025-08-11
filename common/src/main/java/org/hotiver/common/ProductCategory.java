package org.hotiver.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ProductCategory {
    ELECTRONICS("electronics"),
    COMPUTERS("computers-and-accessories"),
    PHONES_GADGETS("phones-and-gadgets"),
    HOME_COMFORT("home-and-comfort"),
    FURNITURE("furniture"),
    GARDEN_COTTAGE("garden-and-country"),
    PET_SUPPLIES("pet-supplies"),
    KIDS("kids-products"),
    CLOTHING_SHOES("clothing-and-shoes"),
    BEAUTY_HEALTH("beauty-and-health"),
    SPORTS_OUTDOORS("sports-and-outdoors"),
    AUTO_PRODUCTS("auto-products"),
    STATIONERY_BOOKS("stationery-and-books"),
    TRAVEL("travel-and-tourism"),
    FOOD_BEVERAGES("food-and-beverages"),
    HOBBIES_CREATIVITY("hobbies-and-creativity"),
    REPAIR_TOOLS("repair-and-tools"),
    SMART_HOME("smart-home"),
    GAMES_ENTERTAINMENT("games-and-entertainment"),
    JEWELRY_ACCESSORIES("jewelry-and-accessories"),
    GADGET_ACCESSORIES("gadget-accessories"),
    SECURITY_SAFETY("security-and-safety"),
    FLOWERS_PLANTS("flowers-and-plants"),
    MUSICAL_INSTRUMENTS("musical-instruments"),
    COLLECTIBLES("collectibles");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public record CategoryDto(String name, String displayName) {}

    public static List<CategoryDto> getAllCategoriesDto() {
        return Arrays.stream(ProductCategory.values())
                .map(c -> new CategoryDto(c.name(), c.toString()))
                .collect(Collectors.toList());
    }
}
