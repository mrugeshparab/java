package edu.campusnest.data;
import edu.campusnest.domain.AuditLog;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditLogRepository extends JpaRepository<AuditLog,Long> { @org.springframework.data.jpa.repository.EntityGraph(attributePaths={"actor"}) Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable); }
