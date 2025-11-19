import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { WatchlistComponent } from './watchlist.component';

@NgModule({
  declarations: [WatchlistComponent],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild([{ path: '', component: WatchlistComponent }])
  ]
})
export class WatchlistModule { }
