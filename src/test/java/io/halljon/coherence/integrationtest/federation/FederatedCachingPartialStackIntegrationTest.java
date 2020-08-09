package io.halljon.coherence.integrationtest.federation;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.TypeAssertion;
import com.tangosol.util.Filter;
import com.tangosol.util.Filters;
import com.tangosol.util.aggregator.Count;
import com.tangosol.util.extractor.IdentityExtractor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.littlegrid.support.SystemUtils;

import java.util.Properties;
import java.util.UUID;

import static io.halljon.coherence.testsupport.cluster.ClusterTestSupportUtils.getLdnClusterName;
import static io.halljon.coherence.testsupport.cluster.ClusterTestSupportUtils.getNycClusterName;
import static io.halljon.coherence.testsupport.cluster.ClusterTestSupportUtils.getOrCreateLdnCluster;
import static io.halljon.coherence.testsupport.cluster.ClusterTestSupportUtils.getOrCreateNycCluster;
import static io.halljon.coherence.testsupport.cluster.ClusterTestSupportUtils.joinLdnClusterAsStorageDisabledClient;
import static io.halljon.coherence.testsupport.cluster.ClusterTestSupportUtils.joinNycClusterAsStorageDisabledClient;
import static java.util.Collections.singletonMap;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class FederatedCachingPartialStackIntegrationTest {
    private static final String CACHE_NAME = "test";
    private static final String LDN_CLUSTER_NAME = getLdnClusterName();
    private static final String NYC_CLUSTER_NAME = getNycClusterName();
    private static final TypeAssertion.WithTypesAssertion<String, String> CACHE_TYPE_ASSERTION =
            new TypeAssertion.WithTypesAssertion<>(String.class, String.class);

    private static final Properties SYSTEM_PROPERTIES_BEFORE_ALL_TESTS = new Properties();

    @BeforeClass
    public static void beforeAllTests() {
        SYSTEM_PROPERTIES_BEFORE_ALL_TESTS.putAll(SystemUtils.snapshotSystemProperties());
    }

    @AfterClass
    public static void afterAllTests() {
        System.setProperties(SYSTEM_PROPERTIES_BEFORE_ALL_TESTS);
    }

    @Before
    public void beforeEachTest() {
        getOrCreateNycCluster();
        getOrCreateLdnCluster();
    }

    @Test
    public void putItemsViaLdnCluster() {
        int totalItems = 11;
        String entryValue = "orange";

        joinLdnClusterAsStorageDisabledClient();
        clearAndPutItemsThenCheckAsExpectedInThisCluster(totalItems, LDN_CLUSTER_NAME, entryValue);

        joinNycClusterAsStorageDisabledClient();
        checkItemsAsExpectedInOtherCluster(totalItems, NYC_CLUSTER_NAME, entryValue);
    }

    @Test
    public void putItemsViaNycCluster() {
        int totalItems = 12;
        String entryValue = "yellow";

        joinNycClusterAsStorageDisabledClient();
        clearAndPutItemsThenCheckAsExpectedInThisCluster(totalItems, NYC_CLUSTER_NAME, entryValue);

        joinLdnClusterAsStorageDisabledClient();
        checkItemsAsExpectedInOtherCluster(totalItems, LDN_CLUSTER_NAME, entryValue);
    }

    private void clearAndPutItemsThenCheckAsExpectedInThisCluster(int totalItems,
                                                                  String clusterName,
                                                                  String entryValue) {

        NamedCache<String, String> cache = CacheFactory.getTypedCache(CACHE_NAME, CACHE_TYPE_ASSERTION);
        cache.clear();

        assertThat(CacheFactory.getCluster().getClusterName(), equalTo(clusterName));

        for (int i = 0; i < totalItems; i++) {
            cache.putAll(singletonMap(UUID.randomUUID().toString(), entryValue));
        }

        assertThat(cache.size(), equalTo(totalItems));
    }

    private void checkItemsAsExpectedInOtherCluster(int totalItems,
                                                    String clusterName,
                                                    String expectedEntryValue) {

        NamedCache<String, String> cache = CacheFactory.getTypedCache(CACHE_NAME, CACHE_TYPE_ASSERTION);

        assertThat(CacheFactory.getCluster().getClusterName(), equalTo(clusterName));
        assertThat(cache.size(), equalTo(totalItems));

        Filter<String> filter = Filters.equal(IdentityExtractor.INSTANCE(), expectedEntryValue);
        assertThat(cache.aggregate(filter, new Count<>()), equalTo(totalItems));
    }
}
