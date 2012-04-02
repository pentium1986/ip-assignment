package orca.ip_assignment;

import java.io.File;

import orca.ip_assignment.ndl.*;
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        //System.out.println( "Hello World!" );
    	RequestLoader RL = new RequestLoader();
    	File infile = new File("/home/kexu/workspace/ip-assignment/testinput.ndl");
    	File outfile = new File("/home/kexu/workspace/ip-assignment/testoutput.ndl");
 //   	System.out.println(infile.exists());
    	RL.loadGraph(infile);
    	System.out.println(outfile.canWrite());
    	System.out.println(RequestState.getInstance().g.getEdgeCount());
    	RequestSaver.getInstance().saveGraph(outfile, RequestState.getInstance().g);
    }
}
