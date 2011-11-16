Ehcache-JCache
==============

About
-----

*ehcache-jcache* is a full implementation of the API and SPI from from JSR107 (aka JCache). It provides a wrapper around an Ehcache cache
that allows allows you to use Ehcache as the caching provider using only JSR107 APIs.

Getting Started
---------------
### Configuring a JCache ###

There are two ways to configure a JCache.
1. Programatically using a CacheBuilder
2. Declaratively using an ehcache-*.xml file

### Configuring a JCache Programatically ###

The `JCacheCacheManager` is responsible for creating a JCache that delegates the storage and retrieval of cache
elements to an underlying ehcache.

The `CacheManager` can be created manually, or you can use the `Caching` singleton entrypoint to retrieve it.

 	    Cache foo = Caching.getCacheManager().createCacheBuilder("foo").build();

You can set additional parameters on the cache as well. For instance, to create a new cache that will have entries
expire 10 minutes after they are created (or last modified) that stores cache values as references:

 	    Cache blarg = Caching.getCacheManager().createCacheBuilder("blarg")
                .setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new Duration(TimeUnit.MINUTES, 10))
                .setStoreByValue(false)
                .build();

Currently only the configuration parameters specified in the JSR107 spec are exposed via the builder interface.
You can also configure caches declartively in ehcache's well-known xml cache configuration format.

When you create a named cache manager, the jcache-ehcache provider will look in the classpath for a file named
"`ehcache-NAME.xml`" (where NAME is the name of the cache manager you are creating).

If you have a file named `ehcache-jcache-example.xml`, for instance, then when you call:

 	    Cache boo = Caching.getCacheManager("jcache-example").getCache("boo");

The cache will be configured based on the parameters set in the `ehcache-jcache-example.xml` file in the classpath.
In that xml file additional parameters (such as the size of the cache) can be configured.

Using with JSR107 annotations
-------------
The reference implementation of the JSR107 annotations can be used with any JSR107 caching provider.
There are annotation implementations provided for both CDI and Spring that will work with ehcache-jcache.
For more information on annotations, see <https://github.com/jsr107/RI/tree/master/cache-annotations-ri>

Documentation
-------------

See See the <http://ehcache.org/documentation/jsr107.html> for full documentation.

Development
--------
Active development of the jcache-ehcache module follows changes to the spec. It will happen on the

Release
--------

Following releases of the JSR107 spec APIs, an updated release milestone will be released and the latest stable release code will sit on the master
branch of the jcache-ehcache github repository.



Building From Source
--------------------

`mvn clean install`


Mailing list
------------

Please join the Ehcache mailing list if you're interested in using or developing the software: <http://ehcache.org/mail-lists.html>

IRC
---

We will be using the `#jsr107` channel on Freenode for chat.


Issue tracker
-------------

Please log issues to: <https://github.com/jsr107/ehcache-jcache/issues>


Contributing
------------

Right now contribution is limited to the Expert Group, but as we go along we will open it up.


License
-------

This software is provided under an Apache 2 open source license, read the `LICENSE.txt` file for details.


Contributors
------------

This free, open source software was made possible by Terracotta, Inc.. See the `CONTRIBUTORS.markdown` file for details.


Copyright
---------

Copyright (c) Terracotta