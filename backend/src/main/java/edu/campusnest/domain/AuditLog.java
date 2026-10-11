package edu.campusnest.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

@Entity @Table(name="audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="actor_id",nullable=false) private User actor;
    @Column(nullable=false,length=60) private String action;
    @Column(name="entity_type",nullable=false,length=40) private String entityType;
    @Column(name="entity_id",nullable=false) private Long entityId;
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON) @Column(name="metadata_json",columnDefinition="json") private Map<String,Object> metadataJson;
    @org.hibernate.annotations.CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    protected AuditLog(){}
    public AuditLog(User actor,String action,String type,Long entityId,Map<String,Object> metadata){this.actor=actor;this.action=action;this.entityType=type;this.entityId=entityId;this.metadataJson=metadata;}
    public Long getId(){return id;} public User getActor(){return actor;} public String getAction(){return action;}
    public String getEntityType(){return entityType;} public Long getEntityId(){return entityId;}
    public Instant getCreatedAt(){return createdAt;} public Map<String,Object> getMetadataJson(){return metadataJson;}
}
