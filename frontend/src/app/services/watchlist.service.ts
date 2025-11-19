import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Watchlist } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class WatchlistService {
  constructor(private http: HttpClient) {}

  getWatchlists(): Observable<Watchlist[]> {
    return this.http.get<Watchlist[]>(`${environment.apiUrl}/watchlists`);
  }

  createWatchlist(name: string, description: string): Observable<Watchlist> {
    return this.http.post<Watchlist>(`${environment.apiUrl}/watchlists`, { name, description });
  }

  addStock(watchlistId: number, stockId: number): Observable<Watchlist> {
    return this.http.post<Watchlist>(`${environment.apiUrl}/watchlists/${watchlistId}/stocks/${stockId}`, {});
  }

  removeStock(watchlistId: number, stockId: number): Observable<Watchlist> {
    return this.http.delete<Watchlist>(`${environment.apiUrl}/watchlists/${watchlistId}/stocks/${stockId}`);
  }

  deleteWatchlist(watchlistId: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/watchlists/${watchlistId}`);
  }
}
