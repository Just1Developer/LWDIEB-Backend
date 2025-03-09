package net.justonedev.lwdiebbackend.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import de.dieb.dashboard.backend.model.widgetdata.football.TeamResultsById;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * This class is used to configure the cache by using Caffeine as the caching provider.
 * <p>
 * These following caches are configured:
 *<ul>
 *     <li><b>30s-cache:</b> Expires after 30 seconds.</li>
 *     <li><b>10m-cache:</b> Expires after 10 minutes.</li>
 *     <li><b>1h-cache:</b> Expires after 1 hour.</li>
 *     <li><b>12h-cache:</b> Expires after 12 hours.</li>
 *     <li><b>24h-cache:</b> Expires after 24 hours.</li>
 *     <li><b>football-cache:</b> Dynamically expires based on match timing: If there is any current run shown match, the cache time is reduced for example.</li>
 *</ul>
 *
 * The caches are managed by using {@link SimpleCacheManager}.
 */
@Configuration
@EnableCaching
@AllArgsConstructor
public class CacheConfiguration {

    /**
     * Creates and configures a {@link CacheManager} with caches.
     *
     * @return the configured CacheManager instance.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCache thirtySecondCache =
                new CaffeineCache(
                        "30s-cache",
                        Caffeine.newBuilder()
                                .expireAfterWrite(Duration.ofSeconds(30))
                                .maximumSize(50)
                                .build());
        CaffeineCache tenMinuteCache =
                new CaffeineCache(
                        "10m-cache",
                        Caffeine.newBuilder()
                                .expireAfterWrite(Duration.ofMinutes(10))
                                .maximumSize(50)
                                .build());
        CaffeineCache oneHourCache =
                new CaffeineCache(
                        "1h-cache",
                        Caffeine.newBuilder()
                                .expireAfterWrite(Duration.ofHours(1))
                                .maximumSize(50)
                                .build());
        CaffeineCache twelveHourCache =
                new CaffeineCache(
                        "12h-cache",
                        Caffeine.newBuilder()
                                .expireAfterWrite(Duration.ofHours(12))
                                .maximumSize(50)
                                .build());
        CaffeineCache twentyFourHourCache =
                new CaffeineCache(
                        "24h-cache",
                        Caffeine.newBuilder()
                                .expireAfterWrite(Duration.ofHours(24))
                                .maximumSize(50)
                                .build());
        CaffeineCache footballResultCache =
                new CaffeineCache(
                        "football-cache",
                        Caffeine.newBuilder()
                                .expireAfter(
                                        new Expiry<>() {
                                            private static final int SHORT_CACHE_DURATION_MINUTES =
                                                    5;
                                            private static final int LONG_CACHE_DURATION_HOURS = 12;

                                            @Override
                                            public long expireAfterCreate(
                                                    Object key, Object value, long currentTime) {
                                                if (value instanceof TeamResultsById teamResults) {
                                                    if (!teamResults.presentMatches().isEmpty()
                                                            || teamResults
                                                                    .futureMatches()
                                                                    .getFirst()
                                                                    .matchDateTime()
                                                                    .isBefore(
                                                                            LocalDateTime.now()
                                                                                    .plusHours(
                                                                                            1))) {
                                                        return Duration.ofMinutes(
                                                                        SHORT_CACHE_DURATION_MINUTES)
                                                                .toNanos();
                                                    }
                                                }
                                                return Duration.ofHours(LONG_CACHE_DURATION_HOURS)
                                                        .toNanos();
                                            }

                                            @Override
                                            public long expireAfterUpdate(
                                                    Object key,
                                                    Object value,
                                                    long currentTime,
                                                    @NonNegative long currentDuration) {
                                                return getCacheDuration(value, currentDuration);
                                            }

                                            @Override
                                            public long expireAfterRead(
                                                    Object key,
                                                    Object value,
                                                    long currentTime,
                                                    @NonNegative long currentDuration) {
                                                return getCacheDuration(value, currentDuration);
                                            }

                                            private long getCacheDuration(
                                                    Object value, long currentDuration) {
                                                if (value instanceof TeamResultsById teamResults) {
                                                    if (!teamResults.presentMatches().isEmpty()
                                                            || (teamResults
                                                                            .futureMatches()
                                                                            .getFirst()
                                                                            .matchDateTime())
                                                                    .isBefore(
                                                                            LocalDateTime.now()
                                                                                    .plusHours(
                                                                                            1))) {
                                                        if ((currentDuration
                                                                > Duration.ofMinutes(
                                                                                SHORT_CACHE_DURATION_MINUTES)
                                                                        .toNanos())) {
                                                            return 0;
                                                        }
                                                    }
                                                }
                                                return currentDuration;
                                            }
                                        })
                                .build());

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(
                Arrays.asList(
                        thirtySecondCache,
                        tenMinuteCache,
                        oneHourCache,
                        twelveHourCache,
                        twentyFourHourCache,
                        footballResultCache));
        return manager;
    }
}
