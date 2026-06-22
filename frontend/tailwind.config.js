/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        feitu: {
          bg: '#E8E4DC',
          surface: '#F7F4EE',
          blue: '#A7C7E7',
          'blue-deep': '#5E8BB6',
          teal: '#B5EAD7',
          'teal-deep': '#2F7D63',
          peach: '#FFDAC1',
          'peach-deep': '#E07B4F',
          lavender: '#E2C6FF',
          'lavender-deep': '#8A5FC0',
          text: '#36332E',
        },
        night: {
          bg: '#16161F',
          surface: '#222230',
          card: '#262635',
          text: '#ECE6DC',
        },
      },
      fontFamily: {
        sans: ['"Space Grotesk"', 'system-ui', 'sans-serif'],
        display: ['"Instrument Serif"', 'Georgia', 'serif'],
      },
      keyframes: {
        'feitu-spin': { to: { transform: 'rotate(360deg)' } },
        'feitu-breathe': {
          '0%, 100%': { opacity: '.45', transform: 'scale(.7)' },
          '50%': { opacity: '1', transform: 'scale(1)' },
        },
      },
      animation: {
        'feitu-spin': 'feitu-spin 1.4s linear infinite',
        'feitu-breathe': 'feitu-breathe 1.6s ease-in-out infinite',
      },
    },
  },
  plugins: [],
}
