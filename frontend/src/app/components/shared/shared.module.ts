import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from './modal.component';
import { IconPickerComponent } from './icon-picker.component';

@NgModule({
  declarations: [ModalComponent, IconPickerComponent],
  imports: [CommonModule, FormsModule],
  exports: [ModalComponent, IconPickerComponent]
})
export class SharedModule { }
