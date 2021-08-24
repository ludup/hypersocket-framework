package com.hypersocket.cache;

import java.util.Map;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.boot.registry.selector.spi.StrategySelector;
import org.hibernate.cache.internal.NoCachingRegionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.jboss.logging.Logger;

public class HypersocketCacheRegionFactoryServiceInitiator implements StandardServiceInitiator<RegionFactory>{

	private static final CoreMessageLogger LOG = Logger.getMessageLogger( CoreMessageLogger.class,
			HypersocketCacheRegionFactoryServiceInitiator.class.getName() );

	/**
	 * Singleton access
	 */
	public static final HypersocketCacheRegionFactoryServiceInitiator INSTANCE = new HypersocketCacheRegionFactoryServiceInitiator();

	@Override
	public Class<RegionFactory> getServiceInitiated() {
		return RegionFactory.class;
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public RegionFactory initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
		final Properties p = new Properties();
		if (configurationValues != null) {
			p.putAll( configurationValues );
		}
		
		
		final boolean useSecondLevelCache = ConfigurationHelper.getBoolean(
				AvailableSettings.USE_SECOND_LEVEL_CACHE,
				configurationValues,
				true
		);
		final boolean useQueryCache = ConfigurationHelper.getBoolean(
				AvailableSettings.USE_QUERY_CACHE,
				configurationValues
		);

		RegionFactory regionFactory = NoCachingRegionFactory.INSTANCE;

		// The cache provider is needed when we either have second-level cache enabled
		// or query cache enabled.  Note that useSecondLevelCache is enabled by default
		final Object object = configurationValues.get(AvailableSettings.CACHE_REGION_FACTORY);
		
		if ( ( useSecondLevelCache || useQueryCache ) && object != null ) {
			try {
				if(object instanceof String){
					final Class<? extends RegionFactory> regionFactoryClass = registry.getService( StrategySelector.class )
							.selectStrategyImplementor( RegionFactory.class, (String) object );
					try {
						regionFactory = regionFactoryClass.getConstructor( Properties.class ).newInstance( p );
					}
					catch ( NoSuchMethodException e ) {
						// no constructor accepting Properties found, try no arg constructor
						LOG.debugf(
								"%s did not provide constructor accepting java.util.Properties; attempting no-arg constructor.",
								regionFactoryClass.getSimpleName() );
						regionFactory = regionFactoryClass.getConstructor().newInstance();
					}
					return regionFactory;
				}else if (object instanceof RegionFactory){
					return (RegionFactory) object;
				}
				
				throw new IllegalStateException("Cannot determine Region Factory");
			}
			catch ( Exception e ) {
				throw new HibernateException( "could not instantiate RegionFactory [" + object + "]", e );
			}
		}

		LOG.debugf( "Cache region factory : %s", regionFactory.getClass().getName() );

		return regionFactory;
	}

	/**
	 * Map legacy names unto the new corollary.
	 *
	 * TODO: temporary hack for org.hibernate.cfg.SettingsFactory.createRegionFactory()
	 *
	 * @param name The (possibly legacy) factory name
	 *
	 * @return The factory name to use.
	 */
	public static String mapLegacyNames(final String name) {
		if ( "org.hibernate.cache.EhCacheRegionFactory".equals( name ) ) {
			return "org.hibernate.cache.ehcache.EhCacheRegionFactory";
		}

		if ( "org.hibernate.cache.SingletonEhCacheRegionFactory".equals( name ) ) {
			return "org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory";
		}

		return name;
	}

}
