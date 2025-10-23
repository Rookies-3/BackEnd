package com.store.rookiesoneteam.domain.entity;

import com.store.rookiesoneteam.domain.enums.UserRole;
import com.store.rookiesoneteam.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Table(name = "users")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Getter
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, length = 11)
    private String phone;

    @Column(nullable = false, length = 150)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    private LocalDateTime lastLogin;
    private LocalDateTime deleted;
    private LocalDateTime approvedAt;

    @CreatedDate
    private LocalDateTime created;
    @LastModifiedDate
    private LocalDateTime updated;

    // === 비즈니스 로직 === //
    public void deleted() {
        this.status = UserStatus.DELETED;
        this.deleted = LocalDateTime.now();
    }

    public void changeName(String newName) {
        if (newName != null && !newName.isBlank()) {
            this.name = newName;
        }
    }

    public void changeNickname(String newNickname) {
        if (newNickname != null && !newNickname.isBlank()) {
            this.nickname = newNickname;
        }
    }

    public void changeEmail(String newEmail) {
        if (newEmail != null && !newEmail.isBlank()) {
            this.email = newEmail;
        }
    }

    public void changePhone(String newPhone) {
        if (newPhone != null && !newPhone.isBlank()) {
            this.phone = newPhone;
        }
    }

    public void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }

    // === UserDetails 구현 메소드 === //
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isEnabled() { return status == UserStatus.ACTIVE; }

    // 사용하지 않는 UserDetails 메소드들
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }
}
