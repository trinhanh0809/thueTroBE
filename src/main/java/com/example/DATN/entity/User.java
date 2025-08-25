package com.example.DATN.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"listRoles"})
@Entity @Table(name = "user")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Integer idUser;

    @Column(name = "first_name") private String firstName;
    @Column(name = "last_name")  private String lastName;
    @Column(name = "username", unique = true) private String username;
    @Column(name = "password", length = 512) private String password;
    @Column(name = "gender") private Character gender;
    @Column(name = "date_of_birth") private java.util.Date dateOfBirth;
    @Column(name = "email", unique = true) private String email;
    @Column(name = "phone_number") private String phoneNumber;
    @Column(name = "avatar") private String avatar;
    @Column(name = "enabled") private boolean enabled;
    @Column(name = "activation_code") private String activationCode;

    @Column(name = "is_host", nullable = false) private boolean host = false;
    @Column(name = "host_verified_at") private Instant hostVerifiedAt;

    @Column(name = "created_at") private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;

    @PrePersist void onCreate(){ createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate  void onUpdate(){ updatedAt = Instant.now(); }

    @ManyToMany(fetch = FetchType.EAGER,
            cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "id_user"),
            inverseJoinColumns = @JoinColumn(name = "id_role"))
    private List<Role> listRoles;
}

