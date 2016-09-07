package org.infinispan.tutorial.simple.map;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;

public class InfinispanMap {

   private static final String CONFIG_PATH = "infinispan-replication.xml";

   public static void main(String[] args) throws IOException, InterruptedException {
      // Construct a simple local cache manager with default configuration
      DefaultCacheManager cacheManager = new DefaultCacheManager(InfinispanMap.class.getClassLoader().getResourceAsStream(CONFIG_PATH));
      // Obtain the default cache
      Cache<String, String> cache = cacheManager.getCache();

      TimeUnit.SECONDS.sleep(20);

      System.out.println("Cluster members: " + cacheManager.getMembers());

      // Store a value
      cache.put("key", "value");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", cache.get("key"));
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
