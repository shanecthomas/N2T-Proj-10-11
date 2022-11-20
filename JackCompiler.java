import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class JackCompiler {
  public JackCompiler () {
    
    System.out.println("Enter a Jack file/directory to compile: ");
    Scanner keyboard = new Scanner(System.in);
    String input = keyboard.nextLine();
    File srcFile = new File(input);
    File destFile = null;
    int index = -1;

    String fileFullName = srcFile.getName();
    if (!srcFile.isDirectory()) {
      index = fileFullName.lastIndexOf(".");
      String fExt = fileFullName.substring(index);
  
      //Jack file doesn't exist
      if (!fExt.equals(".jack") || !srcFile.exists()) {
        System.out.println("Jack file doesn't exist. Exiting program.");
        System.exit(0);
      }
    }

    try {
      //compile single Jack file
      if (!srcFile.isDirectory()) {
        String fileName = fileFullName.substring(0, index);
        String outFileName = fileName + ".vm";
        destFile = new File(outFileName);
        CompilationEngine comp = new CompilationEngine(srcFile, destFile);
        comp.compileClass();
        System.out.println(srcFile + " has been compiled!");
      }

      //compile multiple Jack files
      else {
        File[] directory = srcFile.listFiles();
        for ( File f : directory) {
          fileFullName = f.getName();
          index = fileFullName.lastIndexOf(".");
          String fExt = fileFullName.substring(index);
          if (fExt.equals(".jack")) {
            String fileName = fileFullName.substring(0, index);
            String outFileName = fileName + ".vm";
            destFile = new File(outFileName);
            CompilationEngine comp = new CompilationEngine(f, destFile);
            comp.compileClass();
          }
        }
      }
      System.out.println(srcFile + " has been compiled!");
      keyboard.close();
    }

    catch (IOException e) {
      System.out.println("An unknown error occurred.");
      System.exit(0);
    }
  }
}