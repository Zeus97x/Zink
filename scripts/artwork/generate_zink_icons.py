"""Regenerate ZInk launcher/splash rasters from the canonical vector artwork.
Requires cairosvg and Pillow. Run from any directory with python3.
"""
from pathlib import Path
import io
import cairosvg
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
# Keep the full silhouette inside the adaptive icon's central 66dp safe zone.
PATHS = [
    ('#FFB52E', 'M59 26H70L59 43L68 41L53 59L56 46L47 48Z'),
    ('#F7F1E5', 'M42 61H66Q68 61 68 63Q68 65 66 65H64V69C74 72 76 78 76 81Q76 83 73 83H35Q32 83 32 81C32 74 36 71 44 69V65H42Q40 65 40 63Q40 61 42 61Z'),
    ('#202124', 'M43 73C39 75 37 78 37 80Q37 81 39 81C39 78 41 76 45 73Z'),
    ('#F7F1E5', 'M44 56C35 52 34 49 37 48C40 47 43 52 44 56Z'),
    ('#F7F1E5', 'M65 57C66 52 70 51 71 54C72 56 68 57 65 57Z'),
    ('#F7F1E5', 'M43 59A1.5 1.5 0 1 0 40 59A1.5 1.5 0 1 0 43 59Z'),
]

def svg(background=None, roundness=0, mono=False):
    bg = f'<rect width="108" height="108" rx="{roundness}" fill="{background}"/>' if background else ''
    paths = ''.join(f'<path fill="{("#FFFFFF" if mono and c != "#202124" else c)}" d="{d}"/>' for c, d in PATHS if not (mono and c == '#202124'))
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108">{bg}<g transform="translate(5.4 5.4) scale(0.9)">{paths}</g></svg>\n'

def vector(mono=False):
    paths = ''.join(f'    <path android:fillColor="{("#FFFFFF" if mono else c)}" android:pathData="{d}" />\n' for c, d in PATHS if not (mono and c == '#202124'))
    return '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">\n' + '    <group android:scaleX="0.9" android:scaleY="0.9" android:translateX="5.4" android:translateY="5.4">\n' + paths + '    </group>\n</vector>\n'

(ROOT / 'artwork/zink-icon.svg').write_text(svg('#202124', 24))
(ROOT / 'artwork/zink-mark.svg').write_text(svg())
for path, content in {
    'app/src/main/res/drawable/ic_zink_logo.xml': vector(),
    'app/src/main/res/drawable/ic_zink_monochrome_launcher.xml': vector(True),
    'app/src/main/res/drawable/ic_tachi.xml': vector(True),
    'app/src/main/res/drawable/ic_launcher_foreground.xml': vector(),
    'app/src/debug/res/drawable/ic_launcher_foreground.xml': vector(),
}.items():
    (ROOT / path).write_text(content)
for density, size in [('mdpi',48), ('hdpi',72), ('xhdpi',96), ('xxhdpi',144), ('xxxhdpi',192)]:
    for source_set in ['main','debug']:
        folder = ROOT / f'app/src/{source_set}/res/mipmap-{density}'
        for name, radius in [('ic_launcher',24), ('ic_launcher_round',54)]:
            cairosvg.svg2png(bytestring=svg('#202124', radius).encode(), write_to=str(folder / f'{name}.png'), output_width=size, output_height=size)
    splash = Image.open(io.BytesIO(cairosvg.svg2png(bytestring=svg().encode(), output_width=size, output_height=size)))
    splash.save(ROOT / f'app/src/main/res/drawable-{density}/splash_icon.webp', lossless=True)
cairosvg.svg2png(bytestring=svg('#202124',24).encode(), write_to=str(ROOT / 'app/src/main/ic_launcher-web.png'), output_width=512, output_height=512)
cairosvg.svg2png(bytestring=svg('#202124',24).encode(), write_to=str(ROOT / 'artwork/zink-icon-preview.png'), output_width=512, output_height=512)
