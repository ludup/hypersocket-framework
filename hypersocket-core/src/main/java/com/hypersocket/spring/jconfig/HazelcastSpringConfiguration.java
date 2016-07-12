package com.hypersocket.spring.jconfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.hibernate.cache.RegionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.hazelcast.cache.HazelcastCachingProvider;
import com.hazelcast.config.AwsConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hibernate.HazelcastLocalCacheRegionFactory;

@Configuration
public class HazelcastSpringConfiguration {
	
	@Autowired ApplicationContext applicationContext; 
	@Autowired Environment environment; 

	@Bean
    Config config(ApplicationContext applicationContext, NetworkConfig networkConfig) {
        Config config = new Config();
        config.setInstanceName(applicationContext.getId());
        config.setNetworkConfig(networkConfig);
        //config.getGroupConfig().setName(String.format("group_%s", applicationContext.getId()));
        config.setProperty( "hazelcast.logging.type", "log4j" );
        return config;
    }
	
	
	@Bean(destroyMethod = "shutdown")
    HazelcastInstance hazelcast(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }
 
    @Bean
    NetworkConfig networkConfig(@Value("${hazelcast.port:5900}") int port, JoinConfig joinConfig) {
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setJoin(joinConfig);
        networkConfig.setPort(port);
        networkConfig.setPortAutoIncrement(true);
        networkConfig.addOutboundPortDefinition("55000-55100");
        networkConfig.setReuseAddress(true);
        return networkConfig;
    }
 
    @Bean
    JoinConfig joinConfig() {
    	JoinConfig joinConfig = new JoinConfig();
    	awsConfig(joinConfig);
        multicastConfig(joinConfig);
        tcpIpConfig(joinConfig);
        return joinConfig;
    }
    
    @Bean(destroyMethod = "stop")
    RegionFactory regionFactory(HazelcastInstance instance){
    	HazelcastLocalCacheRegionFactory cacheRegionFactory = new HazelcastLocalCacheRegionFactory(instance);
    	return cacheRegionFactory;
    }
    
    @Bean(destroyMethod = "close")
    CacheManager cacheManager(HazelcastInstance instance) throws URISyntaxException{
    	CachingProvider cachingProvider = Caching.getCachingProvider("com.hazelcast.cache.impl.HazelcastServerCachingProvider", null);

    	// Create Properties instance pointing to a named HazelcastInstance
    	//Properties properties = new Properties();
    	//properties.setProperty(HazelcastCachingProvider.HAZELCAST_INSTANCE_NAME, instance.getName());

    	Properties properties = HazelcastCachingProvider.propertiesByInstanceName(applicationContext.getId());
    	
    	URI cacheManagerName = new URI("hypersocket-cache-manager");
    	return cachingProvider.getCacheManager(cacheManagerName, null, properties);
    } 
 
    private void tcpIpConfig(JoinConfig joinConfig) {
        TcpIpConfig tcpIpConfig = new TcpIpConfig();
        if(environment.acceptsProfiles("HA")){
        	tcpIpConfig.setEnabled(true);
        	tcpIpConfig.addMember("127.0.0.1");
        }else{
        	tcpIpConfig.setEnabled(false);
        }
        
        joinConfig.setTcpIpConfig(tcpIpConfig);
    }
    
    private void multicastConfig(JoinConfig joinConfig) {
        MulticastConfig multicastConfig = new MulticastConfig();
        multicastConfig.setEnabled(false);
        joinConfig.setMulticastConfig(multicastConfig);
    }
    
    private void awsConfig(JoinConfig joinConfig){
    	AwsConfig awsConfig = new AwsConfig();
    	awsConfig.setEnabled(false);
    	joinConfig.setAwsConfig(awsConfig);
    }
 
}
