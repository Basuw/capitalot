const CURRENCY_SYMBOLS = {
  USD: '$',
  EUR: '€',
  GBP: '£',
  JPY: '¥',
  CHF: 'CHF',
  CAD: 'C$',
  AUD: 'A$'
}

const EXCHANGE_RATES = {
  USD: 1,
  EUR: 0.92,
  GBP: 0.79,
  JPY: 149.50,
  CHF: 0.88,
  CAD: 1.36,
  AUD: 1.53
}

export function formatPrice(price, currency = 'USD') {
  if (price === null || price === undefined) return '-'
  
  const symbol = CURRENCY_SYMBOLS[currency] || '$'
  const rate = EXCHANGE_RATES[currency] || 1
  const convertedPrice = price * rate
  
  if (currency === 'JPY') {
    return `${symbol} ${Math.round(convertedPrice).toLocaleString()}`
  }
  
  return `${symbol} ${convertedPrice.toFixed(2)}`
}

export function formatPriceShort(price, currency = 'USD') {
  if (price === null || price === undefined) return '-'
  
  const rate = EXCHANGE_RATES[currency] || 1
  const convertedPrice = price * rate
  
  if (Math.abs(convertedPrice) >= 1000000) {
    return (convertedPrice / 1000000).toFixed(1) + 'M'
  }
  if (Math.abs(convertedPrice) >= 1000) {
    return (convertedPrice / 1000).toFixed(1) + 'K'
  }
  
  if (currency === 'JPY') {
    return Math.round(convertedPrice).toString()
  }
  
  return convertedPrice.toFixed(2)
}

export function getCurrencySymbol(currency = 'USD') {
  return CURRENCY_SYMBOLS[currency] || '$'
}

export function convertToBaseCurrency(price, currency = 'USD') {
  if (price === null || price === undefined) return 0
  const rate = EXCHANGE_RATES[currency] || 1
  return price / rate
}
