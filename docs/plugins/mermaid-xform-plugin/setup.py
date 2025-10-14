from setuptools import setup, find_packages

setup(
    name="mkdocs-mermaid-xform-plugin",
    version="1.0.0",
    author="Juan Manuel Piñero Sánchez",
    description="Plugin para MkDocs que transforma bloques Mermaid personalizados en diagramas con estilos y envoltorios HTML automáticos.",
    packages=find_packages(),
    include_package_data=True,
    python_requires=">=3.8",
    install_requires=["mkdocs>=1.5", "mkdocs-material>=9.0"],
    entry_points={
        "mkdocs.plugins": [
            # Este es el nombre que se usa en mkdocs.yml → plugins: [ mermaid-xform ]
            "mermaid-xform = mermaid_xform_plugin.plugin:MermaidXFormPlugin",
        ]
    },
)
