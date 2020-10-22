package com.hclc.mobilecs.backend;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.session.init.KeyspacePopulator;
import org.springframework.data.cassandra.core.cql.session.init.ResourceKeyspacePopulator;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification.createKeyspace;
import static org.springframework.data.cassandra.core.mapping.NamingStrategy.SNAKE_CASE;

@Configuration
public class CassandraConfiguration extends AbstractCassandraConfiguration implements BeanClassLoaderAware {
    @Value("${spring.data.cassandra.contact-points}")
    private String contactPoints;
    @Value("${spring.data.cassandra.port}")
    private int port;
    @Value("${spring.data.cassandra.keyspace-name}")
    private String keyspaceName;
    @Value("${spring.data.cassandra.local-datacenter}")
    private String localDataCenter;
    @Value("classpath:cassandra/keyspace-populator.cql")
    private Resource keyspacePopulator;

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    protected int getPort() {
        return port;
    }

    @Override
    protected String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        return singletonList(createKeyspace(keyspaceName).ifNotExists().withSimpleReplication(1));
    }

    @Override
    protected KeyspacePopulator keyspacePopulator() {
        return new ResourceKeyspacePopulator(keyspacePopulator);
    }

    /**
     * Once AbstractCassandraConfiguration is configured programmatically to automatically create a keyspace,
     * DataStax driver complaints that "the local DC must be explicitly set" (spring.data.cassandra.local-datacenter
     * is ignored in such case). Hence it has to be configured programmatically as well (however honoring appropriate
     * property).
     */
    @Override
    protected String getLocalDataCenter() {
        return localDataCenter;
    }

    @Override
    public CassandraMappingContext cassandraMapping() throws ClassNotFoundException {
        CassandraMappingContext cassandraMappingContext = super.cassandraMapping();
        cassandraMappingContext.setNamingStrategy(SNAKE_CASE);
        return cassandraMappingContext;
    }
}
