import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class HW3 extends JFrame{
	
	//define necessary component
	private JPanel upleft, downleft, upright, downright;
	private JPanel mcPanel, scPanel, attPanel;
	private JPanel reviewPanel, datePanel, starPanel, votePanel;
	private JPanel dateSubPanel1, dateSubPanel2;
	private JPanel starSubPanel1, starSubPanel2;
	private JPanel voteSubPanel1, voteSubPanel2;
	private JPanel resultPanel;
	private JScrollPane mcScroll, scScroll, attScroll, queryScroll, resultScroll;
	private JLabel fromLabel;
	private JLabel toLabel;
	private JLabel starLabel, starValueLabel;
	private JLabel voteLabel, voteValueLabel;
	private JTextField fromValue;
	private JTextField toValue;
	private JTextField starRange, starValue;
	private JTextField voteRange, voteValue;
	private String[] mcBoxes;
	private JTextArea queryLabel;
	private JButton execute;
	private JCheckBox isOr;
	private Connection conn;
	
	public HW3(Connection conn){
		super("Yelp GUI");
		this.conn = conn;
		mcBoxes = new String[]{ "Active Life", "Arts & Entertainment", "Automotive", "Car Rental", "Cafes", "Beauty & Spas", "Convenience Stores",
				                "Dentists", "Drugstores", "Department Stores", "Education", "Event Planning & Services", "Flowers & Gifts",
				                "Food", "Health & Medical", "Home Services", "Home & Garden", "Hospitals", "Hotels & Travel", "Hardware Stores",
				                "Grocery", "Medical Centers", "Nurseries & Gardening", "Nightlife", "Restaurants", "Shopping", "Transportation"};
		Container container = getContentPane();
		container.setLayout(new GridLayout(2,2));
		container.setPreferredSize(new Dimension(1500,1000));
		
		//set up major panels
		upleft = new JPanel(new GridLayout(0,3));
		upleft.setBorder(BorderFactory.createTitledBorder("Business Filter"));
		downleft = new JPanel(new GridLayout(6,0));
		downleft.setBorder(BorderFactory.createTitledBorder("User Filter"));
		upright = new JPanel(new BorderLayout());
		upright.setBorder(BorderFactory.createTitledBorder("Review Filter & Result"));
		downright = new JPanel(new BorderLayout());
		downright.setBorder(BorderFactory.createTitledBorder("Query and Execute"));
		
		//create isOr check box
		isOr = new JCheckBox("OR Between Attributes");
		//set up category panels
		mcPanel = new JPanel(new GridLayout(0,1));
		scPanel = new JPanel(new GridLayout(0,1));
		attPanel = new JPanel(new GridLayout(0,1));
		mcPanel.setBorder(BorderFactory.createTitledBorder("Main Category"));
		scPanel.setBorder(BorderFactory.createTitledBorder("Sub Category"));
		attPanel.setBorder(BorderFactory.createTitledBorder("Attributes"));
		
		//add queryLabel to downright panel
		queryLabel = new JTextArea("");
		queryLabel.setLineWrap(true);
		
		//put check boxes to mcPanel
		for(int i=0; i< mcBoxes.length; i++){
			JCheckBox box = new JCheckBox(mcBoxes[i]);
			mcPanel.add(box);
		}
		//add item listener to box
		for(int i=0; i<mcBoxes.length; i++){
			JCheckBox box = (JCheckBox)mcPanel.getComponent(i);
			box.addItemListener(
					new ItemListener(){
						@Override
						public void itemStateChanged(ItemEvent e){
							scPanel.removeAll();
							attPanel.removeAll();
							queryLabel.setText("");
							scPanel.updateUI();
							attPanel.updateUI();
							try{
								Statement stmt = conn.createStatement();
								String mcQuery = "SELECT name FROM category WHERE business_id IN (SELECT business_id FROM category WHERE ";
								boolean selected = false;
								for(int i=0; i<mcPanel.getComponentCount();i++){
									JCheckBox checkbox = (JCheckBox)mcPanel.getComponent(i);
									if(checkbox.isSelected()) {
										selected = true;
										mcQuery = mcQuery + "name = '" + checkbox.getText() + "'";
										mcQuery = mcQuery + " OR ";
									}
								}
								if(selected == true){
									mcQuery = mcQuery.substring(0, mcQuery.length() - 4);
									mcQuery = mcQuery + ")";
									for(int i=0;i<mcBoxes.length;i++){
										mcQuery = mcQuery + "\n AND name != '" + mcBoxes[i]+"'";
									}
									mcQuery = mcQuery + " GROUP BY name";
									queryLabel.setText(mcQuery);
									ResultSet rs = stmt.executeQuery(mcQuery);
									while(rs.next()){
										scPanel.add(new JCheckBox(rs.getString(1)));
									}
									for(int j=0;j<scPanel.getComponentCount();j++){
										JCheckBox checkbox = (JCheckBox)scPanel.getComponent(j);
										checkbox.addItemListener(new scListener(conn, mcPanel, scPanel, attPanel, queryLabel, isOr));
									}
									scPanel.updateUI();
									rs.close();
									stmt.close();
								}
							}catch(SQLException p){
								p.printStackTrace();
							}
						}
					});
		}
		//add category panels to JScrollPane
		mcScroll = new JScrollPane(mcPanel);
		scScroll = new JScrollPane(scPanel);
		attScroll = new JScrollPane(attPanel);
		queryScroll =new JScrollPane(queryLabel);
		
		//add scroll panes to up left
		upleft.add(mcScroll);
		upleft.add(scScroll);
		upleft.add(attScroll);
		downright.add(queryScroll, BorderLayout.CENTER);
		
		//add Review panels
		reviewPanel = new JPanel(new GridLayout(4,0));
		//create date components
		datePanel = new JPanel(new GridLayout(2,0));
		datePanel.setBorder(BorderFactory.createTitledBorder("Date"));
		dateSubPanel1 = new JPanel(new FlowLayout());
		dateSubPanel2 = new JPanel(new FlowLayout());
		fromLabel = new JLabel("From:");
		fromValue = new JTextField(8);
		dateSubPanel1.add(fromLabel);
		dateSubPanel1.add(fromValue);
		toLabel = new JLabel("    To:");
		toValue = new JTextField(8);
		dateSubPanel2.add(toLabel);
		dateSubPanel2.add(toValue);
		datePanel.add(dateSubPanel1);
		datePanel.add(dateSubPanel2);
		
		//create star components
		starPanel = new JPanel(new GridLayout(2,0));
		starPanel.setBorder(BorderFactory.createTitledBorder("Rating"));
		starSubPanel1 = new JPanel(new FlowLayout());
		starSubPanel2 = new JPanel(new FlowLayout());
		starLabel = new JLabel("Stars:");
		starRange = new JTextField(8);
		starSubPanel1.add(starLabel);
		starSubPanel1.add(starRange);
		starValueLabel = new JLabel("Value:");
		starValue = new JTextField(8);
		starSubPanel2.add(starValueLabel);
		starSubPanel2.add(starValue);
		starPanel.add(starSubPanel1);
		starPanel.add(starSubPanel2);
	
		//create vote components
		votePanel = new JPanel(new GridLayout(2,0));
		votePanel.setBorder(BorderFactory.createTitledBorder("Votes"));
		voteSubPanel1 = new JPanel(new FlowLayout());
		voteSubPanel2 = new JPanel(new FlowLayout());
		voteLabel = new JLabel("Range:");
		voteRange = new JTextField(8);
		voteSubPanel1.add(voteLabel);
		voteSubPanel1.add(voteRange);
		voteValueLabel = new JLabel("Value:");
		voteValue = new JTextField(8);
		voteSubPanel2.add(voteValueLabel);
		voteSubPanel2.add(voteValue);
		votePanel.add(voteSubPanel1);
		votePanel.add(voteSubPanel2);
		
				
		//pack review panels
		reviewPanel.add(isOr);
		reviewPanel.add(datePanel);
		reviewPanel.add(starPanel);
		reviewPanel.add(votePanel);
		
		//add reviewPanel to upright panel
		upright.add(reviewPanel, BorderLayout.WEST);
		
		//create result panel
		resultPanel = new JPanel(new GridLayout(0,1));
		//create result scroll pane
		resultScroll = new JScrollPane(resultPanel);
		resultScroll.setBorder(BorderFactory.createTitledBorder("Result"));
		//add result scroll pane to upright panel
		upright.add(resultScroll, BorderLayout.CENTER);
		
		//create execute query button
		execute = new JButton("Execute Query");
		execute.setForeground(Color.red);
		execute.addActionListener(
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						resultPanel.removeAll();
						resultPanel.updateUI();
						try {
							Statement stmt = conn.createStatement();
							String query = queryLabel.getText();
							ResultSet rs = stmt.executeQuery(query);
							if(rs.getMetaData().getColumnCount() == 5){
								while(rs.next()){
									String id = rs.getString(1);
									String name = rs.getString(2);
									String state = rs.getString(3);
									String city = rs.getString(4);
									double star    = rs.getDouble(5);
									String btnLabel = name + ", "+ state+", "+ city+", "+star;
									JButton temp = new JButton(btnLabel);
									temp.setActionCommand("biz"+id);
									temp.addActionListener(
											new ActionListener(){
												@Override
												public void actionPerformed(ActionEvent e) {
													// TODO Auto-generated method stub
													String s = e.getActionCommand();
													resultFrame resultframe = new resultFrame(conn, s);
													resultframe.pack();
													resultframe.setVisible(true);
												}
											});
									resultPanel.add(temp);
								}
							}else{
								while(rs.next()){
									String id = rs.getString(1);
									String name = rs.getString(2);
									String btnLabel = "ID: " + id + "   Name: " + name;
									JButton temp = new JButton(btnLabel);
									temp.setActionCommand("usr"+id);
									temp.addActionListener(
											new ActionListener(){
												@Override
												public void actionPerformed(ActionEvent e) {
													// TODO Auto-generated method stub
													String s = e.getActionCommand();
													resultFrame resultframe = new resultFrame(conn, s);
													resultframe.pack();
													resultframe.setVisible(true);
												}
											});
									resultPanel.add(temp);
								}
							}
							resultPanel.updateUI();
							rs.close();
							stmt.close();
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}	
				}
				);
		downright.add(execute, BorderLayout.SOUTH);
		
		
		//add Document listeners
		reviewListener listener = new reviewListener(mcPanel,scPanel,attPanel,isOr,fromValue,toValue
										,starRange,voteRange,starValue,voteValue,queryLabel);
		fromValue.getDocument().addDocumentListener(listener);
		toValue.getDocument().addDocumentListener(listener);
		starRange.getDocument().addDocumentListener(listener);
		voteRange.getDocument().addDocumentListener(listener);
		starValue.getDocument().addDocumentListener(listener);
		voteValue.getDocument().addDocumentListener(listener);
		
		
		//build user panel and components
		JPanel sincePanel, reviewcountPanel, friendcountPanel, avgstarPanel, votecountPanel;
		JTextField membersinceRange, membersinceValue;
		JTextField reviewcountRange, reviewcountValue;
		JTextField friendcountRange, friendcountValue;
		JTextField avgstarRange, avgstarValue;
		JTextField votecountRange, votecountValue;
		JCheckBox userOR;
		
		sincePanel = new JPanel(new FlowLayout());
		reviewcountPanel  = new JPanel(new FlowLayout());
		friendcountPanel  = new JPanel(new FlowLayout());
		avgstarPanel  = new JPanel(new FlowLayout());
		votecountPanel  = new JPanel(new FlowLayout());
		
		membersinceRange = new JTextField(3);
		membersinceValue = new JTextField(10);
		reviewcountRange = new JTextField(3);
		reviewcountValue = new JTextField(10);
		friendcountRange = new JTextField(3);
		friendcountValue = new JTextField(10);
		avgstarRange = new JTextField(3);
		avgstarValue = new JTextField(10);
		votecountRange = new JTextField(3);
		votecountValue = new JTextField(10);
		userOR = new JCheckBox("OR Between Attributes");
		
		sincePanel.add(new JLabel("Member Since:"));
		sincePanel.add(membersinceRange);
		sincePanel.add(new JLabel("Value:"));
		sincePanel.add(membersinceValue);
		reviewcountPanel.add(new JLabel("Review Count:"));
		reviewcountPanel.add(reviewcountRange);
		reviewcountPanel.add(new JLabel("Value:"));
		reviewcountPanel.add(reviewcountValue);
		friendcountPanel.add(new JLabel("Number of Friends:"));
		friendcountPanel.add(friendcountRange);
		friendcountPanel.add(new JLabel("Value:"));
		friendcountPanel.add(friendcountValue);
		avgstarPanel.add(new JLabel("Average Stars"));
		avgstarPanel.add(avgstarRange);
		avgstarPanel.add(new JLabel("Value:"));
		avgstarPanel.add(avgstarValue);
		votecountPanel.add(new JLabel("Number of Votes:"));
		votecountPanel.add(votecountRange);
		votecountPanel.add(new JLabel("Value: "));
		votecountPanel.add(votecountValue);
		
		//add listener to text fields
		userListener userlistener = new userListener(membersinceRange, membersinceValue, reviewcountRange, reviewcountValue
					   ,friendcountRange, friendcountValue, avgstarRange, avgstarValue
					   ,votecountRange, votecountValue, userOR, queryLabel);
		membersinceRange.getDocument().addDocumentListener(userlistener);
		membersinceValue.getDocument().addDocumentListener(userlistener);
		reviewcountRange.getDocument().addDocumentListener(userlistener);
		reviewcountValue.getDocument().addDocumentListener(userlistener);
		friendcountRange.getDocument().addDocumentListener(userlistener);
		friendcountValue.getDocument().addDocumentListener(userlistener);
		avgstarRange.getDocument().addDocumentListener(userlistener);
		avgstarValue.getDocument().addDocumentListener(userlistener);
		votecountRange.getDocument().addDocumentListener(userlistener);
		votecountValue.getDocument().addDocumentListener(userlistener);
		
		downleft.add(sincePanel);
		downleft.add(reviewcountPanel);
		downleft.add(friendcountPanel);
		downleft.add(avgstarPanel);
		downleft.add(votecountPanel);
		downleft.add(userOR);
		//add panels to container
		container.add(upleft);
		container.add(upright);
		container.add(downleft);
		container.add(downright);
		pack();
		setVisible(true);
	}
	
	public static void createAndShowGUI(){
		Connection conn = connect();
		HW3 app = new HW3(conn);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args){
		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				createAndShowGUI();
			}
		});
	}
	
	private static Connection connect(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Class.forName("oracle.jdbc.OracleDriver");
		}catch (ClassNotFoundException cnfe) { 
			System.out.println("Error loading driver: " + cnfe);
		}
		Connection conn = null;
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/coen280?"+
												"user=root&password=123QWE");
			System.out.println("It works");
		}catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return conn;
	}
}

