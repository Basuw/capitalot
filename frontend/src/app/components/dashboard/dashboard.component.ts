import { Component, OnInit } from '@angular/core';
import { StatsService } from '../../services/stats.service';
import { PortfolioService } from '../../services/portfolio.service';
import { PerformanceStats, Portfolio } from '../../models/models';

@Component({
  selector: 'app-dashboard',
  template: `
    <div class="dashboard">
      <h1>Tableau de bord</h1>
      
      <div class="stats-container">
        <div class="stat-card">
          <h3>Aujourd'hui</h3>
          <div class="stat-value" [class.positive]="dailyStats.totalGainLoss >= 0" 
               [class.negative]="dailyStats.totalGainLoss < 0">
            {{ dailyStats.totalGainLoss | number:'1.2-2' }} €
            <span class="percentage">({{ dailyStats.totalGainLossPercent | number:'1.2-2' }}%)</span>
          </div>
        </div>
        
        <div class="stat-card">
          <h3>Ce mois</h3>
          <div class="stat-value" [class.positive]="monthlyStats.totalGainLoss >= 0" 
               [class.negative]="monthlyStats.totalGainLoss < 0">
            {{ monthlyStats.totalGainLoss | number:'1.2-2' }} €
            <span class="percentage">({{ monthlyStats.totalGainLossPercent | number:'1.2-2' }}%)</span>
          </div>
        </div>
        
        <div class="stat-card">
          <h3>Cette année</h3>
          <div class="stat-value" [class.positive]="yearlyStats.totalGainLoss >= 0" 
               [class.negative]="yearlyStats.totalGainLoss < 0">
            {{ yearlyStats.totalGainLoss | number:'1.2-2' }} €
            <span class="percentage">({{ yearlyStats.totalGainLossPercent | number:'1.2-2' }}%)</span>
          </div>
        </div>
        
        <div class="stat-card">
          <h3>Total</h3>
          <div class="stat-value" [class.positive]="allTimeStats.totalGainLoss >= 0" 
               [class.negative]="allTimeStats.totalGainLoss < 0">
            {{ allTimeStats.totalGainLoss | number:'1.2-2' }} €
            <span class="percentage">({{ allTimeStats.totalGainLossPercent | number:'1.2-2' }}%)</span>
          </div>
          <div class="stat-detail">
            Investi: {{ allTimeStats.totalInvested | number:'1.2-2' }} € | 
            Valeur: {{ allTimeStats.currentValue | number:'1.2-2' }} €
          </div>
        </div>
      </div>
      
      <div class="portfolios-section">
        <h2>Mes portefeuilles</h2>
        <div class="portfolios-grid">
          <div class="portfolio-card" *ngFor="let portfolio of portfolios">
            <h3>{{ portfolio.name }}</h3>
            <p>{{ portfolio.description }}</p>
            <a [routerLink]="['/portfolios', portfolio.id]" class="btn-view">Voir détails</a>
          </div>
          <div class="portfolio-card add-card">
            <button (click)="createPortfolio()" class="btn-add">+ Nouveau portefeuille</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard {
      max-width: 1200px;
      margin: 0 auto;
    }
    
    h1 {
      color: #2c3e50;
      margin-bottom: 2rem;
    }
    
    .stats-container {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 1rem;
      margin-bottom: 3rem;
    }
    
    .stat-card {
      background: white;
      padding: 1.5rem;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .stat-card h3 {
      margin: 0 0 1rem 0;
      color: #7f8c8d;
      font-size: 0.9rem;
      text-transform: uppercase;
    }
    
    .stat-value {
      font-size: 1.8rem;
      font-weight: bold;
    }
    
    .stat-value.positive {
      color: #27ae60;
    }
    
    .stat-value.negative {
      color: #e74c3c;
    }
    
    .percentage {
      font-size: 1rem;
      margin-left: 0.5rem;
    }
    
    .stat-detail {
      margin-top: 0.5rem;
      font-size: 0.9rem;
      color: #7f8c8d;
    }
    
    .portfolios-section h2 {
      color: #2c3e50;
      margin-bottom: 1rem;
    }
    
    .portfolios-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 1rem;
    }
    
    .portfolio-card {
      background: white;
      padding: 1.5rem;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .portfolio-card h3 {
      margin: 0 0 0.5rem 0;
      color: #2c3e50;
    }
    
    .portfolio-card p {
      color: #7f8c8d;
      margin-bottom: 1rem;
    }
    
    .btn-view, .btn-add {
      background-color: #3498db;
      color: white;
      padding: 0.5rem 1rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      text-decoration: none;
      display: inline-block;
    }
    
    .add-card {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 150px;
    }
    
    .btn-add {
      font-size: 1.1rem;
    }
  `]
})
export class DashboardComponent implements OnInit {
  dailyStats: PerformanceStats = {} as PerformanceStats;
  monthlyStats: PerformanceStats = {} as PerformanceStats;
  yearlyStats: PerformanceStats = {} as PerformanceStats;
  allTimeStats: PerformanceStats = {} as PerformanceStats;
  portfolios: Portfolio[] = [];

  constructor(
    private statsService: StatsService,
    private portfolioService: PortfolioService
  ) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadPortfolios();
  }

  loadStats(): void {
    this.statsService.getDailyStats().subscribe(stats => this.dailyStats = stats);
    this.statsService.getMonthlyStats().subscribe(stats => this.monthlyStats = stats);
    this.statsService.getYearlyStats().subscribe(stats => this.yearlyStats = stats);
    this.statsService.getAllTimeStats().subscribe(stats => this.allTimeStats = stats);
  }

  loadPortfolios(): void {
    this.portfolioService.getPortfolios().subscribe(portfolios => this.portfolios = portfolios);
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
}
