export interface User {
  email: string;
  firstName: string;
  lastName: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  firstName: string;
  lastName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export enum StockType {
  STOCK = 'STOCK',
  ETF = 'ETF',
  CRYPTO = 'CRYPTO'
}

export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export interface Tag {
  id: number;
  name: string;
  color?: string;
}

export interface Stock {
  id: number;
  symbol: string;
  name: string;
  exchange: string;
  currency: string;
  sector: string;
  industry: string;
  isPopular: boolean;
  stockType: StockType;
  priority?: Priority;
  marketScore?: number;
  tags?: Tag[];
}

export interface Portfolio {
  id: number;
  name: string;
  description: string;
  link?: string;
  icon?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PortfolioStock {
  id: number;
  stock: Stock;
  quantity: number;
  purchasePrice: number;
  purchaseDate: string;
  notes: string;
}

export interface Watchlist {
  id: number;
  name: string;
  description: string;
  link?: string;
  icon?: string;
  stocks: Stock[];
  createdAt: string;
  updatedAt: string;
}

export interface StockPrice {
  symbol: string;
  currentPrice: number;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  previousClose: number;
  changePercent: number;
  volume: number;
  currency: string;
}

export interface PerformanceStats {
  totalInvested: number;
  currentValue: number;
  totalGainLoss: number;
  totalGainLossPercent: number;
  numberOfStocks: number;
}
