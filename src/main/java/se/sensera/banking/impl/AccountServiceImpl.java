package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

import javax.naming.Name;
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
        if (usersRepository.getEntityById(userId).isEmpty()) {
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND);
        }
        if (accountsRepository.all().anyMatch(x -> x.getName().equals(accountName))) {
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
        }
        AccountImpl account = new AccountImpl(usersRepository.getEntityById(userId).get(), accountName, userId, true);
        return accountsRepository.save(account);
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        Account account = accountsRepository.getEntityById(accountId).orElseThrow();
        User user = usersRepository.getEntityById(userId).orElseThrow();

        if (!account.getOwner().equals(user))
            throw new UseException(Activity.UPDATE_ACCOUNT,UseExceptionType.NOT_OWNER);
        if (!account.isActive()){
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        }
        changeAccountConsumer.accept(name -> {
            if (accountsRepository.all().anyMatch(account1 -> account1.getName().equals(name))){
                throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
            }
            if (!name.equals(account.getName())) {
            account.setName(name);
            accountsRepository.save(account);
            }
        });
        return account;
    }

    @Override
    public Account inactivateAccount(String userId, String accountId) throws UseException {
        if(accountsRepository.getEntityById(accountId).isEmpty()){
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_FOUND);
        }
        if (usersRepository.getEntityById(userId).isEmpty()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND);
        }

        Account account = accountsRepository.getEntityById(accountId).get();
        User user = usersRepository.getEntityById(userId).get();
        if (!account.isActive())
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        if (!account.getOwner().equals(user))
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_OWNER);

        account.setActive(false);
        accountsRepository.save(account);
        return account;
    }

    @Override
    public Account addUserToAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        Account account = accountsRepository.getEntityById(accountId).get();
        User user = usersRepository.getEntityById(userIdToBeAssigned).get();

        if (userId.equals(userIdToBeAssigned))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.CANNOT_ADD_OWNER_AS_USER);
        if (!userId.equals(account.getOwner().getId()))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);

        account.addUser(user);
        return accountsRepository.save(account);
    }

    @Override
    public Account removeUserFromAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        return null;
    }


    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {
        return null;
    }
}
