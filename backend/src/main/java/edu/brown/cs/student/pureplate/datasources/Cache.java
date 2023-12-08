package edu.brown.cs.student.pureplate.datasources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.json.simple.parser.ParseException;

/**
 * Like the class ApiCall, this class queries the ACS API and retrieves the broadband access
 * percentage for a URI containing specific state and county identifiers. Unlike ApiCall, this class
 * stores previous API requests in a cache.
 */
public class Cache implements Query<String, String> {

  private final Query<String, String> wrappedSearcher;
  private final LoadingCache<String, String> cache;

  /**
   * Constructor for CachedApiCall that establishes the cache.
   *
   * @param toWrap        - a Query that retrieves broadband percentages from the ACS API
   * @param maxEntries    - the maximum number of entries the cache can store at a time
   * @param maxStorageMin - how long entries remain in the cache (in minutes)
   */
  public Cache(Query<String, String> toWrap, int maxEntries, int maxStorageMin) {
    this.wrappedSearcher = toWrap;
    this.cache =
        CacheBuilder.newBuilder()
            .maximumSize(maxEntries)
            .expireAfterWrite(maxStorageMin, TimeUnit.MINUTES)
            .recordStats()
            .build(
                new CacheLoader<>() {
//                  @Override
//                  public List<String> load(List<String> strings) throws Exception {
//                    return null;
//                  }

                  @Override
                  public String load(String key)
                      throws URISyntaxException, IOException, InterruptedException, ParseException, DatasourceException {
                    // We kept this print statement to aid our caching demo
                    System.out.println("called load for: " + key);
                    return wrappedSearcher.query(key);
                  }
                });
  }

  /**
   * Retrieves the broadband percentage associated with the provided target URI to query to.
   *
   * @param target - the API URI to query to
   * @return the target's corresponding String broadband percentage
   */
  @Override
  public String query(String target) {
    String result = this.cache.getUnchecked(target);
    // For debugging and demo (would remove in a "real" version):
    System.out.println(this.cache.stats());
    return result;
  }

}
