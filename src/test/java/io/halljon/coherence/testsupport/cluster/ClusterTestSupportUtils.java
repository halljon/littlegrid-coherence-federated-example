package io.halljon.coherence.testsupport.cluster;

import com.tangosol.net.CacheFactory;
import org.apache.commons.lang3.StringUtils;
import org.littlegrid.ClusterMemberGroup;
import org.littlegrid.ClusterMemberGroupUtils;
import org.littlegrid.impl.SimpleKeepAliveClusterMemberGroup;

import java.lang.management.ManagementFactory;

/**
 * Test support class to make tests free from configuration/setup type of code.
 */
public final class ClusterTestSupportUtils {
    private static final int MAXIMUM_CLUSTER_NAME_LENGTH = 66;
    private static final String PROCESS_NAME = ManagementFactory.getRuntimeMXBean().getName();
    private static final String LDN_CLUSTER_NAME = StringUtils.truncate("LDN-" + PROCESS_NAME, MAXIMUM_CLUSTER_NAME_LENGTH);
    private static final String NYC_CLUSTER_NAME = StringUtils.truncate("NYC-" + PROCESS_NAME, MAXIMUM_CLUSTER_NAME_LENGTH);

    private ClusterTestSupportUtils() {
        // Private constructor to prevent creation
    }

    public static String getLdnClusterName() {
        return LDN_CLUSTER_NAME;
    }

    public static String getNycClusterName() {
        return NYC_CLUSTER_NAME;
    }

    public static ClusterMemberGroup getOrCreateLdnCluster() {
        return getClusterMemberGroup(getLdnClusterName());
    }

    public static ClusterMemberGroup getOrCreateNycCluster() {
        return getClusterMemberGroup(getNycClusterName());
    }

    public static void joinLdnClusterAsStorageDisabledClient() {
        CacheFactory.shutdown();
        getOrCreateLdnCluster();
    }

    public static void joinNycClusterAsStorageDisabledClient() {
        CacheFactory.shutdown();
        getOrCreateNycCluster();
    }

    private static ClusterMemberGroup getClusterMemberGroup(String clusterName) {
        return ClusterMemberGroupUtils.newBuilder()
                .setClusterName(clusterName)
                .setStorageEnabledCount(1)
                .setClusterMemberGroupInstanceClassName(SimpleKeepAliveClusterMemberGroup.class.getName())
                .buildAndConfigureForStorageDisabledClient();
    }
}
