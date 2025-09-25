package com.swaply.swaplybackend.entity;
import com.swaply.swaplybackend.enums.Category;
import com.swaply.swaplybackend.enums.ListingCondition;
import com.swaply.swaplybackend.enums.ListingStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_listing")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, name = "listing_id")
    private Long listingId;

    @Column(nullable = false, name = "title")
    private String title;

    @Column(length = 1000, name = "description")
    private String description;

    @Column(nullable = false, name = "price")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition")
    private ListingCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ListingStatus status;

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updatedDate;

    // Constructors
    public Listing() {}

    // Getters and Setters
    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public ListingCondition getCondition() {
        return condition;
    }

    public void setCondition(ListingCondition condition) {
        this.condition = condition;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public String toString() {
        return "Listing{" +
                "listingId=" + listingId +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", category=" + category +
                ", condition=" + condition +
                ", status=" + status +
                '}';
    }

}
