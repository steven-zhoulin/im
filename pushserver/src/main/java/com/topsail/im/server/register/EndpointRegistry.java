package com.topsail.im.server.register;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 *
 *
 * @author Steven
 * @date 2021-02-01
 */
@Slf4j
@Component
public class EndpointRegistry {

    /**
     * zookeeper 集群地址
     */
    @Value("${pushserver.zookeeper.address:127.0.0.1:2181}")
    private String zookeeperAddress;

    /**
     * 实例接入地址对应的路径
     */
    @Value("${pushserver.zookeeper.instancePath:/push-server/instances}")
    private String instancePath;

    /**
     * 客户端
     */
    private CuratorFramework zkClient;

    @PostConstruct
    private void init() {

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3, 5000);
        zkClient = CuratorFrameworkFactory.builder()
            .connectString(zookeeperAddress)
            .sessionTimeoutMs(5000)
            .connectionTimeoutMs(5000)
            .retryPolicy(retryPolicy)
            .build();
        zkClient.start();

    }

    /**
     * 上报接入地址
     *
     * @param endpoint
     */
    public void online(String endpoint) {
        try {
            Stat stat = zkClient.checkExists().forPath(instancePath);
            if (Objects.isNull(stat)) {
                this.zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(instancePath);
            }
            this.zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath + "/" + endpoint, new byte[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}