package com.example.transactions_service.repository;

import com.example.transactions_service.entities.Wallet;
import com.example.transactions_service.enums.WalletType;
import com.example.transactions_service.repositories.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class WalletRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByOwnerUserId_returnsWallet() {
        entityManager.persist(Wallet.builder()
                .ownerUserId(1L).walletType(WalletType.PERSONAL).balance(100L).build());
        entityManager.flush();

        Optional<Wallet> found = walletRepository.findByOwnerUserIdAndWalletType(1L, WalletType.PERSONAL);
        assertThat(found).isPresent();
        assertThat(found.get().getBalance()).isEqualTo(100L);
    }

    @Test
    void duplicatePersonalWallet_throwsConstraintViolation() {
        entityManager.persist(Wallet.builder().ownerUserId(1L).walletType(WalletType.PERSONAL).build());
        entityManager.flush();

        Wallet duplicate = Wallet.builder().ownerUserId(1L).walletType(WalletType.PERSONAL).build();

        assertThatThrownBy(() -> walletRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

}
