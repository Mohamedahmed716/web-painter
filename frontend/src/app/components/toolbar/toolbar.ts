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

  tools = [
    { id: 'select', icon: '🖱️', label: 'Select' },
    { id: 'freehand', icon: '✎', label: 'Freehand' },
    { id: 'line', icon: '╱', label: 'Line' },
    { id: 'circle', icon: '○', label: 'Circle' },
    { id: 'rectangle', icon: '▭', label: 'Rectangle' },
    { id: 'square', icon: '□', label: 'Square' },
    { id: 'triangle', icon: '△', label: 'Triangle' },
    { id: 'ellipse', icon: '⬭', label: 'Ellipse' },
  ];

  activeTool: string = 'line';
  strokeColor: string = '#333333';
  fillColor: string = '#ffffff';
  strokeWidth: number = 2;
  isDropdownOpen = false;

  selectTool(toolId: string) {
    this.activeTool = toolId;
    this.drawingService.setTool(toolId);
  }

  onStrokeColorChange() {
    this.drawingService.setColor(this.strokeColor);
    this.api.updateColor(this.strokeColor).subscribe((shapes) => {
      this.drawingService.colorChange$.next(shapes);
    });
  }

  onFillColorChange() {
    console.log('Fill color changed:', this.fillColor);
  }

  undo() {
    this.drawingService.undo();
  }
  redo() {
    this.drawingService.redo();
  }

  deleteSelected() {
    this.drawingService.triggerAction('delete');
  }
  resizeSelected() {
    this.drawingService.setTool('resize');
  }
  copySelected() {
    this.drawingService.copy$.next();
  }

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent) {
    if (!this.isDropdownOpen) return;
    const clickedInside = this.elementRef.nativeElement.contains(event.target);
    if (!clickedInside) this.isDropdownOpen = false;
  }

  saveFile(format: string) {
    // Delegate download to API service
    this.api.save(format);
    this.isDropdownOpen = false;
  }

  // Handle File Upload
  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.api.load(file).subscribe({
        next: (response) => {
          console.log(response);
          // Force refresh
          window.location.reload();
        },
        error: (err) => console.error('Load failed', err),
      });
    }
  }
}
