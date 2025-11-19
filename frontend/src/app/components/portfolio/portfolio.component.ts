import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { PortfolioService } from '../../services/portfolio.service';
import { StockService } from '../../services/stock.service';
import { Portfolio, PortfolioStock, Stock, StockType, Priority } from '../../models/models';

@Component({
  selector: 'app-portfolio',
  template: `
    <div class="portfolio-container" [@fadeIn]>
      <div class="header" *ngIf="!selectedPortfolioId">
        <h1 class="title">Mes Portefeuilles</h1>
        <button (click)="openCreateModal()" class="btn-create">
          <span class="icon">+</span> Créer un portefeuille
        </button>
      </div>
      
      <div class="portfolios-grid" *ngIf="!selectedPortfolioId">
        <div class="portfolio-card" *ngFor="let portfolio of portfolios" [@slideUp]>
          <div class="card-header">
            <div class="portfolio-icon" *ngIf="portfolio.icon">{{ portfolio.icon }}</div>
            <div class="portfolio-info">
              <h2>{{ portfolio.name }}</h2>
              <p class="description">{{ portfolio.description }}</p>
              <a *ngIf="portfolio.link" [href]="portfolio.link" target="_blank" class="portfolio-link">
                🔗 Lien
              </a>
            </div>
          </div>
          <div class="card-actions">
            <button [routerLink]="[portfolio.id]" class="btn-manage">Gérer</button>
            <button (click)="deletePortfolio(portfolio.id)" class="btn-delete">Supprimer</button>
          </div>
        </div>
      </div>

      <div class="portfolio-detail" *ngIf="selectedPortfolioId">
        <button (click)="goBack()" class="btn-back">← Retour</button>
        <h1 class="title">Gérer le portefeuille</h1>
        
        <div class="add-stock-card">
          <h2>Ajouter une action</h2>
          <div class="search-container">
            <input 
              type="text" 
              [(ngModel)]="searchQuery" 
              (input)="searchStocks()"
              placeholder="🔍 Rechercher une action (symbole ou nom)..."
              class="search-input">
          </div>
          
          <div class="search-results" *ngIf="searchResults.length > 0">
            <div class="stock-result" *ngFor="let stock of searchResults" (click)="selectStock(stock)">
              <div class="stock-info">
                <strong>{{ stock.symbol }}</strong>
                <span class="stock-name">{{ stock.name }}</span>
              </div>
              <span class="stock-type">{{ stock.stockType }}</span>
            </div>
          </div>

          <div class="add-stock-form" *ngIf="selectedStock">
            <div class="form-header">
              <h3>{{ selectedStock.symbol }} - {{ selectedStock.name }}</h3>
            </div>
            <div class="form-grid">
              <div class="form-group">
                <label>Quantité</label>
                <input type="number" [(ngModel)]="newStock.quantity" step="0.01" min="0">
              </div>
              <div class="form-group">
                <label>Prix d'achat (€)</label>
                <input type="number" [(ngModel)]="newStock.purchasePrice" step="0.01" min="0">
              </div>
              <div class="form-group">
                <label>Date d'achat</label>
                <input type="date" [(ngModel)]="newStock.purchaseDate">
              </div>
              <div class="form-group full-width">
                <label>Notes</label>
                <textarea [(ngModel)]="newStock.notes" rows="2" placeholder="Notes optionnelles..."></textarea>
              </div>
            </div>
            <div class="form-actions">
              <button (click)="addStockToPortfolio()" class="btn-add">Ajouter au portefeuille</button>
              <button (click)="cancelAddStock()" class="btn-cancel">Annuler</button>
            </div>
          </div>
        </div>

        <div class="stocks-card">
          <h2>Actions du portefeuille</h2>
          <div *ngIf="portfolioStocks.length === 0" class="empty-state">
            <div class="empty-icon">📊</div>
            <p>Aucune action dans ce portefeuille</p>
            <span>Ajoutez votre première action ci-dessus</span>
          </div>
          
          <div *ngIf="portfolioStocks.length > 0" class="stocks-table-container">
            <table class="stocks-table">
              <thead>
                <tr>
                  <th>Action</th>
                  <th>Quantité</th>
                  <th>Total investi</th>
                  <th>Historique</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <ng-container *ngFor="let ps of getGroupedStocks()">
                  <tr>
                    <td>
                      <div class="stock-cell">
                        <strong>{{ ps.stock.symbol }}</strong>
                        <span class="stock-name">{{ ps.stock.name }}</span>
                      </div>
                    </td>
                    <td>{{ ps.totalQuantity }}</td>
                    <td class="amount">{{ ps.totalInvested.toFixed(2) }} €</td>
                    <td>
                      <button (click)="toggleHistory(ps.stock.id)" class="btn-history">
                        {{ expandedStockId === ps.stock.id ? '▼' : '▶' }} 
                        {{ ps.transactions.length }} transaction(s)
                      </button>
                    </td>
                    <td>
                      <button (click)="removeAllStock(ps.stock.id)" class="btn-remove-small">×</button>
                    </td>
                  </tr>
                  <tr *ngIf="expandedStockId === ps.stock.id" class="history-row" 
                      [@slideDown]
                      [attr.data-stock]="ps.stock.id">
                    <td colspan="5">
                      <div class="transaction-history">
                        <h4>Historique des transactions</h4>
                        <div class="transaction-list">
                          <div class="transaction-item" *ngFor="let transaction of ps.transactions">
                            <div class="transaction-date">
                              📅 {{ transaction.purchaseDate | date:'dd/MM/yyyy' }}
                            </div>
                            <div class="transaction-details">
                              <span class="quantity">{{ transaction.quantity }} actions</span>
                              <span class="price">{{ transaction.purchasePrice }} € / action</span>
                              <span class="total">Total: {{ (transaction.quantity * transaction.purchasePrice).toFixed(2) }} €</span>
                            </div>
                            <div class="transaction-notes" *ngIf="transaction.notes">
                              💭 {{ transaction.notes }}
                            </div>
                            <button (click)="removeStock(transaction.id)" class="btn-remove-transaction">
                              Supprimer
                            </button>
                          </div>
                        </div>
                      </div>
                    </td>
                  </tr>
                </ng-container>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <app-modal 
      [isOpen]="isCreateModalOpen" 
      [title]="'Créer un portefeuille'"
      (close)="closeCreateModal()">
      <div class="modal-form">
        <div class="form-group">
          <label>Nom du portefeuille *</label>
          <input type="text" [(ngModel)]="newPortfolio.name" placeholder="Mon portefeuille">
        </div>
        <div class="form-group">
          <label>Description</label>
          <textarea [(ngModel)]="newPortfolio.description" rows="3" 
                    placeholder="Description du portefeuille..."></textarea>
        </div>
        <div class="form-group">
          <label>Icon (emoji)</label>
          <div class="icon-input-container">
            <input type="text" [(ngModel)]="newPortfolio.icon" placeholder="📈" readonly>
            <button type="button" (click)="openIconPicker()" class="btn-icon-picker">Choisir</button>
          </div>
        </div>
        <div class="form-group">
          <label>Lien (optionnel)</label>
          <input type="url" [(ngModel)]="newPortfolio.link" placeholder="https://...">
        </div>
        <button (click)="createPortfolio()" class="btn-submit" [disabled]="!newPortfolio.name">
          Créer le portefeuille
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
    .portfolio-container {
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

    .portfolios-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 1.5rem;
      animation: fadeIn 0.5s ease;
    }

    .portfolio-card {
      background: white;
      border-radius: 16px;
      padding: 1.5rem;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
      transition: all 0.3s ease;
      animation: slideUp 0.5s ease;
    }

    .portfolio-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
    }

    .card-header {
      display: flex;
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .portfolio-icon {
      font-size: 2.5rem;
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea20 0%, #764ba220 100%);
      border-radius: 12px;
    }

    .portfolio-info h2 {
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

    .portfolio-link {
      color: #667eea;
      text-decoration: none;
      font-size: 0.875rem;
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
    }

    .portfolio-link:hover {
      text-decoration: underline;
    }

    .card-actions {
      display: flex;
      gap: 0.75rem;
    }

    .btn-manage {
      flex: 1;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      padding: 0.75rem;
      border-radius: 10px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .btn-manage:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    .btn-delete {
      background: #ef4444;
      color: white;
      border: none;
      padding: 0.75rem 1.25rem;
      border-radius: 10px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .btn-delete:hover {
      background: #dc2626;
      transform: translateY(-2px);
    }

    .btn-back {
      background: #6b7280;
      color: white;
      border: none;
      padding: 0.75rem 1.5rem;
      border-radius: 10px;
      font-weight: 600;
      cursor: pointer;
      margin-bottom: 1.5rem;
      transition: all 0.3s ease;
    }

    .btn-back:hover {
      background: #4b5563;
    }

    .add-stock-card, .stocks-card {
      background: white;
      border-radius: 16px;
      padding: 2rem;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
      margin-bottom: 2rem;
    }

    .add-stock-card h2, .stocks-card h2 {
      font-size: 1.5rem;
      font-weight: 700;
      color: #1f2937;
      margin: 0 0 1.5rem 0;
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
      max-height: 300px;
      overflow-y: auto;
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

    .add-stock-form {
      margin-top: 1.5rem;
      padding-top: 1.5rem;
      border-top: 2px solid #f3f4f6;
    }

    .form-header h3 {
      font-size: 1.25rem;
      font-weight: 600;
      color: #667eea;
      margin: 0 0 1.5rem 0;
    }

    .form-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .form-group {
      display: flex;
      flex-direction: column;
    }

    .form-group.full-width {
      grid-column: 1 / -1;
    }

    .form-group label {
      font-weight: 600;
      color: #374151;
      margin-bottom: 0.5rem;
      font-size: 0.875rem;
    }

    .form-group input,
    .form-group textarea {
      padding: 0.75rem;
      border: 2px solid #e5e7eb;
      border-radius: 10px;
      font-size: 1rem;
      transition: all 0.3s ease;
    }

    .form-group input:focus,
    .form-group textarea:focus {
      outline: none;
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }

    .form-actions {
      display: flex;
      gap: 1rem;
    }

    .btn-add {
      flex: 1;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      padding: 1rem;
      border-radius: 12px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .btn-add:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    .btn-cancel {
      background: #f3f4f6;
      color: #6b7280;
      border: none;
      padding: 1rem 1.5rem;
      border-radius: 12px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .btn-cancel:hover {
      background: #e5e7eb;
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

    .stocks-table td {
      padding: 1rem;
      border-bottom: 1px solid #f3f4f6;
    }

    .stocks-table tbody tr:hover {
      background: #f9fafb;
    }

    .stock-cell {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .stock-cell strong {
      color: #1f2937;
      font-size: 1rem;
    }

    .stock-cell .stock-name {
      color: #6b7280;
      font-size: 0.875rem;
    }

    .amount {
      font-weight: 600;
      color: #667eea;
    }

    .btn-history {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      padding: 0.5rem 1rem;
      border-radius: 8px;
      font-size: 0.875rem;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .btn-history:hover {
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
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

    .history-row td {
      padding: 0 !important;
      background: #f9fafb;
    }

    .transaction-history {
      padding: 1.5rem;
    }

    .transaction-history h4 {
      font-size: 1.125rem;
      font-weight: 700;
      color: #374151;
      margin: 0 0 1rem 0;
    }

    .transaction-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .transaction-item {
      background: white;
      padding: 1rem;
      border-radius: 12px;
      border-left: 4px solid #667eea;
      display: grid;
      grid-template-columns: auto 1fr auto;
      gap: 1rem;
      align-items: center;
    }

    .transaction-date {
      font-weight: 600;
      color: #374151;
      white-space: nowrap;
    }

    .transaction-details {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
      font-size: 0.875rem;
    }

    .transaction-details .quantity {
      color: #667eea;
      font-weight: 600;
    }

    .transaction-details .price {
      color: #6b7280;
    }

    .transaction-details .total {
      color: #374151;
      font-weight: 600;
    }

    .transaction-notes {
      grid-column: 1 / -1;
      color: #6b7280;
      font-size: 0.875rem;
      font-style: italic;
      padding-top: 0.5rem;
      border-top: 1px solid #f3f4f6;
    }

    .btn-remove-transaction {
      background: #ef4444;
      color: white;
      border: none;
      padding: 0.5rem 1rem;
      border-radius: 8px;
      font-size: 0.875rem;
      cursor: pointer;
      transition: all 0.3s ease;
      white-space: nowrap;
    }

    .btn-remove-transaction:hover {
      background: #dc2626;
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
        max-height: 500px;
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
        animate('300ms ease', style({ opacity: 1, maxHeight: '500px' }))
      ]),
      transition(':leave', [
        animate('300ms ease', style({ opacity: 0, maxHeight: 0 }))
      ])
    ])
  ]
})
export class PortfolioComponent implements OnInit {
  portfolios: Portfolio[] = [];
  selectedPortfolioId: number | null = null;
  portfolioStocks: PortfolioStock[] = [];
  searchQuery: string = '';
  searchResults: Stock[] = [];
  selectedStock: Stock | null = null;
  expandedStockId: number | null = null;
  isCreateModalOpen = false;
  isIconPickerOpen = false;
  
  newStock = {
    quantity: 0,
    purchasePrice: 0,
    purchaseDate: new Date().toISOString().split('T')[0],
    notes: ''
  };

  newPortfolio = {
    name: '',
    description: '',
    icon: '',
    link: ''
  };

  constructor(
    private portfolioService: PortfolioService,
    private stockService: StockService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.selectedPortfolioId = +params['id'];
        this.loadPortfolioStocks();
      } else {
        this.selectedPortfolioId = null;
        this.loadPortfolios();
      }
    });
  }

  loadPortfolios(): void {
    this.portfolioService.getPortfolios().subscribe(
      portfolios => this.portfolios = portfolios
    );
  }

  loadPortfolioStocks(): void {
    if (this.selectedPortfolioId) {
      this.portfolioService.getPortfolioStocks(this.selectedPortfolioId).subscribe(
        stocks => this.portfolioStocks = stocks
      );
    }
  }

  getGroupedStocks(): any[] {
    const grouped = new Map();
    
    this.portfolioStocks.forEach(ps => {
      const stockId = ps.stock.id;
      if (!grouped.has(stockId)) {
        grouped.set(stockId, {
          stock: ps.stock,
          totalQuantity: 0,
          totalInvested: 0,
          transactions: []
        });
      }
      
      const group = grouped.get(stockId);
      group.totalQuantity += ps.quantity;
      group.totalInvested += ps.quantity * ps.purchasePrice;
      group.transactions.push(ps);
    });

    return Array.from(grouped.values());
  }

  toggleHistory(stockId: number): void {
    this.expandedStockId = this.expandedStockId === stockId ? null : stockId;
  }

  searchStocks(): void {
    if (this.searchQuery.length >= 1) {
      this.stockService.searchStocks(this.searchQuery).subscribe(
        results => this.searchResults = results
      );
    } else {
      this.searchResults = [];
    }
  }

  selectStock(stock: Stock): void {
    this.selectedStock = stock;
    this.searchResults = [];
    this.searchQuery = '';
  }

  cancelAddStock(): void {
    this.selectedStock = null;
    this.newStock = {
      quantity: 0,
      purchasePrice: 0,
      purchaseDate: new Date().toISOString().split('T')[0],
      notes: ''
    };
  }

  addStockToPortfolio(): void {
    if (!this.selectedPortfolioId || !this.selectedStock) return;

    const request = {
      stockId: this.selectedStock.id,
      quantity: this.newStock.quantity,
      purchasePrice: this.newStock.purchasePrice,
      purchaseDate: new Date(this.newStock.purchaseDate).toISOString(),
      notes: this.newStock.notes
    };

    this.portfolioService.addStock(this.selectedPortfolioId, request).subscribe(
      () => {
        this.loadPortfolioStocks();
        this.cancelAddStock();
      }
    );
  }

  removeStock(portfolioStockId: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette transaction ?')) {
      this.portfolioService.removeStock(portfolioStockId).subscribe(
        () => this.loadPortfolioStocks()
      );
    }
  }

  removeAllStock(stockId: number): void {
    if (confirm('Êtes-vous sûr de vouloir retirer toutes les transactions de cette action ?')) {
      const stockTransactions = this.portfolioStocks.filter(ps => ps.stock.id === stockId);
      let completed = 0;
      
      stockTransactions.forEach(ps => {
        this.portfolioService.removeStock(ps.id).subscribe(() => {
          completed++;
          if (completed === stockTransactions.length) {
            this.loadPortfolioStocks();
          }
        });
      });
    }
  }

  openCreateModal(): void {
    this.isCreateModalOpen = true;
  }

  closeCreateModal(): void {
    this.isCreateModalOpen = false;
    this.newPortfolio = {
      name: '',
      description: '',
      icon: '',
      link: ''
    };
  }

  createPortfolio(): void {
    if (!this.newPortfolio.name) return;

    this.portfolioService.createPortfolio(
      this.newPortfolio.name,
      this.newPortfolio.description,
      this.newPortfolio.link,
      this.newPortfolio.icon
    ).subscribe(() => {
      this.loadPortfolios();
      this.closeCreateModal();
    });
  }

  deletePortfolio(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce portefeuille ?')) {
      this.portfolioService.deletePortfolio(id).subscribe(() => {
        this.loadPortfolios();
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/portfolios']);
  }

  openIconPicker(): void {
    this.isIconPickerOpen = true;
  }

  closeIconPicker(): void {
    this.isIconPickerOpen = false;
  }

  onIconSelected(icon: string): void {
    this.newPortfolio.icon = icon;
  }
}
