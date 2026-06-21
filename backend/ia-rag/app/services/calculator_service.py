import ast
import operator
import re
from typing import Optional

# ── Safe arithmetic (+, -, *, /) ─────────────────────────────────────────────

_MAX_POWER_EXP = 1000  # evita resultados de miles de dígitos

_OPS = {
    ast.Add: operator.add,
    ast.Sub: operator.sub,
    ast.Mult: operator.mul,
    ast.Div: operator.truediv,
    ast.USub: operator.neg,
    ast.UAdd: operator.pos,
}


def _eval_node(node):
    if isinstance(node, ast.Constant) and isinstance(node.value, (int, float)):
        return float(node.value)
    if isinstance(node, ast.BinOp):
        if isinstance(node.op, ast.Pow):
            base = _eval_node(node.left)
            exp = _eval_node(node.right)
            if exp > _MAX_POWER_EXP:
                raise ValueError(f"Exponente demasiado grande (máximo {_MAX_POWER_EXP})")
            return float(base ** exp)
        fn = _OPS.get(type(node.op))
        if not fn:
            raise ValueError("Operador no soportado")
        return fn(_eval_node(node.left), _eval_node(node.right))
    if isinstance(node, ast.UnaryOp):
        fn = _OPS.get(type(node.op))
        if not fn:
            raise ValueError("Operador no soportado")
        return fn(_eval_node(node.operand))
    raise ValueError("Expresión no válida")


def _safe_eval(expr: str) -> float:
    expr = expr.strip().replace(",", ".")
    tree = ast.parse(expr, mode="eval")
    return _eval_node(tree.body)


def _fmt(n: float) -> str:
    if n == int(n) and abs(n) < 1e15:
        return str(int(n))
    return f"{n:.4f}".rstrip("0").rstrip(".")


# ── Natural language preprocessing ───────────────────────────────────────────

def _normalize_arith(q: str) -> str:
    q = q.lower()
    q = re.sub(r'\bpor\b', '*', q)
    q = re.sub(r'\bentre\b', '/', q)
    q = re.sub(r'\bmás\b|\bmas\b', '+', q)
    q = re.sub(r'\bmenos\b', '-', q)
    q = re.sub(r'\bsumado a\b', '+', q)
    q = re.sub(r'\bdividido entre\b|\bdividido por\b', '/', q)
    q = re.sub(r'\bmultiplicado por\b', '*', q)
    q = re.sub(r'\bal cuadrado\b', '**2', q)
    q = re.sub(r'\bal cubo\b', '**3', q)
    q = re.sub(r'\belevado a\b|\bpotencia\b', '**', q)
    q = q.replace("^", "**")
    return q


# ── Value extractor ──────────────────────────────────────────────────────────

def _get(patterns: list[str], text: str) -> Optional[float]:
    for pat in patterns:
        m = re.search(pat, text, re.IGNORECASE)
        if m:
            try:
                return float(m.group(1).replace(",", "."))
            except ValueError:
                pass
    return None


_V_PATS = [
    r'(?:voltaje|tensión|tension)\s*[=:]\s*([\d.,]+)',
    r'\bv\s*[=:]\s*([\d.,]+)',
]
_I_PATS = [
    r'(?:corriente|intensidad)\s*[=:]\s*([\d.,]+)',
    r'\bi\s*[=:]\s*([\d.,]+)',
]
_R_PATS = [
    r'(?:resistencia)\s*[=:]\s*([\d.,]+)',
    r'\br\s*[=:]\s*([\d.,]+)',
]


def _count_ohm_vars(q: str) -> int:
    return sum([
        _get(_V_PATS, q) is not None,
        _get(_I_PATS, q) is not None,
        _get(_R_PATS, q) is not None,
    ])


# ── Ohms law ────────────────────────────────────────────────────────────────

def _ohm(q: str) -> Optional[str]:
    V = _get(_V_PATS, q)
    I = _get(_I_PATS, q)
    R = _get(_R_PATS, q)
    known = sum(x is not None for x in [V, I, R])
    if known < 2:
        return None
    if V is None:
        return f"Ley de Ohm: V = I × R\nV = {_fmt(I)} × {_fmt(R)} = {_fmt(I * R)} V"
    if I is None:
        if R == 0:
            return "No se puede calcular I: resistencia es 0 Ω."
        return f"Ley de Ohm: I = V / R\nI = {_fmt(V)} / {_fmt(R)} = {_fmt(V / R)} A"
    if R is None:
        if I == 0:
            return "No se puede calcular R: corriente es 0 A."
        return f"Ley de Ohm: R = V / I\nR = {_fmt(V)} / {_fmt(I)} = {_fmt(V / I)} Ω"
    return (
        f"Verificación Ohm: I × R = {_fmt(I)} × {_fmt(R)} = {_fmt(I * R)} V "
        f"(V dado = {_fmt(V)} V)"
    )


