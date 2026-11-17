package luma.ui;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RuntimeShader;
import android.graphics.Shader;
/** Android 13+ (AGSL) refraction pass. Only ever loaded behind an SDK_INT check in LiquidGlass; any failure there falls back to the frosted look. */
final class GlassAgsl {
  static final String SRC=
    "uniform shader sharpTex;\n"+
    "uniform shader blurTex;\n"+
    "uniform float2 origin;\n"+
    "uniform float2 size;\n"+
    "uniform float radius;\n"+
    "uniform float bezel;\n"+
    "uniform float strength;\n"+
    "uniform float dispersion;\n"+
    "uniform float sat;\n"+
    "uniform float alpha;\n"+
    "uniform float4 tint;\n"+
    "float sdBox(float2 p, float2 hb, float r) {\n"+
    "  float2 q = abs(p) - hb + float2(r);\n"+
    "  return min(max(q.x, q.y), 0.0) + length(max(q, float2(0.0))) - r;\n"+
    "}\n"+
    "half4 main(float2 fc) {\n"+
    "  float2 hb = size * 0.5;\n"+
    "  float2 p = fc - origin - hb;\n"+
    "  float d = sdBox(p, hb, radius);\n"+
    "  float depth = max(-d, 0.0);\n"+
    "  float2 g = float2(\n"+
    "    sdBox(p + float2(1.0, 0.0), hb, radius) - sdBox(p - float2(1.0, 0.0), hb, radius),\n"+
    "    sdBox(p + float2(0.0, 1.0), hb, radius) - sdBox(p - float2(0.0, 1.0), hb, radius));\n"+
    "  float gl = length(g);\n"+
    "  float2 n = gl > 0.0001 ? g / gl : float2(0.0);\n"+
    "  float t = clamp(depth / bezel, 0.0, 1.0);\n"+
    "  float lens = (1.0 - t) * (1.0 - t);\n"+
    "  float2 off = -n * (strength * bezel * lens);\n"+
    "  float2 sc = fc + off;\n"+
    "  float3 sharpC = float3(sharpTex.eval(sc + off * dispersion).r, sharpTex.eval(sc).g, sharpTex.eval(sc - off * dispersion).b);\n"+
    "  float3 blurC = blurTex.eval(sc).rgb;\n"+
    "  float edge = pow(1.0 - t, 1.5);\n"+
    "  float3 col = mix(blurC, sharpC, 0.9 * edge);\n"+
    "  float lum = dot(col, float3(0.299, 0.587, 0.114));\n"+
    "  col = mix(float3(lum), col, sat);\n"+
    "  col = mix(col, tint.rgb, tint.a);\n"+
    "  col += float3(0.10) * lens;\n"+
    "  return half4(half3(col * alpha), half(alpha));\n"+
    "}\n";
  private final RuntimeShader shader=new RuntimeShader(SRC);
  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Matrix sharpMatrix=new Matrix(),blurMatrix=new Matrix();
  private Bitmap sharpBitmap,blurBitmap;private BitmapShader sharpShader,blurShader;
  private static BitmapShader linear(Bitmap b){BitmapShader s=new BitmapShader(b,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);s.setFilterMode(BitmapShader.FILTER_MODE_LINEAR);return s;}
  void draw(Canvas c,Bitmap sharp,Bitmap blur,float x,float y,float w,float h,float radius,float bezel,float strength,float dispersion,float sat,int tintColor,float tintAlpha,float alpha){
    if(sharp!=sharpBitmap){sharpBitmap=sharp;sharpShader=linear(sharp);}
    if(blur!=blurBitmap){blurBitmap=blur;blurShader=linear(blur);}
    sharpMatrix.setScale(w/sharp.getWidth(),h/sharp.getHeight());sharpMatrix.postTranslate(x,y);sharpShader.setLocalMatrix(sharpMatrix);
    blurMatrix.setScale(w/blur.getWidth(),h/blur.getHeight());blurMatrix.postTranslate(x,y);blurShader.setLocalMatrix(blurMatrix);
    shader.setInputShader("sharpTex",sharpShader);shader.setInputShader("blurTex",blurShader);
    shader.setFloatUniform("origin",x,y);shader.setFloatUniform("size",w,h);shader.setFloatUniform("radius",radius);shader.setFloatUniform("bezel",bezel);
    shader.setFloatUniform("strength",strength);shader.setFloatUniform("dispersion",dispersion);shader.setFloatUniform("sat",sat);shader.setFloatUniform("alpha",alpha);
    shader.setFloatUniform("tint",((tintColor>>16)&255)/255f,((tintColor>>8)&255)/255f,(tintColor&255)/255f,tintAlpha);
    paint.setShader(shader);c.drawRoundRect(x,y,x+w,y+h,radius,radius,paint);paint.setShader(null);
  }
}
