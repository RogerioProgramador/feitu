export function formatarDataBR(isoDate: string): string {
  const [y, m, d] = isoDate.split('-')
  return `${d}/${m}/${y}`
}

export function hojeISO(): string {
  const fuso = localStorage.getItem('feitu_fuso') ?? 'BR'
  const tz = fuso === 'BR' ? 'America/Sao_Paulo' : 'UTC'
  return new Date().toLocaleDateString('sv-SE', { timeZone: tz })
}
