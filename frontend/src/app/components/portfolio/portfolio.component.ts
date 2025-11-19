import { Component, OnInit } from '@angular/core';
import { PortfolioService } from '../../services/portfolio.service';
import { Portfolio } from '../../models/models';

@Component({
  selector: 'app-portfolio',
  template: `
    <div class="portfolio-container">
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
  `,
  styles: [`
    .portfolio-container {
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
  `]
})
export class PortfolioComponent implements OnInit {
  portfolios: Portfolio[] = [];

  constructor(private portfolioService: PortfolioService) {}

  ngOnInit(): void {
    this.loadPortfolios();
  }

  loadPortfolios(): void {
    this.portfolioService.getPortfolios().subscribe(
      portfolios => this.portfolios = portfolios
    );
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
}
