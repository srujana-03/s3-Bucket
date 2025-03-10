package com.rest.s3.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.rest.s3.model.UserData;
import com.rest.s3.repository.UserRepo;

@Service
public class UserService {
    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public UserData saveUser(UserData user) {
        validateUser(user);
        
        UserData userWithSameEmail = userRepo.findByEmailIgnoreCase(user.getEmail());//no same email exists for different users
        
        if (userWithSameEmail != null && !userWithSameEmail.getUsername().equalsIgnoreCase(user.getUsername())) {
            throw new IllegalArgumentException("This email is already associated with another user. Please change your email ID.");
        }

        UserData existingUser = userRepo.findByUsernameIgnoreCase(user.getUsername());
        if (existingUser != null) {
            if (existingUser.getEmail().equalsIgnoreCase(user.getEmail())) {
                throw new IllegalArgumentException("Username already has this email ID. Please update the email.");
            }

            if (!existingUser.getEmail().equalsIgnoreCase(user.getEmail())) {
                existingUser.setEmail(user.getEmail());
                return userRepo.save(existingUser);
            }
        }

        return userRepo.save(user);
    }








    private void validateUser(UserData user) {
        if (!StringUtils.hasText(user.getUsername())) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (!StringUtils.hasText(user.getEmail())) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
            throw new IllegalArgumentException("Username must be between 3 and 20 characters.");
        }

//        if (userRepo.existsByUsernameIgnoreCase(user.getUsername())) {
//            throw new IllegalArgumentException("Username already exists, please choose another one.");
//        }

//        if (userRepo.existsByEmailIgnoreCase(user.getEmail())) {
//            throw new IllegalArgumentException("Email already exists, please choose another one.");
//        }

        if (!isValidEmailFormat(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format.");
        }

        if (containsWhitespace(user.getEmail())) {
            throw new IllegalArgumentException("Email cannot contain whitespace.");
        }

        if (hasMultipleAtSymbols(user.getEmail())) {
            throw new IllegalArgumentException("Email cannot contain more than one '@' symbol.");
        }

        if (isInvalidEmailDomain(user.getEmail())) {
            throw new IllegalArgumentException("Email domain is invalid.");
        }

        if (user.getEmail().length() > 254) {
            throw new IllegalArgumentException("Email is too long. Maximum length is 254 characters.");
        }

        if (startsWithNumber(user.getEmail())) {
            throw new IllegalArgumentException("Email cannot start with a number.");
        }

        if (containsSpaces(user.getUsername())) {
            throw new IllegalArgumentException("Username cannot contain spaces.");
        }

        if (containsSpecialCharacters(user.getUsername())) {
            throw new IllegalArgumentException("Username cannot contain special characters.");
        }

        if (!startsWithLetter(user.getUsername())) {
            throw new IllegalArgumentException("Username must start with a letter.");
        }
    }

    private boolean isValidEmailFormat(String email) {
        String emailRegex = "^[A-Za-z][A-Za-z0-9._%+-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private boolean containsWhitespace(String email) {
        return email.contains(" ");
    }

    private boolean hasMultipleAtSymbols(String email) {
        int atCount = email.length() - email.replace("@", "").length();
        return atCount > 1;
    }

    private boolean isInvalidEmailDomain(String email) {
        String domainPart = email.substring(email.indexOf('@') + 1);
        return !domainPart.contains(".");
    }

    private boolean startsWithNumber(String email) {
        String localPart = email.split("@")[0];
        return localPart.length() > 0 && Character.isDigit(localPart.charAt(0));
    }

    private boolean containsSpaces(String username) {
        return username.contains(" ");
    }

    private boolean containsSpecialCharacters(String username) {
        String specialCharacters = "[^a-zA-Z0-9]";
        return username.matches(".*" + specialCharacters + ".*");
    }

    private boolean startsWithLetter(String username) {
        return Character.isLetter(username.charAt(0));
    }
}
