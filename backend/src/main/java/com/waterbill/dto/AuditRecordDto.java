package com.waterbill.dto;

import java.time.Instant;
import java.util.Map;

public class AuditRecordDto {

    private String entityName;
    private Long entityId;
    private Number revisionNumber;
    private String revisionType;
    private Instant revisionDate;
    private String modifiedBy;
    private String clientIp;
    private Map<String, Object> previousValue;
    private Map<String, Object> newValue;

    public AuditRecordDto() {}

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public Number getRevisionNumber() { return revisionNumber; }
    public void setRevisionNumber(Number revisionNumber) { this.revisionNumber = revisionNumber; }

    public String getRevisionType() { return revisionType; }
    public void setRevisionType(String revisionType) { this.revisionType = revisionType; }

    public Instant getRevisionDate() { return revisionDate; }
    public void setRevisionDate(Instant revisionDate) { this.revisionDate = revisionDate; }

    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public Map<String, Object> getPreviousValue() { return previousValue; }
    public void setPreviousValue(Map<String, Object> previousValue) { this.previousValue = previousValue; }

    public Map<String, Object> getNewValue() { return newValue; }
    public void setNewValue(Map<String, Object> newValue) { this.newValue = newValue; }
}
