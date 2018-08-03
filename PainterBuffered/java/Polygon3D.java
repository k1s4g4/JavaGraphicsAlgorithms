import java.util.*;
public class Polygon3D
{
    private int[] nrs;
    private double a,b,c,h;
    private Tria[] t;
    
    public Polygon3D(Vector vnrs)
    {
        int n=vnrs.size();
        nrs=new int[n];
        for(int i=0;i<n;i++){
            nrs[i]=((Integer)vnrs.elementAt(i)).intValue();
        }
    }
    int[] getNrs(){return nrs;}
    double getA(){return a;}
    double getB(){return b;}
    double getC(){return c;}
    double getH(){return h;}
    
    void setAbch(double a,double b,double c,double h){
        this.a=a; this.b=b; this.c=c; this.h=h;
    }
    
    Tria[] getT(){return t;}
    
    void triangulate(Obj3D obj){
        int n=nrs.length;
        int[] next=new int[n];
        t=new Tria[n-2];
        Point2D[] vScr=obj.getVScr();
        int iA=0,iB,iC;
        int j=n-1;
        for(int i=0;i<n;i++){ next[j]=i; j=i;}
        for(int k=0;k<n-2;k++){
            Point2D A,B,C;
            boolean found=false;
            int count=0, nA=-1, nB=0, nC=0, nj;
            while(!found && ++count < n){
                iB=next[iA];iC=next[iB];
                nA=Math.abs(nrs[iA]); A=vScr[nA];
                nB=Math.abs(nrs[iB]); B=vScr[nB];
                nC=Math.abs(nrs[iC]); C=vScr[nC];
                if (Tools2D.area2(A,B,C)>=0){
                    j=next[iC]; nj=Math.abs(nrs[j]);
                    while(j!=iA && (nj==nA || nj==nB || nj==nC || !Tools2D.insideTriangle(A,B,C,vScr[nj]) )){
                        j=next[j]; nj=Math.abs(nrs[j]);
                    }
                    if(j==iA){
                        t[k]=new Tria(nA,nB,nC);
                        next[iA]=iC;
                        found=true;
                    }
                }
                iA=next[iA];
            }
            if(count==n){
                if(nA>=0){
                    t[k]=new Tria(nA,nB,nC);
                }else{
                    System.out.println("Nonsimple polygon");
                    System.exit(1); 
                }
            }
        }
    }
}
