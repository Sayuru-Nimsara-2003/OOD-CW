public class User {             // Instances of this will be created when a user is logged in or registered
    private String userName;
    private String password;


    public void setUserName(String userName){
        this.userName = userName;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getUserName(){return userName;}
    public String getPassword(){return password;}
}
