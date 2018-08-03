import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class Painter extends JFrame
{   private static Fr3D myFr;
    public static void main(String[] args){
        myFr=new Fr3D("letterl.dat",new CvPainter());
        myFr.setVisible(true);
    }
}
