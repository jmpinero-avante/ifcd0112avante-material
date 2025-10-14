import os
import re
import json
import logging
import subprocess
import tempfile
import importlib.resources
import xml.etree.ElementTree as ET
from pathlib import Path

from mkdocs.plugins import BasePlugin
from mkdocs.config import config_options

# Logger integrado con MkDocs: mostrará DEBUG cuando ejecutes `mkdocs -v`
log = logging.getLogger("mkdocs.plugins.mermaid_xform")

# ──────────────────────────────────────────────────────────────────────────────
# Estilos UML (solo visibilidad)
# ──────────────────────────────────────────────────────────────────────────────
UML_STYLES = """
classDef publicClass fill:#bbdefb,stroke:#0d47a1,color:#000,stroke-width:1.5px;     %% +
classDef protectedClass fill:#fff9c4,stroke:#fbc02d,color:#000,stroke-width:1.2px;  %% #
classDef privateClass fill:#ffcdd2,stroke:#c62828,color:#000,stroke-width:1.2px;    %% -
classDef packageClass fill:#c8e6c9,stroke:#2e7d32,color:#000,stroke-width:1.2px;    %% ~ o sin prefijo
""".strip()

# ──────────────────────────────────────────────────────────────────────────────
# Tipos Mermaid y alias
# ──────────────────────────────────────────────────────────────────────────────
MERMAID_TYPES = {
    "class": "classDiagram",
    "sequence": "sequenceDiagram",
    "seq": "sequenceDiagram",
    "state": "stateDiagram-v2",
    "flow": "flowchart",
    "flowchart": "flowchart",
    "er": "erDiagram",
    "gantt": "gantt",
    "journey": "journey",
    "pie": "pie",
    "timeline": "timeline",
    "quadrant": "quadrantChart",
    "git": "gitGraph",
}

# Bloques tipo ```mermaid-xform[-tipo] ... ```
MERMAID_BLOCK_RE = re.compile(r"```mermaid-xform(?:-([\w]+))?\n(.*?)```", re.DOTALL)

SVG_NS = "http://www.w3.org/2000/svg"


