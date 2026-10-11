package edu.campusnest.service;
import edu.campusnest.data.*;
import edu.campusnest.domain.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class AdminService {
 private final UserRepository users; private final PropertyRepository properties; private final InquiryRepository inquiries; private final AuditLogRepository logs; private final PropertyImageRepository images;
 public AdminService(UserRepository u,PropertyRepository p,InquiryRepository i,AuditLogRepository l,PropertyImageRepository images){users=u;properties=p;inquiries=i;logs=l;this.images=images;}
 public record UserRow(Long id,String fullName,String email,String phone,String role,String status,Instant createdAt){}
 public record PropertyRow(Long id,String title,String description,String propertyType,String locality,String city,String state,String postalCode,String furnishing,String occupancy,java.math.BigDecimal monthlyRent,String availabilityStatus,String listingStatus,String ownerName,String ownerEmail,Instant createdAt,String rejectionReason,List<String> imageUrls){}
 public record InquiryRow(Long id,String propertyTitle,String city,String ownerName,String seekerName,String seekerEmail,String message,String status,Instant createdAt){}
 public record AuditRow(Long id,String actor,String action,String entityType,Long entityId,Map<String,Object> metadata,Instant createdAt){}
 public record Dashboard(long totalAccounts,long totalUsers,long totalOwners,long totalProperties,long approvedActiveListings,long pendingApprovals,long rejectedListings,long inactiveListings,long totalInquiries,List<AuditRow> recentActivity){}
 private UserRow userDto(User u){return new UserRow(u.getId(),u.getFullName(),u.getEmail(),u.getPhone(),u.getRole().name(),u.getAccountStatus().name(),u.getCreatedAt());}
 private PropertyRow propertyDto(Property p){var imageUrls=images.findAllByPropertyIdOrderByCreatedAtAsc(p.getId()).stream().map(i->"/api/properties/"+p.getId()+"/images/"+i.getId()).toList();return new PropertyRow(p.getId(),p.getTitle(),p.getDescription(),p.getPropertyType(),p.getLocality(),p.getCity(),p.getState(),p.getPostalCode(),p.getFurnishing(),p.getOccupancy(),p.getMonthlyRent(),p.getAvailabilityStatus().name(),p.getListingStatus().name(),p.getOwner().getFullName(),p.getOwner().getEmail(),p.getCreatedAt(),p.getRejectionReason(),imageUrls);}
 private AuditRow auditDto(AuditLog a){return new AuditRow(a.getId(),a.getActor().getFullName(),a.getAction(),a.getEntityType(),a.getEntityId(),a.getMetadataJson(),a.getCreatedAt());}
 private Pageable page(int number,int size){if(number<0)throw new IllegalArgumentException("Page must be zero or greater.");if(size<1||size>100)throw new IllegalArgumentException("Page size must be from 1 to 100.");return PageRequest.of(number,size);}
 public Dashboard dashboard(){var recent=logs.findAllByOrderByCreatedAtDesc(PageRequest.of(0,8)).map(this::auditDto).getContent();
  return new Dashboard(users.count(),users.countByRole(Role.USER),users.countByRole(Role.OWNER),properties.count(),properties.countByListingStatusAndAvailabilityStatus(ListingStatus.APPROVED,Availability.AVAILABLE),properties.countByListingStatus(ListingStatus.PENDING),properties.countByListingStatus(ListingStatus.REJECTED),properties.countByListingStatus(ListingStatus.INACTIVE),inquiries.count(),recent);}
 public Page<UserRow> users(String term,String role,String status,int page,int size){Role r=parseRole(role);AccountStatus s=parseAccount(status);return users.search(clean(term),r,s,page(page,size)).map(this::userDto);}
 public UserRow user(long id){return userDto(users.findById(id).orElseThrow(()->new NoSuchElementException("Account not found.")));}
 @Transactional public UserRow setUserStatus(long id,String status,String actorEmail){var target=users.findById(id).orElseThrow(()->new NoSuchElementException("Account not found."));AccountStatus next=parseAccount(status);if(next==null)throw new IllegalArgumentException("Status must be ACTIVE or SUSPENDED.");
  var actor=users.findByEmailIgnoreCase(actorEmail).orElseThrow();if(next==AccountStatus.SUSPENDED&&actor.getId().equals(id))throw new IllegalArgumentException("You cannot suspend your own administrator account.");
  if(target.getRole()==Role.ADMIN&&target.getAccountStatus()==AccountStatus.ACTIVE&&next==AccountStatus.SUSPENDED&&users.lockByRoleAndAccountStatus(Role.ADMIN,AccountStatus.ACTIVE).size()<=1)throw new IllegalArgumentException("The last active administrator cannot be suspended.");
  target.setAccountStatus(next);logs.save(new AuditLog(actor,"ACCOUNT_"+next,"USER",id,Map.of("status",next.name())));return userDto(target);}
 public Page<PropertyRow> properties(String term,String listing,String availability,String type,int page,int size){return properties.search(clean(term),parseListing(listing),parseAvailability(availability),clean(type),page(page,size)).map(this::propertyDto);}
 public PropertyRow property(long id){return propertyDto(properties.findWithOwnerById(id).orElseThrow(()->new NoSuchElementException("Property not found.")));}
 @Transactional public PropertyRow approve(long id,String actorEmail){var p=properties.findById(id).orElseThrow(()->new NoSuchElementException("Property not found."));if(p.getListingStatus()!=ListingStatus.PENDING)throw new IllegalArgumentException("Only pending properties can be approved.");p.approve();audit(actorEmail,"LISTING_APPROVED",id,Map.of("status","APPROVED"));return propertyDto(p);}
 @Transactional public PropertyRow reject(long id,String reason,String actorEmail){if(reason==null||reason.isBlank()||reason.length()>500)throw new IllegalArgumentException("A rejection reason of 1 to 500 characters is required.");var p=properties.findById(id).orElseThrow(()->new NoSuchElementException("Property not found."));if(p.getListingStatus()!=ListingStatus.PENDING)throw new IllegalArgumentException("Only pending properties can be rejected.");p.reject(reason.trim());audit(actorEmail,"LISTING_REJECTED",id,Map.of("status","REJECTED"));return propertyDto(p);}
 @Transactional public PropertyRow propertyStatus(long id,String status,String actorEmail){var p=properties.findById(id).orElseThrow(()->new NoSuchElementException("Property not found."));ListingStatus next=parseListing(status);if(next!=ListingStatus.INACTIVE&&next!=ListingStatus.APPROVED)throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE.");p.setListingStatus(next);audit(actorEmail,"LISTING_"+next,id,Map.of("status",next.name()));return propertyDto(p);}
 public Page<InquiryRow> inquiries(String status,String term,int page,int size){var pg=page(page,size);String normalized=clean(status)==null?null:status.trim().toUpperCase();if(normalized!=null&&!Set.of("OPEN","RESPONDED","CLOSED").contains(normalized))throw new IllegalArgumentException("Inquiry status must be OPEN, RESPONDED, or CLOSED.");return inquiries.search(normalized,clean(term),pg).map(i->new InquiryRow(i.getId(),i.getProperty().getTitle(),i.getProperty().getCity(),i.getProperty().getOwner().getFullName(),i.getSeeker().getFullName(),i.getSeeker().getEmail(),i.getMessage(),i.getInquiryStatus(),i.getCreatedAt()));}
 public Page<AuditRow> audit(int page,int size){return logs.findAllByOrderByCreatedAtDesc(page(page,size)).map(this::auditDto);}
 private void audit(String email,String action,Long id,Map<String,Object> metadata){logs.save(new AuditLog(users.findByEmailIgnoreCase(email).orElseThrow(),action,"PROPERTY",id,metadata));}
 private Role parseRole(String v){return v==null||v.isBlank()?null:Role.valueOf(v.trim().toUpperCase());}
 private AccountStatus parseAccount(String v){return v==null||v.isBlank()?null:AccountStatus.valueOf(v.trim().toUpperCase());}
 private ListingStatus parseListing(String v){return v==null||v.isBlank()?null:ListingStatus.valueOf(v.trim().toUpperCase());}
 private Availability parseAvailability(String v){return v==null||v.isBlank()?null:Availability.valueOf(v.trim().toUpperCase());}
 private String clean(String s){return s==null||s.isBlank()?null:s.trim();}
}
