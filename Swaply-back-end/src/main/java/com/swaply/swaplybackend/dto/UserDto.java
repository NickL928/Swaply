package com.swaply.swaplybackend.dto;

import com.swaply.swaplybackend.enums.UserRole;

import java.time.LocalDateTime;

public class UserDto {
    private Long userId;
    private String userName;
    private String email;
    private String password;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private UserRole role;

    //Constructors
    public UserDto() {}

    public UserDto(Long userId, String userName, String email, String password) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    //does not include password for security reasons
    public UserDto(Long userId, String userName, String email) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
    }

  //Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    @Override
    public String toString() {
        return "UserDto{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isActive=" + isActive +
                ", role=" + role +
                '}';
    }
}
