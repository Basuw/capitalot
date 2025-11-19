import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { StockSearchComponent } from './stock-search.component';

const routes: Routes = [
  { path: '', component: StockSearchComponent }
];

@NgModule({
  declarations: [StockSearchComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class StockSearchModule { }
