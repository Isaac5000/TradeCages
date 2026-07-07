from pathlib import Path
import math
import struct
import zlib

OUT = Path("src/main/resources/assets/trading_cells/textures/item/piglin_capturer.png")
W = H = 32
CX = CY = 15.5
R = 13.2


def chunk(tag: bytes, data: bytes) -> bytes:
    return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)


pixels = bytearray()
for y in range(H):
    pixels.append(0)  # PNG filter type 0
    for x in range(W):
        dx = x - CX
        dy = y - CY
        dist = math.hypot(dx, dy)

        if dist > R + 1.2:
            rgba = (0, 0, 0, 0)
        else:
            nx = dx / R
            ny = dy / R
            d = math.hypot(nx, ny)
            body = max(0.0, 1.0 - d)
            rim = max(0.0, 1.0 - abs(d - 0.92) / 0.18)
            glow = max(0.0, 1.0 - math.hypot((nx + 0.28) * 1.25, (ny + 0.34) * 1.45))
            shadow = max(0.0, 1.0 - math.hypot((nx - 0.28) * 1.1, (ny - 0.18) * 1.15))
            aura = max(0.0, 1.0 - (dist - R) / 1.2)

            # Red-themed base colors (piglin tone)
            rr = 186 + 48 * body + 28 * glow - 18 * shadow + 10 * rim
            gg = 70 + 40 * body + 26 * glow - 22 * shadow + 8 * rim
            bb = 62 + 20 * body + 18 * glow - 18 * shadow + 6 * rim
            aa = 70 + 110 * body + 28 * glow + 18 * rim + 22 * aura

            sparkle = max(0.0, 1.0 - math.hypot((nx + 0.50) * 3.2, (ny + 0.58) * 3.2))
            rr += 48 * sparkle
            gg += 20 * sparkle
            bb += 20 * sparkle
            aa += 30 * sparkle

            ember = max(0.0, 1.0 - math.hypot((nx - 0.10) * 1.8, (ny + 0.06) * 1.8))
            rr += 24 * ember
            gg += 6 * ember

            rgba = (
                max(0, min(255, int(rr))),
                max(0, min(255, int(gg))),
                max(0, min(255, int(bb))),
                max(0, min(255, int(aa))),
            )

        pixels.extend(rgba)

raw = bytes(pixels)
comp = zlib.compress(raw, 9)
png = (
    b"\x89PNG\r\n\x1a\n"
    + chunk(b"IHDR", struct.pack(">IIBBBBB", W, H, 8, 6, 0, 0, 0))
    + chunk(b"IDAT", comp)
    + chunk(b"IEND", b"")
)
OUT.write_bytes(png)
print(f"written {OUT}")
print(f"bytes {len(png)}")

