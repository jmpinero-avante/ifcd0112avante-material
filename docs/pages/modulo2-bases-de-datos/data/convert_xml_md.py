#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import os
import re
import unicodedata
import xml.etree.ElementTree as ET
from pathlib import Path

# =========================
# Utilidades de texto
# =========================

def _normalize_newlines(s: str) -> str:
    return (s or "").replace("\r\n", "\n").replace("\r", "\n")

def _text(el: ET.Element) -> str:
    """Devuelve el CDATA/texto del elemento (sin tocar formato inline)."""
    return (el.text or "").strip()

def _strip_accents(s: str) -> str:
    s = unicodedata.normalize("NFKD", s)
    return "".join(c for c in s if not unicodedata.combining(c))

def slugify(value: str) -> str:
    value = _strip_accents(value.lower())
    value = re.sub(r"[^a-z0-9]+", "-", value)
    return value.strip("-")

# =========================
# Acumulador Markdown
# =========================

class MD:
    def __init__(self):
        self.lines = []

    def add(self, s: str = ""):
        self.lines.append(s)

    def blank(self, n: int = 1):
        for _ in range(n):
            if not self.lines or self.lines[-1] != "\n":
                self.lines.append("\n")

    def add_block(self, s: str):
        s = _normalize_newlines(s)
        if s and not s.endswith("\n"):
            s += "\n"
        self.lines.append(s)

    def extend(self, lines):
        self.lines.extend(lines)

    def write(self, fp):
        fp.writelines(self.lines)

# =========================
# Búsqueda de primera sección (para nombre de archivo)
# =========================

def find_first_section(root: ET.Element):
    """Devuelve el primer elemento <section> encontrado (en preorder)."""
    if root.tag.lower() == "section":
        return root
    found = root.find(".//section")
    return found

def make_filename_from_first_section(root: ET.Element, fallback_stem: str) -> str:
    first = find_first_section(root)
    title = _text(first) if first is not None else fallback_stem
    title_clean = _strip_accents(title).strip()
    m = re.match(r"^(\d+)[\.\-\s_]*", title_clean)
    if m:
        num = int(m.group(1))
        rest = title_clean[m.end():].strip()
        return f"{num:02d}-{slugify(rest)}.md"
    return f"{slugify(title_clean)}.md"

# =========================
# Render helpers (indentación para contenido dentro de listas)
# =========================

def _indent_block(block: str, spaces: int) -> str:
    if spaces <= 0:
        return block
    pad = " " * spaces
    return "\n".join(pad + ln if ln != "" else "" for ln in block.splitlines())

def _indent_lines(lines, spaces: int):
    if spaces <= 0:
        return list(lines)
    pad = " " * spaces
    out = []
    for ln in lines:
        if ln == "\n":
            out.append(ln)
        else:
            # mantener fin de línea
            if ln.endswith("\n"):
                out.append(pad + ln)
            else:
                out.append(pad + ln)
    return out

# =========================
# Renderizado principal
# =========================

def render_document(md: MD, root: ET.Element):
    """Renderiza todo el documento (root puede ser <section> o un contenedor)."""
    # Si el root es section, renderízalo con contexto (no tiene padre)
    if root.tag.lower() == "section":
        _render_section(md, root, parent=None, siblings=None, index_in_parent=None, indent=0)
    else:
        # Renderizar hijos en orden
        children = list(root)
        for i, child in enumerate(children):
            _render_element(md, child, parent=root, siblings=children, index_in_parent=i, indent=0)

    md.blank()

def _render_element(md: MD, el: ET.Element, parent, siblings, index_in_parent, indent: int):
    tag = el.tag.lower()

    # Secciones anidadas
    if tag == "section":
        _render_section(md, el, parent, siblings, index_in_parent, indent)
        return

    # Texto plano
    if tag == "text":
        _render_text(md, el, indent)
        return

    # Bloques de código
    if tag == "code":
        _render_code(md, el, indent)
        return

    # Listas (ordenadas o no)
    if tag == "list":
        _render_list(md, el, indent)
        return

    # Tablas
    if tag == "table":
        _render_table(md, el, indent)
        return

    # Diagramas SVG
    if tag == "diagram" and el.attrib.get("type") == "svg":
        _render_diagram_svg(md, el, indent)
        return

    # Elemento no reconocido → se ignora con aviso opcional
    if tag.strip():
        md.add(f"<!-- ⚠️ Etiqueta desconocida: {tag} -->\n")

def _render_section(md: MD, el: ET.Element, parent, siblings, index_in_parent, indent: int):
    # Encabezado
    try:
        level = int(el.attrib.get("level", "1"))
    except Exception:
        level = 1
    level = max(1, min(6, level))
    title = _text(el)

    md.blank()
    header = f"{'#' * level} {title}\n"
    md.add(header if indent == 0 else _indent_block(header, indent))
    md.blank()

    # Contenido interno (otros elementos y subsecciones)
    children = list(el)
    for i, ch in enumerate(children):
        _render_element(md, ch, parent=el, siblings=children, index_in_parent=i, indent=indent)

    # Al cerrar una section level=2: añadir divisor "---"
    # SOLO si hay otra sección después en el documento (a continuación en su lista de hermanos).
    if level == 2 and siblings is not None:
        # ¿Hay elemento posterior que sea <section>?
        has_next_section = False
        for j in range(index_in_parent + 1, len(siblings)):
            if siblings[j].tag.lower() == "section":
                has_next_section = True
                break
        if has_next_section:
            md.blank()
            md.add("---\n")
            md.blank()

