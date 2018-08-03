import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.awt.image.BufferStrategy;
import java.awt.Toolkit;

public class CvPainter extends Canvas3D implements Runnable
{
    private int maxX,maxY,centerX,centerY;
    private Obj3D obj;
    private Point2D imgCenter;
    boolean running;
    public Graphics bufferGraphics1=null;
    public BufferStrategy bufferStrategy1=null;
    Thread thread;
    int nTria;
    Tria[] tr;
    int[] colorCode;
    int xMouseStart,yMouseStart,xMouseEnd,yMouseEnd,phiStart,thetaStart; 
    Obj3D getObj(){return obj;}
    void setObj(Obj3D obj){this.obj=obj;}
    
    int iX(float x){return Math.round(centerX+x-imgCenter.x);}
    int iY(float y){return Math.round(centerY-y+imgCenter.y);}
    void sort(Tria[] tr,int[] colorCode,float[] zTr,int l,int r){
        int i=l,j=r,wInt;
        float x=zTr[(i+j)/2], w;
        Tria wTria;
        do{
            while(zTr[i]<x){i++;}
            while(zTr[j]>x){j--;}
            if(i<j){
                w=zTr[i]; zTr[i]=zTr[j]; zTr[j]=w;
                wTria=tr[i]; tr[i]=tr[j]; tr[j]=wTria;
                wInt=colorCode[i]; colorCode[i]=colorCode[j]; colorCode[j]=wInt;
                i++;
                j--;
            }else if(i==j){
                i++;
                j--;
            }
        }while(i<=j);
        if(l<j){sort(tr,colorCode,zTr,l,j);}
        if(i<r){sort(tr,colorCode,zTr,i,r);}
    }
    
