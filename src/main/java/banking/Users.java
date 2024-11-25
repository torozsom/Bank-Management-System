package banking;

import java.util.ArrayList;
import java.util.List;


public class Users {

    private final List<User> users;


    public Users() {
        users = new ArrayList<>();
    }

    public Users(List<User> users) {
        this.users = users;
    }


    public User getUser(int idx) {
        return users.get(idx);
    }

    public User getFirstUser() {
        return users.getFirst();
    }

    public User getLastUser() {
        return users.getLast();
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public void eraseUsers() {
        users.clear();
    }

    public void addUsers(List<User> users) {
        this.users.addAll(users);
    }

    public void removeUsers(List<User> users) {
        this.users.removeAll(users);
    }

}
