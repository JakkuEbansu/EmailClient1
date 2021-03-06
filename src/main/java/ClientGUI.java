import com.objectdb.o.EXP;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import javax.swing.*;

public class ClientGUI
{
    private int desiredNoSections;

    public ClientGUI(){
        setup();
    }

    //Updates pane/sections data, re-draws GUI
    public void setup()
    {
        desiredNoSections = FileOperations.getNumSections();

        String[] sections = new String[desiredNoSections];
        int arrayCounter = 0;

        for(int i = 2; i < desiredNoSections + 2; i++)
        {
            //Read desired section(s) from panes data file
            sections[arrayCounter] = FileOperations.readFileContents("secData.txt", i);
            arrayCounter++;
        }
        System.out.println(Arrays.toString(sections));

        //Initialise - draw window, basic components
        formGUI.main(sections);
    }

    //Intention : Simple function that springs up a nice little window, which you can use to add a tag to an email
    public static void addTagGUI(eMailObject targetEmail)
    {
        final JFrame tagGUIWindow = new JFrame("Add a Tag to this email!");
        Container tagGUIContainer = tagGUIWindow.getContentPane();
        tagGUIWindow.setLayout(new GridLayout(0, 1));
        tagGUIWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        final java.util.List<eMailObject> emailsToUpdate = new ArrayList<eMailObject>();
        emailsToUpdate.add(targetEmail);

        JPanel emailInfoPanel = new JPanel();
        emailInfoPanel.setLayout(new GridLayout(0, 1));

        final JLabel sender = new JLabel(Arrays.toString(targetEmail.getSenders().toArray()));
        emailInfoPanel.add(sender);

        final JLabel title = new JLabel(targetEmail.getSubject());
        emailInfoPanel.add(title);

        final JLabel received = new JLabel("" + targetEmail.getReceivedDate());
        emailInfoPanel.add(received);
        tagGUIContainer.add(emailInfoPanel);

        //Add input field to outline the tag itself
        final JTextField input = new JTextField("", 10);
        tagGUIContainer.add(input);

        //Create text field to edit
        //Add button to add tag to email - close frame once tag update has occurred
        JButton updateButton = new JButton("Add Tag");
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                SkeletonClient.updateTags(emailsToUpdate, input.getText());
                tagGUIWindow.dispose();}
        });
        tagGUIContainer.add(updateButton);
        tagGUIWindow.setVisible(true);
        tagGUIWindow.pack();
    }

    //Intention : Simple function that springs up a nice little window, which you can use to write a reply to an email
    public static void writeEmailReply(final eMailObject replyEmail)
    {
        final JFrame writeEmailWindow = new JFrame("Reply To " + replyEmail.getSenders());

        Container writeEmailContainer = writeEmailWindow.getContentPane();
        writeEmailWindow.setLayout(new GridLayout(0, 1));
        writeEmailWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel writePanel = new JPanel();
        writePanel.setLayout(new GridLayout(0, 1));

        final JLabel title = new JLabel(replyEmail.getSubject());
        writePanel.add(title);

        final JLabel date = new JLabel("Received On: " + replyEmail.getReceivedDate());
        writePanel.add(date);
        writeEmailContainer.add(writePanel);

        String emailBody = SkeletonClient.retrieveBody(replyEmail);
        final String[] splitBody = emailBody.split("\\?");

        JPanel inlineReplies = new JPanel();
        inlineReplies.setLayout(new GridLayout(0, 1));

        final String[] emailQuestions = new String[splitBody.length];
        final JTextField[] inlineReply = new JTextField[splitBody.length];
        int currentArrayIndex = 0;

        for (String component : splitBody)
        {
            component = component.concat("?");
            final JLabel componentLabel = new JLabel(component);
            inlineReplies.add(componentLabel);
            emailQuestions[currentArrayIndex] = component;

            inlineReply[currentArrayIndex] = new JTextField("", 80);
            inlineReplies.add(inlineReply[currentArrayIndex]);

            currentArrayIndex = currentArrayIndex++;
        }

        writeEmailContainer.add(inlineReplies);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            {
                String emailToSend = "";

                for (int currentIndex = 0; currentIndex <= splitBody.length; currentIndex++)
                {
                    emailToSend = emailToSend.concat(emailQuestions[currentIndex]);
                    emailToSend = emailToSend.concat("\n" + inlineReply[currentIndex].getText());
                }

                SkeletonClient.writeReply(emailToSend, replyEmail);
            }
        });
        writeEmailContainer.add(sendButton);
        writeEmailWindow.setVisible(true);
        writeEmailWindow.pack();
    }

    //Intention : Simple function that springs up a nice little window, which you can use to write an email
    public static void writeNewEmail()
    {
        final JFrame writeEmailWindow = new JFrame("New Email");

        final Container writeEmailContainer = writeEmailWindow.getContentPane();
        writeEmailWindow.setLayout(new GridLayout(0, 1));
        writeEmailWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel emailInfo = new JPanel();
        emailInfo.setLayout(new GridLayout(0, 2));

        final JLabel title = new JLabel("Subject: ");
        emailInfo.add(title);

        final JTextField desiredTitle = new JTextField();
        emailInfo.add(desiredTitle);

        final JLabel recipientTitle = new JLabel("Recipient: ");
        emailInfo.add(recipientTitle);

        final JTextField recipient = new JTextField();
        emailInfo.add(recipient);
        writeEmailWindow.add(emailInfo);

        int numberOfServers = Integer.parseInt(FileOperations.readFileContents("mailData.txt", 1));

        JPanel mailServerButtonPanel = new JPanel();

        final JTextArea emailServer = new JTextArea();
        emailInfo.add(emailServer);
        emailServer.setEditable(false);

        for (int i = 0; i < numberOfServers; i++)
        {
            final int currentServer = i;
            JButton mailServerButton = new JButton(FileOperations.retrieveCredentials("userName", i));

            mailServerButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    emailServer.setText(FileOperations.retrieveCredentials("userName", currentServer));
                }
            });

            mailServerButtonPanel.add(mailServerButton);
        }

        writeEmailContainer.add(mailServerButtonPanel);

        final JTextField emailBody = new JTextField();
        writeEmailContainer.add(emailBody);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            {
                JLabel alertLabel;

                if (desiredTitle.getText().equals(""))
                {
                    alertLabel = new JLabel("Add Email subject!");
                    writeEmailContainer.add(alertLabel);
                }
                else if (emailBody.getText().equals(""))
                {
                    alertLabel = new JLabel("Add Email body!");
                    writeEmailContainer.add(alertLabel);
                }
                else if (recipient.getText().equals(""))
                {
                    alertLabel = new JLabel("Add Email recipient!");
                    writeEmailContainer.add(alertLabel);
                }
                else if (emailServer.getText().equals(""))
                {
                    alertLabel = new JLabel("Select Email Server to send from!");
                    writeEmailContainer.add(alertLabel);
                }
                else
                {
                    SkeletonClient.writeNew(recipient.getText(),
                            desiredTitle.getText(), emailBody.getText(), Integer.parseInt(emailServer.getText()));
                }
            }
        });
        writeEmailContainer.add(sendButton);
        writeEmailWindow.setVisible(true);
        writeEmailWindow.pack();
    }

    //Opens a 'read email' dialogue, with more detailed information about said email including retrieving the body of
    //Said email
    public static void readEmail(final eMailObject targetEmail)
    {
        final JFrame readEmailWindow = new JFrame("Reading " + targetEmail.getSubject());
        Container readEmailGUIContainer = readEmailWindow.getContentPane();
        readEmailWindow.setLayout(new GridLayout(0, 1));
        readEmailWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel emailInfo = new JPanel();
        emailInfo.setLayout(new GridLayout(0, 1));

        final JLabel senders = new JLabel("From: " + targetEmail.getSenders().toString());
        emailInfo.add(senders);

        final JLabel recipients = new JLabel("To: " + targetEmail.getRecipients().toString());
        emailInfo.add(recipients);

        final JLabel sentDate = new JLabel("Sent: " + targetEmail.getSentDate().toString());
        emailInfo.add(sentDate);

        final JLabel receivedDate = new JLabel("Received " + targetEmail.getReceivedDate().toString());
        emailInfo.add(receivedDate);

        final JLabel tags = new JLabel("Tags: " + targetEmail.getTags().toString());
        emailInfo.add(tags);
        readEmailGUIContainer.add(emailInfo);

        final JLabel subject = new JLabel("Subject: " + targetEmail.getSubject());
        emailInfo.add(subject);

        final JTextArea email = new JTextArea(SkeletonClient.retrieveBody(targetEmail));
        readEmailGUIContainer.add(email);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 0));
        readEmailGUIContainer.add(buttonsPanel);

        final JButton addTag = new JButton("Add tag to this email");
        addTag.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) { addTagGUI(targetEmail); }
        });
        buttonsPanel.add(addTag);

        final JButton addReply = new JButton("Reply to this email");
        addReply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) { writeEmailReply(targetEmail); }
        });
        buttonsPanel.add(addReply);

        final JButton customReply = new JButton("Reply using custom reply points");
        customReply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                ExpandedReplies reply = new ExpandedReplies();
                reply.defineReplyPoints(targetEmail);
            }
        });
        buttonsPanel.add(customReply);

        readEmailWindow.setVisible(true);
        readEmailWindow.pack();
    }

    public static void displayResults(final eMailObject[] results, String[] query)
    {
        final JFrame displayResultsWindow = new JFrame("Results of " + Arrays.toString(query));
        final Container dRWContainer = displayResultsWindow.getContentPane();
        dRWContainer.setLayout(new GridLayout(0, 1));

        for (eMailObject result : results) {
            final JPanel singleResultPane = new JPanel();
            final eMailObject currentEmail = result;

            JLabel timeSent = new JLabel();
            timeSent.setText(currentEmail.getReceivedDate().toString());
            singleResultPane.add(timeSent);

            JLabel emailHeader = new JLabel();
            emailHeader.setText(currentEmail.getSubject());
            singleResultPane.add(emailHeader);

            JLabel senderLabel = new JLabel();
            senderLabel.setText(currentEmail.getSenders().toString());
            singleResultPane.add(senderLabel);

            JLabel tagLabel = new JLabel();
            if (currentEmail.getTags() != null) {
                tagLabel.setText(currentEmail.getTags().toString());
            }
            singleResultPane.add(tagLabel);

            JPanel singleEmailButtons = new JPanel();

            JButton readButton = new JButton("Read");
            readButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    ClientGUI.readEmail(currentEmail);
                }
            });
            singleEmailButtons.add(readButton);

            JButton tagButton = new JButton("Tag");
            tagButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    ClientGUI.addTagGUI(currentEmail);
                }
            });
            singleEmailButtons.add(tagButton);

            JButton replyButton = new JButton("Reply");
            replyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    ClientGUI.writeEmailReply(currentEmail);
                }
            });
            singleEmailButtons.add(replyButton);

            singleResultPane.add(singleEmailButtons);
            dRWContainer.add(singleResultPane);
        }
        displayResultsWindow.setVisible(true);
        displayResultsWindow.pack();
    }

    public static void addPaneSectionsGUI()
    {
        final JFrame panSecWindow = new JFrame("Add a Pane/Section to the Client");
        Container addPanSecContainer = panSecWindow.getContentPane();
        panSecWindow.setLayout(new GridLayout(0, 1));
        panSecWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel currentSecInfoPanel = new JPanel();
        currentSecInfoPanel.setLayout(new GridLayout(0, 1));

        //Add panel - add x pane (outline tag) in y section
        JPanel addPanel = new JPanel();
        addPanel.setLayout(new GridLayout(0, 1));

        JLabel sectionLabel = new JLabel("Section to add to: ");
        addPanel.add(sectionLabel);

        final JTextField sectionField = new JTextField("");
        addPanel.add(sectionField);

        JLabel paneLabel = new JLabel("Panel Tag to add: ");
        addPanel.add(paneLabel);

        final JTextField paneField = new JTextField("");
        addPanel.add(paneField);

        JButton addButton = new JButton("Add to GUI");
        addPanel.add(addButton);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                File ifExists = new File("section" + sectionField.getText() + ".txt");
                if (!ifExists.exists())
                {   FileOperations.addNewSection(sectionField.getText()); }

                FileOperations.addPane(paneField.getText(), sectionField.getText());
            }
        });

        addPanSecContainer.add(addPanel);
        addPanSecContainer.add(addButton);

        panSecWindow.setVisible(true);
        panSecWindow.pack();
    }
}