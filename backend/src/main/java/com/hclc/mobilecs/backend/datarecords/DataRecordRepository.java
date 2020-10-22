package com.hclc.mobilecs.backend.datarecords;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
interface DataRecordRepository extends CassandraRepository<DataRecord, DataRecordKey> {

    List<DataRecord> findAllByKeyAgreementIdAndKeyYearAndKeyMonth(UUID agreementId, short year, byte month);
}
