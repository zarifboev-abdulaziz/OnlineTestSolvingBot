package uz.pdp.service.answerService;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
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

public class AnswerService {

    public void answerCallBackQuery(User currentUser, CallbackQuery callbackQuery){
        switch (currentUser.getState()) {
            case ANSWER_MENU:
                menuAnswerCallBackQuery(currentUser, callbackQuery);
                break;
            case UPDATE_ANSWER:
                updateAnswerCallBackQuery(currentUser, callbackQuery);
                break;
            case DELETE_ANSWER:
                deleteAnswerCallBackQuery(currentUser, callbackQuery);
                break;
        }
    }

    private void deleteAnswerCallBackQuery(User currentUser, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        Test test = currentUser.getSelectedSubject().getTestList().get(currentUser.getCurrentTestNumber());
        test.getAnswerList().remove(Integer.parseInt(data));

        currentUser.setState(State.ANSWER_MENU);
        sendMessage(currentUser, "Successfully deleted! \nChoose one option ", true);

    }

    private void updateAnswerCallBackQuery(User currentUser, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        currentUser.setCorrectAnswers(Integer.parseInt(data));
        sendMessage(currentUser, "Enter new body for selected Answer: ", true);

    }

    private void menuAnswerCallBackQuery(User currentUser, CallbackQuery callbackQuery) {
        MyBot myBot = new MyBot();

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(currentUser.getChatId());
        editMessageText.setMessageId(currentUser.getCurrentMessageId());

        String data = callbackQuery.getData();

        if (currentUser.getSelectedSubject() == null){
            Subject subject = DataBase.subjectList.get(Integer.parseInt(data));

            currentUser.setSelectedSubject(subject);

            editMessageText.setText("Ok! now, you should select one of the tests to go Answer menu!");
            editMessageText.setReplyMarkup(getInlineMarkup(currentUser));

            try {
                myBot.execute(editMessageText);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            currentUser.setCurrentTestNumber(Integer.parseInt(data));
            sendMessage(currentUser, "Good job, now you can select one of the options below!", true);
        }

    }

    public void answerMenu(User currentUser, Message message){
        String msg = message.getText();

        switch (msg){
            case "Show Answers":
                currentUser.setState(State.SHOW_ANSWERS);
                break;
            case "Add Answer":
                currentUser.setState(State.ADD_ANSWER);
                break;
            case "Update Answer":
                currentUser.setState(State.UPDATE_ANSWER);
                break;
            case "Delete Answer":
                currentUser.setState(State.DELETE_ANSWER);
                break;
            case "Main Menu":
                currentUser.setSelectedSubject(null);
                currentUser.setCurrentTestNumber(0);
                currentUser.setState(State.ADMIN_MENU);
                sendMessage(currentUser, "Choose one option: ", true);

                break;

            default:break;
        }

        switch (currentUser.getState()) {
            case SHOW_ANSWERS:
                showAnswersProcess(currentUser, message);
                break;
            case ADD_ANSWER:
                addAnswerProcess(currentUser, message);
                break;
            case UPDATE_ANSWER:
                updateAnswerProcess(currentUser, message);
                break;
            case DELETE_ANSWER:
                deleteAnswerProcess(currentUser, message);
                break;
        }

    }

    private void deleteAnswerProcess(User currentUser, Message message) {

        sendMessage(currentUser, "Select one Answer to delete: ", false);

    }

    private void updateAnswerProcess(User currentUser, Message message) {
        String text = message.getText();

        if (text.equals("Update Answer")){
            sendMessage(currentUser, "Select one Answer to update: ", false);

        } else {
            Test test = currentUser.getSelectedSubject().getTestList().get(currentUser.getCurrentTestNumber());
            Answer answer = test.getAnswerList().get(currentUser.getCorrectAnswers());

            answer.setBody(text);
            currentUser.setCorrectAnswers(0);

            currentUser.setState(State.ANSWER_MENU);
            sendMessage(currentUser, "Successfully updated! \nChoose one option", true);

        }
    }

    private void addAnswerProcess(User currentUser, Message message) {

        if (message.getText().equals("Add Answer")){
            sendMessage(currentUser, "Enter body of answer: ", true);

            return;
        }

        if (currentUser.getCorrectAnswers() == 0){
            Test test = currentUser.getSelectedSubject().getTestList().get(currentUser.getCurrentTestNumber());

            test.getAnswerList().add(new Answer(message.getText()));

            currentUser.setState(State.ANSWER_MENU);
            sendMessage(currentUser, "Successfully added!!! \nChoose one option!!!", true);

        }
    }

    private void showAnswersProcess(User currentUser, Message message) {
        Test test = currentUser.getSelectedSubject().getTestList().get(currentUser.getCurrentTestNumber());

        String buffer = test.getQuestion() + "\n";

        for (int i = 0; i < test.getAnswerList().size(); i++) {
            Answer answer = test.getAnswerList().get(i);

            buffer += answer.getBody() + " \n";
        }

        sendMessage(currentUser, buffer, true);

        currentUser.setState(State.ANSWER_MENU);
        sendMessage(currentUser, "Choose one option: ", true);

    }

    public InlineKeyboardMarkup getInlineMarkup(User currentUser){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        switch (currentUser.getState()){

            case ANSWER_MENU:{
                for (int i = 0; i < currentUser.getSelectedSubject().getTestList().size(); i++) {
                    Test test = currentUser.getSelectedSubject().getTestList().get(i);

                    InlineKeyboardButton button = new InlineKeyboardButton();

                    button.setText((i+1) + ". " + test.getQuestion());
                    button.setCallbackData(String.valueOf(i));

                    List<InlineKeyboardButton> row = new ArrayList<>();
                    row.add(button);
                    rowList.add(row);
                }
            }break;

            case DELETE_ANSWER:
            case UPDATE_ANSWER: {
                Test test = currentUser.getSelectedSubject().getTestList().get(currentUser.getCurrentTestNumber());

                for (int i = 0; i < test.getAnswerList().size(); i++) {
                    Answer answer = test.getAnswerList().get(i);
                    InlineKeyboardButton button = new InlineKeyboardButton();

                    button.setText(answer.getBody());
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
