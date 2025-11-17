from pathlib import Path
path = Path('orders-service/src/main/java/com/tpibackend/orders/controller/SolicitudController.java')
for idx, line in enumerate(path.read_text(encoding='utf-8').splitlines(), 1):
    if 40 <= idx <= 70:
        print(idx, repr(line))
