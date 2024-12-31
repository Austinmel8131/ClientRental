/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package clientrental;

import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author macbook
 */
public class ClientRental {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        
        Scanner myKB = new Scanner(System.in);
        try { 
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/ClientRental","root","Password");

            System.out.println("Connected to DB..."); 
            
            Scanner scanner = new Scanner(System.in);
            int Options;
            
            do {
                // Display menu
                System.out.println("\nMenu:");
                System.out.println("1. View all clients and their properties");
                System.out.println("2. Identify owners who own multiple properties");
                System.out.println("3. Find the client who pays the highest monthly rent");
                System.out.println("4. Count the total number of properties owned by each owner");
                System.out.println("5. List all clients along with the total rent they pay annually, sorted in ascending order");
                System.out.println("6. Exit");
                System.out.print("Enter your choice of Option from 1 to 6: ");

                // Get user input
                while (!scanner.hasNextInt()) {
                    System.out.print("Invalid input. Enter a number: ");
                    scanner.next();
                }
                Options = scanner.nextInt();

                // Handle menu options
                switch (Options) {
                    case 1:
                        viewClientsAndProperties(con);
                        break;
                    case 2:
                        identifyOwnersWithMultipleProperties(con);
                        break;
                    case 3:
                        findHighestPayingClient(con);
                        break;
                    case 4:
                        countPropertiesByOwner(con);
                        break;
                    case 5:
                        listClientsByAnnualRent(con);
                        break;
                    case 6:
                        System.out.println("Exiting program. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } while (Options != 5);

        } catch (SQLException e) {
            System.err.println("Error!!! connecting to the database: " + e.getMessage());
        } catch (ClassNotFoundException ex) {
            System.out.println("SQL Error --> ");
            //Logger.getLogger(ClientRental.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    // Query 1: View all clients and their properties
    private static void viewClientsAndProperties(Connection con) {
        String query = "SELECT Clients.Client_No, Clients.Client_Name, Properties.PropertyNo, Properties.Property_Address " +
                       "FROM Clients JOIN Rentals ON Clients.Client_No = Rentals.Client_No " +
                       "JOIN Properties ON Rentals.PropertyNo = Properties.PropertyNo";
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\nClients and their Properties:");
            while (rs.next()) {
                System.out.printf("Client: %s (%s), Property: %s - %s\n", 
                                  rs.getString("Client_Name"), rs.getString("Client_No"),
                                  rs.getString("PropertyNo"), rs.getString("Property_Address"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching clients and properties: " + e.getMessage());
        }
    }

  // Query 2: Identify owners who own multiple properties
    private static void identifyOwnersWithMultipleProperties(Connection con) {
        String query = "SELECT Owners.Owner_No, Owners.Owner_Name " +
                       "FROM Owners JOIN Properties ON Owners.Owner_No = Properties.Owner_No " +
                       "GROUP BY Owners.Owner_No, Owners.Owner_Name " +
                       "HAVING COUNT(Properties.PropertyNo) > 1";

        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\nOwners with multiple properties:");
            while (rs.next()) {
                System.out.printf("Owner: %s (%s)\n", rs.getString("Owner_Name"), rs.getString("Owner_No"));
            }
        } catch (SQLException e) {
            System.err.println("Error identifying owners with multiple properties: " + e.getMessage());
        }
    }


    // Query 3: Find the client who pays the highest monthly rent
    private static void findHighestPayingClient(Connection con) {
        String query = "SELECT Clients.Client_No, Clients.Client_Name, MAX(Properties.Monthly_Rent) AS Highest_Monthly_Rent " +
                       "FROM Clients JOIN Rentals ON Clients.Client_No = Rentals.Client_No " +
                       "JOIN Properties ON Rentals.PropertyNo = Properties.PropertyNo " +
                       "GROUP BY Clients.Client_No, Clients.Client_Name " +
                       "ORDER BY Highest_Monthly_Rent DESC LIMIT 1";

        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                System.out.printf("\nClient: %s (%s) pays the highest monthly rent: %.2f\n", 
                                  rs.getString("Client_Name"), rs.getString("Client_No"), rs.getDouble("Highest_Monthly_Rent"));
            } else {
                System.out.println("No data found.");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching highest paying client: " + e.getMessage());
        }
    }


    // Query 4: Count the total number of properties owned by each owner
    private static void countPropertiesByOwner(Connection con) {
        String query = "SELECT Owners.Owner_No, Owners.Owner_Name, COUNT(Properties.PropertyNo) AS Total_Properties " +
                       "FROM Owners JOIN Properties ON Owners.Owner_No = Properties.Owner_No " +
                       "GROUP BY Owners.Owner_No, Owners.Owner_Name";

        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\nTotal number of properties owned by each owner:");
            while (rs.next()) {
                System.out.printf("Owner: %s (%s), Total Properties: %d\n", 
                                  rs.getString("Owner_Name"), rs.getString("Owner_No"), rs.getInt("Total_Properties"));
            }
        } catch (SQLException e) {
            System.err.println("Error counting properties by owner: " + e.getMessage());
        }
    }
    
        // Query 5: List all clients along with the total rent they pay annually, sorted in ascending order
    private static void listClientsByAnnualRent(Connection con) {
        String query = "SELECT Clients.Client_No, Clients.Client_Name, SUM(Properties.Monthly_Rent * 12) AS Total_Annual_Rent " +
                       "FROM Clients JOIN Rentals ON Clients.Client_No = Rentals.Client_No " +
                       "JOIN Properties ON Rentals.PropertyNo = Properties.PropertyNo " +
                       "GROUP BY Clients.Client_No, Clients.Client_Name " +
                       "ORDER BY Total_Annual_Rent ASC";

        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\nClients and their Total Annual Rent (Sorted in Ascending Order):");
            while (rs.next()) {
                System.out.printf("Client: %s (%s), Total Annual Rent: %.2f\n", 
                                  rs.getString("Client_Name"), rs.getString("Client_No"), rs.getDouble("Total_Annual_Rent"));
            }
        } catch (SQLException e) {
            System.err.println("Error listing clients by annual rent: " + e.getMessage());
        }
    }
        
  
    }

//  catch (ClassNotFoundException  e){
            
          //  System.out.println("error!!! Connecting to Database " + e.getMessage());
       // } catch (SQLException ex) {
       //     System.out.println("SQL Error --> ");
         //  Logger.getLogger(ClientRental.class.getName()).log(Level.SEVERE, null, ex);
        
        
        
    
    

