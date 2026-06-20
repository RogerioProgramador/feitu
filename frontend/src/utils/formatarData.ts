export function formatarDataBR(isoDate: string): string {
  const [y, m, d] = isoDate.split('-')
  return `${d}/${m}/${y}`
}

export function hojeISO(): string {
  return new Date().toISOString().slice(0, 10)
}
