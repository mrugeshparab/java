package edu.campusnest.data;
import edu.campusnest.domain.Inquiry;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InquiryRepository extends JpaRepository<Inquiry,Long> {
 long count();
 @org.springframework.data.jpa.repository.EntityGraph(attributePaths={"property","property.owner","seeker"}) Page<Inquiry> findAll(Pageable pageable);
 @org.springframework.data.jpa.repository.EntityGraph(attributePaths={"property","property.owner","seeker"}) Page<Inquiry> findAllByInquiryStatus(String status,Pageable pageable);
 @org.springframework.data.jpa.repository.EntityGraph(attributePaths={"property","property.owner","seeker"})
 @org.springframework.data.jpa.repository.Query("select i from Inquiry i join i.property p join i.seeker s where (:status is null or i.inquiryStatus=:status) and (:term is null or lower(p.title) like lower(concat('%',:term,'%')) or lower(p.city) like lower(concat('%',:term,'%')) or lower(s.fullName) like lower(concat('%',:term,'%')) or lower(s.email) like lower(concat('%',:term,'%')))")
 Page<Inquiry> search(@org.springframework.data.repository.query.Param("status") String status,@org.springframework.data.repository.query.Param("term") String term,Pageable pageable);
}
