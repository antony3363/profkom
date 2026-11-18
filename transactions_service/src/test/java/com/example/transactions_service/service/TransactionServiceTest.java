package com.example.transactions_service.service;


import com.example.transactions_service.DTOs.*;
import com.example.transactions_service.entities.Transaction;
import com.example.transactions_service.entities.Wallet;
import com.example.transactions_service.enums.WalletType;
import com.example.transactions_service.exceptions.InsufficientBalanceException;
import com.example.transactions_service.repositories.TransactionRepository;
import com.example.transactions_service.repositories.WalletRepository;
import com.example.transactions_service.services.TransactionService;
import com.example.transactions_service.services.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.example.transactions_service.enums.TransactionType.*;
import static com.example.transactions_service.enums.TransactionStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletService walletService;

    @InjectMocks
    private TransactionService transactionService;



    private Wallet createWallet(UUID walletId, long ballance, boolean unleamited, WalletType walletType) {
        return Wallet.builder()
                .walletId(walletId)
                .walletType(walletType)
                .ownerUserId(1L)
                .balance(ballance)
                .unlimited(unleamited)
                .build();
    }

    private void stubTransactionSave(){
        when(transactionRepository.saveAndFlush(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }


    @Test
    @DisplayName("eventReward: успешное начисление баллов за посещение мероприятия")
    public void eventReward(){
        UUID senderWalletId = UUID.randomUUID();
        UUID receiverWalletId = UUID.randomUUID();

        Wallet senderWallet = createWallet(senderWalletId, 100, true, WalletType.PERSONAL);
        Wallet receiverWallet = createWallet(receiverWalletId, 0, false, WalletType.PERSONAL);

        when(walletService.getOrCreatePersonalWallet(1L, true)).thenReturn(senderWallet);
        when(walletService.getOrCreatePersonalWallet(2L, false)).thenReturn(receiverWallet);
        when(walletRepository.findWithLockByWalletId(senderWalletId)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findWithLockByWalletId(receiverWalletId)).thenReturn(Optional.of(receiverWallet));
        stubTransactionSave();

        EventRewardRequestDTO dto = EventRewardRequestDTO.builder()
                .receiverUserId(2L)
                .amount(10L)
                .eventId(UUID.randomUUID())
                .build();

        transactionService.eventReward(1L, dto);

        assertThat(receiverWallet.getBalance()).isEqualTo(10L);
        assertThat(senderWallet.getBalance()).isEqualTo(90L);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).saveAndFlush(captor.capture());

        Transaction transaction = captor.getValue();
        assertThat(transaction.getType()).isEqualTo(EVENT_REWARD);
        assertThat(transaction.getAmount()).isEqualTo(10L);
        assertThat(transaction.getStatus()).isEqualTo(SUCCESS);
        assertThat(transaction.getSenderWallet().getWalletId()).isEqualTo(senderWalletId);
        assertThat(transaction.getReceiverWallet().getWalletId()).isEqualTo(receiverWalletId);
    }

    @Test
    @DisplayName("transferFromAdmin: перевод от Литвинова")
    public void transferFromAdmin(){
        UUID senderWalletId = UUID.randomUUID();
        UUID receiverWalletId = UUID.randomUUID();

        Wallet senderWallet = createWallet(senderWalletId, 100, true, WalletType.PERSONAL);
        Wallet receiverWallet = createWallet(receiverWalletId, 0, false, WalletType.PERSONAL);

        when(walletService.getOrCreatePersonalWallet(1L, true)).thenReturn(senderWallet);
        when(walletService.getOrCreatePersonalWallet(2L, false)).thenReturn(receiverWallet);
        when(walletRepository.findWithLockByWalletId(senderWalletId)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findWithLockByWalletId(receiverWalletId)).thenReturn(Optional.of(receiverWallet));
        stubTransactionSave();

        TransferRequestDTO dto = TransferRequestDTO.builder()
                .receiverUserId(2L)
                .amount(10L)
                .build();

        transactionService.transferFromAdmin(1L, dto);

        assertThat(receiverWallet.getBalance()).isEqualTo(10L);
        assertThat(senderWallet.getBalance()).isEqualTo(90L);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).saveAndFlush(captor.capture());

        Transaction transaction = captor.getValue();
        assertThat(transaction.getSenderWallet()).isEqualTo(senderWallet);
        assertThat(transaction.getReceiverWallet()).isEqualTo(receiverWallet);
        assertThat(transaction.getType()).isEqualTo(TRANSFER);
        assertThat(transaction.getAmount()).isEqualTo(10L);
        assertThat(transaction.getStatus()).isEqualTo(SUCCESS);
    }

    @Test
    @DisplayName("schoolAward: профорг успешно переводит баллы с кошелька школы")
    public void schoolAwardSuccessful(){
        UUID receiverWalletId = UUID.randomUUID();
        UUID schoolWalletId = UUID.randomUUID();

        Wallet receiverWallet = createWallet(receiverWalletId, 0, false, WalletType.PERSONAL);
        Wallet schoolWallet = createWallet(schoolWalletId, 100, false, WalletType.SCHOOL);

        when(walletService.getOrCreatePersonalWallet(1L, false)).thenReturn(receiverWallet);
        when(walletService.getOrCreateSchoolWallet(2L)).thenReturn(schoolWallet);
        when(walletRepository.findWithLockByWalletId(receiverWalletId)).thenReturn(Optional.of(receiverWallet));
        when(walletRepository.findWithLockByWalletId(schoolWalletId)).thenReturn(Optional.of(schoolWallet));
        stubTransactionSave();

        SchoolAwardRequestDTO dto = SchoolAwardRequestDTO.builder()
                .receiverUserId(1L)
                .amount(10L)
                .build();

        transactionService.schoolAward(2L, dto);

        assertThat(receiverWallet.getBalance()).isEqualTo(10L);
        assertThat(schoolWallet.getBalance()).isEqualTo(90L);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).saveAndFlush(captor.capture());

        Transaction transaction = captor.getValue();
        assertThat(transaction.getAmount()).isEqualTo(10L);
        assertThat(transaction.getType()).isEqualTo(TRANSFER);
        assertThat(transaction.getStatus()).isEqualTo(SUCCESS);
        assertThat(transaction.getSenderWallet()).isEqualTo(schoolWallet);
        assertThat(transaction.getReceiverWallet()).isEqualTo(receiverWallet);
    }

    @Test
    @DisplayName("schoolAward: профорг переводит баллы, но баланса не хватает")
    public void schoolAward_InsufficientBalanceException(){
        UUID receiverWalletId = UUID.randomUUID();
        UUID schoolWalletId = UUID.randomUUID();

        Wallet receiverWallet = createWallet(receiverWalletId, 0, false, WalletType.PERSONAL);
        Wallet schoolWallet = createWallet(schoolWalletId, 5, false, WalletType.SCHOOL);

        when(walletService.getOrCreatePersonalWallet(1L, false)).thenReturn(receiverWallet);
        when(walletService.getOrCreateSchoolWallet(2L)).thenReturn(schoolWallet);
        when(walletRepository.findWithLockByWalletId(schoolWalletId)).thenReturn(Optional.of(schoolWallet));

        SchoolAwardRequestDTO dto = SchoolAwardRequestDTO.builder()
                .receiverUserId(1L)
                .amount(10L)
                .build();

        assertThatThrownBy(() -> transactionService.schoolAward(2L, dto))
            .isInstanceOf(InsufficientBalanceException.class);

        assertThat(receiverWallet.getBalance()).isEqualTo(0L);
        assertThat(schoolWallet.getBalance()).isEqualTo(5L);
    }

    @Test
    @DisplayName("chargePurchase: успешное совершение покупки")
    public void chargePurchase(){
        UUID walletId = UUID.randomUUID();
        Wallet wallet = createWallet(walletId, 100L, false, WalletType.PERSONAL);

        when(walletService.getOrCreatePersonalWallet(1L, false)).thenReturn(wallet);
        when(walletRepository.findWithLockByWalletId(walletId)).thenReturn(Optional.of(wallet));
        stubTransactionSave();

        PurchaseChargeRequestDTO dto = PurchaseChargeRequestDTO.builder()
                .buyerUserId(1L)
                .amount(30L)
                .purchaseId(UUID.randomUUID())
                .build();

        TransactionResponseDTO result = transactionService.chargePurchase(dto);

        assertThat(wallet.getBalance()).isEqualTo(70L);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).saveAndFlush(captor.capture());

        Transaction transaction = captor.getValue();
        assertThat(transaction.getAmount()).isEqualTo(30L);
        assertThat(transaction.getType()).isEqualTo(PURCHASE);
        assertThat(transaction.getStatus()).isEqualTo(SUCCESS);
        assertThat(transaction.getSenderWallet()).isSameAs(wallet);
        assertThat(transaction.getReceiverWallet()).isNull();

        assertThat(result.getAmount()).isEqualTo(30L);
        assertThat(result.getType()).isEqualTo(PURCHASE);
    }

    @Test
    @DisplayName("chargePurchase: не хватает средств")
    public void chargePurchase_InsufficientBalanceException(){
        UUID walletId = UUID.randomUUID();
        Wallet wallet = createWallet(walletId, 10L, false, WalletType.PERSONAL);

        when(walletService.getOrCreatePersonalWallet(1L, false)).thenReturn(wallet);
        when(walletRepository.findWithLockByWalletId(walletId)).thenReturn(Optional.of(wallet));

        PurchaseChargeRequestDTO dto = new PurchaseChargeRequestDTO();
        dto.setBuyerUserId(1L);
        dto.setAmount(30L);
        dto.setPurchaseId(UUID.randomUUID());

        assertThatThrownBy(() -> transactionService.chargePurchase(dto))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(wallet.getBalance()).isEqualTo(10L);
    }

    @Test
    @DisplayName("refundPurchase: успешный возврат средств за покупку")
    public void refundPurchase(){
        UUID walletId = UUID.randomUUID();
        Wallet wallet = createWallet(walletId, 100L, false, WalletType.PERSONAL);

        when(walletService.getOrCreatePersonalWallet(1L, false)).thenReturn(wallet);
        when(walletRepository.findWithLockByWalletId(walletId)).thenReturn(Optional.of(wallet));
        stubTransactionSave();

        PurchaseChargeRequestDTO dto = PurchaseChargeRequestDTO.builder()
                .buyerUserId(1L)
                .amount(30L)
                .purchaseId(UUID.randomUUID())
                .build();

        transactionService.refundPurchase(dto);

        assertThat(wallet.getBalance()).isEqualTo(130L);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).saveAndFlush(captor.capture());

        Transaction transaction = captor.getValue();
        assertThat(transaction.getAmount()).isEqualTo(30L);
        assertThat(transaction.getType()).isEqualTo(REFUND);
        assertThat(transaction.getStatus()).isEqualTo(SUCCESS);
        assertThat(transaction.getSenderWallet()).isNull();
    }

    

}
