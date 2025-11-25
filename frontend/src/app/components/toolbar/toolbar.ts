import { Component, inject, ElementRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DrawingService } from '../../services/drawing';

@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toolbar.html',
  styleUrls: ['./toolbar.css'],
})
export class ToolbarComponent {
  private drawingService = inject(DrawingService);

  tools = [
    { id: 'line', icon: '╱', label: 'Line' },
    { id: 'circle', icon: '○', label: 'Circle' },
    { id: 'rectangle', icon: '▭', label: 'Rectangle' },
    { id: 'square', icon: '□', label: 'Square' },
    { id: 'triangle', icon: '△', label: 'Triangle' },
    { id: 'ellipse', icon: '⬭', label: 'Ellipse' },
  ];

  colors: string[] = [
    '#000000',
    '#FF0000',
    '#00FF00',
    '#0000FF',
    '#FFFF00',
    '#FFA500',
    '#800080',
    '#FFFFFF',
  ];

  activeTool: string = 'line';
  activeColor: string = '#000000';

  selectTool(toolId: string) {
    this.activeTool = toolId; // Update local state for UI highlighting
    this.drawingService.setTool(toolId); // Update Service
  }

  selectColor(color: string) {
    this.activeColor = color;
    this.drawingService.setColor(color);
  }
  undo() {
    this.drawingService.undo();
  }

  redo() {
    this.drawingService.redo();
  }

  private elementRef = inject(ElementRef);

  isDropdownOpen = false;

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent) {
    if (!this.isDropdownOpen) return;
    const clickedInside = this.elementRef.nativeElement.contains(event.target);
    if (!clickedInside) {
      this.isDropdownOpen = false;
    }
  }

  saveFile(format: string) {
    console.log('Saving as', format);
    this.isDropdownOpen = false;
  }
}
