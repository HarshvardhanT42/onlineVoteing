import java.sql.*;
import java.util.Scanner;

public class onlinevoting {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Database connection variables
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root"; // default MySQL username
        String password = ""; // default MySQL password; set accordingly
        Connection connection = null;

        try {
            // Establish database connection
            connection = DriverManager.getConnection(url, user, password);

            // Ask for user details
            System.out.print("Please enter your name: ");
            String userName = input.nextLine();

            System.out.print("Please enter your age: ");
            int userAge = input.nextInt();

            if (userAge < 18) {
                System.out.println("I'm sorry, you are not eligible to vote.");
                return;
            }

            // Check if the user has already voted
            PreparedStatement checkVoter = connection.prepareStatement(
                "SELECT * FROM voters_db.voters WHERE name = ?"
            );
            checkVoter.setString(1, userName);
            ResultSet resultSet = checkVoter.executeQuery();

            if (resultSet.next()) {
                System.out.println("You have already voted. Thank you!");
                return;
            }

            // Present candidates for voting
            System.out.println("Please select a candidate to vote for:");
            System.out.println("1. Candidate 1");
            System.out.println("2. Candidate 2");
            int userSelection = input.nextInt();

            String candidateName = userSelection == 1 ? "Candidate 1" : "Candidate 2";
            
            // Increment vote count for the selected candidate
            PreparedStatement updateVote = connection.prepareStatement(
                "UPDATE candidates_db.candidates SET votes = votes + 1 WHERE name = ?"
            );
            updateVote.setString(1, candidateName);
            updateVote.executeUpdate();

            // Add the user to the voters list
            PreparedStatement addVoter = connection.prepareStatement(
                "INSERT INTO voters_db.voters (name, age, voted) VALUES (?, ?, ?)"
            );
            addVoter.setString(1, userName);
            addVoter.setInt(2, userAge);
            addVoter.setBoolean(3, true);
            addVoter.executeUpdate();

            System.out.println("Thank you for voting, " + userName + "!");

            // Display final vote count (optional)
            Statement statement = connection.createStatement();
            ResultSet votesResult = statement.executeQuery(
                "SELECT name, votes FROM candidates_db.candidates ORDER BY votes DESC"
            );
            System.out.println("Vote count:");
            while (votesResult.next()) {
                System.out.println(votesResult.getString("name") + ": " + votesResult.getInt("votes") + " votes");
            }

        } catch (SQLException e) {
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
            }
        }
    }
}
