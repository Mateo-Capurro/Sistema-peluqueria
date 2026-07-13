/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}"
  ],
  theme: {
    extend: {
      // Paleta "Salón Alma" — terracota cálido sobre crema (oklch, del template de diseño).
      colors: {
        alma: {
          bg:      'oklch(0.97 0.01 75)',    // fondo de página
          card:    'oklch(0.99 0.006 75)',   // fondo de card
          cream:   'oklch(0.98 0.005 80)',   // crema claro (texto sobre oscuro / botón claro)
          ink:     'oklch(0.16 0.02 40)',    // marrón oscuro (footer, texto fuerte)
          heading: 'oklch(0.30 0.02 40)',    // títulos
          body:    'oklch(0.48 0.02 50)',    // texto de cuerpo
          muted:   'oklch(0.55 0.02 50)',    // texto atenuado
          line:    'oklch(0.88 0.015 60)',   // bordes / separadores
          accent:  'oklch(0.60 0.18 38)',    // terracota (acento principal)
          'accent-light': 'oklch(0.72 0.16 38)',
          'accent-soft':  'oklch(0.93 0.05 40)',
          footer:  'oklch(0.75 0.02 60)',    // texto de footer
          soft:    'oklch(0.93 0.02 60)',    // panel suave
          ok:      'oklch(0.52 0.05 140)',
          danger:  'oklch(0.55 0.14 30)',
        },
      },
      fontFamily: {
        serif: ['"Playfair Display"', 'Georgia', 'serif'],
        sans:  ['Manrope', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        pill: '100px',
      },
      boxShadow: {
        card: '0 1px 3px oklch(0.05 0 0 / 0.08)',
      },
    },
  },
  plugins: [],
}
