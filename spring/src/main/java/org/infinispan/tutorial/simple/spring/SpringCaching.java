package org.infinispan.tutorial.simple.spring;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.spring.provider.SpringEmbeddedCacheManager;
import org.infinispan.spring.provider.SpringEmbeddedCacheManagerFactoryBean;
import org.infinispan.transaction.TransactionMode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * This example shows how to use Spring's {@link Cacheable} annotation on slow methods.
 */
public class SpringCaching {

    private static final String CACHE_STORE_PATH = "/home/slaskawi/tmp";
    private static final String CACHE_STORE_FILE = CACHE_STORE_PATH + "/default.dat";

    @Configuration
    @EnableCaching
    public static class SpringConfiguration {

        @Bean
        public SpringEmbeddedCacheManager springCache() {
            ConfigurationBuilder config = new ConfigurationBuilder();
            config.expiration().lifespan(2, TimeUnit.DAYS)
                    .transaction()
                    .transactionMode(TransactionMode.TRANSACTIONAL)
                    .persistence()
                    .addSingleFileStore()
                    .shared(false)
                    .fetchPersistentState(true)
                    .ignoreModifications(false)
                    .purgeOnStartup(false)
                    .location(CACHE_STORE_PATH);

            GlobalConfigurationBuilder globalConfig = new GlobalConfigurationBuilder();
            globalConfig.globalJmxStatistics()
                    .allowDuplicateDomains(true);
            return new SpringEmbeddedCacheManager(new DefaultCacheManager(globalConfig.build(), config.build(), true));
        }

        @Bean
        public CachedObject cachedObject() {
            return new CachedObject();
        }
    }

    public static class CachedObject {

        @Cacheable(value = "default", sync = true)
        public String verySlowMethod() {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Spring and Infinispan will speed this one up!";
        }
    }

    public static void main(String[] args) throws Exception {
        File cacheStoreFile = new File(CACHE_STORE_FILE);
        cacheStoreFile.delete();

        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);

        CachedObject cachePlayground = applicationContext.getBean(CachedObject.class);

        printWithTime(() -> cachePlayground.verySlowMethod());
        printWithTime(() -> cachePlayground.verySlowMethod());

        System.out.println("File size: " + cacheStoreFile.length());
        try (BufferedReader br = new BufferedReader(new FileReader(cacheStoreFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    private static void printWithTime(Callable<String> functionToCall) throws Exception {
        long startTime = System.currentTimeMillis();
        String result = functionToCall.call();
        System.out.println("Returned: \"" + result + "\" in " + (System.currentTimeMillis() - startTime) / 1000 + " s");
    }
}
