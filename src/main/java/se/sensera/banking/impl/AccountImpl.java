package se.sensera.banking.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import se.sensera.banking.Account;
import se.sensera.banking.User;

import java.util.stream.Stream;


@AllArgsConstructor
@Getter
@Setter
public class AccountImpl implements Account {
    User owner;
    String name;
    String id;
    boolean active;

    @Override
    public Stream<User> getUsers() {
        return Stream.empty();  //Create_account_success collect to list
    }

    @Override
    public void addUser(User user) {

    }

    @Override
    public void removeUser(User user) {

    }
}
