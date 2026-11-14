import java.util.*;
import vn.duongngang.monitor.Rules;
public class WebAxleTest {
  public static void main(String[] args) {
    for(int delta:new int[]{-2,-1,1,2}) {
      var counts=new ArrayList<>(List.of("40","40","40","40","40","40"));
      counts.set(0,""+(40+delta));
      if(!Rules.axleNote(counts).isEmpty())throw new AssertionError("small note "+delta);
      counts.set(1,"50");
      String note=Rules.axleNote(counts);
      if(note.contains("CB1")||!note.contains("CB2"))throw new AssertionError(note);
    }
    System.out.println("8 web axle-note checks passed (small sensor excluded independently)");
  }
}
