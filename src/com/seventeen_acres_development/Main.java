package com.seventeen_acres_development;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    static BufferedReader r;
    static BufferedWriter writer;
    static boolean jagPID;
    public static void main(int COMPort) throws InterruptedException, IOException {
        for (int i = 0; i < 6; i++) {
            ReaderReader.shortLog[i] = new ArrayList<>();
        }
        Process p = Runtime.getRuntime().exec("bdc-comm-107.exe -c " + COMPort);
        r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
        //writer=new BufferedWriter(new OutputStreamWriter(System.out));
        writer.write("id 1\n");
        writer.flush();
        ScheduledExecutorService service = Executors.newScheduledThreadPool(3);
        service.scheduleAtFixedRate(new ReaderReader(), 10, 10, TimeUnit.MILLISECONDS);
        service.scheduleAtFixedRate(() -> {
            try {
                writer.write("stat temp\n");
                writer.write("stat vbus\n");
                writer.write("stat cur\n");
                writer.write("stat vout\n");
                writer.write("stat vout2\n");
                if (jagPID)
                    writer.write("cur set\n");
                else
                    writer.write("volt set\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }
    public static void setCurrent(double current){
        if(jagPID) {
             {
                try {
                    writer.write("cur set " + current * 256 + "\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        else {
            //Set PIDController setpoint to current*1000
        }

    }
    public static void enableJagPID(){
        jagPID=true;
        try {
            writer.write("cur en\ncur p 32767\ncur i 32767\ncur d 32767\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void enableExternalPID(){
        try {
            writer.write("volt en\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
