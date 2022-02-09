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
    String id;
    User owner;
    String name;
    boolean active;

    public AccountImpl(User owner, String name, String id, boolean active) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.active = active;
    }

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
