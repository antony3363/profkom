package com.example.profile_service.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileCreateRequestDTO {

    /**
     * Приходит извне от Auth Service (пока — явно, до подключения Kafka).
     */
    @NotNull
    private Long lichnostId;

    private Long groupId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String secondName;

    @NotBlank
    @Email
    private String email;

    private String cardNumber;

    private String image;
}
