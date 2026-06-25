import re
import unicodedata

STOPWORDS: set[str] = {
    "que", "dice", "libro", "antiguo", "nuevo", "sobre", "del", "los",
    "las", "una", "uno", "unos", "unas", "para", "como", "cual", "cuales",
    "cuando", "donde", "el", "la", "de", "en", "por", "con", "sin", "al",
    "se", "es", "son", "un", "sus", "has", "hay", "este", "esta", "estos",
    "estan", "ser", "fue", "han", "muy", "mas", "pero", "sino",
    "bien", "mal", "puede", "pueden",
}

LIST_TRIGGERS: set[str] = {
    "cuales", "tipos", "partes", "componentes", "enumera", "lista",
    "cuantos", "menciona", "diferencias", "caracteristicas", "niveles",
    "elementos", "funciones", "formas", "clases",
}

SYNONYMS: dict[str, list[str]] = {
    # Electricidad
    "voltaje":      ["voltaje", "voltage", "tension", "potencial"],
    "voltage":      ["voltage", "voltaje", "tension", "potencial"],
    "corriente":    ["corriente", "current", "intensidad"],
    "current":      ["current", "corriente", "intensidad"],
    "resistencia":  ["resistencia", "resistance", "resistor"],
    "resistance":   ["resistance", "resistencia", "resistor"],
    "potencia":     ["potencia", "power", "watt", "vatio"],
    "condensador":  ["condensador", "capacitor", "capacitancia"],
    "diodo":        ["diodo", "diode"],
    "transistor":   ["transistor"],
    "ohm":          ["ohm", "ohmio"],
    "ohmio":        ["ohmio", "ohm"],
    "amperio":      ["amperio", "ampere", "amperaje"],
    "ampere":       ["ampere", "amperio"],
    "voltio":       ["voltio", "volt", "voltaje"],
    "dc":           ["dc", "corriente continua", "direct current"],
    "ac":           ["ac", "corriente alterna", "alternating current"],
    "frecuencia":   ["frecuencia", "frequency", "hz", "hertz"],
    "circuito":     ["circuito", "circuit"],
    "conductor":    ["conductor", "conductora", "conductividad"],
    "aislante":     ["aislante", "aislamiento", "aislador"],
    "semiconductor": ["semiconductor"],
    "electrodo":    ["electrodo", "electrode"],
    "transformador": ["transformador", "transformer"],
    # Hardware / componentes
    "cpu":          ["cpu", "procesador", "microprocesador", "unidad central"],
    "procesador":   ["procesador", "cpu", "microprocesador"],
    "ram":          ["ram", "memoria ram", "memoria principal"],
    "memoria":      ["memoria", "ram", "almacenamiento temporal"],
    "ssd":          ["ssd", "solid state", "estado solido", "unidad solida"],
    "hdd":          ["hdd", "disco duro", "hard disk", "disco mecanico"],
    "disco":        ["disco", "hdd", "ssd", "almacenamiento"],
    "gpu":          ["gpu", "tarjeta grafica", "grafica", "video"],
    "mobo":         ["mobo", "placa base", "motherboard"],
    "mb":           ["mb", "placa base", "motherboard"],
    "placa":        ["placa base", "placa", "motherboard", "mainboard"],
    "motherboard":  ["motherboard", "placa base", "placa madre"],
    "fuente":       ["fuente de alimentacion", "fuente", "psu", "power supply"],
    "psu":          ["psu", "fuente de alimentacion", "fuente"],
    "cooler":       ["cooler", "disipador", "ventilador", "refrigeracion"],
    "disipador":    ["disipador", "cooler", "ventilador", "refrigeracion"],
    "caja":         ["caja", "chasis", "torre", "gabinete", "case"],
    "chasis":       ["chasis", "caja", "torre", "gabinete"],
    "socket":       ["socket", "zocalo", "ranura procesador"],
    "chipset":      ["chipset", "chip", "controlador"],
    "dimm":         ["dimm", "ranura ram", "slot ram", "modulo ram"],
    "sata":         ["sata", "serial ata", "conector sata"],
    "nvme":         ["nvme", "m.2", "m2", "pcie ssd"],
    "pcie":         ["pcie", "pci express", "ranura expansion"],
    "usb":          ["usb", "universal serial bus", "conector usb"],
    "hdmi":         ["hdmi", "salida video", "video digital"],
    "bios":         ["bios", "uefi", "firmware", "basic input output"],
    "uefi":         ["uefi", "bios", "firmware"],
    "alu":          ["alu", "unidad aritmetico logica"],
    "io":           ["io", "entrada salida", "input output"],
    "atx":          ["atx", "advanced technology", "factor forma"],
    # Conexiones / periféricos
    "puerto":       ["puerto", "conector", "conexion", "interfaz", "slot"],
    "conector":     ["conector", "conexion", "puerto", "interfaz"],
    "conexion":     ["conexion", "conector", "puerto", "enlace"],
    "periferico":   ["periferico", "dispositivo externo", "dispositivo entrada", "dispositivo salida"],
    "entrada":      ["entrada", "input", "teclado", "raton", "periferico entrada"],
    "salida":       ["salida", "output", "monitor", "impresora", "periferico salida"],
    "video":        ["video", "monitor", "pantalla", "hdmi", "vga", "displayport"],
    "pantalla":     ["pantalla", "monitor", "display", "video"],
    # Montaje y mantenimiento
    "montar":       ["montar", "ensamblar", "armar", "construir", "instalar componentes"],
    "ensamblar":    ["ensamblar", "montar", "armar", "construir"],
    "instalar":     ["instalar", "montar", "configurar", "poner"],
    "mantenimiento": ["mantenimiento", "limpieza", "revision", "preventivo"],
    "limpieza":     ["limpieza", "limpiar", "mantenimiento", "polvo"],
    "herramienta":  ["herramienta", "destornillador", "utensilio"],
    # Software / SO
    "sistema operativo": ["sistema operativo", "so", "os", "windows", "linux"],
    "so":           ["so", "sistema operativo", "os"],
    "windows":      ["windows", "sistema operativo", "so"],
    "linux":        ["linux", "sistema operativo", "so"],
    "particion":    ["particion", "particiones", "formatear", "disco"],
    "formatear":    ["formatear", "particion", "instalar so"],
    "driver":       ["driver", "controlador", "software hardware"],
    "controlador":  ["controlador", "driver", "software"],
}


