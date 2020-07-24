package vk;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import date.User;
import date.Word;
import org.apache.logging.log4j.core.util.JsonUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class VkServer {

    public static VkCore vkCore;
    public static List<Word> allWords = Arrays.asList(
            new Word("Lion","лев"),
            new Word("Car","машина"),
            new Word("Yellow","желтый"),
            new Word("Home","дом") ); /*Слова, которые будут использоваться*/
    public static HashMap<Integer, User> userData = new HashMap<>();
    static {
        try {
            vkCore = new VkCore();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception {
        System.out.println("Running server...");
        while (true) {
            Thread.sleep(300);
            try {
                Message msg = vkCore.getMessage();
                if (msg != null && (!msg.getText().isEmpty()))
                    Executors.newCachedThreadPool().execute(()->sendMessage(getReplyMessage(msg),/*User id*/msg.getPeerId(),msg.getRandomId()));
            } catch (ClientException e) {
                System.out.println("Повторное соединение..");
                Thread.sleep(10000);
            }
        }
    }
    public static String getReplyMessage(Message msg){
        String userMessage = msg.getText();
        int userId = msg.getPeerId();
        User data = userData.getOrDefault(userId,null);
        if(data == null) {
            data = new User(userId);
            userData.put(userId,data);
        }
        String replyMessage;
        if(data.isTranslating()){
            if(data.currentWord.inRussian.equalsIgnoreCase(userMessage)) {
                data.totalCorrect++;
                replyMessage = "Верно!"+System.lineSeparator();
            }
            else{
                data.totalWrong++;
                replyMessage = "Ты ошибся :("+System.lineSeparator();
            }
            replyMessage += "Слово: "+data.currentWord.inEnglish+", Перевод: "+data.currentWord.inRussian;
            data.answeredWords.add(data.currentWord);
            data.currentWord = null;
        }
        else if(userMessage.equalsIgnoreCase("start")){
            Word translateWord = null;
            for(Word word : allWords){
                if(!data.answeredWords.contains(word)){
                    translateWord = word;
                    break;
                }
            }
            if(translateWord == null)
                replyMessage = "Поздравляю, ты перевёл все слова!";
            else{
                data.currentWord = translateWord;
                replyMessage = "В ответе напиши перевод слова "+translateWord.inEnglish;
            }
        }
        else if(userMessage.equalsIgnoreCase("score"))
            replyMessage = "✅: "+data.totalCorrect + System.lineSeparator() + "❌: "+data.totalWrong;
        else
            replyMessage = "Нераспознанная команда '"+userMessage+"'";
        return replyMessage;
    }
    public static void sendMessage(String message, int userId, int randomId) {
        try { vkCore.vk.messages().send(vkCore.actor).userId(userId).randomId(randomId).message(message).execute(); }
        catch (ApiException | ClientException e){ e.printStackTrace(); }
    }
}
