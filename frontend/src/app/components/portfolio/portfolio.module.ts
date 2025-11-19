import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { PortfolioComponent } from './portfolio.component';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [PortfolioComponent],
  imports: [
    CommonModule,
    FormsModule,
    SharedModule,
    RouterModule.forChild([
      { path: '', component: PortfolioComponent },
      { path: ':id', component: PortfolioComponent }
    ])
  ]
})
export class PortfolioModule { }
