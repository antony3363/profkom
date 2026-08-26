package com.example.transactions_service.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Ровно одно из двух полей получателя должно быть заполнено:
 * receiverUserId — личный перевод человеку, receiverSchoolId — пополнение рабочего
 * кошелька школы.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequestDTO {

    private Long receiverUserId;

    private Long receiverSchoolId;

    @NotNull
    @Positive
    private Long amount;

    private String description;
}
