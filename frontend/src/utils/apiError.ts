export function getApiErrorMessage(e: unknown, fallback: string): string {
  const err = e as { response?: { data?: { detail?: string } } }
  return err?.response?.data?.detail ?? fallback
}
