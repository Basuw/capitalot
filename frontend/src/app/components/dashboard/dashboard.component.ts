import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { StatsService } from '../../services/stats.service';
import { PortfolioService } from '../../services/portfolio.service';
import { PerformanceStats, Portfolio } from '../../models/models';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  template: `
    <div class="dashboard">
      <div class="welcome-section">
        <h1>Bienvenue sur Capitalot</h1>
        <p class="subtitle">Gérez vos investissements avec élégance</p>
      </div>

      <div class="chart-section">
        <div class="section-header">
          <h2>Performance de vos portefeuilles</h2>
          <div class="period-selector">
            <button 
              *ngFor="let period of periods" 
              [class.active]="selectedPeriod === period.value"
              (click)="changePeriod(period.value)">
              {{ period.label }}
            </button>
          </div>
        </div>
        <div class="chart-container">
          <canvas #performanceChart></canvas>
        </div>
      </div>

      <div class="quick-actions">
        <a routerLink="/stocks" class="action-card primary">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          <span>Rechercher des actions</span>
        </a>
        <button (click)="createPortfolio()" class="action-card secondary">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          <span>Nouveau portefeuille</span>
        </button>
      </div>
      
      <div class="stats-container">
        <div class="stat-card" style="animation-delay: 0.1s">
          <div class="stat-icon today">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
          </div>
          <h3>Aujourd'hui</h3>
          <div class="stat-value" [class.positive]="dailyStats.totalGainLoss >= 0" 
               [class.negative]="dailyStats.totalGainLoss < 0">
            {{ dailyStats.totalGainLoss | number:'1.2-2' }} €
          </div>
          <div class="stat-badge" [class.positive]="dailyStats.totalGainLossPercent >= 0" 
               [class.negative]="dailyStats.totalGainLossPercent < 0">
            {{ dailyStats.totalGainLossPercent >= 0 ? '+' : '' }}{{ dailyStats.totalGainLossPercent | number:'1.2-2' }}%
          </div>
        </div>
        
        <div class="stat-card" style="animation-delay: 0.2s">
          <div class="stat-icon month">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
          </div>
          <h3>Ce mois</h3>
          <div class="stat-value" [class.positive]="monthlyStats.totalGainLoss >= 0" 
               [class.negative]="monthlyStats.totalGainLoss < 0">
            {{ monthlyStats.totalGainLoss | number:'1.2-2' }} €
          </div>
          <div class="stat-badge" [class.positive]="monthlyStats.totalGainLossPercent >= 0" 
               [class.negative]="monthlyStats.totalGainLossPercent < 0">
            {{ monthlyStats.totalGainLossPercent >= 0 ? '+' : '' }}{{ monthlyStats.totalGainLossPercent | number:'1.2-2' }}%
          </div>
        </div>
        
        <div class="stat-card" style="animation-delay: 0.3s">
          <div class="stat-icon year">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
            </svg>
          </div>
          <h3>Cette année</h3>
          <div class="stat-value" [class.positive]="yearlyStats.totalGainLoss >= 0" 
               [class.negative]="yearlyStats.totalGainLoss < 0">
            {{ yearlyStats.totalGainLoss | number:'1.2-2' }} €
          </div>
          <div class="stat-badge" [class.positive]="yearlyStats.totalGainLossPercent >= 0" 
               [class.negative]="yearlyStats.totalGainLossPercent < 0">
            {{ yearlyStats.totalGainLossPercent >= 0 ? '+' : '' }}{{ yearlyStats.totalGainLossPercent | number:'1.2-2' }}%
          </div>
        </div>
        
        <div class="stat-card total" style="animation-delay: 0.4s">
          <div class="stat-icon total-icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="1" x2="12" y2="23"></line>
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
            </svg>
          </div>
          <h3>Performance totale</h3>
          <div class="stat-value" [class.positive]="allTimeStats.totalGainLoss >= 0" 
               [class.negative]="allTimeStats.totalGainLoss < 0">
            {{ allTimeStats.totalGainLoss | number:'1.2-2' }} €
          </div>
          <div class="stat-badge" [class.positive]="allTimeStats.totalGainLossPercent >= 0" 
               [class.negative]="allTimeStats.totalGainLossPercent < 0">
            {{ allTimeStats.totalGainLossPercent >= 0 ? '+' : '' }}{{ allTimeStats.totalGainLossPercent | number:'1.2-2' }}%
          </div>
          <div class="stat-details">
            <div class="detail-item">
              <span class="label">Investi</span>
              <span class="value">{{ allTimeStats.totalInvested | number:'1.2-2' }} €</span>
            </div>
            <div class="detail-item">
              <span class="label">Valeur actuelle</span>
              <span class="value">{{ allTimeStats.currentValue | number:'1.2-2' }} €</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="portfolios-section">
        <div class="section-header">
          <h2>Mes portefeuilles</h2>
          <button (click)="createPortfolio()" class="btn-create">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            Créer un portefeuille
          </button>
        </div>
        <div class="portfolios-grid">
          <div class="portfolio-card" *ngFor="let portfolio of portfolios; let i = index" 
               [style.animation-delay]="(i * 0.1) + 's'">
            <div class="portfolio-header">
              <div class="portfolio-icon">
                <span *ngIf="portfolio.icon" class="emoji-icon">{{ portfolio.icon }}</span>
                <svg *ngIf="!portfolio.icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                  <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                </svg>
              </div>
              <h3>{{ portfolio.name }}</h3>
            </div>
            <p class="portfolio-description">{{ portfolio.description }}</p>
            <a [routerLink]="['/portfolios', portfolio.id]" class="btn-view">
              Voir les détails
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="5" y1="12" x2="19" y2="12"></line>
                <polyline points="12 5 19 12 12 19"></polyline>
              </svg>
            </a>
          </div>
          <div class="portfolio-card add-card" *ngIf="portfolios.length === 0">
            <div class="empty-state">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
              </svg>
              <p>Aucun portefeuille pour le moment</p>
              <button (click)="createPortfolio()" class="btn-add">Créer mon premier portefeuille</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard {
      max-width: 1400px;
      margin: 0 auto;
      animation: fadeIn 0.6s ease-out;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(20px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .welcome-section {
      text-align: center;
      margin-bottom: 3rem;
      padding: 3rem 0;
    }

    .welcome-section h1 {
      font-size: 3.5rem;
      font-weight: 700;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin-bottom: 0.5rem;
      animation: slideDown 0.8s ease-out;
    }

    @keyframes slideDown {
      from { opacity: 0; transform: translateY(-30px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .subtitle {
      color: #6b7280;
      font-size: 1.3rem;
      font-weight: 300;
    }

    .chart-section {
      background: white;
      border-radius: 20px;
      padding: 2rem;
      margin-bottom: 3rem;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
      animation: slideUp 0.6s ease-out;
    }

    .chart-container {
      position: relative;
      height: 400px;
      margin-top: 2rem;
    }

    .period-selector {
      display: flex;
      gap: 0.5rem;
      background: #f3f4f6;
      padding: 0.5rem;
      border-radius: 12px;
    }

    .period-selector button {
      padding: 0.6rem 1.2rem;
      border: none;
      background: transparent;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
      color: #6b7280;
      transition: all 0.3s ease;
    }

    .period-selector button:hover {
      background: rgba(102, 126, 234, 0.1);
      color: #667eea;
    }

    .period-selector button.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
    }

    .quick-actions {
      display: flex;
      gap: 1rem;
      max-width: 800px;
      margin: 0 auto 4rem;
    }

    .action-card {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.8rem;
      padding: 1.5rem 2rem;
      border-radius: 16px;
      border: none;
      cursor: pointer;
      font-size: 1.1rem;
      font-weight: 600;
      transition: all 0.3s ease;
      text-decoration: none;
    }

    .action-card.primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      box-shadow: 0 10px 25px -5px rgba(102, 126, 234, 0.4);
    }

    .action-card.primary:hover {
      transform: translateY(-3px);
      box-shadow: 0 15px 30px -5px rgba(102, 126, 234, 0.5);
    }

    .action-card.secondary {
      background: white;
      color: #667eea;
      border: 2px solid #667eea;
    }

    .action-card.secondary:hover {
      background: #f5f7ff;
      transform: translateY(-3px);
    }

    .action-card svg {
      width: 24px;
      height: 24px;
    }
    
    .stats-container {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1.5rem;
      margin-bottom: 4rem;
    }
    
    .stat-card {
      background: white;
      padding: 2rem;
      border-radius: 20px;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
      transition: all 0.3s ease;
      position: relative;
      overflow: hidden;
      animation: slideUp 0.6s ease-out backwards;
    }

    @keyframes slideUp {
      from { opacity: 0; transform: translateY(30px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .stat-card::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 4px;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    }

    .stat-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
    }

    .stat-card.total {
      grid-column: 1 / -1;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .stat-card.total::before {
      background: rgba(255, 255, 255, 0.3);
    }

    .stat-card.total h3,
    .stat-card.total .stat-value,
    .stat-card.total .stat-badge,
    .stat-card.total .label {
      color: white;
    }

    .stat-card.total .value {
      color: rgba(255, 255, 255, 0.9);
    }

    .stat-icon {
      width: 50px;
      height: 50px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 1rem;
    }

    .stat-icon.today {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }

    .stat-icon.month {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }

    .stat-icon.year {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }

    .stat-icon.total-icon {
      background: rgba(255, 255, 255, 0.2);
    }

    .stat-icon svg {
      width: 24px;
      height: 24px;
      color: white;
    }
    
    .stat-card h3 {
      margin: 0 0 1rem 0;
      color: #6b7280;
      font-size: 0.95rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    
    .stat-value {
      font-size: 2.2rem;
      font-weight: 700;
      margin-bottom: 0.8rem;
      color: #1f2937;
    }
    
    .stat-value.positive {
      color: #10b981;
    }
    
    .stat-value.negative {
      color: #ef4444;
    }

    .stat-badge {
      display: inline-flex;
      align-items: center;
      padding: 0.4rem 0.8rem;
      border-radius: 8px;
      font-weight: 600;
      font-size: 0.9rem;
    }

    .stat-badge.positive {
      background: #d1fae5;
      color: #065f46;
    }

    .stat-badge.negative {
      background: #fee2e2;
      color: #991b1b;
    }

    .stat-details {
      display: flex;
      gap: 2rem;
      margin-top: 1.5rem;
      padding-top: 1.5rem;
      border-top: 1px solid rgba(255, 255, 255, 0.2);
    }

    .detail-item {
      display: flex;
      flex-direction: column;
      gap: 0.3rem;
    }

    .detail-item .label {
      font-size: 0.85rem;
      opacity: 0.9;
    }

    .detail-item .value {
      font-size: 1.2rem;
      font-weight: 600;
    }
    
    .portfolios-section {
      margin-top: 4rem;
    }

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    .section-header h2 {
      font-size: 2rem;
      font-weight: 700;
      color: #1f2937;
      margin: 0;
    }

    .btn-create {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.8rem 1.5rem;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 12px;
      cursor: pointer;
      font-size: 1rem;
      font-weight: 600;
      transition: all 0.3s;
      box-shadow: 0 4px 6px -1px rgba(102, 126, 234, 0.5);
    }

    .btn-create:hover {
      transform: translateY(-2px);
      box-shadow: 0 10px 15px -3px rgba(102, 126, 234, 0.5);
    }

    .btn-create svg {
      width: 20px;
      height: 20px;
    }
    
    .portfolios-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 1.5rem;
    }
    
    .portfolio-card {
      background: white;
      padding: 2rem;
      border-radius: 20px;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
      transition: all 0.3s ease;
      animation: slideUp 0.6s ease-out backwards;
    }

    .portfolio-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
    }

    .portfolio-header {
      display: flex;
      align-items: center;
      gap: 1rem;
      margin-bottom: 1rem;
    }

    .portfolio-icon {
      width: 50px;
      height: 50px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .portfolio-icon svg {
      width: 24px;
      height: 24px;
      color: white;
    }

    .emoji-icon {
      font-size: 2.5rem;
      line-height: 1;
    }
    
    .portfolio-card h3 {
      margin: 0;
      color: #1f2937;
      font-size: 1.5rem;
      font-weight: 700;
    }
    
    .portfolio-description {
      color: #6b7280;
      margin-bottom: 1.5rem;
      line-height: 1.6;
    }
    
    .btn-view {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 0.8rem 1.5rem;
      border: none;
      border-radius: 12px;
      cursor: pointer;
      text-decoration: none;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn-view:hover {
      transform: translateX(5px);
    }

    .btn-view svg {
      width: 18px;
      height: 18px;
    }
    
    .add-card {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 250px;
      border: 2px dashed #d1d5db;
      background: #f9fafb;
    }

    .empty-state {
      text-align: center;
    }

    .empty-state svg {
      width: 64px;
      height: 64px;
      color: #d1d5db;
      margin-bottom: 1rem;
    }

    .empty-state p {
      color: #6b7280;
      margin-bottom: 1.5rem;
      font-size: 1.1rem;
    }
    
    .btn-add {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 1rem 2rem;
      border: none;
      border-radius: 12px;
      cursor: pointer;
      font-size: 1rem;
      font-weight: 600;
      transition: all 0.3s;
    }

      .btn-add:hover {
      transform: translateY(-2px);
      box-shadow: 0 10px 15px -3px rgba(102, 126, 234, 0.5);
    }

    @media (max-width: 768px) {
      .welcome-section h1 {
        font-size: 2.5rem;
      }

      .quick-actions {
        flex-direction: column;
      }

      .stats-container {
        grid-template-columns: 1fr;
      }

      .portfolios-grid {
        grid-template-columns: 1fr;
      }

      .section-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
      }

      .period-selector {
        flex-wrap: wrap;
      }

      .chart-section {
        padding: 1.5rem;
      }

      .chart-container {
        height: 300px;
      }
    }
  `]
})
export class DashboardComponent implements OnInit, AfterViewInit {
  @ViewChild('performanceChart') performanceChartRef!: ElementRef<HTMLCanvasElement>;
  
