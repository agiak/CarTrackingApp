"""Rebuild the Cariboo launcher icon from the glyph already in the repo.

The existing foreground had the background baked into it as a beige square
that did not fill the adaptive canvas, so launchers masked a white border
around a beige tile. This pulls the white glyph out of that square as a clean
alpha mask and redraws every layer from it:

  foreground  white glyph, fully transparent elsewhere
  monochrome  the same mask, for themed icons
  legacy      teal tile / teal circle with the glyph on it, for pre-API-26
"""
import pathlib

from PIL import Image, ImageDraw

ROOT = pathlib.Path("/home/user/CarTrackingApp/app/src/main/res")
# A copy taken before the first run, not the file in res/: this script
# overwrites that one, so reading it back would extract the glyph from its own
# output and resample it a second time.
SRC = pathlib.Path(__file__).parent / "launcher_glyph_source.webp"

TEAL = (77, 182, 172, 255)          # #4DB6AC, confirmed against the store listing
BEIGE = (221, 189, 168)             # the square baked into the old foreground
WHITE = (249, 249, 249)             # the glyph drawn on top of it
SQUARE = (69, 69, 363, 363)         # its bounds in the 432px source

# 108dp adaptive canvas; content kept inside the 66dp safe zone so no launcher
# mask can clip the antlers.
ADAPTIVE = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}
LEGACY = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}

# The 66dp safe zone is a *circle*, not a square, so a tall glyph sized to 66dp
# high has its top and bottom cut off by a round mask — which is what happened
# to the antlers on the first pass. This is the height at which the glyph's
# whole bounding box fits inside that circle, with a little back for the fact
# that its corners are empty where the antlers taper.
SAFE_FRACTION = 0.52
LEGACY_FRACTION = 0.72              # legacy tiles are not masked, so the glyph
                                    # can sit larger on them


def glyph_mask() -> Image.Image:
    """The white glyph as an L-mode alpha mask, cropped to its ink."""
    src = Image.open(SRC).convert("RGBA")
    px = src.load()
    x0, y0, x1, y1 = SQUARE
    delta = [WHITE[i] - BEIGE[i] for i in range(3)]
    denom = sum(d * d for d in delta)

    mask = Image.new("L", src.size, 0)
    mp = mask.load()
    for y in range(y0, y1):
        for x in range(x0, x1):
            r, g, b, a = px[x, y]
            if a < 8:
                continue
            # How far this pixel has travelled from the beige square towards
            # the white glyph. Antialiased edges land in between and keep
            # their partial alpha, so the curve stays smooth.
            t = sum((v - BEIGE[i]) * delta[i] for i, v in enumerate((r, g, b))) / denom
            mp[x, y] = max(0, min(255, round(t * 255)))

    return mask.crop(mask.getbbox())


def placed(mask: Image.Image, canvas: int, fraction: float) -> Image.Image:
    """The mask scaled to [fraction] of [canvas] and centred on it."""
    target_h = round(canvas * fraction)
    scale = target_h / mask.height
    resized = mask.resize(
        (max(1, round(mask.width * scale)), target_h), Image.LANCZOS
    )
    out = Image.new("L", (canvas, canvas), 0)
    out.paste(
        resized,
        ((canvas - resized.width) // 2, (canvas - resized.height) // 2),
    )
    return out


def white_on(mask: Image.Image) -> Image.Image:
    layer = Image.new("RGBA", mask.size, (255, 255, 255, 0))
    layer.putalpha(mask)
    return layer


def main() -> None:
    mask = glyph_mask()
    print(f"glyph ink {mask.width}x{mask.height}")

    for density, size in ADAPTIVE.items():
        out = ROOT / f"mipmap-{density}"
        shape = placed(mask, size, SAFE_FRACTION)

        white_on(shape).save(out / "ic_launcher_cariboo1_foreground.webp", lossless=True)
        # The themed-icon layer is the same silhouette: the system supplies
        # both colours, and only the alpha is read.
        white_on(shape).save(out / "ic_launcher_cariboo1_monochrome.webp", lossless=True)

    for density, size in LEGACY.items():
        out = ROOT / f"mipmap-{density}"
        shape = placed(mask, size, LEGACY_FRACTION)
        glyph = white_on(shape)

        square = Image.new("RGBA", (size, size), TEAL)
        square.alpha_composite(glyph)
        square.save(out / "ic_launcher_cariboo1.webp", lossless=True)

        circle = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        # Drawn at 4x and shrunk: a circle rasterised straight at 48px has a
        # visibly stepped edge.
        up = size * 4
        disc = Image.new("L", (up, up), 0)
        ImageDraw.Draw(disc).ellipse((0, 0, up - 1, up - 1), fill=255)
        disc = disc.resize((size, size), Image.LANCZOS)
        tinted = Image.new("RGBA", (size, size), TEAL)
        tinted.putalpha(disc)
        circle.alpha_composite(tinted)
        circle.alpha_composite(glyph)
        circle.save(out / "ic_launcher_cariboo1_round.webp", lossless=True)

    print("done")


if __name__ == "__main__":
    main()
