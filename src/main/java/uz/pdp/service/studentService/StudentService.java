package uz.pdp.service.studentService;

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
import uz.pdp.model.Answer;
import uz.pdp.model.Subject;
import uz.pdp.model.Test;
import uz.pdp.model.User;
import uz.pdp.model.enums.State;
import uz.pdp.service.DocGenerator;
import uz.pdp.utils.DataBase;

import java.util.ArrayList;
import java.util.List;

public class StudentService {
    DocGenerator docGenerator = new DocGenerator();
    SolveTest solveTest = new SolveTest();

    public void studentMenu(Update update, User currentUser){

        if (update.hasMessage()){
            process(currentUser, update);
        } else if (update.hasCallbackQuery()){
            solveTest.responseToCallBackQuery(currentUser, update.getCallbackQuery());
        }

    }



    private void process(User currentUser,Update update) {
        String msg = "";

        if (update.hasMessage() && update.getMessage().hasText()){
            msg = update.getMessage().getText();
        }

        if (msg.equals("/start") || update.getMessage().hasContact()){
            String text = "Assalomu Alaykum, Botga hush kelibsiz";
            currentUser.setState(State.MAIN_MENU);
            sendMessage(currentUser, text, true);
            return;
        }

        switch (currentUser.getState()) {
            case MAIN_MENU:
                mainMenuProcess(currentUser, update.getMessage());
                break;
            case SOLVE_TESTS:

            solveTest.isStartingTest(currentUser, update.getMessage());

            break;
        }
    }


    private void mainMenuProcess(User currentUser, Message message) {
        String msg = message.getText();

        if (msg.equals("Solve Tests")){
            currentUser.setState(State.SOLVE_TESTS);
            sendMessage(currentUser, "Choose one Subject to solve", false);

        } else if (msg.equals("Solved Tests History")){
            docGenerator.createAndSendTestHistory(currentUser);

            currentUser.setState(State.MAIN_MENU);
            sendMessage(currentUser, "Choose one option!", true);

        } else {
            sendMessage(currentUser, "Wrong command!", true);
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

        try {
            myBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public InlineKeyboardMarkup getInlineMarkup(User currentUser){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        switch (currentUser.getState()){
            case MAIN_MENU:break;
            case SOLVE_TESTS: {
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

            case TEST_STARTED:{
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
            case MAIN_MENU:
                row1.add("Solve Tests");
                row2.add("Solved Tests History");
                rowList.add(row1);
                rowList.add(row2);
                break;
            case SOLVE_TESTS:
                row1.add("Back");
                row1.add("Start Test");
                rowList.add(row1);
                break;
        }

        return keyboardMarkup;
    }

}
