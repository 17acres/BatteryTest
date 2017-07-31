package com.seventeen_acres_development;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dan on 8/12/2016.
 */
public class BattTest {
    private JPanel panel;
    private static JFrame frame;
    private JTextField inputVoltageTextField;
    private JTextField outputVoltageTextField;
    private JTextField outputCurrentTextField;
    private JSpinner cutoffVoltageSpinner;
    private JComboBox cycleModeComboBox;
    private JTextField outputEnergyTextField;
    private JTextField outputPowerTextField;
    private JPanel setupPanel;
    private JTextField inputCurrentTextField;
    private JTextField inputPowerTextField;
    private JTextField inputEnergyTextField;
    public static FileWriter writer;
    public static long startTime;

    public static void main(String[] args) {
        try {
            String filename = System.getProperty("user.home") + "\\Desktop\\BattTest" + ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".csv";
            filename = filename.replace(":", "-");
            filename = filename.replaceFirst("-", ":");
            File file = new File(filename);
            file.createNewFile();
            writer = new FileWriter(file);
            writer.write("Elapsed Time (ms),Battery Voltage (V),Battery Current (A), Battery Power (W), Battery Energy(W*h),Output Voltage (V),Set Current (A),Output Current (A),Output Power(W),Output Energy(W*h)\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Main.main(Integer.parseInt(JOptionPane.showInputDialog(frame,"Enter COM Port",null)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Main.enableExternalPID();
        frame = new JFrame("BattTest");
        BattTest instance = new BattTest();
        instance.cutoffVoltageSpinner.setModel(new SpinnerNumberModel(10.5, 9, 12, 0.1));
        frame.setContentPane(instance.panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        startTime= System.currentTimeMillis() - (System.currentTimeMillis() % 10)+(100 - (System.currentTimeMillis() % 100));
        Integrator.addPoint(((double)System.currentTimeMillis())/3600000,0);
        instance.cycleModeComboBox.addActionListener((actionEvent)->{
            WaveformManager.setMode(instance.cycleModeComboBox.getSelectedIndex());
        });
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                ()->Integrator.addPoint(((double)System.currentTimeMillis())/3600000,ReaderReader.getOutputPower())
                ,10,10,TimeUnit.MILLISECONDS);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                writer.write(String.valueOf(((System.currentTimeMillis()- startTime)-(System.currentTimeMillis()- startTime)%10) + ","));
                writer.write(String.valueOf(ReaderReader.getInputVoltage()) + ",");
                writer.write(String.valueOf(ReaderReader.getInputCurrent()) + ",");
                writer.write(String.valueOf(ReaderReader.getInputPower()) + ",");
                writer.write(String.valueOf(Integrator.getArea()/.94) + ",");
                writer.write(String.valueOf(ReaderReader.getOutputVoltage()) + ",");
                writer.write(String.valueOf(ReaderReader.getSetCurrent()) + ",");
                writer.write(String.valueOf(ReaderReader.getOutputCurrent()) + ",");
                writer.write(String.valueOf(String.valueOf(ReaderReader.getOutputPower())) + ",");
                writer.write(String.valueOf(Integrator.getArea())+ "\n");
                writer.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }, 100 - (System.currentTimeMillis() % 100), 100, TimeUnit.MILLISECONDS);
        new Timer(1000 / 60, (e) -> {
            if (ReaderReader.getInputVoltage()>0&&ReaderReader.getInputVoltage() <(Double) instance.cutoffVoltageSpinner.getValue()) {
                Main.setCurrent(0);
                Toolkit.getDefaultToolkit().beep();
                JOptionPane optionPane = new JOptionPane("Test Complete. Energy used: "+Integrator.getArea()/.94+" Watt-hours", JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = optionPane.createDialog("Success");
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowDeactivated(WindowEvent e) {
                        try {
                            writer.close();
                            System.exit(0);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        System.exit(0);
                    }
                });
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);
            }
            instance.inputVoltageTextField.setText(String.format("%1$.3f", ReaderReader.getInputVoltage())+"V");
            instance.inputCurrentTextField.setText(String.format("%1$.3f", ReaderReader.getInputCurrent())+"A");
            instance.inputPowerTextField.setText(String.format("%1$.3f", ReaderReader.getInputPower())+"W");
            instance.inputEnergyTextField.setText(String.format("%1$.3f", Integrator.getArea()/.94)+"W*h");
            instance.outputVoltageTextField.setText(String.format("%1$.3f", ReaderReader.getOutputVoltage())+"V");
            instance.outputCurrentTextField.setText(String.format("%1$.3f", ReaderReader.getOutputCurrent())+"A");
            instance.outputPowerTextField.setText(String.format("%1$.3f", ReaderReader.getOutputPower())+"W");
            instance.outputEnergyTextField.setText(String.format("%1$.3f", Integrator.getArea())+"W*h");
        }).start();
    }

    private void createUIComponents() {
        String[] choices=new String[]{"Constant","Sine","Square"," Inv. Sawtooth"};
        cycleModeComboBox=new JComboBox(choices);
        cycleModeComboBox.setSelectedIndex(0);
        setupPanel=WaveformManager.getInstance().panel;
    }
}
