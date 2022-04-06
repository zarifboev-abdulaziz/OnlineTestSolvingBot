package uz.pdp.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
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
import uz.pdp.service.answerService.AnswerService;
import uz.pdp.service.subjectService.SubjectService;
import uz.pdp.service.testService.TestService;
import uz.pdp.utils.DataBase;

import java.util.ArrayList;
import java.util.List;

public class AdminService {
    SubjectService subjectService = new SubjectService();
    TestService testService = new TestService();
    AnswerService answerService = new AnswerService();


    public void adminMenu(Update update, User currentUser){

        if (update.hasMessage()){
            adminProcess(currentUser, update.getMessage());
        } else if (update.hasCallbackQuery()){
            callBackQueryProcess(currentUser, update);
        }

    }

    private void callBackQueryProcess(User currentUser, Update update) {
        switch (currentUser.getState()) {
            case UPDATE_SUBJECT:
            case DELETE_SUBJECT:
            case SUBJECT_MENU:
                subjectService.subjectCallBackQuery(currentUser, update.getCallbackQuery());
                break;

            case ADD_TEST:
            case SHOW_TESTS:
            case UPDATE_TEST:
            case DELETE_TEST:
            case TEST_MENU:
                testService.testCallBackQuery(currentUser, update.getCallbackQuery());
                break;

            case ADD_ANSWER:
            case SHOW_ANSWERS:
            case DELETE_ANSWER:
            case UPDATE_ANSWER:
            case ANSWER_MENU:
                answerService.answerCallBackQuery(currentUser, update.getCallbackQuery());
                break;


        }

    }

    private void adminProcess(User currentUser,Message message) {

        if (message.hasContact() || message.getText().equals("/start")){
            currentUser.setState(State.ADMIN_MENU);
            sendMessage(currentUser, "Assalomu Alaykum admin, Botga hush kelibsiz", true);
            return;
        }

        switch (currentUser.getState()) {
            case ADMIN_MENU:
                adminMenuProcess(currentUser, message);
                break;

            case ADD_SUBJECT:
            case DELETE_SUBJECT:
            case UPDATE_SUBJECT:
            case SHOW_SUBJECTS:
            case SUBJECT_MENU:
                subjectService.subjectMenu(currentUser, message);
                break;

            case ADD_TEST:
            case SHOW_TESTS:
            case UPDATE_TEST:
            case DELETE_TEST:
            case TEST_MENU:
                testService.testMenu(currentUser, message);
                break;

            case ADD_ANSWER:
            case SHOW_ANSWERS:
            case DELETE_ANSWER:
            case UPDATE_ANSWER:
            case ANSWER_MENU:
                answerService.answerMenu(currentUser, message);
                break;
        }
    }

    private void adminMenuProcess(User currentUser, Message message) {
        DocGenerator docGenerator = new DocGenerator();

        switch (message.getText()) {

            case "Show User List":{
                docGenerator.createAndSendUserList(currentUser);
                sendMessage(currentUser, "Choose one option", true);
            }break;

            case "Solved Tests History":{
                docGenerator.createAndSendTestHistory(currentUser);
                sendMessage(currentUser, "Choose one option", true);
            }break;

            case "Subject Menu": {
                currentUser.setState(State.SUBJECT_MENU);
                sendMessage(currentUser, "Choose one option: ", true);

            } break;

            case "Test Menu": {
                currentUser.setState(State.TEST_MENU);
                sendMessage(currentUser, "Please select one subject in order to go Test Menu", false);

            } break;
            case "Answer Menu": {
                currentUser.setState(State.ANSWER_MENU);
                sendMessage(currentUser, "Please select one subject in order to go Answer Menu", false);

            }break;

            default: break;
        }
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

        if (currentUser.getState() == State.ANSWER_MENU){
            try {
                currentUser.setCurrentMessageId(myBot.execute(sendMessage).getMessageId());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            try {
                myBot.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }

    public InlineKeyboardMarkup getInlineMarkup(User currentUser){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        switch (currentUser.getState()){

            case ANSWER_MENU:
            case TEST_MENU: {
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

            case TEST_MENU:{
                row1.add("Show Tests");
                row1.add("Add Test");
                row2.add("Update Test");
                row2.add("Delete Test");
                rowN.add("Main Menu");

                rowList.add(row1);
                rowList.add(row2);
                rowList.add(rowN);
            }break;

            case ANSWER_MENU:{
                row1.add("Show Answers");
                row1.add("Add Answer");
                row2.add("Update Answer");
                row2.add("Delete Answer");
                rowN.add("Main Menu");

                rowList.add(row1);
                rowList.add(row2);
                rowList.add(rowN);
            }break;
        }

        return keyboardMarkup;
    }




}
