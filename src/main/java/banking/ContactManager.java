package banking;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactManager {
    private static final String FILE_NAME = "contacts.json";

    public static void saveContacts(List<Contact> contacts) throws IOException {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            new Gson().toJson(contacts, writer);
        }
    }

    public static List<Contact> loadContacts() throws IOException {
        try (FileReader reader = new FileReader(FILE_NAME)) {
            return new Gson().fromJson(reader, new TypeToken<List<Contact>>(){}.getType());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}
