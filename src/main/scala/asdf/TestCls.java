package asdf;

import asdf1.User;

import java.beans.BeanProperty;
import java.util.Collection;

public class TestCls {
    String name;
    Collection<User> users;
    User user;
    long id;
    int age;
    double dec;

    public TestCls(Collection<User> users, String name) {
        this.users = users;
        this.name = name;
    }

    public void setUsers(Collection<User> users) {
        this.users = users;
    }

    public Collection<User> getUsers() {
        return users;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "id: "+id+" age: "+age+" dec: "+dec+" name: " + name + " user: " + user;
    }
}
