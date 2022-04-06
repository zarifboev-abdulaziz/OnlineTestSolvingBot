package uz.pdp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SolveTestHistory {

    private int id = (int)(Math.random()*10000);
    private User user;
    private Subject subject;
    private double resultInPercentage;
    private int numberOfTests;
    private int correctAnswers;
    private LocalDateTime localDateTime;

    public SolveTestHistory(User user, Subject subject, double resultInPercentage, int numberOfTests, int correctAnswers, LocalDateTime localDateTime) {
        this.user = user;
        this.subject = subject;
        this.resultInPercentage = resultInPercentage;
        this.numberOfTests = numberOfTests;
        this.correctAnswers = correctAnswers;
        this.localDateTime = localDateTime;
    }
}