class scListener implements ItemListener{
	Connection conn;
	JPanel mcPanel, scPanel, attPanel;
	JTextArea queryLabel;
	JCheckBox isOr;
	
	public scListener(Connection conn, JPanel mcPanel, JPanel scPanel, JPanel attPanel, JTextArea queryLabel, JCheckBox isOr){
		this.conn = conn;
		this.mcPanel = mcPanel;
		this.scPanel = scPanel;
		this.attPanel = attPanel;
		this.queryLabel = queryLabel;
		this.isOr = isOr;
	}
	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		attPanel.removeAll();
		attPanel.updateUI();
		try{
			Statement stmt = conn.createStatement();
			String attQuery = "SELECT name FROM attributes WHERE "+
							  "business_id IN (SELECT business_id FROM category WHERE ";
			for(int i=0; i<mcPanel.getComponentCount(); i++){
				JCheckBox checkbox = (JCheckBox)mcPanel.getComponent(i);
				if(checkbox.isSelected()){
					attQuery = attQuery + " name = '" + checkbox.getText() + "' OR";
				}	
			}
			attQuery = attQuery.substring(0, attQuery.length()-3);
			attQuery = attQuery + ")";
			boolean selected = false;
			for(int i=0; i<scPanel.getComponentCount();i++){
				JCheckBox checkbox = (JCheckBox)scPanel.getComponent(i);
				if(checkbox.isSelected()){
					selected = true;
				}	
			}
			if(selected == true){
				attQuery = attQuery + "AND business_id IN (SELECT business_id FROM category WHERE ";
				for(int i=0; i<scPanel.getComponentCount();i++){
					JCheckBox checkbox = (JCheckBox)scPanel.getComponent(i);
					if(checkbox.isSelected()){
						attQuery = attQuery + " name = '" + checkbox.getText() + "' OR";
					}
				}
				attQuery = attQuery.substring(0, attQuery.length()-3);
				attQuery = attQuery + ") GROUP BY name";
				queryLabel.setText(attQuery);
				ResultSet rs = stmt.executeQuery(attQuery);
				while(rs.next()){
					attPanel.add(new JCheckBox(rs.getString(1)));
				}
				for(int i=0;i<attPanel.getComponentCount();i++){
					JCheckBox checkbox = (JCheckBox)attPanel.getComponent(i);
					checkbox.addItemListener(new attListener(conn,attPanel, attQuery, queryLabel, isOr));
				}
				attPanel.updateUI();
				rs.close();
				stmt.close();
			}
		}catch(SQLException p){
			p.printStackTrace();
		}
	}
}

