package edu.campusnest.data;
import edu.campusnest.domain.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Long> {
 Optional<User> findByEmailIgnoreCase(String email);
 long countByRole(Role role); long countByRoleAndAccountStatus(Role role,AccountStatus status);
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select u from User u where u.role=:role and u.accountStatus=:status")
 java.util.List<User> lockByRoleAndAccountStatus(@org.springframework.data.repository.query.Param("role") Role role,@org.springframework.data.repository.query.Param("status") AccountStatus status);
 long countByAccountStatus(AccountStatus status);
 @Query("select u from User u where (:term is null or lower(u.fullName) like lower(concat('%',:term,'%')) or lower(u.email) like lower(concat('%',:term,'%'))) and (:role is null or u.role=:role) and (:status is null or u.accountStatus=:status)")
 Page<User> search(@Param("term") String term,@Param("role") Role role,@Param("status") AccountStatus status,Pageable pageable);
}