def to_ascii(text: str) -> str:
    return unicodedata.normalize("NFKD", text).encode("ascii", "ignore").decode("ascii")


def keyword_variants(kw: str) -> list[str]:
    variants = [kw]
    if kw.endswith("es") and len(kw) > 4:
        variants.append(kw[:-2])
    elif kw.endswith("s") and len(kw) > 4:
        variants.append(kw[:-1])
    else:
        variants.append(kw + "s")
    return variants


def get_search_variants(kw: str) -> list[str]:
    base = SYNONYMS.get(kw, []) + keyword_variants(kw)
    return list(dict.fromkeys(base))


def extract_keywords(question: str) -> list[str]:
    normalized = to_ascii(question.lower())
    words = re.findall(r"\w+", normalized)
    keywords = [w for w in words if len(w) >= 2 and w not in STOPWORDS]
    seen: set[str] = set()
    unique = []
    for w in keywords:
        if w not in seen:
            seen.add(w)
            unique.append(w)
    return unique


def is_list_question(question: str) -> bool:
    normalized = to_ascii(question.lower())
    words = set(re.findall(r"\w+", normalized))
    return bool(words & LIST_TRIGGERS)


def normalize_question(text: str) -> str:
    result = to_ascii(text.lower())
    result = re.sub(r"[^\w\s]", " ", result)
    result = re.sub(r"\s+", " ", result).strip()
    return result


_INJECTION_PHRASES = [
    "ignore previous", "ignore all", "forget everything", "forget all",
    "act as", "you are now", "pretend you", "roleplay as", "jailbreak",
    "ignore instrucciones", "olvida todo", "olvida las instrucciones",
    "actua como", "ahora eres", "nuevo rol", "system:", "###", "<<<",
    "prompt injection", "dan mode", "developer mode",
]


def is_injection_attempt(question: str) -> bool:
    normalized = to_ascii(question.lower())
    return any(phrase in normalized for phrase in _INJECTION_PHRASES)
