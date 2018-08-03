import java.awt.*;
import javax.swing.*;
public class OpenFile extends JFrame
{
   
    public OpenFile()
    {
        FileDialog fDia=new FileDialog(OpenFile.this,"Open",FileDialog.LOAD);
        String sDir="";
        fDia.setDirectory(sDir);
        fDia.setFile("*.dat");
        fDia.show();
        String sDir1=fDia.getDirectory();
        String sFile=fDia.getFile();
        String fName=sDir1+sFile;
        Obj3D obj=new Obj3D();
        new Fr3D(fName,new CvPainter());           
    }

    
   
}
