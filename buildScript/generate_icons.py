import io
import os
import shutil
import struct
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
SOURCE = Path(r"D:\work3\Comet\logo.png")
if not SOURCE.exists():
    SOURCE = ROOT / "logo.png"

BACKGROUND_HEX = "#0F0C20"
BACKGROUND_RGBA = (15, 12, 32, 255)


def make_square_icon(im: Image.Image, target_size: int = 1024, padding_ratio: float = 0.0) -> Image.Image:
    alpha = im.split()[3]
    mask = alpha.point(lambda p: 255 if p > 5 else 0)
    bbox = mask.getbbox()
    if bbox:
        im = im.crop(bbox)
    w, h = im.size
    padding = int(target_size * padding_ratio)
    available = target_size - padding * 2
    scale = min(available / w, available / h)
    nw = int(w * scale)
    nh = int(h * scale)
    resized = im.resize((nw, nh), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0))
    offset = ((target_size - nw) // 2, (target_size - nh) // 2)
    canvas.paste(resized, offset, resized)
    return canvas


def make_tray_icon(im: Image.Image, target_size: int = 512) -> Image.Image:
    """Specialized high-visibility tray icon: fills 96% of height/width to display 2x larger in taskbar."""
    alpha = im.split()[3]
    mask = alpha.point(lambda p: 255 if p > 5 else 0)
    bbox = mask.getbbox()
    if bbox:
        im = im.crop(bbox)
    w, h = im.size
    # Scale so height fills 96% of the canvas
    scale = (target_size * 0.96) / h
    nw = int(w * scale)
    nh = int(h * scale)
    resized = im.resize((nw, nh), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0))
    offset = ((target_size - nw) // 2, (target_size - nh) // 2)
    canvas.paste(resized, offset, resized)
    # Ensure it stays within target_size bounds
    if canvas.size != (target_size, target_size):
        canvas = canvas.crop((0, 0, target_size, target_size))
    return canvas


def main():
    if not SOURCE.exists():
        raise FileNotFoundError(f"Source logo not found at {SOURCE}")

    # Copy to root logo.png and cometlogo.png if different
    if SOURCE.resolve() != (ROOT / "logo.png").resolve():
        shutil.copy2(SOURCE, ROOT / "logo.png")
    if SOURCE.resolve() != (ROOT / "cometlogo.png").resolve():
        shutil.copy2(SOURCE, ROOT / "cometlogo.png")

    raw_im = Image.open(SOURCE).convert("RGBA")
    square_1024 = make_square_icon(raw_im, 1024, padding_ratio=0.0)
    square_512 = square_1024.resize((512, 512), Image.Resampling.LANCZOS)
    square_256 = square_1024.resize((256, 256), Image.Resampling.LANCZOS)
    tray_512 = make_tray_icon(raw_im, 512)

    # 1. Windows icon (launcher/icon.ico)
    windows_ico = ROOT / "launcher" / "icon.ico"
    windows_ico.parent.mkdir(parents=True, exist_ok=True)
    square_256.save(
        windows_ico,
        format="ICO",
        sizes=[(16, 16), (24, 24), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)],
    )
    print(f"Generated {windows_ico}")

    # 2. Linux icon (release/linux/desktop/icon.png)
    linux_png = ROOT / "release" / "linux" / "desktop" / "icon.png"
    linux_png.parent.mkdir(parents=True, exist_ok=True)
    square_512.save(linux_png, format="PNG")
    print(f"Generated {linux_png}")

    # 3. macOS icon (release/macos/desktop/icon.icns)
    MACOS_ELEMENTS = {
        "icp4": 16,
        "icp5": 32,
        "ic11": 32,
        "ic12": 64,
        "ic07": 128,
        "ic08": 256,
        "ic13": 256,
        "ic09": 512,
        "ic14": 512,
        "ic10": 1024,
    }
    ICNS_MAGIC = b"icns"
    ICNS_HEADER_SIZE = 8

    payloads = {}
    for size in sorted(set(MACOS_ELEMENTS.values())):
        resized = square_1024.resize((size, size), Image.Resampling.LANCZOS)
        buf = io.BytesIO()
        resized.save(buf, format="PNG")
        payloads[size] = buf.getvalue()

    elements = b""
    for element, size in MACOS_ELEMENTS.items():
        payload = payloads[size]
        elements += struct.pack(">4sI", element.encode(), ICNS_HEADER_SIZE + len(payload)) + payload
    archive = struct.pack(">4sI", ICNS_MAGIC, ICNS_HEADER_SIZE + len(elements)) + elements

    macos_icns = ROOT / "release" / "macos" / "desktop" / "icon.icns"
    macos_icns.parent.mkdir(parents=True, exist_ok=True)
    macos_icns.write_bytes(archive)
    print(f"Generated {macos_icns}")

    # 4. Android playstore / Fastlane icons
    store_png = ROOT / "composeApp" / "src" / "androidMain" / "ic_launcher-playstore.png"
    store_png.parent.mkdir(parents=True, exist_ok=True)
    square_512.save(store_png, format="PNG")
    print(f"Generated {store_png}")

    fastlane_png = ROOT / "fastlane" / "metadata" / "android" / "en-US" / "images" / "icon.png"
    fastlane_png.parent.mkdir(parents=True, exist_ok=True)
    square_512.save(fastlane_png, format="PNG")
    print(f"Generated {fastlane_png}")

    # 5. Android legacy mipmaps (ic_launcher.webp)
    res_dir = ROOT / "composeApp" / "src" / "androidMain" / "res"
    legacy_sizes = {
        "mdpi": 48,
        "hdpi": 72,
        "xhdpi": 96,
        "xxhdpi": 144,
        "xxxhdpi": 192,
    }
    for density, size in legacy_sizes.items():
        legacy_im = square_1024.resize((size, size), Image.Resampling.LANCZOS)
        mask = Image.new("L", (size, size), 0)
        draw = ImageDraw.Draw(mask)
        draw.rounded_rectangle([(0, 0), (size - 1, size - 1)], radius=int(size * 0.18), fill=255)
        legacy_im.putalpha(mask)
        out_path = res_dir / f"mipmap-{density}" / "ic_launcher.webp"
        out_path.parent.mkdir(parents=True, exist_ok=True)
        legacy_im.save(out_path, format="WEBP", lossless=True)
        print(f"Generated {out_path}")

    # 6. Android adaptive icon foreground (ic_launcher_foreground.webp)
    adaptive_canvas_sizes = {
        "mdpi": 108,
        "hdpi": 162,
        "xhdpi": 216,
        "xxhdpi": 324,
        "xxxhdpi": 432,
    }
    for density, canvas_size in adaptive_canvas_sizes.items():
        fg = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
        content_size = int(canvas_size * 78 / 108)
        content_im = square_1024.resize((content_size, content_size), Image.Resampling.LANCZOS)
        offset = (canvas_size - content_size) // 2
        fg.paste(content_im, (offset, offset), content_im)

        out_path = res_dir / f"mipmap-{density}" / "ic_launcher_foreground.webp"
        out_path.parent.mkdir(parents=True, exist_ok=True)
        fg.save(out_path, format="WEBP", lossless=True)
        print(f"Generated {out_path}")

    # 7. Compose Multiplatform App Icon (for desktop Window and Sidebar)
    compose_drawable = ROOT / "composeApp" / "src" / "commonMain" / "composeResources" / "drawable"
    compose_drawable.mkdir(parents=True, exist_ok=True)
    comet_app_icon = compose_drawable / "comet_logo.png"
    square_512.save(comet_app_icon, format="PNG")
    print(f"Generated {comet_app_icon}")

    # 8. Compose Multiplatform Dedicated Tray Icon (2x visual size)
    comet_tray_icon = compose_drawable / "comet_tray_icon.png"
    tray_512.save(comet_tray_icon, format="PNG")
    print(f"Generated {comet_tray_icon}")


if __name__ == "__main__":
    main()
