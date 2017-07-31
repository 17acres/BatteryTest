package com.seventeen_acres_development;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dan on 8/13/2016.
 */
public class WaveformManager {
    public static int mode;//constant,sine,square,sawtooth
    private static double period=1;
    private static double dcOffset;
    private static double amplitude;
    private static double dutyCycle=50;
    private static double currentValue;
    private static long startTime;
    public JPanel panel;
    private JSpinner perSpinner;
    private JLabel perLabel;
    private JLabel perLabel2;
    private JLabel dcoLabel;
    private JSpinner dcoSpinner;
    private JLabel dcoLabel2;
    private JLabel ampLabel;
    private JSpinner ampSpinner;
    private JLabel ampLabel2;
    private JLabel dutyLabel;
    private JSpinner dutySpinner;
    private JLabel dutyLabel2;
    private JLabel perLabel3;
    private JSpinner freqSpinner;
    private static WaveformManager instance;

    public static WaveformManager getInstance() {
        if (instance == null)
            instance = new WaveformManager();
        return instance;
    }

    public WaveformManager() {
        dcoSpinner.setModel(new SpinnerNumberModel(0, 0, 40, .1));
        dcoSpinner.addChangeListener((e)->{
            dcOffset=(double)instance.dcoSpinner.getValue();
            if(Main.jagPID){
                Main.setCurrent((Double) instance.dcoSpinner.getValue());
            }
        });
        ampSpinner.setModel(new SpinnerNumberModel(0, 0, 40, .1));
        ampSpinner.addChangeListener((e)->amplitude=(double)instance.ampSpinner.getValue());
        dutySpinner.setModel(new SpinnerNumberModel(50, 0, 100, 1));
        dutySpinner.addChangeListener((e)->dutyCycle=Double.valueOf((Integer)instance.dutySpinner.getValue()));
        perSpinner.setModel(new SpinnerNumberModel(1, 0, 10, .01));
        perSpinner.addChangeListener((e)->{
            period=(double)instance.perSpinner.getValue();
            freqSpinner.setValue(1/period);
        });
        freqSpinner.setModel(new SpinnerNumberModel(1, 0, 60, .1));
        freqSpinner.addChangeListener((e)->{
            period=(1/(double)instance.freqSpinner.getValue());
            perSpinner.setValue(period);
        });
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            refreshValue();
            if(ReaderReader.getSetCurrent()!=currentValue)
                Main.setCurrent(currentValue);
        }, 10, 10, TimeUnit.MILLISECONDS);
    }

    public static void setMode(int m) {
        getInstance();
        mode=m;
        startTime=System.currentTimeMillis();
        switch (m) {
            case 0:
                enableDCOffset();
                disablePeriod();
                disableAmplitude();
                disableDutyCycle();
                Main.enableExternalPID();
                break;
            case 1:
                enablePeriod();
                enableDCOffset();
                enableAmplitude();
                disableDutyCycle();
                Main.enableJagPID();
                break;
            case 2:
                enablePeriod();
                enableAmplitude();
                enableDutyCycle();
                enableDCOffset();
                Main.enableJagPID();
                break;
            case 3:
                enablePeriod();
                enableAmplitude();
                enableDCOffset();
                disableDutyCycle();
                Main.enableJagPID();
                break;
        }
    }
    public static void refreshValue(){
        double periodPositionRatio=((System.currentTimeMillis()-startTime)%(period*1000))/(period*1000);
        switch(mode){
            case 0:
                currentValue=(double)instance.dcoSpinner.getValue();
                break;
            case 1:
                currentValue=dcOffset+amplitude*Math.sin(2*Math.PI*periodPositionRatio);
                break;
            case 2:
                if(periodPositionRatio<(dutyCycle/100))
                    currentValue=dcOffset+amplitude;
                else
                    currentValue=dcOffset-amplitude;
                break;
            case 3:
                currentValue=dcOffset+amplitude*(-2)*(periodPositionRatio-.5);
        }
    }

    public static void disablePeriod() {
        instance.perLabel.setEnabled(false);
        instance.perSpinner.setEnabled(false);
        instance.perLabel2.setEnabled(false);
        instance.freqSpinner.setEnabled(false);
        instance.perLabel3.setEnabled(false);
    }

    public static void disableDCOffset() {
        instance.dcoLabel.setEnabled(false);
        instance.dcoSpinner.setEnabled(false);
        instance.dcoLabel2.setEnabled(false);
    }

    public static void disableAmplitude() {
        instance.ampLabel.setEnabled(false);
        instance.ampSpinner.setEnabled(false);
        instance.ampLabel2.setEnabled(false);
    }

    public static void disableDutyCycle() {
        instance.dutyLabel.setEnabled(false);
        instance.dutySpinner.setEnabled(false);
        instance.dutyLabel2.setEnabled(false);
    }

    public static void enablePeriod() {
        instance.perLabel.setEnabled(true);
        instance.perSpinner.setEnabled(true);
        instance.perLabel2.setEnabled(true);
        instance.freqSpinner.setEnabled(true);
        instance.perLabel3.setEnabled(true);
    }

    public static void enableDCOffset() {
        instance.dcoLabel.setEnabled(true);
        instance.dcoSpinner.setEnabled(true);
        instance.dcoLabel2.setEnabled(true);
    }

    public static void enableAmplitude() {
        instance.ampLabel.setEnabled(true);
        instance.ampSpinner.setEnabled(true);
        instance.ampLabel2.setEnabled(true);
    }

    public static void enableDutyCycle() {
        instance.dutyLabel.setEnabled(true);
        instance.dutySpinner.setEnabled(true);
        instance.dutyLabel2.setEnabled(true);
    }
}
