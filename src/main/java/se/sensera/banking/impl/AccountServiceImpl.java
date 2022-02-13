package se.sensera.banking.impl;

import se.sensera.banking.*;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;
import se.sensera.banking.utils.ListUtils;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AccountServiceImpl implements AccountService {

    private UsersRepository usersRepository;
    private AccountsRepository accountsRepository;


    public AccountServiceImpl(UsersRepository usersRepository, AccountsRepository accountsRepository) {
        this.usersRepository = usersRepository;
        this.accountsRepository = accountsRepository;
    }

    @Override
    public Account createAccount(String userId, String accountName) throws UseException {
        verifyAccountCreating(userId, accountName);
        AccountImpl account = new AccountImpl(usersRepository.getEntityById(userId).get(), accountName, userId, true);
        return accountsRepository.save(account);
    }

    private void verifyAccountCreating(String userId, String accountName) throws UseException {
        if (usersRepository.getEntityById(userId).isEmpty()) {
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND);
        }
        if (accountsRepository.all().anyMatch(x -> x.getName().equals(accountName))) {
            throw new UseException(Activity.CREATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
        }
    }

    @Override
    public Account changeAccount(String userId, String accountId, Consumer<ChangeAccount> changeAccountConsumer) throws UseException {
        Account account = accountsRepository.getEntityById(accountId).orElseThrow();
        User user = usersRepository.getEntityById(userId).orElseThrow();

        verifyChangingAccount(changeAccountConsumer, account, user);
        return account;
    }

    private void verifyChangingAccount(Consumer<ChangeAccount> changeAccountConsumer, Account account, User user) throws UseException {
        if (!account.getOwner().equals(user))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        if (!account.isActive()) {
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        }
        changeAccountConsumer.accept(name -> {
            if (accountsRepository.all().anyMatch(account1 -> account1.getName().equals(name))) {
                throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NAME_NOT_UNIQUE);
            }
            changingAccountName(account, name);
        });
    }

    private void changingAccountName(Account account, String name) {
        if (!name.equals(account.getName())) {
            account.setName(name);
            accountsRepository.save(account);
        }
    }

    @Override
    public Account inactivateAccount(String userId, String accountId) throws UseException {
        checkIfAccountOrUserNotFound(userId, accountId);

        Account account = accountsRepository.getEntityById(accountId).get();
        User user = usersRepository.getEntityById(userId).get();
        verifyInactivateAccount(account, user);

        account.setActive(false);
        accountsRepository.save(account);
        return account;
    }

    private void verifyInactivateAccount(Account account, User user) throws UseException {
        if (!account.isActive())
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_ACTIVE);
        if (!account.getOwner().equals(user))
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_OWNER);
    }

    private void checkIfAccountOrUserNotFound(String userId, String accountId) throws UseException {
        if (accountsRepository.getEntityById(accountId).isEmpty()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.NOT_FOUND);
        }
        if (usersRepository.getEntityById(userId).isEmpty()) {
            throw new UseException(Activity.INACTIVATE_ACCOUNT, UseExceptionType.USER_NOT_FOUND);
        }
    }

    @Override
    public Account addUserToAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        ifAccountNotFound(accountId);
        Account account = accountsRepository.getEntityById(accountId).get();
        User user = usersRepository.getEntityById(userIdToBeAssigned).get();

        verifyAddingUserToAccount(userId, userIdToBeAssigned, account);

        account.addUser(user);
        return accountsRepository.save(account);
    }

    private void ifAccountNotFound(String accountId) throws UseException {
        if (accountsRepository.getEntityById(accountId).isEmpty())
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_FOUND);
    }

    private void verifyAddingUserToAccount(String userId, String userIdToBeAssigned, Account account) throws UseException {
        if (!account.isActive())
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.ACCOUNT_NOT_ACTIVE);
        if (userId.equals(userIdToBeAssigned))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.CANNOT_ADD_OWNER_AS_USER);
        if (!userId.equals(account.getOwner().getId()))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        if (account.getUsers().anyMatch(user1 -> user1.equals(usersRepository.getEntityById(userIdToBeAssigned).get())))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.USER_ALREADY_ASSIGNED_TO_THIS_ACCOUNT);
    }

    @Override
    public Account removeUserFromAccount(String userId, String accountId, String userIdToBeAssigned) throws UseException {
        Account account = accountsRepository.getEntityById(accountId).get();
        User user = usersRepository.getEntityById(userIdToBeAssigned).get();

        verifyRemovingUserFromAccount(userId, userIdToBeAssigned, account);

        account.removeUser(user);
        return accountsRepository.save(account);
    }

    private void verifyRemovingUserFromAccount(String userId, String userIdToBeAssigned, Account account) throws UseException {
        if (!account.getOwner().getId().equals(userId))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.NOT_OWNER);
        if (account.getUsers().noneMatch(user1 -> user1.equals(usersRepository.getEntityById(userIdToBeAssigned).get())))
            throw new UseException(Activity.UPDATE_ACCOUNT, UseExceptionType.USER_NOT_ASSIGNED_TO_THIS_ACCOUNT);
    }


    @Override
    public Stream<Account> findAccounts(String searchValue, String userId, Integer pageNumber, Integer pageSize, SortOrder sortOrder) throws UseException {

        Stream<Account> all = accountsRepository.all();
        if (searchValue != null && !searchValue.isEmpty())
            all = all.filter(account -> account.getName().contains(searchValue));
        if (userId != null)
            all = all.filter(account -> account.getOwner().getId().equals(userId) || account.getUsers().anyMatch(user -> user.getId().equals(userId)));
        if (sortOrder == SortOrder.AccountName)
            all = all.sorted(Comparator.comparing(Account::getName));
        return ListUtils.applyPage(all, pageNumber, pageSize);
    }
}
