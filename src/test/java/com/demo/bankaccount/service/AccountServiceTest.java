package com.demo.bankaccount.service;

import com.demo.bankaccount.entity.Account;
import com.demo.bankaccount.exception.AccountNotFoundException;
import com.demo.bankaccount.exception.InsufficientFundsException;
import com.demo.bankaccount.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class) tells JUnit to use Mockito to manage the mocks in this test class
// this means no Spring context is started — tests run in milliseconds
@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    // @Mock creates a fake AccountRepository — no real DB, no real queries
    // Mockito intercepts all calls to this and returns whatever we tell it to
    @Mock
    private AccountRepository accountRepository;

    // @InjectMocks creates a real AccountServiceImpl and injects the mock repository into it
    // this works because we used constructor injection in the service — not field injection
    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void whenOwnerIsBlank_thenThrowIllegalArgument() {
        // arrange + act + assert in one block — no setup needed because
        // the validation throws before even touching the repository
        // assertThatThrownBy captures the exception thrown by the lambda
        // and lets us verify both the type and the message
        assertThatThrownBy(() -> accountService.createAccount("", BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Owner name cannot be empty");
    }

    @Test
    void whenInitialBalanceIsNegative_thenThrowIllegalArgument() {
        // arrange + act + assert in one block — same as above
        // negative BigDecimal "-1" triggers the validation before the repository is touched
        assertThatThrownBy(() -> accountService.createAccount("Maria", new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Initial balance cannot be negative");
    }

    @Test
    void whenValidInput_thenAccountIsCreated() {
        // arrange: tell Mockito what to return when save() is called
        // thenAnswer(inv -> inv.getArgument(0)) means "return whatever was passed as the first argument"
        // getArgument(0) is the Account object the service built and passed to save()
        // this simulates what a real DB would do — persist it and return it back
        // it also means we are testing the actual Account the service creates, not a fake one we set up
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(repositorySaveCall -> repositorySaveCall.getArgument(0));

        // act: call the service with valid input
        Account result = accountService.createAccount("Maria", BigDecimal.TEN);

        // assert: verify the returned account has the correct data
        assertThat(result.getOwner()).isEqualTo("Maria");
        // isEqualByComparingTo instead of isEqualTo because BigDecimal("10") and BigDecimal("10.00")
        // are equal in value but not by equals() — always use isEqualByComparingTo for BigDecimal
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.TEN);
        // verify that save() was actually called — not just that the result is correct
        // this ensures the service is not returning data without persisting it
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void whenWithdrawExceedsBalance_thenThrowInsufficientFunds() {
        // arrange: create a fake account with a balance of 100
        UUID id = UUID.randomUUID();
        Account account = new Account();
        account.setBalance(new BigDecimal("100"));

        // when findById is called with this id, return the fake account wrapped in Optional
        // Optional.of(account) simulates the DB finding the account
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        // act + assert: try to withdraw 200 from an account with only 100
        // the service should throw InsufficientFundsException before calling save()
        assertThatThrownBy(() -> accountService.withdraw(id, new BigDecimal("200")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void whenWithdrawValidAmount_thenBalanceIsReduced() {
        // arrange: create a fake account with a balance of 100
        UUID id = UUID.randomUUID();
        Account account = new Account();
        account.setBalance(new BigDecimal("100"));

        // when findById is called with this id, return the fake account wrapped in Optional
        // Optional.of(account) simulates the DB finding the account
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        // when save is called with any Account, return that same Account back
        // getArgument(0) is the Account object the service passed to save()
        // this simulates the DB persisting the account and returning it
        when(accountRepository.save(any(Account.class))).thenAnswer(repositorySaveCall -> repositorySaveCall.getArgument(0));

        // act: withdraw 40 from the account that has 100
        Account result = accountService.withdraw(id, new BigDecimal("40"));

        // assert: balance should be 100 - 40 = 60
        // isEqualByComparingTo instead of isEqualTo because BigDecimal("60") and BigDecimal("60.00")
        // are equal in value but not by equals() — always use isEqualByComparingTo for BigDecimal
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("60"));
    }

    @Test
    void whenAccountNotFound_thenThrowAccountNotFoundException() {
        // arrange: generate a random id that does not exist in the DB
        UUID id = UUID.randomUUID();

        // when findById is called with this id, return an empty Optional
        // Optional.empty() simulates the DB not finding any account with this id
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        // act + assert: the service should throw AccountNotFoundException
        // because findById returned empty and orElseThrow() triggers
        assertThatThrownBy(() -> accountService.withdraw(id, BigDecimal.TEN))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
