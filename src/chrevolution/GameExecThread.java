/*--------------------------------------------------------------------------------
File : GameExecThread.java
----------------------------------------------------------------------------------
* Contains background treatment (scoring, etc) of CHRevolution 
* Version : 0
* Project : Cock Hero Revolution 
* License : Apache 2.0 (see file License apache 2.0.txt)
----------------------------------------------------------------------------------
* Modifications     :
--------------------------------------------------------------------------------*/
package chrevolution;

import static chrevolution.CHGameLogic.GE_lock;
import static chrevolution.CHGameLogic.score;
import static chrevolution.CHGameLogic.stat_to_get;
import static chrevolution.CHGameLogic.stat_to_get_int;
import static chrevolution.CHGameLogic.user_value;
import static chrevolution.CHGameLogic.user_value_int;
import static chrevolution.ServerGUI.result_label;
import static chrevolution.ServerGUI.score_label;

import java.text.*;


public class GameExecThread extends Thread {
          
            public static long score_to_display;
    
      public static void ReceiveInputs() {
            NumberFormat score_format_disp = null;
            score_format_disp = new DecimalFormat("#,### "); // Display score like 1 235 111...

            while (GE_lock == false) {
                 if (user_value.equals("no_value")) {
                   result_label.setText("No input");  
                 }
                 else if (user_value.equals(stat_to_get)) {
                     result_label.setText("<html><font color = #12F23F >OK !</font></html>"); // change the char color in green (HTML color code)
                     score = score + 1;
                     score_to_display = score / 100000;
                     score_label.setText(String.valueOf(score_format_disp.format(score_to_display)));
                     }
                     else if (user_value_int < stat_to_get_int) {
                         result_label.setText("<html><font color = #FF0000 >Too slow !!</font></html>"); // change the char color in red (HTML color code)
                        }
                     else {
                         result_label.setText("<html><font color = #FF0000 >Too Fast !!!</font></html>");
                     }  
            }
                       
     }      
}
