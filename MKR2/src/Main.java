import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.*;

public class Main {
    public static void main(String[] args){
        try{
            Scanner in = new Scanner(new File("input.txt"));
            int n = in.nextInt();
            if(n <= 0) throw new Exception("n має бути > 0");

            int[] a = new int[n];
            for(int i = 0; i < n; i++){
                a[i] = in.nextInt();
                if(a[i]<-1000 || a[i]>1000) throw new Exception("число поза межами");}
                if(in.hasNext()) throw new Exception("зайві дані");

                PrintWriter out = new PrintWriter("output.txt");
                for(int i = 0; i < n; i++){
                    boolean was = false;
                    for(int j = 0; j < i; j++)
                        if(a[j] == a[i]) was = true;

                    if(!was){
                        long count = 0;
                        for(int j = 0; j < n; j++)
                            if(a[j] == a[i]) count++;
                        out.println(a[i] + " " + count);
                    }
                }
                out.close();
            }catch (FileNotFoundException e){
                System.out.println("file not found");
            }catch (InputMismatchException e){
                System.out.println("not int");
            }catch (NoSuchFileException e){
                System.out.println("many nambers");
            }catch (Exception e){
                System.out.println("Err" + e.getMessage());
            }
        }
    }
