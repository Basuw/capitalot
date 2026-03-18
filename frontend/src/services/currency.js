const CURRENCY_SYMBOLS = {
  USD: '$',
  EUR: '€',
  GBP: '£',
  JPY: '¥',
  CHF: 'CHF',
  CAD: 'C$',
  AUD: 'A$'
}

// Default rates (fallbacks)
let EXCHANGE_RATES = {
  USD: 1.15,
  EUR: 1.00,
  GBP: 0.86,
  JPY: 150.0,
  CHF: 0.91,
  CAD: 1.36,
  AUD: 1.53
}

/**
 * Fetches latest exchange rates from Frankfurter API
 * Base is EUR as requested
 */
export async function fetchExchangeRates() {
  try {
    const response = await fetch('https://api.frankfurter.dev/v1/latest?from=EUR&to=USD,CHF,GBP,JPY,CAD,AUD');
    if (!response.ok) throw new Error('Failed to fetch exchange rates');
    
    const data = await response.json();
    if (data && data.rates) {
      EXCHANGE_RATES = {
        ...EXCHANGE_RATES,
        ...data.rates,
        EUR: 1.00 // Base is EUR
      };
      console.log('Exchange rates updated successfully:', EXCHANGE_RATES);
    }
  } catch (error) {
    console.error('Error fetching exchange rates, using fallbacks:', error);
  }
}

/**
 * Formats a price in the target currency
 * @param {number} priceInUSD - The price as returned by API (usually in USD or base stock currency)
 * @param {string} targetCurrency - User preferred currency (EUR, USD, etc.)
 */
export function formatPrice(price, targetCurrency = 'USD') {
  if (price === null || price === undefined) return '-'
  
  const symbol = CURRENCY_SYMBOLS[targetCurrency] || '$'
  
  // Convert from USD (API base) to EUR, then to target currency
  // Since our rates are based on 1 EUR = X target
  const priceInEUR = price / EXCHANGE_RATES['USD'];
  const convertedPrice = priceInEUR * (EXCHANGE_RATES[targetCurrency] || 1);
  
  if (targetCurrency === 'JPY') {
    return `${symbol} ${Math.round(convertedPrice).toLocaleString()}`
  }
  
  return `${symbol} ${convertedPrice.toFixed(2)}`
}

export function formatPriceShort(price, targetCurrency = 'USD') {
  if (price === null || price === undefined) return '-'
  
  const priceInEUR = price / EXCHANGE_RATES['USD'];
  const convertedPrice = priceInEUR * (EXCHANGE_RATES[targetCurrency] || 1);
  
  if (Math.abs(convertedPrice) >= 1000000) {
    return (convertedPrice / 1000000).toFixed(1) + 'M'
  }
  if (Math.abs(convertedPrice) >= 1000) {
    return (convertedPrice / 1000).toFixed(1) + 'K'
  }
  
  if (targetCurrency === 'JPY') {
    return Math.round(convertedPrice).toString()
  }
  
  return convertedPrice.toFixed(2)
}

export function getCurrencySymbol(currency = 'USD') {
  return CURRENCY_SYMBOLS[currency] || '$'
}

export function convertToBaseCurrency(price, currency = 'USD') {
  if (price === null || price === undefined) return 0
  // Convert from some currency back to USD (internal base)
  const priceInEUR = price / (EXCHANGE_RATES[currency] || 1);
  return priceInEUR * EXCHANGE_RATES['USD'];
}
