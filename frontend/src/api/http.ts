import axios from 'axios'

const http = axios.create({ baseURL: '/api' })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('feitu_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (res) => res,
  async (error) => {
    const isAuthRoute = error.config?.url?.startsWith('/auth/')
    const isRetry = error.config?._retry

    if (error.response?.status === 401 && !isAuthRoute && !isRetry) {
      error.config._retry = true
      const rt = localStorage.getItem('feitu_refresh_token')

      if (rt) {
        try {
          // Usar axios direto para não passar pelo interceptor de Authorization
          // e evitar loop infinito caso o refresh também retorne 401
          const res = await axios.post<{ token: string; refreshToken: string }>('/api/auth/refresh', { refreshToken: rt })
          const { token: newToken, refreshToken: newRefresh } = res.data
          localStorage.setItem('feitu_token', newToken)
          localStorage.setItem('feitu_refresh_token', newRefresh)
          error.config.headers.Authorization = `Bearer ${newToken}`
          return http(error.config)
        } catch {
          // refresh falhou — prossegue com logout
        }
      }

      localStorage.removeItem('feitu_token')
      localStorage.removeItem('feitu_refresh_token')
      localStorage.removeItem('feitu_email')
      window.location.href = '/login'
    }

    return Promise.reject(error)
  },
)

export default http
