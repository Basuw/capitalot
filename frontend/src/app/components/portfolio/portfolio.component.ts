import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PortfolioService } from '../../services/portfolio.service';
import { StockService } from '../../services/stock.service';
import { Portfolio, PortfolioStock, Stock } from '../../models/models';

@Component({
  selector: 'app-portfolio',
  template: `
    <div class="portfolio-container" *ngIf="!selectedPortfolioId">
      <h1>Mes Portefeuilles</h1>
      <button (click)="createPortfolio()" class="btn-create">+ Créer un portefeuille</button>
      
      <div class="portfolios-list">
        <div class="portfolio-item" *ngFor="let portfolio of portfolios">
          <div class="portfolio-header">
            <h2>{{ portfolio.name }}</h2>
            <button (click)="deletePortfolio(portfolio.id)" class="btn-delete">Supprimer</button>
          </div>
          <p>{{ portfolio.description }}</p>
          <a [routerLink]="[portfolio.id]" class="btn-manage">Gérer les actions</a>
        </div>
      </div>
    </div>

    <div class="portfolio-detail" *ngIf="selectedPortfolioId">
      <button (click)="goBack()" class="btn-back">← Retour</button>
      <h1>Gérer les actions du portefeuille</h1>
      
      <div class="add-stock-section">
        <h2>Ajouter une action</h2>
        <div class="search-box">
          <input 
            type="text" 
            [(ngModel)]="searchQuery" 
            (input)="searchStocks()"
            placeholder="Rechercher une action (symbole ou nom)..."
            class="search-input">
        </div>
        
        <div class="search-results" *ngIf="searchResults.length > 0">
          <div class="stock-result" *ngFor="let stock of searchResults" (click)="selectStock(stock)">
            <strong>{{ stock.symbol }}</strong> - {{ stock.name }}
            <span class="exchange">{{ stock.exchange }}</span>
          </div>
        </div>

        <div class="add-stock-form" *ngIf="selectedStock">
          <h3>{{ selectedStock.symbol }} - {{ selectedStock.name }}</h3>
          <div class="form-group">
            <label>Quantité:</label>
            <input type="number" [(ngModel)]="newStock.quantity" step="0.01" min="0">
          </div>
          <div class="form-group">
            <label>Prix d'achat:</label>
            <input type="number" [(ngModel)]="newStock.purchasePrice" step="0.01" min="0">
          </div>
          <div class="form-group">
            <label>Date d'achat:</label>
            <input type="date" [(ngModel)]="newStock.purchaseDate">
          </div>
          <div class="form-group">
            <label>Notes:</label>
            <textarea [(ngModel)]="newStock.notes" rows="3"></textarea>
          </div>
          <div class="form-actions">
            <button (click)="addStockToPortfolio()" class="btn-add">Ajouter</button>
            <button (click)="cancelAddStock()" class="btn-cancel">Annuler</button>
          </div>
        </div>
      </div>

      <div class="stocks-list">
        <h2>Actions du portefeuille</h2>
        <div *ngIf="portfolioStocks.length === 0" class="empty-message">
          Aucune action dans ce portefeuille
        </div>
        <table *ngIf="portfolioStocks.length > 0">
          <thead>
            <tr>
              <th>Symbole</th>
              <th>Nom</th>
              <th>Quantité</th>
              <th>Prix d'achat</th>
              <th>Date d'achat</th>
              <th>Notes</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let ps of portfolioStocks">
              <td><strong>{{ ps.stock.symbol }}</strong></td>
              <td>{{ ps.stock.name }}</td>
              <td>{{ ps.quantity }}</td>
              <td>{{ ps.purchasePrice }} {{ ps.stock.currency }}</td>
              <td>{{ ps.purchaseDate | date:'short' }}</td>
              <td>{{ ps.notes || '-' }}</td>
              <td>
                <button (click)="removeStock(ps.id)" class="btn-remove">Retirer</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .portfolio-container, .portfolio-detail {
      max-width: 1200px;
      margin: 0 auto;
      padding: 1rem;
    }
    
    h1 {
      color: #2c3e50;
      margin-bottom: 1.5rem;
    }

    h2 {
      color: #34495e;
      margin: 1.5rem 0 1rem 0;
    }

    h3 {
      color: #2c3e50;
      margin: 1rem 0;
    }
    
    .btn-create, .btn-back {
      background-color: #2ecc71;
      color: white;
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      margin-bottom: 2rem;
    }

    .btn-back {
      background-color: #95a5a6;
    }
    
    .portfolios-list {
      display: grid;
      gap: 1rem;
    }
    
    .portfolio-item {
      background: white;
      padding: 1.5rem;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .portfolio-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }
    
    .portfolio-header h2 {
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
    
    .btn-manage {
      background-color: #3498db;
      color: white;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      text-decoration: none;
      display: inline-block;
      margin-top: 1rem;
    }

    .add-stock-section {
      background: white;
      padding: 1.5rem;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      margin-bottom: 2rem;
    }

    .search-input {
      width: 100%;
      padding: 0.75rem;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 1rem;
    }

    .search-results {
      margin-top: 1rem;
      border: 1px solid #ddd;
      border-radius: 4px;
      max-height: 300px;
      overflow-y: auto;
    }

    .stock-result {
      padding: 0.75rem;
      cursor: pointer;
      border-bottom: 1px solid #eee;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .stock-result:hover {
      background-color: #f8f9fa;
    }

    .exchange {
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .add-stock-form {
      margin-top: 1.5rem;
      padding-top: 1.5rem;
      border-top: 1px solid #ddd;
    }

    .form-group {
      margin-bottom: 1rem;
    }

    .form-group label {
      display: block;
      margin-bottom: 0.5rem;
      color: #2c3e50;
      font-weight: 500;
    }

    .form-group input,
    .form-group textarea {
      width: 100%;
      padding: 0.5rem;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 1rem;
    }

    .form-actions {
      display: flex;
      gap: 1rem;
      margin-top: 1rem;
    }

    .btn-add {
      background-color: #2ecc71;
      color: white;
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }

    .btn-cancel {
      background-color: #95a5a6;
      color: white;
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }

    .stocks-list {
      background: white;
      padding: 1.5rem;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .empty-message {
      color: #7f8c8d;
      text-align: center;
      padding: 2rem;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 1rem;
    }

    th, td {
      padding: 0.75rem;
      text-align: left;
      border-bottom: 1px solid #eee;
    }

    th {
      background-color: #f8f9fa;
      font-weight: 600;
      color: #2c3e50;
    }

    .btn-remove {
      background-color: #e74c3c;
      color: white;
      padding: 0.5rem 1rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.9rem;
    }

    .btn-remove:hover {
      background-color: #c0392b;
    }
  `]
})
export class PortfolioComponent implements OnInit {
  portfolios: Portfolio[] = [];
  selectedPortfolioId: number | null = null;
  portfolioStocks: PortfolioStock[] = [];
  searchQuery: string = '';
  searchResults: Stock[] = [];
  selectedStock: Stock | null = null;
  newStock = {
    quantity: 0,
    purchasePrice: 0,
    purchaseDate: new Date().toISOString().split('T')[0],
    notes: ''
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
    if (confirm('Êtes-vous sûr de vouloir retirer cette action du portefeuille ?')) {
      this.portfolioService.removeStock(portfolioStockId).subscribe(
        () => this.loadPortfolioStocks()
      );
    }
  }

  createPortfolio(): void {
    const name = prompt('Nom du portefeuille:');
    const description = prompt('Description:');
    if (name) {
      this.portfolioService.createPortfolio(name, description || '').subscribe(() => {
        this.loadPortfolios();
      });
    }
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
}
