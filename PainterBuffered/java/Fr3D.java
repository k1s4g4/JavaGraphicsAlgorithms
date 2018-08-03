import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.KeyStroke;
public class Fr3D extends JFrame
{
    private JMenuItem open,exit,eyeUp,eyeDown,eyeLeft,eyeRight,incrDist,decrDist;
    private String sDir;
    private Canvas3D cv;
    private Obj3D obj;
    public Fr3D(String argFileName,Canvas3D cv)
    {
        super("Painter");
        this.requestFocus();
        addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){System.exit(0);}});
        Container cp=getContentPane();
        Dimension dim=getToolkit().getScreenSize();
        setSize(dim.width/2,dim.height/2);
        setLocationRelativeTo(null);
        this.cv=cv;
        JMenuBar mBar=new JMenuBar();
        setJMenuBar(mBar);
        JMenu mF=new JMenu("File"),
              mV=new JMenu("View");
        mBar.add(mF);
        mBar.add(mV);
        open=new JMenuItem("Open",KeyEvent.VK_O);
        KeyStroke ctrlO=KeyStroke.getKeyStroke("control O");
        open.setAccelerator(ctrlO);
        eyeDown=new JMenuItem("Viewpoint Down", KeyEvent.VK_DOWN);
        KeyStroke ctrlDown=KeyStroke.getKeyStroke("control DOWN");
        eyeDown.setAccelerator(ctrlDown);
        eyeUp=new JMenuItem("Viewpoint Up", KeyEvent.VK_UP);
        KeyStroke ctrlUp=KeyStroke.getKeyStroke("control UP");
        eyeUp.setAccelerator(ctrlUp);
        eyeLeft=new JMenuItem("Viewpoint Left", KeyEvent.VK_LEFT);
        KeyStroke ctrlLeft=KeyStroke.getKeyStroke("control LEFT");
        eyeLeft.setAccelerator(ctrlLeft);
        eyeRight=new JMenuItem("Viewpoint Right", KeyEvent.VK_RIGHT);
        KeyStroke ctrlRight=KeyStroke.getKeyStroke("control RIGHT");
        eyeRight.setAccelerator(ctrlRight);
        
        
        incrDist=new JMenuItem("Increase viewing distance", KeyEvent.VK_INSERT);
        KeyStroke ctrlIns=KeyStroke.getKeyStroke("control INSERT");
        incrDist.setAccelerator(ctrlIns);
        decrDist=new JMenuItem("Decrease viewing distance", KeyEvent.VK_DELETE);
        KeyStroke ctrlDel=KeyStroke.getKeyStroke("control DELETE");
        decrDist.setAccelerator(ctrlDel);
        exit=new JMenuItem("Exit", KeyEvent.VK_Q);
        
        mF.add(open);
        mF.add(exit);
        mV.add(eyeDown);
        mV.add(eyeUp);
        mV.add(eyeLeft);
        mV.add(eyeRight);
        mV.add(incrDist);
        mV.add(decrDist);
        
        MenuCommands mListener=new MenuCommands();
        open.addActionListener(mListener);
        exit.addActionListener(mListener);
        eyeDown.addActionListener(mListener);
        eyeUp.addActionListener(mListener);
        eyeLeft.addActionListener(mListener);
        eyeRight.addActionListener(mListener);
        incrDist.addActionListener(mListener);
        decrDist.addActionListener(mListener);
        
        
        cp.setLayout(new BorderLayout(10,10));
        cp.add(cv,BorderLayout.CENTER);
        
        
        
        if(argFileName!=null){
            obj=new Obj3D();
            if(obj.read(argFileName)){
                cv.setObj(obj);
            }
        }
        cv.setBackground(new Color(0,153,0));
        
        
        
        setVisible(true);
    }
    void vp(float dTheta,float dPhi,float fRho){
        obj=cv.getObj();
        obj.vp(cv,dTheta,dPhi,fRho);
    }
    class MenuCommands implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent evt){
            if(evt.getSource() instanceof JMenuItem){
                JMenuItem mi=(JMenuItem)evt.getSource();
                if(mi==open){
                    FileDialog fDia=new FileDialog(Fr3D.this,"Open",FileDialog.LOAD);
                    fDia.setDirectory(sDir);
                    fDia.setFile("*.dat");
                    fDia.show();
                    String sDir1=fDia.getDirectory();
                    String sFile=fDia.getFile();
                    String fName=sDir1+sFile;
                    Obj3D obj=new Obj3D();
                    if(obj.read(fName)){
                        sDir=sDir1;
                        cv.setObj(obj);
                    }
                }else if(mi==exit){
                    System.exit(0);
                }else if(mi==eyeDown){
                    vp(0F,.1F,1F);
                }else if(mi==eyeUp){
                    vp(0,-.1F,1);
                }else if(mi==eyeLeft){
                    vp(-.1F,0,1);
                }else if(mi==eyeRight){
                    vp(.1F,0,1);
                }else if(mi==incrDist){
                    vp(0,0,1.1F);
                }else if(mi==decrDist){
                    vp(0,0,.9F);
                }
            }
        }
    }
    
}
