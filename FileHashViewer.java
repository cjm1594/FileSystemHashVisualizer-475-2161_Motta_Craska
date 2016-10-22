/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Current Issues: Recursion not going deep enough. Only able to view the files in
 * subdirectories of drive C. Not able to process them.
 * 
 * Goals left:
 * 1. Process entire filesystem.
 * 2. Hash files upon encounter.
 * 3. Output in easy to read format.
 */

//Authors: Christian Motta and Sean Craska
package filehashviewer;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

class FileNode{
    String currentDir; //Stores the full path of the file the node represents.
    FileNode prevNode; //Stores the parent directory of the current node.
    ArrayList<String> hashVals = new ArrayList<>(); //This array stores the calculated hash values.
    ArrayList<FileNode> children = new ArrayList<>(); //THis array stores the files and directories within the current directory.
    
    /**
     *  Constructor for the FileNode object.
     */
    void createNode(String cDir, FileNode pNode){ 
        currentDir = cDir;
        prevNode = pNode;
    }
}
/**
 *
 * 
 */
public class FileHashViewer {

    /**
     * @param args the command line arguments
     * Main function for the program.
     */
    public static void main(String[] args) {
        // TODO code application logic here
        FileNode root = new FileNode();
        root.createNode("C:", null); //Creates the initial node for the file system root.
        populateNode(root);
        parseNode(root);
        
        //debug print statements
        System.out.print("\n");
        System.out.println(root.children.get(2).currentDir);
    }
    
    /**
     * This function takes a node, and populates its children parameter with its subdirectories.
     * @param node
     */
    public static void populateNode(FileNode node){
        System.out.println("Processing directory: " + node.currentDir);
        ArrayList<String> splitDir = new ArrayList<>();
        /*
        * Runs the cmd line commands. Based off of http://stackoverflow.com/questions/15464111/run-cmd-commands-through-java
        */
        ProcessBuilder dir = new ProcessBuilder ("cmd.exe", "/c", "dir " + node.currentDir); //Runs the dir command for the current directory.
        dir.redirectErrorStream(true);
        try{
            Process d = dir.start();
            BufferedReader dirOut = new BufferedReader(new InputStreamReader(d.getInputStream())); //Sends cmd line output to program.
            String dirLine;
            
            //Allows the program to capture the output.
            while (true){
                dirLine = dirOut.readLine();
                System.out.println(dirLine);
                
                //Stops loop once cmd line output is finished.
                if (dirLine == null){
                    break;
                }
                
                //Ignores empty lines of output.
                if (!(dirLine.matches("\\s*?"))){
                    splitDir.add(dirLine);
                }
            }
        }
        catch (Exception e){
            e.addSuppressed(e);
        }
        
        int i = 0;
        
        //Ignores drive information from dir command for root node.
        if(node.prevNode == null){
            i = 3;
        }
        
        //Ignores drive information and the . and .. directories for all other directories.
        else{
            i = 5;
        }
        
        //Loops through the directories and files while ignoring the last two lines of storage usage information.
        while(i < splitDir.size() - 2){
            /*
            * Slits each line at any point where more than one space occurs in order to keep multiple word 
            * file and directory names intact.
            */
            String[] tmp = splitDir.get(i).split("\\s{2,}");
            if(tmp.length != 1){
                
                //Creates a new node for any entry that is declared a directory.
                if(tmp[2].equals("<DIR>")){ //The <DIR> flag occurs in the second index of the array.
                    String next = node.currentDir + "\\" + tmp[3]; //Directory name begins at index 3, uses double slash to escape single backslash.
                    FileNode nextNode = new FileNode();
                    nextNode.currentDir = next; //New node gets the name of the next directory.
                    System.out.println("Adding directory: " + nextNode.currentDir);
                    nextNode.prevNode = node; //New node's previous node is the current node.
                    node.children.add(nextNode); //Adds this node as a child of the current node
                    parseNode(nextNode); //Theoretically should allow the program to recurse, although it refuses to process the new nodes.
                }
                
                //Creates a node for any entry that is not a directory.
                else{
                    String[] tmp2 = tmp[2].split("\\s"); //Splits the file size apart from the filename.
                    
                    //Same procedure as above.
                    String next = node.currentDir + "\\" + tmp2[1];
                    FileNode nextNode = new FileNode();
                    nextNode.currentDir = next;
                    System.out.println("Adding new file: " + nextNode.currentDir);
                    nextNode.prevNode = node;
                    node.children.add(nextNode);
                    //break;
                }
                i++;
            }
            else{
                break;
            }
        }
    }
    
    /**
     * Helper function for recursion. Should force program to process subdirectories.
     * Note: 
     * @param node 
     */
    public static void parseNode(FileNode node){
        
        //Attempts to force the system to go deeper into the directory structure by cding into the directory.
        System.out.println("Current dir: " + node.currentDir); //Debug print statement to see which dir is targeted.
        ProcessBuilder cd = new ProcessBuilder ("cmd.exe", "/c", "cd " + node.currentDir); //Runs the dir command for the current directory.
        cd.redirectErrorStream(true);
        try{
            cd.start();
        }
        catch (Exception e){
            e.addSuppressed(e);
        }
        if(node.children.size() != 0){ //Prevents the program from attempting to access children from files with none.
            for(int i = 0; i < node.children.size(); i++){ //Iterates through all children in the node.
                /*
                * Debug print statement to see which dir is about to be parsed through.
                */
                System.out.println("Parsing through :" + node.children.get(i).currentDir);
                populateNode(node.children.get(i)); //Should populate the child node with its own children, just as it does with C:.
            }
        }
    }
}
