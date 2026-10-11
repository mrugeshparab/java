package edu.campusnest.web;

import edu.campusnest.data.*;
import edu.campusnest.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.List;
import java.util.UUID;
import java.nio.file.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;

/** Shared starter endpoints for Owner submissions and public User browsing/inquiries. */
@RestController
public class PortalController {
 private final UserRepository users; private final PropertyRepository properties; private final InquiryRepository inquiries; private final PropertyImageRepository images; private final Path uploadRoot;
 public PortalController(UserRepository u,PropertyRepository p,InquiryRepository i,PropertyImageRepository images,@Value("${campusnest.upload-dir:./uploads}") String uploadDir){users=u;properties=p;inquiries=i;this.images=images;uploadRoot=Path.of(uploadDir).toAbsolutePath().normalize();}
 public record PropertyInput(@NotBlank @Size(max=160) String title,@NotBlank @Size(max=5000) String description,@NotBlank @Size(max=32) String propertyType,@Size(max=120) String locality,@NotBlank @Size(max=100) String city,@Size(max=100) String state,@Size(max=16) String postalCode,@NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal monthlyRent,@Size(max=24) String furnishing,@Size(max=40) String occupancy){}
 public record ImageInfo(Long id,String originalName,String url){}
 public record PublicProperty(Long id,String title,String description,String propertyType,String locality,String city,String state,String postalCode,BigDecimal monthlyRent,String furnishing,String occupancy,String ownerName,Instant createdAt,List<ImageInfo> images){}
 public record StudentProperty(Long id,String title,String description,String propertyType,String locality,String city,String state,String postalCode,BigDecimal monthlyRent,String furnishing,String occupancy,String ownerName,String ownerEmail,String ownerPhone,Instant createdAt,List<ImageInfo> images){}
 public record OwnerProperty(Long id,String title,String city,BigDecimal monthlyRent,String listingStatus,String rejectionReason,Instant createdAt,List<ImageInfo> images){}
 public record NewInquiry(@Positive long propertyId,@NotBlank @Size(max=3000) String message){}
 @GetMapping("/api/properties") public Page<PublicProperty> publicProperties(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,@RequestParam(required=false) String city){if(page<0||size<1||size>100)throw new IllegalArgumentException("Invalid page or size.");var pageable=PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt"));return properties.publicSearch(ListingStatus.APPROVED,Availability.AVAILABLE,trim(city),pageable).map(this::publicDto);}
 @GetMapping("/api/user/properties/{id}") public StudentProperty studentProperty(@PathVariable long id){var p=properties.findWithOwnerById(id).filter(x->x.getListingStatus()==ListingStatus.APPROVED&&x.getAvailabilityStatus()==Availability.AVAILABLE).orElseThrow(()->new java.util.NoSuchElementException("Home not found."));var owner=p.getOwner();return new StudentProperty(p.getId(),p.getTitle(),p.getDescription(),p.getPropertyType(),p.getLocality(),p.getCity(),p.getState(),p.getPostalCode(),p.getMonthlyRent(),p.getFurnishing(),p.getOccupancy(),owner.getFullName(),owner.getEmail(),owner.getPhone(),p.getCreatedAt(),imageInfos(p.getId()));}
 @GetMapping("/api/owner/properties") public java.util.List<OwnerProperty> ownerProperties(Authentication auth){var owner=users.findByEmailIgnoreCase(auth.getName()).orElseThrow();return properties.findByOwnerIdOrderByCreatedAtDesc(owner.getId()).stream().map(p->new OwnerProperty(p.getId(),p.getTitle(),p.getCity(),p.getMonthlyRent(),p.getListingStatus().name(),p.getRejectionReason(),p.getCreatedAt(),imageInfos(p.getId()))).toList();}
 @PostMapping("/api/owner/properties") public PublicProperty submit(@Valid @RequestBody PropertyInput in,Authentication auth){var owner=users.findByEmailIgnoreCase(auth.getName()).orElseThrow();var p=new Property(owner,in.title().trim(),in.description().trim(),in.propertyType().trim().toUpperCase(Locale.ROOT),trim(in.locality()),in.city().trim(),trim(in.state()),trim(in.postalCode()),in.monthlyRent(),trim(in.furnishing()),trim(in.occupancy()));return publicDto(properties.save(p));}
 @PostMapping(value="/api/owner/properties/{id}/images",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public List<ImageInfo> uploadImages(@PathVariable long id,@RequestParam("images") List<MultipartFile> files,Authentication auth){
   var owner=users.findByEmailIgnoreCase(auth.getName()).orElseThrow();var property=properties.findWithOwnerById(id).orElseThrow(()->new java.util.NoSuchElementException("Property not found."));
   if(!property.getOwner().getId().equals(owner.getId()))throw new java.util.NoSuchElementException("Property not found.");
   if(files==null||files.isEmpty())throw new IllegalArgumentException("Choose at least one image.");
   if(files.size()>8||images.countByPropertyId(id)+files.size()>8)throw new IllegalArgumentException("A property can have up to 8 images.");
   try{Files.createDirectories(uploadRoot);for(var file:files){if(file.isEmpty())continue;if(file.getSize()>5L*1024*1024)throw new IllegalArgumentException("Each image must be 5 MB or smaller.");String type=validatedImageType(file);String ext=switch(type){case "image/jpeg"->"jpg";case "image/png"->"png";default->"webp";};String key=UUID.randomUUID()+"."+ext;Path target=uploadRoot.resolve(key).normalize();if(!target.startsWith(uploadRoot))throw new IllegalArgumentException("Invalid image name.");try(InputStream input=file.getInputStream()){Files.copy(input,target,StandardCopyOption.REPLACE_EXISTING);}String name=Path.of(file.getOriginalFilename()==null?"property-image":file.getOriginalFilename()).getFileName().toString();images.save(new PropertyImage(property,key,name,type,file.getSize()));}}
   catch(IOException e){throw new IllegalStateException("The property images could not be saved.",e);}
   return imageInfos(id);
 }
 @GetMapping("/api/properties/{propertyId}/images/{imageId}") public ResponseEntity<Resource> getImage(@PathVariable long propertyId,@PathVariable long imageId,Authentication auth){
   var image=images.findWithPropertyAndOwnerById(imageId).orElseThrow(()->new java.util.NoSuchElementException("Image not found."));var property=image.getProperty();if(!property.getId().equals(propertyId))throw new java.util.NoSuchElementException("Image not found.");
   boolean publicListing=property.getListingStatus()==ListingStatus.APPROVED&&property.getAvailabilityStatus()==Availability.AVAILABLE;
   boolean owner=auth!=null&&auth.getName().equalsIgnoreCase(property.getOwner().getEmail());boolean admin=auth!=null&&auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));
   if(!publicListing&&!owner&&!admin)throw new java.util.NoSuchElementException("Image not found.");
   Path file=uploadRoot.resolve(image.getStorageRef()).normalize();if(!file.startsWith(uploadRoot))throw new java.util.NoSuchElementException("Image not found.");
   try{Resource resource=new UrlResource(file.toUri());if(!resource.exists()||!resource.isReadable())throw new java.util.NoSuchElementException("Image not found.");return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.getContentType())).header(HttpHeaders.CONTENT_DISPOSITION,"inline; filename=\""+image.getStorageRef()+"\"").body(resource);}catch(MalformedURLException e){throw new java.util.NoSuchElementException("Image not found.");}
 }
 @PostMapping("/api/user/inquiries") public java.util.Map<String,Object> createInquiry(@Valid @RequestBody NewInquiry input,Authentication auth){var seeker=users.findByEmailIgnoreCase(auth.getName()).orElseThrow();var property=properties.findById(input.propertyId()).orElseThrow(()->new java.util.NoSuchElementException("Property not found."));if(property.getListingStatus()!=ListingStatus.APPROVED||property.getAvailabilityStatus()!=Availability.AVAILABLE)throw new java.util.NoSuchElementException("Property not found.");var saved=inquiries.save(new Inquiry(property,seeker,input.message().trim()));return java.util.Map.of("id",saved.getId(),"status","OPEN","createdAt",saved.getCreatedAt()==null?Instant.now():saved.getCreatedAt());}
 private PublicProperty publicDto(Property p){return new PublicProperty(p.getId(),p.getTitle(),p.getDescription(),p.getPropertyType(),p.getLocality(),p.getCity(),p.getState(),p.getPostalCode(),p.getMonthlyRent(),p.getFurnishing(),p.getOccupancy(),p.getOwner().getFullName(),p.getCreatedAt(),imageInfos(p.getId()));}
 private List<ImageInfo> imageInfos(Long id){return images.findAllByPropertyIdOrderByCreatedAtAsc(id).stream().map(i->new ImageInfo(i.getId(),i.getOriginalName(),"/api/properties/"+id+"/images/"+i.getId())).toList();}
 private String validatedImageType(MultipartFile file){try(InputStream in=file.getInputStream()){byte[] b=in.readNBytes(12);if(b.length>=8&&(b[0]&255)==0x89&&b[1]=='P'&&b[2]=='N'&&b[3]=='G')return "image/png";if(b.length>=3&&(b[0]&255)==0xff&&(b[1]&255)==0xd8&&(b[2]&255)==0xff)return "image/jpeg";if(b.length>=12&&new String(b,0,4,java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF")&&new String(b,8,4,java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP"))return "image/webp";}catch(IOException ignored){}throw new IllegalArgumentException("Only JPEG, PNG, or WebP image files are accepted.");}
 private String trim(String s){return s==null||s.isBlank()?null:s.trim();}
}
