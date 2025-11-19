import { Component, OnInit } from '@angular/core';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { WatchlistService } from '../../services/watchlist.service';
import { StockService } from '../../services/stock.service';
import { Watchlist, Stock, StockType, Priority } from '../../models/models';

@Component({
  selector: 'app-watchlist',
  template: `
    <div class="watchlist-container" [@fadeIn]>
      <div class="header">
        <h1 class="title">Mes Listes d'Intérêt</h1>
        <button (click)="openCreateModal()" class="btn-create">
          <span class="icon">+</span> Créer une liste
        </button>
      </div>
      
      <div class="watchlists-grid">
        <div class="watchlist-card" *ngFor="let watchlist of watchlists" [@slideUp]>
          <div class="card-header">
            <div class="watchlist-icon" *ngIf="watchlist.icon">{{ watchlist.icon }}</div>
            <div class="watchlist-info">
              <h2>{{ watchlist.name }}</h2>
              <p class="description">{{ watchlist.description }}</p>
              <a *ngIf="watchlist.link" [href]="watchlist.link" target="_blank" class="watchlist-link">
                🔗 Lien
              </a>
            </div>
          </div>

          <div class="watchlist-actions">
            <button (click)="toggleWatchlist(watchlist.id)" class="btn-toggle">
              {{ expandedWatchlistId === watchlist.id ? '▼' : '▶' }} 
              {{ watchlist.stocks?.length || 0 }} actions
            </button>
            <button (click)="deleteWatchlist(watchlist.id)" class="btn-delete-small">×</button>
          </div>

          <div class="watchlist-content" *ngIf="expandedWatchlistId === watchlist.id" [@slideDown]>
            <div class="add-stock-section">
              <div class="search-container">
                <input 
                  type="text" 
                  [(ngModel)]="searchQueries[watchlist.id]"
                  (input)="searchStocks(watchlist.id)"
                  placeholder="🔍 Ajouter une action..."
                  class="search-input">
              </div>
              
              <div class="search-results" *ngIf="searchResults[watchlist.id] && searchResults[watchlist.id].length > 0">
                <div class="stock-result" 
                     *ngFor="let stock of searchResults[watchlist.id]" 
                     (click)="showAddStockModal(watchlist.id, stock)">
                  <div class="stock-info">
                    <strong>{{ stock.symbol }}</strong>
                    <span class="stock-name">{{ stock.name }}</span>
                  </div>
                  <span class="stock-type">{{ stock.stockType }}</span>
                </div>
              </div>
            </div>

            <div class="stocks-table-container" *ngIf="watchlist.stocks && watchlist.stocks.length > 0">
              <div class="table-controls">
                <input type="text" 
                       [(ngModel)]="filterText[watchlist.id]" 
                       placeholder="Filtrer..." 
                       class="filter-input">
              </div>
              
              <table class="stocks-table">
                <thead>
                  <tr>
                    <th (click)="sortBy(watchlist.id, 'symbol')" class="sortable">
                      Symbole {{ getSortIcon(watchlist.id, 'symbol') }}
                    </th>
                    <th (click)="sortBy(watchlist.id, 'name')" class="sortable">
                      Nom {{ getSortIcon(watchlist.id, 'name') }}
                    </th>
                    <th (click)="sortBy(watchlist.id, 'stockType')" class="sortable">
                      Type {{ getSortIcon(watchlist.id, 'stockType') }}
                    </th>
                    <th (click)="sortBy(watchlist.id, 'priority')" class="sortable">
                      Priorité {{ getSortIcon(watchlist.id, 'priority') }}
                    </th>
                    <th (click)="sortBy(watchlist.id, 'marketScore')" class="sortable">
                      Score {{ getSortIcon(watchlist.id, 'marketScore') }}
                    </th>
                    <th>Tags</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let stock of getFilteredStocks(watchlist.id, watchlist.stocks)">
                    <td><strong>{{ stock.symbol }}</strong></td>
                    <td>{{ stock.name }}</td>
                    <td>
                      <span class="badge badge-type" [class]="'type-' + stock.stockType?.toLowerCase()">
                        {{ stock.stockType || 'STOCK' }}
                      </span>
                    </td>
                    <td>
                      <span class="badge badge-priority" 
                            [class]="'priority-' + stock.priority?.toLowerCase()"
                            *ngIf="stock.priority">
                        {{ stock.priority }}
                      </span>
                      <span class="badge badge-priority priority-none" *ngIf="!stock.priority">-</span>
                    </td>
                    <td>
                      <div class="score-container" *ngIf="stock.marketScore !== null && stock.marketScore !== undefined">
                        <div class="score-bar">
                          <div class="score-fill" [style.width.%]="stock.marketScore"></div>
                        </div>
                        <span class="score-text">{{ stock.marketScore?.toFixed(0) }}%</span>
                      </div>
                      <span *ngIf="stock.marketScore === null || stock.marketScore === undefined">-</span>
                    </td>
                    <td>
                      <div class="tags-container">
                        <span class="tag" *ngFor="let tag of stock.tags">{{ tag.name }}</span>
                        <span class="tag tag-add" (click)="openTagModal(stock)">+</span>
                      </div>
                    </td>
                    <td>
                      <button (click)="removeStockFromWatchlist(watchlist.id, stock.id)" 
                              class="btn-remove-small">×</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="empty-state" *ngIf="!watchlist.stocks || watchlist.stocks.length === 0">
              <div class="empty-icon">👁️</div>
              <p>Aucune action dans cette liste</p>
              <span>Ajoutez votre première action ci-dessus</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <app-modal 
      [isOpen]="isCreateModalOpen" 
      [title]="'Créer une liste d\\'intérêt'"
      (close)="closeCreateModal()">
      <div class="modal-form">
        <div class="form-group">
          <label>Nom de la liste *</label>
          <input type="text" [(ngModel)]="newWatchlist.name" placeholder="Ma liste">
        </div>
        <div class="form-group">
          <label>Description</label>
          <textarea [(ngModel)]="newWatchlist.description" rows="3" 
                    placeholder="Description de la liste..."></textarea>
        </div>
        <div class="form-group">
          <label>Icon (emoji)</label>
          <div class="icon-input-container">
            <input type="text" [(ngModel)]="newWatchlist.icon" placeholder="👁️" readonly>
            <button type="button" (click)="openIconPicker()" class="btn-icon-picker">Choisir</button>
          </div>
        </div>
        <div class="form-group">
          <label>Lien (optionnel)</label>
          <input type="url" [(ngModel)]="newWatchlist.link" placeholder="https://...">
        </div>
        <button (click)="createWatchlist()" class="btn-submit" [disabled]="!newWatchlist.name">
          Créer la liste
        </button>
      </div>
    </app-modal>

    <app-modal 
      [isOpen]="isAddStockModalOpen" 
      [title]="'Ajouter à une liste'"
      (close)="closeAddStockModal()">
      <div class="modal-form" *ngIf="selectedStockToAdd">
        <div class="stock-preview">
          <strong>{{ selectedStockToAdd.symbol }}</strong>
          <span>{{ selectedStockToAdd.name }}</span>
        </div>
        <div class="watchlist-selector">
          <div class="watchlist-option" 
               *ngFor="let wl of watchlists"
               (click)="addToSelectedWatchlist(wl.id)"
               [class.selected]="selectedWatchlistsForAdd.has(wl.id)">
            <div class="option-icon">{{ wl.icon || '📋' }}</div>
            <div class="option-info">
              <strong>{{ wl.name }}</strong>
              <span>{{ wl.stocks?.length || 0 }} actions</span>
            </div>
            <div class="option-check" *ngIf="selectedWatchlistsForAdd.has(wl.id)">✓</div>
          </div>
        </div>
        <button (click)="confirmAddStock()" 
                class="btn-submit" 
                [disabled]="selectedWatchlistsForAdd.size === 0">
          Ajouter aux listes sélectionnées
        </button>
      </div>
    </app-modal>

    <app-icon-picker
      [isOpen]="isIconPickerOpen"
      (close)="closeIconPicker()"
      (iconSelected)="onIconSelected($event)">
    </app-icon-picker>
  `,
  styles: [`
    .watchlist-container {
      max-width: 1400px;
      margin: 0 auto;
      padding: 2rem;
      min-height: 100vh;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
      animation: fadeIn 0.5s ease;
    }

    .title {
      font-size: 2.5rem;
      font-weight: 700;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      margin: 0;
    }

    .btn-create {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      padding: 0.875rem 1.75rem;
      border-radius: 12px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .btn-create:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
    }

    .icon {
      font-size: 1.25rem;
    }

    .watchlists-grid {
      display: grid;
      gap: 1.5rem;
    }

    .watchlist-card {
      background: white;
      border-radius: 16px;
      padding: 1.5rem;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
      transition: all 0.3s ease;
      animation: slideUp 0.5s ease;
    }

    .watchlist-card:hover {
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
    }

    .card-header {
      display: flex;
      gap: 1rem;
      margin-bottom: 1rem;
    }

    .watchlist-icon {
      font-size: 2.5rem;
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea20 0%, #764ba220 100%);
      border-radius: 12px;
    }

    .watchlist-info h2 {
      font-size: 1.5rem;
      font-weight: 700;
      color: #1f2937;
      margin: 0 0 0.5rem 0;
    }

    .description {
      color: #6b7280;
      margin: 0 0 0.5rem 0;
      font-size: 0.9rem;
    }

    .watchlist-link {
      color: #667eea;
      text-decoration: none;
      font-size: 0.875rem;
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
    }

    .watchlist-link:hover {
      text-decoration: underline;
    }

    .watchlist-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 1rem;
      border-bottom: 2px solid #f3f4f6;
    }

    .btn-toggle {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      padding: 0.75rem 1.5rem;
      border-radius: 10px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .btn-toggle:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    .btn-delete-small {
      background: #ef4444;
      color: white;
      border: none;
      width: 36px;
      height: 36px;
      border-radius: 50%;
      font-size: 1.5rem;
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .btn-delete-small:hover {
      background: #dc2626;
      transform: scale(1.1);
    }

    .watchlist-content {
      padding-top: 1.5rem;
      animation: slideDown 0.3s ease;
    }

    .add-stock-section {
      margin-bottom: 1.5rem;
    }

    .search-container {
      position: relative;
    }

    .search-input {
      width: 100%;
      padding: 1rem;
      border: 2px solid #e5e7eb;
      border-radius: 12px;
      font-size: 1rem;
      transition: all 0.3s ease;
    }

    .search-input:focus {
      outline: none;
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }

    .search-results {
      margin-top: 1rem;
      border: 1px solid #e5e7eb;
      border-radius: 12px;
      max-height: 200px;
      overflow-y: auto;
      background: white;
      position: absolute;
      width: 100%;
      z-index: 10;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
    }

    .stock-result {
      padding: 1rem;
      cursor: pointer;
      border-bottom: 1px solid #f3f4f6;
      display: flex;
      justify-content: space-between;
      align-items: center;
      transition: all 0.2s ease;
    }

    .stock-result:hover {
      background: linear-gradient(135deg, #667eea10 0%, #764ba210 100%);
    }

    .stock-result:last-child {
      border-bottom: none;
    }

    .stock-info {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .stock-name {
      color: #6b7280;
      font-size: 0.875rem;
    }

    .stock-type {
      background: #667eea;
      color: white;
      padding: 0.25rem 0.75rem;
      border-radius: 20px;
      font-size: 0.75rem;
      font-weight: 600;
    }

    .table-controls {
      margin-bottom: 1rem;
    }

    .filter-input {
      padding: 0.75rem;
      border: 2px solid #e5e7eb;
      border-radius: 10px;
      font-size: 0.875rem;
      width: 250px;
      transition: all 0.3s ease;
    }

    .filter-input:focus {
      outline: none;
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }

    .stocks-table-container {
      overflow-x: auto;
    }

    .stocks-table {
      width: 100%;
      border-collapse: collapse;
    }

    .stocks-table th {
      background: linear-gradient(135deg, #667eea10 0%, #764ba210 100%);
      padding: 1rem;
      text-align: left;
      font-weight: 700;
      color: #374151;
      border-bottom: 2px solid #e5e7eb;
    }

    .stocks-table th.sortable {
      cursor: pointer;
      user-select: none;
      transition: all 0.2s ease;
    }

    .stocks-table th.sortable:hover {
      background: linear-gradient(135deg, #667eea20 0%, #764ba220 100%);
    }

    .stocks-table td {
      padding: 1rem;
      border-bottom: 1px solid #f3f4f6;
    }

    .stocks-table tbody tr:hover {
      background: #f9fafb;
    }

    .badge {
      padding: 0.375rem 0.75rem;
      border-radius: 20px;
      font-size: 0.75rem;
      font-weight: 600;
      display: inline-block;
    }

    .badge-type {
      text-transform: uppercase;
    }

    .type-stock {
      background: #3b82f6;
      color: white;
    }

    .type-etf {
      background: #8b5cf6;
      color: white;
    }

    .type-crypto {
      background: #f59e0b;
      color: white;
    }

    .badge-priority.priority-low {
      background: #10b981;
      color: white;
    }

    .badge-priority.priority-medium {
      background: #f59e0b;
      color: white;
    }

    .badge-priority.priority-high {
      background: #ef4444;
      color: white;
    }

    .badge-priority.priority-critical {
      background: #991b1b;
      color: white;
    }

    .badge-priority.priority-none {
      background: #e5e7eb;
      color: #6b7280;
    }

    .score-container {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .score-bar {
      flex: 1;
      height: 8px;
      background: #e5e7eb;
      border-radius: 4px;
      overflow: hidden;
    }

    .score-fill {
      height: 100%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      transition: width 0.3s ease;
    }

    .score-text {
      font-weight: 600;
      color: #667eea;
      min-width: 40px;
    }

    .tags-container {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
    }

    .tag {
      background: #f3f4f6;
      color: #374151;
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 500;
    }

    .tag-add {
      background: #667eea;
      color: white;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .tag-add:hover {
      background: #5568d3;
    }

    .btn-remove-small {
      background: #ef4444;
      color: white;
      border: none;
      width: 32px;
      height: 32px;
      border-radius: 50%;
      font-size: 1.25rem;
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .btn-remove-small:hover {
      background: #dc2626;
      transform: scale(1.1);
    }

    .empty-state {
      text-align: center;
      padding: 4rem 2rem;
      color: #9ca3af;
    }

    .empty-icon {
      font-size: 4rem;
      margin-bottom: 1rem;
    }

    .empty-state p {
      font-size: 1.25rem;
      font-weight: 600;
      margin: 0 0 0.5rem 0;
    }

    .empty-state span {
      font-size: 0.875rem;
    }

    .modal-form {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
    }

    .modal-form .form-group label {
      display: block;
      font-weight: 600;
      color: #374151;
      margin-bottom: 0.5rem;
      font-size: 0.875rem;
    }

    .modal-form .form-group input,
    .modal-form .form-group textarea {
      width: 100%;
      padding: 0.75rem;
      border: 2px solid #e5e7eb;
      border-radius: 10px;
      font-size: 1rem;
      transition: all 0.3s ease;
    }

    .modal-form .form-group input:focus,
    .modal-form .form-group textarea:focus {
      outline: none;
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }

    .btn-submit {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      padding: 1rem;
      border-radius: 12px;
      font-weight: 600;
      font-size: 1rem;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .btn-submit:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    .btn-submit:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .icon-input-container {
      display: flex;
      gap: 0.5rem;
    }

    .icon-input-container input {
      flex: 1;
      text-align: center;
      font-size: 1.5rem;
    }

    .btn-icon-picker {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      padding: 0.75rem 1.25rem;
      border-radius: 10px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
      white-space: nowrap;
    }

    .btn-icon-picker:hover {
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
    }

    .stock-preview {
      background: linear-gradient(135deg, #667eea10 0%, #764ba210 100%);
      padding: 1rem;
      border-radius: 12px;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
      margin-bottom: 1rem;
    }

    .stock-preview strong {
      font-size: 1.125rem;
      color: #1f2937;
    }

    .stock-preview span {
      color: #6b7280;
      font-size: 0.875rem;
    }

    .watchlist-selector {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      max-height: 300px;
      overflow-y: auto;
    }

    .watchlist-option {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 1rem;
      border: 2px solid #e5e7eb;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .watchlist-option:hover {
      border-color: #667eea;
      background: #f9fafb;
    }

    .watchlist-option.selected {
      border-color: #667eea;
      background: linear-gradient(135deg, #667eea10 0%, #764ba210 100%);
    }

    .option-icon {
      font-size: 2rem;
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f3f4f6;
      border-radius: 10px;
    }

    .option-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .option-info strong {
      color: #1f2937;
      font-size: 1rem;
    }

    .option-info span {
      color: #6b7280;
      font-size: 0.875rem;
    }

    .option-check {
      color: #667eea;
      font-size: 1.5rem;
      font-weight: 700;
    }

    @keyframes fadeIn {
      from {
        opacity: 0;
      }
      to {
        opacity: 1;
      }
    }

    @keyframes slideUp {
      from {
        opacity: 0;
        transform: translateY(20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @keyframes slideDown {
      from {
        opacity: 0;
        max-height: 0;
      }
      to {
        opacity: 1;
        max-height: 2000px;
      }
    }
  `],
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('500ms ease', style({ opacity: 1 }))
      ])
    ]),
    trigger('slideUp', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(20px)' }),
        animate('500ms ease', style({ opacity: 1, transform: 'translateY(0)' }))
      ])
    ]),
    trigger('slideDown', [
      transition(':enter', [
        style({ opacity: 0, maxHeight: 0 }),
        animate('300ms ease', style({ opacity: 1, maxHeight: '2000px' }))
      ]),
      transition(':leave', [
        animate('300ms ease', style({ opacity: 0, maxHeight: 0 }))
      ])
    ])
  ]
})
export class WatchlistComponent implements OnInit {
  watchlists: Watchlist[] = [];
  expandedWatchlistId: number | null = null;
  searchQueries: {[key: number]: string} = {};
  searchResults: {[key: number]: Stock[]} = {};
  filterText: {[key: number]: string} = {};
  sortColumn: {[key: number]: string} = {};
  sortDirection: {[key: number]: 'asc' | 'desc'} = {};
  
