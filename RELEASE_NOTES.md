## Lqd Glass UI 1.6.1 · Refresh rate and background lock

- **Glass refresh rate** slider (Glass tab): how often the glass copies the game behind it, from **1 to 120 fps** (default 11 fps). Higher is smoother and costs more battery; the next capture only starts after the previous one finished, so the real rate can be lower on slow devices.
- **Background lock:** while the menu is open nothing reaches the game. A tap outside the panel closes the menu, and so now do an extra finger placed outside it and scrolling outside it with a mouse or trackpad. Mouse and stylus hovering no longer reaches the game either.
- Glass tab sliders are spaced slightly closer to fit the new one.

## Lqd Glass UI 1.6.0 · Fog (condensation)

- **New glass mode: Fog.** Pick it under **Glass → Liquid glass**. The panel gets a misted, frosted layer with water droplets on it.
- **Wipe it with your finger.** Touch or drag anywhere on the panel to clear the fog and the droplets under your finger. Menu controls still work while you wipe.
- **Droplets live.** They grow slowly, merge when they touch, and once they get heavy they run down the glass, leaving a clear trail and picking up smaller drops on the way. Some stop halfway.
- **Fog comes back.** Wiped areas mist over again over time.
- **New Fog tab** with Fog amount, Droplets, Drip speed, Wipe size and Fog regrow, plus a **Fog the glass again** button.
- Each droplet shows the game behind it upside down, like real water. The clear glass underneath keeps its refraction. **Glass blur** sets how frosted the fog looks.
- The sidebar is a little tighter to fit the extra tab.

## Lqd Glass UI 1.5.3 · Clear glass

- **Glass blur 0% now gives clear glass.** The panel interior shows the live game instead of a captured copy, so with **Transparency 100%** the menu is fully see-through. In Refraction mode only the rim bends and shimmers (an oil-slick look); in Frosted mode only the tint and rim remain.
- **Panel opacity can reach 0%.** It used to stop at about 68%.
- **New "Menu dim" slider** (Glass tab) for the dark veil behind the menu. 0% removes it; the default matches the old look.
- Sliders snap to 0% and 100% within 2% of either end, so the exact ends are easy to hit.
- With liquid glass on, the depth shadow no longer darkens the inside of the panel.

## Lqd Glass UI 1.5.2 · Renamed

- The mod is now called **Lqd Glass UI** (sidebar title, launcher entry and floating-button label). The file is `LqdGlassUI.levipack`. Internal ids are unchanged, so settings carry over.

## LumaUI 1.5.1 · Liquid glass tuning

- **New Glass tab** in the sidebar (after Appearance). The Off / Frosted / Refraction switch moved here from the Appearance plan.
- **Five sliders:** Refraction, Glass blur, Transparency, Brightness and Menu background blur.
- **Menu background blur:** while the menu is open, the game behind it is blurred and dimmed. Set it to 0% to turn it off (the second backdrop capture then stops too).
- Sidebar items are spaced a little closer to fit the new tab. Appearance sliders keep their original spacing.
- Reset restores all glass sliders (Refraction 55%, Glass blur 55%, Transparency 50%, Brightness 50%, Menu background blur 35%).

## LumaUI 1.5.0 · Liquid Glass

- **Liquid glass panel:** under **Appearance → Liquid glass** choose **Off**, **Frosted** or **Refraction**. The panel shows a blurred, saturated copy of the game behind it, tinted by your panel color. **Panel opacity** controls how strongly it is tinted.
- **Refraction (Android 13+):** an AGSL pass bends the backdrop along the panel's rounded edge with slight chromatic dispersion. Older devices, or any shader failure, fall back to Frosted automatically.
- **Specular rim:** a bright edge highlight and top sheen that follow your touch (uses **Glow follows touch**).
- Default is **Refraction**. The backdrop is sampled from the game SurfaceView about 11 times per second at reduced size, never recorded or uploaded; if it cannot be sampled the normal panel is used.
- Appearance sliders are spaced slightly closer to make room for the new row.

Android device testing is still needed to tune edge strength, blur amount and sampling alignment.

## LumaUI 1.4.0 · Spatial color & motion refinements

- **Spatial adaptive color:** the panel now samples a low-resolution grid behind its position, so differently colored parts of the background tint corresponding parts of the panel. The grid is blurred and smoothly transitions; accents still use the overall color.
- **HUD editor:** moved the toolbar into a touchable, non-modal application panel above the game. Use − / + on the selected launcher HUD element, drag the toolbar title to move it, and tap Done to exit. The launcher’s own HUD elements remain draggable.
- **Elastic opening:** keeps rendering the live panel through the opening instead of switching from a frozen mesh at the end; the opening stretch is monotonic. This targets the reported jerk.
- **Press feedback:** choose **Off**, **Soft**, or **Water** under **Button → Button press feedback**. Controls compress or squash and rebound when pressed, including the complete mod toggle. This is separate from touch ripples.
- **Scatter:** each opening picks fresh directions and staggered entrances. Text reveals letter by letter as sections and controls settle into place.

Adaptive sampling needs a compatible, accessible game SurfaceView and uses no recording or upload; unsupported surfaces fall back to manual colors. HUD editing depends on launcher internal APIs. Build and host/facade tests passed, but Android device testing is still needed to verify window layering, sampling alignment and animation feel.
