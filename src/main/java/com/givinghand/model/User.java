package com.givinghand.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

/**
 * Stores application users, their login/profile data, and the donor contribution history.
 * Endpoints using this entity are exposed from /api/register, /api/login, and /api/profile/{userId}.
 * Important notes: roles are stored as strings for JAAS-friendly readability, and contribution history
 * is updated when a donation reaches the Distributed lifecycle state.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String bio;

    @Column(nullable = false)
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ElementCollection
    @CollectionTable(name = "user_contribution_history", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "entry_text", nullable = false)
    private List<String> contributionHistory = new ArrayList<String>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<String> getContributionHistory() {
        return contributionHistory;
    }

    public void setContributionHistory(List<String> contributionHistory) {
        this.contributionHistory = contributionHistory;
    }
}
