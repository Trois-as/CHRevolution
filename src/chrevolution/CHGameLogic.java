/*--------------------------------------------------------------------------------
File : CHGameLogic.java
----------------------------------------------------------------------------------
* Contains gaming methods of CHRevolution 
* Version : 0
* Project : Cock Hero Revolution 
* License : Apache 2.0 (see file License apache 2.0.txt)
----------------------------------------------------------------------------------
* Modifications     :
--------------------------------------------------------------------------------*/
package chrevolution;

// All imports from ServerGUI class are used to modify directly
// the value of the corresponding graphic component (variables 
// must be set to public static in Code -> Variable Modifiers of UI design)
import static chrevolution.ServerGUI.LoadConfigFile;
import static chrevolution.ServerGUI.VerifyConfigFile;
import static chrevolution.ServerGUI.VerifyLoadStringProperties;
import static chrevolution.ServerGUI.gui_tot_t_elaps;
import static chrevolution.ServerGUI.jButton1;
import static chrevolution.ServerGUI.jButton4;
import static chrevolution.ServerGUI.jButton5;
import static chrevolution.ServerGUI.jButton6;
import static chrevolution.ServerGUI.max_speed_PBar;
import static chrevolution.ServerGUI.minYval;
import static chrevolution.ServerGUI.minYval1;
import static chrevolution.ServerGUI.minYval2;
import static chrevolution.ServerGUI.minYval3;
import static chrevolution.ServerGUI.minYval4;
import static chrevolution.ServerGUI.min_speed_PBar;
import static chrevolution.ServerGUI.nb_min_a_s_label;
import static chrevolution.ServerGUI.nb_sec_a_s_label;
import static chrevolution.ServerGUI.next_speed_PBar;
import static chrevolution.ServerGUI.result_label;
import static chrevolution.ServerGUI.title_label;
import static chrevolution.ServerGUI.str_m_speed_to_get_1;
import static chrevolution.ServerGUI.str_m_speed_to_get_3;
import static chrevolution.ServerGUI.str_m_speed_to_get_4;
import static chrevolution.ServerGUI.str_m_speed_to_get_5;
import static chrevolution.ServerGUI.video_player;
import static chrevolution.ServerGUI.video_stream;
import static chrevolution.GameExecThread.score_to_display;

import java.io.IOException;
import java.util.Properties;


public class CHGameLogic extends Thread {
      
