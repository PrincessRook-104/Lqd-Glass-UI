package luma.ui;
public final class GlassMathTest {
  static void check(boolean ok,String m){if(!ok)throw new AssertionError(m);}
  public static void main(String[] a){
    int w=12,h=8;int[] px=new int[w*h],tmp=new int[w*h];
    for(int i=0;i<px.length;i++)px[i]=0xff000000|(((i%w)<6)?0xff0000:0x0000ff);
    GlassMath.blur(px,w,h,2,2,tmp);
    int edge=px[3*w+5],far=px[3*w+0];
    check(((edge>>16)&255)<255&&((edge>>16)&255)>0,"edge red should soften");
    check(((edge)&255)>0,"edge blue should bleed in");
    check(((far>>16)&255)>200,"far pixel stays mostly red");
    for(int p:px)check((p>>>24)==255,"opaque");
    int[] flat=new int[w*h];java.util.Arrays.fill(flat,0xff336699);GlassMath.blur(flat,w,h,3,3,new int[w*h]);
    for(int p:flat)check(p==0xff336699,"flat image unchanged");
    check(GlassMath.tint(0f)>GlassMath.tint(1f)&&GlassMath.tint(1f)==0f,"tint falls as glass gets clearer");
    check(GlassMath.tint(5f)==GlassMath.tint(1f)&&GlassMath.tint(-1f)==GlassMath.tint(0f),"tint clamps");
    check(Math.abs(GlassMath.captureScale(900,450)-1f/3f)<1e-6f&&Math.abs(GlassMath.captureScale(4800,2400)*4800-480)<1e-3f&&GlassMath.captureScale(0,0)==1f,"capture scale");
    check(GlassMath.mode(-3)==0&&GlassMath.mode(9)==2&&GlassMath.mode(1)==1,"mode clamp");
    check(GlassMath.blurRadius(0f)==1&&GlassMath.blurRadius(1f)==6&&GlassMath.blurRadius(9f)==6,"blur radius");
    check(GlassMath.veil(.5f)==0f&&GlassMath.veil(1f)==1f&&GlassMath.veil(0f)==-1f&&GlassMath.veil(3f)==1f,"veil");
    System.out.println("GlassMathTest passed");
  }
}
