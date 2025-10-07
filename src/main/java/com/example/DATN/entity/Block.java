// com/example/DATN/entity/Block.java
package com.example.DATN.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "block",
        indexes = {
                @Index(name = "idx_block_name", columnList = "name"),
                @Index(name = "idx_block_owner_id", columnList = "owner_id"),
                @Index(name = "idx_block_enable", columnList = "enable")
        }
)
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 255)
    @Column(length = 255)
    private String address;

    @Column(name = "count_room")
    private int count_room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "owner_id",
            referencedColumnName = "id_user",
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
            // khi dữ liệu sạch thì có thể đổi: foreignKey = @ForeignKey(name="fk_block_owner")
    )
    private User owner;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Ảnh đại diện cho dãy (URL/path), không bắt buộc
    @Size(max = 500)
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    // Trạng thái hoạt động của dãy: true = hoạt động
    @Builder.Default
    @Column(nullable = false)
    private boolean enable = true;

    @OneToMany(mappedBy = "block", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<Room> rooms = new LinkedHashSet<>();
}
