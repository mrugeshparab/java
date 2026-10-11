package edu.campusnest.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="users")
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="full_name", nullable=false, length=120) private String fullName;
    @Column(nullable=false, unique=true, length=254) private String email;
    @Column(name="password_hash", nullable=false, length=100) private String passwordHash;
    @Column(length=30) private String phone;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16) private Role role;
    @Enumerated(EnumType.STRING) @Column(name="account_status", nullable=false, length=16) private AccountStatus accountStatus=AccountStatus.ACTIVE;
    @org.hibernate.annotations.CreationTimestamp @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @org.hibernate.annotations.UpdateTimestamp @Column(name="updated_at", nullable=false) private Instant updatedAt;
    protected User() {}
    public User(String name,String email,String hash,String phone,Role role){this.fullName=name;this.email=email;this.passwordHash=hash;this.phone=phone;this.role=role;}
    public Long getId(){return id;} public String getFullName(){return fullName;} public String getEmail(){return email;}
    public String getPasswordHash(){return passwordHash;} public String getPhone(){return phone;} public Role getRole(){return role;}
    public AccountStatus getAccountStatus(){return accountStatus;} public Instant getCreatedAt(){return createdAt;}
    public void setAccountStatus(AccountStatus status){this.accountStatus=status;}
}
