package com.example.profile_service.DTOs;

import com.example.profile_service.enums.MembershipStatus;
import jakarta.validation.constraints.Email;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdateRequestDTO {

    private Long groupId;
    private String firstName;
    private String lastName;
    private String secondName;

    @Email
    private String email;

    private String cardNumber;
    private String image;
    private MembershipStatus membershipStatus;
}
