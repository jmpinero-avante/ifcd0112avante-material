// Inicializa Rellax con una configuración mínima.
// Comentarios en español para alumnos:
//
//  - La clase .rellax se usa en el HTML para marcar capas con parallax.
//  - 'speed' controla la velocidad relativa del scroll.
//  - Este script es intencionalmente simple para que se entienda.
//
// Nota: Asegúrate de tener /js/rellax.min.js en static.
document.addEventListener('DOMContentLoaded', function () {
	/* eslint-disable no-undef */
	// Si Rellax no está disponible, no hacemos nada (evita errores en desarrollo).
	if (typeof Rellax === 'function') {
		new Rellax('.rellax');
	}
});