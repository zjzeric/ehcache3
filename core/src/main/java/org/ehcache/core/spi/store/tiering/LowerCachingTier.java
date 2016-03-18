/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ehcache.core.spi.store.tiering;

import org.ehcache.exceptions.CacheAccessException;
import org.ehcache.core.spi.function.Function;
import org.ehcache.core.spi.function.NullaryFunction;
import org.ehcache.core.spi.store.ConfigurationChangeSupport;
import org.ehcache.core.spi.store.Store;
import org.ehcache.spi.service.Service;
import org.ehcache.spi.service.ServiceConfiguration;

/**
 * Interface for the lower tier of a multi-tier {@link CachingTier}.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface LowerCachingTier<K, V> extends ConfigurationChangeSupport {

  /**
   * Either return the {@link org.ehcache.core.spi.store.Store.ValueHolder} currently in the caching tier
   * or installs and returns the result of the passed in function.
   * <P>
   *   Note that in case of expired {@link org.ehcache.core.spi.store.Store.ValueHolder} {@code null} will be returned
   *   and the mapping will be invalidated.
   * </P>
   *
   * @param key the key
   * @param source the function that computes the value
   * @return the value holder, or {@code null}
   *
   * @throws CacheAccessException if the mapping cannot be accessed, installed or removed
   */
  Store.ValueHolder<V> installMapping(K key, Function<K, Store.ValueHolder<V>> source) throws CacheAccessException;

  /**
   * Return the value holder currently in this tier and removes it atomically.
   *
   * @param key the key
   * @return the value holder, or {@code null}
   *
   * @throws CacheAccessException if the mapping cannot be access or removed
   */
  Store.ValueHolder<V> getAndRemove(K key) throws CacheAccessException;

  /**
   * Removes a mapping, triggering the {@link org.ehcache.core.spi.store.tiering.CachingTier.InvalidationListener} if
   * registered.
   *
   * @param key the key to remove
   *
   * @throws CacheAccessException if the mapping cannot be removed
   */
  void invalidate(K key) throws CacheAccessException;

  /**
   * Removes a mapping, then call a function under the same lock scope irrespectively of a mapping being there or not.
   *
   * @param key the key
   * @param function the function to call
   *
   * @throws CacheAccessException if the mapping cannot be removed or the function throws
   */
  void invalidate(K key, NullaryFunction<K> function) throws CacheAccessException;

  /**
   * Empty out this tier
   *
   * @throws CacheAccessException if mappings cannot be removed
   */
  void clear() throws CacheAccessException;

  /**
   * Set the caching tier's invalidation listener.
   *
   * @param invalidationListener the listener
   */
  void setInvalidationListener(CachingTier.InvalidationListener<K, V> invalidationListener);

  /**
   * {@link Service} interface for providing {@link LowerCachingTier} instances.
   */
  interface Provider extends Service {

    /**
     * Creates a new {@link LowerCachingTier} instance using the provided configuration
     *
     * @param storeConfig the {@code Store} configuration
     * @param serviceConfigs a collection of service configurations
     * @param <K> the key type for this tier
     * @param <V> the value type for this tier
     *
     * @return the new lower caching tier
     */
    <K, V> LowerCachingTier<K, V> createCachingTier(Store.Configuration<K, V> storeConfig, ServiceConfiguration<?>... serviceConfigs);

    /**
     * Releases a {@link LowerCachingTier}.
     *
     * @param resource the lower caching tier to release
     *
     * @throws IllegalArgumentException if this provider does not know about this lower caching tier
     */
    void releaseCachingTier(LowerCachingTier<?, ?> resource);

    /**
     * Initialises a {@link LowerCachingTier}.
     *
     * @param resource the lower caching tier to initialise
     */
    void initCachingTier(LowerCachingTier<?, ?> resource);
  }

}
