/* vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 : */

/*
============================================================
PARALLAX-INIT.JS
============================================================
Script de inicialización del efecto Parallax en HtmlApp.

Utiliza la librería externa Rellax.js, cargada desde CDN en la
página principal (main.html). Este archivo activa el efecto una
vez que el DOM está listo.

Ubicación:
  src/main/resources/static/js/parallax-init.js

Se referencia en main.html mediante:
  <script th:src="@{/js/parallax-init.js}"></script>
*/

document.addEventListener("DOMContentLoaded", () => {
	console.log("[HtmlApp] Iniciando efecto Parallax...");

	// Verifica que Rellax esté disponible
	if (typeof Rellax === "undefined") {
		console.error("Rellax.js no está cargado. Verifica el CDN.");
		return;
	}

	/*
	Inicializa el efecto Parallax sobre los elementos con clase .parallax.

	IMPORTANTE:
	-----------
	Si los elementos definen el atributo `data-rellax-speed` en su HTML,
	Rellax usará esos valores individuales de velocidad y no es necesario
	establecer la opción `speed` aquí.
	*/
	new Rellax(".parallax");

	console.log("[HtmlApp] Parallax inicializado correctamente.");
});