package lxz.tutorial.java.lang;

import java.util.*;
import java.io.*;
// StreamGobbler omitted for brevity
public class BadWinRedirect
{
    public static void main(String args[])
    {
        try
        {            
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("cmd.exe /c echo Hello World > test.txt");
            // any error message?
            StreamGobbler errorGobbler = new 
                StreamGobbler(proc.getErrorStream(), "ERROR");            
            
            // any output?
            StreamGobbler outputGobbler = new 
                StreamGobbler(proc.getInputStream(), "OUTPUT");
                
            // kick them off
            errorGobbler.start();
            outputGobbler.start();
                                    
            // any error???
            int exitVal = proc.waitFor();
            System.out.println("ExitValue: " + exitVal);        
        } catch (Throwable t)
          {
            t.printStackTrace();
          }
    }
}