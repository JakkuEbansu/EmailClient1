import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class ClientGUI
{
    public static void setup()
    {
        //Read from GUI data for desired number of panes, and pane content (desired tags to be displayed)
        int desiredNoPanes = Integer.parseInt(SkeletonClient.readFileContents("panesData.txt", 0));
        int desiredNoSections = Integer.parseInt(SkeletonClient.readFileContents("sectionsData.txt", 0));

        String[] panes = new String[desiredNoPanes];
        String[] sections = new String[desiredNoSections];

        //Read line by line into the data files - for each line, the corresponding pane stores its' associated tag
        //Or section-label
        for(int i = 1; i <= desiredNoPanes; i++)
        {
            //Read desired tag(s) from panes data file
            panes[i] = SkeletonClient.readFileContents("panesData.txt", i);
        }

        for(int i = 1; i <= desiredNoSections; i++)
        {
            //Read desired section(s) from panes data file
            sections[i] = SkeletonClient.readFileContents("sectionsData.txt", i);
        }

        //Initialise - draw window, basic components
        drawWindow(panes, sections);
    }

    private static int drawWindow(String[] panes, String[] sections)
    {
        //Draw frame, add information - dimensions, title, icon, etc.
        JFrame mainWindow = new JFrame("Tagging Email Client Test");
        //TODO : Add icon image for program
        //mainWindow.setIconImage(Image image);

        //Define the dimensions of the full window
        int window_width = Integer.parseInt(SkeletonClient.readFileContents("windowData.txt", 0));
        int window_height = Integer.parseInt(SkeletonClient.readFileContents("windowData.txt", 1));
        mainWindow.setSize(window_width, window_height);

        //Get content pane (Container for other objects)
        Container content = mainWindow.getContentPane();

        //Define layout of container
        content.setLayout(new GridLayout(1, 2));

        //Create container for storing the panes
        JPanel panesPanel = new JPanel();
        panesPanel.setLayout(new GridLayout(0, 2));

        //Add toolbar to container
        //For each pane, we create a panel to add to the panes container
        for (String pane : panes){
            JPanel tempPanel = new JPanel();
            tempPanel.setLayout(new GridLayout(0, 1));

            //Enable scrolling for the temporary pane to be added
            JScrollPane tempScrollPane = new JScrollPane(tempPanel);

            //Load emails based on relevant tag, add to container -
            //Stored in panes[i]

            String[] searchArray = new String[2];
            searchArray[0] = "Tag ";
            searchArray[1] = pane;

            eMailObject[] paneQueryResults = SkeletonClient.searchQuery(searchArray, " ");

            //TODO: loop through paneQueryResults, adding entries for each email, buttons to open, delete, etc.
            //Loop through email results, adding to scrollable list of emails, each with read/tag/etc. buttons
            for (eMailObject result : paneQueryResults)
            {
                JPanel resultPanel = new JPanel();
                resultPanel.setLayout(new GridLayout(1, 0));

                //TODO: Add text-preview for email
                String emailTitle = result.getSubject().substring(0, 20);
                String emailBody = result.getBody().substring(0, 7) + "...";

                resultPanel.add(new JLabel(emailTitle));
                resultPanel.add(new JLabel(emailBody));

                //TODO: Add buttons (Read, Tag, etc.), listeners
                JButton readButton = new JButton("Read"); //TODO: Add icon
                readButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        //Call Read Email function from SkeletonClient
                        //TODO
                        SkeletonClient.readEmail(result);
                    }
                });

                JButton tagButton = new JButton("Tag");
                tagButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        //Call Add Tag function from GUI somewhere
                        //TODO
                        addTagGUI(result);
                    }
                });

                tempPanel.add(resultPanel);
            }

            panesPanel.add(tempScrollPane);
        }

        content.add(panesPanel);

        return 0;
    }

    //TODO
    //Intention : Simple function that springs up a nice little window, which you can use to add a tag to an email
    public static void addTagGUI(eMailObject targetEmail)
    {
        final java.util.List<eMailObject> emailsToUpdate = new ArrayList<eMailObject>();
        emailsToUpdate.add(targetEmail);

        final JTextField input = new JTextField("", 10);

        //Create text field to edit
        //Add button to add tag to email
        JButton updateButton = new JButton("Add Tag");
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                SkeletonClient.updateTags(emailsToUpdate, stringToAdd);
            }
        });

    }

    //Add method for searchbox - draw searchbox, add actionlistener for search button, load new window with results?
    //Feed to search function in SkeletonClient
}
