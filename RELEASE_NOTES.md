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
