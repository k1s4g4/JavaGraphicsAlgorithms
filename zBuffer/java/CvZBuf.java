import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.awt.image.BufferStrategy;
import java.awt.Toolkit;

public class CvZBuf extends Canvas3D implements Runnable
{
    private int maxX,maxY,centerX,centerY,maxX0=-1,maxY0=-1;
    private float buf[][];
    private Obj3D obj;
    private Point2D imgCenter;
    boolean running;
    public Graphics bufferGraphics1=null;
    public BufferStrategy bufferStrategy1=null;
    Thread thread;
    private int nFaces;
    private Vector polyList;
    Tria[] tr;
    int[] colorCode;
    int xMouseStart,yMouseStart,xMouseEnd,yMouseEnd,phiStart,thetaStart; 
    Obj3D getObj(){return obj;}
    void setObj(Obj3D obj){this.obj=obj;}
    
    int iX(float x){return Math.round(centerX+x-imgCenter.x);}
    int iY(float y){return Math.round(centerY-y+imgCenter.y);}
    
    
    
    public CvZBuf(){
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
        polyList=obj.getPolyList();
        if(polyList==null){return;}
        nFaces=polyList.size();
        if(nFaces==0){return;}
        Dimension dim=getSize();
        maxX=dim.width-1;maxY=dim.height-1;
        centerX=maxX/2; centerY=maxY/2;
        
        //ze-axis towards eye, so ze-coordinates of object points
        //are all negative.
        //since screen coordinates x and y are used to interpolate
        //for the z direction, we have to deal with 1/z
        //instead of z.With negative z, a small value of 1/z means
        //a small value of |z| for a nearby point.
        //We threfore begin with large buffer values 1e30
        if(maxX!=maxX0 || maxY!=maxY0){
            buf=new float [dim.width][dim.height];
            maxX0=maxX;
            maxY0=maxY;
        }
        for(int iy=0;iy<dim.height;iy++){
            for(int ix=0;ix<dim.width;ix++){
                buf[ix][iy]=1e30f;
            }
        }
        
        obj.eyeAndScreen(dim);  //Computation of eye and screen coordinates.
        imgCenter=obj.getImgCenter();
        obj.planeCoeff(); //Computes the coefficients a,b,c,h for all faces
        
    }
     public void Draw(){
       bufferGraphics1=bufferStrategy1.getDrawGraphics();
       Point3D[] e=obj.getE();
       Point2D[] vScr=obj.getVScr(); 
       try{
            bufferGraphics1.clearRect(0,0,this.getSize().width,this.getSize().height);
        for (int j=0; j<nFaces;j++){
            Polygon3D pol=(Polygon3D)(polyList.elementAt(j));
            if(pol.getNrs().length<3 || pol.getH()>=0){continue;}
            int cCode=obj.colorCode(pol.getA(),pol.getB(),pol.getC());
            bufferGraphics1.setColor(new Color(cCode,cCode,0));
            
            pol.triangulate(obj);
            tr=pol.getT();
            for(int i=0;i<tr.length;i++){
                Tria tri=tr[i];
                int iA=tri.iA,iB=tri.iB,iC=tri.iC;
                Point2D A=vScr[iA],B=vScr[iB],C=vScr[iC];
                double zAi=1/e[tri.iA].z,zBi=1/e[tri.iB].z,zCi=1/e[tri.iC].z;
                //computation of the coefficients a,b,c of the imaginary plane
                //ax+by+cz=h, where zi is 1/z and x,y,z are eye coordinates
                double u1=B.x-A.x, u2=B.y-A.y,
                       v1=C.x-A.x, v2=C.y-A.y;
                double c=u1*v2-u2*v1;
                if(c<=0){continue;}
                double xA=A.x,yA=A.y,
                       xB=B.x,yB=B.y,
                       xC=C.x,yC=C.y,
                       xD=(xA+xB+xC)/3,
                       yD=(yA+yB+yC)/3,
                       zDi=(zAi+zBi+zCi)/3,
                       u3=zBi-zAi,v3=zCi-zAi,
                       a=u2*v3-u3*v2,
                       b=u3*v1-u1*v3,
                       dzdx=-a/c,dzdy=-b/c,
                       yBottomR=Math.min(yA,Math.min(yB,yC)),
                       yTopR=Math.max(yA,Math.max(yB,yC));
                int yBottom=(int)Math.ceil(yBottomR),
                    yTop=(int)Math.floor(yTopR);
                for(int y=yBottom; y<=yTop; y++){
                    double xI,xJ,xK,xI1,xJ1,xK1,xL,xR;
                    xI=xJ=xK=1e30;
                    xI1=xJ1=xK1=-1e30;
                    if((y-yB)*(y-yC)<=0 && yB!=yC){
                        xI=xI1=xC+(y-yC)/(yB-yC)*(xB-xC);
                    }
                    if((y-yC)*(y-yA)<=0 && yC!=yA){
                        xJ=xJ1=xA+(y-yA)/(yC-yA)*(xC-xA);
                    }
                    if((y-yA)*(y-yB)<=0 && yA!=yB){
                        xK=xK1=xB+(y-yB)/(yA-yB)*(xA-xB);
                        //xL=xR=xI;
                    }
                    xL=Math.min(xI,Math.min(xJ,xK));
                    xR=Math.max(xI1,Math.max(xJ1,xK1));
                    int iy=iY((float)y),iXL=iX((float)(xL+0.5)),
                        iXR=iX((float)(xR-0.5));
                    double zi=1.01*zDi+(y-yD)*dzdy+(xL-xD)*dzdx;
                    /*
                     * for(int x=iXL; x<=iXR; x++){
                     *     if(zi< buf[][]){//is nearer
                     *         bufferGraphics1.drawline(x,iy,x,iy);
                     *         buf[x][iy]=(float)zi;
                     *     }
                     *     zi+=dzdx;
                     * }
                       */
                    //The above comment fragment is optimized below:
                    
                    boolean emptyStore=true;
                    int xLeftmost=0;
                    for(int ix=iXL;ix<=iXR;ix++){
                        if(zi < buf[ix][iy] ){//means nearer
                            if(emptyStore){
                                xLeftmost=ix;
                                emptyStore=false;
                            }
                            buf[ix][iy]=(float)zi;
                        }else if(!emptyStore){
                            bufferGraphics1.drawLine(xLeftmost,iy,ix-1,iy);
                            emptyStore=true;
                        }
                        zi+=dzdx;
                    }
                    if(!emptyStore){
                        bufferGraphics1.drawLine(xLeftmost,iy,iXR,iy);
                    }
                }
            }
        }  
       }catch(Exception exc){
            exc.printStackTrace();
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
