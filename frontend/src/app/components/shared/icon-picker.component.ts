import { Component, Output, EventEmitter, Input } from '@angular/core';

@Component({
  selector: 'app-icon-picker',
  template: `
    <app-modal 
      [isOpen]="isOpen" 
      [title]="'Choisir une icône'"
      (close)="close.emit()">
      <div class="icon-picker">
        <div class="search-box">
          <input 
            type="text" 
            [(ngModel)]="searchQuery"
            (input)="filterIcons()"
            placeholder="🔍 Rechercher une icône..."
            class="search-input">
        </div>
        
        <div class="categories">
          <button 
            *ngFor="let category of categories"
            [class.active]="selectedCategory === category.name"
            (click)="selectCategory(category.name)"
            class="category-btn">
            {{ category.label }}
          </button>
        </div>

        <div class="icons-grid">
          <button 
            *ngFor="let icon of filteredIcons"
            (click)="selectIcon(icon)"
            class="icon-item"
            [title]="icon">
            {{ icon }}
          </button>
        </div>
      </div>
    </app-modal>
  `,
  styles: [`
    .icon-picker {
      max-height: 600px;
      display: flex;
      flex-direction: column;
    }

    .search-box {
      margin-bottom: 1.5rem;
    }

    .search-input {
      width: 100%;
      padding: 0.875rem;
      border: 2px solid #e5e7eb;
      border-radius: 12px;
      font-size: 1rem;
      transition: all 0.3s ease;
    }

    .search-input:focus {
      outline: none;
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }

    .categories {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 1.5rem;
      overflow-x: auto;
      padding-bottom: 0.5rem;
    }

    .categories::-webkit-scrollbar {
      height: 4px;
    }

    .categories::-webkit-scrollbar-thumb {
      background: #d1d5db;
      border-radius: 4px;
    }

    .category-btn {
      padding: 0.6rem 1.2rem;
      border: 2px solid #e5e7eb;
      background: white;
      border-radius: 10px;
      cursor: pointer;
      font-weight: 600;
      color: #6b7280;
      transition: all 0.3s ease;
      white-space: nowrap;
    }

    .category-btn:hover {
      border-color: #667eea;
      color: #667eea;
    }

    .category-btn.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-color: transparent;
    }

    .icons-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
      gap: 0.75rem;
      overflow-y: auto;
      max-height: 400px;
      padding: 0.5rem;
    }

    .icons-grid::-webkit-scrollbar {
      width: 8px;
    }

    .icons-grid::-webkit-scrollbar-thumb {
      background: #d1d5db;
      border-radius: 4px;
    }

    .icon-item {
      width: 100%;
      aspect-ratio: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 2rem;
      border: 2px solid #e5e7eb;
      background: white;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .icon-item:hover {
      transform: scale(1.1);
      border-color: #667eea;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      z-index: 1;
    }

    .icon-item:active {
      transform: scale(0.95);
    }
  `]
})
export class IconPickerComponent {
  @Input() isOpen = false;
  @Output() close = new EventEmitter<void>();
  @Output() iconSelected = new EventEmitter<string>();

  searchQuery = '';
  selectedCategory = 'all';
  filteredIcons: string[] = [];

  categories = [
    { name: 'all', label: 'Tous' },
    { name: 'finance', label: 'Finance' },
    { name: 'charts', label: 'Graphiques' },
    { name: 'objects', label: 'Objets' },
    { name: 'nature', label: 'Nature' },
    { name: 'symbols', label: 'Symboles' }
  ];

  icons = {
    finance: ['💰', '💵', '💴', '💶', '💷', '💳', '💎', '🏦', '📊', '📈', '📉', '💹', '🪙', '💸', '🤑'],
    charts: ['📊', '📈', '📉', '💹', '🎯', '📱', '💻', '⚡', '🔥', '⭐', '✨', '🌟', '💫', '🎨', '🎭'],
    objects: ['🎯', '🏆', '🎓', '💼', '📱', '💻', '⌚', '🔑', '🎁', '📦', '🔧', '⚙️', '🔨', '🛠️', '📌'],
    nature: ['🌍', '🌎', '🌏', '🌱', '🌲', '🌳', '🌴', '🌵', '🌾', '🌿', '☘️', '🍀', '🌺', '🌸', '🌼'],
    symbols: ['⚡', '🔥', '💥', '⭐', '✨', '🌟', '💫', '🔆', '🎯', '🚀', '🛸', '⚓', '🔱', '♾️', '🔰']
  };

  allIcons: string[] = [];

  constructor() {
    this.allIcons = Object.values(this.icons).flat();
    this.filteredIcons = this.allIcons;
  }

  selectCategory(category: string): void {
    this.selectedCategory = category;
    this.filterIcons();
  }

  filterIcons(): void {
    let icons = this.selectedCategory === 'all' 
      ? this.allIcons 
      : this.icons[this.selectedCategory as keyof typeof this.icons] || [];

    if (this.searchQuery) {
      icons = icons;
    }

    this.filteredIcons = icons;
  }

  selectIcon(icon: string): void {
    this.iconSelected.emit(icon);
    this.close.emit();
  }
}
