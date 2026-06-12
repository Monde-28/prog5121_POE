package com.mycompany.assignment;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class message {

    private String[] sentMessages = new String[100];
    private String[] disregardedMessages = new String[100];
    private String[] storedMessages = new String[100];
    private String[] messageHashes = new String[100];
    private String[] messageIDs = new String[100];
    private String[] recipients = new String[100];

    private int messageCount = 0; // Tracks the current index across all parallel arrays

    private static final String JSON_FILE = "messages.json";
    private int totalMessages = 0;
    private Random random = new Random();

    // Runs after successful login
    public void startMessaging() {
        JOptionPane.showMessageDialog(null, "Welcome to QuickChat.");

        int choice;
        do {
            String menu = """
                    Please choose an option:
                    1) Send Message
                    2) Show Recently Sent Messages
                    3) Quit
                    4) Stored Messages Report
                    """;

            String inputChoice = JOptionPane.showInputDialog(menu);
            if (inputChoice == null)
                return;

            try {
                choice = Integer.parseInt(inputChoice);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid option. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1 -> sendMessageFeature();
                case 2 -> JOptionPane.showMessageDialog(null, "Coming Soon.");
                case 3 -> {
                    int confirm = JOptionPane.showConfirmDialog(
                            null,
                            "Are you sure you want to exit?",
                            "Confirm Exit",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        JOptionPane.showMessageDialog(null, "Goodbye!");
                        System.exit(0);
                    }
                }
                case 4 -> handleStoredMessagesMenu();
                default -> JOptionPane.showMessageDialog(null, "Invalid option. Please try again.");
            }
        } while (true);
    }

    // SEND MESSAGE FEATURE
    private void sendMessageFeature() {
        String inputNum = JOptionPane.showInputDialog("How many messages do you want to send?");
        if (inputNum == null)
            return;

        int numMessages;
        try {
            numMessages = Integer.parseInt(inputNum);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Enter a valid number.");
            return;
        }

        for (int i = 0; i < numMessages; i++) {
            // Get and validate recipient
            String recipient = JOptionPane.showInputDialog("Enter recipient cell number (+27XXXXXXXXX):");
            if (recipient == null)
                return;
            if (!checkRecipientCell(recipient)) {
                JOptionPane.showMessageDialog(null,
                        "Cell number is incorrectly formatted or does not contain an international code. Please correct the number and try again.");
                i--;
                continue;
            }

            // Get and validate message
            String messageText = JOptionPane.showInputDialog("Enter your message (max 250 characters):");
            if (messageText == null)
                return;
            if (messageText.length() > 250) {
                int excess = messageText.length() - 250;
                JOptionPane.showMessageDialog(null,
                        "Message exceeds 250 characters by " + excess + "; please reduce the size.");
                i--;
                continue;
            }

            // Generate Data
            String messageID = generateMessageID();
            String messageHash = createMessageHash(messageID, i + 1, messageText);

            JOptionPane.showMessageDialog(null,
                    "Message ID: " + messageID +
                            "\nMessage Hash: " + messageHash +
                            "\nRecipient: " + recipient +
                            "\nMessage: " + messageText,
                    "Message Summary",
                    JOptionPane.INFORMATION_MESSAGE);

            // User Action
            String[] options = { "Send Message", "Disregard Message", "Store Message" };
            int action = JOptionPane.showOptionDialog(null, "Choose what to do with this message:",
                    "Message Options", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            String actionResult = sentMessage(action + 1);

            // Save to the exact arrays required
            messageIDs[messageCount] = messageID;
            messageHashes[messageCount] = messageHash;
            recipients[messageCount] = recipient;

            if (action == 0) {
                sentMessages[messageCount] = messageText;
                totalMessages++;
            } else if (action == 1) {
                disregardedMessages[messageCount] = messageText;
            } else if (action == 2) {
                storedMessages[messageCount] = messageText;
                storeMessageToJSON(messageID, messageHash, recipient, messageText);
            }

            messageCount++;
            JOptionPane.showMessageDialog(null, actionResult);
        }

        JOptionPane.showMessageDialog(null, "Total messages sent: " + totalMessages);
    }

    public boolean checkRecipientCell(String recipient) {
        return recipient.matches("^\\+27\\d{9}$");
    }

    public String generateMessageID() {
        long id = 1000000000L + (long) (random.nextDouble() * 8999999999L);
        JOptionPane.showMessageDialog(null, "Message ID generated: " + id);
        return String.valueOf(id);
    }

    public String createMessageHash(String messageID, int messageNum, String messageText) {
        String[] words = messageText.trim().split("\\s+");
        String firstWord = words[0];
        String lastWord = words.length > 1 ? words[words.length - 1] : "";
        String firstTwoDigits = messageID.substring(0, 2);
        return (firstTwoDigits + ":" + messageNum + ":" + firstWord + lastWord).toUpperCase();
    }

    public String sentMessage(int choice) {
        return switch (choice) {
            case 1 -> "Message successfully sent.";
            case 2 -> "Press 0 to delete the message";
            case 3 -> "Message successfully stored.";
            default -> "Invalid option.";
        };
    }

    // STORED MESSAGES REPORTING (ARRAYS)
    private void handleStoredMessagesMenu() {
        loadJsonMessagesIntoArrays();

        String menu = """
                Stored Messages Menu:
                a) Display Sender and Recipient of stored messages
                b) Display the longest stored message
                c) Search for a Message ID
                d) Search messages by Recipient
                e) Delete a message using Hash
                f) Display Full Report
                """;

        String choice = JOptionPane.showInputDialog(menu);
        if (choice == null)
            return;

        switch (choice.toLowerCase()) {
            case "a" -> displaySenderAndRecipient();
            case "b" -> displayLongestMessage();
            case "c" -> searchMessageID();
            case "d" -> searchByRecipient();
            case "e" -> deleteMessageByHash();
            case "f" -> displayReport();
            default -> JOptionPane.showMessageDialog(null, "Invalid selection.");
        }
    }

    private void displaySenderAndRecipient() {
        StringBuilder report = new StringBuilder("Sender & Recipient of Stored Messages:\n\n");
        for (int i = 0; i < messageCount; i++) {
            if (storedMessages[i] != null) {
                report.append("Sender: Current User | Recipient: ").append(recipients[i]).append("\n");
            }
        }
        JOptionPane.showMessageDialog(null, report.toString());
    }

    private void displayLongestMessage() {
        String longest = "";
        for (int i = 0; i < messageCount; i++) {
            if (storedMessages[i] != null && storedMessages[i].length() > longest.length()) {
                longest = storedMessages[i];
            }
        }
        JOptionPane.showMessageDialog(null, "Longest Stored Message:\n" + longest);
    }

    private void searchMessageID() {
        String searchID = JOptionPane.showInputDialog("Enter Message ID to search:");
        for (int i = 0; i < messageCount; i++) {
            if (messageIDs[i] != null && messageIDs[i].equals(searchID)) {
                String text = getActiveMessageText(i);
                JOptionPane.showMessageDialog(null, "Recipient: " + recipients[i] + "\nMessage: " + text);
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Message ID not found.");
    }

    private void searchByRecipient() {
        String searchRecip = JOptionPane.showInputDialog("Enter Recipient Cell Number to search:");
        StringBuilder results = new StringBuilder("Messages for " + searchRecip + ":\n");
        boolean found = false;

        for (int i = 0; i < messageCount; i++) {
            if (recipients[i] != null && recipients[i].equals(searchRecip)) {
                results.append("- ").append(getActiveMessageText(i)).append("\n");
                found = true;
            }
        }
        JOptionPane.showMessageDialog(null, found ? results.toString() : "No messages found for this recipient.");
    }

    private void deleteMessageByHash() {
        String searchHash = JOptionPane.showInputDialog("Enter Message Hash to delete:");
        for (int i = 0; i < messageCount; i++) {
            if (messageHashes[i] != null && messageHashes[i].equals(searchHash)) {
                String deletedMsgText = getActiveMessageText(i);

                // Nullify the index across all parallel arrays
                messageIDs[i] = null;
                messageHashes[i] = null;
                recipients[i] = null;
                sentMessages[i] = null;
                disregardedMessages[i] = null;
                storedMessages[i] = null;

                JOptionPane.showMessageDialog(null, "Message: \"" + deletedMsgText + "\" successfully deleted.");
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Hash not found.");
    }

    private void displayReport() {
        StringBuilder report = new StringBuilder("FULL MESSAGES REPORT:\n\n");
        for (int i = 0; i < messageCount; i++) {
            if (messageIDs[i] != null) {
                report.append("Hash: ").append(messageHashes[i]).append("\n")
                        .append("Recipient: ").append(recipients[i]).append("\n")
                        .append("Message: ").append(getActiveMessageText(i)).append("\n")
                        .append("----------------------------\n");
            }
        }
        JOptionPane.showMessageDialog(null, report.toString());
    }

    // Helper method to grab the text from whichever array it resides in
    private String getActiveMessageText(int index) {
        if (sentMessages[index] != null)
            return sentMessages[index];
        if (disregardedMessages[index] != null)
            return disregardedMessages[index];
        if (storedMessages[index] != null)
            return storedMessages[index];
        return "";
    }

    // JSON HANDLING
    @SuppressWarnings("unchecked")
    public void storeMessageToJSON(String messageID, String messageHash, String recipient, String messageText) {
        JSONObject messageData = new JSONObject();
        messageData.put("MessageID", messageID);
        messageData.put("MessageHash", messageHash);
        messageData.put("Recipient", recipient);
        messageData.put("Message", messageText);

        JSONArray messagesArray = readExistingArray();
        messagesArray.add(messageData);

        try (FileWriter file = new FileWriter(JSON_FILE)) {
            file.write(messagesArray.toJSONString());
            file.flush();
        } catch (IOException e) {
            System.out.println("Error saving message: " + e.getMessage());
        }
    }

    private void loadJsonMessagesIntoArrays() {
        JSONArray jsonArray = readExistingArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject obj = (JSONObject) jsonArray.get(i);
            String loadedID = (String) obj.get("MessageID");

            boolean alreadyExists = false;
            for (int j = 0; j < messageCount; j++) {
                if (messageIDs[j] != null && messageIDs[j].equals(loadedID)) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                messageIDs[messageCount] = loadedID;
                messageHashes[messageCount] = (String) obj.get("MessageHash");
                recipients[messageCount] = (String) obj.get("Recipient");
                storedMessages[messageCount] = (String) obj.get("Message");
                messageCount++;
            }
        }
    }

    private JSONArray readExistingArray() {
        JSONParser parser = new JSONParser();
        File f = new File(JSON_FILE);
        if (!f.exists())
            return new JSONArray();

        try (FileReader reader = new FileReader(f)) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            }
        } catch (IOException | ParseException e) {
            // Ignored
        }
        return new JSONArray();
    }

    public void injectTestData(String id, String hash, String recipient, String text, String status) {
        messageIDs[messageCount] = id;
        messageHashes[messageCount] = hash;
        recipients[messageCount] = recipient;

        if (status.equals("Sent")) {
            sentMessages[messageCount] = text;
        } else if (status.equals("Disregard")) {
            disregardedMessages[messageCount] = text;
        } else if (status.equals("Stored")) {
            storedMessages[messageCount] = text;
        }
        messageCount++;
    }

    // 2. Returns Sent Messages (For Test 1)
    public String getSentMessagesTest() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messageCount; i++) {
            if (sentMessages[i] != null) {
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append("\"").append(sentMessages[i]).append("\"");
            }
        }
        return sb.toString();
    }

    // 3. Returns the longest message (For Test 2)
    public String getLongestMessageTest() {
        String longest = "";
        for (int i = 0; i < messageCount; i++) {
            String currentText = getActiveMessageText(i);
            if (currentText.length() > longest.length()) {
                longest = currentText;
            }
        }
        return longest;
    }

    // 4. Returns message by ID (For Test 3)
    public String searchMessageIDTest(String searchID) {
        for (int i = 0; i < messageCount; i++) {
            if (messageIDs[i] != null && messageIDs[i].equals(searchID)) {
                return getActiveMessageText(i);
            }
        }
        return "Not found";
    }

    // 5. Returns messages by Recipient (For Test 4)
    public String searchByRecipientTest(String searchRecip) {
        StringBuilder results = new StringBuilder();
        for (int i = 0; i < messageCount; i++) {
            if (recipients[i] != null && recipients[i].equals(searchRecip)) {
                if (results.length() > 0)
                    results.append(" ");
                results.append("\"").append(getActiveMessageText(i)).append("\"");
            }
        }
        return results.toString();
    }

    // 6. Deletes by Hash and returns the exact string (For Test 5)
    public String deleteMessageByHashTest(String searchHash) {
        for (int i = 0; i < messageCount; i++) {
            if (messageHashes[i] != null && messageHashes[i].equals(searchHash)) {
                String deletedMsgText = getActiveMessageText(i);
                messageIDs[i] = null;
                messageHashes[i] = null;
                return "Message: \"" + deletedMsgText + "\" successfully deleted.";
            }
        }
        return "Hash not found.";
    }
}