import { Component } from '@angular/core';
import { StockService } from '../../services/stock.service';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';

@Component({
  selector: 'app-stock-search',
  template: `
    <div class="stock-search-container">
      <div class="search-header">
        <h1>Rechercher des actions</h1>
        <p class="subtitle">Découvrez les meilleures opportunités d'investissement</p>
      </div>

      <div class="search-box">
        <div class="search-input-wrapper">
          <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          <input 
            type="text" 
            class="search-input"
            placeholder="Rechercher une action par symbole (ex: AAPL, GOOGL, MSFT)..."
            (input)="onSearch($event)"
            [value]="searchQuery"
          />
          <button *ngIf="searchQuery" class="clear-btn" (click)="clearSearch()">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
      </div>

      <div class="search-results" *ngIf="searchQuery">
        <div class="loading" *ngIf="loading">
          <div class="spinner"></div>
          <p>Recherche en cours...</p>
        </div>

        <div class="stock-grid" *ngIf="!loading && searchResults.length > 0">
          <div class="stock-card" *ngFor="let stock of searchResults" (click)="selectStock(stock)">
            <div class="stock-card-header">
              <div class="stock-symbol">{{ stock.symbol }}</div>
              <div class="stock-price" [class.positive]="stock.change >= 0" [class.negative]="stock.change < 0">
                {{ stock.price | number:'1.2-2' }} €
              </div>
            </div>
            <div class="stock-name">{{ stock.name }}</div>
            <div class="stock-change" [class.positive]="stock.change >= 0" [class.negative]="stock.change < 0">
              <span class="change-arrow">{{ stock.change >= 0 ? '↑' : '↓' }}</span>
              {{ stock.change | number:'1.2-2' }}%
            </div>
          </div>
        </div>

        <div class="no-results" *ngIf="!loading && searchResults.length === 0 && searchQuery">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          <p>Aucune action trouvée pour "{{ searchQuery }}"</p>
        </div>
      </div>

      <div class="selected-stock-detail" *ngIf="selectedStock">
        <div class="detail-header">
          <button class="back-btn" (click)="selectedStock = null">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="19" y1="12" x2="5" y2="12"></line>
              <polyline points="12 19 5 12 12 5"></polyline>
            </svg>
            Retour
          </button>
        </div>

        <div class="detail-content">
          <div class="stock-info-card">
            <div class="info-header">
              <div>
                <h2>{{ selectedStock.symbol }}</h2>
                <p class="company-name">{{ selectedStock.name }}</p>
              </div>
              <div class="price-info">
                <div class="current-price">{{ selectedStock.price | number:'1.2-2' }} €</div>
                <div class="price-change" [class.positive]="selectedStock.change >= 0" [class.negative]="selectedStock.change < 0">
                  <span class="change-arrow">{{ selectedStock.change >= 0 ? '↑' : '↓' }}</span>
                  {{ selectedStock.change | number:'1.2-2' }}%
                </div>
              </div>
            </div>

            <div class="chart-container">
              <div class="chart-placeholder">
                <svg viewBox="0 0 800 300" class="stock-chart">
                  <defs>
                    <linearGradient id="gradient" x1="0%" y1="0%" x2="0%" y2="100%">
                      <stop offset="0%" [attr.stop-color]="selectedStock.change >= 0 ? '#10b981' : '#ef4444'" stop-opacity="0.3"/>
                      <stop offset="100%" [attr.stop-color]="selectedStock.change >= 0 ? '#10b981' : '#ef4444'" stop-opacity="0"/>
                    </linearGradient>
                  </defs>
                  <path 
                    d="M 0 150 Q 100 120, 200 140 T 400 130 T 600 110 T 800 100" 
                    fill="url(#gradient)" 
                    stroke="none"
                  />
                  <path 
                    d="M 0 150 Q 100 120, 200 140 T 400 130 T 600 110 T 800 100" 
                    fill="none" 
                    [attr.stroke]="selectedStock.change >= 0 ? '#10b981' : '#ef4444'" 
                    stroke-width="3"
                  />
                </svg>
                <p class="chart-label">Graphique illustratif - 30 jours</p>
              </div>
            </div>

            <div class="stock-stats">
              <div class="stat-item">
                <span class="stat-label">Volume</span>
                <span class="stat-value">1.2M</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Cap. boursière</span>
                <span class="stat-value">2.8B €</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">P/E Ratio</span>
                <span class="stat-value">24.5</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">52W High</span>
                <span class="stat-value">{{ (selectedStock.price * 1.15) | number:'1.2-2' }} €</span>
              </div>
            </div>

            <div class="action-buttons">
              <button class="btn-primary" (click)="addToPortfolio()">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="12" y1="5" x2="12" y2="19"></line>
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
                Ajouter au portefeuille
              </button>
              <button class="btn-secondary" (click)="addToWatchlist()">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path>
                </svg>
                Ajouter à la watchlist
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="trending-section" *ngIf="!searchQuery && !selectedStock">
        <h2>Actions populaires</h2>
        <div class="trending-grid">
          <div class="trending-card" *ngFor="let stock of trendingStocks" (click)="selectStock(stock)">
            <div class="trending-header">
              <span class="trending-symbol">{{ stock.symbol }}</span>
              <span class="trending-badge">Tendance</span>
            </div>
            <div class="trending-price">{{ stock.price | number:'1.2-2' }} €</div>
            <div class="trending-change" [class.positive]="stock.change >= 0" [class.negative]="stock.change < 0">
              {{ stock.change >= 0 ? '+' : '' }}{{ stock.change | number:'1.2-2' }}%
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .stock-search-container {
      max-width: 1400px;
      margin: 0 auto;
      animation: fadeIn 0.5s ease-in;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(20px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .search-header {
      text-align: center;
      margin-bottom: 3rem;
    }

    .search-header h1 {
      font-size: 3rem;
      font-weight: 700;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin-bottom: 0.5rem;
    }

    .subtitle {
      color: #6b7280;
      font-size: 1.1rem;
    }

    .search-box {
      margin-bottom: 3rem;
    }

    .search-input-wrapper {
      position: relative;
      max-width: 800px;
      margin: 0 auto;
    }

    .search-icon {
      position: absolute;
      left: 1.5rem;
      top: 50%;
      transform: translateY(-50%);
      width: 24px;
      height: 24px;
      color: #9ca3af;
    }

    .search-input {
      width: 100%;
      padding: 1.5rem 4rem 1.5rem 4rem;
      font-size: 1.1rem;
      border: 2px solid #e5e7eb;
      border-radius: 50px;
      outline: none;
      transition: all 0.3s ease;
      background: white;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
    }

    .search-input:focus {
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1), 0 10px 15px -3px rgba(0, 0, 0, 0.1);
    }

    .clear-btn {
      position: absolute;
      right: 1.5rem;
      top: 50%;
      transform: translateY(-50%);
      background: none;
      border: none;
      cursor: pointer;
      padding: 0.5rem;
      color: #9ca3af;
      transition: color 0.2s;
    }

    .clear-btn:hover {
      color: #4b5563;
    }

    .clear-btn svg {
      width: 20px;
      height: 20px;
    }

    .loading {
      text-align: center;
      padding: 4rem 0;
    }

    .spinner {
      width: 50px;
      height: 50px;
      border: 4px solid #e5e7eb;
      border-top-color: #667eea;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto 1rem;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .stock-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 1.5rem;
      animation: slideUp 0.5s ease-out;
    }

    @keyframes slideUp {
      from { opacity: 0; transform: translateY(30px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .stock-card {
      background: white;
      border-radius: 16px;
      padding: 1.5rem;
      cursor: pointer;
      transition: all 0.3s ease;
      border: 2px solid transparent;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    }

    .stock-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
      border-color: #667eea;
    }

    .stock-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.5rem;
    }

    .stock-symbol {
      font-size: 1.5rem;
      font-weight: 700;
      color: #1f2937;
    }

    .stock-price {
      font-size: 1.3rem;
      font-weight: 600;
    }

    .stock-name {
      color: #6b7280;
      margin-bottom: 1rem;
      font-size: 0.95rem;
    }

    .stock-change {
      display: inline-flex;
      align-items: center;
      gap: 0.3rem;
      padding: 0.4rem 0.8rem;
      border-radius: 8px;
      font-weight: 600;
      font-size: 0.95rem;
    }

    .stock-change.positive {
      background: #d1fae5;
      color: #065f46;
    }

    .stock-change.negative {
      background: #fee2e2;
      color: #991b1b;
    }

    .stock-price.positive {
      color: #10b981;
    }

    .stock-price.negative {
      color: #ef4444;
    }

    .change-arrow {
      font-size: 1.2rem;
    }

    .no-results {
      text-align: center;
      padding: 4rem 0;
      color: #6b7280;
    }

    .no-results svg {
      width: 64px;
      height: 64px;
      margin-bottom: 1rem;
      color: #d1d5db;
    }

    .selected-stock-detail {
      animation: slideUp 0.5s ease-out;
    }

    .detail-header {
      margin-bottom: 2rem;
    }

    .back-btn {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1.5rem;
      background: white;
      border: 2px solid #e5e7eb;
      border-radius: 12px;
      cursor: pointer;
      font-size: 1rem;
      font-weight: 500;
      transition: all 0.3s;
      color: #4b5563;
    }

    .back-btn:hover {
      background: #f9fafb;
      border-color: #667eea;
      color: #667eea;
    }

    .back-btn svg {
      width: 20px;
      height: 20px;
    }

    .stock-info-card {
      background: white;
      border-radius: 24px;
      padding: 2.5rem;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
    }

    .info-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 2rem;
      padding-bottom: 2rem;
      border-bottom: 2px solid #f3f4f6;
    }

    .info-header h2 {
      font-size: 2.5rem;
      font-weight: 700;
      color: #1f2937;
      margin: 0 0 0.5rem 0;
    }

    .company-name {
      color: #6b7280;
      font-size: 1.1rem;
      margin: 0;
    }

    .price-info {
      text-align: right;
    }

    .current-price {
      font-size: 2.5rem;
      font-weight: 700;
      color: #1f2937;
      margin-bottom: 0.5rem;
    }

    .price-change {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.5rem 1rem;
      border-radius: 10px;
      font-weight: 600;
      font-size: 1.1rem;
    }

    .price-change.positive {
      background: #d1fae5;
      color: #065f46;
    }

    .price-change.negative {
      background: #fee2e2;
      color: #991b1b;
    }

    .chart-container {
      margin: 2rem 0;
      background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
      border-radius: 16px;
      padding: 2rem;
    }

    .chart-placeholder {
      position: relative;
    }

    .stock-chart {
      width: 100%;
      height: auto;
    }

    .chart-label {
      text-align: center;
      color: #6b7280;
      margin-top: 1rem;
      font-size: 0.9rem;
    }

    .stock-stats {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 1.5rem;
      margin: 2rem 0;
      padding: 2rem;
      background: #f9fafb;
      border-radius: 16px;
    }

    .stat-item {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .stat-label {
      color: #6b7280;
      font-size: 0.9rem;
      font-weight: 500;
    }

    .stat-value {
      color: #1f2937;
      font-size: 1.3rem;
      font-weight: 700;
    }

    .action-buttons {
      display: flex;
      gap: 1rem;
      margin-top: 2rem;
    }

    .btn-primary, .btn-secondary {
      flex: 1;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      padding: 1rem 2rem;
      border: none;
      border-radius: 12px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      box-shadow: 0 4px 6px -1px rgba(102, 126, 234, 0.5);
    }

    .btn-primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 10px 15px -3px rgba(102, 126, 234, 0.5);
    }

    .btn-secondary {
      background: white;
      color: #667eea;
      border: 2px solid #667eea;
    }

    .btn-secondary:hover {
      background: #f5f7ff;
      transform: translateY(-2px);
    }

    .btn-primary svg, .btn-secondary svg {
      width: 20px;
      height: 20px;
    }

    .trending-section {
      margin-top: 4rem;
    }

    .trending-section h2 {
      font-size: 2rem;
      font-weight: 700;
      color: #1f2937;
      margin-bottom: 1.5rem;
    }

    .trending-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 1.5rem;
    }

    .trending-card {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 1.5rem;
      border-radius: 16px;
      cursor: pointer;
      transition: all 0.3s;
      box-shadow: 0 4px 6px -1px rgba(102, 126, 234, 0.3);
    }

    .trending-card:hover {
      transform: translateY(-5px) scale(1.02);
      box-shadow: 0 20px 25px -5px rgba(102, 126, 234, 0.3);
    }

    .trending-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }

    .trending-symbol {
      font-size: 1.3rem;
      font-weight: 700;
    }

    .trending-badge {
      background: rgba(255, 255, 255, 0.2);
      padding: 0.3rem 0.6rem;
      border-radius: 6px;
      font-size: 0.75rem;
      font-weight: 600;
    }

    .trending-price {
      font-size: 1.5rem;
      font-weight: 700;
      margin-bottom: 0.5rem;
    }

    .trending-change {
      font-size: 1rem;
      font-weight: 600;
      opacity: 0.9;
    }

    .trending-change.positive::before {
      content: '↑ ';
    }

    .trending-change.negative::before {
      content: '↓ ';
    }

    @media (max-width: 768px) {
      .search-header h1 {
        font-size: 2rem;
      }

      .stock-grid, .trending-grid {
        grid-template-columns: 1fr;
      }

      .action-buttons {
        flex-direction: column;
      }

      .info-header {
        flex-direction: column;
        gap: 1.5rem;
      }

      .price-info {
        text-align: left;
      }
    }
  `]
})
export class StockSearchComponent {
  searchQuery = '';
  loading = false;
  searchResults: any[] = [];
  selectedStock: any = null;
  trendingStocks: any[] = [
    { symbol: 'AAPL', name: 'Apple Inc.', price: 178.50, change: 2.34 },
    { symbol: 'GOOGL', name: 'Alphabet Inc.', price: 142.80, change: -0.87 },
    { symbol: 'MSFT', name: 'Microsoft Corporation', price: 378.91, change: 1.56 },
    { symbol: 'TSLA', name: 'Tesla Inc.', price: 238.45, change: 4.21 },
    { symbol: 'AMZN', name: 'Amazon.com Inc.', price: 145.32, change: -1.23 },
    { symbol: 'NVDA', name: 'NVIDIA Corporation', price: 495.22, change: 3.78 }
  ];

  private searchSubject = new Subject<string>();

  constructor(private stockService: StockService) {
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(query => {
        this.loading = true;
        return this.stockService.searchStocks(query);
      })
    ).subscribe(results => {
      this.searchResults = results;
      this.loading = false;
    });
  }

  onSearch(event: any): void {
    this.searchQuery = event.target.value;
    if (this.searchQuery.trim()) {
      this.searchSubject.next(this.searchQuery);
    } else {
      this.searchResults = [];
    }
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchResults = [];
  }

  selectStock(stock: any): void {
    this.selectedStock = stock;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  addToPortfolio(): void {
    alert(`Ajouter ${this.selectedStock.symbol} au portefeuille`);
  }

  addToWatchlist(): void {
    alert(`Ajouter ${this.selectedStock.symbol} à la watchlist`);
  }
}
