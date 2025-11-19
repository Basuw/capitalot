import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { 
    path: 'login', 
    loadChildren: () => import('./components/login/login.module').then(m => m.LoginModule)
  },
  { 
    path: 'register', 
    loadChildren: () => import('./components/register/register.module').then(m => m.RegisterModule)
  },
  { 
    path: 'dashboard', 
    loadChildren: () => import('./components/dashboard/dashboard.module').then(m => m.DashboardModule),
    canActivate: [authGuard]
  },
  { 
    path: 'portfolios', 
    loadChildren: () => import('./components/portfolio/portfolio.module').then(m => m.PortfolioModule),
    canActivate: [authGuard]
  },
  { 
    path: 'watchlists', 
    loadChildren: () => import('./components/watchlist/watchlist.module').then(m => m.WatchlistModule),
    canActivate: [authGuard]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
