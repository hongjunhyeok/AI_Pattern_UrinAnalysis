package com.example.urineanalysis.utils;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class FolderUtil {

    FileReader fileReader=null;
    BufferedReader bufferedReader =null;
    String line =null;
    double avg=0.0;
    int counter=0;



    public FolderUtil(){
        //class cannot be instantiated
    }


    public static void createDefaultFolder(String dirPath){
        File directory = new File(dirPath);
        if(!directory.exists()){
            directory.mkdir();
        }
    }


    public boolean checkIfFileExist(String filePath){
        File file = new File(filePath);
        return file.exists();
    }



    public ArrayList<Double> fileRead(String fileName){
        String path = Environment.getExternalStorageDirectory() + "/OneFileUrineCup/" ;
        ArrayList<Double>data=new ArrayList<>();
        try{
            fileReader=new FileReader(path+fileName);
            bufferedReader =
                    new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                counter++;
                if(counter <100)
                {

                    // line = null이면 break
                    if(line.isEmpty())
                    {
                        break;
                    }

                    //line = NaN이면 pass
                    Double doub=Double.parseDouble(line);
                    if(doub.isNaN()){
                        counter--;
                        continue;
                    }
//                    Log.i(TAG,String.format("%d %.3f",counter,avg));
                    data.add(doub);
                    avg+=doub;
                }
            }

            bufferedReader.close();
            fileReader.close();

            Log.i(TAG,String.format("Result %.3f",avg/counter));

        } catch(FileNotFoundException ex) {
            Log.i(TAG,ex.toString());
        }
        catch(Exception ex) {
            Log.i(TAG,ex.toString());
        }




        //null방지용
        if(data.size()<1){
            data.add(0.0);
        }

        return data;
    }
}