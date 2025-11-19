import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Stock, StockPrice } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class StockService {
  constructor(private http: HttpClient) {}

  searchStocks(query?: string): Observable<Stock[]> {
    const url = query 
      ? `${environment.apiUrl}/stocks/search?query=${query}`
      : `${environment.apiUrl}/stocks/search`;
    return this.http.get<Stock[]>(url);
  }

  getPopularStocks(): Observable<Stock[]> {
    return this.http.get<Stock[]>(`${environment.apiUrl}/stocks/popular`);
  }

  getStockPrice(symbol: string): Observable<StockPrice> {
    return this.http.get<StockPrice>(`${environment.apiUrl}/stocks/${symbol}/price`);
  }
}
