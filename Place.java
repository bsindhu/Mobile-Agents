package Mobile;

import java.io.*;
import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;

/**
 * Mobile.Place is the our mobile-agent execution platform that accepts an
 * agent transferred by Mobile.Agent.hop( ), deserializes it, and resumes it
 * as an independent thread.
 *
 * @author  Munehiro Fukuda modified sindhuri Bolisetty
 * @version %I% %G$
 * @since   1.0
 */
public class Place extends UnicastRemoteObject implements PlaceInterface {
	private AgentLoader loader = null;  // a loader to define a new agent class
	private int agentSequencer = 0;     // a sequencer to give a unique agentId
	Agent agent;
	ArrayList<Object> messagequeue = new ArrayList<Object>();
  
	public void receiveMessage(Object message){
    System.out.println("message sent the arraylist");
	messagequeue.add(message);
	
	}

	public Object[] sendMessage(){
		Object[] message = null;
		for(int i =0 ; i < messagequeue.size();i++){
			message[i] = messagequeue.toArray();
		}
		System.out.println("send the message");
		return message;
	}
	
	/**
	 * This constructor instantiates a Mobiel.AgentLoader object that
	 * is used to define a new agent class coming from remotely.
	 */
	public Place( ) throws RemoteException {
		super( );
		loader = new AgentLoader( );
	}

	/**
	 * deserialize( ) deserializes a given byte array into a new agent.
	 *
	 * @param buf a byte array to be deserialized into a new Agent object.
	 * @return a deserialized Agent object
	 */
	private Agent deserialize( byte[] buf ) 
			throws IOException, ClassNotFoundException {
		// converts buf into an input stream
		ByteArrayInputStream in = new ByteArrayInputStream( buf );

		// AgentInputStream identify a new agent class and deserialize
		// a ByteArrayInputStream into a new object
		AgentInputStream input = new AgentInputStream( in, loader );
		return ( Agent )input.readObject();
	}

	/**
	 * transfer( ) accepts an incoming agent and launches it as an independent
	 * thread.
	 *
	 * @param classname The class name of an agent to be transferred.
	 * @param bytecode  The byte code of  an agent to be transferred.
	 * @param entity    The serialized object of an agent to be transferred.
	 * @return true if an agent was accepted in success, otherwise false.
	 * 
	 */

	public boolean transfer( String classname, byte[] bytecode, byte[] entity )
			throws RemoteException {
		
		//deserializes the agent.
		try {
			loader.loadClass(classname, bytecode);
			agent = deserialize(entity);
			System.out.println("deserialized the agent");
			//agent identifier - combination of place IP address and sequencer.
			if(agent.agentId == -1){
				agentSequencer += agentSequencer;
				
			agent.setId((InetAddress.getLocalHost().hashCode() + agentSequencer));
			System.out.println("set an agent id");
			}
			
			System.out.println("create a new thread");
			//spawns a new thread for the agent
			Thread t = new Thread(agent);
			t.start();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		return true;
	}

	/**
	 * main( ) starts an RMI registry in local, instantiates a Mobile.Place
	 * agent execution platform, and registers it into the registry.
	 *
	 * @param args receives a port, (i.e., 5001-65535).
	 */
	public static void main( String args[] ) {

		// verify the port number.
		int port = 0;
		try {
			if ( args.length == 1 ) {
				port = Integer.parseInt( args[0] );
				if ( port < 5001 || port > 65535 )
					throw new Exception( );
			}
			else 
				throw new Exception( );
		} catch ( Exception e ) {
			System.err.println( "usage: java Mobile.Place port" );
			System.exit( -1 );
		}

		//register the place object
		try{
			startRegistry(port);
			Place placeObject = new Place();
			
			System.out.println("register place object into rmi registry");
			//register place object into rmi registry
			Naming.rebind( "rmi://localhost:" + port + "/place", placeObject );
			System.out.println( "Place ready." );
		}catch(Exception e){
			e.printStackTrace( );
			System.exit( -1 );
		}

	}

	/**
	 * startRegistry( ) starts an RMI registry process in local to this Place.
	 * 
	 * @param port the port to which this RMI should listen.
	 */
	private static void startRegistry( int port ) throws RemoteException {
		try {
			Registry registry = LocateRegistry.getRegistry( port );
			registry.list( );
		}
		catch ( RemoteException e ) {
			Registry registry = LocateRegistry.createRegistry( port );
		}
	}
}
