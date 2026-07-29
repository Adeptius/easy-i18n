package de.marhali.easyi18n.unitalk.utils;

import de.marhali.easyi18n.core.domain.model.LocaleId;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class LanguageSorter {

    public static void sortLocales(List<@NotNull LocaleId> localeIds) {
        localeIds.sort(Comparator.comparingInt((LocaleId l) -> switch (l.tag()) {
            case "en" -> 0;
            case "uk" -> 1;
            case "ru" -> 2;
            default -> 3;
        }).thenComparing(Comparator.naturalOrder()));
    }

    public static List<LocaleId> sortLocales(Set<@NotNull LocaleId> localeIds) {
        return localeIds.stream()
            .sorted(Comparator.comparingInt((LocaleId l) -> switch (l.tag()) {
                case "en" -> 0;
                case "uk" -> 1;
                case "ru" -> 2;
                default -> 3;
            }).thenComparing(Comparator.naturalOrder()))
            .toList();
    }

}
