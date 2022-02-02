package se.sensera.banking.impl;

import se.sensera.banking.User;
import se.sensera.banking.UserService;
import se.sensera.banking.UsersRepository;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;
import se.sensera.banking.utils.ListUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserServiceImpl implements UserService {
    private UsersRepository usersRepository;

    public UserServiceImpl(UsersRepository usersRepository) {

        this.usersRepository = usersRepository;
    }

    @java.lang.Override
    public User createUser(String name, String personalIdentificationNumber) throws UseException {
        if(usersRepository.all()
                .anyMatch(user1 -> user1.getPersonalIdentificationNumber().equals(personalIdentificationNumber)))
            throw new UseException(Activity.CREATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE);

        UserImpl user = new UserImpl(UUID.randomUUID().toString(), name, personalIdentificationNumber, true);
        return usersRepository.save(user);

    }

    @java.lang.Override
    public User changeUser(String userId, Consumer<ChangeUser> changeUser) throws UseException {
        User user;
        if (usersRepository.getEntityById(userId).isEmpty()) //Tittar först om det vi hämtar är tomt.
            throw new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND, "empty");
        else
            user = usersRepository.getEntityById(userId)
                .get();

        AtomicBoolean save = new AtomicBoolean(false);
        changeUser.accept(new ChangeUser() {
            @Override
            public void setName(String name) {
                user.setName(name);
                save.set(true);
            }

            @Override
            public void setPersonalIdentificationNumber(String personalIdentificationNumber) throws UseException {
                if (usersRepository.all()
                        .anyMatch(userToCheck -> userToCheck.getPersonalIdentificationNumber().equals(personalIdentificationNumber))){
                    throw new UseException(Activity.UPDATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE);
                }
                /*if (usersRepository.all()
                        .noneMatch(user1 -> user1.getPersonalIdentificationNumber().equals(personalIdentificationNumber))){
                    throw new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND);
                }*/
                user.setPersonalIdentificationNumber(personalIdentificationNumber);
                save.set(true);

            }
        });

        if(!save.get())
            return user;
        return usersRepository.save(user);


    }

    @java.lang.Override
    public User inactivateUser(String userId) throws UseException {
        User user= getUser(userId)
                .orElseThrow(() -> new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND));
        user.setActive(false);
        return usersRepository.save(user);
    }

    @java.lang.Override
    public Optional<User> getUser(String userId) {

        return usersRepository.getEntityById(userId);
    }

    @Override
    public Stream<User> find(String searchString, Integer pageNumber, Integer pageSize, SortOrder sortOrder) {

        if(sortOrder==SortOrder.PersonalId){
            return ListUtils.applyPage(usersRepository.all()
                    .sorted(Comparator.comparing(User::getPersonalIdentificationNumber)),pageNumber , pageSize);
        }

        if(pageNumber== null && pageNumber== null && !SortOrder.PersonalId.equals(sortOrder) && !SortOrder.Name.equals(sortOrder) && searchString==""){  // Dont show inactivated user
            return usersRepository.all().filter(User::isActive);
        }
            return ListUtils.applyPage(usersRepository.all()
                .filter(user -> user.getName().toLowerCase().contains(searchString)).sorted(Comparator.comparing(User::getName)),pageNumber ,pageSize);

//        if(usersRepository.all().allMatch(User::isActive))
//        return  usersRepository.all().filter(User::isActive);

    }
}