def _render_text(md: MD, el: ET.Element, indent: int):
    txt = _text(el)
    if not txt:
        return
    block = txt + "\n"
    if indent:
        md.add(_indent_block(block, indent))
        md.blank()
    else:
        md.add_block(block)
        md.blank()

def _render_code(md: MD, el: ET.Element, indent: int):
    lang = el.attrib.get("type", "plain").lower()
    code = _normalize_newlines(_text(el))
    block = f"```{lang}\n{code.rstrip()}\n```\n"
    if indent:
        md.blank()
        md.add(_indent_block(block, indent))
        md.blank()
    else:
        md.blank()
        md.add(block + "\n")
        md.blank()

def _render_table(md: MD, el: ET.Element, indent: int):
    # Recoger filas/celdas
    rows = []
    for row in el.findall("./row"):
        cells = [ _text(c) for c in row.findall("./cell") ]
        rows.append(cells)
    if not rows:
        return

    # Normalizar columnas
    max_cols = max(len(r) for r in rows)
    for r in rows:
        while len(r) < max_cols:
            r.append("")

    # Markdown: primera fila = cabecera
    out = []
    out.append("| " + " | ".join(rows[0]) + " |\n")
    out.append("| " + " | ".join("---" for _ in rows[0]) + " |\n")
    for r in rows[1:]:
        out.append("| " + " | ".join(r) + " |\n")

    if indent:
        md.blank()
        md.extend(_indent_lines(out, indent))
        md.blank()
    else:
        md.blank()
        md.extend(out)
        md.blank()

def _render_list(md: MD, el: ET.Element, indent: int):
    ordered = el.attrib.get("ordered", "false").lower() in ("1", "true", "yes")
    items = el.findall("./item")
    idx = 0
    for item in items:
        idx += 1
        prefix = f"{idx}. " if ordered else "- "
        first_line = _text(item)

        # Línea del ítem
        line = prefix + first_line + "\n"
        if indent:
            md.add(_indent_block(line, indent))
        else:
            md.add(line)

        # Hijos del item (indentados 2 espacios adicionales)
        sub_children = list(item)
        if sub_children:
            for i, sub in enumerate(sub_children):
                # renderizamos en un buffer y luego indentamos +2
                sub_md = MD()
                _render_element(sub_md, sub, parent=item, siblings=sub_children, index_in_parent=i, indent=0)
                content = "".join(sub_md.lines)
                if content.strip():
                    indented = _indent_block(content.rstrip("\n"), (indent + 2))
                    md.add(indented + "\n")
        # separación entre ítems controlada
    md.blank()

def _render_diagram_svg(md: MD, el: ET.Element, indent: int):
    svg_code = _text(el)
    caption = el.attrib.get("caption")

    block_lines = []
    block_lines.append(f"<!-- svg-diagram start -->\n")

    if caption:
        # Solo crea la admonición si hay caption
        block_lines.append(f"!!! example \"{caption}\"\n")
        block_lines.append("<div class=\"diagram-block\" align=\"center\">\n")
        block_lines.append(svg_code.strip() + "\n")
        block_lines.append("</div>\n")
    else:
        # Inserta el SVG directamente sin admonición
        block_lines.append("<div class=\"diagram-block\" align=\"center\">\n")
        block_lines.append(svg_code.strip() + "\n")
        block_lines.append("</div>\n")

    block_lines.append(f"<!-- svg-diagram end -->\n")

    if indent:
        md.blank()
        md.extend(_indent_lines(block_lines, indent))
        md.blank()
    else:
        md.blank()
        md.extend(block_lines)
        md.blank()

# =========================
# Proceso principal
# =========================

def xml_to_markdown(xml_path: str, output_dir: str):
    tree = ET.parse(xml_path)
    root = tree.getroot()

    # Nombre de archivo
    filename = make_filename_from_first_section(root, Path(xml_path).stem)
    out_dir = Path(output_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    out_path = out_dir / filename

    # Render
    md = MD()
    render_document(md, root)
    with open(out_path, "w", encoding="utf-8") as f:
        md.write(f)

    print(f"✅ Generado: {out_path.name}")

# =========================
# CLI
# =========================

def main():
    if len(sys.argv) < 2:
        print("Uso: convert_xml_md.py <input.xml> [output_dir]")
        sys.exit(1)
    xml_file = sys.argv[1]
    output_dir = sys.argv[2] if len(sys.argv) > 2 else "."
    xml_to_markdown(xml_file, output_dir)

if __name__ == "__main__":
    main()
