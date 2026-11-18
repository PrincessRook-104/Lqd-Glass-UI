package luma.ui;
/** Pure-Java helpers for the liquid glass backdrop (host-testable, no Android imports). */
final class GlassMath {
  private GlassMath(){}
  static final int OFF=0,FROSTED=1,REFRACT=2,FOG=3,MODES=4;
  /** Separable box blur on opaque pixels, edge-clamped. Result ends in px; tmp is scratch of the same size. */
  static void blur(int[] px,int w,int h,int radius,int passes,int[] tmp){
    if(radius<1||passes<1||w<2||h<2)return;
    for(int i=0;i<passes;i++){line(px,tmp,w,h,radius,true);line(tmp,px,w,h,radius,false);}
  }
  private static void line(int[] src,int[] dst,int w,int h,int r,boolean horizontal){
    int lines=horizontal?h:w,len=horizontal?w:h,step=horizontal?1:w,stride=horizontal?w:1;int span=2*r+1;
    for(int l=0;l<lines;l++){int base=l*stride;int sr=0,sg=0,sb=0;
      for(int k=-r;k<=r;k++){int c=src[base+Math.max(0,Math.min(len-1,k))*step];sr+=(c>>16)&255;sg+=(c>>8)&255;sb+=c&255;}
      for(int i=0;i<len;i++){dst[base+i*step]=0xff000000|((sr/span)<<16)|((sg/span)<<8)|(sb/span);
        int add=src[base+Math.min(len-1,i+r+1)*step],rem=src[base+Math.max(0,i-r)*step];
        sr+=((add>>16)&255)-((rem>>16)&255);sg+=((add>>8)&255)-((rem>>8)&255);sb+=(add&255)-(rem&255);}}
  }
  private static float clamp(float v){return Math.max(0f,Math.min(1f,v));}
  /** Transparency slider (0 = solid tint, 1 = clear glass) to tint strength. */
  static float tint(float transparency){return .90f*(1f-clamp(transparency));}
  /** Backdrop refresh slider (0..1) to frames per second, 1..120 on a log scale so low rates stay easy to pick. */
  static int fps(float v){return Math.max(1,Math.min(120,Math.round((float)Math.exp(Math.log(120.0)*clamp(v)))));}
  static long frameMs(float v){return Math.max(1L,Math.round(1000.0/fps(v)));}
  /** Glass blur at (nearly) 0% means clear glass: the interior shows the live game instead of a captured copy. */
  static boolean isClear(float blurAmount){return blurAmount<.02f;}
  /** Slider values within 2% of an end snap to it, so 0% and 100% are easy to reach by touch. */
  static float snap(float v){float c=clamp(v);return c<.02f?0f:c>.98f?1f:c;}
  /** Glass blur slider to box-blur radius on the reduced backdrop (1..6). */
  static int blurRadius(float amount){return 1+Math.round(clamp(amount)*5f);}
  /** Brightness slider (0.5 neutral) to a signed white/black veil strength in -1..1. */
  static float veil(float brightness){return (clamp(brightness)-.5f)*2f;}
  /** Capture scale: about a third of the panel, capped so the longer side stays within 480 px and PixelCopy stays cheap. */
  static float captureScale(float width,float height){float longest=Math.max(width,height);return longest<=0f?1f:Math.min(1f/3f,480f/longest);}
  static int mode(int stored){return Math.max(0,Math.min(MODES-1,stored));}
}
