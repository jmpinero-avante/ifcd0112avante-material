function renderAllMermaid() {
  if (typeof mermaid === "undefined") {
    console.warn("⚠️ Mermaid aún no está disponible.");
    return;
  }

  const elements = document.querySelectorAll(".mermaid:not(.rendered)");
  if (elements.length === 0) return;

  console.log(`🎨 Renderizando ${elements.length} diagramas Mermaid nuevos...`);
  mermaid.run({ nodes: elements });
}

document.addEventListener("DOMContentLoaded", () => {
	mermaid.initialize({
	  startOnLoad: false,
	  theme: "base",
	  themeVariables: {
	    fontFamily: "DejaVu Sans, Arial, Helvetica, sans-serif",
	    fontSize: "14px",
	    primaryTextColor: "#000000",
	  },
	  themeCSS: `
	    text, tspan {
	      font-family: "DejaVu Sans", Arial, Helvetica, sans-serif !important;
	      fill: #000000 !important;
	    }
	  `,
	  securityLevel: "loose",
	});

  renderAllMermaid();
});

// Re-renderizar tras navegación SPA en MkDocs Material
document.addEventListener("pjax:complete", renderAllMermaid);
document.addEventListener("navigation:end", renderAllMermaid);
