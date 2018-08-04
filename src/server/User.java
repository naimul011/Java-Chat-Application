
package server;


public class User {
    String username;
    String password;

    public User(String username,String password) {
        this.username = username;
        this.password = password;
    }
    
    public String getUserName(){
        return username;
    }
    
    public String getPassword(){
        return password;
    }
}
