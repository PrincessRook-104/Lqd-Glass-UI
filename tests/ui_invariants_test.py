#!/usr/bin/env python3
"""Static guards for rendering and gesture lifecycle; not a substitute for Android UI testing."""
from pathlib import Path
s=Path('src/LumaUI.java').read_text()
sheet=Path('src/SettingsSheet.java').read_text()
assert 'if(!snapshotValid){Canvas snapshot=new Canvas(warpBitmap)' in s
assert 'snapshotValid=true;warpBitmap.prepareToDraw()' in s
assert 'warpBitmap.recycle()' not in s
assert 'if(act==MotionEvent.ACTION_UP&&!dragged&&!modBusy)activateModAt' in s
assert 'if(act==MotionEvent.ACTION_UP&&dragged&&modLayout==0' in s
assert 'ModBridge.renderSettings' in sheet and 'ModBridge.stopSettings(container)' in sheet
assert 'dispatchTouchEvent(MotionEvent event)' in sheet and 'super.dispatchTouchEvent(event)' in sheet
assert 'setClickable(false)' in sheet and 'animate&&!dialog.gestureActive' in sheet
assert 'ValueAnimator.areAnimatorsEnabled()' in sheet and 'closeAnimated(true)' in sheet
assert 'modLayout",selected' in s and 'saveModOffset()' in s and 'loadModOffset()' in s
assert 'if(filtering())return;modPages.set' in s and 'if(!filtering()&&modLayout!=0)' in s
assert 'ModSearch.filter(allModRows' in s and 'ModBridge.syncModules(getContext(),allModRows)' in s
assert '"Off","Lava Lamp","Space","Pop","Trail","Magnet"' in s and 'playField.pointer(' in s
assert 'playField.pressed=false' in s and 'drawPlayField(c,effect,mode,fade)' in s
assert 'finishBubbleEffect();recycleWarp();scatterSeed=' in s and 'hudEditor.close(false)' in s
assert 'revealBubble(buttonReturn,target[4])' in s and 'setVisibility(View.INVISIBLE)' in s
assert '"Off","Portal","Pulse","Orbit"' in s and 'putInt("buttonReturn",buttonReturn)' in s
assert 'backgroundBlend.select(selected)' in s and 'backgroundBlend.step(dt,target[4])' in s
assert 'addOnPreDrawListener(styler)' in sheet and 'categories.get(view)!=view.getBackground()' in sheet
assert 'animateCategoryContent(list,duration)' in sheet and 'super.dispatchTouchEvent(event)' in sheet
assert 'setTrackActiveTintList' in sheet and 'blend(panel,accent' in sheet
assert 'putBoolean("glowFollow",glowFollow)' in s and 'if(glowFollow&&(act==' in s
assert 'playField.pointer((px-body)' in s and 'lava.render' in s
assert 'if(!shown)return false;' in s and 'snapshotMix(open)' in s
assert 'PixelCopy.request(surface' in Path('src/AdaptiveSampler.java').read_text()
assert 'putBoolean("adaptiveColor",adaptiveColor)' in s and 'sampler.stop()' in s
assert 'HudEditor.start(' in s and '"Mods","Appearance","Glass","Fog","Motion","Effects","HUD Editor"' in s
assert '"Fade","Portal","Elastic","Scatter"' in s
assert 'transitionStyle==2&&!shown&&open<1f' in s
assert 'adaptiveGrid.accept(pixels)' in s and 'adaptiveBitmap,null,new RectF(x,y,x+w,y+h)' in s
assert 'pressTransform(c,left,top,right,bottom)' in s and 'putInt("pressStyle",pressStyle)' in s
assert 'scatterProgress=transitionStyle==3?0:1' in s and 'TransitionMath.visibleCharacters' in s
hud=Path('src/HudEditor.java').read_text()
assert 'TYPE_APPLICATION_PANEL' in hud and 'FLAG_NOT_TOUCH_MODAL' in hud and 'window.addView(tools,windowParams)' in hud
assert 'window.removeViewImmediate(tools)' in hud
gl=Path('src/LiquidGlass.java').read_text()
assert 'putInt("glassMode",glassMode)' in s and 'glass.draw(c,x,y,w,h' in s and 'stopGlass()' in s
assert 'PixelCopy.request(surface' in Path('src/GlassSampler.java').read_text()
assert 'SDK_INT>=33' in gl and 'isHardwareAccelerated()' in gl and 'catch(Throwable' in gl
assert 'drawBackdrop(c,shownValue[10],open)' in s and 'tab==7' in s and 'glassSampler.configure(' in s
assert 'condensation.wipe(' in s and 'condensation.step(' in s and 'condensation.draw(' in s and 'condensation.refog()' in s
assert 'DST_IN' in Path('src/Condensation.java').read_text()
assert 'onGenericMotionEvent' in s and 'ACTION_POINTER_DOWN' in s and 'glassSampler.interval(' in s and 'GlassMath.fps(' in s
print('UI snapshot, search, background and gesture lifecycle guards passed')
