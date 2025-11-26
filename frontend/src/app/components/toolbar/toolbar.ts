import { Component, inject, ElementRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DrawingService } from '../../services/drawing';
import { ApiService } from '../../services/api';


@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './toolbar.html',
  styleUrls: ['./toolbar.css'],
})
export class ToolbarComponent {
  private drawingService = inject(DrawingService);
  private elementRef = inject(ElementRef);
  private api = inject(ApiService);              

  // Added 'freehand' tool to the list
  tools = [
    { id: 'select', icon: '🖱️', label: 'Select' },
    { id: 'freehand', icon: '✎', label: 'Freehand' }, // The new cursor tool
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
  strokeColor: string = '#333333';
  fillColor: string = '#ffffff';
  strokeWidth: number = 2;

  // State
  isDropdownOpen = false;

  selectTool(toolId: string) {
    this.activeTool = toolId;
    this.drawingService.setTool(toolId);
  }

  // --- Properties Logic ---
 onStrokeColorChange() {
  // Update drawing color for new shapes
  this.drawingService.setColor(this.strokeColor);

  // Update selected shape color
  this.api.updateColor(this.strokeColor).subscribe(shapes => {
    this.drawingService.colorChange$.next(shapes);
  });
}




  onFillColorChange() {
    // Implement fill logic in service if needed
    console.log('Fill color changed:', this.fillColor);
  }

  onPropertyChange(prop: string, value: any) {
    console.log('Property changed:', prop, value);
    // You can extend DrawingService to handle width
  }

  // --- Actions ---
  undo() {
    this.drawingService.undo();
  }

  redo() {
    this.drawingService.redo();
  }

  triggerAction(action: string) {
    console.log('Action triggered:', action);
  }

  // --- Dropdown Logic ---
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
 deleteSelected() {
  this.drawingService.triggerAction('delete');
}

resizeSelected() {
  this.drawingService.setTool('resize');
}
copySelected() {
  this.drawingService.copy$.next();     // trigger board to start paste mode
}




}
