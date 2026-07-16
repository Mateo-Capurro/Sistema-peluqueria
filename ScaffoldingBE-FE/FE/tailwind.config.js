/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}"
  ],
  theme: {
    extend: {
      // Paleta "Salón Alma" — editorial cálido: oro sobre crema (del template de diseño Figma).
      colors: {
        alma: {
          bg:      '#F5F1E8',   // fondo de página (crema)
          card:    '#FDFAF4',   // fondo de card
          cream:   '#F5F1E8',   // crema claro (texto sobre oscuro / botón claro)
          ink:     '#1C1A17',   // tinta casi negra (header, footer, texto fuerte)
          heading: '#1C1A17',   // títulos
          body:    '#4A4534',   // texto de cuerpo
          muted:   '#6B6556',   // texto atenuado
          line:    '#DAD3C4',   // bordes / separadores (~12% tinta)
          accent:  '#C9A96E',   // oro (acento principal)
          'accent-light': '#D4B888',
          'accent-soft':  '#EFE6D3',
          footer:  '#A79E88',   // texto de footer (crema atenuado)
          soft:    '#EDE8DC',   // panel suave (secondary)
          ok:      'oklch(0.52 0.05 140)',
          danger:  '#B4433B',
        },
      },
      fontFamily: {
        serif: ['"Playfair Display"', 'Georgia', 'serif'],
        sans:  ['"DM Sans"', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        pill: '100px',
      },
      letterSpacing: {
        widest: '0.15em',
        editorial: '0.3em',
      },
      boxShadow: {
        card: '0 1px 3px oklch(0.05 0 0 / 0.08)',
      },
    },
  },
  plugins: [],
}
