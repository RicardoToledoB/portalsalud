#!/usr/bin/env python3
import sys
from pathlib import Path
import qrcode

url = sys.argv[1] if len(sys.argv) > 1 else "https://portalesalud.dssm.cl/solicitud"
out = Path(sys.argv[2]) if len(sys.argv) > 2 else Path("docs/qr-portales-salud-solicitud.png")
out.parent.mkdir(parents=True, exist_ok=True)
img = qrcode.make(url)
img.save(out)
print(f"QR generado: {out} -> {url}")
