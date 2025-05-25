package banking.controller;

import banking.model.Contact;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * ContactManager is responsible for managing contacts by saving and loading them
 * to and from a JSON file. It provides methods to save a list of contacts and
 * load them back, handling any I/O exceptions that may occur.
 */
public class ContactManager {

    private static final String FILE_NAME = "contacts.json";


    /**
     * Saves a list of contacts to a JSON file.
     *
     * @param contacts the list of contacts to be saved
     * @throws IOException if an I/O error occurs while writing to the file
     */
    public static void saveContacts(List<Contact> contacts) throws IOException {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            new Gson().toJson(contacts, writer);
        }
    }


    /**
     * Loads the list of contacts from a JSON file.
     * If the file cannot be read or is not found, an empty list is returned.
     *
     * @return a List of Contact objects loaded from the file, or an empty list if an IOException occurs
     * @throws IOException if an error occurs during file reading
     */
    public static List<Contact> loadContacts() throws IOException {
        try (FileReader reader = new FileReader(FILE_NAME)) {
            return new Gson().fromJson(reader, new TypeToken<List<Contact>>() {
            }.getType());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

}
