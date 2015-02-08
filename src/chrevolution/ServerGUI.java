/*--------------------------------------------------------------------------------
File : ServerGUI.java
----------------------------------------------------------------------------------
* Contains initialisation of CHRev and most of GUI code 
* Version : 0
* Project : Cock Hero Revolution 
* License : Apache 2.0 (only for the modification)
----------------------------------------------------------------------------------
* Informations :
This class is based on ServerGUI.java from the project AccelerometerMouseServer.
More informations and original code at 
https://github.com/MohammadAdib/AccelerometerMouseServer
--------------------------------------------------------------------------------*/
package chrevolution;

import static chrevolution.CHGameLogic.change_stat_lock;
import static chrevolution.CHGameLogic.getMinSecTime;
import static chrevolution.CHGameLogic.total_t_elaps_cool;
import static chrevolution.CHGameLogic.total_t_elaps_good;
import static chrevolution.CHGameLogic.total_t_elaps_ok;
import static chrevolution.CHGameLogic.total_t_elaps_spare;
import static chrevolution.CHGameLogic.user_value;
import static chrevolution.CHGameLogic.user_value_int;
import static chrevolution.CHGameLogic.GE_lock;
import static chrevolution.CHGameLogic.game_config_file;
import static chrevolution.CHGameLogic.game_title;
import static chrevolution.CHGameLogic.video_player_args;
import static chrevolution.CHGameLogic.video_stream_start_time;
import static chrevolution.CHGameLogic.stop_game_lock;
import static chrevolution.GameExecThread.score_to_display;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.InetAddress;
import java.util.LinkedList;
import javax.swing.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class ServerGUI extends JFrame {

    // Variables for all methods of this class :
    private TCPServer tcpServer;
    private UDPServer udpServer;
    private GraphPanel graphPanel;
    private int str_meter;
    //private boolean jButton5_lock;
    private String CHR_base_config_file = "./ini/CHRevolution.properties";
    private String accelerometer_records_file = "./test.txt";
    private String high_score_dir = "./Score/";
    private String file_chooser_dir_str = "./ini/Games";
    private boolean CHR_base_config_exist = false;
    private Properties CHR_base_properties_set;
    private float acc_low_neutral_values = 0; // acc_xxx_yyy_values are directly compared to the
    private float acc_high_neutral_values = 0; // metrics of accelerometer computed by the function
    private float acc_low_level_1_values = -2; // AccelerometerValuesAnalysis to get the speed of
    private float acc_high_level_1_values = 2; // moves of the phones
    private float acc_low_level_2_values = -5;
    private float acc_high_level_2_values = 5;
    private float acc_low_level_3_values = -10;
    private float acc_high_level_3_values = 10;
    private int str_m_lv_indexed_modifier_1 = -5; // integer values add or subtract to str_meter
    private int str_m_lv_indexed_modifier_2 = 8; // synchronized with the acceleration of the
    private int str_m_lv_indexed_modifier_3 = 12; // phone move
    private int str_m_lv_indexed_modifier_4 = 15;
    private int str_value_indexed_malus_1 = 5;
    private int str_value_indexed_malus_2 = 7;
    private int str_value_indexed_malus_3 = 9;
    private int str_value_indexed_malus_4 = 20;
    private int str_m_auto_loss_step_1 = 500;
    private int str_m_auto_loss_step_2 = 1000;
    private int str_m_auto_loss_step_3 = 2000;
    
    // Variables accessible by every classes :    
    public static long gui_tot_t_elaps;
    public static Runnable GameSched;
    public static Thread GameSchedThread;
    public static int str_m_speed_to_get_1 = 3;
    public static int str_m_speed_to_get_2 = 20;
    public static int str_m_speed_to_get_3 = 500;
    public static int str_m_speed_to_get_4 = 1500;
    public static int str_m_speed_to_get_5 = 2000;
    public static String video_player = "./MPlayer/mplayer.exe";
    public static String video_stream = "./media/video/Cock Hero One Off - Big Wet Asses.flv";
    public static File file_chooser_base_dir;

    public ServerGUI() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                }
            }
        }
        catch (Exception e) {
        }
        CHR_base_config_exist = VerifyConfigFile(CHR_base_config_file);
        CHR_base_properties_set = LoadConfigFile(CHR_base_config_file, CHR_base_config_exist);
        accelerometer_records_file = VerifyLoadStringProperties(CHR_base_properties_set, "Record_file", accelerometer_records_file);
        high_score_dir = VerifyLoadStringProperties(CHR_base_properties_set, "Score_directory", high_score_dir);
        video_player = VerifyLoadStringProperties(CHR_base_properties_set, "Video_player_exe", video_player);
        file_chooser_dir_str = VerifyLoadStringProperties(CHR_base_properties_set, "Game_ini_directory", file_chooser_dir_str);
        file_chooser_base_dir = new File(file_chooser_dir_str);
        //// Customisation of the values to test the accelerometer's data :
        acc_low_neutral_values = VerifyLoadFloatProperties(CHR_base_properties_set, "motionless_low_value", acc_low_neutral_values);
        acc_high_neutral_values = VerifyLoadFloatProperties(CHR_base_properties_set, "motionless_high_value", acc_high_neutral_values);
        acc_low_level_1_values = VerifyLoadFloatProperties(CHR_base_properties_set, "slow_moves_low_values", acc_low_level_1_values);
        acc_high_level_1_values = VerifyLoadFloatProperties(CHR_base_properties_set, "slow_moves_high_values", acc_high_level_1_values);
        acc_low_level_2_values = VerifyLoadFloatProperties(CHR_base_properties_set, "more_fast_moves_low_values", acc_low_level_2_values);
        acc_high_level_2_values = VerifyLoadFloatProperties(CHR_base_properties_set, "more_fast_moves_high_values", acc_high_level_2_values);
        acc_low_level_3_values = VerifyLoadFloatProperties(CHR_base_properties_set, "fastest_moves_low_values", acc_low_level_3_values);
        acc_high_level_3_values = VerifyLoadFloatProperties(CHR_base_properties_set, "fastest_moves_high_values", acc_high_level_3_values);
        //// Customisation of modifiers applied to the stroke meter drectly after the tests of accelerometer's data :
        str_m_lv_indexed_modifier_1 = VerifyLoadIntProperties(CHR_base_properties_set, "motionless_str_m_modifier", str_m_lv_indexed_modifier_1);
        str_m_lv_indexed_modifier_2 = VerifyLoadIntProperties(CHR_base_properties_set, "slow_moves_str_m_modifier", str_m_lv_indexed_modifier_2);
        str_m_lv_indexed_modifier_3 = VerifyLoadIntProperties(CHR_base_properties_set, "more_fast_moves_str_m_modifier", str_m_lv_indexed_modifier_3);
        str_m_lv_indexed_modifier_4 = VerifyLoadIntProperties(CHR_base_properties_set, "fastest_moves_str_m_modifier", str_m_lv_indexed_modifier_4);
        //// Customisation of maluses applied to stroke meter even when the phone is moving, used to decreasing stroke meter when move are
        // too slow or staying at the same position when the speed is constant :
        str_value_indexed_malus_1 = VerifyLoadIntProperties(CHR_base_properties_set, "str_m_auto_decreasing_value_1", str_value_indexed_malus_1);
        str_value_indexed_malus_2 = VerifyLoadIntProperties(CHR_base_properties_set, "str_m_auto_decreasing_value_2", str_value_indexed_malus_2);
        str_value_indexed_malus_3 = VerifyLoadIntProperties(CHR_base_properties_set, "str_m_auto_decreasing_value_3", str_value_indexed_malus_3);
        str_value_indexed_malus_4 = VerifyLoadIntProperties(CHR_base_properties_set, "str_m_auto_decreasing_value_4", str_value_indexed_malus_4);
        //// Customisation of the moment when the malus is changed, based on actual value of of the stroke meter : 
        str_m_auto_loss_step_1 = VerifyLoadIntProperties(CHR_base_properties_set, "str_m_auto_decreasing_step_1", str_m_auto_loss_step_1);
        str_m_auto_loss_step_2 = VerifyLoadIntProperties(CHR_base_properties_set, "str_m_auto_decreasing_step_2", str_m_auto_loss_step_2);
        str_m_auto_loss_step_3 = VerifyLoadIntProperties(CHR_base_properties_set, "str_m_auto_decreasing_step_3", str_m_auto_loss_step_3);
        //// Customisation of all stroke meter bars and game steps linked to them :
        str_m_speed_to_get_1 = VerifyLoadIntProperties(CHR_base_properties_set, "stroke_m_step_value_1", str_m_speed_to_get_1);
        str_m_speed_to_get_3 = VerifyLoadIntProperties(CHR_base_properties_set, "stroke_m_step_value_2", str_m_speed_to_get_3);
        str_m_speed_to_get_4 = VerifyLoadIntProperties(CHR_base_properties_set, "stroke_m_step_value_3", str_m_speed_to_get_4);
        str_m_speed_to_get_5 = VerifyLoadIntProperties(CHR_base_properties_set, "stroke_meter_max_value", str_m_speed_to_get_5);
        System.out.println(accelerometer_records_file);
        initComponents();
        setGUIPosition();
        graphPanel = new GraphPanel();
        accelTab.invalidate();
        GameTab.invalidate();
        //
        //Graph
        javax.swing.GroupLayout chartPanelLayout = new javax.swing.GroupLayout(chartPanel);
        chartPanel.setLayout(chartPanelLayout);
        chartPanelLayout.setHorizontalGroup(
                chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(graphPanel).addGap(0, 349, Short.MAX_VALUE));
        chartPanelLayout.setVerticalGroup(
                chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(graphPanel).addGap(0, 160, Short.MAX_VALUE));
        //
        this.setMinimumSize(this.getSize());
        tcpServer = new TCPServer("0.0.0.0", 18250);
        tcpServer.start();
        udpServer = new UDPServer();
        udpServer.start();
        Runnable r;
        r = new Runnable() {
            
            public void run() {

                try {
                    InetAddress localhost = InetAddress.getLocalHost();
                    LinkedList<String> ips = new LinkedList<String>();
                    InetAddress[] allMyIps = InetAddress.getAllByName(localhost.getHostName());
                    if (allMyIps != null && allMyIps.length > 1) {
                        for (int i = 0; i < allMyIps.length; i++) {
                            if (!allMyIps[i].toString().contains(":")) {
                                ips.add(allMyIps[i].toString().substring(allMyIps[i].toString().indexOf("/") + 1));
                                System.out.println(allMyIps[i].toString().substring(allMyIps[i].toString().indexOf("/") + 1));
                                ipCB.setModel(new javax.swing.DefaultComboBoxModel(ips.toArray()));
                            }
                        }
                    }
                    ips.add("Listen on all");
                    ipCB.setModel(new javax.swing.DefaultComboBoxModel(ips.toArray()));
                }
                catch (Exception e) {
                }

                try {
                    while (tcpServer.isRunning()) {
                        if (tcpServer.isConnected()) {
                          if (tabbedPane.getTabCount() < 2) {
                                tabbedPane.addTab("Accelerometer Values", accelTab);
                                tabbedPane.addTab("Game", GameTab);
                          }
                          applyButton.setEnabled(false);
                          updateGraphs();
                        }
                        else {
                            applyButton.setEnabled(true);
                            xProgressBar.setValue(0);
                            yProgressBar.setValue(0);
                            zProgressBar.setValue(0);
                        }
                        setServerStatus(tcpServer.serverStatus);
                        setClientStatus(tcpServer.clientStatus);
                        Thread.sleep(20);
                    }
                }
                catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        new Thread(r).start();
        Runnable netRefresh = new Runnable() {

            @Override
            public void run() {
                while (tcpServer.isRunning()) {
                    try {
                        InetAddress localhost = InetAddress.getLocalHost();
                        LinkedList<String> ips = new LinkedList<String>();
                        InetAddress[] allMyIps = InetAddress.getAllByName(localhost.getHostName());
                        if (allMyIps != null && allMyIps.length > 1) {
                            for (int i = 0; i < allMyIps.length; i++) {
                                if (!allMyIps[i].toString().contains(":")) {
                                    ips.add(allMyIps[i].toString().substring(allMyIps[i].toString().indexOf("/") + 1));
                                    ipCB.setModel(new javax.swing.DefaultComboBoxModel(ips.toArray()));
                                }
                            }
                            ips.add("Listen on all");
                            ipCB.setModel(new javax.swing.DefaultComboBoxModel(ips.toArray()));
                        }
                        ips.add("Listen on all");
                        Thread.sleep(10000);
                    }
                    catch (Exception e) {
                    }
                }
            }
        };
        new Thread(netRefresh).start();
    }

    private void setServerStatus(String s) {
        serverStatusLabel.setText(s);
    }

    private void setClientStatus(String s) {
        clientStatusLabel.setText(s);
    }

    // GUI location at start up :
    private void setGUIPosition() {
        
           Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); // get the resolution in pixels
           int locX = dim.width - this.getWidth(); // dim.width : width of the screen, this.getWidth() : width of CHRev GUI
           int locY = dim.height - this.getHeight() - 50; // dim.height : height of the screen, this.getHeight() : height of CHRev GUI, - 50 is aproximatively the 
           this.setLocation(locX, locY); // height of the Windows 7 taskbar placed horizontally (Todo : variabilize for others OS)
           
    }

    private int getProgress(float f) {
        return (int) ((f * 100.0f) + 0.5f);
    }

    protected void updateGraphs() {
        if (tabbedPane.getSelectedIndex() == 1) {
            graphPanel.setInfo(tcpServer, graphPanel.getWidth(), graphPanel.getHeight(), new boolean[]{xCB.isSelected(), yCB.isSelected(), zCB.isSelected()}, tcpServer.packetsPerSec);
            graphPanel.repaint();
            xProgressBar.setValue(getProgress(-Float.parseFloat(tcpServer.getLastMessage().split(",")[0])));
            xtextvalue.setText(tcpServer.getLastMessage().split(",")[0]);
            yProgressBar.setValue(getProgress(-Float.parseFloat(tcpServer.getLastMessage().split(",")[1])));
            
            ytextvalue.setText(tcpServer.getLastMessage().split(",")[1]);            
            zProgressBar.setValue(getProgress(-Float.parseFloat(tcpServer.getLastMessage().split(",")[2])));
            ztextvalue.setText(tcpServer.getLastMessage().split(",")[2]);
        }
        else if (tabbedPane.getSelectedIndex() == 2) {
            //str_meter_txt.setText(String.valueOf(str_meter)); // String.valueOf : convert an int/double/float in string
            str_meter_PBar.setValue(str_meter);
            str_meter_PBar.setString(String.valueOf(str_meter)); // Display the value of str_meter instead of a percent when setStringPainted is true
            float accelero_values = CHGameLogic.AccelerometerValuesAnalysis();
            
                  // Traitement des messages de l'accelerometre :
                  if (accelero_values <= acc_low_level_3_values || accelero_values >= acc_high_level_3_values) { // accelero_values between -10 & 10 for acer Liquid Z130
                    str_meter = str_meter + str_m_lv_indexed_modifier_4; // 15 for acer Liquid Z130
                  }
                  else {                          
                      if (accelero_values <= acc_low_level_2_values || accelero_values >= acc_high_level_2_values) { // accelero_values between -5 & 5 for acer Liquid Z130
                        str_meter = str_meter + str_m_lv_indexed_modifier_3; // 12 for acer Liquid Z130 
                      }
                      else {
                          if (accelero_values <= acc_low_level_1_values || accelero_values >= acc_high_level_1_values) { // accelero_values between -2 & 2 for acer Liquid Z130
                            str_meter = str_meter + str_m_lv_indexed_modifier_2; // 8 for acer Liquid Z130 
                          }
                          else { 
                              if (accelero_values <= acc_low_neutral_values || accelero_values >= acc_high_neutral_values) { // accelero_values around 0 for acer Liquid Z130
                                str_meter = str_meter + str_m_lv_indexed_modifier_1; // -5 for acer Liquid Z130  
                              }
                          }
                      }
                  }
                  
                  // Update of the stat for others classes :
                  if (str_meter > str_m_speed_to_get_4) { // 500 for acer Liquid Z130
                    CHGameLogic.change_stat ("good");
                    user_value = "good";
                    user_value_int = 4;
                  }
                  else {                          
                      if (str_meter > str_m_speed_to_get_3) { // 500 for acer Liquid Z130
                        CHGameLogic.change_stat ("ok");
                        user_value = "ok";
                        user_value_int = 3;
                      }
                      else {
                          if (str_meter > str_m_speed_to_get_2) { // 20 for acer Liquid Z130
                            CHGameLogic.change_stat ("cooling");
                            user_value = "cooling";
                            user_value_int = 2;
                          }
                          else { 
                              if (str_meter < str_m_speed_to_get_1) { // 3 for acer Liquid Z130
                                CHGameLogic.change_stat ("spare");
                                user_value = "spare";
                                user_value_int = 1;
                              }
                          }
                      }
                  }
                 // Nested if to apply a constant loss to str_meter to decrease its value when no or too slow moves 
                 if (str_meter > str_m_auto_loss_step_3) {
                   str_meter = str_meter - str_value_indexed_malus_4;  
                 }
                 else {
                     if (str_meter > str_m_auto_loss_step_2) {
                       str_meter = str_meter - str_value_indexed_malus_3;
                     }
                     else {
                         if (str_meter > str_m_auto_loss_step_1) {
                           str_meter = str_meter - str_value_indexed_malus_2;
                         }
                         else {
                             if (str_meter > 0) {
                               str_meter = str_meter - str_value_indexed_malus_1;  
                             }
                             else {
                                 if (str_meter < 0) { // stabilize the str_meter when the phone is not moving
                                   str_meter = 0;
                                 }                                                      
                             }
                         }    
                     }
                  }
        }
    }
    
    
    public void GameScheduler_start() {
            
          GameSched = new Runnable() {
              public void run() {
                  try {   
                      CHGameLogic.GameScheduler();
                  } catch (IOException ex) {
                      Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                  }
              }
           };
           GameSchedThread = new Thread (GameSched);
           GameSchedThread.start();
            
    }
      
      
    public void GameScheduler_interrupt() {
            
          GameSchedThread.interrupt();
            
    }
    
        
    public static boolean VerifyConfigFile(String config_file_path) {
          
          boolean config_file_readable = false;
          
          try {
               Path path_to_file = Paths.get(config_file_path);
               if (Files.isRegularFile(path_to_file) == true) {
                 config_file_readable = true; 
                 System.out.println("File is a regular file");
               }
               else {
                                    config_file_readable = false; 
                 System.out.println("Not a file"); 
               }
                              if (Files.isReadable(path_to_file) == true) {
                 config_file_readable = true; 
                 System.out.println("File is readable");
               }
               else {
                                    config_file_readable = false; 
                 System.out.println("File isn't readable"); 
               }
               //config_file_readable = Files.isReadable(path_to_file) & Files.isRegularFile(path_to_file);
               // To do : log all these action 
          }
          catch (Exception e) {
              
               //config_file_readable = false;
          }
          
          return config_file_readable;
            
    }
    
    
    public static Properties LoadConfigFile(String config_file_path, boolean LCF_config_file_readable) {
          
          FileReader config_file_reader = null;
          Properties properties = new Properties();
          
          try {
               if (LCF_config_file_readable == true) {
                 config_file_reader = new FileReader(config_file_path);
                 properties.load(config_file_reader);
                 System.out.println("property loaded");
               } // To do : log all these action
               else {
                   System.out.println("Wanted property is not in the file");
               }
          }
          catch (Exception e) {
               System.out.println("Wanted property is not in the file");
          }
          
          return properties;
            
    }
    
    
    public static String VerifyLoadStringProperties(Properties properties_set, String properties_name, String default_value) {
          
          String new_value;
          
          if (properties_set.isEmpty() == true) {
            new_value = default_value; // To do : log the absent properties and the file  
          }
          else if (properties_set.getProperty(properties_name) == null) {
                 new_value = default_value; // To do : log the absent properties and the file  
          }
          else if (properties_set.getProperty(properties_name).equals("")) {
                 new_value = default_value; // To do : log the absent properties and the file  
          }          
          else {
              new_value = properties_set.getProperty(properties_name);
          }
          
          return new_value;
            
    }
    
    
    public static int VerifyLoadIntProperties(Properties properties_set, String properties_name, int default_value) {
          
          int new_int_value;
          String new_string_value;
          
          try {
             new_string_value = VerifyLoadStringProperties(properties_set, properties_name, String.valueOf(default_value));
             new_int_value = Integer.parseInt(new_string_value);
          }   
          catch (Exception e) {
               return default_value;
          }          
          
          return new_int_value;
            
    }
    
    
    public static float VerifyLoadFloatProperties(Properties properties_set, String properties_name, float default_value) {
          
          float new_float_value;
          String new_string_value;
          
          try {
             new_string_value = VerifyLoadStringProperties(properties_set, properties_name, String.valueOf(default_value));
             new_float_value = Float.parseFloat(new_string_value);
          }   
          catch (Exception e) {
               return default_value;
          }          
          
          return new_float_value;
            
    }    
    
    