# ── Series / parallel resistance ─────────────────────────────────────────────

def _extract_numbers(q: str) -> list[float]:
    return [float(v.replace(",", ".")) for v in re.findall(r'\b([\d]+(?:[.,]\d+)?)\b', q)]


def _series(q: str) -> Optional[str]:
    vals = _extract_numbers(q)
    if len(vals) < 2:
        return None
    total = sum(vals)
    parts = " + ".join(_fmt(v) for v in vals)
    return f"Circuito en serie: R_total = {parts} = {_fmt(total)} Ω"


def _parallel(q: str) -> Optional[str]:
    vals = _extract_numbers(q)
    if len(vals) < 2:
        return None
    if any(v == 0 for v in vals):
        return "No se puede calcular: hay una resistencia de 0 Ω en paralelo."
    total = 1 / sum(1 / v for v in vals)
    parts = " + ".join(f"1/{_fmt(v)}" for v in vals)
    return f"Circuito en paralelo: 1/R = {parts}\nR_total = {_fmt(total)} Ω"


# ── Arithmetic ────────────────────────────────────────────────────────────────

_ARITH_TRIGGERS = {
    "cuanto es", "cuánto es", "cuanto vale", "cuánto vale",
    "cuanto da", "cuánto da", "calcula", "calcular", "resultado de",
    "suma de", "resta de", "multiplica", "divide", "dividir",
    "cuanto son", "cuánto son",
}


def _has(q: str, triggers: set) -> bool:
    return any(t in q for t in triggers)


def _extract_expr(q: str) -> Optional[str]:
    q = q.replace(",", ".").replace("^", "**")
    m = re.search(
        r'((?:[\d.]+\s*(?:\*\*|[\+\-\*\/])\s*)+[\d.]+)',
        q,
    )
    return m.group(1).strip() if m else None


def _is_bare_expr(q: str) -> bool:
    """True si la pregunta ES directamente una expresión numérica (sin palabras extra)."""
    stripped = q.strip()
    return bool(
        re.fullmatch(r'[\d\s\+\-\*\/\.\,\^\(\)]+', stripped)
        and re.search(r'\d', stripped)
        and re.search(r'[\+\-\*\/\^]', stripped)
    )


# ── Public service ────────────────────────────────────────────────────────────

_CALC_SOURCE = [{"source": "Calculadora", "label": "Calculadora", "version": "static"}]

_SERIE_KW    = {"serie", "series", "en serie"}
_PARALELO_KW = {"paralelo", "paralelos", "en paralelo"}
_ELEC_KW     = {"ohm", "voltaje", "tensión", "tension", "corriente", "resistencia"}


class CalculatorService:
    def calculate(self, question: str) -> Optional[dict]:
        ql = question.lower().strip()

        # 1. Circuito en serie
        if _has(ql, _SERIE_KW) and re.search(r'\d', ql):
            r = _series(ql)
            if r:
                return {"answer": r, "sources": _CALC_SOURCE}

        # 2. Circuito en paralelo
        if _has(ql, _PARALELO_KW) and re.search(r'\d', ql):
            r = _parallel(ql)
            if r:
                return {"answer": r, "sources": _CALC_SOURCE}

        # 3. Ley de Ohm — con palabras clave o con 2+ variables v/i/r explícitas
        if (_has(ql, _ELEC_KW) or _count_ohm_vars(ql) >= 2) and re.search(r'\d', ql):
            r = _ohm(ql)
            if r:
                return {"answer": r, "sources": _CALC_SOURCE}

        # 4. Aritmética básica (con palabra clave o expresión directa)
        qn = _normalize_arith(ql)
        expr = _extract_expr(qn)
        if expr and (_has(ql, _ARITH_TRIGGERS) or _is_bare_expr(ql)):
            try:
                result = _safe_eval(expr)
                return {"answer": f"{expr.strip()} = {_fmt(result)}", "sources": _CALC_SOURCE}
            except ZeroDivisionError:
                return {"answer": "⚠️ No se puede dividir entre cero.", "sources": _CALC_SOURCE}
            except ValueError as e:
                if "grande" in str(e):
                    return {"answer": f"⚠️ Exponente demasiado grande (máximo {_MAX_POWER_EXP}).", "sources": _CALC_SOURCE}
            except Exception:
                pass

        return None


calculator_service = CalculatorService()
