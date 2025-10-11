#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import xml.etree.ElementTree as ET
import textwrap
import re
import unicodedata
from pathlib import Path


# ─────────────────────────────────────────
# Utilidades
# ─────────────────────────────────────────
def safe_text(x: str) -> str:
    return (x or "").strip()


def normalize_title(title: str) -> str:
    """Convierte el título al formato '01 Texto...' si empieza con un número."""
    title = title.strip()
    match = re.match(r"^(\d+)([\s.-]+)(.*)", title)
    if match:
        num = int(match.group(1))
        rest = match.group(3)
        return f"{num:02d} {rest.strip()}"
    return title


def slugify(text: str) -> str:
    """Convierte un título en un slug seguro para usar como nombre de archivo."""
    text = unicodedata.normalize("NFKD", text).encode("ascii", "ignore").decode("ascii")
    text = text.lower()
    text = re.sub(r"[^\w\s-]", "", text)
    text = re.sub(r"[\s_-]+", "-", text).strip("-")
    return text or "documento"


# ─────────────────────────────────────────
# Conversión XML → Markdown
# ─────────────────────────────────────────
def render_section_content(section: ET.Element, level: int = 1) -> str:
    """Convierte recursivamente una sección XML en Markdown."""
    md = []
    title_text = safe_text(section.text)
    if title_text:
        md.append("#" * level + " " + title_text)

    for node in list(section):
        tag = node.tag.lower()

        if tag == "text":
            content = safe_text(node.text)
            if content:
                md.append(content)
                md.append("")

        elif tag == "list":
            ordered = node.attrib.get("ordered", "false").lower() == "true"
            for i, item in enumerate(node.findall("item"), 1):
                prefix = f"{i}. " if ordered else "- "
                md.append(prefix + safe_text(item.text))
            md.append("")

        elif tag == "table":
            rows = node.findall("row")
            if rows:
                headers = [safe_text(c.text) for c in rows[0].findall("cell")]
                md.append("| " + " | ".join(headers) + " |")
                md.append("| " + " | ".join(["---"] * len(headers)) + " |")
                for row in rows[1:]:
                    cells = [safe_text(c.text) for c in row.findall("cell")]
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
            md.append(render_section_content(node, level + 1))

    return "\n".join(md).rstrip() + "\n"


# ─────────────────────────────────────────
# Localiza la primera sección con texto real
# ─────────────────────────────────────────
def find_first_nonempty_section(root: ET.Element) -> ET.Element:
    """Devuelve la primera <section> cuyo texto no esté vacío."""
    for section in root.iter("section"):
        if safe_text(section.text):
            return section
    return None


# ─────────────────────────────────────────
# Función principal
# ─────────────────────────────────────────
def xml_to_markdown(xml_path: str, output_dir: str):
    xml_path = Path(xml_path)
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    tree = ET.parse(xml_path)
    root = tree.getroot()

    section = find_first_nonempty_section(root)
    if section is None:
        raise SystemExit("⚠️ No se encontró ninguna <section> con texto en el XML.")

    title_raw = safe_text(section.text) or "Documento"
    title_norm = normalize_title(title_raw)
    slug = slugify(title_norm)

    filename = f"{slug}.md"
    output_path = output_dir / filename

    front_matter = textwrap.dedent(f"""\
    ---
    title: "{title_norm}"
    ---
    """)

    body = render_section_content(section, level=1)

    output_path.write_text(front_matter + "\n" + body, encoding="utf-8")

    print(f"✅ Archivo generado: {output_path.name}")
    print(f"📘 Título: {title_norm}")
    print(f"📂 Guardado en: {output_dir.resolve()}")


# ─────────────────────────────────────────
# CLI
# ─────────────────────────────────────────
if __name__ == "__main__":
    import sys

    if len(sys.argv) != 3:
        print("Uso: xml_to_md.py <input.xml> <output_dir>")
        sys.exit(1)

    xml_to_markdown(sys.argv[1], sys.argv[2])

