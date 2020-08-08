package io.halljon.coherence.testsupport.cluster;

import com.tangosol.net.CacheFactory;
import org.littlegrid.ClusterMemberGroup;
import org.littlegrid.ClusterMemberGroupUtils;
import org.littlegrid.impl.SimpleKeepAliveClusterMemberGroup;

import java.lang.management.ManagementFactory;

/**
 * Test support class to make tests free from configuration/setup type of code.
 */
public final class ClusterTestSupportUtils {
    private static final String PROCESS_NAME = ManagementFactory.getRuntimeMXBean().getName();
    private static final String LDN_CLUSTER_NAME = "LDN-" + PROCESS_NAME;
    private static final String NYC_CLUSTER_NAME = "NYC-" + PROCESS_NAME;

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
        return ClusterMemberGroupUtils.newBuilder()
                .setClusterName(getLdnClusterName())
                .setStorageEnabledCount(1)
                .setClusterMemberGroupInstanceClassName(SimpleKeepAliveClusterMemberGroup.class.getName())
                .buildAndConfigureForStorageDisabledClient();
    }

    public static ClusterMemberGroup getOrCreateNycCluster() {
        return ClusterMemberGroupUtils.newBuilder()
                .setClusterName(getNycClusterName())
                .setStorageEnabledCount(1)
                .setClusterMemberGroupInstanceClassName(SimpleKeepAliveClusterMemberGroup.class.getName())
                .buildAndConfigureForStorageDisabledClient();
    }

    public static void joinLdnClusterAsStorageDisabledClient() {
        CacheFactory.shutdown();
        getOrCreateLdnCluster();
    }

    public static void joinNycClusterAsStorageDisabledClient() {
        CacheFactory.shutdown();
        getOrCreateNycCluster();
    }
}
