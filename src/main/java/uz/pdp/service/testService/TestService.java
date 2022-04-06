package uz.pdp.service.testService;

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
import uz.pdp.model.Answer;
import uz.pdp.model.Subject;
import uz.pdp.model.Test;
import uz.pdp.model.User;
import uz.pdp.model.enums.State;
import uz.pdp.utils.DataBase;

import java.util.ArrayList;
import java.util.List;

public class TestService {

    public void testCallBackQuery(User currentUser, CallbackQuery callbackQuery){
        switch (currentUser.getState()) {
            case TEST_MENU:
                menuTestCallBackQuery(currentUser, callbackQuery);
                break;
            case UPDATE_TEST:
                updateTestCallBackQuery(currentUser, callbackQuery);
                break;
            case DELETE_TEST:
                deleteTestCallBackQuery(currentUser, callbackQuery);
                break;
        }
    }

    private void deleteTestCallBackQuery(User currentUser, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        Test test = currentUser.getSelectedSubject().getTestList().get(Integer.parseInt(data));
        currentUser.getSelectedSubject().getTestList().remove(test);

        sendMessage(currentUser, "Successfully Deleted!", true);

        currentUser.setState(State.TEST_MENU);
        sendMessage(currentUser, "Please enter one option: ", true);
    }

    private void updateTestCallBackQuery(User currentUser, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        currentUser.setCurrentTestNumber(Integer.parseInt(data));

        sendMessage(currentUser, "Good job, Now Enter new question body for selected test!", true);
    }

    private void menuTestCallBackQuery(User currentUser, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        Subject subject = DataBase.subjectList.get(Integer.parseInt(data));

        currentUser.setSelectedSubject(subject);

        sendMessage(currentUser, "Good job, now let's choose one option below: ", true);
    }

    public void testMenu(User currentUser, Message message){
        String msg = message.getText();

        switch (msg){
            case "Show Tests":
                currentUser.setState(State.SHOW_TESTS);
                break;
            case "Add Test":
                currentUser.setState(State.ADD_TEST);
                break;
            case "Update Test":
                currentUser.setState(State.UPDATE_TEST);
                break;
            case "Delete Test":
                currentUser.setState(State.DELETE_TEST);
                break;
            case "Main Menu":
                currentUser.setSelectedSubject(null);
                currentUser.setState(State.ADMIN_MENU);
                sendMessage(currentUser, "Choose one option: ", true);

                break;

            default:break;
        }

        switch (currentUser.getState()) {
            case SHOW_TESTS:
                showTestsProcess(currentUser, message);
                break;
            case ADD_TEST:
                addTestProcess(currentUser, message);
                break;
            case UPDATE_TEST:
                updateTestProcess(currentUser, message);
                break;
            case DELETE_TEST:
                deleteTestProcess(currentUser, message);
                break;
        }

    }

    private void deleteTestProcess(User currentUser, Message message) {
        String msg = message.getText();

        if (msg.equals("Delete Test")){
            sendMessage(currentUser, "Please, select one test from list below to delete!", false);
        }

    }

    private void updateTestProcess(User currentUser, Message message) {
        String msg = message.getText();

        if (msg.equals("Update Test")){
            sendMessage(currentUser, "Please, select one test from list below to update!", false);

            return;
        }

        //For checking
        if (currentUser.getCurrentMessageId() == null){
            Subject selectedSubject = currentUser.getSelectedSubject();
            Test test = selectedSubject.getTestList().get(currentUser.getCurrentTestNumber());

            test.setQuestion(msg);

            for (int i = 0; i < test.getAnswerList().size(); i++) {
                Answer answer = test.getAnswerList().get(i);
                sendMessage(currentUser, "(Order - " + (i+1) +  ") " + answer.getBody(), true);

            }

            sendMessage(currentUser, "Please enter true option order: ", true);
            currentUser.setCurrentMessageId(1);

            return;
        }

        if (currentUser.getCurrentMessageId() == 1){
            Subject selectedSubject = currentUser.getSelectedSubject();
            Test test = selectedSubject.getTestList().get(currentUser.getCurrentTestNumber());

            String trueAnswer = "" + (Integer.parseInt(msg) - 1);

            test.setTrueAnswer(trueAnswer);

            sendMessage(currentUser, "Successfully Updated", true);


            currentUser.setState(State.TEST_MENU);
            sendMessage(currentUser, "Please enter one option: ", true);

            currentUser.setCurrentMessageId(null);
            currentUser.setCurrentTestNumber(0);
        }

    }

    private void addTestProcess(User currentUser, Message message) {
        String msg = message.getText();

        if (msg.equals("Add Test")){

            sendMessage(currentUser, "Enter question body with \"?\" character: ", true);
            return;
        }

        if (currentUser.getCurrentTestNumber() == 0){
            Test test = new Test(msg);
            currentUser.getSelectedSubject().getTestList().add(test);

            String text = "Now, you should send at least 3 choices as an initial value for test!\n" + "Please start sending choices: ";
            sendMessage(currentUser, text, true);

            currentUser.setCurrentTestNumber(1);
            return;
        }

        if (currentUser.getCurrentTestNumber() == 1){
            Test lastTest = currentUser.getSelectedSubject().getTestList().get(currentUser.getSelectedSubject().getTestList().size() - 1);

            if (lastTest.getAnswerList().size() < 2){
                lastTest.getAnswerList().add(new Answer(msg));

                return;
            } else {
                lastTest.getAnswerList().add(new Answer(msg));

                sendMessage(currentUser, "Good Job, now you should send Order of true answer! (1, 2 or 3)", true);

                currentUser.setCurrentTestNumber(2);

                return;
            }
        }

        if (currentUser.getCurrentTestNumber() == 2){
            Test lastTest = currentUser.getSelectedSubject().getTestList().get(currentUser.getSelectedSubject().getTestList().size() - 1);

            if (msg.equals("1") || msg.equals("2") || msg.equals("3")){
                lastTest.setTrueAnswer(String.valueOf(Integer.parseInt(msg) - 1));
            } else {

                sendMessage(currentUser, "Wrong order, Please send (1) or (2) or (3) ", true);
                return;
            }

            sendMessage(currentUser, "Congratulations, one Test Added to selected Subject!", true);

            currentUser.setCurrentTestNumber(0);
            currentUser.setState(State.TEST_MENU);
            sendMessage(currentUser, "Choose one option!", true);

        }

    }

    private void showTestsProcess(User currentUser, Message message) {
        String tests = "";

        for (int i = 0; i < currentUser.getSelectedSubject().getTestList().size(); i++) {
            Test test = currentUser.getSelectedSubject().getTestList().get(i);

            tests += "" + (i+1) + ". " + test.getQuestion() + "\n" +
                    "Correct answer: " + test.getAnswerList().get(Integer.parseInt(test.getTrueAnswer())) +
                    "  (Order - " + (Integer.parseInt(test.getTrueAnswer())+1) + ")" + "\n\n";

        }

        sendMessage(currentUser, tests, true);

        currentUser.setState(State.TEST_MENU);
        sendMessage(currentUser, "Choose one option: ", true);
    }

    public InlineKeyboardMarkup getInlineMarkup(User currentUser){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        switch (currentUser.getState()){

            case DELETE_TEST:
            case UPDATE_TEST: {
                for (int i = 0; i < currentUser.getSelectedSubject().getTestList().size(); i++) {
                    Test test = currentUser.getSelectedSubject().getTestList().get(i);

                    InlineKeyboardButton button = new InlineKeyboardButton();

                    button.setText((i+1) + ". " + test.getQuestion());
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
