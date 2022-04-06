package uz.pdp.utils;

import uz.pdp.model.Answer;
import uz.pdp.model.Subject;
import uz.pdp.model.Test;


public class HardCodedData {



    public static void getHardCodedData(){
        Subject subject1 = new Subject("Math");
        Test test1 = new Test("Choose Natural Number?");
        Answer answer1 = new Answer("1, 2, 3, 4 ...");
        Answer answer2 = new Answer("1.1, 2.1, 3.1, 4.1 ...");
        Answer answer3 = new Answer("-1, -2, -3, -4 ...");
        Answer answer4 = new Answer("1/1, 1/2, 1/3, 1/4 ...");
        test1.setTrueAnswer("0");

        test1.getAnswerList().add(answer1);
        test1.getAnswerList().add(answer2);
        test1.getAnswerList().add(answer3);
        test1.getAnswerList().add(answer4);

        //Test 2
        Test test2 = new Test("Choose Decimal Number?");
        Answer answer5 = new Answer("1, 2, 3, 4 ...");
        Answer answer6 = new Answer("1.1, 2.1, 3.1, 4.1 ...");
        Answer answer7 = new Answer("-1, -2, -3, -4 ...");
        Answer answer8 = new Answer("1/1, 1/2, 1/3, 1/4 ...");
        test2.setTrueAnswer("1");

        test2.getAnswerList().add(answer5);
        test2.getAnswerList().add(answer6);
        test2.getAnswerList().add(answer7);
        test2.getAnswerList().add(answer8);

        subject1.getTestList().add(test1);
        subject1.getTestList().add(test2);


        Subject subject2 = new Subject("English");
        Test test3 = new Test("What is the Translation of Book?");
        Answer answer9 = new Answer("Daftar");
        Answer answer10 = new Answer("Kitob");
        Answer answer11 = new Answer("Jurnal");
        Answer answer12 = new Answer("Ruchka");
        test3.setTrueAnswer("1");

        test3.getAnswerList().add(answer9);
        test3.getAnswerList().add(answer10);
        test3.getAnswerList().add(answer11);
        test3.getAnswerList().add(answer12);

        //Test 2
        Test test4 = new Test("What is the Translation of Pen?");
        Answer answer13 = new Answer("Qalam");       //0
        Answer answer14 = new Answer("o'chirg'ich"); //1
        Answer answer15 = new Answer("ruchka");      //2
        Answer answer16 = new Answer("lineyka");     //3
        test4.setTrueAnswer("2");

        test4.getAnswerList().add(answer13);
        test4.getAnswerList().add(answer14);
        test4.getAnswerList().add(answer15);
        test4.getAnswerList().add(answer16);

        subject2.getTestList().add(test3);
        subject2.getTestList().add(test4);






        DataBase.subjectList.add(subject1);
        DataBase.subjectList.add(subject2);


    }

}
