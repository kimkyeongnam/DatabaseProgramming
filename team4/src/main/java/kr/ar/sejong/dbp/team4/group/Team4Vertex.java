package kr.ar.sejong.dbp.team4.group;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;

import kr.ar.sejong.dbp.team4.Direction;
import kr.ar.sejong.dbp.team4.Edge;
import kr.ar.sejong.dbp.team4.Vertex;

public class Team4Vertex implements Vertex {
    
	private Connection connection;
	private Statement stmt;
	private ResultSet rs;
	private Team4Graph graph;
    //예: string 형태의 고유 아이디, '|' 사용 금지
    private int id;
    private String property = null;

    Team4Vertex(final int id,final Team4Graph graph) throws SQLException{
        //15011137 김지수
		this.id = id;
		this.graph = graph;
		
		connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306" , "root" , "0000");
		stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_UPDATABLE);
		stmt.executeUpdate("USE Team4Graph");
	}
    
    @Override
    public Iterable<Edge> getEdges(Direction direction, String... labels) {
        //15011137 김지수
    	if(direction.equals(Direction.OUT)) {
    		return this.getOutEdges(labels);
    	} else if(direction.equals(Direction.IN)) {
    			return this.getInEdges(labels);
    	}
        return null;
    }
    
    private Iterable<Edge> getInEdges(String... labels){
        //15011137 김지수
    	List<Edge> totalEdges = new ArrayList<Edge>();	
    	String label;
    	try {
			ResultSet set = stmt.executeQuery("SELECT * FROM edge WHERE destination = "+this.id+" AND label = \"label\";");
			while(set.next()) {
				Vertex sVertex = new Team4Vertex(set.getInt(1),graph);
				Vertex dVertex = new Team4Vertex(set.getInt(2),graph);
				label = set.getString(3);
				Edge newEdge = new Team4Edge(sVertex,dVertex,label,graph);
				totalEdges.add(newEdge);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return totalEdges;
    }
    
    private Iterable<Edge> getOutEdges(String... labels){
        //15011137 김지수
    	List<Edge> totalEdges = new ArrayList<Edge>();
    	String label;
    	try {
			ResultSet set = stmt.executeQuery("SELECT * FROM edge WHERE source = "+this.id+" AND label = \"label\";");
			while(set.next()) {
				Vertex sVertex = new Team4Vertex(set.getInt(1),graph);
				Vertex dVertex = new Team4Vertex(set.getInt(2),graph);
				label = set.getString(3);
				Edge newEdge = new Team4Edge(sVertex,dVertex,label,graph);
				totalEdges.add(newEdge);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return totalEdges;
    }
    
    @Override
    public Iterable<Vertex> getVertices(Direction direction, String... labels) {
        //15011137 김지수
    	List<Vertex> totalVertices = new ArrayList<Vertex>();
    	
    	int newId;
    	if(direction.equals(Direction.OUT)) {
			try {
				ResultSet set = stmt.executeQuery("SELECT * FROM edge WHERE source = "+this.id+" AND label = \"label\";");
				while(set.next()) {
					newId = set.getInt(2);
					Vertex newVertex = new Team4Vertex(newId, graph);
					totalVertices.add(newVertex);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}else {
    		try {
				ResultSet set = stmt.executeQuery("SELECT * FROM edge WHERE destination = "+this.id+" AND label = \"label\";");
				while(set.next()) {
					newId = set.getInt(1);
					Vertex newVertex = new Team4Vertex(newId, graph);
					totalVertices.add(newVertex);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    	
    	 
        return totalVertices;
    }

    @Override
    public Edge addEdge(String label, Vertex inVertex) {
        //15011137 김지수
        return this.graph.addEdge(this, inVertex, label);
    }

    @Override
    public Object getProperty(String key) {
        //15011137 김지수
    	try{
    		ResultSet set = stmt.executeQuery("SELECT JSON_VALUE(properties, '$."+key+"') FROM Vertex WHERE id = "+this.id+";");
    		set.next();
    		property = set.getString(1);
    		return property;
    	} catch(Exception e){
    		return null;
    	}
    }

    @Override
    public Set<String> getPropertyKeys() {
        //15011137 김지수
    	try {
			ResultSet set = stmt.executeQuery("SELECT JSON_KEYS(properties) FROM Vertex WHERE id = "+this.id+";");
			set.next();
			String propKeys = set.getString(1);
			JSONArray arr = new JSONArray(propKeys);
			
			HashSet<String> returnKeys = new HashSet<String>();
			
			for(int i=0;i<arr.length();i++) {
				returnKeys.add(arr.getString(i));
			}
			return returnKeys;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return null;
    }
    
	// '{"key":"value"}'
    @Override
    public void setProperty(String key, Object value) {    	
        //15011137 김지수
    	//property가 비어있으면 선언
    	if (property == null) {
    		property = new String();
    		
    		//value가 String 이다.
        	if(value instanceof String) {
        		try {
        		stmt.executeUpdate("UPDATE vertex SET properties = '{\"" + key +"\":\""+ value + "\"}' WHERE ID ="+ this.id +";" );
        		}
        		catch(Exception e){
        			return;
        		}
        		}
        	// value가 숫자형.
        	else {
        		try {
            		stmt.executeUpdate("UPDATE vertex SET properties = '{\"" + key +"\":"+ value + "}' WHERE ID ="+ this.id +";" );
            		}
            		catch(Exception e){
            			return;
            		}
        	}
    	}
    	// 해당 vertex에 기존에 저장된 property가 있다
    	else {
    		try {
    			ResultSet set = stmt.executeQuery("SELECT properties from vertex WHERE ID = "+this.id+";");	
    			set.next();
    			property = set.getString(1);
    			property = property.substring(1,property.length()-1);	
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
    		//value가 String 이다.
        	if(value instanceof String) {
        		try {
        		stmt.executeUpdate("UPDATE vertex SET properties = '{"+property+",\"" + key +"\":\""+ value + "\"}' WHERE ID ="+ this.id +";" );
        		}
        		catch(Exception e){
        			return;
        		}
        		}
        	// value가 숫자형.
        	else {
        		try {
            		stmt.executeUpdate("UPDATE vertex SET properties = '{"+property+",\"" + key +"\":"+ value + "}' WHERE ID ="+ this.id +";" );
            		}
            		catch(Exception e){
            			return;
            		}
        	}
    	}
    }

    @Override
    public Object getId() {
        //15011137 김지수
        return id;
    }
    
    @Override
	public String toString() {
        //15011137 김지수
    	//16011176 박병훈
		return "v[" + id + "]";
	}
}
