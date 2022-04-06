package uz.pdp;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.pdp.utils.HardCodedData;

public class Main {
    public static void main(String[] args) {

        HardCodedData.getHardCodedData();


        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);

            api.registerBot(new MyBot());


        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}
