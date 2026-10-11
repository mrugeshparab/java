package edu.campusnest.service;

import edu.campusnest.data.*;
import edu.campusnest.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {
 private UserRepository users; private PropertyRepository properties; private InquiryRepository inquiries; private AuditLogRepository logs; private PropertyImageRepository images; private AdminService service;
 @BeforeEach void setup(){users=mock(UserRepository.class);properties=mock(PropertyRepository.class);inquiries=mock(InquiryRepository.class);logs=mock(AuditLogRepository.class);images=mock(PropertyImageRepository.class);service=new AdminService(users,properties,inquiries,logs,images);}
 private User user(long id,String email,Role role){var u=new User("Admin",email,"$2a$12$hash",null,role);ReflectionTestUtils.setField(u,"id",id);return u;}
 @Test void preventsLastActiveAdminFromBeingSuspended(){var actor=user(1,"admin@campusnest.test",Role.ADMIN);var target=user(2,"second-admin@campusnest.test",Role.ADMIN);when(users.findById(2L)).thenReturn(java.util.Optional.of(target));when(users.findByEmailIgnoreCase("admin@campusnest.test")).thenReturn(java.util.Optional.of(actor));when(users.lockByRoleAndAccountStatus(Role.ADMIN,AccountStatus.ACTIVE)).thenReturn(java.util.List.of(target));
  var ex=assertThrows(IllegalArgumentException.class,()->service.setUserStatus(2,"SUSPENDED","admin@campusnest.test"));assertTrue(ex.getMessage().contains("last active administrator"));verify(logs,never()).save(any());}
 @Test void preventsAdminFromSuspendingSelf(){var admin=user(1,"admin@campusnest.test",Role.ADMIN);when(users.findById(1L)).thenReturn(java.util.Optional.of(admin));when(users.findByEmailIgnoreCase("admin@campusnest.test")).thenReturn(java.util.Optional.of(admin));
  assertThrows(IllegalArgumentException.class,()->service.setUserStatus(1,"SUSPENDED","admin@campusnest.test"));verify(logs,never()).save(any());}
 @Test void rejectsUnknownAccountStatuses(){assertThrows(IllegalArgumentException.class,()->service.users(null,null,"LOCKED",0,20));}
}
