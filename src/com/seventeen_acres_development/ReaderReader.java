package com.seventeen_acres_development;


import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Dan on 8/12/2016.
 */
public class ReaderReader implements Runnable {
    public static double[] normalized = new double[6];
    public static ArrayList<Double>[] shortLog = new ArrayList[6];//temp,vbus,cur,vout,vout2,volt or cur set
    @Override
    public void run() {
        try {
            if(!Main.jagPID) {
                int pidval=PIDController.getInstance().doPID((int) (ReaderReader.getInputCurrent() * 1000));
                if((getOutputVoltagePercentage()*326.27!=pidval)&&((pidval>0&&getSetCurrent()>0)||pidval==getSetCurrent())) {
                    Main.writer.write("volt set " + pidval + "\n");
                    System.out.println("ran"+(double)pidval/326.27);
                }
                Main.writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while (Main.r.ready()) {
                String line = Main.r.readLine();
                if (line.contains("t") || line.contains("u")) {
                    int begin = line.indexOf("=") + 2;
                    int end = line.indexOf(" ", begin);
                    int val;
                    if (end > 0)
                        val = Integer.parseInt(line.substring(begin, end));
                    else
                        val = Integer.parseInt(line.substring(begin));
                    if (line.contains("te"))
                        add(0,val,256);
                    else if (line.contains("vb"))
                        add(1,val,256);
                    else if (line.contains("set"))
                        add(5,val,256);
                    else if (line.contains("cu"))
                        add(2,val,256);
                    else if(line.contains("t2"))
                        add(4,val,256);
                    else if(line.contains("ut"))
                        add(3,val,327.67);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void add(int index,double val,double divisor){
        shortLog[index].add(val / divisor);
        if(shortLog[index].size()>5)
            shortLog[index].remove(0);
        double total = 0;
        for (double d : shortLog[index])
            total += d;
        normalized[index] = total / shortLog[index].size();
    }
    public static double getTemperature(){
        return normalized[0];
    }
    public static double getInputVoltage(){
        return normalized[1];
    }
    public static double getOutputCurrent(){
        return normalized[2];
    }
    public static double getOutputPower(){
        return getOutputCurrent()*getOutputVoltage();
    }
    public static double getInputPower(){
        return getOutputPower()/0.94;
    }
    public static double getInputCurrent(){
        return getInputPower()/ getInputVoltage();
    }
    public static double getOutputVoltagePercentage(){
        return normalized[3];
    }
    public static double getOutputVoltage(){
        return normalized[4];
    }
    public static double getSetVoltage(){
        return normalized[5];
    }
    public static double getSetCurrent(){
        if(Main.jagPID)
            return normalized[5];
        else
            return (double)PIDController.getInstance().getPIDParam(PIDController.PID_SETPOINT)/1000;
    }
}