    public CvPainter(){
        this.thread=new Thread(this);
        running=true;
        this.addMouseListener(new MouseAdapter());
        this.addMouseWheelListener(new ScrollListener());
        this.addMouseMotionListener(new MotionListener());
    }
    @Override
    public void run(){
        while(running){
            
            //int dtheta=obj.getIntTheta()+2;
            //obj.setTheta(dtheta);
            
            doLogic();
            
            Draw();
            
            DrawBackBufferToScreen();
            
            //=======================================================
            Thread.currentThread();
            try{
                  Thread.sleep(17);  
            }catch(Exception e){
                  e.printStackTrace();
            }
        }
    }
    public void paint(Graphics g){
        if(bufferStrategy1==null){
            this.createBufferStrategy(2);
            bufferStrategy1=this.getBufferStrategy();
            bufferGraphics1=this.bufferStrategy1.getDrawGraphics();
            thread.start();
        }
    }
    public void doLogic(){
        if(obj==null){return;}
        Vector polyList=obj.getPolyList();
        if(polyList==null){return;}
        int nFaces=polyList.size();
        if(nFaces==0){return;}
        Dimension dim=getSize();
        maxX=dim.width-1;maxY=dim.height-1;
        centerX=maxX/2; centerY=maxY/2;
        
        
        //ze-axis towards eye, so ze-coordinates of object points
        //are all negative.
        //obj is a java object that contains all data:
        //-Vector w         (world coordinates)
        //-Array e          (eye coordinates)
        //-Array vScr       (screen coordinates)
        //-Vector polyList  (Polygon 3D objects)
        
        //Every Polygon3D value contains:
        //-Array 'nrs' for vertex numbers
        //-Values a,b,c,h for the plane ax+by+cz=h
        //-Array t (with nrs.length-2 elements of type Tria)
        
        //Every Tria value consists of the three vertex
        //numbers iA,iB and iC.
        obj.eyeAndScreen(dim);  //Computation of eye and screen coordinates.
        imgCenter=obj.getImgCenter();
        obj.planeCoeff(); //Computes the coefficients a,b,c,h for all faces
        
        //Constuct an array of triangles in
        //each polygon and count the total number of triangles.
        nTria=0;
        for (int j=0; j<nFaces;j++){
            Polygon3D pol=(Polygon3D)(polyList.elementAt(j));
            if(pol.getNrs().length<3 || pol.getH()>=0){continue;}
            pol.triangulate(obj);
            nTria+=pol.getT().length;
        }
        tr=new Tria[nTria];
        colorCode=new int[nTria];
        float[] zTr=new float[nTria];
        int iTria=0;
        Point3D[] e=obj.getE();
        Point2D[] vScr=obj.getVScr();
        for (int j=0;j<nFaces;j++){
            Polygon3D pol=(Polygon3D)(polyList.elementAt(j));
            if(pol.getNrs().length<3 || pol.getH()>=0){continue;}
            int cCode=obj.colorCode(pol.getA(),pol.getB(),pol.getC());
            Tria[] t=pol.getT();
            for(int i=0; i<t.length; i++){
                Tria tri=t[i];
                tr[iTria]=tri;
                colorCode[iTria]=cCode;
                float zA=e[tri.iA].z,zB=e[tri.iB].z,zC=e[tri.iC].z;
                zTr[iTria++]=zA+zB+zC;
            }
        }
        sort(tr,colorCode,zTr,0,nTria-1);
        
    }
     public void Draw(){
       bufferGraphics1=bufferStrategy1.getDrawGraphics();
        
       try{
            bufferGraphics1.clearRect(0,0,this.getSize().width,this.getSize().height);
            int iTria=0;
            Point2D[] vScr=obj.getVScr();
            for(iTria=0; iTria<nTria; iTria++){
            Tria tri=tr[iTria];
            Point2D A=vScr[tri.iA],
                    B=vScr[tri.iB],
                    C=vScr[tri.iC];
            int cCode=colorCode[iTria];
            int cCodeLimit=170;
           /* if(cCode>=cCodeLimit){
                bufferGraphics1.setColor(new Color(0,cCodeLimit,0));
            }else{
                bufferGraphics1.setColor(new Color(0,cCode,0));
            }*/
            bufferGraphics1.setColor(new Color(cCode,cCode,cCode));
            int[] x={iX(A.x), iX(B.x), iX(C.x)};
            int[] y={iY(A.y), iY(B.y), iY(C.y)};
            bufferGraphics1.fillPolygon(x, y, 3);
            bufferGraphics1.setColor(Color.black);
            //bufferGraphics1.drawPolygon(x,y,3);
        }
            
       }catch(Exception e){
            e.printStackTrace();
       }finally{
           bufferGraphics1.dispose();
       }
    }
    public void DrawBackBufferToScreen(){
        bufferStrategy1.show();
        Toolkit.getDefaultToolkit().sync();
    }
    class MouseAdapter implements MouseListener{
        @Override
        public void mouseClicked(MouseEvent mEvt){
            //System.out.println("Clicked");
        }
        public void mouseExited(MouseEvent mEvt){
            //System.out.println("Exited");
        }
        public void mouseEntered(MouseEvent mEvt){
            //System.out.println("Entered");
        }
        public void mouseReleased(MouseEvent mEvt){
        }
        public void mousePressed(MouseEvent mEvt){
            //System.out.println("Pressed");
            xMouseStart=mEvt.getX();
            yMouseStart=mEvt.getY();
            phiStart=obj.getIntPhi();
            thetaStart=obj.getIntTheta();
        }
    }
    class ScrollListener implements MouseWheelListener{
        @Override
        public void mouseWheelMoved(MouseWheelEvent mEvt){
            //System.out.println(mEvt.getWheelRotation());
            obj.setRho(obj.getIntRho()*(10+mEvt.getWheelRotation())/10);
            
        }
    }
    class MotionListener implements MouseMotionListener{
        @Override
        public void mouseMoved(MouseEvent motionEvt){
        }
        public void mouseDragged(MouseEvent motionEvt){
            //System.out.println("Dragged");
            xMouseEnd=motionEvt.getX();
            yMouseEnd=motionEvt.getY();
            int dx=xMouseEnd-xMouseStart,dy=yMouseEnd-yMouseStart;
            //System.out.println("dx= "+dx+"  dy= "+dy);
            //System.out.println("maxX= "+maxX+" maxY= "+maxY);
            float xRatio,yRatio;
            xRatio=100*dx/maxX;
            yRatio=100*dy/maxY;
            //System.out.println("xRatio= "+xRatio+" yRatio= "+ yRatio);
            int dPhi=-Math.round(yRatio*360/100),dTheta=-Math.round(xRatio*360/100);
            //System.out.println("Phi= "+obj.getIntPhi()+" Theta= "+obj.getIntTheta());
            //System.out.println("dPhi= "+dPhi+" dTheta= "+dTheta);
            
            obj.setPhi(phiStart+dPhi);
            obj.setTheta(thetaStart+dTheta);
        }
    }
}
