import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  template: `
    <div class="login-container">
      <div class="login-card">
        <h1>Connexion à Capitalot</h1>
        <form (ngSubmit)="login()">
          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="credentials.email" name="email" required>
          </div>
          <div class="form-group">
            <label>Mot de passe</label>
            <input type="password" [(ngModel)]="credentials.password" name="password" required>
          </div>
          <button type="submit" class="btn-primary">Se connecter</button>
          <div class="error" *ngIf="error">{{ error }}</div>
        </form>
        <div class="divider">ou</div>
        <button class="btn-google">Se connecter avec Google</button>
        <p class="register-link">
          Pas encore de compte ? <a routerLink="/register">S'inscrire</a>
        </p>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
    }
    
    .login-card {
      background: white;
      padding: 2rem;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      width: 100%;
      max-width: 400px;
    }
    
    h1 {
      text-align: center;
      margin-bottom: 2rem;
      color: #2c3e50;
    }
    
    .form-group {
      margin-bottom: 1rem;
    }
    
    .form-group label {
      display: block;
      margin-bottom: 0.5rem;
      color: #555;
    }
    
    .form-group input {
      width: 100%;
      padding: 0.75rem;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 1rem;
    }
    
    .btn-primary, .btn-google {
      width: 100%;
      padding: 0.75rem;
      border: none;
      border-radius: 4px;
      font-size: 1rem;
      cursor: pointer;
      transition: background-color 0.3s;
    }
    
    .btn-primary {
      background-color: #3498db;
      color: white;
      margin-bottom: 1rem;
    }
    
    .btn-primary:hover {
      background-color: #2980b9;
    }
    
    .btn-google {
      background-color: #db4437;
      color: white;
    }
    
    .btn-google:hover {
      background-color: #c23321;
    }
    
    .divider {
      text-align: center;
      margin: 1rem 0;
      color: #999;
    }
    
    .register-link {
      text-align: center;
      margin-top: 1rem;
    }
    
    .register-link a {
      color: #3498db;
      text-decoration: none;
    }
    
    .error {
      color: #e74c3c;
      margin-top: 1rem;
      text-align: center;
    }
  `]
})
export class LoginComponent {
  credentials = { email: '', password: '' };
  error = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  login(): void {
    this.authService.login(this.credentials).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => this.error = 'Email ou mot de passe incorrect'
    });
  }
}
