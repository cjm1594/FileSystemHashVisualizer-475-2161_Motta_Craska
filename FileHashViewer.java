/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Current Issues: 
 * 
 * Recursion not going deep enough. Only able to view the files in
 * subdirectories of drive C. Not able to process them.
 * 
 * Program must have administrator access to process all files.
 * 
 * Goals left:
 * 1. Process entire filesystem.
 * 2. Output in easy to read format.
 */

//Authors: Christian Motta and Sean Craska
package filehashviewer;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.*;
import java.util.regex.Pattern;

class FileNode{
    String currentDir; //Stores the full path of the file the node represents.
    FileNode prevNode; //Stores the parent directory of the current node.
    int dirLevel = 0; //Stores the directory level for printing later.
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
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        // Gathers hashes to use from user.
        ArrayList<String> selections = new ArrayList<>();
        System.out.println("Please enter select your 0hashes:");
        System.out.println("MD5");
        System.out.println("SHA1");
        System.out.println("SHA-256");
        System.out.println("0 = Finished selections");
        Scanner scan = new Scanner(System.in);
        while (true){
            String option = scan.next();
            if(option.equals("0")){
                break;
            }
            selections.add(option);
            //currSelection = option;
        }
        FileNode root = new FileNode();
        root.createNode("C:", null); //Creates the initial node for the file system root.
        populateNode(root, selections);
        parseNode(root, selections);
        
        
        
        
        /**Based off of solution found at http://stackoverflow.com/questions/2885173/how-to-create-a-file-and-write-to-a-file-in-java
         * Outputs all directories and hashes to a text file for further processing.
         */
        ArrayList<String> output = new ArrayList<>(); //Array for text output.
        ArrayList<String> csvOutput = new ArrayList<>(); //Array for D3js output.
        csvOutput.add("id,value"); //Needed at top of CSV file.
        printNodes(root, output); //Sends tree to standard printing.
        csvNodes(root, csvOutput); //Sends tree to CSV printing.
        
        //Writes text representation to file.
        Path outFile = Paths.get("hashOutput.txt");
        Files.write(outFile, output, Charset.forName("UTF-8"));
        
