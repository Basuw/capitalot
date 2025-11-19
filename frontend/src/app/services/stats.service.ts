import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PerformanceStats } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class StatsService {
  constructor(private http: HttpClient) {}

  getDailyStats(): Observable<PerformanceStats> {
    return this.http.get<PerformanceStats>(`${environment.apiUrl}/stats/daily`);
  }

  getMonthlyStats(): Observable<PerformanceStats> {
    return this.http.get<PerformanceStats>(`${environment.apiUrl}/stats/monthly`);
  }

  getYearlyStats(): Observable<PerformanceStats> {
    return this.http.get<PerformanceStats>(`${environment.apiUrl}/stats/yearly`);
  }

  getAllTimeStats(): Observable<PerformanceStats> {
    return this.http.get<PerformanceStats>(`${environment.apiUrl}/stats/all-time`);
  }
}
