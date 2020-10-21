create table if not exists data_record (
    agreement_id uuid,
    year smallint,
    month tinyint,
    recorded_at timestamp,
    internal_record_id uuid,
    recorded_bytes bigint,
    primary key ((agreement_id, year, month), recorded_at, internal_record_id)
) with clustering order by (recorded_at desc);