package uz.pdp.service.studentService;

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
import uz.pdp.service.DocGenerator;
import uz.pdp.utils.DataBase;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SolveTest {
    DocGenerator docGenerator = new DocGenerator();
    SendingResultToUser resultService = new SendingResultToUser();

    public void isStartingTest(User currentUser, Message message){
        MyBot myBot = new MyBot();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(currentUser.getChatId());

            if (message.getText().equals("Back")) {
                currentUser.setState(State.SOLVE_TESTS);
                sendMessage(currentUser, "Choose one Subject to solve: ", false);

            } else if (message.getText().equals("Start Test")) {

                String buffer ="====================== My Profile ======================\n" +
                        "Name: " + currentUser.getFirstName() + " \n" +
                        "Selected Subject: " + currentUser.getSelectedSubject().getName() + "\n" +
                        "Number of tests: " + currentUser.getSelectedSubject().getTestList().size() + "\n" +
                        "Maximum score in percentage: 100%" + "\n" +
                        "Given minutes to solve: " + (currentUser.getSelectedSubject().getTestList().size()) + " minutes\n" +
                        "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("")) + "\n\n";

                currentUser.setBuffer(buffer);
                currentUser.setState(State.TEST_STARTED);
                Test test = currentUser.getSelectedSubject().getTestList().get(currentUser.getCurrentTestNumber());

                sendMessage.setText((currentUser.getCurrentTestNumber()+1) + ". " + test.getQuestion());
                sendMessage.setReplyMarkup(getInlineMarkup(currentUser));

                try {
                    currentUser.setCurrentMessageId(myBot.execute(sendMessage).getMessageId());
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

    }

    public void responseToCallBackQuery(User currentUser, CallbackQuery callbackQuery){
        MyBot myBot = new MyBot();
        switch (currentUser.getState()){
            case SOLVE_TESTS: {
                String data = callbackQuery.getData();
                Subject subject = DataBase.subjectList.get(Integer.parseInt(data));
                currentUser.setSelectedSubject(subject);

                String text = "Selected Subject: " + subject.getName() + "\n" +
                        "Number of tests: " + subject.getTestList().size() + "\n" +
                        "Maximum score in percentage: 100%" + "\n" +
                        "Given minutes to solve: " + (subject.getTestList().size()) + " minutes\n";

                sendMessage(currentUser, text, true);
            }break;
            case TEST_STARTED:
                passingTestsOneByOne(currentUser, callbackQuery);
            break;
        }

    }

    public void passingTestsOneByOne(User currentUser, CallbackQuery callbackQuery){
        MyBot myBot = new MyBot();
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(currentUser.getChatId());
        editMessageText.setMessageId(currentUser.getCurrentMessageId());

        String data = callbackQuery.getData();
        Test previousTest = currentUser.getSelectedSubject().getTestList().get(currentUser.getCurrentTestNumber());

        writeToUserBuffer(currentUser, previousTest, data);

        if (previousTest.getTrueAnswer().equals(data)){
            currentUser.setCorrectAnswers(currentUser.getCorrectAnswers() + 1);
        }

        if ((currentUser.getCurrentTestNumber() + 1) == currentUser.getSelectedSubject().getTestList().size()){
            userFinishedTest(currentUser);
            return;
        }

        currentUser.setCurrentTestNumber(currentUser.getCurrentTestNumber() + 1);
        Test currentTest = currentUser.getSelectedSubject().getTestList().get(currentUser.getCurrentTestNumber());

        editMessageText.setText((currentUser.getCurrentTestNumber()+1) + ". " + currentTest.getQuestion());
        editMessageText.setReplyMarkup(getInlineMarkup(currentUser));

        try {
            myBot.execute(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void userFinishedTest(User currentUser) {
        
        float result = (float) currentUser.getCorrectAnswers() / currentUser.getSelectedSubject().getTestList().size() * 100;

        String userScore = resultService.userScore(currentUser, result);
        sendMessage(currentUser, userScore, true);
        currentUser.setBuffer(currentUser.getBuffer() + userScore);
        
        File pdfFile = docGenerator.userResultAnalyzePdf(currentUser.getBuffer());
        resultService.sendAnalyzedResultPdf(pdfFile, currentUser);
        resultService.recordToHistory(currentUser, result);

        currentUser.setBuffer("");
        currentUser.setCurrentTestNumber(0);
        currentUser.setCorrectAnswers(0);
        currentUser.setSelectedSubject(null);
        currentUser.setState(State.MAIN_MENU);
        sendMessage(currentUser, "Choose one option: ", true);
    }


    private void writeToUserBuffer(User currentUser, Test previousTest, String data) {
        String buffer = "=========================================\n";
        Answer trueAnswer = previousTest.getAnswerList().get(Integer.parseInt(previousTest.getTrueAnswer()));
        Answer yourAnswer = previousTest.getAnswerList().get(Integer.parseInt(data));

        buffer += (currentUser.getCurrentTestNumber() + 1) +". " + previousTest.getQuestion() + "\n";
        buffer += "True answer: " + trueAnswer.getBody() + "\n";
        buffer += "Your selected Answer: " + yourAnswer.getBody() + "\n\n";

        currentUser.setBuffer(currentUser.getBuffer() + buffer);

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

