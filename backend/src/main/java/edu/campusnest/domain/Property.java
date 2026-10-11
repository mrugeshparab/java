package edu.campusnest.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name="properties")
public class Property {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="owner_id",nullable=false) private User owner;
    @Column(nullable=false,length=160) private String title;
    @Column(nullable=false,columnDefinition="TEXT") private String description;
    @Column(name="property_type",nullable=false,length=32) private String propertyType;
    @Column(length=120) private String locality;
    @Column(nullable=false,length=100) private String city;
    @Column(length=100) private String state;
    @Column(name="postal_code",length=16) private String postalCode;
    @Column(name="monthly_rent",nullable=false,precision=12,scale=2) private BigDecimal monthlyRent;
    @Column(length=24) private String furnishing;
    @Column(length=40) private String occupancy;
    @Enumerated(EnumType.STRING) @Column(name="availability_status",nullable=false,length=20) private Availability availabilityStatus=Availability.AVAILABLE;
    @Enumerated(EnumType.STRING) @Column(name="listing_status",nullable=false,length=20) private ListingStatus listingStatus=ListingStatus.PENDING;
    @Column(name="rejection_reason",length=500) private String rejectionReason;
    @org.hibernate.annotations.CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @org.hibernate.annotations.UpdateTimestamp @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected Property(){}
    public Property(User owner,String title,String description,String type,String locality,String city,String state,String postal,BigDecimal rent,String furnishing,String occupancy){this.owner=owner;this.title=title;this.description=description;this.propertyType=type;this.locality=locality;this.city=city;this.state=state;this.postalCode=postal;this.monthlyRent=rent;this.furnishing=furnishing;this.occupancy=occupancy;this.listingStatus=ListingStatus.PENDING;this.availabilityStatus=Availability.AVAILABLE;}
    public Long getId(){return id;} public User getOwner(){return owner;} public String getTitle(){return title;}
    public String getDescription(){return description;} public String getPropertyType(){return propertyType;}
    public String getLocality(){return locality;} public String getCity(){return city;} public String getState(){return state;}
    public String getPostalCode(){return postalCode;} public BigDecimal getMonthlyRent(){return monthlyRent;}
    public String getFurnishing(){return furnishing;} public String getOccupancy(){return occupancy;}
    public Availability getAvailabilityStatus(){return availabilityStatus;} public ListingStatus getListingStatus(){return listingStatus;}
    public String getRejectionReason(){return rejectionReason;} public Instant getCreatedAt(){return createdAt;}
    public void approve(){listingStatus=ListingStatus.APPROVED;rejectionReason=null;}
    public void reject(String reason){listingStatus=ListingStatus.REJECTED;rejectionReason=reason;}
    public void setAvailabilityStatus(Availability status){availabilityStatus=status;}
    public void setListingStatus(ListingStatus status){listingStatus=status;}
}
