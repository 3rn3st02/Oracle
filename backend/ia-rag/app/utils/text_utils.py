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
    "cpu":          ["cpu", "procesador", "unidad central"],
    "procesador":   ["procesador", "cpu"],
    "ram":          ["ram", "memoria ram"],
    "ssd":          ["ssd", "solid state", "estado solido"],
    "hdd":          ["hdd", "disco duro", "hard disk"],
    "gpu":          ["gpu", "tarjeta grafica", "grafica"],
    "mobo":         ["mobo", "placa base", "motherboard"],
    "mb":           ["mb", "placa base", "motherboard"],
    "atx":          ["atx", "advanced technology"],
    "uefi":         ["uefi", "firmware"],
    "bios":         ["bios", "firmware", "basic input output"],
    "pcie":         ["pcie", "pci express"],
    "usb":          ["usb", "universal serial bus"],
    "alu":          ["alu", "unidad aritmetico logica"],
    "io":           ["io", "entrada salida", "input output"],
    "conductor":    ["conductor", "conductora", "conductividad"],
    "aislante":     ["aislante", "aislamiento", "aislador"],
    "semiconductor": ["semiconductor"],
    "circuito":     ["circuito", "circuit"],
    "electrodo":    ["electrodo", "electrode"],
    "frecuencia":   ["frecuencia", "frequency", "hz", "hertz"],
    "placa":        ["placa base", "placa", "motherboard", "mainboard"],
    "socket":       ["socket", "zocalo"],
    "chipset":      ["chipset", "chip"],
    "dimm":         ["dimm", "ranura ram", "slot ram"],
    "sata":         ["sata", "serial ata"],
    "nvme":         ["nvme", "m.2", "m2"],
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
