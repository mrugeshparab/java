package edu.campusnest.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.campusnest.data.*;
import edu.campusnest.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:campusnest;DB_CLOSE_DELAY=-1","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.jpa.hibernate.ddl-auto=create-drop","spring.jpa.database-platform=org.hibernate.dialect.H2Dialect","spring.flyway.enabled=false","campusnest.upload-dir=target/campusnest-test-uploads"})
@AutoConfigureMockMvc
class AdminPortalIntegrationTest {
 @Autowired MockMvc mvc; @Autowired UserRepository users; @Autowired PropertyRepository properties; @Autowired InquiryRepository inquiries; @Autowired AuditLogRepository logs; @Autowired PropertyImageRepository images; @Autowired ObjectMapper mapper;
 private User admin;
 @BeforeEach void seed() throws Exception {logs.deleteAll();inquiries.deleteAll();images.deleteAll();properties.deleteAll();users.deleteAll();var dir=java.nio.file.Path.of("target/campusnest-test-uploads");if(java.nio.file.Files.exists(dir))try(var paths=java.nio.file.Files.list(dir)){paths.forEach(path->{try{java.nio.file.Files.deleteIfExists(path);}catch(Exception ignored){}});}admin=users.saveAndFlush(new User("Portal Admin","portal-admin@campusnest.test",new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("safe-test-password"),null,Role.ADMIN));users.saveAndFlush(new User("Regular User","user@campusnest.test","unused-hash",null,Role.USER));}
 @Test void anonymousCannotReadAdminApi() throws Exception {mvc.perform(get("/api/admin/dashboard")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));}
 @Test @WithMockUser(username="user@campusnest.test",roles="USER") void regularUserCannotReadAdminApi() throws Exception {mvc.perform(get("/api/admin/dashboard")).andExpect(status().isForbidden());}
 @Test @WithMockUser(username="portal-admin@campusnest.test",roles="ADMIN") void dashboardUsesDatabaseAndEmptyTablesReturnZero() throws Exception {mvc.perform(get("/api/admin/dashboard")).andExpect(status().isOk()).andExpect(jsonPath("$.totalAccounts").value(2)).andExpect(jsonPath("$.totalUsers").value(1)).andExpect(jsonPath("$.totalProperties").value(0)).andExpect(jsonPath("$.totalInquiries").value(0));}
 @Test @WithMockUser(username="portal-admin@campusnest.test",roles="ADMIN") void approvingPendingListingPersistsAndWritesAuditLog() throws Exception {
  var owner=users.saveAndFlush(new User("Owner","owner@campusnest.test","unused-hash",null,Role.OWNER));
  var property=properties.saveAndFlush(new Property(owner,"Garden room","Bright private room","PRIVATE_ROOM","Central","Pune","Maharashtra","411001",new java.math.BigDecimal("12000.00"),"FURNISHED","SINGLE"));
  mvc.perform(patch("/api/admin/properties/{id}/approve",property.getId()).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.listingStatus").value("APPROVED"));
  org.junit.jupiter.api.Assertions.assertEquals(ListingStatus.APPROVED,properties.findById(property.getId()).orElseThrow().getListingStatus());
  org.junit.jupiter.api.Assertions.assertEquals(1,logs.count());
 }
 @Test @WithMockUser(username="portal-admin@campusnest.test",roles="ADMIN") void publicSearchIncludesOnlyApprovedAvailableProperties() throws Exception {
  var owner=users.saveAndFlush(new User("Owner","owner@campusnest.test","unused-hash",null,Role.OWNER));
  properties.saveAndFlush(new Property(owner,"Pending place","Waiting moderation","PG",null,"Pune",null,null,new java.math.BigDecimal("9000"),null,null));
  var publicPlace=new Property(owner,"Available place","Approved listing","PRIVATE_ROOM",null,"Pune",null,null,new java.math.BigDecimal("12000"),null,null);publicPlace.approve();properties.saveAndFlush(publicPlace);
  var unavailable=new Property(owner,"Unavailable place","Not currently available","HOSTEL",null,"Pune",null,null,new java.math.BigDecimal("8000"),null,null);unavailable.approve();unavailable.setAvailabilityStatus(Availability.UNAVAILABLE);properties.saveAndFlush(unavailable);
  mvc.perform(get("/api/properties").param("city","Pune")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].title").value("Available place"));
 }
 @Test @WithMockUser(username="user@campusnest.test",roles="USER") void studentCanViewApprovedHomeAndOwnerContact() throws Exception {
  var owner=users.saveAndFlush(new User("Contact Owner","contact-owner@campusnest.test","unused-hash","+91 9876543210",Role.OWNER));var property=new Property(owner,"Student home","A bright room","PRIVATE_ROOM","Central","Pune","Maharashtra","411001",new java.math.BigDecimal("12000"),"FURNISHED","SINGLE");property.approve();property=properties.saveAndFlush(property);
  mvc.perform(get("/api/user/properties/{id}",property.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Student home")).andExpect(jsonPath("$.ownerEmail").value("contact-owner@campusnest.test")).andExpect(jsonPath("$.ownerPhone").value("+91 9876543210"));
 }
 @Test void anonymousCannotReadStudentOwnerContact() throws Exception {
  var owner=users.saveAndFlush(new User("Contact Owner","contact-owner@campusnest.test","unused-hash","+91 9876543210",Role.OWNER));var property=new Property(owner,"Student home","A bright room","PRIVATE_ROOM",null,"Pune",null,null,new java.math.BigDecimal("12000"),null,null);property.approve();property=properties.saveAndFlush(property);
  mvc.perform(get("/api/user/properties/{id}",property.getId())).andExpect(status().isUnauthorized());
 }
 @Test @WithMockUser(username="portal-admin@campusnest.test",roles="ADMIN") void propertySearchSupportsTypeFilterAndOwnerSearch() throws Exception {
  var owner=users.saveAndFlush(new User("Owner","owner@campusnest.test","unused-hash",null,Role.OWNER));
  properties.saveAndFlush(new Property(owner,"Room","Description","PRIVATE_ROOM",null,"Pune",null,null,new java.math.BigDecimal("12000"),null,null));
  properties.saveAndFlush(new Property(owner,"Flat","Description","RENTAL_FLAT",null,"Pune",null,null,new java.math.BigDecimal("22000"),null,null));
  mvc.perform(get("/api/admin/properties").param("propertyType","PRIVATE_ROOM").param("search","Owner")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].title").value("Room"));
 }
 @Test @WithMockUser(username="portal-admin@campusnest.test",roles="ADMIN") void propertyDetailsLoadsOwnerAndFullListingFields() throws Exception {
  var owner=users.saveAndFlush(new User("Detail Owner","detail-owner@campusnest.test","unused-hash",null,Role.OWNER));var property=properties.saveAndFlush(new Property(owner,"Detail listing","Full listing description","PG","North","Pune","Maharashtra","411001",new java.math.BigDecimal("14000"),"FURNISHED","SINGLE"));
  mvc.perform(get("/api/admin/properties/{id}",property.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.ownerName").value("Detail Owner")).andExpect(jsonPath("$.description").value("Full listing description")).andExpect(jsonPath("$.postalCode").value("411001"));
 }
 @Test @WithMockUser(username="portal-admin@campusnest.test",roles="ADMIN") void ownerImagesAreAvailableToModeratorsAndAttachedToListing() throws Exception {
  var owner=users.saveAndFlush(new User("Owner","owner@campusnest.test","unused-hash",null,Role.OWNER));var property=properties.saveAndFlush(new Property(owner,"Photo listing","Listing with photos","PG",null,"Pune",null,null,new java.math.BigDecimal("12000"),null,null));
  byte[] png=java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p3sAAAAASUVORK5CYII=");var file=new org.springframework.mock.web.MockMultipartFile("images","room.png","image/png",png);
  var result=mvc.perform(multipart("/api/owner/properties/{id}/images",property.getId()).file(file).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("owner@campusnest.test").roles("OWNER")).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())).andExpect(status().isOk()).andExpect(jsonPath("$[0].url").value(org.hamcrest.Matchers.containsString("/api/properties/"+property.getId()+"/images/"))).andReturn();
  var imageId=mapper.readTree(result.getResponse().getContentAsString()).get(0).path("id").asLong();mvc.perform(get("/api/admin/properties")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].imageUrls[0]").isNotEmpty());
  mvc.perform(get("/api/properties/{propertyId}/images/{imageId}",property.getId(),imageId)).andExpect(status().isOk()).andExpect(content().contentType("image/png"));
 }
 @Test @WithMockUser(username="portal-admin@campusnest.test",roles="ADMIN") void inquirySearchMatchesSeekerAndShowsInquiry() throws Exception {
  var owner=users.saveAndFlush(new User("Owner","owner@campusnest.test","unused-hash",null,Role.OWNER));var seeker=users.findByEmailIgnoreCase("user@campusnest.test").orElseThrow();
  var property=new Property(owner,"Garden room","Description","PRIVATE_ROOM",null,"Pune",null,null,new java.math.BigDecimal("12000"),null,null);property.approve();property=properties.saveAndFlush(property);
  var inquiry=inquiries.saveAndFlush(new Inquiry(property,seeker,"Please share more details."));
  mvc.perform(get("/api/admin/inquiries").param("search","user@campusnest.test").param("status","OPEN")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].message").value("Please share more details."));
 }
 @Test void loginRequiresAndAcceptsSessionCsrfToken() throws Exception {
  var csrfResult=mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();var token=mapper.readTree(csrfResult.getResponse().getContentAsString()).path("token").asText();var session=(org.springframework.mock.web.MockHttpSession)csrfResult.getRequest().getSession(false);
  var response=mvc.perform(post("/api/auth/login").session(session).header("X-CSRF-TOKEN",token).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"portal-admin@campusnest.test\",\"password\":\"safe-test-password\"}" )).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN")).andReturn();
  mvc.perform(get("/api/auth/me").session((org.springframework.mock.web.MockHttpSession)response.getRequest().getSession(false))).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("portal-admin@campusnest.test"));
 }
 @Test void loginWithoutCsrfIsRejected() throws Exception {mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"portal-admin@campusnest.test\",\"password\":\"safe-test-password\"}")).andExpect(status().isForbidden());}
 @Test void csrfTokenCanBeReturnedForBrowserClient() throws Exception {mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());}
}
