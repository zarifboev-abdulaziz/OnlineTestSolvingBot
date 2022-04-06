package uz.pdp.service.studentService;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.MyBot;
import uz.pdp.model.SolveTestHistory;
import uz.pdp.model.User;
import uz.pdp.utils.DataBase;

import java.io.File;
import java.time.LocalDateTime;

public class SendingResultToUser {

    public void sendAnalyzedResultPdf(File file, User currentUser){
        MyBot myBot = new MyBot();
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(currentUser.getChatId());
        sendDocument.setDocument(new InputFile(file));
        sendDocument.setCaption("You can analyze your results through this file!");

        try {
            myBot.execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String userScore(User currentUser,float result){
        String data = "======================== Result ========================\n";

        if (result > 90){
            data += "Awesome!" + "\n";
        } else if (result > 70){
            data += "Congratulations!" + "\n";
        } else if (result > 50){
            data += "Not bad! work on yourself" + "\n";
        } else {
            data += "You should try more!" + "\n";
        }


        data += "Your selected subject: " + currentUser.getSelectedSubject().getName() + "\n"  +
                "Your result: " + result + "%\n"  +
                "True answers: " + currentUser.getCorrectAnswers() + " out of " + currentUser.getSelectedSubject().getTestList().size() + "\n";

        return data;
    }

    public void recordToHistory(User currentUser, float result){

        SolveTestHistory solveTestHistory = new SolveTestHistory();

        solveTestHistory.setUser(currentUser);
        solveTestHistory.setSubject(currentUser.getSelectedSubject());
        solveTestHistory.setResultInPercentage(result);
        solveTestHistory.setNumberOfTests(currentUser.getSelectedSubject().getTestList().size());
        solveTestHistory.setCorrectAnswers(currentUser.getCorrectAnswers());
        solveTestHistory.setLocalDateTime(LocalDateTime.now());

        DataBase.testHistoryList.add(solveTestHistory);

        System.out.println("Successful record!");
    }

}
