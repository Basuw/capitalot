import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Portfolio, PortfolioStock } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class PortfolioService {
  constructor(private http: HttpClient) {}

  getPortfolios(): Observable<Portfolio[]> {
    return this.http.get<Portfolio[]>(`${environment.apiUrl}/portfolios`);
  }

  createPortfolio(name: string, description: string): Observable<Portfolio> {
    return this.http.post<Portfolio>(`${environment.apiUrl}/portfolios`, { name, description });
  }

  getPortfolioStocks(portfolioId: number): Observable<PortfolioStock[]> {
    return this.http.get<PortfolioStock[]>(`${environment.apiUrl}/portfolios/${portfolioId}/stocks`);
  }

  addStock(portfolioId: number, request: any): Observable<PortfolioStock> {
    return this.http.post<PortfolioStock>(`${environment.apiUrl}/portfolios/${portfolioId}/stocks`, request);
  }

  deletePortfolio(portfolioId: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/portfolios/${portfolioId}`);
  }

  removeStock(portfolioStockId: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/portfolios/stocks/${portfolioStockId}`);
  }
}
