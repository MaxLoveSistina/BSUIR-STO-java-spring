const moneyFormatter = new Intl.NumberFormat('ru-RU', {
  style: 'currency',
  currency: 'RUB',
  maximumFractionDigits: 2,
})

export const money = (value) => moneyFormatter.format(Number(value ?? 0))

export function days(n) {
  const mod10 = n % 10
  const mod100 = n % 100
  if (mod10 === 1 && mod100 !== 11) return `${n} день`
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return `${n} дня`
  return `${n} дней`
}

export const dateTime = (iso) =>
  iso
    ? new Date(iso).toLocaleString('ru-RU', { dateStyle: 'medium', timeStyle: 'short' })
    : ''
