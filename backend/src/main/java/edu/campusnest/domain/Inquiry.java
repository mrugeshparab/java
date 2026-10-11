package edu.campusnest.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="inquiries")
public class Inquiry {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="property_id",nullable=false) private Property property;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="seeker_id",nullable=false) private User seeker;
    @Column(nullable=false,columnDefinition="TEXT") private String message;
    @Column(name="inquiry_status",nullable=false,length=20) private String inquiryStatus;
    @org.hibernate.annotations.CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @org.hibernate.annotations.UpdateTimestamp @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected Inquiry(){}
    public Inquiry(Property property,User seeker,String message){this.property=property;this.seeker=seeker;this.message=message;this.inquiryStatus="OPEN";}
    public Long getId(){return id;} public Property getProperty(){return property;} public User getSeeker(){return seeker;}
    public String getMessage(){return message;} public String getInquiryStatus(){return inquiryStatus;} public Instant getCreatedAt(){return createdAt;}
}
