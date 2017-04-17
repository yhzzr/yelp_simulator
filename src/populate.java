import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.json.*;
import org.json.simple.parser.*;

public class populate{
	
	public static void main(String[] args){
		try{
			Connection conn = connect();
			if(conn != null){
				popUser(args[0], conn);
				popBiz(args[1], conn);
				popReview(args[2], conn);
				conn.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	private static Connection connect(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException cnfe) { 
			System.out.println("Error loading driver: " + cnfe);
		}
			
		Connection conn = null;
	
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/user?"+
												"user=root&password=password");
			System.out.println("It works");
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return conn;
	}

	private static void popUser(String filename, Connection conn){
		final String SINCE = "yelping_since";
		final String NAME = "name";
		final String USER_ID = "user_id";
		final String AVG_STARS = "average_stars";
		final String FRIENDS = "friends";
		try{
			Statement stmt = conn.createStatement();
			stmt.execute("DELETE FROM review");
			stmt.execute("DELETE FROM attributes");
			stmt.execute("DELETE FROM category");
			stmt.execute("DELETE FROM business");
			stmt.execute("DELETE FROM friends");
			stmt.execute("DELETE FROM yelp_user");
			FileInputStream fis = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			while( (line = br.readLine()) != null){
				JSONObject userObj = new JSONObject(line);
				String this_date = userObj.getString(SINCE)+"-01";
				String this_name = userObj.getString(NAME);
				this_name = this_name.replaceAll("[^a-zA-Z]","");
				String this_id   = userObj.getString(USER_ID);
				double this_star = userObj.getDouble(AVG_STARS);
				String userInsert = "INSERT INTO yelp_user (yelping_since, name, user_id, average_stars)VALUES ('" +
									this_date + "','"+
									this_name + "','"+
									this_id  +  "','"+
									this_star + "')";
				stmt.executeUpdate(userInsert);
			}
			fis.getChannel().position(0);
			br = new BufferedReader(new InputStreamReader(fis));
			while( (line = br.readLine()) != null){
				JSONObject userObj = new JSONObject(line);
				String this_id = userObj.getString(USER_ID);
				JSONArray friendsArray = userObj.getJSONArray(FRIENDS);
				for(int i=0;i<friendsArray.length();i++){
					try{
						String friendInsert = "INSERT INTO friends (user_1, user_2) VALUES ('"+
									  	this_id + "','"+
									  	friendsArray.getString(i) + "')";
						stmt.executeUpdate(friendInsert);
					}catch(SQLException p){
					}
				}
			}
			br.close();
			fis.close();
			stmt.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void popBiz(String filename, Connection conn){
		final String BUSINESS_ID = "business_id";
		final String CITY        = "city";
		final String STATE       = "state";
		final String NAME        = "name";
		final String STARS       = "stars";
		final String ATTRIBUTES  = "attributes";
		final String CATEGORIES  = "categories";
		try{
			Statement stmt = conn.createStatement();
			stmt.execute("DELETE FROM review");
			stmt.execute("DELETE FROM attributes");
			stmt.execute("DELETE FROM category");
			stmt.execute("DELETE FROM business");
			FileInputStream fis = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			while( (line = br.readLine()) != null){
				JSONObject businessObj = new JSONObject(line);
				String this_id 	 	= businessObj.getString(BUSINESS_ID);
				String this_city 	= businessObj.getString(CITY);
				String this_state	= businessObj.getString(STATE);
				String this_name 	= businessObj.getString(NAME);
				this_name = this_name.replaceAll("\'", "\"");
				double this_stars   = businessObj.getDouble(STARS);
				JSONObject this_attributes = businessObj.getJSONObject(ATTRIBUTES);
				JSONArray this_categories = businessObj.getJSONArray(CATEGORIES);
				//create insert for business table
				String businessInsert = "INSERT INTO business (business_id, city, state, name, stars) VALUES ('" +
										this_id   	+ "','" +
										this_city 	+ "','" +
										this_state	+ "','" +
										this_name   + "', " +
										this_stars  + ")";
				stmt.executeUpdate(businessInsert);
				//create insert for category table
				for(int i=0; i< this_categories.length(); i++){
					try{
						String categoryInsert = "INSERT INTO category (business_id, name) VALUES ('" +
											this_id + "','" + 
											this_categories.getString(i) + "')";
						stmt.executeUpdate(categoryInsert);
					}catch(SQLException e){
					}
				}
				//iterate through attributes and create insert for attributes table
				try{
					addAttributes(this_attributes, stmt, this_id);
				}catch(SQLException e){
					e.printStackTrace();
				}
			}
			br.close();
			fis.close();
			stmt.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	private static void popReview(String filename, Connection conn){
		final String USER_ID 	= "user_id";
		final String REVIEW_ID 	= "review_id";
		final String STARS      = "stars";
		final String DATE       = "date";
		final String TEXT       = "text";
		final String BUSINESS_ID= "business_id";
		final String VOTES      = "votes";
		final String USEFUL     = "useful";
		final String FUNNY      = "funny";
		final String COOL       = "cool";
		try{
			Statement stmt = conn.createStatement();
			stmt.execute("DELETE FROM review");
			PreparedStatement prestmt = conn.prepareStatement("INSERT INTO review (user_id, review_id, stars, entry_date, review_text, business_id, vote_count) VALUES (" +
										"?" 	+ "," +
										"?"     + "," +
										"?"     + "," +
										"?"     + "," +
										"?"     + "," + 
										"?"     + "," +
										"?"     + ")");
			FileInputStream fis = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			while ( (line = br.readLine()) != null){
				JSONObject reviewObj = new JSONObject(line);
				String this_user 		= reviewObj.getString(USER_ID);
				String this_review		= reviewObj.getString(REVIEW_ID);
				int    this_star        = reviewObj.getInt(STARS);
				String this_date  		= reviewObj.getString(DATE);
				String this_text 		= reviewObj.getString(TEXT);
				String this_business    = reviewObj.getString(BUSINESS_ID);
				int    this_vote_count  = 0;
				JSONObject this_votes   = reviewObj.getJSONObject(VOTES);
				int useful_count = this_votes.getInt(USEFUL);
				int funny_count  = this_votes.getInt(FUNNY);
				int cool_count   = this_votes.getInt(COOL);
				this_vote_count  = useful_count + funny_count + cool_count;

				//modify text to drop special characters
				this_text = this_text.replaceAll("\'", "\"");
				this_text = this_text.replaceAll("\\\\", "");
				//create insert for review table
				/*String reviewInsert = "INSERT INTO review (user_id, review_id, stars, entry_date, review_text, business_id) VALUES ('" +
										this_user 	+ "','" +
										this_review + "', " +
										this_star   + ", '" +
										this_date   + "','" +
										this_text   + "','" + 
										this_business + "')";*/
				//prepare statement for inserting review
				prestmt.setString(1, this_user);
				prestmt.setString(2, this_review);
				prestmt.setInt(3, this_star);
				prestmt.setString(4, this_date);
				prestmt.setString(5, this_text);
				prestmt.setString(6, this_business);
				prestmt.setInt(7, this_vote_count);
				//stmt.executeUpdate(reviewInsert)
				prestmt.executeUpdate();
			}
			br.close();
			fis.close();
			stmt.close();
			prestmt.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//The method is used to iterate throught JSONObject and capture attributes;
	private static void addAttributes(JSONObject this_attributes, Statement stmt, String this_id) throws SQLException{
		Iterator<String> keyIterator = this_attributes.keys();
		while(keyIterator.hasNext()){
			String key = keyIterator.next();
			Object temp = this_attributes.get(key);
			if( temp instanceof JSONObject){
				addAttributes((JSONObject)temp, stmt, this_id);
			}else{
				key = key + " - " + temp.toString();
				String attributesInsert = "INSERT INTO attributes (business_id, name) VALUES ('" +
										   this_id + "','" +
										   key 	   + "')";
				stmt.executeUpdate(attributesInsert);
			}
		}
	}
}