  isCreateModalOpen = false;
  isIconPickerOpen = false;
  isAddStockModalOpen = false;
  selectedStockToAdd: Stock | null = null;
  selectedWatchlistsForAdd = new Set<number>();

  newWatchlist = {
    name: '',
    description: '',
    icon: '',
    link: ''
  };

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

  toggleWatchlist(watchlistId: number): void {
    this.expandedWatchlistId = this.expandedWatchlistId === watchlistId ? null : watchlistId;
  }

  searchStocks(watchlistId: number): void {
    const query = this.searchQueries[watchlistId];
    if (query && query.length >= 1) {
      this.stockService.searchStocks(query).subscribe(
        results => this.searchResults[watchlistId] = results
      );
    } else {
      this.searchResults[watchlistId] = [];
    }
  }

  showAddStockModal(watchlistId: number, stock: Stock): void {
    this.selectedStockToAdd = stock;
    this.selectedWatchlistsForAdd.clear();
    this.selectedWatchlistsForAdd.add(watchlistId);
    this.isAddStockModalOpen = true;
    this.searchResults[watchlistId] = [];
    this.searchQueries[watchlistId] = '';
  }

  addToSelectedWatchlist(watchlistId: number): void {
    if (this.selectedWatchlistsForAdd.has(watchlistId)) {
      this.selectedWatchlistsForAdd.delete(watchlistId);
    } else {
      this.selectedWatchlistsForAdd.add(watchlistId);
    }
  }

