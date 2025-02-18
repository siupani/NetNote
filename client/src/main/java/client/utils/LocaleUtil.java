package client.utils;

import javafx.scene.image.Image;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class LocaleUtil {

    public static final String BUNDLE_BASE_NAME = "locale/translations";
    private List<Locale> availableLocales;

    public List<Locale> getAvailableLocales() {
        // Lazy-loading
        if (availableLocales == null) {
            Set<ResourceBundle> resourceBundles = new HashSet<>();
            for (Locale locale : Locale.getAvailableLocales()) {
                try {
                    resourceBundles.add(ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale));
                } catch (MissingResourceException _) {
                    System.out.println("Not available");
                }
            }
            this.availableLocales = resourceBundles.stream()
                    .map(ResourceBundle::getLocale)
                    .filter(locale -> !locale.toLanguageTag().equals("und"))
                    .toList();
            System.out.println("Found " + availableLocales.size() + " available locales: "
                    + availableLocales.stream().map(Locale::toString).collect(Collectors.joining(", ")));
        }

        return availableLocales;
    }

    public Image getFlagImage(Locale locale) {
        try {
            return new Image("flags/" + locale.toLanguageTag() + ".png");
        } catch (IllegalArgumentException ex) {
            System.err.println("Loading flag image failed for " + locale);
            return null;
        }
    }

    /**
     * Maps a language string to its corresponding Locale object.
     *
     * @param language A string representing the language (e.g., "Dutch", "nl", "English").
     * @return The Locale object corresponding to the given language string.
     * Returns Locale.ENGLISH if the language is not recognized.
     */
    public Locale mapLanguageToLocale(String language) {
        return switch (language) {
            case "Dutch", "nl" -> new Locale("nl");
            case "Romanian", "ro" -> new Locale("ro");
            case "Bulgarian", "bg" -> new Locale("bg");
            case "Italian", "it" -> new Locale("it");
            case "English", "en" -> Locale.ENGLISH;
            default -> Locale.ENGLISH;
        };
    }

    /**
     * Maps a Locale object to its corresponding language string.
     *
     * @param locale The Locale object to be mapped.
     * @return A string representing the language of the given Locale (e.g., "Dutch", "English").
     * Returns "English" if the Locale is not recognized.
     */
    public String mapLocaleToLanguage(Locale locale) {
        return switch (locale.getLanguage()) {
            case "nl" -> "Dutch";
            case "ro" -> "Romanian";
            case "bg" -> "Bulgarian";
            case "it" -> "Italian";
            default -> "English";
        };
    }

}
