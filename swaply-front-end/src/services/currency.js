export function fmtFt(value) {
  if (value == null) return '— Ft'
  const num = Number(value)
  if (Number.isNaN(num)) return '— Ft'
  // Forints are typically shown without decimals
  return num.toLocaleString(undefined, { maximumFractionDigits: 0 }) + ' Ft'
}

