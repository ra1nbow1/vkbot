package date;

import java.util.ArrayList;
import java.util.List;

public class User {

    public User(int _id) {
        id = _id;
    }

    public int id;
    public int totalWrong;
    public int totalCorrect;
    public List<Word> answeredWords = new ArrayList<>();

    public boolean isTranslating() {return currentWord != null;}


    public transient  Word currentWord = null;

}
