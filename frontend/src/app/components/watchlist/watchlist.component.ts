import { Component, OnInit } from '@angular/core';
import { WatchlistService } from '../../services/watchlist.service';
import { StockService } from '../../services/stock.service';
import { Watchlist, Stock } from '../../models/models';

@Component({
  selector: 'app-watchlist',
  template: `
    <div class="watchlist-container">
      <h1>Mes Listes d'Intérêt</h1>
      <button (click)="createWatchlist()" class="btn-create">+ Créer une liste</button>
      
      <div class="watchlists-list">
        <div class="watchlist-item" *ngFor="let watchlist of watchlists">
          <div class="watchlist-header">
            <h2>{{ watchlist.name }}</h2>
            <button (click)="deleteWatchlist(watchlist.id)" class="btn-delete">Supprimer</button>
          </div>
          <p>{{ watchlist.description }}</p>
          
          <div class="stocks-section">
            <h3>Actions</h3>
            <div class="add-stock">
              <input type="text" [(ngModel)]="searchQuery" 
                     (input)="searchStocks()" placeholder="Rechercher une action...">
              <div class="search-results" *ngIf="searchResults.length > 0">
                <div class="stock-result" *ngFor="let stock of searchResults"
                     (click)="addStockToWatchlist(watchlist.id, stock.id)">
                  {{ stock.symbol }} - {{ stock.name }}
                </div>
              </div>
            </div>
            
            <div class="stocks-list">
              <div class="stock-item" *ngFor="let stock of watchlist.stocks">
                <span>{{ stock.symbol }} - {{ stock.name }}</span>
                <button (click)="removeStockFromWatchlist(watchlist.id, stock.id)" 
                        class="btn-remove">×</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .watchlist-container {
      max-width: 1200px;
      margin: 0 auto;
    }
    
    h1 {
      color: #2c3e50;
      margin-bottom: 1.5rem;
    }
    
    .btn-create {
      background-color: #2ecc71;
      color: white;
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      margin-bottom: 2rem;
    }
    
    .watchlists-list {
      display: grid;
      gap: 1rem;
    }
    
    .watchlist-item {
      background: white;
      padding: 1.5rem;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .watchlist-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }
    
    .watchlist-header h2 {
      margin: 0;
      color: #2c3e50;
    }
    
    .btn-delete {
      background-color: #e74c3c;
      color: white;
      padding: 0.5rem 1rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    
    .stocks-section {
      margin-top: 1.5rem;
    }
    
    .stocks-section h3 {
      margin-bottom: 1rem;
      color: #555;
    }
    
    .add-stock {
      position: relative;
      margin-bottom: 1rem;
    }
    
    .add-stock input {
      width: 100%;
      padding: 0.75rem;
      border: 1px solid #ddd;
      border-radius: 4px;
    }
    
    .search-results {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      background: white;
      border: 1px solid #ddd;
      border-radius: 4px;
      max-height: 200px;
      overflow-y: auto;
      z-index: 10;
    }
    
    .stock-result {
      padding: 0.75rem;
      cursor: pointer;
    }
    
    .stock-result:hover {
      background-color: #f5f5f5;
    }
    
    .stocks-list {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    
    .stock-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem;
      background-color: #f8f9fa;
      border-radius: 4px;
    }
    
    .btn-remove {
      background-color: #e74c3c;
      color: white;
      border: none;
      width: 30px;
      height: 30px;
      border-radius: 50%;
      cursor: pointer;
      font-size: 1.2rem;
    }
  `]
})
export class WatchlistComponent implements OnInit {
  watchlists: Watchlist[] = [];
  searchQuery = '';
  searchResults: Stock[] = [];

  constructor(
    private watchlistService: WatchlistService,
    private stockService: StockService
  ) {}

  ngOnInit(): void {
    this.loadWatchlists();
  }

  loadWatchlists(): void {
    this.watchlistService.getWatchlists().subscribe(
      watchlists => this.watchlists = watchlists
    );
  }

  createWatchlist(): void {
    const name = prompt('Nom de la liste:');
    const description = prompt('Description:');
    if (name) {
      this.watchlistService.createWatchlist(name, description || '').subscribe(() => {
        this.loadWatchlists();
      });
    }
  }

  deleteWatchlist(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette liste ?')) {
      this.watchlistService.deleteWatchlist(id).subscribe(() => {
        this.loadWatchlists();
      });
    }
  }

  searchStocks(): void {
    if (this.searchQuery.length > 1) {
      this.stockService.searchStocks(this.searchQuery).subscribe(
        results => this.searchResults = results
      );
    } else {
      this.searchResults = [];
    }
  }

  addStockToWatchlist(watchlistId: number, stockId: number): void {
    this.watchlistService.addStock(watchlistId, stockId).subscribe(() => {
      this.loadWatchlists();
      this.searchQuery = '';
      this.searchResults = [];
    });
  }

  removeStockFromWatchlist(watchlistId: number, stockId: number): void {
    this.watchlistService.removeStock(watchlistId, stockId).subscribe(() => {
      this.loadWatchlists();
    });
  }
}
