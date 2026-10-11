package edu.campusnest.data;
import edu.campusnest.domain.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface PropertyRepository extends JpaRepository<Property,Long> {
 java.util.List<Property> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
 @org.springframework.data.jpa.repository.EntityGraph(attributePaths={"owner"}) java.util.Optional<Property> findWithOwnerById(Long id);
 @org.springframework.data.jpa.repository.EntityGraph(attributePaths={"owner"}) Page<Property> findAllByListingStatusAndAvailabilityStatus(ListingStatus listing,Availability availability,Pageable pageable);
 @org.springframework.data.jpa.repository.Query("select p from Property p join fetch p.owner where p.listingStatus=:listing and p.availabilityStatus=:availability and (:city is null or lower(p.city)=lower(:city))") Page<Property> publicSearch(@org.springframework.data.repository.query.Param("listing") ListingStatus listing,@org.springframework.data.repository.query.Param("availability") Availability availability,@org.springframework.data.repository.query.Param("city") String city,Pageable pageable);
 long countByListingStatus(ListingStatus status);
 long countByListingStatusAndAvailabilityStatus(ListingStatus status,Availability availability);
 @Query("select p from Property p join fetch p.owner o where (:term is null or lower(p.title) like lower(concat('%',:term,'%')) or lower(p.city) like lower(concat('%',:term,'%')) or lower(coalesce(p.locality,'')) like lower(concat('%',:term,'%')) or lower(o.fullName) like lower(concat('%',:term,'%')) or lower(o.email) like lower(concat('%',:term,'%'))) and (:listing is null or p.listingStatus=:listing) and (:availability is null or p.availabilityStatus=:availability) and (:type is null or lower(p.propertyType)=lower(:type))")
 Page<Property> search(@Param("term") String term,@Param("listing") ListingStatus listing,@Param("availability") Availability availability,@Param("type") String type,Pageable pageable);
}