class MermaidXFormPlugin(BasePlugin):
    """Plugin MkDocs: transforma bloques mermaid-xform en HTML/SVG según contexto."""

    config_scheme = (("enable", config_options.Type(bool, default=True)),)

    # ──────────────────────────────────────────────────────────────────────
    # Hook de configuración
    # ──────────────────────────────────────────────────────────────────────
    def on_config(self, config):
        if self.config.get("enable", True):
            log.debug("🧩 Mermaid-XForm: plugin activo")
        else:
            log.warning("⚠️ Mermaid-XForm está deshabilitado en la configuración")
        return config

    # ──────────────────────────────────────────────────────────────────────
    # Utilidades de render (PDF/WeasyPrint): mmdc → SVG → limpiar foreignObject
    # ──────────────────────────────────────────────────────────────────────
    def _mmdc_to_svg_clean(self, mmd_text: str) -> str:
        """
        Llama a mermaid-cli (mmdc) para generar un SVG y elimina <foreignObject>
        convirtiéndolos en <text> con coordenadas x/y conservadas.
        Devuelve el SVG final como cadena (inline).
        """
        with tempfile.TemporaryDirectory(prefix="mermaid-xform-") as tmpdir:
            tmp = Path(tmpdir)
            mmd_path = tmp / "diagram.mmd"
            svg_path = tmp / "diagram.svg"

            # Guardar .mmd
            mmd_path.write_text(mmd_text, encoding="utf-8")

            # Localizar config-mermaid.json dentro del paquete
            try:
                with importlib.resources.path(
                    "mermaid_xform_plugin", "config-mermaid.json"
                ) as cfg_path:
                    config_path = str(cfg_path)
            except Exception as e:
                log.warning(
                    f"⚠️ No se encontró config-mermaid.json en el paquete: {e}. "
                    "Se usará mmdc sin config."
                )
                config_path = None

            # Ejecutar mmdc
            cmd = ["mmdc", "-i", str(mmd_path), "-o", str(svg_path)]
            if config_path:
                cmd += ["--configFile", config_path]

            log.debug("🎨 Ejecutando Mermaid CLI: %s", " ".join(cmd))
            try:
                # Usamos text=True para capturar errores legibles si falla
                result = subprocess.run(cmd, check=False, text=True, capture_output=True)
                if result.returncode != 0:
                    log.warning(
                        "⚠️ mmdc devolvió código %s\nSTDERR:\n%s",
                        result.returncode,
                        result.stderr,
                    )
                    # Devolver el contenido sin transformar, encerrado en <pre>
                    return f"<pre class='mermaid-error'>{mmd_text}</pre>"
            except FileNotFoundError:
                log.error("❌ No se encontró 'mmdc' en PATH. Instala @mermaid-js/mermaid-cli.")
                return f"<pre class='mermaid-error'>{mmd_text}</pre>"
            except Exception as e:
                log.error("❌ Error ejecutando mmdc: %s", e)
                return f"<pre class='mermaid-error'>{mmd_text}</pre>"

            # Leer SVG
            try:
                svg_raw = svg_path.read_text(encoding="utf-8")
            except Exception as e:
                log.error("❌ No se pudo leer el SVG generado por mmdc: %s", e)
                return f"<pre class='mermaid-error'>{mmd_text}</pre>"

            # Parsear y limpiar foreignObject → text
            try:
                # Registrar namespace para búsquedas
                ET.register_namespace("", SVG_NS)
                tree = ET.ElementTree(ET.fromstring(svg_raw))
                root = tree.getroot()

                # Mapa de padres (ElementTree no expone getparent)
                parent_map = {c: p for p in tree.iter() for c in p}

                foreign_elems = root.findall(f".//{{{SVG_NS}}}foreignObject")
                log.debug("🔎 foreignObject encontrados: %d", len(foreign_elems))

                for fo in foreign_elems:
                    parent = parent_map.get(fo)
                    if parent is None:
                        # No debería pasar, pero por si acaso
                        continue

                    x = fo.attrib.get("x", "0")
                    y = fo.attrib.get("y", "0")

                    # Extraer texto "visible": combinamos todo el texto interno
                    inner_text = "".join(fo.itertext()).strip()
                    if not inner_text:
                        parent.remove(fo)
                        continue

                    # Crear <text> en el mismo lugar que el foreignObject
                    text_elem = ET.Element(
                        "text",
                        {
                            "x": x,
                            "y": y,
                            # Fuente segura para WeasyPrint (Fontconfig)
                            "font-family": "DejaVu Sans, Noto Sans, Arial, sans-serif",
                            "font-size": "14px",
                            "fill": "#000000",
                        },
                    )
                    text_elem.text = inner_text

                    # Insertar <text> justo después del foreignObject y eliminarlo
                    idx = list(parent).index(fo)
                    parent.insert(idx + 1, text_elem)
                    parent.remove(fo)

                # SVG final limpio (cadena)
                svg_clean = ET.tostring(root, encoding="unicode")
                svg_clean = re.sub(
                    r"<svg([^>]+)>",
                    r"<svg\1 style='display:block;margin:auto;'>",
                    svg_clean,
                )
                return svg_clean

            except Exception as e:
                log.error("❌ Error limpiando SVG (foreignObject → text): %s", e)
                # Devuelve el SVG original si falló la limpieza
                return svg_raw

    # ──────────────────────────────────────────────────────────────────────
    # Normalización de tipo y contenido
    # ──────────────────────────────────────────────────────────────────────
    def _normalize_type_and_content(self, subtype: str, content: str) -> tuple[str, str]:
        """
        - Normaliza 'subtype' con alias (MERMAID_TYPES)
        - Asegura que el contenido empiece por el tipo
        - Añade estilos UML si es classDiagram
        """
        subtype_norm = MERMAID_TYPES.get((subtype or "").lower(), subtype or "")
        text = content.strip()

        # Detectar tipo si no se proporcionó
        if not subtype_norm:
            # Intentar detectar por palabras clave al inicio
            for full in MERMAID_TYPES.values():
                if re.match(rf"^\s*{re.escape(full)}\b", text):
                    subtype_norm = full
                    break
            # Fallback muy conservador
            if not subtype_norm:
                subtype_norm = "graph TD"

        # Prepend si el contenido no empieza por el tipo
        if not re.match(rf"^\s*{re.escape(subtype_norm)}\b", text):
            text = f"{subtype_norm}\n{text}"

        if subtype_norm == "classDiagram":
            text = f"{text}\n\n{UML_STYLES}"

        return subtype_norm, text

    # ──────────────────────────────────────────────────────────────────────
    # Hook principal: transformar bloques en la página
    # ──────────────────────────────────────────────────────────────────────
    def on_page_markdown(self, markdown, page, config, files):
        if not self.config.get("enable", True):
            return markdown

        # Detectar modo PDF (mkdocs-with-pdf o bandera de entorno)
        pdf_mode = os.getenv("PDF_EXPORT") == "1" or "with-pdf" in {getattr(p, "config_key_name", "") for p in config.get("plugins", [])}
        # Fallback adicional: cadena 'with-pdf' en la lista de plugins
        if not pdf_mode and isinstance(config.get("plugins"), dict):
            pdf_mode = "with-pdf" in config["plugins"]

        log.debug("📄 Modo PDF: %s (página: %s)", pdf_mode, getattr(page, "file", None))

        def replace_block(m):
            raw_type = m.group(1) or ""
            body = m.group(2) or ""
            subtype, content = self._normalize_type_and_content(raw_type, body)

            log.debug("🔁 Bloque mermaid-xform detectado: subtype=%s", subtype)

            if pdf_mode:
                # Render → SVG limpio (sin foreignObject) → embebido inline
                svg_inline = self._mmdc_to_svg_clean(content)
                return f"\n<div class='diagram-block'>\n{svg_inline}\n</div>\n"
                #return (
                #    "\n<div class='diagram-block' "
                #    "style='display:flex; justify-content:center; align-items:center; text-align:center;'>\n"
                #    f"{svg_inline}\n"
                #    "</div>\n"
                #)

            # HTML normal: dejar bloque Mermaid para render JS en el navegador
            return (
                "<div class='diagram-block' align='center'>\n"
                f"<div class='mermaid'>\n{content}\n</div>\n"
                "</div>\n"
            )

        new_md = MERMAID_BLOCK_RE.sub(replace_block, markdown)
        return new_md

