import java.util.ArrayList;
import java.util.List;

public class UserList {

    private List<User> list;

    public UserList(){
        list = new ArrayList<User>();
    }

    public void add(User u){
        list.add(u);
    }

    public int size(){
        return list.size();
    }

    public List<User> getUsers(){
        return list;
    }
}
