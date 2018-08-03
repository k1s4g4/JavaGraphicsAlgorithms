// Torus.java: Generating a data file for a torus. R is the radius of 
//    a large horizontal circle, on which n equidistant points will be
//    the centers of small vertical circles with radius 1. The values
//    of n and R as well as the output file name are to be supplied as
//    program arguments.
import java.io.*;

public class Steel {
   Steel(int n, double R, String fileName) throws IOException {
      
      FileWriter fw = new FileWriter(fileName+".dat");
      double delta = 2 * Math.PI / n;
      for (int i = 0; i < n; i++) {
         double alpha = i * delta, 
                cosa = Math.cos(alpha), sina = Math.sin(alpha);
         for (int j = 0; j < n; j++) {
            double beta = j * delta, x = R + Math.cos(beta);
            float x1 = (float) (cosa * x), 
                  y1 = (float) (sina * x), 
                  z1 = (float) Math.sin(beta);
            fw.write(
            (i * n + j + 1) + " " + x1 + " " + y1 + " " + z1 + "\r\n");
         }
      }
      fw.write("Faces:\r\n");
      for (int i = 0; i < n; i++) {
         for (int j = 0; j < n; j++) {
            int i1 = (i + 1) % n, j1 = (j + 1) % n, 
                a = i * n + j + 1, b = i1 * n + j + 1, 
                c = i1 * n + j1 + 1, d = i * n + j1 + 1;
            fw.write(a + " " + b + " " + c + " " + d + ".\r\n");
         }
      }
      fw.close();
   }
   Steel() throws IOException {
      int n=29;
      float R=0.05f;
      float r=0.02f;
      float dx=0.1f;
      float dz=0.4f;
      String fileName="steel";
      FileWriter fw = new FileWriter(fileName+".dat");
      double delta = 2 * Math.PI / n;
      for (int i = 0; i < n; i++) {
         double alpha = i * delta, 
                cosa = Math.cos(alpha), sina = Math.sin(alpha);
                if(alpha<Math.PI/2){
                    dx=Math.abs(dx);
                    dz=Math.abs(dz);
                }else if(alpha<Math.PI){
                    dx=-Math.abs(dx);
                    dz=Math.abs(dz);
                }else if(alpha<3*Math.PI/2){
                    dx=-Math.abs(dx);
                    dz=-Math.abs(dz);
                }else if(alpha<2*Math.PI){
                    dx=Math.abs(dx);
                    dz=-Math.abs(dz);
                }
         for (int j = 0; j < n; j++) {
            double beta = j * delta, x = R + r*Math.cos(beta);
            float x1 = (float) (cosa * x+dx), 
                  //y1 = (float) (sina * x),
                  y1 = (float)- (r*Math.sin(beta)),
                  z1 =  (float) (sina * x+dz);
                    
            fw.write(
            (i * n + j + 1) + " " + x1 + " " + y1 + " " + z1 + "\r\n");
         }
      }
      fw.write("Faces:\r\n");
      for (int i = 0; i < n; i++) {
         for (int j = 0; j < n; j++) {
            int i1 = (i + 1) % n, j1 = (j + 1) % n, 
                a = i * n + j + 1, b = i1 * n + j + 1, 
                c = i1 * n + j1 + 1, d = i * n + j1 + 1;
            fw.write(a + " " + b + " " + c + " " + d + ".\r\n");
         }
      }
      fw.close();
   }
}
