/* vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 : */

/*
============================================================
PARALLAX-INIT.JS
============================================================
Script de inicialización del efecto Parallax en HtmlApp.

Este archivo activa la librería externa Rellax.js, que se carga desde
un CDN en la página `main.html`. El objetivo es aplicar el efecto
"parallax" a los elementos con clase `.parallax`.

------------------------------------------------------------
UBICACIÓN Y ACCESO
------------------------------------------------------------
Este archivo se encuentra en:
  src/main/resources/static/js/parallax-init.js

Spring Boot sirve automáticamente los archivos ubicados en `/static`
como recursos públicos. Por tanto, estará disponible en:
  http://localhost:8080/js/parallax-init.js

En el HTML se referencia usando:
  <script th:src="@{/js/parallax-init.js}"></script>
*/

document.addEventListener("DOMContentLoaded", () => {
	console.log("[HtmlApp] Iniciando efecto Parallax...");

	// ------------------------------------------------------------
	// 1. Comprobación de que la librería Rellax está disponible
	// ------------------------------------------------------------
	if (typeof Rellax === "undefined") {
		console.error("Rellax.js no está cargado. Verifica el CDN o la conexión.");
		return;
	}

	/*
	------------------------------------------------------------
	2. Inicialización del efecto
	------------------------------------------------------------

	La librería Rellax busca automáticamente en el DOM todos los
	elementos que coincidan con el selector proporcionado (en este
	caso, '.parallax') y les aplica el desplazamiento según los
	atributos `data-rellax-*` definidos en el HTML.

	Por ejemplo:
	  <div class="parallax" data-rellax-speed="-3"></div>

	En este caso, no es necesario pasar ninguna configuración extra
	en el constructor (como { speed: -3 }), ya que el atributo
	`data-rellax-speed` tiene prioridad sobre las opciones globales.
	*/
	new Rellax(".parallax");

	console.log("[HtmlApp] Parallax inicializado correctamente.");
});