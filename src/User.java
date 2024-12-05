public class User {             // Instances of this will be created when a user is logged in or registered
    private String userName;
    private String password;
    private int userId;

    public void setUserName(String userName){
        this.userName = userName;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getUserName(){return userName;}
    public String getPassword(){return password;}

    public void setUserId(int userId){
        this.userId = userId;
    }

    public int getUserId(){
        return userId;
    }
}