class attListener implements ItemListener{
	Connection conn;
	JPanel attPanel;
	String attQuery;
	JTextArea queryLabel;
	JCheckBox isOr;
	public attListener(Connection conn, JPanel attPanel, String attQuery, JTextArea queryLabel, JCheckBox isOr){
		this.conn = conn;
		this.attPanel = attPanel;
		this.attQuery = attQuery;
		this.queryLabel = queryLabel;
		this.isOr = isOr;
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		String relation;
		if(isOr.isSelected()) relation = " OR";
		else relation = "AND";
		String bizQuery = "SELECT business_id,name,state,city,stars FROM business WHERE " +
							"business_id IN (SELECT business_id FROM attributes WHERE ";
		String queryFinisher = attQuery.substring(34, attQuery.length()-13);
		boolean selected = false;
		for(int i=0; i<attPanel.getComponentCount(); i++){
			JCheckBox checkbox = (JCheckBox)attPanel.getComponent(i);
			if(checkbox.isSelected()) {
				selected = true;
				bizQuery = bizQuery + " name = '" + checkbox.getText() +"'" + relation;
			}
		}
		if(selected){
			bizQuery = bizQuery.substring(0, bizQuery.length()-3) + ") AND ";
		}else{
			bizQuery = bizQuery.substring(0, bizQuery.length()- 57);
		}
		bizQuery = bizQuery + queryFinisher;
		queryLabel.setText(bizQuery);
	}	
}

