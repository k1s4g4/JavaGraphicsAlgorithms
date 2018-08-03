import java.io.*;

public class Input
{
    private File f;
    private FileInputStream fis;
    private int buf;
    private boolean ok=true;
    
    public Input(String fileName)
    {
        try{
            
            File f=new File(fileName);
            fis=new FileInputStream(f);
            buf=fis.read();
            
        }catch(IOException ioe){
            System.out.println("arxeio Input-fails1");
            ok=false;
        }
    }
    void close(){
        if(fis!=null){
            try{fis.close();}catch(IOException ioe){ok=false;}
        }
    }
    int readInt(){
        int x;
        boolean neg=false;
        while (Character.isWhitespace((char)buf)){nextChar();}
        if(buf=='-'){
            neg=true;
            nextChar();
        }
        if(!Character.isDigit((char)buf)){
            ok=false; 
            return 0;
        }
        x=buf-'0';
        while( nextChar() && (Character.isDigit((char)buf)) ){
            x=10*x+(buf-'0');
        }
        return(neg ? -x : x);
    }
    float readFloat(){
        float x;
        int nDec=-1;
        boolean neg=false;
        while(Character.isWhitespace((char)buf)){nextChar();}
        if(buf=='-'){
            neg=true;
            nextChar();
        }
        if(buf=='.'){
            nDec=0;
            nextChar();
        }
        if(!Character.isDigit((char)buf)){
            ok=false; 
            return 0;
        }
        x=buf-'0';
        while(nextChar() && (Character.isDigit((char)buf) || (nDec==-1 && buf=='.'))){
            if (buf =='.'){
                nDec=0;
            }else{
                x=10*x+(buf-'0');
                if (nDec>=0){
                    nDec++;
                }
            }
        }
        while(nDec>0){
            x*=0.1;
            nDec--;
        }
        if (buf=='e' || buf=='E'){
            nextChar();
            int exp=readInt();
            if(!fails()){
                while(exp>0){
                    x*=0.1;
                    exp++;
                }
                while(exp>0){
                    x*=10;
                    exp--;
                }
            }
        }
        return(neg ? -x : x);
    }
    char readChar(){
        char ch=(char)buf;
        nextChar();
        return ch;
    }
    boolean eof(){return !ok && buf<0;}
    boolean fails(){
        return !ok;
    }
    void clear(){ok=true;}
    private boolean nextChar(){
        if(buf<0){
            ok=false;
        }else{
            try{
                buf=fis.read();
            }catch(IOException ioe){
                ok=false;
            }
        }
        return ok;
    }
}
