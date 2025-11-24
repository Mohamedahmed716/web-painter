import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DrawingService } from '../../services/drawing';

@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toolbar.html',
  styleUrls: ['./toolbar.css']
})
export class ToolbarComponent {
  private drawingService = inject(DrawingService);

  // 1. Define Tools with Labels (Unicode icons)
  tools = [
    { id: 'line', icon: '╱', label: 'Line' },
    { id: 'circle', icon: '○', label: 'Circle' },
    { id: 'rectangle', icon: '▭', label: 'Rectangle' },
    { id: 'square', icon: '□', label: 'Square' },
    { id: 'triangle', icon: '△', label: 'Triangle' },
    { id: 'ellipse', icon: '⬭', label: 'Ellipse' }
  ];

  // 2. Define Colors
  colors: string[] = ['#000000', '#FF0000', '#00FF00', '#0000FF', '#FFFF00', '#FFA500', '#800080', '#FFFFFF'];

  // State
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
}