        //Writes CSV representation to file.
        Path outFile2 = Paths.get("hashOutput.csv");
        Files.write(outFile2, csvOutput, Charset.forName("UTF-8"));
        
        
    }
    
    /**
     * This function takes a node, and populates its children parameter with its subdirectories.
     * @param node
     */
    public static void populateNode(FileNode node, ArrayList selections) throws NoSuchAlgorithmException, IOException{
        ArrayList<String> splitDir = new ArrayList<>();
        /*
        * Runs the cmd line commands. Based off of http://stackoverflow.com/questions/15464111/run-cmd-commands-through-java
        */
        ProcessBuilder cd = new ProcessBuilder ("cmd.exe", "/c", "cd " + node.currentDir + "\\"); //CDs to directory
        ProcessBuilder dir = new ProcessBuilder ("cmd.exe", "/c", "dir " + node.currentDir); //Runs the dir command for the current directory.
        dir.redirectErrorStream(true);
        try{
            cd.start();
            Process d = dir.start();
            BufferedReader dirOut = new BufferedReader(new InputStreamReader(d.getInputStream())); //Sends cmd line output to program.
            String dirLine;
            
            //Allows the program to capture the output.
            while (true){
                dirLine = dirOut.readLine();
                
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
                    nextNode.dirLevel = node.dirLevel++; //New node's dir level inserted.
                    nextNode.prevNode = node; //New node's previous node is the current node.
                    node.children.add(nextNode); //Adds this node as a child of the current node
                    parseNode(nextNode, selections); //Theoretically should allow the program to recurse, although it refuses to process the new nodes.
                }
                
                //Creates a node for any entry that is not a directory.
                else{
                    String[] tmp2 = tmp[2].split("\\s"); //Splits the file size apart from the filename.
                    
                    //Same procedure as above.
                    String next = node.currentDir + "\\" + tmp2[1];
                    FileNode nextNode = new FileNode();
                    nextNode.currentDir = next;
                    nextNode.dirLevel = node.dirLevel++; 
                    nextNode.prevNode = node;
                    node.children.add(nextNode);
                    hashFunction(nextNode, selections);
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
    public static void parseNode(FileNode node, ArrayList selections) throws NoSuchAlgorithmException, IOException{
        
        //Attempts to force the system to go deeper into the directory structure by cding into the directory.
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
                populateNode(node.children.get(i), selections); //Should populate the child node with its own children, just as it does with C:.
            }
        }
    }
    
    /**
     * Function to hash files. Based off of: http://howtodoinjava.com/core-java/io/how-to-generate-sha-or-md5-file-checksum-hash-in-java/
     * @param node
     * @param selections
     * @throws FileNotFoundException 
     */
    public static void hashFunction(FileNode node, ArrayList selections) throws FileNotFoundException, NoSuchAlgorithmException, IOException{
        //Iterates through list of chosen hashes.
        for(int i = 0; i < selections.size(); i++){
            String dString = selections.get(i).toString(); //Converts array entry to string.
            MessageDigest digest = MessageDigest.getInstance(dString); //Creates a digest based on the string.
            File toHash = new File(node.currentDir); //Opens the file to be hashed.
            FileInputStream fileStream = new FileInputStream(toHash); //Opens a stream for the file to be parsed through.
        
            byte[] fileArray = new byte[1024]; //Breaks the file into bytes.
            int counter = 0;
            
            //Digests all bytes in the file.
            while ((counter = fileStream.read(fileArray)) != -1){
                digest.update(fileArray, 0, counter);
            }
        
            fileStream.close();
        
            //Adds digested bytes into an array.
            byte[] hashByte = digest.digest();
            
            //Converts digested byte array into ASCII string.
            StringBuilder hashString = new StringBuilder();
            for(int j = 0; j < hashByte.length; j++){
                hashString.append(Integer.toString((hashByte[j] & 0xff) + 0x100, 16).substring(1));
            }
            
            //Adds hash to array of hashes.
            node.hashVals.add(dString + ": " +hashString.toString());
        }
    }
    
    /**
     * Function that writes all nodes to an array to be written to file.
     * @param node 
     */
    public static void printNodes(FileNode node, ArrayList<String> output){
        String spaces = ""; //Stores the delimiting spaces needed for each line.
        
        //Adds a number of spaces to the spaces variable equal to the the level of the directory.
        for(int i = 0; i < node.dirLevel; i++){
            spaces = spaces + " ";
        }
        
        //Stores the name of the node being processed and all delimiting spaces for printing.
        String finalHeader = spaces + node.currentDir;
        output.add(finalHeader);
        
        //Adds delimited calculated hashes to the output array for printing.
        if(!(node.hashVals.isEmpty())){
            for(int j = 0; j < node.hashVals.size(); j++){
                String finalHash = spaces + node.hashVals.get(j);
                output.add(finalHash);
            }
        }
        
        //Recurses through the current node's children if they exist.
        if(!(node.children.isEmpty())){
            for(int k = 0; k < node.children.size(); k++){
                printNodes(node.children.get(k), output);
            }
        }
        
        //Adds a newline after each node for easier readability.
        output.add(" ");
    }
    
    /**
     * Function that writes node to CSV file in representation used by D3js file.
     * @param node
     * @param csvOutput 
     */
    public static void csvNodes(FileNode node, ArrayList<String> csvOutput){
        String holder = node.currentDir; //Stores information to be stored in file.
        //Creates CSV entry for directory nodes.
        if(!(node.children.isEmpty())){
           holder = holder + ","; //Appends , to string for CSV format.
           holder = holder.replace('.', ' '); //Changes all dots to spaces to prevent errors in D3js output.
           holder = holder.replace('\\', '.'); //Changes all \ characters to dots for D3js output.
           csvOutput.add(holder); //Adds processed line to file.
           //Repeats process for each child node.
           for(int i = 0; i< node.children.size(); i++){
               csvNodes(node.children.get(i), csvOutput);
           }
        }
        //Creates CSV entry for file nodes.
        if(!(node.hashVals.isEmpty())){
            holder = holder.replace('.', ' '); //Same as above.
            holder = holder + " Hashes: "; //Adds hashes section to output line.
            //Appends actual hashes to output line.
            for(int i = 0; i < node.hashVals.size(); i++){
                //Allows for extra hash to be added if more remain.
                if(i != (node.hashVals.size() - 1)){
                    holder = holder + node.hashVals.get(i) + " ";
                    holder = holder.replace('\\', '.'); //Same as above.
                }
                //Appends , and value to line if on the last hash.
                else {
                    holder = holder + node.hashVals.get(i) + ",4";
                    holder = holder.replace('\\', '.');
                }
            }
            csvOutput.add(holder); //Adds line to CSV output array.
        }
    }
}