class reviewListener implements DocumentListener{
	JPanel mcPanel, scPanel, attPanel;
	JCheckBox isOr;
	JTextField fromValue, toValue;
	JTextField starRange, voteRange;
	JTextField starValue, voteValue;
	JTextArea queryLabel;
	
	public reviewListener(JPanel mcPanel, JPanel scPanel, JPanel attPanel, JCheckBox isOr, 
						  JTextField fromValue, JTextField toValue, JTextField starRange, 
						  JTextField voteRange, JTextField starValue, JTextField voteValue, JTextArea queryLabel){
		this.mcPanel = mcPanel;
		this.scPanel = scPanel;
		this.attPanel = attPanel;
		this.isOr = isOr;
		this.fromValue = fromValue;
		this.toValue = toValue;
		this.starRange = starRange;
		this.voteRange = voteRange;
		this.starValue = starValue;
		this.voteValue = voteValue;
		this.queryLabel = queryLabel;
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		changeQuery();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		changeQuery();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		changeQuery();
	}
	
	public void changeQuery(){
		String relation;
		if(isOr.isSelected()) 
			relation = " OR";
		else 
			relation = "AND";
		String bizQuery = "SELECT business_id,name,state,city,stars FROM business WHERE " +
				"business_id IN (SELECT business_id FROM category WHERE ";
		for(int i=0; i<mcPanel.getComponentCount(); i++){
			JCheckBox checkbox = (JCheckBox)mcPanel.getComponent(i);
			if(checkbox.isSelected()){
				bizQuery = bizQuery + " name = '" + checkbox.getText() + "' OR";
			}	
		}
		bizQuery = bizQuery.substring(0, bizQuery.length()-3);
		bizQuery = bizQuery + ") AND business_id IN (SELECT business_id FROM category WHERE ";
		for(int i=0; i<scPanel.getComponentCount();i++){
			JCheckBox checkbox = (JCheckBox)scPanel.getComponent(i);
			if(checkbox.isSelected()){
				bizQuery = bizQuery + " name = '" + checkbox.getText() + "' OR";
			}
		}
		bizQuery = bizQuery.substring(0, bizQuery.length()-3);
		bizQuery = bizQuery + ") AND business_id IN (SELECT business_id FROM attributes WHERE ";
		for(int i=0; i<attPanel.getComponentCount(); i++){
			JCheckBox checkbox = (JCheckBox)attPanel.getComponent(i);
			if(checkbox.isSelected()) {
				bizQuery = bizQuery + " name = '" + checkbox.getText() +"'" + relation;
			}
		}
		if(!fromValue.getText().isEmpty() || !toValue.getText().isEmpty() || (!voteRange.getText().isEmpty() && !voteValue.getText().isEmpty())){
			bizQuery = bizQuery.substring(0, bizQuery.length()-3);
			bizQuery = bizQuery + ") AND business_id IN (SELECT business_id from review WHERE";
			if(!fromValue.getText().isEmpty()) bizQuery = bizQuery + " entry_date >= date'" + fromValue.getText() + "' AND";
			if(!toValue.getText().isEmpty())   bizQuery = bizQuery + " entry_date <= date'" + toValue.getText()   + "' AND";
			if(!voteRange.getText().isEmpty() && !voteValue.getText().isEmpty())
				bizQuery = bizQuery + " vote_count " + voteRange.getText() + voteValue.getText() + " AND";
			if(!starRange.getText().isEmpty() && !starValue.getText().isEmpty()){
				bizQuery = bizQuery.substring(0, bizQuery.length()-3) + ") AND business_id IN (SELECT business_id FROM business WHERE";
				bizQuery = bizQuery + " stars " + starRange.getText() + starValue.getText() + ")";
			}else{
				bizQuery = bizQuery.substring(0, bizQuery.length()-3) + ")";
			}
		}else{
			if(!starRange.getText().isEmpty() && !starValue.getText().isEmpty()){
				bizQuery = bizQuery.substring(0, bizQuery.length()-3) + ") AND business_id IN (SELECT business_id FROM business WHERE";
				bizQuery = bizQuery + " stars " + starRange.getText() + starValue.getText() + ")";
			}else{
				bizQuery = bizQuery.substring(0, bizQuery.length()-3) + ")";
			}
		}
		queryLabel.setText(bizQuery);
	}
}