      public static String stat_to_get = "Bob";
      public static int stat_to_get_int; // numeric version of stat_to_get, used to display too slow/too fast correct value
      public static long debut_chrono = 0;
      public static long total_t_elaps_spare = 0; // total amount of ms for entire game in spare state/1 in numeric/neutral 
      public static long t_elaps_spare = 0; 
      public static long fin_chrono_spare = 0;
      public static long debut_chrono_spare = 0;
      public static long total_t_elaps_cool = 0; // total amount of ms for entire game in cooling state/2 in numeric/slow move
      public static long t_elaps_cool = 0; 
      public static long fin_chrono_cool = 0;
      public static long debut_chrono_cool = 0;
      public static long total_t_elaps_ok = 0; // total amount of ms for entire game in ok state/3 in numeric/relatively fast move
      public static long t_elaps_ok = 0; 
      public static long fin_chrono_ok = 0;
      public static long debut_chrono_ok = 0;
      public static long total_t_elaps_good = 0; // total amount of ms for entire game in good state/4 in numeric/fastest move
      public static long t_elaps_good = 0; 
      public static long fin_chrono_good = 0;
      public static long debut_chrono_good = 0;
      public static String current_stat = "none";
      public static boolean change_stat_lock = false;
      public static boolean first_time_lock = true;
      public static String user_value = "no_value";
      public static int user_value_int; // numeric version of user_value, used to display too slow/too fast correct value
      public static boolean GE_lock = true; // lock for stopping/starting à GameExecThread.ReceiveInputs to run
      public static long score = 0;
      public static float previous_all_values;
      public static long time_to_wait_a_seg;
      public static boolean stop_game_lock = false;
      public static String game_timeline = new String("S:0m5;C:00m10;O:0m12;G:0m10;S:0m5"); // C -> Cooling down, O -> OK, G -> good, S -> spare ..
      public static String video_player_args = "";
      public static String video_stream_start_time = "";
      public static String game_config_file = "./ini/default_game.properties";
      public static String game_title = "Untitled Game";
      public static Process process_v_player; // Process objects used to launch video player
      
    
      public static void start_chrono() {

            debut_chrono = java.lang.System.currentTimeMillis(); 
  
      } 
      
      
      public static long stop_chrono() {
            long fin_chrono = 0;
            long total_t_elaps = 0;
            
            fin_chrono = java.lang.System.currentTimeMillis(); 
            total_t_elaps = fin_chrono - debut_chrono;
            
            return total_t_elaps;
      }
      
      
      public static String getMinSecTime(long ms_time) {
  
            int total_sec;
            int nb_min;
            int nb_sec;
            String min_sec_time;
          
            total_sec = (int)ms_time / 1000;
            nb_min = total_sec / 60;
            nb_sec = total_sec - (60 * nb_min); // seconds to display
            min_sec_time = String.valueOf(nb_min) + "m" + String.valueOf(nb_sec) + "s";
  
            return min_sec_time;
  
      }
      
      
      public static long getmsTimeFormat(int nb_min, int nb_sec) {

            long min_to_ms = 0; 
            long sec_to_ms = 0;
            long total_ms;
      
            min_to_ms = (nb_min * 60) * 1000;
            sec_to_ms = nb_sec * 1000;
            total_ms = min_to_ms + sec_to_ms; 
      
            return total_ms;

      }      
   
      
      public static void change_stat(String new_stat) {           
            
            if (change_stat_lock == true) {
              if (!new_stat.equals(current_stat)) {
                switch (current_stat) {
                     case "spare" :
                         fin_chrono_spare = java.lang.System.currentTimeMillis(); 
                         t_elaps_spare = fin_chrono_spare - debut_chrono_spare;
                         total_t_elaps_spare = total_t_elaps_spare + t_elaps_spare;
                         fin_chrono_spare = 0;
                         debut_chrono_spare = 0;
                         t_elaps_spare = 0;
                         break;

                     case "cooling" :
                         fin_chrono_cool = java.lang.System.currentTimeMillis(); 
                         t_elaps_cool = fin_chrono_cool - debut_chrono_cool;
                         total_t_elaps_cool = total_t_elaps_cool + t_elaps_cool;
                         fin_chrono_cool = 0;
                         debut_chrono_cool = 0;
                         t_elaps_cool = 0;
                         break;

                     case "ok" :
                         fin_chrono_ok = java.lang.System.currentTimeMillis(); 
                         t_elaps_ok = fin_chrono_ok - debut_chrono_ok;
                         total_t_elaps_ok = total_t_elaps_ok + t_elaps_ok;
                         fin_chrono_ok = 0;
                         debut_chrono_ok = 0;
                         t_elaps_ok = 0;
                         break;

                     case "good" :
                         fin_chrono_good = java.lang.System.currentTimeMillis(); 
                         t_elaps_good = fin_chrono_good - debut_chrono_good;
                         total_t_elaps_good = total_t_elaps_good + t_elaps_good;
                         fin_chrono_good = 0;
                         debut_chrono_good = 0;
                         t_elaps_good = 0;
                         break;                  

                     default :
                         break;
              }
                switch (new_stat) {
                     case "spare" :
                         debut_chrono_spare = java.lang.System.currentTimeMillis(); 
                         current_stat = "spare";
                         break;

                     case "cooling" :
                         debut_chrono_cool = java.lang.System.currentTimeMillis(); 
                         current_stat = "cooling";
                         break;

                     case "ok" :
                         debut_chrono_ok = java.lang.System.currentTimeMillis(); 
                         current_stat = "ok";
                         break;

                     case "good" :
                         debut_chrono_good = java.lang.System.currentTimeMillis(); 
                         current_stat = "good";
                         break;                  

                     default :
                         break;
                }              
              }
            }

      }

      
      public static void reinit_game() {
            
            total_t_elaps_spare = 0; // nbre total de ms de la partie entiere
            t_elaps_spare = 0; // nbre de ms de la "session" actuelle
            fin_chrono_spare = 0;
            debut_chrono_spare = 0;
            total_t_elaps_cool = 0; // nbre total de ms de la partie entiere
            t_elaps_cool = 0; // nbre de ms de la "session" actuelle
            fin_chrono_cool = 0;
            debut_chrono_cool = 0;
            total_t_elaps_ok = 0; // nbre total de ms de la partie entiere
            t_elaps_ok = 0; // nbre de ms de la "session" actuelle
            fin_chrono_ok = 0;
            debut_chrono_ok = 0;
            total_t_elaps_good = 0; // nbre total de ms de la partie entiere
            t_elaps_good = 0; // nbre de ms de la "session" actuelle
            fin_chrono_good = 0;
            debut_chrono_good = 0;
            current_stat = "none";
            score_to_display = 0;
            score = 0;
            
      }
      
      
      public static void GameScheduler() throws IOException {
                         
            int nb_min_actual_segment;
            int nb_sec_actual_segment;
            int timeline_iteration = 0;
            String dur_actual_segment_str; // -> 1m00, 00m30, etc 
            String [] dur_actual_segment_tab;
            boolean game_base_config_exist;
            Properties game_properties_set;
            
            // variables who can be modified via the game.properties
            game_base_config_exist = VerifyConfigFile(game_config_file);
            game_properties_set = LoadConfigFile(game_config_file, game_base_config_exist);
            video_stream = VerifyLoadStringProperties(game_properties_set, "Video", video_stream);
            video_player_args = VerifyLoadStringProperties(game_properties_set, "Player_args", video_player_args);
            video_stream_start_time = VerifyLoadStringProperties(game_properties_set, "Start_time", video_stream_start_time);
            game_timeline = VerifyLoadStringProperties(game_properties_set, "Timeline", game_timeline);
            game_title = VerifyLoadStringProperties(game_properties_set, "Title", game_title); 
            title_label.setText(game_title);
            String [] video_cmd = { video_player, video_stream, video_player_args, video_stream_start_time };            
  
            try {
               Runnable GE_thread; // Runnable interface to execute method
               GE_thread = new Runnable() { // GameExecThread.ReceiveInputs
                           public void run() { // in a thread       
                           GameExecThread.ReceiveInputs();
                           }
               };
               
               Runtime runtime = Runtime.getRuntime(); // get the runtime object assiociate with the JVM
               process_v_player = runtime.exec(video_cmd); // launch the player in an external process
               GameSched_for_1: for (String actual_segment: game_timeline.split(";")){ // GameSched_for_1 is a label of the for
                  if (stop_game_lock == true) {
                    break GameSched_for_1 ; // breaking of the for in case user stop current game before the end
                  } 
                  switch (actual_segment.substring(0, 1)) { // substring treat actual_segment string as an array starting at 0
                        case "S" :
                            stat_to_get = "spare";
                            stat_to_get_int = 1;
                            max_speed_PBar.setValue(str_m_speed_to_get_1);
                            max_speed_PBar.setString(String.valueOf(str_m_speed_to_get_1));
                            min_speed_PBar.setValue(0);
                            min_speed_PBar.setString("0");
                            //stat_to_get_label.setForeground(new java.awt.Color(51, 204, 0));
                            break;
   
                        case "C" :
                            stat_to_get = "cooling";
                            stat_to_get_int = 2;
                            max_speed_PBar.setValue(str_m_speed_to_get_3);
                            max_speed_PBar.setString(String.valueOf(str_m_speed_to_get_3));
                            min_speed_PBar.setValue(str_m_speed_to_get_1);
                            min_speed_PBar.setString(String.valueOf(str_m_speed_to_get_1));                            
                            //stat_to_get_label.setForeground(new java.awt.Color(0, 0, 255));
                            break;
   
                        case "O" :
                            stat_to_get = "ok";
                            stat_to_get_int = 3;
                            max_speed_PBar.setValue(str_m_speed_to_get_4);
                            max_speed_PBar.setString(String.valueOf(str_m_speed_to_get_4));
                            min_speed_PBar.setValue(str_m_speed_to_get_3);
                            min_speed_PBar.setString(String.valueOf(str_m_speed_to_get_3));                            
                            //stat_to_get_label.setForeground(new java.awt.Color(128, 128, 51));
                            break;
   
                        case "G" :
                            stat_to_get = "good";
                            stat_to_get_int = 4;
                            max_speed_PBar.setValue(str_m_speed_to_get_5);
                            max_speed_PBar.setString(String.valueOf(str_m_speed_to_get_5));
                            min_speed_PBar.setValue(str_m_speed_to_get_4);
                            min_speed_PBar.setString(String.valueOf(str_m_speed_to_get_4));                            
                            //stat_to_get_label.setForeground(new java.awt.Color(255, 0, 1)); 
                            break;                  
   
                        default :
                            stat_to_get = "bad_value";                            
                            break;
                  }
                  if (stat_to_get.equals("bad_value")) {
                    continue GameSched_for_1 ; // go directly to the next iteration of the for to skip incorrect values.
                    // To do : log all these incorrect values. 
                  }
                  GetNextStat (timeline_iteration + 1);
                  //stat_to_get_label.setText(stat_to_get);                                   
                  dur_actual_segment_str  = actual_segment.substring(2);
                  dur_actual_segment_tab = dur_actual_segment_str.split("m");
                  nb_min_actual_segment = Integer.parseInt(dur_actual_segment_tab [0]);
                  nb_min_a_s_label.setText(String.valueOf(nb_min_actual_segment));
                  nb_sec_actual_segment = Integer.parseInt(dur_actual_segment_tab [1]);
                  nb_sec_a_s_label.setText(String.valueOf(nb_sec_actual_segment));
                  time_to_wait_a_seg = getmsTimeFormat (nb_min_actual_segment, nb_sec_actual_segment); 
                  GE_lock = false; // delocking of ReceiveInputs 
                  new Thread(GE_thread).start(); // starting of the ReceiveInputs thread
                  Thread.sleep(time_to_wait_a_seg); // GameScheduler put to sleep, during this pause ReceiveInputs update the score               
                  GE_lock = true; // locking of ReceiveInputs to stop is treatment
                  timeline_iteration = timeline_iteration + 1;
               }
               user_value = "no_value";
               score = 0;
               result_label.setText("Congratulation !! You reach the end of game !");
               gui_tot_t_elaps=CHGameLogic.stop_chrono ();
               CHGameLogic.change_stat ("none");
               change_stat_lock = false;
               process_v_player.destroy();
               minYval.setText(getMinSecTime(gui_tot_t_elaps));
               minYval3.setText(getMinSecTime(total_t_elaps_cool));
               minYval1.setText(getMinSecTime(total_t_elaps_good));
               minYval2.setText(getMinSecTime(total_t_elaps_ok));
               minYval4.setText(getMinSecTime(total_t_elaps_spare));              
               jButton4.setEnabled(true);
               jButton1.setEnabled(true);
               jButton6.setEnabled(false);
               jButton5.setEnabled(true);
            }
            catch (InterruptedException e1) {
                 //stat_to_get_label.setText("Thread interrompu!!");
                 gui_tot_t_elaps=CHGameLogic.stop_chrono ();
                 CHGameLogic.change_stat ("none");                
                 change_stat_lock = false;
                 process_v_player.destroy();
                 minYval.setText(getMinSecTime(gui_tot_t_elaps));
                 minYval3.setText(getMinSecTime(total_t_elaps_cool));
                 minYval1.setText(getMinSecTime(total_t_elaps_good));
                 minYval2.setText(getMinSecTime(total_t_elaps_ok));
                 minYval4.setText(getMinSecTime(total_t_elaps_spare));               
                 jButton4.setEnabled(true);
                 jButton1.setEnabled(true);
                 jButton6.setEnabled(false);
                 jButton5.setEnabled(true);
            }   
      }
      
      
      public static float AccelerometerValuesAnalysis() {

            String AVA_lastMessage = TCPServer.getLastMessage();
            float xvalue = Float.parseFloat(AVA_lastMessage.split(",")[0]);
            float yvalue = Float.parseFloat(AVA_lastMessage.split(",")[1]);
            float zvalue = Float.parseFloat(AVA_lastMessage.split(",")[2]);
            float all_values;
            float accelero_values_evolution;

            if (first_time_lock == true) {
              previous_all_values = xvalue + yvalue + zvalue;
              first_time_lock = false;
            }
            all_values = xvalue + yvalue + zvalue;
            accelero_values_evolution = previous_all_values - all_values;
            previous_all_values = all_values;
            
            return accelero_values_evolution;

      }
      
      
      public static void GetNextStat(int iteration) {

            String next_stat;
            String next_segment;
            int nb_segment;
            
            String [] timeline_tab = game_timeline.split(";");
            nb_segment = timeline_tab.length - 1;
            if (iteration > nb_segment) {
              next_segment = "E:0m0";  
            }
            else {
                next_segment = timeline_tab [iteration];
            }    
            switch (next_segment.substring(0, 1)) {
                  case "S" :
                      next_stat = "spare";
                      //next_stat_label.setForeground(new java.awt.Color(51, 204, 0));
                      //next_stat_label.setText(next_stat);
                      next_speed_PBar.setValue(str_m_speed_to_get_1);
                      next_speed_PBar.setString(String.valueOf(str_m_speed_to_get_1));                              
                      break;
   
                  case "C" :
                      next_stat = "cooling";
                      //next_stat_label.setForeground(new java.awt.Color(0, 0, 255));
                      //next_stat_label.setText(next_stat);
                      next_speed_PBar.setValue(str_m_speed_to_get_3);
                      next_speed_PBar.setString(String.valueOf(str_m_speed_to_get_3));                      
                      break;
   
                  case "O" :
                      next_stat = "ok";
                      //next_stat_label.setForeground(new java.awt.Color(128, 128, 51));
                      //next_stat_label.setText(next_stat);
                      next_speed_PBar.setValue(str_m_speed_to_get_4);
                      next_speed_PBar.setString(String.valueOf(str_m_speed_to_get_4));                      
                      break;
   
                  case "G" :
                      next_stat = "good";
                      //next_stat_label.setForeground(new java.awt.Color(255, 0, 1));
                      //next_stat_label.setText(next_stat);
                      next_speed_PBar.setValue(str_m_speed_to_get_5);
                      next_speed_PBar.setString(String.valueOf(str_m_speed_to_get_5));                      
                      break; 
                      
                  case "E" :
                      next_stat = "End of game";
                      next_speed_PBar.setValue(str_m_speed_to_get_5);
                      next_speed_PBar.setString("End of game");                      
                      //next_stat_label.setText(next_stat);
                      break;                       
   
                  default :
                      next_stat = "bad_value";
                      next_speed_PBar.setValue(0);
                      next_speed_PBar.setString("bad value");                      
                      //next_stat_label.setText(next_stat);
                      break;
            }

      }      
      
}

  
