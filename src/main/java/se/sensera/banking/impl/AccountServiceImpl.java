package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AccountServiceImpl implements AccountService {

    private UsersRepository usersRepository;
    private AccountsRepository accountsRepository;

    public AccountServiceImpl() {
    }

    public AccountServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
    }

    @Override
    public Account createAccount(String userId, String accountName) throws UseException {

        AccountImpl account= new AccountImpl(usersRepository.getEntityById(userId).get(), accountName, userId , true);
        return accountsRepository.save(account);
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        return null;
    }

    @Override
    public Account addUserToAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        return null;
    }

    @Override
    public Account removeUserFromAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        return null;
    }

    @Override
    public Account inactivateAccount(String userId, String accountId) throws UseException {
        return null;
    }

    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {
        return null;
    }
}
