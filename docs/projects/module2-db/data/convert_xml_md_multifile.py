#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import re
import unicodedata
import textwrap
import xml.etree.ElementTree as ET
from pathlib import Path


# ─────────────────────────────────────────
# Utils
# ─────────────────────────────────────────
def slugify(text: str) -> str:
    """Convierte un título en un slug de archivo seguro."""
    text = unicodedata.normalize("NFKD", text).encode("ascii", "ignore").decode("ascii")
    text = text.lower()
    text = re.sub(r"[^\w\s-]", "", text)          # quita símbolos raros
    text = re.sub(r"[\s_-]+", "-", text).strip("-")  # espacios/guiones múltiples -> uno
    if not text:
        text = "seccion"
    return text


def safe_text(x):
    return (x or "").strip()


# ─────────────────────────────────────────
# Render de una sección (con subtítulos)
# ─────────────────────────────────────────
def render_section_content(section: ET.Element, level: int = 1) -> str:
    """
    Convierte una sección XML en Markdown.
    - Usa '#' * level para encabezados.
    - Soporta <text>, <list ordered="true|false">, <table>, <code type="sql|bash|..."> y subsections.
    """
    md = []

    # Título de la sección (H1 para secciones principales)
    title_text = safe_text(section.text)
    if title_text:
        md.append("#" * level + " " + title_text)

    for node in list(section):
        tag = node.tag.lower()

        if tag == "text":
            content = safe_text(node.text)
            if content:
                md.append(content)
                md.append("")  # salto de línea

        elif tag == "list":
            ordered = node.attrib.get("ordered", "false").lower() == "true"
            items = node.findall("item")
            for i, item in enumerate(items, 1):
                line = f"{i}. " if ordered else "- "
                md.append(line + safe_text(item.text))
            md.append("")

        elif tag == "table":
            rows = node.findall("row")
            if rows:
                # primera fila como cabecera
                headers = [safe_text(c.text) for c in rows[0].findall("cell")]
                md.append("| " + " | ".join(headers) + " |")
                md.append("| " + " | ".join(["---"] * len(headers)) + " |")
                for row in rows[1:]:
                    cells = [safe_text(c.text) for c in row.findall("cell")]
                    # rellena celdas faltantes
                    if len(cells) < len(headers):
                        cells += [""] * (len(headers) - len(cells))
                    md.append("| " + " | ".join(cells) + " |")
                md.append("")

        elif tag == "code":
            lang = node.attrib.get("type", "plain")
            code_text = textwrap.dedent(safe_text(node.text))
            md.append(f"```{lang}\n{code_text}\n```")
            md.append("")

        elif tag == "section":
            # subsección: aumenta el nivel de encabezado
            md.append(render_section_content(node, level + 1))

    return "\n".join(md).rstrip() + "\n"


# ─────────────────────────────────────────
# Primera pasada: recolecta títulos/archivos
# ─────────────────────────────────────────
def collect_main_sections(root: ET.Element):
    """
    Devuelve una lista de dicts con: index, title, filename, element
    Considera <section> de primer nivel como secciones principales.
    """
    sections = []
    main_sections = [n for n in list(root) if n.tag.lower() == "section"]

    for i, sec in enumerate(main_sections, 1):
        title = safe_text(sec.text) or f"Seccion {i}"
        filename = f"{i:02d}-{slugify(title)}.md"
        sections.append({
            "index": i,
            "title": title,
            "filename": filename,
            "element": sec
        })
    return sections


# ─────────────────────────────────────────
# Generación multi-archivo + índice + prev/next
# ─────────────────────────────────────────
def xml_to_markdown_multi(xml_path: str, output_dir: str):
    tree = ET.parse(xml_path)
    root = tree.getroot()

    out = Path(output_dir)
    out.mkdir(parents=True, exist_ok=True)

    # 1) Colecta secciones y decide nombres (primera pasada)
    sections = collect_main_sections(root)
    if not sections:
        raise SystemExit("⚠️ No se encontraron <section> principales en el XML.")

    # 2) Genera cada archivo conociendo prev/next
    for idx, info in enumerate(sections):
        title = info["title"]
        filename = info["filename"]
        element = info["element"]

        prev_file = sections[idx - 1]["filename"] if idx > 0 else None
        next_file = sections[idx + 1]["filename"] if idx < len(sections) - 1 else None

        front_matter = textwrap.dedent(f"""\
        ---
        title: "{title}"
        ---
        """)

        body = render_section_content(element, level=1)

        # Pie de navegación
        links = []
        if prev_file:
            links.append(f"[⬅️ Anterior](./{prev_file})")
        if next_file:
            links.append(f"[Siguiente ➡️](./{next_file})")

        footer = "\n---\n\n" + " | ".join(links) + ("\n" if links else "")

        (out / filename).write_text(front_matter + "\n" + body + footer, encoding="utf-8")
        print(f"✅ Generado {filename}")

    # 3) Crea index.md con el índice
    # Título del índice: toma atributo title del root, o usa "Contenido"
    index_title = root.attrib.get("title") or "Contenido"
    index_lines = [
        "---",
        f'title: "{index_title}"',
        "---",
        "",
        "# Índice de secciones",
        "",
    ]
    for i, info in enumerate(sections, start=1):
        index_lines.append(f"{i}. [{info['title']}](./{info['filename']})")

    (out / "index.md").write_text("\n".join(index_lines) + "\n", encoding="utf-8")
    print(f"📘 Índice generado: index.md")


# ─────────────────────────────────────────
# CLI
# ─────────────────────────────────────────
if __name__ == "__main__":
    import sys

    if len(sys.argv) != 3:
        print("Uso: xml_to_md.py <input.xml> <output_dir>")
        sys.exit(1)

    xml_to_markdown_multi(sys.argv[1], sys.argv[2])

