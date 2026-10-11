package edu.campusnest.data;

import edu.campusnest.domain.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PropertyImageRepository extends JpaRepository<PropertyImage,Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths={"property","property.owner"}) java.util.Optional<PropertyImage> findWithPropertyAndOwnerById(Long id);
    List<PropertyImage> findAllByPropertyIdOrderByCreatedAtAsc(Long propertyId);
    long countByPropertyId(Long propertyId);
}
