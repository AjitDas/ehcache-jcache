package net.sf.ehcache.jcache;


import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheLoader;
import javax.cache.CacheWriter;
import javax.cache.Caching;
import javax.cache.InvalidConfigurationException;
import javax.cache.OptionalFeature;
import javax.cache.Status;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.NotificationScope;
import javax.cache.transaction.IsolationLevel;
import javax.cache.transaction.Mode;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class JCacheManager implements javax.cache.CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(JCacheManager.class);

    private final HashMap<String, Cache<?, ?>> caches = new HashMap<String, Cache<?, ?>>();
    private final HashSet<Class<?>> immutableClasses = new HashSet<Class<?>>();

    private final ClassLoader classLoader;
    private volatile Status status;
    private final CacheManager ehcacheManager;

    public CacheManager getEhcacheManager() {
        return ehcacheManager;
    }

    /**
     * Creates a JCacheManager that uses the name of the cache to configure the underlying EhCache CacheManager
     * via an ehcache-name.xml config file
     *
     * @param name
     * @param classLoader
     */
    public JCacheManager(String name, ClassLoader classLoader) {
        status = Status.UNINITIALISED;
        if (classLoader == null) {
            throw new NullPointerException("No classLoader specified");
        }
        if (name == null) {
            throw new NullPointerException("No name specified");
        }
        this.classLoader = classLoader;

        ehcacheManager = configureEhCacheManager(name);
        ehcacheManager.setName(name);

        status = Status.STARTED;
    }

    protected CacheManager configureEhCacheManager(String name) {
        String configName;
        // if (name.equals(Caching.DEFAULT_CACHE_MANAGER_NAME)) {
        return new CacheManager();
        // } else {
        //return new CacheManager("/ehcache-" + name + ".xml");
        // }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The name returned will be that passed in to the constructor {@link #JCacheManager(String, ClassLoader)}
     */
    @Override
    public String getName() {
        return ehcacheManager.getName();
    }

    /**
     * Returns the status of this CacheManager.
     * <p/>
     *
     * @return one of {@link javax.cache.Status}
     */
    @Override
    public Status getStatus() {
        return JCacheStatusAdapter.adaptStatus(ehcacheManager.getStatus());
    }

    @Override
    public <K, V> CacheBuilder<K, V> createCacheBuilder(String cacheName) {
        return new JCacheBuilder<K, V>(cacheName);
    }

    @Override
    public <K, V> Cache<K, V> getCache(String s) {
        return new JCache<K, V>(ehcacheManager.getCache(s), this.getName(), this.classLoader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> Set<Cache<K, V>> getCaches() {
        synchronized (caches) {
            HashSet<Cache<K, V>> set = new HashSet<Cache<K, V>>();
            for (Cache<?, ?> cache : caches.values()) {
                /*
                 * Can't really verify K/V cast but it is required by the API, using a 
                 * local variable for the cast to allow for a minimal scoping of @SuppressWarnings 
                 */
                @SuppressWarnings("unchecked")
                final Cache<K, V> castCache = (Cache<K, V>) cache;
                set.add(castCache);
            }
            return Collections.unmodifiableSet(set);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeCache(String cacheName) {
        if (status != Status.STARTED) {
            throw new IllegalStateException();
        }
        if (cacheName == null) {
            throw new NullPointerException();
        }
        Cache<?, ?> oldCache;
        synchronized (caches) {
            oldCache = caches.remove(cacheName);
        }
        if (oldCache != null) {
            oldCache.stop();
        }

        return oldCache != null;
    }


    @Override
    public javax.transaction.UserTransaction getUserTransaction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSupported(OptionalFeature optionalFeature) {
        return Caching.isSupported(optionalFeature);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addImmutableClass(Class<?> immutableClass) {
        if (immutableClass == null) {
            throw new NullPointerException();
        }
        immutableClasses.add(immutableClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        if (status != Status.STARTED) {
            throw new IllegalStateException();
        }
        synchronized (immutableClasses) {
            immutableClasses.clear();
        }
        ehcacheManager.shutdown();
        status = Status.STOPPED;
    }

    @Override
    public <T> T unwrap(java.lang.Class<T> cls) {
        if (cls.isAssignableFrom(this.getClass())) {
            return cls.cast(this);
        }

        throw new IllegalArgumentException("Unwapping to " + cls + " is not a supported by this implementation");
    }


    private class JCacheBuilder<K, V> implements CacheBuilder<K, V> {
        private final JCache.Builder<K, V> cacheBuilder;

        public JCacheBuilder(String cacheName) {
            cacheBuilder = new JCache.Builder<K, V>(cacheName, ehcacheManager.getName(), classLoader);
        }

        @Override
        public JCache<K, V> build() {
            JCache<K, V> cache = cacheBuilder.build();
            addInternal(cache);
            return cache;
        }


        @Override
        public CacheBuilder<K, V> setCacheLoader(CacheLoader<K, V> cacheLoader) {
            cacheBuilder.setCacheLoader(cacheLoader);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setCacheWriter(CacheWriter<K, V> cacheWriter) {
            cacheBuilder.setCacheWriter(cacheWriter);
            return this;
        }

        @Override
        public CacheBuilder<K, V> registerCacheEntryListener(CacheEntryListener<K, V> listener, NotificationScope scope, boolean synchronous) {
            cacheBuilder.registerCacheEntryListener(listener, scope, synchronous);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setStoreByValue(boolean storeByValue) {
            cacheBuilder.setStoreByValue(storeByValue);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setTransactionEnabled(IsolationLevel isolationLevel, Mode mode) {
            cacheBuilder.setTransactionEnabled(isolationLevel, mode);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setStatisticsEnabled(boolean enableStatistics) {
            cacheBuilder.setStatisticsEnabled(enableStatistics);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setReadThrough(boolean readThrough) {
            cacheBuilder.setReadThrough(readThrough);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setWriteThrough(boolean writeThrough) {
            cacheBuilder.setWriteThrough(writeThrough);
            return this;
        }

        @Override
        public CacheBuilder<K, V> setExpiry(javax.cache.CacheConfiguration.ExpiryType type, javax.cache.CacheConfiguration.Duration timeToLive) {
            cacheBuilder.setExpiry(type, timeToLive);
            return this;
        }
    }

    private void addInternal(JCache cache) {
        ehcacheManager.addCache(cache.getEhcache());
    }


}
