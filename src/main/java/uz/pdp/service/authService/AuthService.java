package uz.pdp.service.authService;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.MyBot;
import uz.pdp.model.User;
import uz.pdp.model.enums.Role;

import java.util.ArrayList;
import java.util.List;

import static uz.pdp.utils.Constants.adminPhone;

public class AuthService {
    public User registerProcess(User currentUser, Update update){
        if (update.hasMessage() && update.getMessage().hasText() && !update.getMessage().getText().equals("/start")){
            return currentUser;
        }
        if (update.hasCallbackQuery()){
            return currentUser;
        }

        if (currentUser.getPhoneNumber() == null && !update.getMessage().hasContact() && update.getMessage().getText().equals("/start")){
            sendMessage(currentUser, "Please share your contact in order to use our facilities!");
        }

        if (update.getMessage().hasContact()){
            Contact contact = update.getMessage().getContact();
            currentUser.setPhoneNumber(contact.getPhoneNumber());

            if (contact.getPhoneNumber().equals(adminPhone)){
                currentUser.setRole(Role.ADMIN);
            }
            return currentUser;
        }

        return currentUser;
    }

    private void sendMessage(User currentUser, String text) {
        MyBot myBot = new MyBot();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(currentUser.getChatId());
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(getReplyKeyBoard(currentUser));


        try {
            myBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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

        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText("Share Contact");
        keyboardButton.setRequestContact(true);
        row1.add(keyboardButton);
        rowList.add(row1);
        return keyboardMarkup;
    }
}