  dailyStats: PerformanceStats = {} as PerformanceStats;
  monthlyStats: PerformanceStats = {} as PerformanceStats;
  yearlyStats: PerformanceStats = {} as PerformanceStats;
  allTimeStats: PerformanceStats = {} as PerformanceStats;
  portfolios: Portfolio[] = [];
  
  chart: Chart | null = null;
  selectedPeriod: string = '1M';
  periods = [
    { value: '1D', label: 'Jour' },
    { value: '1W', label: 'Semaine' },
    { value: '1M', label: 'Mois' },
    { value: '1Y', label: 'Année' },
    { value: '10Y', label: '10 ans' }
  ];

  constructor(
    private statsService: StatsService,
    private portfolioService: PortfolioService
  ) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadPortfolios();
  }

  ngAfterViewInit(): void {
    this.initChart();
  }

  initChart(): void {
    const ctx = this.performanceChartRef.nativeElement.getContext('2d');
    if (!ctx) return;

    const data = this.generateChartData(this.selectedPeriod);

    this.chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: data.labels,
        datasets: [{
          label: 'Valeur du portefeuille (€)',
          data: data.values,
          borderColor: '#667eea',
          backgroundColor: 'rgba(102, 126, 234, 0.1)',
          borderWidth: 3,
          fill: true,
          tension: 0.4,
          pointRadius: 0,
          pointHoverRadius: 6,
          pointHoverBackgroundColor: '#667eea',
          pointHoverBorderColor: '#fff',
          pointHoverBorderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          intersect: false,
          mode: 'index'
        },
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: 'rgba(0, 0, 0, 0.8)',
            padding: 12,
            titleFont: { size: 14, weight: 'bold' as const },
            bodyFont: { size: 13 },
            displayColors: false,
            callbacks: {
              label: (context) => `${context.parsed.y?.toFixed(2) ?? '0.00'} €`
            }
          }
        },
        scales: {
          y: {
            beginAtZero: false,
            grid: {
              color: 'rgba(0, 0, 0, 0.05)'
            },
            ticks: {
              callback: (value) => `${value} €`,
              font: { size: 12 },
              color: '#6b7280'
            }
          },
          x: {
            grid: {
              display: false
            },
            ticks: {
              font: { size: 12 },
              color: '#6b7280',
              maxRotation: 0
            }
          }
        }
      }
    });
  }

  generateChartData(period: string): { labels: string[], values: number[] } {
    const now = Date.now();
    let points = 30;
    let interval = 24 * 60 * 60 * 1000;
    let baseValue = 10000;

    switch (period) {
      case '1D':
        points = 24;
        interval = 60 * 60 * 1000;
        break;
      case '1W':
        points = 7;
        interval = 24 * 60 * 60 * 1000;
        break;
      case '1M':
        points = 30;
        interval = 24 * 60 * 60 * 1000;
        break;
      case '1Y':
        points = 12;
        interval = 30 * 24 * 60 * 60 * 1000;
        break;
      case '10Y':
        points = 10;
        interval = 365 * 24 * 60 * 60 * 1000;
        break;
    }

    const labels: string[] = [];
    const values: number[] = [];
    
    for (let i = points - 1; i >= 0; i--) {
      const date = new Date(now - i * interval);
      
      if (period === '1D') {
        labels.push(date.getHours() + 'h');
      } else if (period === '1W') {
        labels.push(['Dim', 'Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam'][date.getDay()]);
      } else if (period === '1M') {
        labels.push(date.getDate().toString());
      } else if (period === '1Y') {
        labels.push(['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc'][date.getMonth()]);
      } else {
        labels.push(date.getFullYear().toString());
      }
      
      const variation = (Math.random() - 0.45) * (baseValue * 0.02);
      baseValue += variation;
      values.push(Math.round(baseValue * 100) / 100);
    }

    return { labels, values };
  }

  changePeriod(period: string): void {
    this.selectedPeriod = period;
    if (this.chart) {
      const data = this.generateChartData(period);
      this.chart.data.labels = data.labels;
      this.chart.data.datasets[0].data = data.values;
      this.chart.update();
    }
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
