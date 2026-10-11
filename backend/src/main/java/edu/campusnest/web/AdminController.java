package edu.campusnest.web;
import edu.campusnest.service.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@org.springframework.validation.annotation.Validated @RestController @RequestMapping("/api/admin")
public class AdminController {
 private final AdminService service;
 public AdminController(AdminService service){this.service=service;}
 public record StatusChange(@NotBlank String status){}
 public record Rejection(@NotBlank @Size(max=500) String reason){}
 @GetMapping("/dashboard") public AdminService.Dashboard dashboard(){return service.dashboard();}
 @GetMapping("/users") public Page<AdminService.UserRow> users(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(required=false) String search,@RequestParam(required=false) String role,@RequestParam(required=false) String status){return service.users(search,role,status,page,size);}
 @GetMapping("/users/{id}") public AdminService.UserRow user(@PathVariable @Positive long id){return service.user(id);}
 @PatchMapping("/users/{id}/status") public AdminService.UserRow status(@PathVariable @Positive long id,@Valid @RequestBody StatusChange body,Authentication a){return service.setUserStatus(id,body.status(),a.getName());}
 @GetMapping("/properties") public Page<AdminService.PropertyRow> properties(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(required=false) String search,@RequestParam(required=false) String listingStatus,@RequestParam(required=false) String availabilityStatus,@RequestParam(required=false) String propertyType){return service.properties(search,listingStatus,availabilityStatus,propertyType,page,size);}
 @GetMapping("/properties/{id}") public AdminService.PropertyRow property(@PathVariable @Positive long id){return service.property(id);}
 @PatchMapping("/properties/{id}/approve") public AdminService.PropertyRow approve(@PathVariable @Positive long id,Authentication a){return service.approve(id,a.getName());}
 @PatchMapping("/properties/{id}/reject") public AdminService.PropertyRow reject(@PathVariable @Positive long id,@Valid @RequestBody Rejection body,Authentication a){return service.reject(id,body.reason(),a.getName());}
 @PatchMapping("/properties/{id}/status") public AdminService.PropertyRow propertyStatus(@PathVariable @Positive long id,@Valid @RequestBody StatusChange body,Authentication a){return service.propertyStatus(id,body.status(),a.getName());}
 @GetMapping("/inquiries") public Page<AdminService.InquiryRow> inquiries(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(required=false) String status,@RequestParam(required=false) String search){return service.inquiries(status,search,page,size);}
 @GetMapping("/audit-logs") public Page<AdminService.AuditRow> audit(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.audit(page,size);}
}
