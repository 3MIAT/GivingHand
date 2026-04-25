package com.givinghand.service;

import java.time.LocalDate;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import com.givinghand.dto.LoginDTO;
import com.givinghand.dto.ProfileDTO;
import com.givinghand.dto.RegisterDTO;
import com.givinghand.model.Role;
import com.givinghand.model.User;


@Stateless
public class UserService {

    @PersistenceContext(unitName = "givinghandPU")
    private EntityManager em;

    public void register(RegisterDTO dto) {
        if (isEmpty(dto.getEmail()) || isEmpty(dto.getPassword()) || isEmpty(dto.getName()) || isEmpty(dto.getBirthday())
                || isEmpty(dto.getRole())) {
            throw new IllegalArgumentException("All fields except bio are required.");
        }

        if (!dto.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }

        LocalDate birthday;
        try {
            birthday = LocalDate.parse(dto.getBirthday());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid birthday format. Use YYYY-MM-DD.");
        }

        if (birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birthday cannot be in the future.");
        }

        Role role = parseRole(dto.getRole());

        Long count = (Long) em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email")
                .setParameter("email", dto.getEmail()).getSingleResult();
        if (count.longValue() > 0L) {
            throw new IllegalArgumentException("Email already registered.");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(PasswordUtil.hash(dto.getPassword()));
        user.setName(dto.getName());
        user.setBio(dto.getBio());
        user.setBirthday(birthday);
        user.setRole(role);
        em.persist(user);
    }

    public String login(LoginDTO dto) {
        if (isEmpty(dto.getEmail()) || isEmpty(dto.getPassword())) {
            throw new SecurityException("Invalid credentials.");
        }

        try {
            User user = (User) em.createQuery("SELECT u FROM User u WHERE u.email = :email")
                    .setParameter("email", dto.getEmail()).getSingleResult();

            if (!PasswordUtil.verify(dto.getPassword(), user.getPassword())) {
                throw new SecurityException("Invalid credentials.");
            }

            return UUID.randomUUID().toString();
        } catch (NoResultException e) {
            throw new SecurityException("Invalid credentials.");
        }
    }

    public void updateProfile(Long userId, ProfileDTO dto) {
        User user = em.find(User.class, userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (!isEmpty(dto.getName())) {
            user.setName(dto.getName());
        }
        if (!isEmpty(dto.getBio())) {
            user.setBio(dto.getBio());
        }
        em.merge(user);
    }

    private Role parseRole(String value) {
        if ("donor".equalsIgnoreCase(value)) {
            return Role.DONOR;
        }
        if ("organization".equalsIgnoreCase(value)) {
            return Role.ORGANIZATION;
        }
        throw new IllegalArgumentException("Role must be 'donor' or 'organization'.");
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
