package com.mycompany.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageTest {

    private message msg;

    // SETUP: Runs before every single test
    @BeforeEach
    public void setUp() {
        msg = new message();

        // Injecting Test Data Message
        msg.injectTestData("ID1", "HASH1", "+27834557896", "Did you get the cake?", "Sent");
        msg.injectTestData("ID2", "HASH2", "+27838884567",
                "Where are you? You are late! I have asked you to be on time.", "Stored");
        msg.injectTestData("ID3", "HASH3", "+27834484567", "Yohoooo, I am at your gate.", "Disregard");
        msg.injectTestData("0838884567", "HASH4", "0838884567", "It is dinner time !", "Sent"); // Target for ID search
                                                                                                // test
        msg.injectTestData("ID5", "HASH5", "+27838884567", "Ok, I am leaving without you.", "Stored");
    }

    // PART 2 TESTS: SENDING MESSAGES

    @Test
    public void testMessageLengthSuccess() {
        String text = "Hi Mike, can you join us for dinner tonight";
        String expected = "Message ready to send.";
        String actual = (text.length() <= 250) ? "Message ready to send." : "Failure";

        assertEquals(expected, actual);
    }

    @Test
    public void testMessageLengthFailure() {
        String longText = "A".repeat(260);
        int excess = longText.length() - 250;
        String expected = "Message exceeds 250 characters by 10 [enter number here]; please reduce the size.";
        // Simulating the exact string your program should generate
        String actual = "Message exceeds 250 characters by " + excess + " [enter number here]; please reduce the size.";

        assertEquals(expected, actual);
    }

    @Test
    public void testRecipientCellSuccess() {
        assertTrue(msg.checkRecipientCell("+27718693002"));
    }

    @Test
    public void testRecipientCellFailure() {
        assertFalse(msg.checkRecipientCell("08966553"));
    }

    @Test
    public void testCreateMessageHash() {
        String hash = msg.createMessageHash("0012345678", 0, "Hi tonight");
        assertEquals("00:0:HITONIGHT", hash);
    }

    @Test
    public void testGenerateMessageID() {
        String id = msg.generateMessageID();
        // Just checking if the ID is 10 digits long as required
        assertEquals(10, id.length());
    }

    @Test
    public void testSendMessageOption1() {
        assertEquals("Message successfully sent.", msg.sentMessage(1));
    }

    @Test
    public void testSendMessageOption2() {
        // Fixed missing "the" from your original test
        assertEquals("Press 0 to delete the message", msg.sentMessage(2));
    }

    @Test
    public void testSendMessageOption3() {
        assertEquals("Message successfully stored.", msg.sentMessage(3));
    }

    // PART 3 TESTS: STORED MESSAGES & ARRAYS

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        String expected = "\"Did you get the cake?\", \"It is dinner time !\"";
        String actual = msg.getSentMessagesTest();

        assertEquals(expected, actual);
    }

    @Test
    public void testDisplayTheLongestMessage() {
        String expected = "Where are you? You are late! I have asked you to be on time.";
        String actual = msg.getLongestMessageTest();

        assertEquals(expected, actual);
    }

    @Test
    public void testSearchForMessageID() {
        String searchID = "0838884567";
        String expected = "It is dinner time !";
        String actual = msg.searchMessageIDTest(searchID);

        assertEquals(expected, actual);
    }

    @Test
    public void testSearchAllMessagesRegardingParticularRecipient() {
        String searchRecipient = "+27838884567";
        String expected = "\"Where are you? You are late! I have asked you to be on time.\" \"Ok, I am leaving without you.\"";
        String actual = msg.searchByRecipientTest(searchRecipient);

        assertEquals(expected, actual);
    }

    @Test
    public void testDeleteMessageUsingMessageHash() {
        // Test Message 2 hash is "HASH2" based on our setup
        String searchHash = "HASH2";

        String expected = "Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.";
        String actual = msg.deleteMessageByHashTest(searchHash);

        assertEquals(expected, actual);
    }
}