class userListener implements DocumentListener{
	
	JTextField membersinceRange, membersinceValue;
	JTextField reviewcountRange, reviewcountValue;
	JTextField friendcountRange, friendcountValue;
	JTextField avgstarRange, avgstarValue;
	JTextField votecountRange, votecountValue;
	JCheckBox userOR;
	JTextArea queryLabel;
	
	public userListener(JTextField membersinceRange, JTextField membersinceValue, JTextField reviewcountRange,JTextField reviewcountValue
					   ,JTextField friendcountRange, JTextField friendcountValue, JTextField avgstarRange, JTextField avgstarValue
					   ,JTextField votecountRange, JTextField votecountValue, JCheckBox userOR, JTextArea queryLabel){
		this.membersinceRange = membersinceRange;
		this.membersinceValue = membersinceValue;
		this.reviewcountRange = reviewcountRange;
		this.reviewcountValue = reviewcountValue;
		this.friendcountRange = friendcountRange;
		this.friendcountValue = friendcountValue;
		this.avgstarRange     = avgstarRange;
		this.avgstarValue     = avgstarValue;
		this.votecountRange   = votecountRange;
		this.votecountValue   = votecountValue;
		this.userOR           = userOR;
		this.queryLabel       = queryLabel;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		buildQuery();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		buildQuery();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		buildQuery();
	}
	
