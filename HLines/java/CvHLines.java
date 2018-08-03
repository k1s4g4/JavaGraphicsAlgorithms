import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.awt.image.BufferStrategy;
import java.awt.Toolkit;

public class CvHLines extends Canvas implements Runnable
{
    private int maxX,maxY,centerX,centerY,nTria,nVertices;
    private Obj3D obj;
    private Point2D imgCenter;
    Tria[] tr;
    private HPGL hpgl;
    private int[] refPol;
    private int[][] connect;
    private int[] nConnect;
    private int chunkSize=4;
    private double hLimit;
    private Vector polyList;
    private float maxScreenRange;
    
    
    
    
    
    int xMouseStart,yMouseStart,xMouseEnd,yMouseEnd,phiStart,thetaStart; 
    boolean running;
    public Graphics bufferGraphics1=null;
    public BufferStrategy bufferStrategy1=null;
    Thread thread;
    
    Obj3D getObj(){return obj;}
    void setObj(Obj3D obj){this.obj=obj;}
    void setHPGL(HPGL hpgl){this.hpgl=hpgl;}
    
    
    
    int iX(float x){return Math.round(centerX+x-imgCenter.x);}
    int iY(float y){return Math.round(centerY-y+imgCenter.y);}
    
    
    
    public CvHLines(){
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
        int nFaces=polyList.size();
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
        
        //obj is a java object that contains all data,
        //with w , e and vScr parallel (with vertex numbers as index values)
        
        
        maxScreenRange=obj.eyeAndScreen(dim);
        imgCenter=obj.getImgCenter();
        obj.planeCoeff(); //Computes the coefficients a,b,c,h for all faces
        
        hLimit=-1e-6*obj.getRho();
        buildLineSet();
        
        nTria=0;
        for(int j=0;j<nFaces;j++){
            Polygon3D pol=(Polygon3D)(polyList.elementAt(j));
            if(pol.getNrs().length>2 && pol.getH()<=hLimit){
                pol.triangulate(obj);
                nTria+=pol.getT().length;
            }
        }
        tr=new Tria[nTria];
        refPol=new int[nTria];
        int iTria=0;
        for(int j=0; j<nFaces;j++){
            Polygon3D pol=(Polygon3D)(polyList.elementAt(j));
            Tria[] t=pol.getT();
            if(pol.getNrs().length>2 && pol.getH()<=hLimit){
                for(int i=0;i<t.length;i++){
                    Tria tri=t[i];
                    tr[iTria]=tri;
                    refPol[iTria++]=j;
                }
            }
        }
    }
     public void Draw(){
       bufferGraphics1=bufferStrategy1.getDrawGraphics();
       Point3D[] e=obj.getE();
       Point2D[] vScr=obj.getVScr(); 
       try{
            bufferGraphics1.clearRect(0,0,maxX,maxY);
            for(int i=0; i<nVertices ; i++){
                for(int j=0;j<nConnect[i]; j++){
                    int jj=connect[i][j];
                    lineSegment(bufferGraphics1,e[i],e[jj],vScr[i],vScr[jj],i,jj,0);
                }
            }
            hpgl=null;
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
    private void buildLineSet(){
        //Build the array
        //'connect' of int arrays, where
        //connect[i] is the array of all
        //vertex numbers j such that connect[i][j] is 
        //an edge of the 3D object
        polyList=obj.getPolyList();
        nVertices=obj.getVScr().length;
        connect=new int[nVertices][];
        nConnect=new int[nVertices];
        for(int i=0; i<nVertices ;i++){
            nConnect[i]=0;
        }
        int nFaces=polyList.size();
        for(int j=0;j<nFaces;j++){
            Polygon3D pol=(Polygon3D)(polyList.elementAt(j));
            int[] nrs=pol.getNrs();
            int n =nrs.length;
            if(n>2 && pol.getH()>0){continue;}
            int ii=Math.abs(nrs[n-1]);
            for(int k=0;k<n;k++){
                int jj=nrs[k];
                if(jj<0){
                    jj=-jj;
                }else{
                    int i1=Math.min(ii,jj),j1=Math.max(ii,jj),nCon=nConnect[i1];
                    //look if j1 is already present
                    int l;
                    for(l=0; l<nCon;l++){
                        if(connect[i1][l]==j1){break;}
                    }
                    if(l==nCon){ //j1 not found
                        if (nCon%chunkSize==0){
                            int[] temp=new int[nCon+chunkSize];
                            for(l=0;l<nCon;l++){
                                temp[l]=connect[i1][l];
                            }
                            connect[i1]=temp;
                        }
                        connect[i1][nConnect[i1]++]=j1;
                    }
                }
                ii=jj;
            }
        }
    }
    private String toString(float t){
        //From screen device units to HP-GL units(0-10000):
        int i=Math.round(5000+t*9000/maxScreenRange);
        String s="";
        int n=1000;
        for(int j=3;j>=0;j--){
            s+=i/n;
            i%=n;
            n/=10;
        }
        return s;
    }
    private String hpx(float x){return toString(x-imgCenter.x);}
    private String hpy(float y){return toString(y-imgCenter.y);}
    private void drawLine(Graphics g,float x1,float y1,float x2,float y2){
        if(x1 != x2 || y1 != y2){
            //System.out.println(iX(x1)+" "+iY(y1)+" "+iX(x1)+" "+iY(y2));
            g.drawLine(iX(x1),iY(y1),iX(x2),iY(y2));
            if(hpgl!=null){
                hpgl.write("PU;PA"+hpx(x1)+","+hpy(y1)+";");
                hpgl.write("PD;PA"+hpx(x2)+","+hpy(y2)+";"+"\r\n");
            }
        }
    }
    private void lineSegment(Graphics g,Point3D Pe,Point3D Qe,Point2D PScr,Point2D QScr,int iP,int iQ,int iStart){
        double u1=QScr.x-PScr.x,u2=QScr.y-PScr.y;
        double minPQx=Math.min(PScr.x,QScr.x);
        double maxPQx=Math.max(PScr.x,QScr.x);
        double minPQy=Math.min(PScr.y,QScr.y);
        double maxPQy=Math.max(PScr.y,QScr.y);
        double zP=Pe.z,zQ=Qe.z;
        double minPQz=Math.min(zP,zQ);
        Point3D[] e=obj.getE();
        Point2D[] vScr=obj.getVScr();
        for(int i=iStart;i<nTria;i++){
            Tria t=tr[i];
            int iA=t.iA, iB=t.iB, iC=t.iC;
            Point2D AScr=vScr[iA], BScr=vScr[iB], CScr=vScr[iC];
            g.setColor(new Color(0,255,0));
            //1. Minimax test for x and y screen coordinates:
            if(maxPQx<=AScr.x && maxPQx<=BScr.x && maxPQx<=CScr.x ||
               minPQx>=AScr.x && minPQx>=BScr.x && minPQx>=CScr.x ||
               maxPQy<=AScr.y && maxPQy<=BScr.y && maxPQy<=CScr.y ||
               minPQy>=AScr.y && minPQy>=BScr.y && minPQy>=CScr.y){
                   continue;
            }
            //2. Test if PQ is an edge of ABC:
            if( (iP==iA || iP==iB || iP==iC)&& (iQ==iA || iQ==iB || iQ==iC) ){continue;}
            
            //3.Test if PQ is clearly nearer than ABC
            Point3D Ae=e[iA],Be=e[iB],Ce=e[iC];
            double zA=Ae.z,zB=Be.z,zC=Ce.z;
            if( minPQz>=zA && minPQz>=zB && minPQz>=zC){continue;}
            
            //4.Do P and Q (in 2D) lie in a half plane defined by line AB,
            //  on the side other than of C? Similar for the edges BC and CA
            double eps=0.1; //Relative to numbers of pixels
            if( Tools2D.area2(AScr, BScr, PScr)<eps &&
                Tools2D.area2(AScr, BScr, QScr)<eps ||
                Tools2D.area2(BScr, CScr, PScr)<eps &&
                Tools2D.area2(BScr, CScr, QScr)<eps ||
                Tools2D.area2(CScr, AScr, PScr)<eps &&
                Tools2D.area2(CScr, AScr, QScr)<eps ){continue;}
            
            //5. Test (2D) if A,B,C lie on the same side of the 
            //   infinite line through P and Q:
            double PQA=Tools2D.area2(PScr,QScr,AScr);
            double PQB=Tools2D.area2(PScr,QScr,BScr);
            double PQC=Tools2D.area2(PScr,QScr,CScr);
            if( PQA<+eps && PQB<+eps && PQC <+eps ||
                PQA>-eps && PQB>-eps && PQC>-eps){continue;}
            
            //6.Test if neither P nor Q lies behind the infinite plane through A, B and C:
            int iPol=refPol[i];
            Polygon3D pol=(Polygon3D)(polyList.elementAt(iPol));
            double a=pol.getA(),b=pol.getB(),c=pol.getC(),h=pol.getH(),eps1=1e-5*Math.abs(h),
                   hP=a*Pe.x+b*Pe.y+c*Pe.z,hQ=a*Qe.x+b*Qe.y+c*Qe.z;
            if(hP>h-eps1 && hQ>h-eps1){continue;}
            
            //7.Test if both P and Q behind triangle ABC;
            boolean PInside=Tools2D.insideTriangle(AScr,BScr,CScr,PScr);
            boolean QInside=Tools2D.insideTriangle(AScr,BScr,CScr,QScr);
            if(PInside && QInside){return;}
            
            //8. if P is nearer than ABC and inside, PQ visible;
            //   the same for Q:
            double h1 =h+eps1;
            boolean PNear=hP>h1,QNear=hQ>h1;
            if(PNear && PInside || QNear && QInside){continue;}
            
            //9.Compute the intersections I and J if PQ
            // with ABC in 2D.
            // If this triangle does not obscure PQ.
            // Otherwise, the intersections lie behind ABC
            // and this tringle obscures part of PQ:
            double lambdaMin=1.0,lambdaMax=0.0;
            for(int ii=0;ii<3;ii++){
                double v1=BScr.x-AScr.x , v2=BScr.y-AScr.y,
                       w1=AScr.x-PScr.x , w2=AScr.y-PScr.y,
                       denom=u2*v1-u1*v2;
                if(denom!=0){
                    double mu=(u1*w2-u2*w1)/denom;
                    if(mu>-0.0001 && mu<1.0001){
                        double lambda=(v1*w2-v2*w1)/denom;
                        //lambda=PI/PQ
                        //(I is point of intersection)
                        if(lambda>-0.0001 && lambda <1.0001){
                            if(PInside != QInside && lambda>0.0001 && lambda<0.9999){
                                lambdaMin=lambdaMax=lambda;
                                break;
                            }
                            if(lambda<lambdaMin){
                                lambdaMin=lambda;
                            }
                            if(lambda>lambdaMax){
                                lambdaMax=lambda;
                            }
                        }
                        
                        
                    }
                }
                Point2D temp=AScr; 
                AScr=BScr;
                BScr=CScr;
                CScr=temp;
            }
            float d=obj.getD();
            if(!PInside && lambdaMin>0.001){
                double IScrx=PScr.x+lambdaMin*u1,
                       IScry=PScr.y+lambdaMin*u2;
                //back from screen to eye coordinates
                double zI=1/(lambdaMin/zQ+(1-lambdaMin)/zP),
                       xI=-zI*IScrx/d, yI=-zI*IScry/d;
                if(a*xI+b*yI+c*zI>h1){continue;}
                Point2D IScr=new Point2D((float)IScrx,(float)IScry);
                if(Tools2D.distance2(IScr,PScr)>=1.0){
                    lineSegment(g,Pe,new Point3D(xI,yI,zI),PScr,IScr,iP,-1,i+1);
                }
            }
            if(!QInside && lambdaMax<0.9999){
                double JScrx=PScr.x+lambdaMax*u1,
                       JScry=PScr.y+lambdaMax*u2;
                double zJ=1/(lambdaMax/zQ+(1-lambdaMax)/zP),
                       xJ=-zJ*JScrx/d, yJ=-zJ*JScry/d;
                if(a*xJ+b*yJ+c*zJ>h1){continue;}
                Point2D JScr=new Point2D((float)JScrx,(float)JScry);
                if(Tools2D.distance2(JScr,QScr)>=1.0){
                    lineSegment(g,Qe,new Point3D(xJ,yJ,zJ),QScr,JScr,iQ,-1,i+1);
                }
            }
            return; //if no continue-statement has been executed
        }
        drawLine(g,PScr.x,PScr.y,QScr.x,QScr.y);
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
