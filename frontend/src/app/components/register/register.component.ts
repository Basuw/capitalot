import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  template: `
    <div class="register-container">
      <div class="register-card">
        <h1>Créer un compte Capitalot</h1>
        <form (ngSubmit)="register()">
          <div class="form-group">
            <label>Prénom</label>
            <input type="text" [(ngModel)]="form.firstName" name="firstName" required>
          </div>
          <div class="form-group">
            <label>Nom</label>
            <input type="text" [(ngModel)]="form.lastName" name="lastName" required>
          </div>
          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="form.email" name="email" required>
          </div>
          <div class="form-group">
            <label>Mot de passe</label>
            <input type="password" [(ngModel)]="form.password" name="password" required>
          </div>
          <button type="submit" class="btn-primary">S'inscrire</button>
          <div class="error" *ngIf="error">{{ error }}</div>
        </form>
        <p class="login-link">
          Déjà un compte ? <a routerLink="/login">Se connecter</a>
        </p>
      </div>
    </div>
  `,
  styles: [`
    .register-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
    }
    
    .register-card {
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
    
    .btn-primary {
      width: 100%;
      padding: 0.75rem;
      background-color: #2ecc71;
      color: white;
      border: none;
      border-radius: 4px;
      font-size: 1rem;
      cursor: pointer;
      transition: background-color 0.3s;
    }
    
    .btn-primary:hover {
      background-color: #27ae60;
    }
    
    .login-link {
      text-align: center;
      margin-top: 1rem;
    }
    
    .login-link a {
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
export class RegisterComponent {
  form = { email: '', password: '', firstName: '', lastName: '' };
  error = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  register(): void {
    this.authService.register(this.form).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => this.error = 'Erreur lors de l\'inscription'
    });
  }
}
