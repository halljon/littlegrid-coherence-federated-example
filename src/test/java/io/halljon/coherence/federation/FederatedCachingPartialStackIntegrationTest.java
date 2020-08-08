package io.halljon.coherence.federation;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filters;
import com.tangosol.util.aggregator.Count;
import com.tangosol.util.extractor.IdentityExtractor;
import com.tangosol.util.filter.EqualsFilter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.littlegrid.support.SystemUtils;

import java.util.Properties;
import java.util.UUID;

import static io.halljon.coherence.testsupport.cluster.ClusterTestSupportUtils.configureForLdnStorageDisabledClient;
import static io.halljon.coherence.testsupport.cluster.ClusterTestSupportUtils.configureForNycStorageDisabledClient;
import static io.halljon.coherence.testsupport.cluster.ClusterTestSupportUtils.getLdnClusterName;
import static io.halljon.coherence.testsupport.cluster.ClusterTestSupportUtils.getNycClusterName;
import static io.halljon.coherence.testsupport.cluster.ClusterTestSupportUtils.getOrCreateLdnCluster;
import static io.halljon.coherence.testsupport.cluster.ClusterTestSupportUtils.getOrCreateNycCluster;
import static java.util.Collections.singletonMap;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class FederatedCachingPartialStackIntegrationTest {
    private static final String CACHE_NAME = "test";

    private static Properties systemPropertiesBeforeAllTests;

    private String ldnClusterName;
    private String nycClusterName;
    private NamedCache<String, String> cache;

    @BeforeClass
    public static void beforeAllTests() {
        systemPropertiesBeforeAllTests = SystemUtils.snapshotSystemProperties();

        System.setProperty("ldn-cluster-name", getLdnClusterName());
        System.setProperty("nyc-cluster-name", getNycClusterName());
    }

    @AfterClass
    public static void afterAllTests() {
        System.setProperties(systemPropertiesBeforeAllTests);
    }

    @Before
    public void beforeEachTest() {
        getOrCreateNycCluster();
        getOrCreateLdnCluster();

        nycClusterName = getNycClusterName();
        ldnClusterName = getLdnClusterName();

        cache = CacheFactory.getCache(CACHE_NAME);
        cache.clear();
    }

    @Test
    public void putItemsViaLdnCluster() {
        int totalItems = 11;
        String entryValue = "orange";

        configureForLdnStorageDisabledClient();
        putItemsAndCheckAsExpectedInThisCluster(totalItems, ldnClusterName, entryValue);

        configureForNycStorageDisabledClient();
        checkItemsAsExpectedInOtherCluster(totalItems, nycClusterName, entryValue);
    }

    @Test
    public void putItemsViaNycCluster() {
        int totalItems = 12;
        String entryValue = "yellow";

        configureForNycStorageDisabledClient();
        putItemsAndCheckAsExpectedInThisCluster(totalItems, nycClusterName, entryValue);

        configureForLdnStorageDisabledClient();
        checkItemsAsExpectedInOtherCluster(totalItems, ldnClusterName, entryValue);
    }

    private void putItemsAndCheckAsExpectedInThisCluster(int totalItems,
                                                         String clusterName,
                                                         String entryValue) {

        assertThat(CacheFactory.getCluster().getClusterName(), equalTo(clusterName));

        cache = CacheFactory.getCache(CACHE_NAME);

        for (int i = 0; i < totalItems; i++) {
            cache.putAll(singletonMap(UUID.randomUUID().toString(), entryValue));
        }

        assertThat(cache.size(), equalTo(totalItems));
    }

    private void checkItemsAsExpectedInOtherCluster(int totalItems,
                                                    String clusterName,
                                                    String expectedEntryValue) {

        assertThat(CacheFactory.getCluster().getClusterName(), equalTo(clusterName));

        cache = CacheFactory.getCache(CACHE_NAME);
        assertThat(cache.size(), equalTo(totalItems));

        EqualsFilter<String, String> filter = Filters.equal(new IdentityExtractor<>(), expectedEntryValue);
        assertThat(cache.aggregate(filter, new Count<>()), equalTo(totalItems));
    }
}
