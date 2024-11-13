import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class OnlineVotingSystem {
    
    // Main class: OnlineVotingSystem
    // This class contains the main program to simulate the voting system, handle database connections,
    // and process user input and voting logic.
    public static void main(String[] args) {

        // Creating Scanner object to read user input
        Scanner input = new Scanner(System.in); // Object: input (Scanner) - used to capture user input.

        // Database connection details
        String url = "jdbc:mysql://localhost:3306/";  // Database URL
        String user = "root";                         // Database username
        String password = "";                         // Database password

        Connection connection = null;  // Object: connection (Connection) - used to manage the connection to the database.

        try {
            // Establish a connection to the database
            connection = DriverManager.getConnection(url, user, password); // Method: getConnection() - establishes connection to the DB

            // Ask for the user's name and age
            System.out.print("Please enter your name: ");
            String userName = input.nextLine();  // Object: userName (String) - used to store the input name.

            System.out.print("Please enter your age: ");
            int userAge = input.nextInt();  // Object: userAge (int) - stores the age input.

            // Check if the user is eligible to vote
            if (userAge < 18) {
                System.out.println("I'm sorry, you are not eligible to vote.");
                return;  // Exits the program if the user is underage.
            }

            // Check if the user has already voted in the system
            PreparedStatement checkVoter = connection.prepareStatement(
                "SELECT * FROM voters_db.voters WHERE name = ?"
            );
            checkVoter.setString(1, userName);  // Set the name parameter for the query
            ResultSet resultSet = checkVoter.executeQuery(); // Method: executeQuery() - executes the SELECT statement and returns the result.

            if (resultSet.next()) {
                System.out.println("You have already voted. Thank you!");
                return;  // Exits the program if the user has already voted.
            }

            // Retrieve candidates from the candidates table in the database where age >= 21
            Statement statement = connection.createStatement(); // Object: statement (Statement) - used to execute SQL queries.

            ResultSet candidatesResult = statement.executeQuery("SELECT id, name, age FROM candidates_db.candidates WHERE age >= 21"); // Method: executeQuery() - retrieves candidate data.

            ArrayList<String> candidates = new ArrayList<>();  // Object: candidates (ArrayList) - holds a list of candidate names.
            System.out.println("Please select a candidate to vote for:");

            int index = 1;  // Variable: index (int) - used to display candidate options.
            while (candidatesResult.next()) {
                String candidateName = candidatesResult.getString("name");  // Object: candidateName (String) - stores the candidate's name.
                int candidateAge = candidatesResult.getInt("age");          // Object: candidateAge (int) - stores the candidate's age.
                
                if (candidateAge >= 21) {  // Only show candidates who are eligible (age >= 21).
                    candidates.add(candidateName);
                    System.out.println(index + ". " + candidateName); // Display each candidate with an index number.
                    index++;
                }
            }

            // Check if there are no eligible candidates
            if (candidates.isEmpty()) {
                System.out.println("No eligible candidates to vote for.");
                return;  // Exit the program if no candidates meet the eligibility.
            }

            // Get user's vote selection
            int userSelection = input.nextInt();  // Object: userSelection (int) - stores the user's selection from the list.

            // Check if the user's selection is valid
            if (userSelection > 0 && userSelection <= candidates.size()) {
                String selectedCandidate = candidates.get(userSelection - 1); // Object: selectedCandidate (String) - stores the name of the selected candidate.

                // Increment the vote count for the selected candidate
                PreparedStatement updateVote = connection.prepareStatement(
                    "UPDATE candidates_db.candidates SET votes = votes + 1 WHERE name = ?"
                );
                updateVote.setString(1, selectedCandidate);  // Set the candidate name for the update query
                updateVote.executeUpdate(); // Method: executeUpdate() - executes the UPDATE statement to increment the vote count.

                // Add the user to the list of voters to prevent double voting
                PreparedStatement addVoter = connection.prepareStatement(
                    "INSERT INTO voters_db.voters (name, age, voted) VALUES (?, ?, ?)"
                );
                addVoter.setString(1, userName);  // Set the user's name
                addVoter.setInt(2, userAge);      // Set the user's age
                addVoter.setBoolean(3, true);     // Set the voted status to true
                addVoter.executeUpdate();  // Method: executeUpdate() - inserts a new voter record into the database.

                System.out.println("Thank you for voting, " + userName + "!");
            } else {
                System.out.println("Invalid selection. No vote recorded.");
            }

            // Display the updated vote count for all candidates
            ResultSet votesResult = statement.executeQuery(
                "SELECT name, votes FROM candidates_db.candidates ORDER BY votes DESC"
            );
            System.out.println("Vote count:");
            while (votesResult.next()) {
                System.out.println(votesResult.getString("name") + ": " + votesResult.getInt("votes") + " votes");
            }

        } catch (SQLException e) {
            // Method: printStackTrace() - prints the SQL error details in case of an exception.
            
        } finally {
            try {
                if (connection != null) connection.close();  // Close the database connection
            } catch (SQLException e) {
            }
        }
    }
}
