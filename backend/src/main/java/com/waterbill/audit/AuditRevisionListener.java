package com.waterbill.audit;

import com.waterbill.config.CorrelationIdFilter;
import org.hibernate.envers.RevisionListener;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AuditRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        if (!(revisionEntity instanceof AuditRevisionEntity auditEntity)) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                auditEntity.setModifiedByUsername(userDetails.getUsername());
            } else {
                auditEntity.setModifiedByUsername(principal.toString());
            }
        } else {
            auditEntity.setModifiedByUsername("SYSTEM");
        }

        String clientIp = MDC.get(CorrelationIdFilter.MDC_CLIENT_IP_KEY);
        if (clientIp != null && !clientIp.isBlank()) {
            auditEntity.setClientIp(clientIp);
        } else {
            auditEntity.setClientIp("127.0.0.1");
        }
    }
}
