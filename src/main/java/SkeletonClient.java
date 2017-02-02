import javax.mail.*;
import java.io.*;
import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.*;

public class SkeletonClient
{
    public static void main(String [] args)
    {
        //Read from secure file - username, mailhost, password
        String mailHost = readFileContents("secure.txt", 0);
        String userName = readFileContents("secure.txt", 1);
        String password = readFileContents("secure.txt", 2);

        //Retrieve email stores emails in array - update to remote server later
        retrieveEmail(mailHost, userName, password);

        //Call GUI
        ClientGUI.setup();
    }

    //Allows reading of particular line in a data file
    public static String readFileContents(String fileName, int dataLine)
    {
        try {
            //Set filebuffer to read desired file filename
            FileReader fr = new FileReader(fileName);
            BufferedReader bf = new BufferedReader(fr);

            int currentLine = 0;

            //Loop forward to desired line in file
            while (currentLine < dataLine) {
                currentLine++;
                bf.readLine();
            }

            return bf.readLine();
            //Returns value of outlined line
        }
        catch (Exception ioException){
            ioException.printStackTrace();
        }

        return "-1";
    }

    //Allows writing to particular line in a data file
    public static int writeFileContents(String fileName, int line, int valueToWrite)
    {
        //Write to specific line of data text file, outlining desired value
        try {
            FileWriter fw = new FileWriter("tempWrite.txt");
            BufferedWriter bw = new BufferedWriter(fw);

            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);

            String currentLine;
            int lineBeingRead = 0;

            //Read line, up until desired line and afterwards, copying and pasting to new temp file
            //When desired line to change is found, new data is changed for old
            while ((currentLine = br.readLine()) != null)
            {
                if(line == lineBeingRead)
                {
                    currentLine = Integer.toString(valueToWrite);
                }
                bw.write(currentLine, 0, currentLine.length());

                lineBeingRead++;
            }

            File desiredFile = new File(fileName);
            desiredFile.delete();
            new File("tempWrite.txt").renameTo(desiredFile);

            return 0;
        }

        catch (Exception ioException){
            ioException.printStackTrace();
        }

        return -1;
    }


    /*Enables checking of emails from an IMAP server*/
    public static int retrieveEmail(String mailHost, String username, String password)
    {
        int updated_date_line = 0;

        try
        {
            //ReadFileValue to find last updated date/time
            //Only read in emails since last updated date
            int last_updated = Integer.parseInt(readFileContents("data.txt", updated_date_line));

            Properties sessionProperties = new Properties();

            sessionProperties.put("mail.store.protocol", "imaps");

            Session mailSession = Session.getInstance(sessionProperties);
            Store mailStore = mailSession.getStore();
            mailStore.connect(mailHost, username, password);

            /*Retrieves just data from folder inbox for now*/
            Folder inbox = mailStore.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            /*Creating email array of email objects, length of email inbox size*/
            //TODO: Change Array Size, this is un-necessarily large
            eMailObject[] emailArray = new eMailObject[inbox.getMessageCount()];

            for (int i = inbox.getMessageCount(); i >= 1; i--)
            {
                Message email = inbox.getMessage(i);

                //End loop if the email has already been read before, based on the sending date of said email and the
                //last updated date
                if (email.getSentDate().getTime() <= last_updated) { break; }

                //Add to array of objects for now, prior to creating database
                //Adds email unique identifier, and leaves current tags null for now
                emailArray[i] = new eMailObject(email.getFrom(), email.getAllRecipients(), email.getSentDate(),
                        email.getSubject(), email.getContent(), email.getMessageNumber(),
                        null);
            }

            storeToDatabase(emailArray);
        }
        catch (Exception mailEx) {
            mailEx.printStackTrace();
        }

        Date currentDate = new Date();

        //Write to file, updating last updated date/time
        //Use getTime from Date, aka amount of milliseconds since 1970 - cast to int
        //Write to data file, on line UDL
        writeFileContents("data.txt", updated_date_line, (int) currentDate.getTime());

        return 0;
    }

    //Need to store emails to database (uses ObjectDB)
    public static int storeToDatabase(eMailObject[] emailsToStore)
    {
        //Management of entities, prior to storage - essentially, creating a transaction
        //to push to the database
        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("$objectdb/db/gnocchiEmailStorage.odb");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        //Loop through supplied array of emails, add to EntityManager
        for (eMailObject e : emailsToStore)
        {
            em.persist(e);
        }

        //Push to database
        em.getTransaction().commit();

        //Close connections to database
        //em.close();
        //emf.close();

        return 0;
    }

    //Retrieve emails from database, dependant on query
    public static eMailObject[] searchQuery(String[] providedQuery, String tagName)
    {
        //Set up connection to database
        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("$objectdb/db/gnocchiEmailStorage.odb");
        EntityManager em = emf.createEntityManager();

        TypedQuery<eMailObject> tagQuery;

        int counter = 0;

        String completeQuery = "SELECT eMail FROM eMailObject email WHERE ";

        while (counter < providedQuery.length)
        {
            //If there is a logical operator, concatenate to the SQL query and add a space
            if (providedQuery[counter].equalsIgnoreCase("AND") || providedQuery[counter].equalsIgnoreCase("OR") ||
                    providedQuery[counter].equalsIgnoreCase("NOT"))
            {
                completeQuery = completeQuery.concat(providedQuery[counter].toUpperCase() + " ");
                counter++;
            }

            //Contains - check if email contains the search term
            else if (providedQuery[counter].equalsIgnoreCase("Contains"))
            {
                //WHERE email body contains search term
                completeQuery = completeQuery.concat("eMail.body LIKE %" + providedQuery[counter + 1] + "% ");

                //OR
                completeQuery = completeQuery.concat("OR ");

                //Email subject contains search term
                completeQuery = completeQuery.concat("eMail.subject LIKE %" + providedQuery[counter + 1] + "% ");

                //Adds two to counter, skipping past search-term
                counter = counter + 2;
            }

            else if (providedQuery[counter].equalsIgnoreCase("Sender "))
            {
                //WHERE email sender contains search term
                completeQuery = completeQuery.concat("eMail.senders LIKE %" + providedQuery[counter + 1] + "% ");
                counter = counter + 2;
            }

            else if (providedQuery[counter].equalsIgnoreCase("Date-Match "))
            {
                //WHERE email sent date matches query
                completeQuery = completeQuery.concat("eMail.sentDate LIKE %" + providedQuery[counter + 1] + "% ");
                counter = counter + 2;
            }
        }

        tagQuery = em.createQuery(completeQuery, eMailObject.class);

        //Return results as List
        tagQuery.setParameter("tag", tagName);
        List<eMailObject> results = tagQuery.getResultList();

        //Convert list into array of eMailObjects
        eMailObject[] resultsAsArray = new eMailObject[results.size()];
        results.toArray(resultsAsArray);
        return resultsAsArray;
    }
}
