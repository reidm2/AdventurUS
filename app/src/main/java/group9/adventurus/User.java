/* CSCI 4176: Mobile Computing Group Project
Group #9

This defines a User class, which is stored in Firebase (Name, Email, and Password parameters)
 */
package group9.adventurus;

public class User {
    public String username, email, password;

    //Constructor to Initialize the values

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
