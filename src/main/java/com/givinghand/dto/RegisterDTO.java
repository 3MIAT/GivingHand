package com.givinghand.dto;

public class RegisterDTO {
	private String email;
    private String password;
    private String name;
    private String bio;
    private String birthday;
    private String role;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
