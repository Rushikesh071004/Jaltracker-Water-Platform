package com.waterbill.audit;

import com.waterbill.dto.AuditRecordDto;
import com.waterbill.entity.User;
import com.waterbill.entity.WaterUsageLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AuditService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<AuditRecordDto> getUserAuditTrail(Long userId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<Number> revisions = reader.getRevisions(User.class, userId);
        List<AuditRecordDto> trail = new ArrayList<>();

        User previous = null;
        for (Number rev : revisions) {
            User current = reader.find(User.class, userId, rev);
            AuditRevisionEntity revEntity = reader.findRevision(AuditRevisionEntity.class, rev);
            Date date = reader.getRevisionDate(rev);

            AuditRecordDto dto = new AuditRecordDto();
            dto.setEntityName("User");
            dto.setEntityId(userId);
            dto.setRevisionNumber(rev);
            dto.setRevisionDate(date.toInstant());
            dto.setModifiedBy(revEntity != null ? revEntity.getModifiedByUsername() : "SYSTEM");
            dto.setClientIp(revEntity != null ? revEntity.getClientIp() : "127.0.0.1");

            Map<String, Object> currentMap = new HashMap<>();
            currentMap.put("accountStatus", current.getAccountStatus() != null ? current.getAccountStatus().name() : null);
            currentMap.put("role", current.getRole() != null ? current.getRole().name() : null);
            currentMap.put("fullName", current.getFullName());
            currentMap.put("email", current.getEmail());
            currentMap.put("isActive", current.getIsActive());
            dto.setNewValue(currentMap);

            if (previous != null) {
                dto.setRevisionType("MODIFICATION");
                Map<String, Object> prevMap = new HashMap<>();
                prevMap.put("accountStatus", previous.getAccountStatus() != null ? previous.getAccountStatus().name() : null);
                prevMap.put("role", previous.getRole() != null ? previous.getRole().name() : null);
                prevMap.put("fullName", previous.getFullName());
                prevMap.put("email", previous.getEmail());
                prevMap.put("isActive", previous.getIsActive());
                dto.setPreviousValue(prevMap);
            } else {
                dto.setRevisionType("CREATION");
                dto.setPreviousValue(Collections.emptyMap());
            }

            trail.add(dto);
            previous = current;
        }

        return trail;
    }

    @Transactional(readOnly = true)
    public List<AuditRecordDto> getUsageLogAuditTrail(Long logId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<Number> revisions = reader.getRevisions(WaterUsageLog.class, logId);
        List<AuditRecordDto> trail = new ArrayList<>();

        WaterUsageLog previous = null;
        for (Number rev : revisions) {
            WaterUsageLog current = reader.find(WaterUsageLog.class, logId, rev);
            AuditRevisionEntity revEntity = reader.findRevision(AuditRevisionEntity.class, rev);
            Date date = reader.getRevisionDate(rev);

            AuditRecordDto dto = new AuditRecordDto();
            dto.setEntityName("WaterUsageLog");
            dto.setEntityId(logId);
            dto.setRevisionNumber(rev);
            dto.setRevisionDate(date.toInstant());
            dto.setModifiedBy(revEntity != null ? revEntity.getModifiedByUsername() : "SYSTEM");
            dto.setClientIp(revEntity != null ? revEntity.getClientIp() : "127.0.0.1");

            Map<String, Object> currentMap = new HashMap<>();
            currentMap.put("currentReadingKl", current.getCurrentReadingKl());
            currentMap.put("dailyConsumptionKl", current.getDailyConsumptionKl());
            currentMap.put("readingDate", current.getReadingDate() != null ? current.getReadingDate().toString() : null);
            currentMap.put("logSource", current.getLogSource() != null ? current.getLogSource().name() : null);
            dto.setNewValue(currentMap);

            if (previous != null) {
                dto.setRevisionType("MODIFICATION");
                Map<String, Object> prevMap = new HashMap<>();
                prevMap.put("currentReadingKl", previous.getCurrentReadingKl());
                prevMap.put("dailyConsumptionKl", previous.getDailyConsumptionKl());
                prevMap.put("readingDate", previous.getReadingDate() != null ? previous.getReadingDate().toString() : null);
                prevMap.put("logSource", previous.getLogSource() != null ? previous.getLogSource().name() : null);
                dto.setPreviousValue(prevMap);
            } else {
                dto.setRevisionType("CREATION");
                dto.setPreviousValue(Collections.emptyMap());
            }

            trail.add(dto);
            previous = current;
        }

        return trail;
    }
}
