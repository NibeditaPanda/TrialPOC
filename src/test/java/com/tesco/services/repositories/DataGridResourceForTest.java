package com.tesco.services.repositories;

import com.tesco.services.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.transaction.LockingMode;

public class DataGridResourceForTest extends DataGridResource {
    public DataGridResourceForTest(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected org.infinispan.configuration.cache.Configuration getConfiguration(String indexName) {
        return new ConfigurationBuilder()
                .invocationBatching().enable().transaction().syncCommitPhase(true).lockingMode(LockingMode.PESSIMISTIC)
                .indexing()
                .enable()
                .indexLocalOnly(true)
                .addProperty("default.indexmanager", "near-real-time")
                .addProperty("default.directory_provider", "ram")
                .addProperty("default.indexBase", INDEX_FILE_LOCATION + indexName)
                .addProperty("default.exclusive_index_use", "false")
                .addProperty("default.indexwriter.merge_factor", "4")
                .addProperty("default.indexwriter.merge_max_size", "100")
                .addProperty("default.indexwriter.ram_buffer_size", "256")
                .addProperty("default.sharding_strategy.nbr_of_shards", "12")
                .addProperty("lucene_version", "LUCENE_36")
                .build();
    }
}