//           // Debugging function : start updateGraphs() without wi-fi connection. 
//           // To activate, put it in an jButtonActionPerformed(java.awt.event.ActionEvent evt)
             // event   
//           jButton5.setEnabled(false);
//           jButton5_lock = true;
//           Runnable GraphUpdater;
//           GraphUpdater = new Runnable() {
//                public void run() {
//                      while (jButton5_lock == true) {
//                           updateGraphs();
//                      }
//                }
//           };
//           new Thread(GraphUpdater).start();
    
    // UI auto generated code :
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        File_chooser = new javax.swing.JFileChooser();
        tabbedPane = new javax.swing.JTabbedPane();
        serverConfigTab = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        ipCB = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        portNUD = new javax.swing.JSpinner();
        applyButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        serverStatusLabel = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        clientStatusLabel = new javax.swing.JLabel();
        discoverToggle = new javax.swing.JToggleButton();
        jLabel8 = new javax.swing.JLabel();
        accelTab = new javax.swing.JPanel();
        xProgressBar = new javax.swing.JProgressBar();
        yProgressBar = new javax.swing.JProgressBar();
        zProgressBar = new javax.swing.JProgressBar();
        xCB = new javax.swing.JCheckBox();
        yCB = new javax.swing.JCheckBox();
        zCB = new javax.swing.JCheckBox();
        chartPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        xtextvalue = new javax.swing.JLabel();
        ytextvalue = new javax.swing.JLabel();
        ztextvalue = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        GameTab = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        nb_min_a_s_label = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        nb_sec_a_s_label = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        result_label = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        minYval = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        minYval1 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        minYval2 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        minYval3 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        minYval4 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        score_label = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        str_meter_PBar = new javax.swing.JProgressBar();
        jLabel31 = new javax.swing.JLabel();
        max_speed_PBar = new javax.swing.JProgressBar();
        jLabel32 = new javax.swing.JLabel();
        min_speed_PBar = new javax.swing.JProgressBar();
        jLabel26 = new javax.swing.JLabel();
        next_speed_PBar = new javax.swing.JProgressBar();
        title_label = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();

        File_chooser.setCurrentDirectory(file_chooser_base_dir);
        File_chooser.setDialogTitle("Game select");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Accelerometer Mouse Server");
        setName("gui"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                Closing(evt);
            }
        });

        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Server IP:");

        ipCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "255.255.255.255" }));

        jLabel12.setText("Port:");

        portNUD.setModel(new javax.swing.SpinnerNumberModel(18250, 1, 65535, 1));

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ipCB, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(jLabel12)
                .addGap(2, 2, 2)
                .addComponent(portNUD, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(applyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(ipCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portNUD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(applyButton)
                    .addComponent(jLabel12))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        serverStatusLabel.setForeground(new java.awt.Color(51, 204, 0));
        serverStatusLabel.setText("Listening :");

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Client Status: ");

        clientStatusLabel.setForeground(new java.awt.Color(255, 0, 1));
        clientStatusLabel.setText("None Connected");

        discoverToggle.setSelected(true);
        discoverToggle.setText("Discoverable");
        discoverToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discoverToggleActionPerformed(evt);
            }
        });

        jLabel8.setText("Server Status: ");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(94, 94, 94)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clientStatusLabel))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(serverStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(discoverToggle)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(clientStatusLabel)
                            .addComponent(discoverToggle)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(5, 5, 5)
                        .addComponent(serverStatusLabel)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout serverConfigTabLayout = new javax.swing.GroupLayout(serverConfigTab);
        serverConfigTab.setLayout(serverConfigTabLayout);
        serverConfigTabLayout.setHorizontalGroup(
            serverConfigTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(serverConfigTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(serverConfigTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        serverConfigTabLayout.setVerticalGroup(
            serverConfigTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(serverConfigTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(290, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Server Configuration", serverConfigTab);

        xProgressBar.setMaximum(2000);
        xProgressBar.setMinimum(-2000);

        yProgressBar.setMaximum(2000);
        yProgressBar.setMinimum(-2000);

        zProgressBar.setMaximum(2000);
        zProgressBar.setMinimum(-2000);

        xCB.setForeground(new java.awt.Color(255, 0, 0));
        xCB.setSelected(true);
        xCB.setText("X-Axis");

        yCB.setForeground(new java.awt.Color(0, 255, 51));
        yCB.setSelected(true);
        yCB.setText("Y-Axis");

        zCB.setForeground(new java.awt.Color(0, 0, 255));
        zCB.setSelected(true);
        zCB.setText("Z-Axis");

        chartPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Charts"));

        javax.swing.GroupLayout chartPanelLayout = new javax.swing.GroupLayout(chartPanel);
        chartPanel.setLayout(chartPanelLayout);
        chartPanelLayout.setHorizontalGroup(
            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        chartPanelLayout.setVerticalGroup(
            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 119, Short.MAX_VALUE)
        );

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("0G");

        jLabel6.setText("+2G");

        jLabel7.setText("-2G");

        xtextvalue.setText("jLabel2");

        ytextvalue.setText("jLabel2");

        ztextvalue.setText("jLabel2");

        jLabel22.setText("Y Values : ");

        jLabel25.setText("X Values : ");

        jLabel29.setText("Z Values : ");

        jButton2.setText("Save the buffered accelerometer values");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout accelTabLayout = new javax.swing.GroupLayout(accelTab);
        accelTab.setLayout(accelTabLayout);
        accelTabLayout.setHorizontalGroup(
            accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(accelTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(accelTabLayout.createSequentialGroup()
                        .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel22)
                            .addComponent(jLabel25))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(xtextvalue)
                            .addComponent(ytextvalue))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addGap(27, 27, 27))
                    .addGroup(accelTabLayout.createSequentialGroup()
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ztextvalue)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(accelTabLayout.createSequentialGroup()
                        .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chartPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, accelTabLayout.createSequentialGroup()
                                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(zCB)
                                    .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(xCB)
                                        .addComponent(yCB)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(zProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(yProgressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(xProgressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, accelTabLayout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel6)))))
                        .addContainerGap())))
        );
        accelTabLayout.setVerticalGroup(
            accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(accelTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(xProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(2, 2, 2)
                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(yProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(yCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(2, 2, 2)
                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(zProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(5, 5, 5)
                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22)
                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xtextvalue)
                    .addComponent(jLabel25))
                .addGap(18, 18, 18)
                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ytextvalue)
                    .addComponent(jLabel22)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(accelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(ztextvalue))
                .addContainerGap(45, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Accelerometer Analysis", accelTab);

        jButton4.setText("Start Game");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel23.setText("m");

        jButton5.setText("Save last game results");
        jButton5.setEnabled(false);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        nb_min_a_s_label.setText("00");

        jButton6.setText("Stop Game");
        jButton6.setEnabled(false);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        nb_sec_a_s_label.setText("00");

        jLabel27.setText("Actual segment duration : ");

        result_label.setText("Game not started");
        result_label.setToolTipText("");
        result_label.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabel16.setText("stroke meter :");

        jButton1.setText("Start Chrono");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.setText("Stop Chrono");
        jButton3.setEnabled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel13.setText("Total time :");

        minYval.setText("minY");

        jLabel14.setText("Speed > 1500 : ");

        minYval1.setText("minY");

        jLabel17.setText("Speed > 500 : ");

        minYval2.setText("minY");

        jLabel18.setText("Speed < 500 : ");

        minYval3.setText("minY");

        jLabel19.setText("Speed < 3 : ");

        minYval4.setText("minY");

        jLabel30.setText("Score : ");

        score_label.setText("0");

        jButton7.setText("Game Select");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        str_meter_PBar.setMaximum(str_m_speed_to_get_5);
        str_meter_PBar.setString(String.valueOf(str_meter));
        str_meter_PBar.setStringPainted(true);

        jLabel31.setText("Max stroke speed :");

        max_speed_PBar.setForeground(new java.awt.Color(255, 0, 51));
        max_speed_PBar.setMaximum(str_m_speed_to_get_5);
        max_speed_PBar.setString(String.valueOf(str_meter));
        max_speed_PBar.setStringPainted(true);

        jLabel32.setText("Min stroke speed :");

        min_speed_PBar.setForeground(new java.awt.Color(255, 0, 51));
        min_speed_PBar.setMaximum(str_m_speed_to_get_5);
        min_speed_PBar.setString(String.valueOf(str_meter));
        min_speed_PBar.setStringPainted(true);

        jLabel26.setText("Next speed change : ");

        next_speed_PBar.setForeground(new java.awt.Color(0, 204, 102));
        next_speed_PBar.setMaximum(str_m_speed_to_get_5);
        next_speed_PBar.setString(String.valueOf(str_meter));
        next_speed_PBar.setStringPainted(true);

        title_label.setText("No game loaded");
        title_label.setToolTipText("");
        title_label.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabel15.setText("Stats :");

        javax.swing.GroupLayout GameTabLayout = new javax.swing.GroupLayout(GameTab);
        GameTab.setLayout(GameTabLayout);
        GameTabLayout.setHorizontalGroup(
            GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GameTabLayout.createSequentialGroup()
                .addGap(157, 157, 157)
                .addComponent(title_label)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(GameTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(GameTabLayout.createSequentialGroup()
                        .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel16)
                                .addComponent(jLabel32))
                            .addComponent(jLabel31))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(GameTabLayout.createSequentialGroup()
                                .addComponent(str_meter_PBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton5))
                            .addGroup(GameTabLayout.createSequentialGroup()
                                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(max_speed_PBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(min_speed_PBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(GameTabLayout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addGap(18, 18, 18)
                        .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(GameTabLayout.createSequentialGroup()
                                .addComponent(jLabel30)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(score_label))
                            .addComponent(result_label))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(GameTabLayout.createSequentialGroup()
                        .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(GameTabLayout.createSequentialGroup()
                                .addComponent(jButton6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel27))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, GameTabLayout.createSequentialGroup()
                                .addComponent(jButton7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel13))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, GameTabLayout.createSequentialGroup()
                                .addGap(152, 152, 152)
                                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel17)
                                    .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel15)
                                        .addComponent(jLabel14)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, GameTabLayout.createSequentialGroup()
                                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, GameTabLayout.createSequentialGroup()
                                        .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(minYval2)
                                            .addComponent(minYval1))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                                        .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, GameTabLayout.createSequentialGroup()
                                                .addComponent(jLabel19)
                                                .addGap(18, 18, 18)
                                                .addComponent(minYval4))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, GameTabLayout.createSequentialGroup()
                                                .addComponent(jLabel18)
                                                .addGap(18, 18, 18)
                                                .addComponent(minYval3))))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, GameTabLayout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING))))
                                .addGap(20, 20, 20))
                            .addGroup(GameTabLayout.createSequentialGroup()
                                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(minYval)
                                    .addGroup(GameTabLayout.createSequentialGroup()
                                        .addComponent(nb_min_a_s_label)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(nb_sec_a_s_label)))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, GameTabLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(next_speed_PBar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        GameTabLayout.setVerticalGroup(
            GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GameTabLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(title_label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(next_speed_PBar, javax.swing.GroupLayout.PREFERRED_SIZE, 14, Short.MAX_VALUE)
                    .addComponent(jLabel26))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(max_speed_PBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(str_meter_PBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton5)))
                .addGap(18, 18, 18)
                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32)
                    .addComponent(min_speed_PBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(GameTabLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(result_label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel30)
                            .addComponent(score_label)))
                    .addGroup(GameTabLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jButton4)))
                .addGap(18, 18, 18)
                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(GameTabLayout.createSequentialGroup()
                        .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel27)
                            .addComponent(nb_min_a_s_label)
                            .addComponent(jLabel23)
                            .addComponent(nb_sec_a_s_label))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addComponent(jButton6))
                .addGap(18, 18, 18)
                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton7)
                    .addComponent(jLabel13)
                    .addComponent(minYval))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(minYval3)
                    .addComponent(minYval1)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GameTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(minYval4)
                    .addComponent(jLabel17)
                    .addComponent(minYval2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Game", GameTab);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 457, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 421, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Followings functions are called when the correponding UI element is used
    private void Closing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_Closing
        tcpServer.stop();
    }//GEN-LAST:event_Closing

    private void discoverToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discoverToggleActionPerformed
        udpServer.setDiscoverable(discoverToggle.isSelected());
    }//GEN-LAST:event_discoverToggleActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        tcpServer.stop();
        String ip = ipCB.getSelectedItem().toString();
        if (ipCB.getSelectedItem().toString().contains("Listen on all")) {
            ip = "0.0.0.0";
        }
        tcpServer = new TCPServer(ip, Integer.valueOf(portNUD.getValue().toString()));
        tcpServer.start();
    }//GEN-LAST:event_applyButtonActionPerformed


    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        
           // Start chrono Button
           CHGameLogic.reinit_game();
           jButton3.setEnabled(true); 
           jButton1.setEnabled(false);
           change_stat_lock = true;
           CHGameLogic.start_chrono (); // Non-blocking function, can be
           // called in the GUI thread
 
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
           
           // Save the buffered... Button
           try {
              FileWriter target_file = new FileWriter(accelerometer_records_file);
              String[] messages = tcpServer.getAllMessages();
              for(int i=0 ; i<messages.length ; i++) {
                 target_file.write(messages [i]);
                 target_file.write('\n'); // ecriture du saut de ligne
              }
            } 
            catch (IOException ex) {
                 Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
    
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

           // Stop chrono Button
           gui_tot_t_elaps=CHGameLogic.stop_chrono ();
           CHGameLogic.change_stat ("none");
           change_stat_lock = false;
           minYval.setText(getMinSecTime(gui_tot_t_elaps));
           minYval3.setText(getMinSecTime(total_t_elaps_cool));
           minYval1.setText(getMinSecTime(total_t_elaps_good));
           minYval2.setText(getMinSecTime(total_t_elaps_ok));
           minYval4.setText(getMinSecTime(total_t_elaps_spare));
           jButton1.setEnabled(true);
           jButton3.setEnabled(false);
           jButton5.setEnabled(true);
           
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        
           // Bouton Start Game
           CHGameLogic.reinit_game();
           jButton4.setEnabled(false);
           jButton1.setEnabled(false);
           jButton6.setEnabled(true);
           change_stat_lock = true;
           stop_game_lock = false;
           CHGameLogic.start_chrono ();           
           GameScheduler_start();
           
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
           
           // Save last game score Button
           String score_file_name = game_title + " - score - " + java.lang.System.currentTimeMillis() + ".txt";
           String score_file_path = high_score_dir + score_file_name;
           NumberFormat score_format_disp = null;
           score_format_disp = new DecimalFormat("#,### "); // Display score like 1 235 111...
           FileWriter target_file_sb = null;
           String saved_title_value = "Title : " + game_title;
           String saved_score_value = "Score : " + score_format_disp.format(score_to_display);
           String saved_total_t_value = "Total time : " + getMinSecTime(gui_tot_t_elaps);
           String saved_spare_value = "Speed < 3 : " + getMinSecTime(total_t_elaps_spare);
           String saved_cooling_value = "Speed < 500 : " + getMinSecTime(total_t_elaps_cool);
           String saved_ok_value = "Speed > 500 : " + getMinSecTime(total_t_elaps_ok);
           String saved_good_value = "Speed > 1500 : " + getMinSecTime(total_t_elaps_good);
           String [] tab_to_write = {saved_title_value, saved_score_value, saved_total_t_value, saved_spare_value, saved_cooling_value, saved_ok_value, saved_good_value };
           
           try {
              target_file_sb = new FileWriter(score_file_path); // true : booleen pour le append a la fin d'un fichier existant
              for(int i=0 ; i<tab_to_write.length ; i++) {
                 target_file_sb.write(tab_to_write [i]);
                 target_file_sb.write('\n'); // ecriture du saut de ligne        
              }
           }
           
           catch(IOException e){
                e.printStackTrace();
           }
              
           finally {
                  try { 
                     target_file_sb.close(); 
                  }
                  
                  catch(IOException e) {
                       e.printStackTrace();
                  }
           }
           jButton5.setEnabled(false);
            
    }//GEN-LAST:event_jButton5ActionPerformed
    
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
           
           // Bouton Stop Game
           GE_lock = true;
           GameScheduler_interrupt();
           
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed

           // Game Select Button
           int OpenDialog_return = File_chooser.showOpenDialog(this); // Open a file selection dialog box, this is jButton7
           video_player_args = ""; // video_player_args and video_stream_start_time are zeroed in case they are not used
           video_stream_start_time = ""; // in the next selected games
           
           if (OpenDialog_return == JFileChooser.APPROVE_OPTION) { // APPROVE_OPTION : option of JFileChooser that return an int if chosen
             File selected_file = File_chooser.getSelectedFile();
             game_config_file = selected_file.getAbsolutePath();
           } 
           else {
               System.out.println("File access cancelled by user.");
           }        
        
    }//GEN-LAST:event_jButton7ActionPerformed

    // Variables for Swing/AWT code (can be modified via the designer) :
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser File_chooser;
    private javax.swing.JPanel GameTab;
    private javax.swing.JPanel accelTab;
    private javax.swing.JButton applyButton;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JLabel clientStatusLabel;
    private javax.swing.JToggleButton discoverToggle;
    private javax.swing.JComboBox ipCB;
    public static javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    public static javax.swing.JButton jButton4;
    public static javax.swing.JButton jButton5;
    public static javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    public static javax.swing.JProgressBar max_speed_PBar;
    public static javax.swing.JLabel minYval;
    public static javax.swing.JLabel minYval1;
    public static javax.swing.JLabel minYval2;
    public static javax.swing.JLabel minYval3;
    public static javax.swing.JLabel minYval4;
    public static javax.swing.JProgressBar min_speed_PBar;
    public static javax.swing.JLabel nb_min_a_s_label;
    public static javax.swing.JLabel nb_sec_a_s_label;
    public static javax.swing.JProgressBar next_speed_PBar;
    private javax.swing.JSpinner portNUD;
    public static javax.swing.JLabel result_label;
    public static javax.swing.JLabel score_label;
    private javax.swing.JPanel serverConfigTab;
    private javax.swing.JLabel serverStatusLabel;
    private javax.swing.JProgressBar str_meter_PBar;
    private javax.swing.JTabbedPane tabbedPane;
    public static javax.swing.JLabel title_label;
    private javax.swing.JCheckBox xCB;
    private javax.swing.JProgressBar xProgressBar;
    private javax.swing.JLabel xtextvalue;
    private javax.swing.JCheckBox yCB;
    private javax.swing.JProgressBar yProgressBar;
    private javax.swing.JLabel ytextvalue;
    private javax.swing.JCheckBox zCB;
    private javax.swing.JProgressBar zProgressBar;
    private javax.swing.JLabel ztextvalue;
    // End of variables declaration//GEN-END:variables
}
