export default defineNuxtConfig({
  compatibilityDate: '2026-09-06',
  devtools: { enabled: true },
  modules: ['@nuxt/ui', '@pinia/nuxt', '@vite-pwa/nuxt'],
  ui: { fonts: false },
  css: ['~/assets/css/main.css'],
  typescript: { strict: true },
  colorMode: { preference: 'system', fallback: 'dark', classSuffix: '' },
  runtimeConfig: {
    databaseUrl: process.env.DATABASE_URL || '',
    sessionSecret: process.env.SESSION_SECRET || 'change-me-in-production',
    public: { appName: 'Nexa — Personal OS' }
  },
  app: {
    head: {
      title: 'Nexa — Personal Productivity OS',
      htmlAttrs: { dir: 'rtl', lang: 'fa' },
      meta: [
        { name: 'description', content: 'Personal productivity, learning, planning and progress analysis.' }
      ],
      link: [
        { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=Vazirmatn:wght@400;500;600;700;800&display=swap' }
      ]
    }
  },
  pwa: {
    registerType: 'autoUpdate',
    manifest: {
      name: 'Nexa Productivity OS',
      short_name: 'Nexa',
      description: 'Personal productivity and learning operating system',
      theme_color: '#0f172a',
      background_color: '#0b1120',
      display: 'standalone',
      start_url: '/',
      icons: []
    },
    workbox: { navigateFallback: '/', globPatterns: ['**/*.{js,css,html,png,svg,ico}'] }
  }
})