  confirmAddStock(): void {
    if (!this.selectedStockToAdd || this.selectedWatchlistsForAdd.size === 0) return;

    const promises: any[] = [];
    this.selectedWatchlistsForAdd.forEach(watchlistId => {
      promises.push(
        this.watchlistService.addStock(watchlistId, this.selectedStockToAdd!.id).toPromise()
      );
    });

    Promise.all(promises).then(() => {
      this.loadWatchlists();
      this.closeAddStockModal();
    });
  }

  closeAddStockModal(): void {
    this.isAddStockModalOpen = false;
    this.selectedStockToAdd = null;
    this.selectedWatchlistsForAdd.clear();
  }

  removeStockFromWatchlist(watchlistId: number, stockId: number): void {
    if (confirm('Retirer cette action de la liste ?')) {
      this.watchlistService.removeStock(watchlistId, stockId).subscribe(() => {
        this.loadWatchlists();
      });
    }
  }

  openCreateModal(): void {
    this.isCreateModalOpen = true;
  }

  closeCreateModal(): void {
    this.isCreateModalOpen = false;
    this.newWatchlist = {
      name: '',
      description: '',
      icon: '',
      link: ''
    };
  }

  createWatchlist(): void {
    if (!this.newWatchlist.name) return;

    this.watchlistService.createWatchlist(
      this.newWatchlist.name,
      this.newWatchlist.description,
      this.newWatchlist.link,
      this.newWatchlist.icon
    ).subscribe(() => {
      this.loadWatchlists();
      this.closeCreateModal();
    });
  }

