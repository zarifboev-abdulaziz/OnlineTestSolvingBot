package uz.pdp.utils;

import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.model.SolveTestHistory;
import uz.pdp.model.Subject;
import uz.pdp.model.User;
import uz.pdp.model.enums.State;

import java.util.ArrayList;
import java.util.List;


public class DataBase {

    public static List<Subject> subjectList = new ArrayList<>();
    public static List<SolveTestHistory> testHistoryList = new ArrayList<>();
    public static List<User> userList = new ArrayList<>();

    public static User getUserFromList(Update update){

        if(update.hasMessage()){
            for (User user : userList) {
                if (user.getChatId().equals(update.getMessage().getChatId().toString())){
                    return user;
                }
            }
        } else if (update.hasCallbackQuery()){
            for (User user : userList) {
                if (user.getChatId().equals(update.getCallbackQuery().getMessage().getChatId().toString())){
                    return user;
                }
            }
        }


        User newUser = new User();
        newUser.setFirstName(update.getMessage().getFrom().getFirstName());
        newUser.setLastName(update.getMessage().getFrom().getLastName());
        newUser.setUsername(update.getMessage().getFrom().getUserName());
        newUser.setChatId(update.getMessage().getChatId().toString());
        newUser.setState(State.ADMIN_MENU);


        userList.add(newUser);
        return newUser;
    }

}
