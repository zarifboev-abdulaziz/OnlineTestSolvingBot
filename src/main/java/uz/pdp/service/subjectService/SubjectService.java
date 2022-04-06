package uz.pdp.service.subjectService;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.MyBot;
import uz.pdp.model.Subject;
import uz.pdp.model.User;
import uz.pdp.model.enums.State;
import uz.pdp.utils.DataBase;

import java.util.ArrayList;
import java.util.List;

public class SubjectService {

    public void subjectCallBackQuery(User currentUser, CallbackQuery callbackQuery){
        switch (currentUser.getState()) {
            case UPDATE_SUBJECT:
                updateSubjectCallBackQuery(currentUser, callbackQuery);
                break;
            case DELETE_SUBJECT:
                deleteSubjectCallBackQuery(currentUser, callbackQuery);
                break;
        }

    }

    private void deleteSubjectCallBackQuery(User currentUser, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        DataBase.subjectList.remove(Integer.parseInt(data));

        currentUser.setState(State.SUBJECT_MENU);
        sendMessage(currentUser, "Successfully deleted! \nChoose one option ", true);

    }

    private void updateSubjectCallBackQuery(User currentUser, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        Subject subject = DataBase.subjectList.get(Integer.parseInt(data));

        currentUser.setSelectedSubject(subject);

        sendMessage(currentUser, "Enter new name for selected Subject: ", true);
    }

    public void subjectMenu(User currentUser, Message message){
        String msg = message.getText();

        switch (msg){
            case "Show Subjects":
                currentUser.setState(State.SHOW_SUBJECTS);
            break;
            case "Add Subject":
                currentUser.setState(State.ADD_SUBJECT);
                break;
            case "Update Subject":
                currentUser.setState(State.UPDATE_SUBJECT);
                break;
            case "Delete Subject":
                currentUser.setState(State.DELETE_SUBJECT);
                break;
            case "Main Menu":
                currentUser.setState(State.ADMIN_MENU);
                sendMessage(currentUser, "Choose one option: ", true);

                break;

            default:break;
        }

        switch (currentUser.getState()) {
            case SHOW_SUBJECTS:
                showSubjectsProcess(currentUser, message);
                break;
            case ADD_SUBJECT:
                addSubjectProcess(currentUser, message);
                break;
            case UPDATE_SUBJECT:
                updateSubjectProcess(currentUser, message);
                break;
            case DELETE_SUBJECT:
                deleteSubjectProcess(currentUser, message);
                break;
        }

    }

    private void deleteSubjectProcess(User currentUser, Message message) {
        sendMessage(currentUser, "Select subject to delete: ", false);

    }

    private void updateSubjectProcess(User currentUser, Message message) {
        String text = message.getText();

        if (text.equals("Update Subject")){
            sendMessage(currentUser, "Select subject to update: ", false);

        } else {
            currentUser.getSelectedSubject().setName(text);
            currentUser.setSelectedSubject(null);
            currentUser.setState(State.SUBJECT_MENU);
            sendMessage(currentUser, "Successfully updated! \nChoose one option", true);
        }
    }

    private void addSubjectProcess(User currentUser, Message message) {

        if (message.getText().equals("Add Subject")){
            sendMessage(currentUser, "Enter name of the Subject: ", true);
            return;
        }

        if (currentUser.getSelectedSubject() == null){

            for (Subject subject : DataBase.subjectList) {
                if (subject.getName().equals(message.getText())){
                    sendMessage(currentUser, "This Subject already exists. \nPlease enter different name", true);

                    return;
                }
            }

            DataBase.subjectList.add(new Subject(message.getText()));

            currentUser.setState(State.SUBJECT_MENU);
            sendMessage(currentUser, "Successfully added!!! \nChoose one option!!!", true);

        }

    }

    private void showSubjectsProcess(User currentUser, Message message) {
        String buffer = "";

        for (int i = 0; i < DataBase.subjectList.size(); i++) {
            Subject subject = DataBase.subjectList.get(i);
            buffer += "" + (i+1) + ". " + subject.getName() + " \n";
        }

        sendMessage(currentUser, buffer, true);


        currentUser.setState(State.SUBJECT_MENU);
        sendMessage(currentUser, "Choose one option: ", true);

    }

    public InlineKeyboardMarkup getInlineMarkup(User currentUser){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        switch (currentUser.getState()){
            case UPDATE_SUBJECT:

            case DELETE_SUBJECT: {
                for (int i = 0; i < DataBase.subjectList.size(); i++) {
                    Subject subject = DataBase.subjectList.get(i);

                    InlineKeyboardButton button = new InlineKeyboardButton();

                    button.setText(subject.getName());
                    button.setCallbackData(String.valueOf(i));

                    List<InlineKeyboardButton> row = new ArrayList<>();
                    row.add(button);
                    rowList.add(row);
                }
            } break;

        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private ReplyKeyboard getReplyKeyBoard(User currentUser) {

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> rowList = new ArrayList<>();
        keyboardMarkup.setKeyboard(rowList);
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow rowN = new KeyboardRow();

        State state = currentUser.getState();

        switch (state) {
            case ADMIN_MENU: {
                row1.add("Show User List");
                row1.add("Solved Tests History");
                row2.add("Subject Menu");
                row2.add("Test Menu");
                row2.add("Answer Menu");
                rowList.add(row1);
                rowList.add(row2);
            }break;

            case SUBJECT_MENU:{
                row1.add("Show Subjects");
                row1.add("Add Subject");
                row2.add("Update Subject");
                row2.add("Delete Subject");
                rowN.add("Main Menu");

                rowList.add(row1);
                rowList.add(row2);
                rowList.add(rowN);
            }break;

        }

        return keyboardMarkup;
    }

    private void sendMessage(User currentUser, String text, boolean isReplyMarkup) {
        MyBot myBot = new MyBot();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(currentUser.getChatId());
        sendMessage.setText(text);

        if (isReplyMarkup){
            sendMessage.setReplyMarkup(getReplyKeyBoard(currentUser));
        } else {
            sendMessage.setReplyMarkup(getInlineMarkup(currentUser));
        }

        try {
            myBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
