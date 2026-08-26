package com.example.profile_service.DTOs;

import com.example.profile_service.enums.MembershipStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponseDTO {
    private Long personId;
    private Long groupId;
    private String firstName;
    private String lastName;
    private String secondName;
    private String email;
    private String cardNumber;
    private String image;
    private MembershipStatus membershipStatus;
    private LocalDateTime updatedAt;
}
