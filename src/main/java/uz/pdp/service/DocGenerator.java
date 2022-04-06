package uz.pdp.service;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.MyBot;
import uz.pdp.model.SolveTestHistory;
import uz.pdp.model.User;
import uz.pdp.model.enums.Role;
import uz.pdp.utils.DataBase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DocGenerator {

    public File userResultAnalyzePdf(String userBuffer){
        File file = null;
        try (PdfWriter writer = new PdfWriter("src/main/resources/UserResultAnalyze.pdf")) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            pdfDocument.addNewPage();
            Document document = new Document(pdfDocument);

            Paragraph paragraph1 = new Paragraph(userBuffer);

            document.add(paragraph1);

            document.close();
            file = new File("src/main/resources/UserResultAnalyze.pdf");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public void createAndSendUserList(User currentUser){
        long count = DataBase.userList.stream().count();
        if (count == 0){
            sendMessage(currentUser, "No available users");
            return;
        }


        File file = new File("src/main/resources/UserList.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook();


        XSSFSheet spreadsheet = workbook.createSheet("Sheet1");

        XSSFRow row1= spreadsheet.createRow(0);
        XSSFCellStyle style = workbook.createCellStyle();
        style.setWrapText(true);


        row1.createCell(0).setCellValue("First Name");
        row1.createCell(1).setCellValue("Last Name");
        row1.createCell(2).setCellValue("Phone Number");
        row1.createCell(3).setCellValue("User Name");



        for (int i = 0; i < DataBase.userList.size(); i++) {
            User user = DataBase.userList.get(i);

            XSSFRow row= spreadsheet.createRow(i + 1);
            row.setRowStyle(style);

            String lastName = user.getLastName() == null ? "[empty]" : user.getLastName();
            String userName = user.getUsername() == null ? "[empty]" : user.getUsername();

            row.createCell(0).setCellValue(user.getFirstName());
            row.getCell(0).setCellStyle(style);
            row.createCell(1).setCellValue(lastName);
            row.getCell(1).setCellStyle(style);
            row.createCell(2).setCellValue(user.getPhoneNumber());
            row.getCell(2).setCellStyle(style);
            row.createCell(3).setCellValue(userName);
            row.getCell(3).setCellStyle(style);

            spreadsheet.autoSizeColumn(0);
            spreadsheet.autoSizeColumn(1);
            spreadsheet.autoSizeColumn(2);
            spreadsheet.autoSizeColumn(3);

        }

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        MyBot myBot = new MyBot();
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(currentUser.getChatId());
        sendDocument.setDocument(new InputFile(file));
        sendDocument.setCaption("User List");

        try {
            myBot.execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public void createAndSendTestHistory(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN){
            long count1 = DataBase.testHistoryList.stream().count();
            if (count1 == 0){
                sendMessage(currentUser, "No solved tests yet!!!");
                return;
            }
        } else {
            long count = DataBase.testHistoryList.stream().filter(solveTestHistory -> solveTestHistory.getUser().getId().equals(currentUser.getId())).count();
            if (count == 0){
                sendMessage(currentUser, "No solved tests yet!!!");
                return;
            }
        }


        File file = new File("src/main/resources/userTestSolveHistory.pdf");

        try (PdfWriter writer = new PdfWriter(file)) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            pdfDocument.setDefaultPageSize(PageSize.A3);
            pdfDocument.addNewPage();
            Document document = new Document(pdfDocument);

            Paragraph paragraph1 = new Paragraph("Solved Tests History");
            paragraph1.setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(paragraph1);


            document.add(getTableByRole(currentUser));
            document.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        MyBot myBot = new MyBot();
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(currentUser.getChatId());
        sendDocument.setDocument(new InputFile(file));
        sendDocument.setCaption("Solved Tests History!");

        try {
            myBot.execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private Table getTableByRole(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            float[] pointColWidth = {150F, 150F, 150F, 150F, 150F, 150F, 150F};
            Table table = new Table(pointColWidth);


            table.addCell("T/R");
            table.addCell("User");
            table.addCell("Subject");
            table.addCell("Percentage");
            table.addCell("Number Of Tests");
            table.addCell("Correct Answers");
            table.addCell("Date");

            for (int i = 0; i < DataBase.testHistoryList.size(); i++) {
                SolveTestHistory solveTestHistory = DataBase.testHistoryList.get(i);

                table.addCell(String.valueOf(i + 1));
                table.addCell(solveTestHistory.getUser().getFirstName());
                table.addCell(solveTestHistory.getSubject().getName());
                table.addCell(String.valueOf(solveTestHistory.getResultInPercentage()));
                table.addCell(String.valueOf(solveTestHistory.getNumberOfTests()));
                table.addCell(String.valueOf(solveTestHistory.getCorrectAnswers()));
                table.addCell(String.valueOf(solveTestHistory.getLocalDateTime()));

            }

            return table;

        } else {

            float[] pointColWidth = {150F, 150F, 150F, 150F, 150F, 150F};
            Table table = new Table(pointColWidth);


            table.addCell("T/R");
            table.addCell("Subject");
            table.addCell("Percentage");
            table.addCell("Number Of Tests");
            table.addCell("Correct Answers");
            table.addCell("Date");

            for (int i = 0; i < DataBase.testHistoryList.size(); i++) {
                SolveTestHistory solveTestHistory = DataBase.testHistoryList.get(i);
                if (solveTestHistory.getUser().getId().equals(currentUser.getId())){
                    table.addCell(String.valueOf(i + 1));
                    table.addCell(solveTestHistory.getSubject().getName());
                    table.addCell(String.valueOf(solveTestHistory.getResultInPercentage()));
                    table.addCell(String.valueOf(solveTestHistory.getNumberOfTests()));
                    table.addCell(String.valueOf(solveTestHistory.getCorrectAnswers()));
                    table.addCell(String.valueOf(solveTestHistory.getLocalDateTime()));
                }
            }

            return table;
        }
    }

    private void sendMessage(User currentUser, String text) {
        MyBot myBot = new MyBot();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(currentUser.getChatId());
        sendMessage.setText(text);

        try {
            myBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