  deleteWatchlist(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette liste ?')) {
      this.watchlistService.deleteWatchlist(id).subscribe(() => {
        this.loadWatchlists();
      });
    }
  }

  sortBy(watchlistId: number, column: string): void {
    if (this.sortColumn[watchlistId] === column) {
      this.sortDirection[watchlistId] = this.sortDirection[watchlistId] === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn[watchlistId] = column;
      this.sortDirection[watchlistId] = 'asc';
    }
  }

  getSortIcon(watchlistId: number, column: string): string {
    if (this.sortColumn[watchlistId] !== column) return '';
    return this.sortDirection[watchlistId] === 'asc' ? '↑' : '↓';
  }

  getFilteredStocks(watchlistId: number, stocks: Stock[]): Stock[] {
    if (!stocks) return [];

    let filtered = [...stocks];

    const filter = this.filterText[watchlistId];
    if (filter) {
      const lowerFilter = filter.toLowerCase();
      filtered = filtered.filter(s => 
        s.symbol.toLowerCase().includes(lowerFilter) ||
        s.name.toLowerCase().includes(lowerFilter) ||
        s.stockType?.toLowerCase().includes(lowerFilter) ||
        s.tags?.some(t => t.name.toLowerCase().includes(lowerFilter))
      );
    }

    const sortCol = this.sortColumn[watchlistId];
    if (sortCol) {
      filtered.sort((a: any, b: any) => {
        let aVal = a[sortCol];
        let bVal = b[sortCol];

        if (aVal === null || aVal === undefined) return 1;
        if (bVal === null || bVal === undefined) return -1;

        if (typeof aVal === 'string') aVal = aVal.toLowerCase();
        if (typeof bVal === 'string') bVal = bVal.toLowerCase();

        if (aVal < bVal) return this.sortDirection[watchlistId] === 'asc' ? -1 : 1;
        if (aVal > bVal) return this.sortDirection[watchlistId] === 'asc' ? 1 : -1;
        return 0;
      });
    }

    return filtered;
  }

  openTagModal(stock: Stock): void {
    console.log('Open tag modal for', stock);
  }

  openIconPicker(): void {
    this.isIconPickerOpen = true;
  }

  closeIconPicker(): void {
    this.isIconPickerOpen = false;
  }

  onIconSelected(icon: string): void {
    this.newWatchlist.icon = icon;
  }
}
