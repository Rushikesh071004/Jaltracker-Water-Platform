package com.waterbill.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

@Entity
@Table(name = "revinfo_custom")
@RevisionEntity(AuditRevisionListener.class)
public class AuditRevisionEntity extends DefaultRevisionEntity {

    @Column(name = "modified_by_user_id")
    private Long modifiedByUserId;

    @Column(name = "modified_by_username", length = 150)
    private String modifiedByUsername;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    public Long getModifiedByUserId() { return modifiedByUserId; }
    public void setModifiedByUserId(Long modifiedByUserId) { this.modifiedByUserId = modifiedByUserId; }

    public String getModifiedByUsername() { return modifiedByUsername; }
    public void setModifiedByUsername(String modifiedByUsername) { this.modifiedByUsername = modifiedByUsername; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
}
