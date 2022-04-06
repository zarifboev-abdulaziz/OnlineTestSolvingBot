package uz.pdp.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor

public class Answer {
    int id = (int)(Math.random()*10000);
    String body;

    public Answer(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return body;
    }
}
