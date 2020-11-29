package com.hclc.mobilecs.backend.datarecords;

import java.util.Date;

public class DataRecordUpsertRequest {
    private Date recordedAt;
    private String internalRecordId;
    private Long recordedBytes;

    public Date getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Date recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getInternalRecordId() {
        return internalRecordId;
    }

    public void setInternalRecordId(String internalRecordId) {
        this.internalRecordId = internalRecordId;
    }

    public Long getRecordedBytes() {
        return recordedBytes;
    }

    public void setRecordedBytes(Long recordedBytes) {
        this.recordedBytes = recordedBytes;
    }
}