	public void buildQuery(){
		String relation;
		if(userOR.isSelected())
			relation = " OR ";
		else
			relation = " AND";
		if( (!membersinceRange.getText().isEmpty() && !membersinceValue.getText().isEmpty()) ||
		    (!reviewcountRange.getText().isEmpty() && !reviewcountValue.getText().isEmpty()) ||
		    (!friendcountRange.getText().isEmpty() && !friendcountValue.getText().isEmpty()) ||
		    (!avgstarRange.getText().isEmpty()     && !avgstarValue.getText().isEmpty())     ||
		    (!votecountRange.getText().isEmpty()   && !votecountValue.getText().isEmpty()) ){
			
			String query = "SELECT user_id,name FROM yelp_user WHERE ";
			if( !membersinceRange.getText().isEmpty() && !membersinceValue.getText().isEmpty()){
				query = query + "yelping_since "+ membersinceRange.getText() + " date'"+ membersinceValue.getText() + "'" + relation;
			}
			if( !reviewcountRange.getText().isEmpty() && !reviewcountValue.getText().isEmpty()){
				query = query + " user_id IN ( SELECT user_id  FROM (SELECT user_id, COUNT(review_id) AS count FROM review GROUP BY user_id) AS review_count WHERE " + 
							"count "+ reviewcountRange.getText()+" " + reviewcountValue.getText() + ")" + relation;
			}
			if( !friendcountRange.getText().isEmpty() && !friendcountValue.getText().isEmpty()){
				query = query + " user_id IN ( SELECT user_id FROM (SELECT user_1 AS user_id,COUNT(user_2) AS count FROM friends GROUP BY user_id) AS friend_count WHERE " +
							"count " + friendcountRange.getText() + " " + friendcountValue.getText() + ")" + relation;
			}
			if(!avgstarRange.getText().isEmpty()     && !avgstarValue.getText().isEmpty()) {
				query = query + " average_stars " + " " + avgstarRange.getText() + " "+ avgstarValue.getText() + relation;
			}
			if(!votecountRange.getText().isEmpty()   && !votecountValue.getText().isEmpty()){
				query = query + " user_id IN (SELECT user_id FROM (SELECT user_id, SUM(vote_count) AS vote_count FROM review GROUP BY user_id) AS vote WHERE " +
							"vote_count " + votecountRange.getText() + " " + votecountValue.getText() + ")" + relation;
			}
			queryLabel.setText(query.substring(0, query.length()-3));
		}
	}
}

