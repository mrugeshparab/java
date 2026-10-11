package edu.campusnest.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name="property_images")
public class PropertyImage {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="property_id",nullable=false) private Property property;
    @Column(name="storage_ref",nullable=false,length=500) private String storageRef;
    @Column(name="original_name",nullable=false,length=255) private String originalName;
    @Column(name="content_type",nullable=false,length=100) private String contentType;
    @Column(name="size_bytes",nullable=false) private long sizeBytes;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    protected PropertyImage() {}
    public PropertyImage(Property property,String storageRef,String originalName,String contentType,long sizeBytes){this.property=property;this.storageRef=storageRef;this.originalName=originalName;this.contentType=contentType;this.sizeBytes=sizeBytes;}
    public Long getId(){return id;} public Property getProperty(){return property;} public String getStorageRef(){return storageRef;} public String getOriginalName(){return originalName;} public String getContentType(){return contentType;} public long getSizeBytes(){return sizeBytes;} public Instant getCreatedAt(){return createdAt;}
}
