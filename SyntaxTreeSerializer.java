import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class SyntaxTreeSerializer {
  public boolean serialize(String fileName, ProgramBase program) {
    try {
      FileOutputStream file = new FileOutputStream(fileName);
      ObjectOutputStream out = new ObjectOutputStream(file);
      out.writeObject(program);
      out.close();
      file.close();
      return true;
    } catch (IOException e) {
      System.err.println("Serializing FAILED");
      e.printStackTrace();
      return false;
    }
  }

  public ProgramBase deserialize(String fileName) {
    ProgramBase program = null;
    try {
      FileInputStream file = new FileInputStream(fileName);
      ObjectInputStream in = new ObjectInputStream(file);
      program = (ProgramBase)in.readObject();
      in.close();
      file.close();
      return program;
    } catch(IOException e) {
      e.printStackTrace();
    } catch(ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }
}