class resultFrame extends JFrame{
	JScrollPane result;
	JPanel resultPanel;
	public resultFrame(Connection conn, String s){
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		container.setPreferredSize(new Dimension(500,200));
		resultPanel = new JPanel(new GridLayout(0,1));
		result = new JScrollPane(resultPanel);
		try{
			String head = s.substring(0,3);
			s = s.substring(3);
			Statement stmt = conn.createStatement();
			Statement stmt2 = conn.createStatement();
			if(head.equals("biz")){
				ResultSet rs1 = stmt.executeQuery("SELECT user_id,stars, vote_count FROM review WHERE business_id = '" + s + "'");
				while(rs1.next()){
					String user_id = rs1.getString(1);
					int stars = rs1.getInt(2);
					int vote_count = rs1.getInt(3);
					ResultSet rs2 = stmt2.executeQuery("SELECT name FROM yelp_user WHERE user_id = '"+user_id+"'");
					String name = "User";
					if(rs2.next()) name = rs2.getString(1);
					JPanel entry = new JPanel(new FlowLayout());
					entry.add(new JLabel("Author Name: "+name+",   Star Given: " + stars + ",   Votes Got: " + vote_count));
					resultPanel.add(entry);
					resultPanel.setBorder(BorderFactory.createTitledBorder("Reviews of Selected Business"));
				}
			}else if(head.equals("usr")){
				ResultSet rs1 = stmt.executeQuery("SELECT review_id, stars, entry_date FROM review WHERE user_id = '" + s + "'");
				while(rs1.next()){
					String review_id = rs1.getString(1);
					int stars = rs1.getInt(2);
					String entry_date = rs1.getString(3);
					JPanel entry = new JPanel(new FlowLayout());
					entry.add(new JLabel("Review ID: "+review_id + "  Star: " + stars + "   entry_date: "+ entry_date));
					resultPanel.add(entry);
					resultPanel.setBorder(BorderFactory.createTitledBorder("Reviews of Selected User"));
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		container.add(result, BorderLayout.CENTER);
	}
}
