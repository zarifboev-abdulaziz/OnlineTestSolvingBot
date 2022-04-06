package uz.pdp.model;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class Subject {
    int id = (int)(Math.random()*10000);
    String name;
    List<Test> testList = new ArrayList();

    public Subject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Test> getTestList() {
        return testList;
    }

    public void setTestList(List<Test> testList) {
        this.testList = testList;
    }

    @Override
    public String toString() {
        return name;
    }
}
